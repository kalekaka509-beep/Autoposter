package com.example.util

import com.example.data.model.SocialPlatform
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object UtmBuilder {

    fun buildTrackedUrl(
        originalUrl: String,
        platform: SocialPlatform,
        campaign: String = "us_peak_traffic",
        usPeakHour: Int? = null
    ): String {
        val trimmed = originalUrl.trim()
        if (trimmed.isEmpty()) return ""

        val delimiter = if (trimmed.contains("?")) "&" else "?"
        val queryParams = mutableListOf(
            "utm_source=${encode(platform.id)}",
            "utm_medium=${encode("automated_social")}",
            "utm_campaign=${encode(campaign)}"
        )

        if (usPeakHour != null) {
            queryParams.add("utm_content=${encode("slot_${usPeakHour}est")}")
        }

        return "$trimmed$delimiter${queryParams.joinToString("&")}"
    }

    private fun encode(value: String): String {
        return try {
            URLEncoder.encode(value, StandardCharsets.UTF_8.toString())
        } catch (_: Exception) {
            value
        }
    }
}
