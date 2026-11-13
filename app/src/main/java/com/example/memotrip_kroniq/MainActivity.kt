package com.example.memotrip_kroniq

import android.os.Build
import android.os.Bundle
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import com.memotrip_kroniq.BuildConfig
import com.example.memotrip_kroniq.navigation.ResetPasswordLinkParser
import com.example.memotrip_kroniq.navigation.ResetPasswordLinkViewModel
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.rememberNavController
import com.example.memotrip_kroniq.navigation.AppNavGraph
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme
import com.example.memotrip_kroniq.ui.theme.AppTheme   // 👈 náš nový scaling wrapper

class MainActivity : AppCompatActivity() {
    private val devLinksEnabled = ResetPasswordLinkParser.enabled(BuildConfig.FLAVOR, BuildConfig.BUILD_TYPE)
    private val resetLinks by lazy { ViewModelProvider(this)[ResetPasswordLinkViewModel::class.java] }

    private fun receiveResetLink(incoming: Intent, fresh: Boolean = true) {
        if (!devLinksEnabled || incoming.action != Intent.ACTION_VIEW) return
        if (fresh) resetLinks.accept(incoming.dataString)
        // Navigation must never auto-handle or persist a credential-bearing intent.
        setIntent(Intent(this, MainActivity::class.java).setAction(Intent.ACTION_MAIN))
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        receiveResetLink(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        receiveResetLink(intent, fresh = savedInstanceState == null)

        // Inicializace RetrofitClientu s TokenDataStore
        //RetrofitClient.build(
        //    TokenDataStore(applicationContext)
        //)

        // Fullscreen edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.statusBars())
        controller.hide(WindowInsetsCompat.Type.navigationBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {

            val navController = rememberNavController()

            // ⬇⬇⬇ TADY JE DŮLEŽITÁ ZMĚNA
            AppTheme {   // 🎯 scaling layer
                MemoTripTheme {   // 🎨 tvoje barevné téma
                    AppNavGraph(navController, if (devLinksEnabled) resetLinks else null)
                }
            }
        }
    }
}
