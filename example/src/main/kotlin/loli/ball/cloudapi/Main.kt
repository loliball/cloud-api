package loli.ball.cloudapi

import okhttp3.OkHttpClient
import java.io.FileInputStream
import java.net.InetSocketAddress
import java.net.Proxy

const val address = "127.0.0.1"
const val port = "10800"

fun proxy() {
    System.setProperty("proxySet", "true");
    System.setProperty("http.proxyHost", address);
    System.setProperty("https.proxyHost", address);
    System.setProperty("http.proxyPort", port);
    System.setProperty("https.proxyPort", port);
}

fun main(args: Array<String>) {
    proxy()
    oneDriveExample()
    googleDriveExample()
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
    val root2 = drive.parse("https://drive.google.com/drive/folders/1CoitKJ0G9rWZvEeVyMtF1VitxVaUToAO")
    println(root2)
}