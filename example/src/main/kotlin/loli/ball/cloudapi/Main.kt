package loli.ball.cloudapi

import okhttp3.OkHttpClient
import java.io.File
import java.io.FileInputStream
import java.net.InetSocketAddress
import java.net.Proxy

const val address = "127.0.0.1"
const val port = "10800"

fun main() {
    oneDriveExample()
    googleDriveExample()
    zipFileExample()
}

private fun zipFileExample() {
//    val drive = ZipFile("GBK")
//    val root = drive.parse(File("C:\\Users\\hp\\Desktop\\新建文件夹.zip").toURI().toString())
//    println(root)
//    val root2 = drive.parse(root.dirs.first().url)
//    println(root2)
//    val root3 = drive.parse(root2.dirs.first().url)
//    println(root3)

//    val drive = ZipFile("GBK")
//    val root = drive.parse(File("C:\\Users\\hp\\Desktop\\乾 紗寿叶.zip").toURI().toString())
    val drive = ZipFile("SHIFT_JIS")
    val root = drive.parse(File("C:\\Users\\hp\\Desktop\\バニー衣装ル・マランとえっち.zip").toURI().toString())
    println(root)
    println(drive.parse(root.dirs.first().url).files.first().image!!.thumbnail())
//    val thumbnail = root.files[3].image!!.thumbnail()
//    println(thumbnail)
//    val t = System.currentTimeMillis()
//    ZipFile.getFile(thumbnail).copyTo(FileOutputStream("C:\\Users\\hp\\Desktop\\" + root.files[3].name))
//    println(System.currentTimeMillis() - t)
}

private fun oneDriveExample() {
    val client = OkHttpClient.Builder()
        .followRedirects(false)
        .proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress(address, port.toInt())))
        .build()
    val drive = OneDrive(client)
    val root = drive.parse("https://1drv.ms/u/s!Ai6oanP7THBlg7kTtDtBzJA7xXVrEA?e=2SVm4N")
    println(root)
    val root2 = drive.parse("https://1drv.ms/f/s!Ai6oanP7THBlg75LtDtBzJA7xXVrEA")
    println(root2.files.first().image!!.thumbnail())
    println(root2)
}

private fun googleDriveExample() {
    val client = OkHttpClient.Builder()
        .followRedirects(false)
        .proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress(address, port.toInt())))
        .build()

    val drive = GoogleDrive(client)
    val path =
        "C:\\Users\\hp\\Downloads\\client_secret_782626792532-f6f1nmnnikmv82arjrj8mh9ugko5j2mu.apps.googleusercontent.com.json"
    val credential = FileInputStream(path).reader().readLines().joinToString("\n")
    val oAuthKey = drive.login(credential, ::println)
    println(oAuthKey)

    val oAuthKey2 = drive.refresh(credential, oAuthKey.refresh_token!!)
    println(oAuthKey2)

    val root = drive.parse("https://drive.google.com/drive/folders/1g4Suvo4vKdF0YD_fyNFnNup6JTgKvv_k?usp=sharing")
    println(root)
    val root2 = drive.parse("https://drive.google.com/drive/folders/1HEFhKN4rKSHDFi4OAp2wvuKvsWu6Q367")
    println(root2.files.first().image!!.thumbnail())
    println(root2)
}