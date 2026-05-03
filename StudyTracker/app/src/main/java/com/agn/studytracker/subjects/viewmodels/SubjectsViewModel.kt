package com.agn.studytracker.subjects.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agn.corea.models.session.Session
import com.agn.corea.models.session.SessionAssessment
import com.agn.corea.models.session.SessionTopic
import com.agn.corea.models.subjects.Page
import com.agn.corea.models.subjects.PageCumulativeProgress
import com.agn.corea.models.subjects.Para
import com.agn.corea.models.subjects.Subject
import com.agn.studytracker.common.objectbox.ObjectBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SubjectsUiState(
    val subjects: List<SubjectUiItem> = emptyList(),
    val isLoading: Boolean = false
)

data class SubjectUiItem(
    val subjectId: String,
    val title: String,
    val sessionCount: Long,
    val isLastSessionActive: Boolean
)

class SubjectsViewModel : ViewModel() {

    private val db get() = ObjectBox.get()

    private val _uiState = MutableStateFlow(SubjectsUiState())
    val uiState: StateFlow<SubjectsUiState> = _uiState.asStateFlow()

    init {
        loadSubjects()
    }

    fun loadSubjects() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val items = withContext(Dispatchers.IO) {
                db.subjectDao().getAll().map { subject ->
                    SubjectUiItem(
                        subjectId = subject.subjectId,
                        title = subject.subjectTitle,
                        sessionCount = db.sessionDao().countStartedBySubjectId(subject.subjectId),
                        isLastSessionActive = db.sessionDao().getFirstActiveBySubjectIdSync(subject.subjectId) != null
                    )
                }
            }
            _uiState.update { it.copy(subjects = items, isLoading = false) }
        }
    }

    fun createSubject(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                db.subjectDao().insert(Subject().apply {
                    subjectTitle = title
                    createdOn = System.currentTimeMillis()
                })
            }
            loadSubjects()
        }
    }

    suspend fun ensureFirstSessionExists(subjectId: String) {
        val sessions = withContext(Dispatchers.IO) { db.sessionDao().getBySubjectId(subjectId) }
        if (sessions.isEmpty()) {
            withContext(Dispatchers.IO) {
                db.sessionDao().insert(Session().apply {
                    this.subjectId = subjectId
                    createdOn = System.currentTimeMillis()
                    sessionSerialNo = 1
                    sessionTitle = "First Session"
                })
            }
        }
    }

    fun deleteSubject(subjectId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val subject = db.subjectDao().getBySubjectIdSync(subjectId) ?: return@launch
            val sessionList = db.sessionDao().getBySubjectIdSync(subjectId)
            val sectionList = db.sectionDao().getBySubjectIdSync(subjectId)
            val topList = ArrayList<SessionTopic>()
            val pageList = ArrayList<Page>()
            val progressList = ArrayList<PageCumulativeProgress>()
            val paraList = ArrayList<Para>()
            val assessmentList = ArrayList<SessionAssessment>()

            for (session in sessionList) {
                val topicList = db.sessionTopicDao().getBySessionIdSync(session.sessionId)
                for (topic in topicList) {
                    db.pageDao().getByPageIdSync(topic.firstPageId)?.let { page ->
                        pageList.add(page)
                        paraList.addAll(db.paraDao().getByPageIdSync(page.pageId))
                        db.pageCumulativeProgressDao().getByPageIdSync(page.pageId)?.let { progressList.add(it) }
                    }
                    if (!topic.secondPageId.isNullOrEmpty()) {
                        db.pageDao().getByPageIdSync(topic.secondPageId)?.let { page ->
                            pageList.add(page)
                            paraList.addAll(db.paraDao().getByPageIdSync(page.pageId))
                            db.pageCumulativeProgressDao().getByPageIdSync(page.pageId)?.let { progressList.add(it) }
                        }
                    }
                }
                topList.addAll(topicList)
                if (session.isSessionAssessed)
                    db.sessionAssessmentDao().getBySessionIdSync(session.sessionId)?.let { assessmentList.add(it) }
            }

            db.subjectDao().delete(subject)
            db.sectionDao().deleteList(sectionList)
            db.sessionDao().deleteList(sessionList)
            db.sessionTopicDao().deleteList(topList)
            db.pageDao().deleteList(pageList)
            db.pageCumulativeProgressDao().deleteList(progressList)
            db.paraDao().deleteList(paraList)
            db.sessionAssessmentDao().deleteList(assessmentList)

            withContext(Dispatchers.Main) { loadSubjects() }
        }
    }
}
