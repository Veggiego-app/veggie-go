package com.veggiego.customer

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/** One restaurant opening slot stored in Firestore as { start: "HH:mm", end: "HH:mm" }. */
data class RestaurantTimeSlot(
    val start: String = "",
    val end: String = ""
)

data class RestaurantData(

    val id: String = "",

    val name: String = "",

    val category: String = "",

    val imageUrl: String = "",

    val bannerUrl: String = "",

    val rating: String = "4.5",

    val deliveryTime: String = "20-30 min",

    val offer: String = "",

    val isPureVeg: Boolean = true,

    // Kept for backward compatibility, but customer app open/close no longer depends on it.
    val autoOpen: Boolean = false,

    val liveStatus: String = "",

    val openingText: String = "",

    val isHoliday: Boolean = false,

    val online: Boolean = true,

    val temporaryClosed: Boolean = false,

    val weeklySlots: Map<String, List<RestaurantTimeSlot>> = emptyMap(),

    val lat: Double = 0.0,

    val lng: Double = 0.0,

    val distanceKm: Double = 0.0,

    val distanceText: String = "",

    val displayOrder: Int = 999999
)

/** Safely converts the Firestore weeklySlots map into strongly typed slots. */
fun parseRestaurantWeeklySlots(raw: Any?): Map<String, List<RestaurantTimeSlot>> {
    val days = raw as? Map<*, *> ?: return emptyMap()

    return days.mapNotNull dayLoop@{ (dayKey, slotsValue) ->
        val day = dayKey?.toString()?.trim().orEmpty()
        if (day.isBlank()) return@dayLoop null

        val slots = (slotsValue as? List<*>)
            ?.mapNotNull slotLoop@{ slotValue ->
                val slot = slotValue as? Map<*, *> ?: return@slotLoop null
                val start = slot["start"]?.toString()?.trim().orEmpty()
                val end = slot["end"]?.toString()?.trim().orEmpty()

                if (timeToMinutes(start) == null || timeToMinutes(end) == null) {
                    null
                } else {
                    RestaurantTimeSlot(start = start, end = end)
                }
            }
            .orEmpty()
            .sortedBy { timeToMinutes(it.start) ?: Int.MAX_VALUE }

        day to slots
    }.toMap()
}

/** Final customer-app rule: online + not temporary closed + not holiday + inside weekly slot. */
fun isRestaurantOpenNow(
    restaurant: RestaurantData,
    calendar: Calendar = Calendar.getInstance()
): Boolean {
    return restaurant.online &&
            !restaurant.temporaryClosed &&
            !restaurant.isHoliday &&
            isInsideWeeklyRestaurantSlot(restaurant.weeklySlots, calendar)
}

fun isInsideWeeklyRestaurantSlot(
    weeklySlots: Map<String, List<RestaurantTimeSlot>>,
    calendar: Calendar = Calendar.getInstance()
): Boolean {
    if (weeklySlots.isEmpty()) return false

    val currentMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
    val today = dayName(calendar)

    // Normal slots and the part of an overnight slot before midnight.
    weeklySlots[today].orEmpty().forEach { slot ->
        val start = timeToMinutes(slot.start) ?: return@forEach
        val end = timeToMinutes(slot.end) ?: return@forEach

        if (start <= end) {
            if (currentMinutes in start..end) return true
        } else if (currentMinutes >= start) {
            return true
        }
    }

    // After midnight, an overnight slot belongs to the previous day.
    val previous = calendar.clone() as Calendar
    previous.add(Calendar.DAY_OF_YEAR, -1)
    val previousDay = dayName(previous)

    weeklySlots[previousDay].orEmpty().forEach { slot ->
        val start = timeToMinutes(slot.start) ?: return@forEach
        val end = timeToMinutes(slot.end) ?: return@forEach

        if (start > end && currentMinutes <= end) return true
    }

    return false
}

/** Text shown when an online restaurant is outside its weekly slot. */
fun restaurantWeeklyOpeningText(
    restaurant: RestaurantData,
    calendar: Calendar = Calendar.getInstance()
): String {
    if (restaurant.temporaryClosed) return "Temporarily Closed"
    if (restaurant.isHoliday) return "Holiday Today"
    if (!restaurant.online) return "Currently Closed"
    if (isInsideWeeklyRestaurantSlot(restaurant.weeklySlots, calendar)) return "Open Now"

    val now = calendar.timeInMillis

    for (dayOffset in 0..7) {
        val candidateDay = calendar.clone() as Calendar
        candidateDay.add(Calendar.DAY_OF_YEAR, dayOffset)
        val day = dayName(candidateDay)

        restaurant.weeklySlots[day].orEmpty().forEach { slot ->
            val startMinutes = timeToMinutes(slot.start) ?: return@forEach
            val candidate = candidateDay.clone() as Calendar
            candidate.set(Calendar.HOUR_OF_DAY, startMinutes / 60)
            candidate.set(Calendar.MINUTE, startMinutes % 60)
            candidate.set(Calendar.SECOND, 0)
            candidate.set(Calendar.MILLISECOND, 0)

            if (candidate.timeInMillis > now) {
                val differenceMinutes = ((candidate.timeInMillis - now) / 60_000L).toInt()
                return when {
                    dayOffset == 0 && differenceMinutes <= 60 -> "Opening in $differenceMinutes mins"
                    dayOffset == 0 -> "Opens at ${format12Hour(slot.start)}"
                    else -> "Opens $day ${format12Hour(slot.start)}"
                }
            }
        }
    }

    return "Currently Closed"
}

private fun dayName(calendar: Calendar): String =
    SimpleDateFormat("EEEE", Locale.ENGLISH).format(calendar.time)

private fun timeToMinutes(value: String): Int? {
    val parts = value.trim().split(":")
    if (parts.size != 2) return null

    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    if (hour !in 0..23 || minute !in 0..59) return null

    return hour * 60 + minute
}

private fun format12Hour(value: String): String {
    val minutes = timeToMinutes(value) ?: return value
    val hour24 = minutes / 60
    val minute = minutes % 60
    val suffix = if (hour24 >= 12) "PM" else "AM"
    val hour12 = when (val h = hour24 % 12) {
        0 -> 12
        else -> h
    }

    return String.format(Locale.ENGLISH, "%d:%02d %s", hour12, minute, suffix)
}
