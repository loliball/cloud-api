package loli.ball.cloudapi

class LocalFile : CloudDrive {

    companion object {
        fun isSupport(url: String): Boolean {
            val uri = url.toURI()
            return uri.scheme == "file" && !uri.schemeSpecificPart.endsWith(".zip", true)
        }
    }

    override fun isSupport(url: String): Boolean {
        return LocalFile.isSupport(url)
    }

    override fun parse(url: String): CloudRoot {
        val folder = url.toFile()
        val dirs = mutableListOf<CloudDirectory>()
        val files = mutableListOf<CloudFile>()
        folder.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                dirs += CloudDirectory(
                    name = file.name,
                    url = file.path
                )
            } else if (file.isFile) {
                val image = if (file.name.isImage()) {
                    val getImage = {
                        file.toURI().toString()
                    }
                    val getImage2 = { _: Int ->
                        file.toURI().toString()
                    }
                    CloudImage(
                        thumbnail = getImage,
                        thumbnailSmall = getImage,
                        thumbnailWidth = getImage2,
                        thumbnailHeight = getImage2,
                        width = 0,
                        height = 0
                    )
                } else null
                files += CloudFile(
                    name = file.name,
                    url = file.path,
                    image = image
                )
            }
        }
        dirs.sort()
        files.sort()
        return CloudRoot(dirs, files)
    }

}