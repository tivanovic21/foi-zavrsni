package com.example.zavrsniauth0

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.auth0.android.result.UserProfile
import com.example.zavrsniauth0.ui.theme.Zavrsniauth0Theme

enum class Screen {
    Login,
    Home
}

class MainActivity : ComponentActivity() {

    private lateinit var account: Auth0
    private lateinit var currentUser: CurrentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Zavrsniauth0Theme {
                val navController: NavHostController = rememberNavController()
                val context = LocalContext.current
                account = Auth0(
                    getString(R.string.auth_0_client_id),
                    getString(R.string.auth_0_domain)
                )

                NavHost(navController = navController, startDestination = Screen.Login.name) {
                    composable(Screen.Login.name){
                        LoginScreen(
                            onSignInClick = {
                                WebAuthProvider.login(account)
                                    .withScheme("demo")
                                    .withScope("openid profile name")
                                    .start(context, object : Callback<Credentials, AuthenticationException>{
                                        override fun onFailure(error: AuthenticationException) {
                                            Toast.makeText(context, "Authentication error: ${error.message}", Toast.LENGTH_SHORT).show()
                                        }

                                        override fun onSuccess(result: Credentials) {
                                            val accessToken = result.accessToken
                                            val client = AuthenticationAPIClient(account)

                                            client.userInfo(accessToken)
                                                .start(object : Callback<UserProfile, AuthenticationException>{
                                                    override fun onFailure(error: AuthenticationException) {
                                                        Toast.makeText(context, "Error getting user data: ${error.message}", Toast.LENGTH_SHORT).show()
                                                    }

                                                    override fun onSuccess(result: UserProfile) {
                                                        val name = result.name
                                                        val photoUri = result.pictureURL
                                                        currentUser = CurrentUser(
                                                            idToken = accessToken,
                                                            displayName = name,
                                                            photoUri = photoUri?.toUri()
                                                        )
                                                        navController.navigate(Screen.Home.name)
                                                    }

                                                })

                                        }

                                    })
                            }
                        )
                    }
                    composable(Screen.Home.name){
                        HomeScreen(
                            currentUser = currentUser,
                            onSignOutClick = {
                                WebAuthProvider.logout(account)
                                    .withScheme("demo")
                                    .start(context, object : Callback<Void?, AuthenticationException>{
                                        override fun onFailure(error: AuthenticationException) {
                                            Toast.makeText(context, "Error while signing out: ${error.message}", Toast.LENGTH_SHORT).show()
                                        }

                                        override fun onSuccess(result: Void?) {
                                            navController.navigate(Screen.Login.name)
                                        }
                                    })
                            }
                        )
                    }
                }
            }
        }
    }
}