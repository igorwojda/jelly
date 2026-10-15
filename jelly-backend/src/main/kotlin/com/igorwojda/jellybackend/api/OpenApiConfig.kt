package com.igorwojda.jellybackend.api

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.IntegerSchema
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.StringSchema
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import io.swagger.v3.oas.models.media.MediaType as OpenApiMediaType

private const val PROBLEM_DETAIL_SCHEMA = "ProblemDetail"

@Configuration
class OpenApiConfig {
	@Bean
	fun openApi(): OpenAPI =
		OpenAPI().info(
			Info()
				.title("Jelly Backend API")
				.version("v1")
				.description("Movie data (proxied from TMDB and shaped for the mobile client) and user favorites."),
		)

	/**
	 * Documents every error response (4xx/5xx) as an RFC 9457 problem detail, the body [ApiExceptionHandler] returns.
	 * Controllers declare only the status codes and their meaning.
	 */
	@Bean
	fun problemDetailErrorResponses() =
		OpenApiCustomizer { openApi ->
			// Registered here, not on the OpenAPI bean: springdoc prunes unreferenced schemas before running customizers
			openApi.components = (openApi.components ?: Components()).addSchemas(PROBLEM_DETAIL_SCHEMA, problemDetailSchema())
			val problemContent =
				Content().addMediaType(
					MediaType.APPLICATION_PROBLEM_JSON_VALUE,
					OpenApiMediaType().schema(Schema<Any>().`$ref`(PROBLEM_DETAIL_SCHEMA)),
				)

			openApi.paths.values
				.flatMap { it.readOperations() }
				.flatMap { it.responses.entries }
				.filter { (statusCode, _) -> (statusCode.toIntOrNull() ?: 0) >= 400 }
				.forEach { (_, response) -> response.content = problemContent }
		}

	private fun problemDetailSchema(): Schema<*> =
		ObjectSchema()
			.description("RFC 9457 problem details")
			.addProperty("title", StringSchema().example("Not Found"))
			.addProperty("status", IntegerSchema().example(404))
			.addProperty("detail", StringSchema().example("Movie 1 not found"))
			.addProperty("instance", StringSchema().format("uri").example("/api/v1/movies/1"))
}
