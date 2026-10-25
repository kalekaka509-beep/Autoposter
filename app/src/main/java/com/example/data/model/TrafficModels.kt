package com.example.data.model

import java.util.TimeZone

enum class UsTimezone(
    val code: String,
    val fullName: String,
    val timeZoneId: String,
    val majorCity: String
) {
    EASTERN("EDT / EST", "US Eastern Time", "America/New_York", "New York / Miami"),
    CENTRAL("CDT / CST", "US Central Time", "America/Chicago", "Chicago / Dallas"),
    MOUNTAIN("MDT / MST", "US Mountain Time", "America/Denver", "Denver / Phoenix"),
    PACIFIC("PDT / PST", "US Pacific Time", "America/Los_Angeles", "Los Angeles / Seattle");

    val timeZone: TimeZone
        get() = TimeZone.getTimeZone(timeZoneId)
}

enum class TrafficStatus(
    val title: String,
    val description: String,
    val badgeIcon: String
) {
    PEAK_ACTIVE(
        title = "US Peak Traffic Active",
        description = "Highest engagement window right now in the United States! Optimal time for viral reach.",
        badgeIcon = "🔥"
    ),
    RISING(
        title = "Traffic Rising",
        description = "US users waking up or entering afternoon break. High engagement window begins shortly.",
        badgeIcon = "⚡"
    ),
    OFF_PEAK(
        title = "US Off-Peak Hours",
        description = "US night/early hours. Posts queued now will be automatically dispatched at the next peak.",
        badgeIcon = "🌙"
    )
}

data class UsTrafficStatusInfo(
    val currentEstHour: Int,
    val currentEstMinute: Int,
    val formattedEstTime: String,
    val status: TrafficStatus,
    val activePeakPlatforms: List<SocialPlatform>,
    val nextPeakDescription: String,
    val minutesUntilNextPeak: Int,
    val estimatedReachMultiplier: Double
)
