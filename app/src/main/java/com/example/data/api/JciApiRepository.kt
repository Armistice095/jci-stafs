package com.example.data.api

import android.content.Context
import android.net.Uri
import com.example.BuildConfig
import com.example.data.model.AgentProfile
import com.example.data.model.Counters
import com.example.data.model.Event
import com.example.data.model.ScanRequestBody
import com.example.data.model.ScanResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.UUID
import java.util.concurrent.TimeUnit

/** Erreur de l'API avec son code HTTP (0 = pas de réponse : réseau ou délai dépassé). */
class ApiException(val code: Int, message: String) : Exception(message)

class JciApiRepository(private val context: Context) {

  private val prefs = context.getSharedPreferences("jci_stafs_prefs", Context.MODE_PRIVATE)

  private var retrofitApi: RetrofitJciApi? = null

  var apiBaseUrl: String
    get() {
      val saved = prefs.getString("api_base_url", null)
      if (!saved.isNullOrBlank()) return saved
      val config = BuildConfig.API_BASE_URL
      return if (config.isNotBlank()) {
        if (config.endsWith("/")) config else "$config/"
      } else {
        "https://api.jcicomee.org/"
      }
    }
    set(value) {
      val normalized = if (value.endsWith("/")) value else "$value/"
      prefs.edit().putString("api_base_url", normalized).apply()
      retrofitApi = null
    }

  var authToken: String?
    get() = prefs.getString("auth_token", null)
    set(value) = prefs.edit().putString("auth_token", value).apply()

  var refreshToken: String?
    get() = prefs.getString("refresh_token", null)
    set(value) = prefs.edit().putString("refresh_token", value).apply()

  var lastSelectedEventId: String?
    get() = prefs.getString("last_event_id", null)
    set(value) = prefs.edit().putString("last_event_id", value).apply()

  var soundEnabled: Boolean
    get() = prefs.getBoolean("sound_enabled", true)
    set(value) = prefs.edit().putBoolean("sound_enabled", value).apply()

  var vibrationEnabled: Boolean
    get() = prefs.getBoolean("vibration_enabled", true)
    set(value) = prefs.edit().putBoolean("vibration_enabled", value).apply()

  private val okHttpClient: OkHttpClient by lazy {
    val logging = HttpLoggingInterceptor().apply {
      level = HttpLoggingInterceptor.Level.BASIC
    }
    OkHttpClient.Builder()
      .connectTimeout(5, TimeUnit.SECONDS)
      .readTimeout(5, TimeUnit.SECONDS)
      .writeTimeout(5, TimeUnit.SECONDS)
      .addInterceptor(logging)
      .build()
  }

  private fun getRetrofitService(): RetrofitJciApi {
    retrofitApi?.let { return it }

    val moshi = Moshi.Builder()
      .add(KotlinJsonAdapterFactory())
      .build()

    val retrofit = Retrofit.Builder()
      .baseUrl(apiBaseUrl)
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .build()

    val api = retrofit.create(RetrofitJciApi::class.java)
    retrofitApi = api
    return api
  }

  /**
   * Authenticates using Supabase Auth endpoint:
   * POST {SUPABASE_URL}/auth/v1/token?grant_type=password
   * Headers: apikey: {SUPABASE_ANON_KEY}, Content-Type: application/json
   */
  suspend fun login(email: String, password: String): Result<AgentProfile> = withContext(Dispatchers.IO) {
    val supabaseUrl = BuildConfig.SUPABASE_URL.trim().removeSuffix("/")
    val supabaseAnonKey = BuildConfig.SUPABASE_ANON_KEY.trim()

    try {
      val authEndpoint = "$supabaseUrl/auth/v1/token?grant_type=password"
      val payload = JSONObject().apply {
        put("email", email)
        put("password", password)
      }.toString()

      val request = Request.Builder()
        .url(authEndpoint)
        .addHeader("apikey", supabaseAnonKey)
        .addHeader("Content-Type", "application/json")
        .post(payload.toRequestBody("application/json".toMediaType()))
        .build()

      val response = withTimeoutOrNull(5000L) {
        okHttpClient.newCall(request).execute()
      } ?: return@withContext Result.failure(ApiException(0, "Connexion trop lente. Réessayez."))

      val responseBody = response.body?.string() ?: ""

      if (response.isSuccessful) {
        val json = JSONObject(responseBody)
        val accessToken = json.optString("access_token", "")
        val newRefreshToken = json.optString("refresh_token", "")
        authToken = accessToken
        if (newRefreshToken.isNotBlank()) {
          refreshToken = newRefreshToken
        }

        // Le profil réel vient de GET /me. En cas d'échec, la connexion échoue : aucun profil n'est inventé.
        val profileResult = getMe()
        if (profileResult.isFailure) {
          logout()
        }
        return@withContext profileResult
      } else {
        val errorMsg = try {
          val errJson = JSONObject(responseBody)
          errJson.optString("error_description", errJson.optString("msg", ""))
        } catch (_: Exception) {
          ""
        }.let { raw ->
          if (raw.isBlank() || raw.contains("invalid", ignoreCase = true)) "E-mail ou mot de passe incorrect." else raw
        }
        return@withContext Result.failure(Exception(errorMsg))
      }
    } catch (e: Exception) {
      return@withContext Result.failure(ApiException(0, "Pas de connexion. Vérifiez votre réseau."))
    }
  }

