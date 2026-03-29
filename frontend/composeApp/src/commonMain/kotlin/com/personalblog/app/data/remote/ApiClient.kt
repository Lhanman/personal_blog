package com.personalblog.app.data.remote

import com.personalblog.shared.dto.*
import com.personalblog.app.logging.LoggerFactory
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class ApiClient(
    val baseUrl: String = ApiConfig.apiBaseUrl,
    val tokenProvider: () -> String?
) {
    private val logger = LoggerFactory.getLogger("ApiClient")

    val http = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(Logging) {
            level = LogLevel.INFO
            logger = object : Logger {
                override fun log(message: String) {
                    this@ApiClient.logger.debug(
                        message = message,
                        feature = "network",
                        extras = mapOf("baseUrl" to baseUrl)
                    )
                }
            }
        }
    }

    suspend inline fun <reified T> get(path: String, params: Map<String, String> = emptyMap()): T {
        return http.get("$baseUrl$path") {
            tokenProvider()?.let { bearerAuth(it) }
            params.forEach { (k, v) -> parameter(k, v) }
        }.body()
    }

    suspend inline fun <reified T, reified B : Any> post(path: String, body: B): T {
        return http.post("$baseUrl$path") {
            tokenProvider()?.let { bearerAuth(it) }
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()
    }

    suspend inline fun <reified T, reified B : Any> put(path: String, body: B): T {
        return http.put("$baseUrl$path") {
            tokenProvider()?.let { bearerAuth(it) }
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()
    }

    suspend fun delete(path: String) {
        http.delete("$baseUrl$path") {
            tokenProvider()?.let { bearerAuth(it) }
        }
    }
}
