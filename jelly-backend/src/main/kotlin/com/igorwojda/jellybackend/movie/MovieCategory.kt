package com.igorwojda.jellybackend.movie

import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

enum class MovieCategory(
	val apiValue: String,
) {
	UPCOMING("upcoming"),
	TOP_RATED("top_rated"),
	NOW_PLAYING("now_playing"),
}

/** Binds the `category` query parameter from its lowercase API value (e.g. `top_rated`). */
@Component
class MovieCategoryConverter : Converter<String, MovieCategory> {
	override fun convert(source: String): MovieCategory =
		MovieCategory.entries.firstOrNull { it.apiValue == source.trim().lowercase() }
			?: throw IllegalArgumentException("Unknown movie category: $source")
}
