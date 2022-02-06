import okhttp3.OkHttpClient
import java.net.InetSocketAddress
import java.net.Proxy

const val address = "127.0.0.1"
const val port = "10800"

fun proxy() {
    val address = "127.0.0.1"
    val port = "10800"
    System.setProperty("proxySet", "true");
    System.setProperty("http.proxyHost", address);
    System.setProperty("https.proxyHost", address);
    System.setProperty("http.proxyPort", port);
    System.setProperty("https.proxyPort", port);
}

fun main(args: Array<String>) {
    proxy()
    oneDriveExample()
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
    println(root2)
}

private fun googleDriveExample() {
    val token =
        """
            ya29.A0ARrdaM-yw_M4_PaGN6dDIMXC6tydVUKkUe9eCDbv75lDuIKpGqf-SnisRgFUr_JSx3lmNDDI6KRHOC-1_w2XkYYGr-7cS1YaqLa6LlAQsEeBT1oWAuL3uwYy_YKl8bTbXf0eBgzMaHP7Mq7OpwDOBHi6kR1l
        """.trimIndent()

    val client = OkHttpClient.Builder()
        .followRedirects(false)
        .proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress(address, port.toInt())))
        .build()

    val drive = GoogleDrive(client, token)
    val newToken = drive.login()
    println(newToken)

    val root = drive.parse("https://drive.google.com/drive/folders/1g4Suvo4vKdF0YD_fyNFnNup6JTgKvv_k?usp=sharing")
    println(root)
    val root2 = drive.parse("https://drive.google.com/drive/folders/1CoitKJ0G9rWZvEeVyMtF1VitxVaUToAO")
    println(root2)
}