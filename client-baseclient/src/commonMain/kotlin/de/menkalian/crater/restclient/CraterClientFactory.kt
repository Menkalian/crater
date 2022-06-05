package de.menkalian.crater.restclient

import de.menkalian.crater.createHttpClient
import de.menkalian.crater.restclient.config.CraterClientConfiguration

@Suppress("unused")
object CraterClientFactory {
    fun createClient(configuration: CraterClientConfiguration): ICraterClient {
        return CraterClient(createHttpClient(), configuration)
    }
}

