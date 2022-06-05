package de.menkalian.crater.server.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun Any.logger(): Logger {
    return LoggerFactory.getLogger(this::class.java)
}

fun catchBoolean(function: () -> Boolean) : Boolean {
    return try {
        function()
    } catch (ex: Exception) {
        false
    }
}

fun currentUnixSecond() = System.currentTimeMillis() / 1000L
