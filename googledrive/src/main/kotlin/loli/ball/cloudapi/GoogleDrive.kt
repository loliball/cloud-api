package loli.ball.cloudapi

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

class GoogleDrive(
    private val client: OkHttpClient = OkHttpClient(),
    private var token: String? = null,
    private val credential: String? = null,
    private val tokenStoreFolder: File? = null
) : CloudDrive {

    private val regex = """https://drive.google.com/drive/folders/(\w+)\??.*?""".toRegex()

    companion object {
        fun isSupport(url: String): Boolean {
            return url.startsWith("https://drive.google.com", true)
        }
    }

    override fun isSupport(url: String) = GoogleDrive.isSupport(url)

    override fun parse(url: String): CloudRoot {
        val fileId = url.fromShareLink()
        val request = Request.Builder()
            .url("https://content.googleapis.com/drive/v3/files?q='$fileId' in parents")
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
        gResult.files.forEach {
            if (it.mimeType == "application/vnd.google-apps.folder") {
                dirs.add(CloudDirectory(it.name, it.id.toShareLink()))
            } else {
                files.add(CloudFile(it.name, it.id.toDownloadLink()))
            }
        }
        return CloudRoot(dirs, files)
    }

    fun login(): String? {
        token = DriveAuthorization.login(credential.orEmpty(), tokenStoreFolder)
        return token
    }

    private fun String.fromShareLink() = regex.matchEntire(this)?.groupValues?.get(1) ?: ""

    private fun String.toShareLink() = "https://drive.google.com/drive/folders/$this"

    private fun String.toDownloadLink() = "https://www.googleapis.com/drive/v3/files/$this?alt=media"

    @Serializable
    private data class GResult(
        val files: List<GFile>,
        val incompleteSearch: Boolean,
        val kind: String
    )

    @Serializable
    private data class GFile(
        val id: String,
        val kind: String,
        val mimeType: String,
        val name: String
    )
}
