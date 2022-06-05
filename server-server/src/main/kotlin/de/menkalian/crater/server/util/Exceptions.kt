package de.menkalian.crater.server.util

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.BAD_REQUEST)
class InvalidDataException(text: String) : RuntimeException(text)

@ResponseStatus(HttpStatus.NOT_FOUND)
class NotFoundException : RuntimeException()