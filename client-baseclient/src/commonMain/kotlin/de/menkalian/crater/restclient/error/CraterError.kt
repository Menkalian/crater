package de.menkalian.crater.restclient.error

data class CraterError(
    val code: Int,
    val message: String,
    val cause: Throwable,
) {
    companion object {
        const val ERR_TIMEOUT = -0xffff
        const val ERR_UNKNOWN = -1
    }
}
