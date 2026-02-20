package com.personalblog.backend.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

object JwtConfig {
    private val secret = System.getenv("JWT_SECRET") ?: "dev-secret-change-in-production"
    private val algorithm = Algorithm.HMAC256(secret)
    val realm = "personal-blog"
    private const val EXPIRATION_MS = 3_600_000L // 1 hour

    val verifier: JWTVerifier = JWT.require(algorithm).withIssuer("personal-blog").build()

    fun generateToken(userId: Long, role: String): String = JWT.create()
        .withIssuer("personal-blog")
        .withClaim("userId", userId)
        .withClaim("role", role)
        .withExpiresAt(Date(System.currentTimeMillis() + EXPIRATION_MS))
        .sign(algorithm)
}
