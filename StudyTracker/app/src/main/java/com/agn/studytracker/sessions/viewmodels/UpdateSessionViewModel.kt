package com.agn.studytracker.sessions.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agn.corea.constants.GlobalConstants
import com.agn.corea.constants.PageActionStatus
import com.agn.corea.models.subjects.PageCumulativeProgress
import com.agn.studytracker.common.objectbox.ObjectBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class UpdateSessionUiState(
    val sessionTitle: String = "",
    val isFrozen: Boolean = false,
    val sections: List<UpdateSectionItem> = emptyList(),
    val pageProgress: Map<String, PageCumulativeProgress> = emptyMap(),
    val toastMessage: String? = null,
    val isLoading: Boolean = false
)

data class UpdateSectionItem(
    val sectionId: String,
    val title: String,
    val isExpanded: Boolean = true,
    val pages: List<UpdatePageItem> = emptyList()
)

data class UpdatePageItem(
    val pageId: String,
    val title: String,
    val isRead: Boolean = false,
    val isNotesTaken: Boolean = false,
    val isMemorized: Boolean = false,
    val reviewCount: Int = 0,
    val practiceCount: Int = 0,
    val pageStoryPoints: Float = 0f,
    val totalCovered: Float = 0f
)

class UpdateSessionViewModel : ViewModel() {

    private val db get() = ObjectBox.get()

    private val _uiState = MutableStateFlow(UpdateSessionUiState())
    val uiState: StateFlow<UpdateSessionUiState> = _uiState.asStateFlow()

    private var sessionId: String = ""

    companion object {
        const val ACTION_READ = 0
        const val ACTION_TAKE_NOTES = 1
        const val ACTION_MEMORIZE = 2
        const val ACTION_REVIEW = 3
        const val ACTION_PRACTICE = 4
    }

