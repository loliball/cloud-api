package loli.ball.cloudapi

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

class GoogleDrive(
    private val client: OkHttpClient = OkHttpClient(),
    private var token: String? = null
) : CloudDrive {

    private val regex = """https://drive.google.com/drive/folders/([^?]+)\??.*?""".toRegex()

    companion object {
        fun isSupport(url: String): Boolean {
            return url.startsWith("https://drive.google.com", true)
        }
    }

    override fun isSupport(url: String) = GoogleDrive.isSupport(url)

    override fun parse(url: String): CloudRoot {
        val fileId = url.fromShareLink()
        val fields = listOf(
            "files/id",
            "files/name",
            "files/mimeType",
            "files/thumbnailLink",
            "files/imageMediaMetadata"
        ).joinToString(",")
        val httpUrl = "https://content.googleapis.com/drive/v3/files".toHttpUrl().newBuilder()
            .addQueryParameter("q", "'$fileId' in parents")
            .addQueryParameter("fields", fields)
            .build()
        val request = Request.Builder()
            .url(httpUrl)
            .apply {
                if (token != null) {
                    header("authorization", "Bearer $token")
                }
            }
            .get()
            .build()
        val json = client.newCall(request).execute().body?.string() ?: ""
        val gResult = Json.decodeFromString<GResult>(json)
        val dirs = mutableListOf<CloudDirectory>()
        val files = mutableListOf<CloudFile>()
        gResult.files.forEach { gf ->
            if (gf.mimeType == "application/vnd.google-apps.folder") {
                dirs.add(CloudDirectory(gf.name, gf.id.toShareLink()))
            } else {
                val image = if (gf.thumbnailLink != null) {
                    CloudImage(
                        thumbnail = { queryBigThumbnailWidth(fileId) },
                        thumbnailSmall = { gf.thumbnailLink },
                        thumbnailWidth = { queryBigThumbnailWidth(fileId, it) },
                        thumbnailHeight = { queryBigThumbnailHeight(fileId, it) },
                        width = gf.imageMediaMetadata?.width ?: 0,
                        height = gf.imageMediaMetadata?.height ?: 0
                    )
                } else null
                files.add(CloudFile(gf.name, gf.id.toDownloadLink(), image))
            }
        }
        return CloudRoot(dirs, files)
    }

    private fun queryBigThumbnailWidth(fileId: String, width: Int = 1000) =
        "https://drive.google.com/thumbnail?authuser=0&sz=w$width&id=$fileId"

    private fun queryBigThumbnailHeight(fileId: String, height: Int = 1000) =
        "https://drive.google.com/thumbnail?authuser=0&sz=h$height&id=$fileId"

    fun login(credential: String, loginScreen: (String) -> Unit): OAuthKey {
        return DriveAuthorization.login(client, credential, loginScreen).apply {
            token = access_token
        }
    }

    fun refresh(credential: String, refreshToken: String): OAuthKey {
        return DriveAuthorization.refresh(client, credential, refreshToken).apply {
            token = access_token
        }
    }

    private fun String.fromShareLink() = regex.matchEntire(this)?.groupValues?.get(1).orEmpty()

    private fun String.toShareLink() = "https://drive.google.com/drive/folders/$this"

    private fun String.toDownloadLink() = "https://www.googleapis.com/drive/v3/files/$this?alt=media"

    @Serializable
    private data class GResult(
        val files: List<GFile>
    )

    @Serializable
    private data class GFile(
        val id: String,
        val name: String,
        val mimeType: String,
        val thumbnailLink: String? = null,
        val imageMediaMetadata: GImage? = null
    )

    @Serializable
    private data class GImage(
        val width: Int = 0,
        val height: Int = 0,
        val rotation: Int = 0
    )
}
