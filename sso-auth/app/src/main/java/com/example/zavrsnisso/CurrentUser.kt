package com.example.zavrsnisso

import android.net.Uri

data class CurrentUser(
    val idToken: String?,
    val displayName: String?,
    val photoUri: Uri?
)
