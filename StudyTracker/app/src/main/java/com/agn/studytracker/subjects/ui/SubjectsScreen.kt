package com.agn.studytracker.subjects.ui

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.agn.studytracker.R
import com.agn.studytracker.subjects.viewmodels.SubjectUiItem
import com.agn.studytracker.subjects.viewmodels.SubjectsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsScreen(
    onNavigateToSessions: (subjectId: String) -> Unit,
    onNavigateToDetails: (subjectId: String, subjectName: String) -> Unit,
    viewModel: SubjectsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }
    var deleteSubjectId by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Subjects") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFFFF8D))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = Color(0xFF8BC34A)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Subject", tint = Color.White)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(state.subjects, key = { it.subjectId }) { item ->
                SubjectCard(
                    item = item,
                    onClick = { onNavigateToDetails(item.subjectId, item.title) },
                    onLongClick = { deleteSubjectId = item.subjectId },
                    onSessionsClick = {
                        coroutineScope.launch {
                            viewModel.ensureFirstSessionExists(item.subjectId)
                            onNavigateToSessions(item.subjectId)
                        }
                    }
                )
            }
        }
    }

    if (showCreateDialog) {
        CreateSubjectDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { title ->
                viewModel.createSubject(title)
                showCreateDialog = false
            }
        )
    }

    deleteSubjectId?.let { id ->
        AlertDialog(
            onDismissRequest = { deleteSubjectId = null },
            title = { Text("Delete Subject") },
            text = { Text("Are you sure you want to delete this subject and all related data?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSubject(id)
                    deleteSubjectId = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deleteSubjectId = null }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SubjectCard(
    item: SubjectUiItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onSessionsClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        elevation = CardDefaults.cardElevation(5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.title,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            Box(contentAlignment = Alignment.Center) {
                IconButton(onClick = onSessionsClick) {
                    Icon(
                        painter = painterResource(
                            if (item.isLastSessionActive) R.drawable.ic_education else R.drawable.ic_education_1
                        ),
                        contentDescription = "Sessions",
                        tint = Color.Unspecified
                    )
                }
                if (item.sessionCount > 0) {
                    Surface(
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = 4.dp),
                        shape = CircleShape,
                        color = if (item.isLastSessionActive) Color(0xFF4CAF50) else Color(0xFF9E9E9E)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = item.sessionCount.toString(),
                                color = Color.White,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateSubjectDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Subject") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Subject name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { if (text.isNotBlank()) onCreate(text) }, enabled = text.isNotBlank()) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
