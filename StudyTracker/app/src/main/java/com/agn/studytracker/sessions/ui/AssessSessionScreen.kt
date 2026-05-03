package com.agn.studytracker.sessions.ui

import android.graphics.Typeface
import android.text.Html
import android.view.LayoutInflater
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.irshulx.Editor
import com.github.irshulx.models.EditorTextStyle
import com.agn.studytracker.sessions.viewmodels.AssessPageItem
import com.agn.studytracker.sessions.viewmodels.AssessSessionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssessSessionScreen(
    sessionId: String,
    onNavigateBack: () -> Unit,
    viewModel: AssessSessionViewModel = viewModel()
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
                title = { Text(state.sessionTitle) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFFFF8D))
            )
        }
    ) { padding ->
        if (!state.isLoading) {
            if (state.isAssessed && state.assessment != null) {
                ViewAssessmentContent(
                    assessment = state.assessment!!,
                    pages = state.pages,
                    modifier = Modifier.padding(padding)
                )
            } else if (!state.isAssessed) {
                EditAssessmentContent(
                    pages = state.pages,
                    modifier = Modifier.padding(padding),
                    onSubmit = { summary, questions, todos, planning ->
                        viewModel.submitAssessment(summary, questions, todos, planning, onNavigateBack)
                    }
                )
            }
        }
    }
}

@Composable
private fun EditAssessmentContent(
    pages: List<AssessPageItem>,
    modifier: Modifier = Modifier,
    onSubmit: (summary: String, questions: String, todos: String, planning: String) -> Unit
) {
    var summaryEditor by remember { mutableStateOf<Editor?>(null) }
    var questionsEditor by remember { mutableStateOf<Editor?>(null) }
    var todosEditor by remember { mutableStateOf<Editor?>(null) }
    var planningEditor by remember { mutableStateOf<Editor?>(null) }
    var activeEditor by remember { mutableStateOf<Editor?>(null) }

    val headingTypeface: Map<Int, String> = mapOf(
        Typeface.NORMAL to "fonts/GreycliffCF-Bold.ttf",
        Typeface.BOLD to "fonts/GreycliffCF-Heavy.ttf",
        Typeface.ITALIC to "fonts/GreycliffCF-Heavy.ttf",
        Typeface.BOLD_ITALIC to "fonts/GreycliffCF-Bold.ttf"
    )
    val contentTypeface: Map<Int, String> = mapOf(
        Typeface.NORMAL to "fonts/Lato-Medium.ttf",
        Typeface.BOLD to "fonts/Lato-Bold.ttf",
        Typeface.ITALIC to "fonts/Lato-MediumItalic.ttf",
        Typeface.BOLD_ITALIC to "fonts/Lato-BoldItalic.ttf"
    )

    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Session Topics :", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(pages) { page -> PageContentCard(page) }
            }

            Spacer(Modifier.height(8.dp))

            EditorSection(
                label = "What have you learnt in this session?",
                headingTypeface = headingTypeface,
                contentTypeface = contentTypeface,
                onEditorReady = { editor ->
                    summaryEditor = editor
                    editor.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) activeEditor = editor }
                }
            )

            EditorSection(
                label = "Questions/Doubts:",
                headingTypeface = headingTypeface,
                contentTypeface = contentTypeface,
                onEditorReady = { editor ->
                    questionsEditor = editor
                    editor.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) activeEditor = editor }
                }
            )

            EditorSection(
                label = "ToDo:",
                headingTypeface = headingTypeface,
                contentTypeface = contentTypeface,
                onEditorReady = { editor ->
                    todosEditor = editor
                    editor.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) activeEditor = editor }
                }
            )

            EditorSection(
                label = "What should I learn next?",
                headingTypeface = headingTypeface,
                contentTypeface = contentTypeface,
                onEditorReady = { editor ->
                    planningEditor = editor
                    editor.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) activeEditor = editor }
                }
            )

            Button(
                onClick = {
                    onSubmit(
                        summaryEditor?.contentAsHTML ?: "",
                        questionsEditor?.contentAsHTML ?: "",
                        todosEditor?.contentAsHTML ?: "",
                        planningEditor?.contentAsHTML ?: ""
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8BC34A))
            ) {
                Text("Assess Session", color = Color.White)
            }
        }

        EditorToolbar(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE6E6E6))
                .padding(horizontal = 4.dp, vertical = 4.dp),
            onAction = { action -> activeEditor?.let { action(it) } }
        )
    }
}

