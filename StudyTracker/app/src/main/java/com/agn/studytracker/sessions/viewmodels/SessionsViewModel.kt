package com.agn.studytracker.sessions.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agn.corea.constants.GlobalConstants
import com.agn.corea.models.session.Session
import com.agn.studytracker.common.objectbox.ObjectBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

data class SessionsUiState(
    val sessions: List<Session> = emptyList(),
    val isLoading: Boolean = false,
    val stopConfirmSession: Session? = null,
    val toastMessage: String? = null
)

class SessionsViewModel : ViewModel() {

    private val db get() = ObjectBox.get()

    private val _uiState = MutableStateFlow(SessionsUiState())
    val uiState: StateFlow<SessionsUiState> = _uiState.asStateFlow()

    private var subjectId: String = ""

    companion object {
        private val SESSION_TITLES = listOf(
            "First Session", "Second Session", "Third Session", "Fourth Session",
            "Fifth Session", "Sixth Session", "Seventh Session", "Eighth Session",
            "Ninth Session", "Tenth Session", "Eleventh Session", "Twelfth Session",
            "Thirteenth Session", "Fourteenth Session", "Fifteenth Session",
            "Sixteenth Session", "Seventeenth Session", "Eighteenth Session",
            "Nineteenth Session", "Twentieth Session"
        )
    }

    fun loadSessions(subjectId: String) {
        this.subjectId = subjectId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val sessions = withContext(Dispatchers.IO) { db.sessionDao().getBySubjectId(subjectId) }
            _uiState.update { it.copy(sessions = sessions, isLoading = false) }
            if (sessions.isNotEmpty()) handleAutoActions(sessions)
        }
    }

    private fun handleAutoActions(sessions: List<Session>) {
        val current = sessions.last()
        if (current.expiresOn > 0 && current.expiresOn < System.currentTimeMillis() && current.isSessionActive) {
            endCurrentSession(current)
            return
        }
        if (!current.isSessionActive && current.isSessionAssessed) {
            createNewSession(current.sessionSerialNo + 1)
        }
    }

    private fun endCurrentSession(session: Session) {
        session.endedOn = System.currentTimeMillis()
        session.hasSessionEnded = true
        session.isSessionActive = false
        session.hasSessionExpired = true
        viewModelScope.launch {
            withContext(Dispatchers.IO) { db.sessionDao().update(session) }
            loadSessions(subjectId)
        }
    }

    private fun createNewSession(slno: Int) {
        if (slno <= 0 || slno > SESSION_TITLES.size) return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                db.sessionDao().insert(Session().apply {
                    this.subjectId = this@SessionsViewModel.subjectId
                    createdOn = System.currentTimeMillis()
                    sessionSerialNo = slno
                    sessionTitle = SESSION_TITLES[slno - 1]
                })
            }
            showToast("New Session Created")
            loadSessions(subjectId)
        }
    }

    fun startSession(session: Session) {
        viewModelScope.launch {
            val topicsCount = withContext(Dispatchers.IO) { db.sessionTopicDao().countBySessionId(session.sessionId) }
            if (topicsCount <= 0) { showToast("Topics not defined"); return@launch }
            val topicsList = withContext(Dispatchers.IO) { db.sessionTopicDao().getBySessionIdSync(session.sessionId) }
            var totalStoryPoints = 0.0f
            for (topic in topicsList) totalStoryPoints += topic.topicStoryPoints
            val days = (totalStoryPoints / GlobalConstants.DAILY_STUDY_HOURS_IN_STORY_POINTS).roundToInt()
            session.startedOn = System.currentTimeMillis()
            session.isSessionActive = true
            session.expiresOn = session.startedOn + days * 24L * 60 * 60 * 1000
            withContext(Dispatchers.IO) { db.sessionDao().update(session) }
            showToast("This session has started")
            loadSessions(subjectId)
        }
    }

    fun requestStopSession(session: Session) {
        _uiState.update { it.copy(stopConfirmSession = session) }
    }

    fun confirmStopSession() {
        val session = _uiState.value.stopConfirmSession ?: return
        _uiState.update { it.copy(stopConfirmSession = null) }
        endCurrentSession(session)
    }

    fun dismissStopConfirm() {
        _uiState.update { it.copy(stopConfirmSession = null) }
    }

    private fun showToast(message: String) {
        _uiState.update { it.copy(toastMessage = message) }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}
