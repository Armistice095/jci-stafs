package com.example

import com.example.data.model.AgentProfile
import com.example.data.model.CheckinResultStatus
import com.example.data.model.Counters
import com.example.data.model.RoleGrant
import com.example.data.model.ScanResponse
import com.example.data.model.TicketInfo
import com.example.util.DateFormats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testAgentCanScanWithGlobalPermission() {
    val agent = AgentProfile(
      id = "agent-1",
      email = "marc.k@jci-benin.org",
      firstName = "Marc",
      lastName = "Kouassi",
      roles = listOf(RoleGrant(id = "g1", roleCode = "GATE_AGENT", eventId = null)),
      permissions = listOf("checkin.scan", "event.read")
    )
    assertTrue(agent.canScan)
    assertEquals("Marc Kouassi", agent.fullDisplayName)
    assertEquals("Marc", agent.userFirstName)
    assertEquals("MK", agent.initials)
    assertEquals("Agent d'accueil", agent.displayRole)
  }

  @Test
  fun testAgentCanScanWithEventPermissionOnly() {
    // Un agent d'accueil assigné à un événement n'a pas la permission en global.
    val agent = AgentProfile(
      firstName = "Awa",
      lastName = "Dossou",
      permissions = listOf("event.read"),
      eventPermissions = mapOf("event-1" to listOf("checkin.scan"))
    )
    assertTrue(agent.canScan)
  }

  @Test
  fun testAgentCannotScanWithoutPermission() {
    val agent = AgentProfile(
      firstName = "Invité",
      lastName = "Test",
      permissions = listOf("event.read"),
      eventPermissions = emptyMap()
    )
    assertFalse(agent.canScan)
    assertFalse(AgentProfile().canScan)
  }

  @Test
  fun testScanResponseStatusMapping() {
    val admitted = ScanResponse(
      result = "ADMITTED",
      ticket = TicketInfo("JCI-2026-000123", "Armistice ANAGONOU", "VIP", "Soirée de Gala"),
      counters = Counters(342, 380)
    )
    assertEquals(CheckinResultStatus.ADMITTED, admitted.status)
    assertEquals("Armistice ANAGONOU", admitted.ticket?.holderName)
    assertEquals(342, admitted.counters?.admitted)

    assertEquals(CheckinResultStatus.ALREADY_USED, ScanResponse(result = "ALREADY_USED").status)
    assertEquals(CheckinResultStatus.CANCELLED, ScanResponse(result = "CANCELLED").status)
    assertEquals(CheckinResultStatus.INVALID, ScanResponse(result = "INVALID").status)
    assertEquals(CheckinResultStatus.EVENT_NOT_ACTIVE, ScanResponse(result = "EVENT_NOT_ACTIVE").status)

    // WRONG_EVENT : holderName et category sont null côté serveur.
    val wrongEvent = ScanResponse(
      result = "WRONG_EVENT",
      ticket = TicketInfo(number = "JCI-2026-000009", holderName = null, category = null, eventTitle = "Académie Locale")
    )
    assertEquals(CheckinResultStatus.WRONG_EVENT, wrongEvent.status)
    assertNull(wrongEvent.ticket?.holderName)
  }

  @Test
  fun testUnknownResultIsNeverGuessed() {
    assertEquals(CheckinResultStatus.NETWORK_ERROR, ScanResponse(result = "SOMETHING_NEW").status)
  }

  @Test
  fun testDateFormatsUseBeninTime() {
    val iso = "2026-11-28T19:00:00+01:00"
    assertEquals("19:00", DateFormats.hour(iso))
    assertEquals("28", DateFormats.dayBadge(iso))
    assertEquals("NOV", DateFormats.monthBadge(iso))
    assertEquals("2026", DateFormats.year(iso))
    // Même instant exprimé en UTC : l'heure affichée reste celle du Bénin.
    assertEquals("19:00", DateFormats.hour("2026-11-28T18:00:00.000Z"))
    assertEquals("", DateFormats.hour(null))
    assertNull(DateFormats.elapsedLabel("n'importe quoi"))
  }
}
