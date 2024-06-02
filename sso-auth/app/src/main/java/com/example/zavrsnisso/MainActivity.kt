package com.example.zavrsnisso

import android.content.Intent
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
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.launch
import java.util.Arrays
import com.facebook.FacebookSdk
import com.facebook.GraphRequest
import org.json.JSONException

enum class Screen{
    Login,
    Home
}
class MainActivity : ComponentActivity() {
    private lateinit var WEB_CLIENT_ID: String
    private lateinit var currentUser: CurrentUser
    private lateinit var callbackManager: CallbackManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FacebookSdk.setApplicationId(getString(R.string.facebook_app_id ))
        FacebookSdk.setClientToken(getString(R.string.facebook_client_token))
        FacebookSdk.sdkInitialize(this)

        WEB_CLIENT_ID = getString(R.string.default_web_client_id) // - web application client id
        callbackManager = CallbackManager.Factory.create() // - callback za facebook

        enableEdgeToEdge()
        setContent {
            ZavrsniSSOTheme {
                val navController: NavHostController = rememberNavController()
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                val credentialManager: CredentialManager = CredentialManager.create(context)

                currentUser = CurrentUser("", "", "".toUri())
                LoginManager.getInstance()
                    .registerCallback(callbackManager, object : FacebookCallback<LoginResult>{
                        override fun onCancel() {
                            Toast.makeText(context, "Facebook login canceled!", Toast.LENGTH_SHORT).show()
                        }

                        override fun onError(error: FacebookException) {
                            Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                        }

                        override fun onSuccess(result: LoginResult) {
                            val accessToken = result.accessToken
                            val request = GraphRequest.newMeRequest(accessToken) { obj, _ ->
                                try {
                                    val name = obj?.optString("name", "")
                                    val profilePictureUri = obj?.getJSONObject("picture")?.getJSONObject("data")?.optString("url", "")
                                    currentUser = CurrentUser(
                                        idToken = accessToken.token,
                                        displayName = name,
                                        photoUri = profilePictureUri?.toUri()
                                    )
                                    navController.navigate(Screen.Home.name)
                                } catch (e: JSONException){
                                    Toast.makeText(context, "Error parsing user data: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                            val parameters = Bundle()
                            parameters.putString("fields", "id,name,picture.type(large)")
                            request.parameters = parameters
                            request.executeAsync()
                        }

                    })

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

                                        if(credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL){
                                            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                            currentUser = CurrentUser(
                                                idToken = googleIdTokenCredential.idToken,
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
                            },
                            onFacebookSignInClick = {
                                try {
                                    LoginManager.getInstance().logIn(this@MainActivity, Arrays.asList("public_profile"))
                                } catch (e: Exception){
                                    Log.e("Facebook error", "${e.message}")
                                }
                            }
                        )
                    }
                    composable(Screen.Home.name){
                        HomeScreen(
                            currentGoogleUser = currentUser,
                            onSignOutClick = {
                                scope.launch {
                                    credentialManager.clearCredentialState(
                                        ClearCredentialStateRequest()
                                    )
                                }
                                LoginManager.getInstance().logOut()
                                navController.popBackStack()
                                navController.navigate(Screen.Login.name)
                            }
                        )
                    }
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }
}