@Composable
private fun EditorSection(
    label: String,
    headingTypeface: Map<Int, String>,
    contentTypeface: Map<Int, String>,
    onEditorReady: (Editor) -> Unit
) {
    Text(label, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    AndroidView(
        factory = { ctx ->
            val inflater = LayoutInflater.from(ctx)
            val view = inflater.inflate(
                com.agn.studytracker.R.layout.layout_rich_editor, null
            ) as Editor
            view.headingTypeface = headingTypeface
            view.contentTypeface = contentTypeface
            view.setNormalTextSize(16)
            onEditorReady(view)
            view
        },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp)
    )
}

@Composable
private fun EditorToolbar(
    modifier: Modifier = Modifier,
    onAction: ((Editor) -> Unit) -> Unit
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf("H1", "H2", "H3").forEachIndexed { i, label ->
            TextButton(onClick = {
                val style = when (i) {
                    0 -> EditorTextStyle.H1
                    1 -> EditorTextStyle.H2
                    else -> EditorTextStyle.H3
                }
                onAction { it.updateTextStyle(style) }
            }) {
                Text(label, fontWeight = FontWeight.Bold, color = Color(0xFF444444))
            }
        }
        ToolbarTextButton("B", FontWeight.Bold) { onAction { it.updateTextStyle(EditorTextStyle.BOLD) } }
        ToolbarTextButton("I", fontStyle = FontStyle.Italic) { onAction { it.updateTextStyle(EditorTextStyle.ITALIC) } }
        ToolbarTextButton("→") { onAction { it.updateTextStyle(EditorTextStyle.INDENT) } }
        ToolbarTextButton("←") { onAction { it.updateTextStyle(EditorTextStyle.OUTDENT) } }
        ToolbarTextButton("•") { onAction { it.insertList(false) } }
        ToolbarTextButton("1.") { onAction { it.insertList(true) } }
        ToolbarTextButton("C") { onAction { it.updateTextColor("#FF3333") } }
        ToolbarTextButton("—") { onAction { it.insertDivider() } }
        ToolbarTextButton("IMG") { onAction { it.openImagePicker() } }
        ToolbarTextButton("Link") { onAction { it.insertLink() } }
        ToolbarTextButton("✕") { onAction { it.clearAllContents() } }
    }
}

@Composable
private fun ToolbarTextButton(
    label: String,
    fontWeight: FontWeight? = null,
    fontStyle: FontStyle? = null,
    onClick: () -> Unit
) {
    TextButton(onClick = onClick) {
        Text(
            text = label,
            color = Color(0xFF444444),
            fontWeight = fontWeight,
            fontStyle = fontStyle,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun PageContentCard(page: AssessPageItem) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(120.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF00FFFF).copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(page.pageTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 2)
            Spacer(Modifier.height(4.dp))
            Text(
                text = page.paraContent,
                fontSize = 11.sp,
                color = Color.DarkGray,
                maxLines = 5
            )
        }
    }
}

@Composable
private fun ViewAssessmentContent(
    assessment: com.agn.corea.models.session.SessionAssessment,
    pages: List<AssessPageItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AssessmentSection(
            label = "Session Summary",
            htmlContent = assessment.sessionSummary
        )
        AssessmentSection(
            label = "Questions/Doubts",
            htmlContent = assessment.questions ?: "..."
        )
        AssessmentSection(
            label = "ToDo",
            htmlContent = assessment.todos ?: "..."
        )
        AssessmentSection(
            label = "Next Session Plan",
            htmlContent = assessment.nextSessionPlan
        )
    }
}

@Composable
private fun AssessmentSection(label: String, htmlContent: String?) {
    Column {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF00FFFF).copy(alpha = 0.2f))
                .padding(8.dp)
        ) {
            AndroidView(
                factory = { ctx ->
                    android.widget.TextView(ctx).also { tv ->
                        tv.text = Html.fromHtml(htmlContent ?: "", Html.FROM_HTML_MODE_COMPACT)
                    }
                },
                update = { tv ->
                    tv.text = Html.fromHtml(htmlContent ?: "", Html.FROM_HTML_MODE_COMPACT)
                }
            )
        }
    }
}
