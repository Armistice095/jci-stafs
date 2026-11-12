package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

enum class CheckinResultStatus {
  ADMITTED,
  ALREADY_USED,
  WRONG_EVENT,
  CANCELLED,
  EVENT_NOT_ACTIVE,
  INVALID,
  NETWORK_ERROR
}

@JsonClass(generateAdapter = true)
data class TicketInfo(
  @Json(name = "number") val number: String? = null,
  // null pour WRONG_EVENT
  @Json(name = "holderName") val holderName: String? = null,
  // null pour WRONG_EVENT
  @Json(name = "category") val category: String? = null,
  @Json(name = "eventTitle") val eventTitle: String? = null
)

@JsonClass(generateAdapter = true)
data class Counters(
  @Json(name = "admitted") val admitted: Int,
  @Json(name = "capacity") val capacity: Int
)

@JsonClass(generateAdapter = true)
data class ScanResponse(
  @Json(name = "result") val result: String,
  @Json(name = "ticket") val ticket: TicketInfo? = null,
  @Json(name = "firstCheckedInAt") val firstCheckedInAt: String? = null,
  @Json(name = "firstCheckedInBy") val firstCheckedInBy: String? = null,
  @Json(name = "reason") val reason: String? = null,
  @Json(name = "counters") val counters: Counters? = null
) {
  /** Un résultat inconnu n'est jamais deviné : il est traité comme « vérification impossible ». */
  val status: CheckinResultStatus
    get() = when (result.uppercase()) {
      "ADMITTED" -> CheckinResultStatus.ADMITTED
      "ALREADY_USED" -> CheckinResultStatus.ALREADY_USED
      "WRONG_EVENT" -> CheckinResultStatus.WRONG_EVENT
      "CANCELLED" -> CheckinResultStatus.CANCELLED
      "EVENT_NOT_ACTIVE" -> CheckinResultStatus.EVENT_NOT_ACTIVE
      "INVALID" -> CheckinResultStatus.INVALID
      else -> CheckinResultStatus.NETWORK_ERROR
    }
}

@JsonClass(generateAdapter = true)
data class ScanRequestBody(
  @Json(name = "eventId") val eventId: String,
  @Json(name = "qrToken") val qrToken: String? = null,
  @Json(name = "ticketNumber") val ticketNumber: String? = null,
  @Json(name = "appVersion") val appVersion: String? = null
)

@JsonClass(generateAdapter = true)
data class Event(
  @Json(name = "id") val id: String,
  @Json(name = "title") val title: String,
  @Json(name = "startsAt") val startsAt: String? = null,
  @Json(name = "venueName") val venueName: String? = null,
  @Json(name = "city") val city: String? = null,
  @Json(name = "coverUrl") val coverUrl: String? = null
) {
  /** « Salle polyvalente de la Mairie, Comé » (les parties absentes sont ignorées). */
  val placeLabel: String
    get() = listOfNotNull(venueName?.trim(), city?.trim())
      .filter { it.isNotEmpty() }
      .joinToString(", ")
}

/** Réponse paginée de GET /events : { data: [...], meta: {...} } */
@JsonClass(generateAdapter = true)
data class EventsPage(
  @Json(name = "data") val data: List<Event> = emptyList()
)

@JsonClass(generateAdapter = true)
data class RoleGrant(
  @Json(name = "id") val id: String? = null,
  @Json(name = "roleCode") val roleCode: String? = null,
  @Json(name = "eventId") val eventId: String? = null
)

/** Réponse de GET /me. */
@JsonClass(generateAdapter = true)
data class AgentProfile(
  @Json(name = "id") val id: String? = null,
  @Json(name = "email") val email: String? = null,
  @Json(name = "firstName") val firstName: String? = null,
  @Json(name = "lastName") val lastName: String? = null,
  @Json(name = "roles") val roles: List<RoleGrant>? = null,
  @Json(name = "permissions") val permissions: List<String>? = null,
  @Json(name = "eventPermissions") val eventPermissions: Map<String, List<String>>? = null
) {
  /** Droit de contrôler : permission globale, ou permission donnée sur au moins un événement. */
  val canScan: Boolean
    get() = permissions?.contains("checkin.scan") == true ||
      eventPermissions?.values?.any { it.contains("checkin.scan") } == true

  val userFirstName: String
    get() = firstName?.trim().orEmpty()

  val fullDisplayName: String
    get() {
      val full = listOfNotNull(firstName?.trim(), lastName?.trim())
        .filter { it.isNotEmpty() }
        .joinToString(" ")
      if (full.isNotEmpty()) return full
      return email?.substringBefore("@")?.trim().orEmpty()
    }

  /** Rôle principal, en français, pour le menu. */
  val displayRole: String
    get() {
      val codes = roles?.mapNotNull { it.roleCode } ?: emptyList()
      val order = listOf("ADMIN", "PRESIDENT", "ORGANIZER", "GATE_AGENT", "SELLER")
      val code = order.firstOrNull { it in codes } ?: codes.firstOrNull()
      return when (code) {
        "ADMIN" -> "Administrateur"
        "PRESIDENT" -> "Président"
        "ORGANIZER" -> "Organisateur"
        "GATE_AGENT" -> "Agent d'accueil"
        "SELLER" -> "Vendeur"
        else -> "Accueil et contrôle"
      }
    }

  val initials: String
    get() {
      val first = firstName?.trim()?.firstOrNull()
      val last = lastName?.trim()?.firstOrNull()
      if (first != null && last != null) return "$first$last".uppercase()
      val parts = fullDisplayName.split(" ", ".", "_", "-").filter { it.isNotBlank() }
      return when {
        parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> ""
      }
    }
}

data class ScanHistoryItem(
  val id: String,
  val timestamp: Long,
  val ticketNumber: String,
  val holderName: String,
  val category: String,
  val status: CheckinResultStatus,
  val note: String? = null
)
