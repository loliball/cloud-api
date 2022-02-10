package loli.ball.cloudapi

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.*

class OneDrive(private val client: OkHttpClient = OkHttpClient()) : CloudDrive {

    private val parser = Json {
        ignoreUnknownKeys = true
    }

    companion object {
        fun isSupport(url: String): Boolean {
            return url.startsWith("https://1drv.ms", true) ||
                    url.startsWith("https://onedrive.live.com", true)
        }
    }

    override fun isSupport(url: String): Boolean = OneDrive.isSupport(url)

    override fun parse(url: String): CloudRoot {
        val base64org = Base64.getEncoder().encodeToString(url.toByteArray())
        val encUrl = "u!" + base64org.trimEnd('=').replace('/', '_').replace('+', '-')
        val req = Request.Builder()
            .url("https://api.onedrive.com/v1.0/shares/$encUrl/root?expand=children")
            .get()
            .build()
        val response = client.newCall(req).execute()
        val json = response.body?.string().orEmpty()
        val oResult = parser.decodeFromString<OResult>(json)
        val dirs = mutableListOf<CloudDirectory>()
        val files = mutableListOf<CloudFile>()
        oResult.children.forEach {
            if (it.download == null) {
                dirs.add(CloudDirectory(it.name, it.webUrl))
            } else {
                val image = if(it.image != null) {
                    val thumbnail = {
                        val thumbnailUrl =
                            "https://api.onedrive.com/v1.0/shares/${it.parentReference.shareId}/items/${it.id}/thumbnails"
                        val req1 = Request.Builder()
                            .url(thumbnailUrl)
                            .get()
                            .build()
                        val response1 = client.newCall(req1).execute()
                        val json1 = response1.body?.string().orEmpty()
                        val thumbnails = parser.decodeFromString<OThumbnail>(json1)
                        thumbnails.value.first().large.url
                    }
                    CloudImage(
                        thumbnail = thumbnail,
                        width = it.image.width,
                        height = it.image.height,
                    )
                } else null
                files.add(CloudFile(it.name, it.download, image))
            }
        }
        return CloudRoot(dirs, files)
    }

    @Serializable
    private data class OResult(
        val children: List<OFile>
    )

    @Serializable
    private data class OFile(
        val id: String,
        val name: String,
        val webUrl: String,
        @SerialName("@content.downloadUrl")
        val download: String? = null,
        val image: OImage? = null,
        val parentReference: OParent
    )

    @Serializable
    private data class OImage(
        val width: Int,
        val height: Int
    )

    @Serializable
    private data class OParent(
        val shareId: String
    )

    @Serializable
    private data class OThumbnail(
        val value: List<OValue>
    )

    @Serializable
    private data class OValue(
        val large: OThumbnailImage,
        val medium: OThumbnailImage,
        val small: OThumbnailImage,
    )

    @Serializable
    private data class OThumbnailImage(
        val height: Int,
        val url: String,
        val width: Int
    )
}