package com.example.memotrip_kroniq.ui.tripdetail.components

import android.net.Uri

data class TipsAndTripsItemUi(
    val id: String? = null,          // připraveno do budoucna (DB / backend)
    val title: String = "",           // např. "Hidden beach"
    val imageUri: Uri? = null         // zatím UI-only (galerie)
)