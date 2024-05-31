package com.example.zavrsnisso

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.zavrsnisso.ui.theme.ZavrsniSSOTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.launch
enum class Screen{
    Login,
    Home
}
class MainActivity : ComponentActivity() {

    private lateinit var WEB_CLIENT_ID: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WEB_CLIENT_ID = getString(R.string.default_web_client_id) // - web application client id
        enableEdgeToEdge()
        setContent {
            ZavrsniSSOTheme {
                val navController: NavHostController = rememberNavController()
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                val credentialManager: CredentialManager = CredentialManager.create(context)
                var currentGoogleUser: CurrentGoogleUser = CurrentGoogleUser("", "", "".toUri())

                NavHost(navController = navController, startDestination = Screen.Login.name) {
                    composable(Screen.Login.name){
                        LoginScreen(
                            onGoogleSignInClick = {
                               val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                                   .setServerClientId(WEB_CLIENT_ID)
                                   .setFilterByAuthorizedAccounts(false)
                                   .build()

                                val request: GetCredentialRequest = GetCredentialRequest.Builder()
                                    .addCredentialOption(googleIdOption)
                                    .build()
                                scope.launch {
                                    try {
                                        val result = credentialManager.getCredential(request = request, context = context)
                                        val credential = result.credential
                                        Toast.makeText(context, "${credential}", Toast.LENGTH_LONG).show()

                                        if(credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL){
                                            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                            currentGoogleUser = CurrentGoogleUser(
                                                googleIdToken = googleIdTokenCredential.idToken,
                                                displayName = googleIdTokenCredential.displayName,
                                                photoUri = googleIdTokenCredential.profilePictureUri
                                            )
                                            Toast.makeText(context, "Login successful!", Toast.LENGTH_LONG).show()
                                            navController.navigate(Screen.Home.name)
                                        }

                                    } catch (e: GoogleIdTokenParsingException){
                                        Toast.makeText(context, "Invalid ID token!", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception){
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        )
                    }
                    composable(Screen.Home.name){
                        HomeScreen(
                            currentGoogleUser = currentGoogleUser,
                            onSignOutClick = {
                                scope.launch {
                                    credentialManager.clearCredentialState(
                                        ClearCredentialStateRequest()
                                    )
                                }
                                navController.popBackStack()
                                navController.navigate(Screen.Login.name)
                            }
                        )
                    }
                }
            }
        }
    }
}