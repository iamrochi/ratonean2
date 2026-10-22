package com.example.ratonean2_app.auth.presentation.screen

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ratonean2_app.auth.presentation.viemwodel.LoginViewModel
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen() {
    val context = LocalContext.current
    val viewModel = koinViewModel<LoginViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // P0.6: getSignInCredentialFromIntent lanza ApiException si el intent es nulo/corrupto.
            runCatching {
                val credential = Identity.getSignInClient(context)
                    .getSignInCredentialFromIntent(result.data)
                credential.googleIdToken
            }.onSuccess { idToken ->
                if (!idToken.isNullOrEmpty()) viewModel.loginWithGoogle(idToken)
                else scope.launch { snackbarHostState.showSnackbar("No se pudo obtener el token de Google.") }
            }.onFailure { e ->
                Log.e("GIS Login", "credential error: ${e.message}", e)
                scope.launch { snackbarHostState.showSnackbar("Error de login con Google. Intentá de nuevo.") }
            }
        }
    }

    val signInRequest = BeginSignInRequest.builder()
        .setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                .setServerClientId("368774461247-9v49te82pb20ndpli048fccambrleu8n.apps.googleusercontent.com")
                .setFilterByAuthorizedAccounts(false)
                .build()
        )
        .setAutoSelectEnabled(false)
        .build()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    val oneTapClient = Identity.getSignInClient(context)
                    oneTapClient.beginSignIn(signInRequest)
                        .addOnSuccessListener { result ->
                            launcher.launch(
                                IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                            )
                        }
                        .addOnFailureListener { e ->
                            Log.e("GIS Login", "Error: ${e.message}", e)
                            // P0.6: antes solo Logcat, usuario sin feedback.
                            scope.launch { snackbarHostState.showSnackbar("No se pudo iniciar Google. Revisá tu conexión.") }
                        }
                },
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Login con Google")
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}
