package com.example.zavrsniauth0

import android.net.Uri

data class CurrentUser(
    val idToken: String?,
    val displayName: String?,
    val photoUri: Uri?
)
