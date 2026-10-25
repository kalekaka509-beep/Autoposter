package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.FacebookBlue
import com.example.ui.theme.PinterestRed
import com.example.ui.theme.QuoraBurgundy
import com.example.ui.theme.TwitterBlack

enum class SocialPlatform(
    val id: String,
    val displayName: String,
    val iconSymbol: String,
    val brandColor: Color,
    val usPeakHoursLabel: String,
    val peakHoursEstRange: List<IntRange>, // hours in 24h format EST
    val trafficType: String,
    val characterLimit: Int? = null
) {
    PINTEREST(
        id = "pinterest",
        displayName = "Pinterest",
        iconSymbol = "📌",
        brandColor = PinterestRed,
        usPeakHoursLabel = "8:00 PM – 11:00 PM & 2:00 PM – 4:00 PM EST",
        peakHoursEstRange = listOf(14..16, 20..23),
        trafficType = "High-Converting Long-Term Search & Visual Traffic",
        characterLimit = 500
    ),
    FACEBOOK(
        id = "facebook",
        displayName = "Facebook",
        iconSymbol = "📘",
        brandColor = FacebookBlue,
        usPeakHoursLabel = "9:00 AM – 1:00 PM & 1:00 PM – 3:00 PM EST",
        peakHoursEstRange = listOf(9..15),
        trafficType = "Mid-Day Leisure & Community Shares",
        characterLimit = 2000
    ),
    TWITTER(
        id = "twitter",
        displayName = "Twitter / X",
        iconSymbol = "𝕏",
        brandColor = TwitterBlack,
        usPeakHoursLabel = "8:00 AM – 10:00 AM & 5:00 PM – 7:00 PM EST",
        peakHoursEstRange = listOf(8..10, 17..19),
        trafficType = "Breaking Trends, Viral Retweets & News",
        characterLimit = 280
    ),
    QUORA(
        id = "quora",
        displayName = "Quora",
        iconSymbol = "𝐐",
        brandColor = QuoraBurgundy,
        usPeakHoursLabel = "10:00 AM – 12:00 PM & 7:00 PM – 9:00 PM EST",
        peakHoursEstRange = listOf(10..12, 19..21),
        trafficType = "High-Intent Authority & In-Depth Readers",
        characterLimit = 1500
    );

    fun isPeakNow(currentHourEst: Int): Boolean {
        return peakHoursEstRange.any { range -> currentHourEst in range }
    }
}
