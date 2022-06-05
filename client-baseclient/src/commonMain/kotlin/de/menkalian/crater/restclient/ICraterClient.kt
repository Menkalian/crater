package de.menkalian.crater.restclient

interface ICraterClient {
    fun checkUpToDate(currentDatabaseVersion: Long, callback: (Boolean, Long) -> Unit)
}