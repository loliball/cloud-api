package loli.ball.cloudapi

data class CloudFile(
    val name: String,
    val url: String,
    val image: CloudImage? = null
): Comparable<CloudFile> {
    override fun compareTo(other: CloudFile): Int {
        return if (this.name.length == other.name.length) {
            this.name.compareTo(other.name)
        } else if (this.name.length > other.name.length) {
            1
        } else {
            -1
        }
    }
}

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
) : Comparable<CloudDirectory> {
    override fun compareTo(other: CloudDirectory): Int {
        return if (this.name.length == other.name.length) {
            this.name.compareTo(other.name)
        } else if (this.name.length > other.name.length) {
            1
        } else {
            -1
        }
    }
}

data class CloudRoot(
    val dirs: List<CloudDirectory> = listOf(),
    val files: List<CloudFile> = listOf()
)
