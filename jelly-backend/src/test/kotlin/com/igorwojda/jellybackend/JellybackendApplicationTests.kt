package com.igorwojda.jellybackend

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@SpringBootTest(properties = ["tmdb.api-token=test-token"])
@Import(TestcontainersConfiguration::class)
class JellybackendApplicationTests {
	@Test
	fun contextLoads() {
	}
}
