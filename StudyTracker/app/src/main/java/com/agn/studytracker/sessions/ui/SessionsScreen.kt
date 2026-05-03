package com.agn.studytracker.sessions.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agn.corea.models.session.Session
import com.agn.studytracker.R
import com.agn.studytracker.sessions.viewmodels.SessionsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionsScreen(
    subjectId: String,
    onNavigateToUpdate: (sessionId: String) -> Unit,
    onNavigateToEdit: (sessionId: String) -> Unit,
    onNavigateToAssess: (sessionId: String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: SessionsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(subjectId) {
        viewModel.loadSessions(subjectId)
    }

    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sessions") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFFFF8D))
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(state.sessions, key = { it.sessionId }) { session ->
                SessionCard(
                    session = session,
                    onClick = { onNavigateToUpdate(session.sessionId) },
                    onEditClick = { onNavigateToEdit(session.sessionId) },
                    onStartClick = { viewModel.startSession(session) },
                    onStopClick = { viewModel.requestStopSession(session) },
                    onAssessClick = { onNavigateToAssess(session.sessionId) }
                )
            }
        }
    }

    state.stopConfirmSession?.let {
        AlertDialog(
            onDismissRequest = { viewModel.dismissStopConfirm() },
            text = { Text("Are you sure you want to stop the session?") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmStopSession() }) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissStopConfirm() }) { Text("No") }
            }
        )
    }
}

@Composable
private fun SessionCard(
    session: Session,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit,
    onAssessClick: () -> Unit
) {
    val canEdit = !session.isSessionActive && !session.hasSessionEnded
    val canStart = !session.isSessionActive && !session.hasSessionEnded && !session.isSessionAssessed
    val canStop = session.isSessionActive
    val canAssess = session.hasSessionEnded && !session.isSessionAssessed

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(65.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = session.sessionTitle ?: "",
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
            if (canEdit) {
                IconButton(onClick = onEditClick, modifier = Modifier.size(40.dp)) {
                    Icon(
                        painter = painterResource(R.drawable.ic_edit),
                        contentDescription = "Edit",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            if (canStart) {
                IconButton(onClick = onStartClick, modifier = Modifier.size(40.dp)) {
                    Icon(
                        painter = painterResource(R.drawable.ic_play),
                        contentDescription = "Start",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            if (canStop) {
                IconButton(onClick = onStopClick, modifier = Modifier.size(40.dp)) {
                    Icon(
                        painter = painterResource(R.drawable.ic_stop),
                        contentDescription = "Stop",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            if (canAssess) {
                IconButton(onClick = onAssessClick, modifier = Modifier.size(40.dp)) {
                    Icon(
                        painter = painterResource(R.drawable.ic_assessment),
                        contentDescription = "Assess",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
