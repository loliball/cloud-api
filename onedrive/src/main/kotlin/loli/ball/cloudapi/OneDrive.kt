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
            .url(
                "https://api.onedrive.com/v1.0/shares/$encUrl/root?" +
                        "\$expand=children(\$select=id,name,webUrl,image,@content.downloadUrl)&" +
                        "\$select=children,id,webUrl"
            )
            .get()
            .build()
        val response = client.newCall(req).execute()
        val json = response.body?.string().orEmpty()
        val oResult = parser.decodeFromString<OResult>(json)
        val dirs = mutableListOf<CloudDirectory>()
        val files = mutableListOf<CloudFile>()
        val pShareId = oResult.webUrl.substring(oResult.webUrl.lastIndexOf('/') + 1)
        oResult.children.forEach { of ->
            if (of.download == null) {
                dirs.add(CloudDirectory(of.name, of.webUrl))
            } else {
                val image = if (of.image != null) {
                    CloudImage(
                        thumbnail = { queryThumbnail(pShareId, of.id).large.url },
                        thumbnailSmall = { queryThumbnail(pShareId, of.id).small.url },
                        thumbnailWidth = { queryThumbnailWidth(queryThumbnail(pShareId, of.id).small.url, it) },
                        thumbnailHeight = { queryThumbnailHeight(queryThumbnail(pShareId, of.id).small.url, it) },
                        width = of.image.width,
                        height = of.image.height,
                    )
                } else null
                files.add(CloudFile(of.name, of.download, image))
            }
        }
        dirs.sortBy { it.name }
        files.sortBy { it.name }
        return CloudRoot(dirs, files)
    }

    private fun queryThumbnailWidth(link: String, width: Int = 1000): String {
        val append = "?width=$width&height=10000&cropmode=none"
        return link.substring(0, link.lastIndexOf("?")) + append
    }

    private fun queryThumbnailHeight(link: String, height: Int = 1000): String {
        val append = "?width=10000&height=$height&cropmode=none"
        return link.substring(0, link.lastIndexOf("?")) + append
    }

    private fun queryThumbnail(pShareId: String, id: String): OValue {
        val thumbnailUrl =
            "https://api.onedrive.com/v1.0/shares/$pShareId/items/$id/thumbnails"
        val req1 = Request.Builder()
            .url(thumbnailUrl)
            .get()
            .build()
        val response1 = client.newCall(req1).execute()
        val json1 = response1.body?.string().orEmpty()
        val thumbnails = parser.decodeFromString<OThumbnail>(json1)
        return thumbnails.value.first()
    }

    @Serializable
    private data class OResult(
        val webUrl: String,
        val children: List<OFile>
    )

    @Serializable
    private data class OFile(
        val id: String,
        val name: String,
        val webUrl: String,
        @SerialName("@content.downloadUrl")
        val download: String? = null,
        val image: OImage? = null
    )

    @Serializable
    private data class OImage(
        val width: Int,
        val height: Int
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