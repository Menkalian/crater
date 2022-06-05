package de.menkalian.crater

import io.ktor.client.HttpClient

internal expect fun createHttpClient(): HttpClient
