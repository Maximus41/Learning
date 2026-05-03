package com.agn.studytracker.sessions.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agn.corea.models.session.Session
import com.agn.corea.models.session.SessionAssessment
import com.agn.studytracker.common.objectbox.ObjectBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AssessSessionUiState(
    val sessionTitle: String = "",
    val isAssessed: Boolean = false,
    val pages: List<AssessPageItem> = emptyList(),
    val assessment: SessionAssessment? = null,
    val isLoading: Boolean = false,
    val toastMessage: String? = null
)

data class AssessPageItem(
    val pageId: String,
    val pageTitle: String,
    val paraContent: String
)

class AssessSessionViewModel : ViewModel() {

    private val db get() = ObjectBox.get()

    private val _uiState = MutableStateFlow(AssessSessionUiState())
    val uiState: StateFlow<AssessSessionUiState> = _uiState.asStateFlow()

    private var sessionId: String = ""
    private var sessionObj: Session? = null

    fun loadSession(sessionId: String) {
        if (this.sessionId == sessionId) return
        this.sessionId = sessionId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val sessions = withContext(Dispatchers.IO) { db.sessionDao().getBySessionId(sessionId) }
            if (sessions.isEmpty()) return@launch
            val session = sessions[0]
            sessionObj = session
            if (!session.isSessionAssessed) {
                _uiState.update { it.copy(sessionTitle = "Assess ${session.sessionTitle}", isAssessed = false) }
                loadSessionPages()
            } else {
                loadAssessment(session)
            }
        }
    }

    private suspend fun loadSessionPages() {
        val pageList = withContext(Dispatchers.IO) {
            val topics = db.sessionTopicDao().getBySessionId(sessionId)
            val list = ArrayList<AssessPageItem>()
            for (topic in topics) {
                if (!topic.firstPageId.isNullOrEmpty())
                    list.add(buildPageItem(topic.firstPageId, topic.firstPageTitle ?: ""))
                if (!topic.secondPageId.isNullOrEmpty())
                    list.add(buildPageItem(topic.secondPageId, topic.secondPageTitle ?: ""))
            }
            list
        }
        _uiState.update { it.copy(pages = pageList, isLoading = false) }
    }

    private fun buildPageItem(pageId: String, pageTitle: String): AssessPageItem {
        val paras = db.paraDao().getByPageIdSync(pageId)
        val content = if (paras.isNotEmpty()) buildString {
            paras.forEachIndexed { i, para -> append("\n    ${i + 1} -> ${para.paraTitle}") }
        } else ""
        return AssessPageItem(pageId, pageTitle, content)
    }

    private suspend fun loadAssessment(session: Session) {
        val assessments = withContext(Dispatchers.IO) { db.sessionAssessmentDao().getBySessionId(session.sessionId) }
        _uiState.update {
            it.copy(
                sessionTitle = "View Assessment",
                isAssessed = true,
                assessment = assessments.firstOrNull(),
                isLoading = false
            )
        }
    }

    fun submitAssessment(summary: String, questions: String, todos: String, planning: String, onDone: () -> Unit) {
        if (summary.isBlank() || questions.isBlank() || todos.isBlank() || planning.isBlank()) {
            _uiState.update { it.copy(toastMessage = "Assessment fields cannot be empty") }
            return
        }
        val assessment = SessionAssessment().apply {
            this.sessionId = this@AssessSessionViewModel.sessionId
            sessionSummary = summary
            nextSessionPlan = planning
            this.questions = questions
            this.todos = todos
        }
        viewModelScope.launch {
            val id = withContext(Dispatchers.IO) {
                val insertId = db.sessionAssessmentDao().insert(assessment)
                if (insertId > 0L) {
                    sessionObj?.let { session ->
                        session.isSessionAssessed = true
                        db.sessionDao().update(session)
                    }
                }
                insertId
            }
            if (id > 0L) {
                _uiState.update { it.copy(toastMessage = "Assessment Submitted") }
                onDone()
            } else {
                _uiState.update { it.copy(toastMessage = "Assessment couldn't be submitted! Please try again later") }
            }
        }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}
