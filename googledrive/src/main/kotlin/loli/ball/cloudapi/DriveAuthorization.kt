package loli.ball.cloudapi

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.client.util.store.MemoryDataStoreFactory
import com.google.api.services.drive.DriveScopes
import java.io.File
import kotlin.random.Random

object DriveAuthorization {

    private val JSON_FACTORY: JsonFactory = GsonFactory.getDefaultInstance()

    private val SCOPES = listOf(
        DriveScopes.DRIVE_METADATA_READONLY,
        DriveScopes.DRIVE_READONLY
    )

    private fun getCredentials(credential: String, tokenStoreFolder: File?): Credential {
        val transport = GoogleNetHttpTransport.newTrustedTransport()
        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, credential.reader())
        val storeFactory = if (tokenStoreFolder == null) {
            MemoryDataStoreFactory.getDefaultInstance()
        } else {
            FileDataStoreFactory(tokenStoreFolder)
        }
        val flow = GoogleAuthorizationCodeFlow.Builder(transport, JSON_FACTORY, clientSecrets, SCOPES)
            .setDataStoreFactory(storeFactory)
            .setAccessType("offline")
            .build()
        val receiver = LocalServerReceiver.Builder().setPort(Random.Default.nextInt(10000, 60000)).build()
        return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
    }

    fun login(credential: String, tokenStoreFolder: File? = null): String {
        return getCredentials(credential, tokenStoreFolder).accessToken
    }
}