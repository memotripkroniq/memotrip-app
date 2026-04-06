package com.example.memotrip_kroniq.ui.utils

import android.content.Context
import java.io.File

fun createImageFile(context: Context): File {
    val dir = File(context.cacheDir, "images")
    if (!dir.exists()) dir.mkdirs()
    return File(dir, "photo_${System.currentTimeMillis()}.jpg")
}