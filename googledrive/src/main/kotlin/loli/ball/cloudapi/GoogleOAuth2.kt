package loli.ball.cloudapi

import kotlinx.serialization.Serializable

@Serializable
data class GoogleOAuth2(
    val installed: Installed
)

@Serializable
data class Installed(
    val auth_provider_x509_cert_url: String,
    val auth_uri: String,
    val client_id: String,
    val client_secret: String,
    val project_id: String,
    val redirect_uris: List<String>,
    val token_uri: String
)