package loli.ball.cloudapi

import java.io.File
import java.net.URI


fun String.toURI() = URI(this.replace(" ", "%20"))

fun URI.toFile() = File(this)

fun String.toFile() = File(this.toURI())

fun String.isImage(): Boolean {
    return endsWith(".png", true) ||
            endsWith(".jpg", true) ||
            endsWith(".jpe", true) ||
            endsWith(".jpeg", true) ||
            endsWith(".gif", true) ||
            endsWith(".bmp", true) ||
            endsWith(".webp", true)
}
