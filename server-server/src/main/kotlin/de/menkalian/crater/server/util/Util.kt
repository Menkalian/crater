package de.menkalian.crater.server.util

fun catchBoolean(function: () -> Boolean) : Boolean {
    return try {
        function()
    } catch (ex: Exception) {
        false
    }
}

fun currentUnixSecond() = System.currentTimeMillis() / 1000L
