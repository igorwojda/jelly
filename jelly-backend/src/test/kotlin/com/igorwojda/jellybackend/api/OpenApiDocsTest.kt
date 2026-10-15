package com.igorwojda.jellybackend.api

import com.igorwojda.jellybackend.TestcontainersConfiguration
import com.jayway.jsonpath.JsonPath
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest(properties = ["tmdb.api-token=test-token"])
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
class OpenApiDocsTest(
	@Autowired private val mockMvc: MockMvc,
) {
	private val apiDocs by lazy {
		mockMvc
			.get("/v3/api-docs")
			.andExpect { status { isOk() } }
			.andReturn()
			.response.contentAsString
	}

	@Test
	fun `documents success and error status codes of every operation`() {
		assertSoftly { softly ->
			EXPECTED_STATUS_CODES.forEach { (operation, statusCodes) ->
				softly
					.assertThat(responsesOf(operation).keys)
					.describedAs(operation.toString())
					.containsExactlyInAnyOrderElementsOf(statusCodes)
			}
		}
	}

	@Test
	fun `documents error responses as problem details`() {
		val errorSchemaRefs =
			EXPECTED_STATUS_CODES.flatMap { (operation, statusCodes) ->
				statusCodes
					.filter { it.toInt() >= 400 }
					.map { JsonPath.read<String>(apiDocs, "${operation.responsesPath}['$it'].content['application/problem+json'].schema['\$ref']") }
			}

		assertThat(errorSchemaRefs).isNotEmpty.containsOnly("#/components/schemas/ProblemDetail")
		assertThat(JsonPath.read<Map<String, Any>>(apiDocs, "\$.components.schemas.ProblemDetail.properties"))
			.containsOnlyKeys("title", "status", "detail", "instance")
	}

	@Test
	fun `keeps success responses in their own schema`() {
		val detailsRef =
			JsonPath.read<String>(
				apiDocs,
				"${Operation("get", "/api/v1/movies/{movieId}").responsesPath}['200'].content['*/*'].schema['\$ref']",
			)

		assertThat(detailsRef).isEqualTo("#/components/schemas/MovieDetailsResponse")
	}

	private fun responsesOf(operation: Operation): Map<String, Any> = JsonPath.read(apiDocs, operation.responsesPath)

	private data class Operation(
		val method: String,
		val path: String,
	) {
		val responsesPath get() = "\$.paths['$path'].$method.responses"

		override fun toString() = "${method.uppercase()} $path"
	}

	private companion object {
		const val FAVORITES = "/api/v1/users/{userId}/favorites"

		val EXPECTED_STATUS_CODES =
			mapOf(
				Operation("get", "/api/v1/movies") to listOf("200", "400", "502"),
				Operation("get", "/api/v1/movies/{movieId}") to listOf("200", "400", "404", "502"),
				Operation("get", FAVORITES) to listOf("200", "400"),
				Operation("put", "$FAVORITES/{movieId}") to listOf("200", "201", "400", "404", "502"),
				Operation("delete", "$FAVORITES/{movieId}") to listOf("204", "400", "404"),
			)
	}
}
