package com.example.zavrsnisso

import android.net.Uri

data class CurrentGoogleUser(
    val googleIdToken: String?,
    val displayName: String?,
    val photoUri: Uri?
)
