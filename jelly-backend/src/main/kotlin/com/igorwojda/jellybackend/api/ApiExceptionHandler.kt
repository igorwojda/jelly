package com.igorwojda.jellybackend.api

import com.igorwojda.jellybackend.tmdb.TmdbUnavailableException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

/**
 * Maps errors to RFC 9457 problem details.
 *
 * Spring's base handler already turns invalid input (bad path/query values, failed constraints,
 * missing parameters) into `400 Bad Request`.
 */
@RestControllerAdvice
class ApiExceptionHandler : ResponseEntityExceptionHandler() {
	private val log = LoggerFactory.getLogger(javaClass)

	@ExceptionHandler(NotFoundException::class)
	fun handleNotFound(exception: NotFoundException): ProblemDetail =
		ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.message)

	@ExceptionHandler(TmdbUnavailableException::class)
	fun handleTmdbUnavailable(exception: TmdbUnavailableException): ProblemDetail {
		log.warn("TMDB request failed", exception)
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, "Movie data provider is unavailable")
	}
}
