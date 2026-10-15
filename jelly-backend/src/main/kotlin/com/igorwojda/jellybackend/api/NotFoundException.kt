package com.igorwojda.jellybackend.api

/** Base type for errors that the API reports as `404 Not Found`. */
abstract class NotFoundException(message: String) : RuntimeException(message)
