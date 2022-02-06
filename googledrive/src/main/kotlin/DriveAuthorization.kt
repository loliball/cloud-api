import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.drive.DriveScopes
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStreamReader

object DriveAuthorization {
    private val JSON_FACTORY: JsonFactory = GsonFactory.getDefaultInstance()
    private const val TOKENS_DIRECTORY_PATH = "tokens"

    private val SCOPES = listOf(
        DriveScopes.DRIVE_METADATA_READONLY,
        DriveScopes.DRIVE_READONLY
    )
    private const val CREDENTIALS_FILE_PATH = "/credentials.json"

    private fun getCredentials(HTTP_TRANSPORT: NetHttpTransport): Credential {
        val stream = DriveAuthorization::class.java.getResourceAsStream(CREDENTIALS_FILE_PATH)
            ?: throw FileNotFoundException("Resource not found: $CREDENTIALS_FILE_PATH")
        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(stream))
        val flow = GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
            .setDataStoreFactory(FileDataStoreFactory(File(TOKENS_DIRECTORY_PATH)))
            .setAccessType("offline")
            .build()
        val receiver = LocalServerReceiver.Builder().setPort(8931).build()
        return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
    }

    fun login(): String {
        val credentials = getCredentials(GoogleNetHttpTransport.newTrustedTransport())
        return credentials.accessToken
    }
}