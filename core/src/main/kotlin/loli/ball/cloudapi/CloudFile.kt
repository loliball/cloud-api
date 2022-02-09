package loli.ball.cloudapi

data class CloudFile(
    val name: String,
    val url: String
)

data class CloudDirectory(
    val name: String,
    val url: String
)

data class CloudRoot(
    val dirs: List<CloudDirectory> = listOf(),
    val files: List<CloudFile> = listOf()
)
