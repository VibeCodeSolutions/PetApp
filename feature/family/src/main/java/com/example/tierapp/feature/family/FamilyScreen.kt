package com.example.tierapp.feature.family

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tierapp.core.model.AuthUser
import com.example.tierapp.core.model.FamilyMember
import com.example.tierapp.core.model.MemberRole
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyScreen(
    currentUser: AuthUser,
    viewModel: FamilyViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val errorMessage = (uiState as? FamilyUiState.Error)?.message
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage)
            viewModel.dismissError()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Familie") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (val state = uiState) {
                is FamilyUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is FamilyUiState.NoFamily -> {
                    NoFamilyContent(
                        onCreateFamily = { name ->
                            viewModel.createFamily(name, currentUser)
                        },
                        onJoinFamily = { code ->
                            viewModel.joinByInviteCode(code, currentUser)
                        },
                    )
                }

                is FamilyUiState.HasFamily -> {
                    val context = LocalContext.current
                    FamilyContent(
                        state = state,
                        onCopyCode = {
                            val clipboard =
                                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(
                                ClipData.newPlainText("Einladungscode", state.family.inviteCode)
                            )
                            scope.launch {
                                snackbarHostState.showSnackbar("Code kopiert: ${state.family.inviteCode}")
                            }
                        },
                    )
                }

                is FamilyUiState.Error -> {
                    // Fehler wird per Snackbar gezeigt; hier NoFamily anzeigen
                    NoFamilyContent(
                        onCreateFamily = { name -> viewModel.createFamily(name, currentUser) },
                        onJoinFamily = { code -> viewModel.joinByInviteCode(code, currentUser) },
                    )
                }
            }
        }
    }
}

@Composable
private fun NoFamilyContent(
    onCreateFamily: (String) -> Unit,
    onJoinFamily: (String) -> Unit,
) {
    var familyName by remember { mutableStateOf("") }
    var inviteCode by remember { mutableStateOf("") }
    var showJoinField by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Noch keine Familie", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(
            "Erstelle eine Familie oder trete einer per Einladungscode bei.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = familyName,
            onValueChange = { familyName = it },
            label = { Text("Familienname") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                if (familyName.isNotBlank()) onCreateFamily(familyName)
            }),
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { if (familyName.isNotBlank()) onCreateFamily(familyName) },
            modifier = Modifier.fillMaxWidth(),
            enabled = familyName.isNotBlank(),
        ) {
            Text("Familie erstellen")
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        TextButton(onClick = { showJoinField = !showJoinField }) {
            Text(if (showJoinField) "Abbrechen" else "Per Einladungscode beitreten")
        }

        AnimatedVisibility(
            visible = showJoinField,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Column {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = inviteCode,
                    onValueChange = { inviteCode = it.uppercase() },
                    label = { Text("Einladungscode") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (inviteCode.isNotBlank()) onJoinFamily(inviteCode)
                    }),
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { if (inviteCode.isNotBlank()) onJoinFamily(inviteCode) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = inviteCode.isNotBlank(),
                ) {
                    Text("Beitreten")
                }
            }
        }
    }
}

@Composable
private fun FamilyContent(
    state: FamilyUiState.HasFamily,
    onCopyCode: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(state.family.name, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
        }

        item {
            InviteCodeCard(inviteCode = state.family.inviteCode, onCopyCode = onCopyCode)
        }

        item {
            Spacer(Modifier.height(8.dp))
            Text("Mitglieder (${state.members.size})", style = MaterialTheme.typography.titleMedium)
        }

        items(state.members, key = { it.id }) { member ->
            MemberRow(member = member)
        }
    }
}

@Composable
private fun InviteCodeCard(inviteCode: String, onCopyCode: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Einladungscode", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = inviteCode,
                    style = MaterialTheme.typography.headlineSmall,
                )
                Row {
                    IconButton(onClick = onCopyCode) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Code kopieren")
                    }
                }
            }
            Text(
                "Teile diesen Code mit Familienmitgliedern.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun MemberRow(member: FamilyMember) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .padding(end = 12.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(member.displayName, style = MaterialTheme.typography.bodyLarge)
            Text(member.email, style = MaterialTheme.typography.bodySmall)
        }
        Text(
            text = member.role.label(),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

private fun MemberRole.label(): String = when (this) {
    MemberRole.OWNER -> "Besitzer"
    MemberRole.ADMIN -> "Admin"
    MemberRole.MEMBER -> "Mitglied"
    MemberRole.VIEWER -> "Leser"
}
