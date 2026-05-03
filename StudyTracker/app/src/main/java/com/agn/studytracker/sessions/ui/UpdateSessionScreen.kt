package com.agn.studytracker.sessions.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.agn.studytracker.R
import com.agn.studytracker.sessions.viewmodels.UpdatePageItem
import com.agn.studytracker.sessions.viewmodels.UpdateSectionItem
import com.agn.studytracker.sessions.viewmodels.UpdateSessionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateSessionScreen(
    sessionId: String,
    onNavigateBack: () -> Unit,
    viewModel: UpdateSessionViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

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
                title = { Text("Update ${state.sessionTitle}") },
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
            state.sections.forEach { section ->
                item(key = section.sectionId) {
                    UpdateSectionRow(
                        section = section,
                        onToggle = { viewModel.toggleSection(section.sectionId) }
                    )
                }
                if (section.isExpanded) {
                    items(section.pages, key = { it.pageId }) { page ->
                        UpdatePageRow(
                            page = page,
                            isFrozen = state.isFrozen,
                            onAction = { action -> viewModel.onAction(page.pageId, action) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UpdateSectionRow(section: UpdateSectionItem, onToggle: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onToggle),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = section.title,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            Icon(
                painter = painterResource(if (section.isExpanded) R.drawable.ic_up else R.drawable.ic_down),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun UpdatePageRow(
    page: UpdatePageItem,
    isFrozen: Boolean,
    onAction: (Int) -> Unit
) {
    val percentCovered = if (page.pageStoryPoints > 0)
        Math.round((page.totalCovered / page.pageStoryPoints) * 100) else 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (percentCovered == 100) Color(0xFFE8F5E9) else Color.White
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = page.title,
                    modifier = Modifier.weight(1f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$percentCovered%",
                    fontSize = 12.sp,
                    color = Color(0xFF4CAF50)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                ActionButton(
                    iconRes = if (page.isRead) R.drawable.ic_read else R.drawable.ic_not_read,
                    enabled = !isFrozen && !page.isRead,
                    onClick = { onAction(UpdateSessionViewModel.ACTION_READ) }
                )
                ActionButton(
                    iconRes = if (page.isNotesTaken) R.drawable.ic_notes else R.drawable.ic_notes_not_taken,
                    enabled = !isFrozen && !page.isNotesTaken,
                    onClick = { onAction(UpdateSessionViewModel.ACTION_TAKE_NOTES) }
                )
                ActionButton(
                    iconRes = if (page.isMemorized) R.drawable.ic_memorized else R.drawable.ic_not_memorized,
                    enabled = !isFrozen && !page.isMemorized,
                    onClick = { onAction(UpdateSessionViewModel.ACTION_MEMORIZE) }
                )
                ActionButton(
                    iconRes = if (page.reviewCount > 0) R.drawable.ic_reviewed_once else R.drawable.ic_not_reviewed,
                    label = if (page.reviewCount > 0) "×${page.reviewCount}" else null,
                    enabled = !isFrozen,
                    onClick = { onAction(UpdateSessionViewModel.ACTION_REVIEW) }
                )
                ActionButton(
                    iconRes = if (page.practiceCount > 0) R.drawable.ic_practiced_once else R.drawable.ic_not_practiced,
                    label = if (page.practiceCount > 0) "×${page.practiceCount}" else null,
                    enabled = !isFrozen,
                    onClick = { onAction(UpdateSessionViewModel.ACTION_PRACTICE) }
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    iconRes: Int,
    label: String? = null,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(22.dp)
            )
        }
        label?.let {
            Text(it, fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(start = 2.dp))
        }
    }
}
