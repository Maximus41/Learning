package com.agn.studytracker.sessions.ui

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agn.studytracker.sessions.viewmodels.EditSessionViewModel
import com.agn.studytracker.sessions.viewmodels.EditTopicItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSessionScreen(
    sessionId: String,
    onNavigateBack: () -> Unit,
    viewModel: EditSessionViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var deleteTopicObId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(sessionId) {
        viewModel.loadSession(sessionId)
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
                title = { Text("Edit ${state.sessionTitle}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFFFF8D))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddTopicDialog() },
                containerColor = Color(0xFF8BC34A)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Topic", tint = Color.White)
            }
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
            if (state.approxTimeHours > 0) {
                item {
                    Text(
                        text = "Approx Effort Needed : ${state.approxTimeHours} hrs",
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF9C4))
                            .padding(12.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            items(state.topics) { item ->
                when (item) {
                    is EditTopicItem.Section -> SectionTopicRow(
                        title = item.title,
                        onLongClick = { deleteTopicObId = item.topicObId }
                    )
                    is EditTopicItem.Page -> PageTopicRow(
                        title = item.title,
                        paraContent = item.paraContent,
                        onClick = { viewModel.openAddParaDialog(item.topicId, item.pageId) }
                    )
                }
            }
        }
    }

    if (state.showAddTopicDialog) {
        AddTopicDialog(
            existingSections = state.existingSections,
            existingPages = state.existingPages,
            onDismiss = { viewModel.dismissAddTopicDialog() },
            onCreate = { section, page1, page2 ->
                viewModel.createTopic(section, page1, page2)
                viewModel.dismissAddTopicDialog()
            }
        )
    }

    if (state.showAddParaDialog) {
        AddParasDialog(
            onDismiss = { viewModel.dismissAddParaDialog() },
            onCreate = { paraTitles ->
                viewModel.createParas(paraTitles)
            }
        )
    }

    deleteTopicObId?.let { obId ->
        AlertDialog(
            onDismissRequest = { deleteTopicObId = null },
            title = { Text("Delete Topic") },
            text = { Text("Delete this topic and its pages?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTopic(obId)
                    deleteTopicObId = null
                }) { Text("Delete", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTopicObId = null }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SectionTopicRow(title: String, onLongClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .combinedClickable(onClick = {}, onLongClick = onLongClick),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PageTopicRow(title: String, paraContent: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF00FFFF).copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .combinedClickable(onClick = onClick)
        ) {
            Text(title, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            if (paraContent.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = paraContent.replace(Regex("<[^>]+>"), "").trim(),
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun AddTopicDialog(
    existingSections: List<String>,
    existingPages: List<String>,
    onDismiss: () -> Unit,
    onCreate: (String, String, String) -> Unit
) {
    var sectionTitle by remember { mutableStateOf("") }
    var firstPage by remember { mutableStateOf("") }
    var secondPage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Topic") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AutoCompleteTextField(
                    value = sectionTitle,
                    onValueChange = { sectionTitle = it },
                    label = "Section",
                    suggestions = existingSections
                )
                AutoCompleteTextField(
                    value = firstPage,
                    onValueChange = { firstPage = it },
                    label = "First Page",
                    suggestions = existingPages
                )
                AutoCompleteTextField(
                    value = secondPage,
                    onValueChange = { secondPage = it },
                    label = "Second Page (optional)",
                    suggestions = existingPages
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (sectionTitle.isNotBlank() && firstPage.isNotBlank())
                        onCreate(sectionTitle.trim(), firstPage.trim(), secondPage.trim())
                },
                enabled = sectionTitle.isNotBlank() && firstPage.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AutoCompleteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    suggestions: List<String>
) {
    var expanded by remember { mutableStateOf(false) }
    val filtered = remember(value, suggestions) {
        if (value.isBlank()) emptyList()
        else suggestions.filter { it.contains(value, ignoreCase = true) }.take(5)
    }

    Column {
        OutlinedTextField(
            value = value,
            onValueChange = { onValueChange(it); expanded = it.isNotBlank() && filtered.isNotEmpty() },
            label = { Text(label) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (expanded && filtered.isNotEmpty()) {
            Card(elevation = CardDefaults.cardElevation(4.dp)) {
                Column {
                    filtered.forEach { suggestion ->
                        Text(
                            text = suggestion,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onValueChange(suggestion); expanded = false }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddParasDialog(
    onDismiss: () -> Unit,
    onCreate: (List<String>) -> Unit
) {
    val paraValues = remember { List(5) { mutableStateOf("") } }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Paragraphs") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..5).forEach { i ->
                    OutlinedTextField(
                        value = paraValues[i - 1].value,
                        onValueChange = { paraValues[i - 1].value = it },
                        label = { Text("Para $i") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            val nonEmpty = paraValues.map { it.value.trim() }.filter { it.isNotEmpty() }
            TextButton(
                onClick = { if (nonEmpty.isNotEmpty()) onCreate(nonEmpty) },
                enabled = paraValues.any { it.value.isNotBlank() }
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
