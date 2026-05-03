package com.agn.studytracker.subjects.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agn.corea.constants.PageActionStatus
import com.agn.corea.models.subjects.Para
import com.agn.studytracker.common.objectbox.ObjectBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SubjectDetailsUiState(
    val subjectName: String = "",
    val sections: List<SectionUiItem> = emptyList(),
    val isLoading: Boolean = false,
    val paragraphsDialogPage: PageUiItem? = null,
    val paragraphsDialogParas: List<Para> = emptyList()
)

data class SectionUiItem(
    val sectionId: String,
    val title: String,
    val progressPercent: Int,
    val isExpanded: Boolean = false,
    val pages: List<PageUiItem> = emptyList()
)

data class PageUiItem(
    val pageId: String,
    val title: String,
    val isRead: Boolean,
    val isNotesTaken: Boolean,
    val isMemorized: Boolean,
    val reviewCount: Int,
    val practiceCount: Int
)

class SubjectDetailsViewModel : ViewModel() {

    private val db get() = ObjectBox.get()

    private val _uiState = MutableStateFlow(SubjectDetailsUiState())
    val uiState: StateFlow<SubjectDetailsUiState> = _uiState.asStateFlow()

    private var loadedSubjectId: String = ""

    fun init(subjectId: String, subjectName: String) {
        if (loadedSubjectId == subjectId) return
        loadedSubjectId = subjectId
        _uiState.update { it.copy(subjectName = subjectName) }
        observeSections(subjectId)
    }

    private fun observeSections(subjectId: String) {
        viewModelScope.launch {
            db.sectionDao().observeBySubjectId(subjectId)
                .map { sections ->
                    sections.map { section ->
                        var totalCovered = 0.0f
                        val pages = db.pageDao().getBySectionIdSync(section.sectionId)
                        val pageItems = pages.map { page ->
                            val progress = db.pageCumulativeProgressDao().getByPageIdSync(page.pageId)
                            if (progress != null) totalCovered += progress.totalStoryPointsCovered
                            PageUiItem(
                                pageId = page.pageId,
                                title = page.pageTitle,
                                isRead = progress?.readStatus == PageActionStatus.COMPLETE,
                                isNotesTaken = progress?.notesTakenStatus == PageActionStatus.COMPLETE,
                                isMemorized = progress?.memorizedStatus == PageActionStatus.COMPLETE,
                                reviewCount = progress?.reviewCount ?: 0,
                                practiceCount = progress?.practiceCount ?: 0
                            )
                        }
                        val progressPercent = if (section.totalStoryPoints > 0)
                            Math.round((totalCovered / section.totalStoryPoints) * 100) else 0
                        val existing = _uiState.value.sections.find { it.sectionId == section.sectionId }
                        SectionUiItem(
                            sectionId = section.sectionId,
                            title = section.sectionTitle,
                            progressPercent = progressPercent,
                            isExpanded = existing?.isExpanded ?: false,
                            pages = pageItems
                        )
                    }
                }
                .flowOn(Dispatchers.IO)
                .collect { sections ->
                    _uiState.update { it.copy(sections = sections) }
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

    fun showParagraphsDialog(pageItem: PageUiItem) {
        viewModelScope.launch {
            val paras = withContext(Dispatchers.IO) { db.paraDao().getByPageId(pageItem.pageId) }
            _uiState.update { it.copy(paragraphsDialogPage = pageItem, paragraphsDialogParas = paras) }
        }
    }

    fun dismissParagraphsDialog() {
        _uiState.update { it.copy(paragraphsDialogPage = null, paragraphsDialogParas = emptyList()) }
    }
}
