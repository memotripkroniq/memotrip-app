package com.example.memotrip_kroniq.ui.splash

import PreviewUiScaler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.sx
import com.example.memotrip_kroniq.ui.core.sy
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {

    LaunchedEffect(Unit) {
        delay(3000)
        onTimeout()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {

        val s = LocalUiScaler.current  // 🔥 globální scaling

        // FIGMA hodnoty (px)
        val imageHeightPx = 622f      // horní obrázek
        val logoSizePx = 300f         // velikost loga
        val logoOffsetYpx = -160f     // pozice loga směrem nahoru

        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            // 🖼 Horní obrázek
            Image(
                painter = painterResource(R.drawable.splash_background),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeightPx.sy(s)),  // výška → scaleY
                contentScale = ContentScale.Crop
            )

            // 🔹 LOGO (texty jsou součástí obrázku)
            Image(
                painter = painterResource(R.drawable.ic_logo_memotrip),
                contentDescription = "App Logo",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = logoOffsetYpx.sy(s)) // posunutí → scaleY
                    .size(logoSizePx.sx(s))          // velikost → scaleX
            )
        }
    }
}

@Preview(
    showBackground = true,
    widthDp = 412,
    heightDp = 892
)
@Composable
fun SplashPreview() {
    CompositionLocalProvider(
        LocalUiScaler provides PreviewUiScaler
    ) {
        MemoTripTheme {
            SplashScreen {}
        }
    }
}



