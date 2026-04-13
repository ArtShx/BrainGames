package com.example.braingames.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun formatTimestamp(timestamp: Long, pattern: String = "yyyy-MM-dd HH:mm"): String {
    val formatter = DateTimeFormatter.ofPattern(pattern)
        .withZone(ZoneId.systemDefault())

    val instant = Instant.ofEpochMilli(timestamp)
    return formatter.format(instant)
}