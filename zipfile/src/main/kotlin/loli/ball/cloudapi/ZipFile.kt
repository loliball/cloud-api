package loli.ball.cloudapi

import java.io.File
import java.io.InputStream
import java.net.URI
import java.nio.charset.Charset
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile as ZIP

class ZipFile(var charset: String = "UTF-8") : CloudDrive {

    companion object {

        fun isSupport(url: String): Boolean {
            val uri = url.toURI()
            return uri.scheme == "file" && uri.schemeSpecificPart.endsWith(".zip", true)
        }

        private var buffer: WeakHashMap<String, TreeRoot> = WeakHashMap()

        fun getFile(url: String, charset: String = "UTF-8"): InputStream {
            val pair = init(url, charset)
            val root = pair.first
            val node = pair.second
            return root.zipFile.getInputStream(node.entry!!)
        }

        private fun unZip(file: File, charset: String): TreeRoot {
            val zip = ZIP(file, Charset.forName(charset))
            val root = TreeNode(null, "/", true)
            for (zipEntry in zip.entries()) {
//                println(zipEntry)
                var current = root.children
                var tail: TreeNode? = null
                val sb = StringBuilder()
                zipEntry.name.split("/").forEach { p ->
                    if (p.isNotEmpty()) {
                        sb.append(p).append("/")
                        if (!current.containsKey(p)) {
                            current[p] = TreeNode(null, sb.toString(), true)
                        }
                        tail = current[p]
                        current = current[p]!!.children
                    }
                }
                tail?.path = zipEntry.name
                tail?.isDirectory = zipEntry.isDirectory
                tail?.entry = zipEntry
            }
            return TreeRoot(zip, root)
        }

        private fun init(url: String, charset: String): Pair<TreeRoot, TreeNode> {
            val uri = url.toURI()
            val file = File(URI(uri.scheme, uri.schemeSpecificPart, null))
            val realPath = file.path
            val fragment = uri.fragment.orEmpty()
            if (buffer[realPath] == null) {
                buffer[realPath] = unZip(file, charset)
            }
            var node = buffer[realPath]!!.root
            fragment.split("/").forEach {
                if (it.isNotEmpty()) {
                    node = node.children[it]!!
                }
            }
            return Pair(buffer[realPath]!!, node)
        }

    }

    override fun isSupport(url: String): Boolean {
        return ZipFile.isSupport(url)
    }

    override fun parse(url: String): CloudRoot {
        val pair = init(url, charset)
        val uri = url.toURI()
        val node = pair.second
        val dirs = mutableListOf<CloudDirectory>()
        val files = mutableListOf<CloudFile>()
        node.children.forEach { one ->
            val entry = one.value
            if (entry.isDirectory) {
                dirs.add(
                    CloudDirectory(
                        one.key,
                        URI(uri.scheme, uri.schemeSpecificPart, entry.path).toString()
                    )
                )
            } else {
                entry.entry?.let {
                    val image = if (it.name.isImage()) {
                        val getImage = {
                            URI(uri.scheme, uri.schemeSpecificPart, it.name).toString()
                        }
                        val getImage2 = { _: Int ->
                            URI(uri.scheme, uri.schemeSpecificPart, it.name).toString()
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
                    files.add(
                        CloudFile(
                            one.key,
                            URI(uri.scheme, uri.schemeSpecificPart, it.name).toString(),
                            image
                        )
                    )
                }
            }
        }
        dirs.sort()
        files.sort()
        return CloudRoot(dirs, files)
    }

    data class TreeRoot(
        val zipFile: ZIP,
        val root: TreeNode
    ) {
        @Suppress("unused")
        fun print() {
            printTree(root, zipFile.name, 0)
        }

        private fun printTree(node: TreeNode, name: String, stack: Int) {
            println("    ".repeat(stack) + name)
            node.children.forEach {
                printTree(it.value, it.key, stack + 1)
            }
        }
    }

    data class TreeNode(
        var entry: ZipEntry?,
        var path: String,
        var isDirectory: Boolean,
        val children: MutableMap<String, TreeNode> = mutableMapOf()
    )

}