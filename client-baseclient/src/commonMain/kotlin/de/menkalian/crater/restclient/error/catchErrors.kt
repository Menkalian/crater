package de.menkalian.crater.restclient.error

import io.ktor.client.features.ClientRequestException
import io.ktor.client.features.HttpRequestTimeoutException
import io.ktor.client.features.RedirectResponseException
import io.ktor.client.features.ServerResponseException

suspend fun catchErrors(
    onError: (CraterError) -> Unit,
    executable: suspend () -> Unit
) {
    try {
        executable()
    } catch (ex: RedirectResponseException) {
        onError(
            CraterError(
                ex.response.status.value,
                "${ex.response.status.description}: ${ex.message}",
                ex
            )
        )
    } catch (ex: ClientRequestException) {
        onError(
            CraterError(
                ex.response.status.value,
                "${ex.response.status.description}: ${ex.message}",
                ex
            )
        )
    } catch (ex: ServerResponseException) {
        onError(
            CraterError(
                ex.response.status.value,
                "${ex.response.status.description}: ${ex.message}",
                ex
            )
        )
    } catch (ex: HttpRequestTimeoutException) {
        onError(
            CraterError(
                CraterError.ERR_TIMEOUT,
                "Timeout for request: \"${ex.message}\"",
                ex
            )
        )
    } catch (ex: Exception) {
        onError(
            CraterError(
                CraterError.ERR_UNKNOWN,
                "Unknown Error: \"${ex.message}\"",
                ex
            )
        )
    }
}