  suspend fun refreshSession(): Boolean = withContext(Dispatchers.IO) {
    val supabaseUrl = BuildConfig.SUPABASE_URL.trim().removeSuffix("/")
    val supabaseAnonKey = BuildConfig.SUPABASE_ANON_KEY.trim()
    val token = refreshToken ?: return@withContext false

    try {
      val authEndpoint = "$supabaseUrl/auth/v1/token?grant_type=refresh_token"
      val payload = JSONObject().apply {
        put("refresh_token", token)
      }.toString()

      val request = Request.Builder()
        .url(authEndpoint)
        .addHeader("apikey", supabaseAnonKey)
        .addHeader("Content-Type", "application/json")
        .post(payload.toRequestBody("application/json".toMediaType()))
        .build()

      val response = withTimeoutOrNull(5000L) {
        okHttpClient.newCall(request).execute()
      } ?: return@withContext false

      if (response.isSuccessful) {
        val json = JSONObject(response.body?.string() ?: "")
        val newAccessToken = json.optString("access_token", "")
        val newRefreshToken = json.optString("refresh_token", "")
        if (newAccessToken.isNotBlank()) {
          authToken = newAccessToken
        }
        if (newRefreshToken.isNotBlank()) {
          refreshToken = newRefreshToken
        }
        true
      } else {
        false
      }
    } catch (_: Exception) {
      false
    }
  }

  private fun httpError(code: Int): ApiException = when (code) {
    401 -> ApiException(401, "Votre session a expiré. Reconnectez-vous.")
    403 -> ApiException(403, "Votre compte n'est pas autorisé à contrôler les entrées.")
    404 -> ApiException(404, "Événement introuvable ou hors de votre périmètre.")
    else -> ApiException(code, "Le serveur ne répond pas correctement (erreur $code).")
  }

  /** Appelle le serveur ; sur 401, rafraîchit le jeton et rejoue UNE seule fois. */
  private suspend fun <T> authorized(call: suspend (String) -> retrofit2.Response<T>): Result<T> {
    var token = authToken ?: return Result.failure(ApiException(401, "Non authentifié"))
    return try {
      var resp = withTimeoutOrNull(5000L) { call("Bearer $token") }
        ?: return Result.failure(ApiException(0, "Délai d'attente dépassé"))
      if (resp.code() == 401 && refreshSession()) {
        token = authToken ?: return Result.failure(ApiException(401, "Non authentifié"))
        resp = withTimeoutOrNull(5000L) { call("Bearer $token") }
          ?: return Result.failure(ApiException(0, "Délai d'attente dépassé"))
      }
      val body = resp.body()
      if (resp.isSuccessful && body != null) Result.success(body) else Result.failure(httpError(resp.code()))
    } catch (e: Exception) {
      Result.failure(ApiException(0, "Pas de connexion"))
    }
  }

  suspend fun getMe(): Result<AgentProfile> =
    authorized { bearer -> getRetrofitService().getMe(bearer) }

  suspend fun getEvents(): Result<List<Event>> =
    authorized { bearer -> getRetrofitService().getEvents(bearer) }.map { it.data }

  suspend fun getEventStats(eventId: String): Result<Counters> =
    authorized { bearer -> getRetrofitService().getEventStats(bearer, eventId) }

  /**
   * Extracts qrToken from raw QR scan text according to Prompt Section 4:
   * "Contenu du QR code : une adresse du type https://.../billet/<qrToken>.
   * L'application doit extraire le qrToken = dernier segment du chemin de l'adresse (sans paramètres ni / final).
   * Si le QR ne contient pas d'adresse valide, afficher directement « BILLET INCONNU » sans appeler le serveur."
   * Also accepts direct ticket identifiers (e.g. JCI-2026-000123).
   */
  fun extractQrToken(rawQr: String): String? {
    val trimmed = rawQr.trim()
    if (trimmed.isEmpty()) return null

    if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
      return try {
        val uri = Uri.parse(trimmed)
        val pathSegments = uri.pathSegments
        if (pathSegments.isNullOrEmpty()) {
          null
        } else {
          val last = pathSegments.lastOrNull()?.trim()
          if (!last.isNullOrEmpty()) last else null
        }
      } catch (_: Exception) {
        null
      }
    }

    if (trimmed.startsWith("jci-", ignoreCase = true) || trimmed.startsWith("JCI-", ignoreCase = true)) {
      return trimmed
    }

    return null
  }

  suspend fun scanTicket(
    eventId: String,
    qrToken: String? = null,
    ticketNumber: String? = null,
    idempotencyKey: String
  ): Result<ScanResponse> {
    val body = ScanRequestBody(
      eventId = eventId,
      qrToken = qrToken,
      ticketNumber = ticketNumber,
      appVersion = BuildConfig.VERSION_NAME
    )

    return authorized { bearer -> getRetrofitService().scanTicket(bearer, idempotencyKey, body) }
  }

  fun logout() {
    authToken = null
    refreshToken = null
    prefs.edit().remove("auth_token").remove("refresh_token").apply()
  }
}
