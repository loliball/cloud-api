package loli.ball.cloudapi

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.ServerSocket
import kotlin.random.Random

object DriveAuthorization {

    private fun tinyServer(port: Int) =
        ServerSocket(port).use { server ->
            server.accept().use { socket ->
                socket.getInputStream().use { inp ->
                    val readLine = inp.reader().buffered(50).readLine()
                    socket.getOutputStream().use { out ->
                        out.write(
                            """
                            HTTP/1.1 200 OK
                            Date: Wed, 04 Jul 2001 12:08:56 GMT
                            Content-Type: text/plain
                            Content-Length: 2

                            OK
                        """.trimIndent().toByteArray()
                        )
                    }
                    readLine
                }
            }
        }

    private val exactCode = """.*code=([^ &]+)[ &].*""".toRegex()

    fun login(client: OkHttpClient, credential: String, loginScreen: (String) -> Unit): OAuthKey {
        val oAuth2 = Json.decodeFromString<GoogleOAuth2>(credential)
        val scope = listOf(
            "https://www.googleapis.com/auth/drive.metadata.readonly",
            "https://www.googleapis.com/auth/drive.readonly"
        ).joinToString(" ")
        val port = Random.nextInt(10000, 60000)
        val redirectUri = "http://localhost:$port"
        val httpUrl = "https://accounts.google.com/o/oauth2/auth".toHttpUrlOrNull()!!.newBuilder()
            .addQueryParameter("access_type", "offline")
            .addQueryParameter("response_type", "code")
            .addQueryParameter("client_id", oAuth2.installed.client_id)
            .addQueryParameter("redirect_uri", redirectUri)
            .addQueryParameter("scope", scope)
            .build()
        loginScreen(httpUrl.toString())
        val resp = tinyServer(port)
        val authorizationCode = exactCode.find(resp)?.groupValues?.get(1).orEmpty()

        val request = Request.Builder()
            .url("https://oauth2.googleapis.com/token")
            .post(
                FormBody.Builder()
                    .add("client_id", oAuth2.installed.client_id)
                    .add("client_secret", oAuth2.installed.client_secret)
                    .add("code", authorizationCode)
                    .add("grant_type", "authorization_code")
                    .add("redirect_uri", redirectUri)
                    .build()
            )
            .build()
        val response = client.newCall(request).execute()
        return Json.decodeFromString(response.body?.string().orEmpty())
    }

    fun refresh(client: OkHttpClient, credential: String, refreshToken: String): OAuthKey {
        val oAuth2 = Json.decodeFromString<GoogleOAuth2>(credential)
        val request = Request.Builder()
            .url("https://oauth2.googleapis.com/token")
            .post(
                FormBody.Builder()
                    .add("client_id", oAuth2.installed.client_id)
                    .add("client_secret", oAuth2.installed.client_secret)
                    .add("grant_type", "refresh_token")
                    .add("refresh_token", refreshToken)
                    .build()
            )
            .build()
        val response = client.newCall(request).execute()
        return Json.decodeFromString(response.body?.string().orEmpty())
    }
}