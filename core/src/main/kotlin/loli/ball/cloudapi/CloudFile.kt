package loli.ball.cloudapi

data class CloudFile(
    val name: String,
    val url: String,
    val image: CloudImage? = null
)

data class CloudImage(
    val thumbnail: () -> String,
    val thumbnailSmall: () -> String,
    val thumbnailWidth: (Int) -> String = { "" },
    val thumbnailHeight: (Int) -> String = { "" },
    val width: Int,
    val height: Int
)

data class CloudDirectory(
    val name: String,
    val url: String
)

data class CloudRoot(
    val dirs: List<CloudDirectory> = listOf(),
    val files: List<CloudFile> = listOf()
)
