package com.igorwojda.jellybackend.tmdb

import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties("tmdb")
data class TmdbProperties(
	/** TMDB "API Read Access Token", sent as a bearer token. */
	@NotBlank val apiToken: String,
	@NotBlank val baseUrl: String,
	@NotBlank val imageBaseUrl: String,
)
