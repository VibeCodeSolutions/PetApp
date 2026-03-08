// app/src/main/java/com/example/tierapp/auth/LoginScreen.kt
package com.example.tierapp.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import coil3.compose.AsyncImage
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tierapp.R
import com.example.tierapp.core.ui.theme.TierappTheme

@Composable
fun LoginRoute(
    onAuthenticated: () -> Unit,
    onDatenschutzClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Authenticated) onAuthenticated()
    }

    LoginScreen(
        uiState = uiState,
        onGoogleSignIn = { context, webClientId ->
            viewModel.initiateGoogleSignIn(context, webClientId)
        },
        onClearError = viewModel::clearError,
        onDatenschutzClick = onDatenschutzClick,
        modifier = modifier,
    )
}

@Composable
internal fun LoginScreen(
    uiState: LoginUiState,
    onGoogleSignIn: (context: android.content.Context, webClientId: String) -> Unit,
    onClearError: () -> Unit,
    onDatenschutzClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val isLoading = uiState is LoginUiState.Loading
    val webClientId = stringResource(R.string.default_web_client_id)

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Error) {
            snackbarHostState.showSnackbar(uiState.message)
            onClearError()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AsyncImage(
            model = "file:///android_asset/foreground.png",
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        // Scrim für Lesbarkeit des Texts
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f)),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Pets,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.login_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = stringResource(R.string.login_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 48.dp),
            )

            // Google Sign-In (Credential Manager)
            Button(
                onClick = { onGoogleSignIn(context, webClientId) },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.login_google))
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onDatenschutzClick) {
                Text(
                    text = stringResource(R.string.datenschutz_link),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
        )
    }
}

// ---- Previews --------------------------------------------------------------

@Preview(showBackground = true)
@Composable
private fun LoginScreenUnauthenticatedPreview() {
    TierappTheme {
        LoginScreen(
            uiState = LoginUiState.Unauthenticated,
            onGoogleSignIn = { _, _ -> },
            onClearError = {},
            onDatenschutzClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenLoadingPreview() {
    TierappTheme {
        LoginScreen(
            uiState = LoginUiState.Loading,
            onGoogleSignIn = { _, _ -> },
            onClearError = {},
            onDatenschutzClick = {},
        )
    }
}