    fun loadSession(sessionId: String) {
        if (this.sessionId == sessionId) return
        this.sessionId = sessionId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val sessions = withContext(Dispatchers.IO) { db.sessionDao().getBySessionId(sessionId) }
            if (sessions.isEmpty()) return@launch
            val session = sessions[0]
            val isFrozen = !session.isSessionActive || session.hasSessionEnded || session.hasSessionExpired
            _uiState.update { it.copy(sessionTitle = session.sessionTitle ?: "", isFrozen = isFrozen) }
            loadTopics(isFrozen)
        }
    }

    private suspend fun loadTopics(isFrozen: Boolean) {
        val (sections, allPageIds) = withContext(Dispatchers.IO) {
            val topics = db.sessionTopicDao().getBySessionId(sessionId)
            val sectionMap = LinkedHashMap<String, Triple<String, ArrayList<String>, ArrayList<String>>>()
            val allPageIds = ArrayList<String>()

            for (topic in topics) {
                val entry = sectionMap.getOrPut(topic.sectionId ?: "") {
                    Triple(topic.sectionTitle ?: "", ArrayList(), ArrayList())
                }
                if (!topic.firstPageId.isNullOrEmpty()) {
                    entry.second.add(topic.firstPageId)
                    entry.third.add(topic.firstPageTitle ?: "")
                    allPageIds.add(topic.firstPageId)
                }
                if (!topic.secondPageId.isNullOrEmpty()) {
                    entry.second.add(topic.secondPageId)
                    entry.third.add(topic.secondPageTitle ?: "")
                    allPageIds.add(topic.secondPageId)
                }
            }

            val sections = sectionMap.map { (sectionId, triple) ->
                val pageItems = triple.second.mapIndexed { i, pageId ->
                    val page = db.pageDao().getByPageIdSync(pageId)
                    UpdatePageItem(
                        pageId = pageId,
                        title = triple.third.getOrElse(i) { "" },
                        pageStoryPoints = page?.pageStoryPoints ?: 0f
                    )
                }
                UpdateSectionItem(sectionId = sectionId, title = triple.first, pages = pageItems)
            }
            Pair(sections, allPageIds)
        }
        _uiState.update { it.copy(sections = sections, isLoading = false) }
        loadPageProgress(allPageIds)
    }

    private fun loadPageProgress(pageIds: List<String>) {
        viewModelScope.launch {
            val progressMap = withContext(Dispatchers.IO) {
                db.pageCumulativeProgressDao().getByPageIds(pageIds).associateBy { it.pageId }
            }
            _uiState.update { state ->
                state.copy(
                    pageProgress = progressMap,
                    sections = state.sections.map { section ->
                        section.copy(pages = section.pages.map { page ->
                            val p = progressMap[page.pageId]
                            page.copy(
                                isRead = p?.readStatus == PageActionStatus.COMPLETE,
                                isNotesTaken = p?.notesTakenStatus == PageActionStatus.COMPLETE,
                                isMemorized = p?.memorizedStatus == PageActionStatus.COMPLETE,
                                reviewCount = p?.reviewCount ?: 0,
                                practiceCount = p?.practiceCount ?: 0,
                                totalCovered = p?.totalStoryPointsCovered ?: 0f
                            )
                        })
                    }
                )
            }
        }
    }

    fun toggleSection(sectionId: String) {
        _uiState.update { state ->
            state.copy(sections = state.sections.map { section ->
                if (section.sectionId == sectionId) section.copy(isExpanded = !section.isExpanded)
                else section
            })
        }
    }

    fun onAction(pageId: String, action: Int) {
        if (_uiState.value.isFrozen) return
        val page = db.pageDao().getByPageIdSync(pageId) ?: return
        val progress = _uiState.value.pageProgress[pageId]?.let {
            PageCumulativeProgress().apply {
                obId = it.obId; this.pageId = it.pageId; progressId = it.progressId
                readStatus = it.readStatus; readStatusRecordedOn = it.readStatusRecordedOn
                notesTakenStatus = it.notesTakenStatus; notesStatusRecordedOn = it.notesStatusRecordedOn
                memorizedStatus = it.memorizedStatus; memorizedStatusRecordedOn = it.memorizedStatusRecordedOn
                reviewCount = it.reviewCount; lastReviewedOn = it.lastReviewedOn
                practiceCount = it.practiceCount; lastPracticedOn = it.lastPracticedOn
                totalStoryPointsCovered = it.totalStoryPointsCovered
            }
        } ?: return

        if (page.pageStoryPoints > 0 && Math.round((progress.totalStoryPointsCovered / page.pageStoryPoints) * 100) == 100) {
            _uiState.update { it.copy(toastMessage = "Can't update 100% learnt Topic") }
            return
        }

        val updated: PageCumulativeProgress? = when (action) {
            ACTION_READ -> if (progress.readStatus == PageActionStatus.PENDING) progress.apply {
                readStatus = PageActionStatus.COMPLETE
                readStatusRecordedOn = System.currentTimeMillis()
                totalStoryPointsCovered += page.pageStoryPoints * 0.1f
            } else null

            ACTION_TAKE_NOTES -> if (progress.notesTakenStatus == PageActionStatus.PENDING) progress.apply {
                notesTakenStatus = PageActionStatus.COMPLETE
                notesStatusRecordedOn = System.currentTimeMillis()
                totalStoryPointsCovered += page.pageStoryPoints * 0.2f
            } else null

            ACTION_MEMORIZE -> if (progress.memorizedStatus == PageActionStatus.PENDING) progress.apply {
                memorizedStatus = PageActionStatus.COMPLETE
                memorizedStatusRecordedOn = System.currentTimeMillis()
                totalStoryPointsCovered += page.pageStoryPoints * 0.15f
            } else null

            ACTION_REVIEW -> if (progress.reviewCount < GlobalConstants.MINIMUM_REVIEW_COUNT) progress.apply {
                reviewCount += 1
                lastReviewedOn = System.currentTimeMillis()
                totalStoryPointsCovered += (page.pageStoryPoints * 0.2f) / GlobalConstants.MINIMUM_REVIEW_COUNT
            } else { _uiState.update { it.copy(toastMessage = "Exhausted Review Counts") }; null }

            ACTION_PRACTICE -> if (progress.practiceCount < GlobalConstants.MINIMUM_PRACTICE_COUNT) progress.apply {
                practiceCount += 1
                lastPracticedOn = System.currentTimeMillis()
                totalStoryPointsCovered += (page.pageStoryPoints * 0.35f) / GlobalConstants.MINIMUM_PRACTICE_COUNT
            } else { _uiState.update { it.copy(toastMessage = "Exhausted Practice Counts") }; null }

            else -> null
        }

        updated ?: return

        viewModelScope.launch(Dispatchers.IO) { db.pageCumulativeProgressDao().update(updated) }

        _uiState.update { state ->
            val newMap = state.pageProgress.toMutableMap().also { it[pageId] = updated }
            state.copy(
                pageProgress = newMap,
                sections = state.sections.map { section ->
                    section.copy(pages = section.pages.map { p ->
                        if (p.pageId == pageId) p.copy(
                            isRead = updated.readStatus == PageActionStatus.COMPLETE,
                            isNotesTaken = updated.notesTakenStatus == PageActionStatus.COMPLETE,
                            isMemorized = updated.memorizedStatus == PageActionStatus.COMPLETE,
                            reviewCount = updated.reviewCount,
                            practiceCount = updated.practiceCount,
                            totalCovered = updated.totalStoryPointsCovered
                        ) else p
                    })
                }
            )
        }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}
