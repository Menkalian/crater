package de.menkalian.crater.restclient

import de.menkalian.crater.restclient.config.CraterClientConfiguration
import io.ktor.client.HttpClient

@Suppress("unused")
class CraterClient(private val httpClientTemplate: HttpClient, private val configuration: CraterClientConfiguration) : ICraterClient {
    override fun checkUpToDate(currentDatabaseVersion: Long, callback: (Boolean, Long) -> Unit) {
        TODO("Not yet implemented")
    }
}