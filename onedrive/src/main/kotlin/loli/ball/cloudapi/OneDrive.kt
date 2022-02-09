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
                files.add(CloudFile(it.name, it.download))
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
        val name: String,
        val webUrl: String,
        @SerialName("@content.downloadUrl")
        val download: String? = null
    )

}