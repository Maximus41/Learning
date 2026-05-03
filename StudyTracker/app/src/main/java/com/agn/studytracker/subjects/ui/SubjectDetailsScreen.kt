package com.agn.studytracker.subjects.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agn.corea.models.subjects.Para
import com.agn.studytracker.R
import com.agn.studytracker.subjects.viewmodels.PageUiItem
import com.agn.studytracker.subjects.viewmodels.SectionUiItem
import com.agn.studytracker.subjects.viewmodels.SubjectDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailsScreen(
    subjectId: String,
    subjectName: String,
    onNavigateBack: () -> Unit,
    viewModel: SubjectDetailsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(subjectId) {
        viewModel.init(subjectId, subjectName)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$subjectName Progress") },
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
            items(state.sections, key = { it.sectionId }) { section ->
                SectionRow(
                    section = section,
                    onToggle = { viewModel.toggleSection(section.sectionId) },
                    onPageClick = { page -> viewModel.showParagraphsDialog(page) }
                )
            }
        }
    }

    state.paragraphsDialogPage?.let { page ->
        ParagraphsDialog(
            page = page,
            paras = state.paragraphsDialogParas,
            onDismiss = { viewModel.dismissParagraphsDialog() }
        )
    }
}

@Composable
private fun SectionRow(
    section: SectionUiItem,
    onToggle: () -> Unit,
    onPageClick: (PageUiItem) -> Unit
) {
    Column {
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
                Text(
                    text = "${section.progressPercent}%",
                    fontSize = 13.sp,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.padding(end = 8.dp)
                )
                Icon(
                    painter = painterResource(if (section.isExpanded) R.drawable.ic_up else R.drawable.ic_down),
                    contentDescription = if (section.isExpanded) "Collapse" else "Expand",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (section.isExpanded) {
            section.pages.forEach { page ->
                PageRow(page = page, onClick = { onPageClick(page) })
            }
        }
    }
}

@Composable
private fun PageRow(page: PageUiItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp)
            .padding(start = 8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = page.title,
                modifier = Modifier.weight(1f),
                fontSize = 14.sp
            )
            StatusIcon(R.drawable.ic_read, R.drawable.ic_not_read, page.isRead)
            StatusIcon(R.drawable.ic_notes, R.drawable.ic_notes_not_taken, page.isNotesTaken)
            StatusIcon(R.drawable.ic_memorized, R.drawable.ic_not_memorized, page.isMemorized)
            StatusIcon(R.drawable.ic_reviewed_once, R.drawable.ic_not_reviewed, page.reviewCount > 0)
            StatusIcon(R.drawable.ic_practiced_once, R.drawable.ic_not_practiced, page.practiceCount > 0)
        }
    }
}

@Composable
private fun StatusIcon(activeRes: Int, inactiveRes: Int, isActive: Boolean) {
    Icon(
        painter = painterResource(if (isActive) activeRes else inactiveRes),
        contentDescription = null,
        tint = Color.Unspecified,
        modifier = Modifier
            .size(20.dp)
            .padding(horizontal = 1.dp)
    )
}

@Composable
private fun ParagraphsDialog(
    page: PageUiItem,
    paras: List<Para>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(page.title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    StatusIcon(R.drawable.ic_read, R.drawable.ic_not_read, page.isRead)
                    StatusIcon(R.drawable.ic_notes, R.drawable.ic_notes_not_taken, page.isNotesTaken)
                    StatusIcon(R.drawable.ic_memorized, R.drawable.ic_not_memorized, page.isMemorized)
                    StatusIcon(R.drawable.ic_reviewed_once, R.drawable.ic_not_reviewed, page.reviewCount > 0)
                    StatusIcon(R.drawable.ic_practiced_once, R.drawable.ic_not_practiced, page.practiceCount > 0)
                    if (page.reviewCount > 0) {
                        Text(" ×${page.reviewCount}", fontSize = 11.sp, color = Color.Gray)
                    }
                    if (page.practiceCount > 0) {
                        Text(" ×${page.practiceCount}", fontSize = 11.sp, color = Color.Gray)
                    }
                }
                Spacer(Modifier.height(4.dp))
                val paraArr = paras.toTypedArray()
                listOf(
                    if (paraArr.isNotEmpty()) paraArr[0].paraTitle else "N/A",
                    if (paraArr.size >= 2) paraArr[1].paraTitle else "N/A",
                    if (paraArr.size >= 3) paraArr[2].paraTitle else "N/A",
                    if (paraArr.size >= 4) paraArr[3].paraTitle else "N/A",
                    if (paraArr.size == 5) paraArr[4].paraTitle else "N/A"
                ).forEach { paraText ->
                    Text(paraText, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
