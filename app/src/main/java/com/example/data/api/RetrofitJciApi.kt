package com.example.data.api

import com.example.data.model.AgentProfile
import com.example.data.model.Counters
import com.example.data.model.EventsPage
import com.example.data.model.ScanRequestBody
import com.example.data.model.ScanResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RetrofitJciApi {

  @GET("me")
  suspend fun getMe(
    @Header("Authorization") token: String
  ): Response<AgentProfile>

  @GET("events")
  suspend fun getEvents(
    @Header("Authorization") token: String,
    @Query("status") status: String = "PUBLISHED",
    @Query("upcoming") upcoming: Boolean = true,
    @Query("sort") sort: String = "startsAt",
    @Query("pageSize") pageSize: Int = 50
  ): Response<EventsPage>

  @GET("events/{eventId}/checkin/stats")
  suspend fun getEventStats(
    @Header("Authorization") token: String,
    @Path("eventId") eventId: String
  ): Response<Counters>

  @POST("checkin/scan")
  suspend fun scanTicket(
    @Header("Authorization") token: String,
    @Header("Idempotency-Key") idempotencyKey: String,
    @Body body: ScanRequestBody
  ): Response<ScanResponse>
}
