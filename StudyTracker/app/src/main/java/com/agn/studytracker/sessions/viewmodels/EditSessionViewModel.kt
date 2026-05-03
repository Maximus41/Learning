package com.agn.studytracker.sessions.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agn.corea.constants.GlobalConstants
import com.agn.corea.constants.PageActionStatus
import com.agn.corea.models.session.SessionTopic
import com.agn.corea.models.subjects.Page
import com.agn.corea.models.subjects.PageCumulativeProgress
import com.agn.corea.models.subjects.Para
import com.agn.corea.models.subjects.Section
import com.agn.studytracker.common.objectbox.ObjectBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

data class EditSessionUiState(
    val sessionTitle: String = "",
    val approxTimeHours: Int = 0,
    val topics: List<EditTopicItem> = emptyList(),
    val existingSections: List<String> = emptyList(),
    val existingPages: List<String> = emptyList(),
    val showAddTopicDialog: Boolean = false,
    val showAddParaDialog: Boolean = false,
    val addParaTopicId: String = "",
    val addParaPageId: String = "",
    val toastMessage: String? = null,
    val isLoading: Boolean = false
)

sealed class EditTopicItem {
    data class Section(
        val sectionId: String,
        val title: String,
        val topicId: String,
        val topicObId: Long
    ) : EditTopicItem()

    data class Page(
        val pageId: String,
        val sectionId: String,
        val title: String,
        val topicId: String,
        val paraContent: String
    ) : EditTopicItem()
}

class EditSessionViewModel : ViewModel() {

    private val db get() = ObjectBox.get()

    private val _uiState = MutableStateFlow(EditSessionUiState())
    val uiState: StateFlow<EditSessionUiState> = _uiState.asStateFlow()

    private var sessionId: String = ""
    private var subjectId: String = ""
    private var sessionStoryPoints: Float = 0f

