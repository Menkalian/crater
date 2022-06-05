package de.menkalian.crater

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js

internal actual fun createHttpClient(): HttpClient {
    return HttpClient(Js)
}
