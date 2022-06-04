package ru.leadpogrommer.vk22.balancer

import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

@SpringBootApplication
class BalancerApplication{
    @Bean
    fun jsonCustom(): Jackson2ObjectMapperBuilderCustomizer{
        return Jackson2ObjectMapperBuilderCustomizer {
            it.modulesToInstall(kotlinModule())
            println("DONE SOMETHING")
        }
    }
}

fun main(args: Array<String>) {
    runApplication<BalancerApplication>(*args)
}

