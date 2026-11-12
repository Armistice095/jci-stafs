package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Formats de date de l'application. Toutes les heures sont affichées à l'heure du Bénin (UTC+1). */
object DateFormats {
  private val beninZone: TimeZone = TimeZone.getTimeZone("Africa/Porto-Novo")
  private val fr: Locale = Locale.FRANCE

  fun parse(iso: String?): Date? {
    if (iso.isNullOrBlank()) return null
    val cleaned = iso.trim().replace(Regex("(\\.\\d{3})\\d+"), "$1")
    val patterns = listOf("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", "yyyy-MM-dd'T'HH:mm:ssXXX")
    for (pattern in patterns) {
      try {
        val parser = SimpleDateFormat(pattern, Locale.US)
        parser.isLenient = false
        val parsed = parser.parse(cleaned)
        if (parsed != null) return parsed
      } catch (_: Exception) {
      }
    }
    return null
  }

  private fun format(date: Date, pattern: String): String {
    val formatter = SimpleDateFormat(pattern, fr)
    formatter.timeZone = beninZone
    return formatter.format(date)
  }

  private fun capitalize(text: String): String =
    text.replaceFirstChar { if (it.isLowerCase()) it.titlecase(fr) else it.toString() }

  /** « NOV » */
  fun monthBadge(iso: String?): String {
    val date = parse(iso) ?: return "—"
    return format(date, "MMM").replace(".", "").uppercase(fr)
  }

  /** « 2026 » (année de l'événement, à l'heure du Bénin) ; année courante si la date est absente. */
  fun year(iso: String?): String {
    val date = parse(iso) ?: Date()
    return format(date, "yyyy")
  }

  /** « 28 » */
  fun dayBadge(iso: String?): String {
    val date = parse(iso) ?: return "—"
    return format(date, "dd")
  }

  /** « 19:00 » */
  fun hour(iso: String?): String {
    val date = parse(iso) ?: return ""
    return format(date, "HH:mm")
  }

  /** « Samedi 28 novembre à 19:00 » */
  fun fullLabel(iso: String?): String {
    val date = parse(iso) ?: return "Date à confirmer"
    return capitalize(format(date, "EEEE d MMMM")) + " à " + format(date, "HH:mm")
  }

  /** « Il y a 12 min », ou null si la date est absente, invalide ou dans le futur. */
  fun elapsedLabel(iso: String?): String? {
    val date = parse(iso) ?: return null
    val minutes = (System.currentTimeMillis() - date.time) / 60_000L
    return when {
      minutes < 0L -> null
      minutes < 1L -> "À l'instant"
      minutes < 60L -> "Il y a $minutes min"
      minutes < 24L * 60L -> "Il y a ${minutes / 60L} h ${minutes % 60L} min"
      else -> null
    }
  }
}
