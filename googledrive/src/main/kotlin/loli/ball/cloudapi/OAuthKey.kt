package loli.ball.cloudapi

import kotlinx.serialization.Serializable

@Serializable
data class OAuthKey(
    val access_token: String,
    val expires_in: Int,
    val refresh_token: String? = null,
    val scope: String,
    val token_type: String
)