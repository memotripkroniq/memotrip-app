package com.example.memotrip_kroniq.ui.addtrip

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import PreviewUiScaler
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.memotrip_kroniq.data.AuthRepository
import com.example.memotrip_kroniq.data.datastore.TokenDataStore
import com.example.memotrip_kroniq.data.remote.RetrofitClient
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.core.sx
import com.example.memotrip_kroniq.ui.home.components.AppTopBar
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme
import com.google.android.play.integrity.internal.s


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddTripScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    val s = LocalUiScaler.current

    val tokenStore = remember { TokenDataStore(context) }

    val repository = remember {
        AuthRepository(
            api = RetrofitClient.api,
            tokenStore = tokenStore
        )
    }

    val viewModel: AddTripViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return AddTripViewModel(
                    authRepository = repository
                ) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            AppTopBar(
                modifier = Modifier.statusBarsPadding(),
                title = "Add Trip",
                showBack = true,
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { innerPadding ->

        AddTripContent(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16f.sx(s)),
            uiState = uiState,
            onTripNameChange = viewModel::onTripNameChange,
            onDestinationSelected = viewModel::onDestinationSelected,
            onThemeSelected = viewModel::onThemeSelected,
            onDateClick = {},
            onFromClick = {},
            onToClick = {},
            onTransportSelected = viewModel::onTransportSelected,
            onNextClick = {},
        )
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, widthDp = 412, heightDp = 1110)
@Composable
fun AddTripScreenPreview() {
    CompositionLocalProvider(
        LocalUiScaler provides PreviewUiScaler
    ) {
        MemoTripTheme {
            Scaffold(
                containerColor = Color.Black,
                topBar = {
                    AppTopBar(
                        title = "Add Trip",
                        showBack = true,
                        onBackClick = {}
                    )
                }
            ) { innerPadding ->
                AddTripContent(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(horizontal = 16f.sx(LocalUiScaler.current)),
                    uiState = AddTripUiState(
                        isThemesLocked = false,   // 🔓 ODEMČENO
                        selectedTheme = null,
                        destination = Destination.EUROPE,
                        transport = TransportType.CARAVAN
                    ),
                    onTripNameChange = {},
                    onDestinationSelected = {},
                    onThemeSelected = {},
                    onDateClick = {},
                    onFromClick = {},
                    onToClick = {},
                    onTransportSelected = {},
                    onNextClick = {}
                )
            }
        }
    }
}