    fun loadSession(sessionId: String) {
        if (this.sessionId == sessionId) return
        this.sessionId = sessionId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val sessions = withContext(Dispatchers.IO) { db.sessionDao().getBySessionId(sessionId) }
            if (sessions.isEmpty()) return@launch
            val session = sessions[0]
            subjectId = session.subjectId ?: ""
            sessionStoryPoints = session.sessionStoryPoints
            _uiState.update { it.copy(sessionTitle = session.sessionTitle ?: "", isLoading = false) }
            loadTopics()
        }
    }

    private suspend fun loadTopics() {
        val items = withContext(Dispatchers.IO) {
            val topics = db.sessionTopicDao().getBySessionId(sessionId)
            val list = ArrayList<EditTopicItem>()
            for (topic in topics) {
                list.add(EditTopicItem.Section(
                    sectionId = topic.sectionId ?: "",
                    title = topic.sectionTitle ?: "",
                    topicId = topic.topicId,
                    topicObId = topic.obId
                ))
                if (!topic.firstPageId.isNullOrEmpty()) {
                    list.add(EditTopicItem.Page(
                        pageId = topic.firstPageId,
                        sectionId = topic.sectionId ?: "",
                        title = topic.firstPageTitle ?: "",
                        topicId = topic.topicId,
                        paraContent = formatParaContent(topic.firstPageId)
                    ))
                }
                if (!topic.secondPageId.isNullOrEmpty()) {
                    list.add(EditTopicItem.Page(
                        pageId = topic.secondPageId,
                        sectionId = topic.sectionId ?: "",
                        title = topic.secondPageTitle ?: "",
                        topicId = topic.topicId,
                        paraContent = formatParaContent(topic.secondPageId)
                    ))
                }
            }
            list
        }
        val approxTime = (sessionStoryPoints * GlobalConstants.ONE_STORY_POINT_IN_HOURS).roundToInt()
        _uiState.update { it.copy(topics = items, approxTimeHours = approxTime) }
    }

    private fun formatParaContent(pageId: String): String {
        val list = db.paraDao().getByPageIdSync(pageId)
        if (list.isEmpty()) return ""
        return buildString {
            append("<ul>\n")
            list.forEach { append("<li>      ${it.paraTitle}</li>\n") }
            append("</ul>\n")
        }
    }

    fun openAddTopicDialog() {
        viewModelScope.launch {
            val (sectionNames, pageNames) = withContext(Dispatchers.IO) {
                val sections = db.sectionDao().getBySubjectId(subjectId)
                val sNames = sections.map { it.sectionTitle }
                val pNames = sections.flatMap { db.pageDao().getBySectionIdSync(it.sectionId).map { p -> p.pageTitle } }
                Pair(sNames, pNames)
            }
            _uiState.update { it.copy(showAddTopicDialog = true, existingSections = sectionNames, existingPages = pageNames) }
        }
    }

    fun dismissAddTopicDialog() {
        _uiState.update { it.copy(showAddTopicDialog = false) }
    }

    fun openAddParaDialog(topicId: String, pageId: String) {
        if (db.paraDao().countByPageId(pageId) > 0) return
        _uiState.update { it.copy(showAddParaDialog = true, addParaTopicId = topicId, addParaPageId = pageId) }
    }

    fun dismissAddParaDialog() {
        _uiState.update { it.copy(showAddParaDialog = false, addParaTopicId = "", addParaPageId = "") }
    }

    fun createTopic(sectionTitle: String, firstPage: String, secondPage: String) {
        if (sectionTitle.isBlank() || firstPage == secondPage) return
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                val existing = db.sessionTopicDao().getBySessionAndSectionTitleSync(sessionId, sectionTitle)
                if (existing != null) return@withContext "duplicate_section"

                val section = db.sectionDao().getByTitleAndSubjectSync(sectionTitle, subjectId)
                    ?: Section().apply {
                        createdOn = System.currentTimeMillis()
                        this.sectionTitle = sectionTitle
                        this.subjectId = this@EditSessionViewModel.subjectId
                    }

                val topic = SessionTopic()

                if (firstPage.isNotBlank()) {
                    if (db.pageDao().getByTitleAndSectionSync(firstPage, section.sectionId) != null)
                        return@withContext "duplicate_page:$firstPage"
                    val page1 = Page().apply { createdOn = System.currentTimeMillis(); pageTitle = firstPage; sectionId = section.sectionId }
                    val prog1 = PageCumulativeProgress().apply {
                        pageId = page1.pageId
                        memorizedStatus = PageActionStatus.PENDING
                        readStatus = PageActionStatus.PENDING
                        notesTakenStatus = PageActionStatus.PENDING
                    }
                    section.totalStoryPoints += page1.pageStoryPoints
                    topic.firstPageId = page1.pageId
                    topic.firstPageStoryPoints = page1.pageStoryPoints
                    topic.firstPageTitle = page1.pageTitle
                    topic.topicStoryPoints += page1.pageStoryPoints
                    db.pageDao().insert(page1)
                    db.pageCumulativeProgressDao().insert(prog1)
                }

                if (secondPage.isNotBlank()) {
                    if (db.pageDao().getByTitleAndSectionSync(secondPage, section.sectionId) != null)
                        return@withContext "duplicate_page:$secondPage"
                    val page2 = Page().apply { createdOn = System.currentTimeMillis(); pageTitle = secondPage; sectionId = section.sectionId }
                    val prog2 = PageCumulativeProgress().apply {
                        pageId = page2.pageId
                        memorizedStatus = PageActionStatus.PENDING
                        readStatus = PageActionStatus.PENDING
                        notesTakenStatus = PageActionStatus.PENDING
                    }
                    section.totalStoryPoints += page2.pageStoryPoints
                    topic.secondPageId = page2.pageId
                    topic.secondPageStoryPoints = page2.pageStoryPoints
                    topic.secondPageTitle = page2.pageTitle
                    topic.topicStoryPoints += page2.pageStoryPoints
                    db.pageDao().insert(page2)
                    db.pageCumulativeProgressDao().insert(prog2)
                }

                section.noOfPages = db.pageDao().getBySectionIdSync(section.sectionId).size
                topic.sectionId = section.sectionId
                topic.sectionTitle = section.sectionTitle
                topic.sessionId = sessionId
                topic.createdOn = System.currentTimeMillis()

                val sessions = db.sessionDao().getBySessionId(sessionId)
                if (sessions.isNotEmpty()) {
                    val session = sessions[0]
                    session.sessionStoryPoints += topic.topicStoryPoints
                    sessionStoryPoints = session.sessionStoryPoints
                    val secId = db.sectionDao().insert(section)
                    if (secId > 0) {
                        db.sessionTopicDao().insert(topic)
                        db.sessionDao().update(session)
                    }
                }
                "ok"
            }
            when {
                result == "duplicate_section" -> showToast("You have already added $sectionTitle to this session")
                result.startsWith("duplicate_page:") -> showToast("You have already learnt ${result.substringAfter(":")}")
                else -> loadTopics()
            }
        }
    }

    fun createParas(paraTitles: List<String>) {
        val topicId = _uiState.value.addParaTopicId
        val pageId = _uiState.value.addParaPageId
        if (topicId.isBlank() || pageId.isBlank() || paraTitles.isEmpty()) return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val paraList = paraTitles.map { title ->
                    Para().apply { createdOn = System.currentTimeMillis(); paraTitle = title; this.pageId = pageId }
                }
                val totalPoints = paraList.sumOf { it.storyPoint.toDouble() }.toFloat()
                db.paraDao().insertList(paraList)

                val page = db.pageDao().getByPageIdSync(pageId) ?: return@withContext
                val section = db.sectionDao().getBySectionIdSync(page.sectionId) ?: return@withContext
                val topic = db.sessionTopicDao().getByTopicIdSync(topicId) ?: return@withContext
                val sessions = db.sessionDao().getBySessionId(sessionId)
                if (sessions.isEmpty()) return@withContext
                val session = sessions[0]

                page.pageStoryPoints += totalPoints
                section.totalStoryPoints += totalPoints
                if (topic.firstPageId == page.pageId) topic.firstPageStoryPoints = page.pageStoryPoints
                else topic.secondPageStoryPoints = page.pageStoryPoints
                topic.topicStoryPoints += totalPoints
                session.sessionStoryPoints += totalPoints
                sessionStoryPoints = session.sessionStoryPoints

                db.pageDao().update(page)
                db.sectionDao().update(section)
                db.sessionTopicDao().update(topic)
                db.sessionDao().update(session)
            }
            dismissAddParaDialog()
            loadTopics()
        }
    }

    fun deleteTopic(topicObId: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val topic = db.sessionTopicDao().getByObIdSync(topicObId) ?: return@withContext
                db.pageDao().getByPageIdSync(topic.firstPageId)?.let { page ->
                    db.pageCumulativeProgressDao().getByPageIdSync(page.pageId)?.let { db.pageCumulativeProgressDao().delete(it) }
                    db.paraDao().deleteList(db.paraDao().getByPageIdSync(page.pageId))
                    db.pageDao().delete(page)
                }
                if (!topic.secondPageId.isNullOrEmpty()) {
                    db.pageDao().getByPageIdSync(topic.secondPageId)?.let { page ->
                        db.pageCumulativeProgressDao().getByPageIdSync(page.pageId)?.let { db.pageCumulativeProgressDao().delete(it) }
                        db.paraDao().deleteList(db.paraDao().getByPageIdSync(page.pageId))
                        db.pageDao().delete(page)
                    }
                }
                db.sessionTopicDao().delete(topic)
                db.sectionDao().getBySectionIdSync(topic.sectionId ?: "")?.let { db.sectionDao().deleteList(listOf(it)) }
            }
            loadTopics()
        }
    }

    fun showToast(msg: String) {
        _uiState.update { it.copy(toastMessage = msg) }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}
