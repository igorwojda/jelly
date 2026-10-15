package com.igorwojda.jellybackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class JellybackendApplication

fun main(args: Array<String>) {
	runApplication<JellybackendApplication>(*args)
}
