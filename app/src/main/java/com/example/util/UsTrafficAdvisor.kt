package com.example.util

import com.example.data.model.SocialPlatform
import com.example.data.model.TrafficStatus
import com.example.data.model.UsTimezone
import com.example.data.model.UsTrafficStatusInfo
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object UsTrafficAdvisor {

    private val estTimeZone: TimeZone = TimeZone.getTimeZone("America/New_York")

    fun getUsCalendar(timeZone: TimeZone = estTimeZone): Calendar {
        return Calendar.getInstance(timeZone)
    }

    fun formatTimeForZone(timeZone: TimeZone, date: Date = Date()): String {
        val sdf = SimpleDateFormat("h:mm a", Locale.US)
        sdf.timeZone = timeZone
        return sdf.format(date)
    }

    fun formatDateForZone(timeZone: TimeZone, date: Date = Date()): String {
        val sdf = SimpleDateFormat("EEE, MMM d", Locale.US)
        sdf.timeZone = timeZone
        return sdf.format(date)
    }

    fun getCurrentTrafficStatus(): UsTrafficStatusInfo {
        val cal = getUsCalendar(estTimeZone)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)

        val activePlatforms = SocialPlatform.values().filter { platform ->
            platform.isPeakNow(hour)
        }

        val status = when {
            activePlatforms.isNotEmpty() -> TrafficStatus.PEAK_ACTIVE
            hour in 6..7 || hour in 13..13 || hour in 18..19 -> TrafficStatus.RISING
            else -> TrafficStatus.OFF_PEAK
        }

        val (nextPeakDesc, minutesUntil) = calculateNextPeak(hour, minute)

        val multiplier = when (status) {
            TrafficStatus.PEAK_ACTIVE -> 2.8
            TrafficStatus.RISING -> 1.7
            TrafficStatus.OFF_PEAK -> 0.9
        }

        val timeFormatter = SimpleDateFormat("h:mm a 'EST'", Locale.US).apply {
            timeZone = estTimeZone
        }

        return UsTrafficStatusInfo(
            currentEstHour = hour,
            currentEstMinute = minute,
            formattedEstTime = timeFormatter.format(cal.time),
            status = status,
            activePeakPlatforms = activePlatforms,
            nextPeakDescription = nextPeakDesc,
            minutesUntilNextPeak = minutesUntil,
            estimatedReachMultiplier = multiplier
        )
    }

    /**
     * Finds the next upcoming US peak hour
     */
    fun calculateNextPeak(currentHour: Int, currentMinute: Int): Pair<String, Int> {
        // Known peak start hours in EST: 8 (Twitter morning), 9 (FB morning), 10 (Quora morning), 14 (Pinterest afternoon), 17 (Twitter evening), 19 (Quora evening), 20 (Pinterest prime)
        val peakHours = listOf(8, 9, 10, 14, 17, 19, 20)
        val upcoming = peakHours.firstOrNull { it > currentHour }

        return if (upcoming != null) {
            val minutesLeft = (upcoming - currentHour) * 60 - currentMinute
            val hourFormatted = if (upcoming > 12) "${upcoming - 12}:00 PM EST" else "$upcoming:00 AM EST"
            Pair("Next major US peak begins at $hourFormatted", minutesLeft.coerceAtLeast(1))
        } else {
            // Next morning peak at 8:00 AM EST tomorrow
            val hoursLeft = (24 - currentHour) + 8
            val minutesLeft = hoursLeft * 60 - currentMinute
            Pair("Next prime US morning rush at 8:00 AM EST tomorrow", minutesLeft)
        }
    }

    /**
     * Calculates the best next epoch timestamp in millis to schedule a post for peak US reach
     */
    fun getNextOptimalScheduleEpochMillis(): Pair<Long, String> {
        val cal = getUsCalendar(estTimeZone)
        val currentHour = cal.get(Calendar.HOUR_OF_DAY)

        // Target slots: 9 AM, 2 PM (14:00), 8 PM (20:00)
        val targetHour = when {
            currentHour < 9 -> 9
            currentHour in 9..13 -> 14
            currentHour in 14..19 -> 20
            else -> {
                // Next day 9 AM
                cal.add(Calendar.DAY_OF_YEAR, 1)
                9
            }
        }

        cal.set(Calendar.HOUR_OF_DAY, targetHour)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val slotName = when (targetHour) {
            9 -> "US Morning Break (9:00 AM EST - FB/X)"
            14 -> "US Afternoon Shopping (2:00 PM EST - Pinterest/FB)"
            20 -> "US Prime Evening (8:00 PM EST - Pinterest/Quora)"
            else -> "Peak Window"
        }

        return Pair(cal.timeInMillis, slotName)
    }
}
