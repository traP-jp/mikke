package jp.trap.mikke.config

object Environment {
    val TRAQ_URL: String = System.getenv("TRAQ_URL")?.trim().takeUnless { it.isNullOrEmpty() } ?: "https://q.trap.jp"
    val TRAQ_CLIENT_ID: String =
        System.getenv("TRAQ_CLIENT_ID")?.trim().takeIf { it?.isNotEmpty() ?: false }
            ?: throw IllegalStateException("TRAQ_CLIENT_ID is not set")
    val TRAQ_CLIENT_SECRET: String =
        System.getenv("TRAQ_CLIENT_SECRET")?.trim().takeIf { it?.isNotEmpty() ?: false }
            ?: throw IllegalStateException("TRAQ_CLIENT_SECRET is not set")

    val TRAQ_API_BASE_URL: String = "$TRAQ_URL/api/v3"

    val TRAQ_AUTHORIZE_URL: String = "$TRAQ_API_BASE_URL/oauth2/authorize"
    val TRAQ_TOKEN_URL: String = "$TRAQ_API_BASE_URL/oauth2/token"

    val DB_URL = System.getenv("DB_URL") ?: "jdbc:mariadb://localhost:3306/mikke"
    val DB_USER = System.getenv("DB_USER") ?: "mikke"
    val DB_PASS = System.getenv("DB_PASS") ?: "mikke_dev"
}
