interface CloudDrive {
    fun isSupport(url: String): Boolean
    fun parse(url: String): CloudRoot
}