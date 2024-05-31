package com.example.zavrsnisso

import android.content.Context
import android.net.Uri
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.request.ImageRequest

@Composable
fun HomeScreen(modifier: Modifier = Modifier, currentGoogleUser:CurrentGoogleUser?, onSignOutClick: () -> Unit) {
    val textStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp
    )
    Surface(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            Spacer(modifier = Modifier.size(32.dp))
            currentGoogleUser?.let {user ->
                user.photoUri?.let {
                    AsyncImage(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        model = ImageRequest.Builder(context = LocalContext.current)
                            .data(it)
                            .crossfade(true)
                            .build(),
                        contentDescription = "profile picture",
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.size(32.dp))
                user.displayName?.let { name ->
                    Text(
                        text = name,
                        style = textStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                }
                user.googleIdToken?.let { idToken ->
                    Text(
                        text = "IdToken: " + idToken,
                        style = textStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.size(16.dp))
            Button(onClick = {onSignOutClick()}) {
                Text(
                    text = "Sign Out",
                    style = textStyle.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}