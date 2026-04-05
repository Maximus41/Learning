package com.agn.studytracker.sessions.uicontrollers

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.agn.corea.constants.GlobalConstants
import com.agn.corea.constants.PageActionStatus
import com.agn.corea.models.session.Session
import com.agn.corea.models.subjects.PageCumulativeProgress
import com.agn.studytracker.R
import com.agn.studytracker.common.adapter.BaseExpandableListAdapter
import com.agn.studytracker.common.adapter.OnItemClickListener
import com.agn.studytracker.common.adapter.VerticalSpacingItemDecoration
import com.agn.studytracker.common.objectbox.ObjectBox
import com.agn.studytracker.common.uicontrollers.MainActivity
import com.agn.studytracker.databinding.FragmentUpdateSessionBinding
import com.agn.studytracker.sessions.adapters.UpdateSessionAdapter
import com.agn.studytracker.sessions.models.UpdateTopicPageModel
import com.agn.studytracker.sessions.models.UpdateTopicSectionModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers

class UpdateSessionFragment : Fragment(), OnItemClickListener {

    lateinit var binding: FragmentUpdateSessionBinding
    lateinit var updateSessionListAdapter: UpdateSessionAdapter
    lateinit var pageProgressMap: LinkedHashMap<String, PageCumulativeProgress>
    lateinit var session: Session
    private var sessionId: String? = ""
    private var sessionPages = ArrayList<String>()
    private var storyPointsCoveredInThisSession = 0.0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_update_session, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionId = arguments?.getString("session_id", "")
        loadSession(sessionId)
    }

    private fun loadSession(sessionId: String?) {
        ObjectBox.get().sessionDao().getBySessionId(sessionId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(Consumer {
                session = it[0]
                binding.updateSessionList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                binding.updateSessionList.addItemDecoration(VerticalSpacingItemDecoration(activity?.resources?.getDimensionPixelOffset(R.dimen.dp_1)!!))
                updateSessionListAdapter = UpdateSessionAdapter(this)
                binding.updateSessionList.adapter = updateSessionListAdapter
                val myactivity = activity as MainActivity
                myactivity.setTitle("Update ${session.sessionTitle}")
                loadTopics(sessionId, (!session.isSessionActive || session.hasSessionEnded || session.hasSessionExpired))
            })
    }

    private fun loadTopics(sessionId: String?, isUpdateDisabled: Boolean) {
        ObjectBox.get().sessionTopicDao().getBySessionId(sessionId)
            .subscribeOn(Schedulers.io())
            .map { topics ->
                val listItems = ArrayList<BaseExpandableListAdapter.ExpandableListItem>()
                for (sessionTopic in topics) {
                    val topicSectionModel = UpdateTopicSectionModel()
                    topicSectionModel.sectionTitle = sessionTopic.sectionTitle
                    topicSectionModel.setSectionId(sessionTopic.sectionId)
                    listItems.add(topicSectionModel)

                    if (!TextUtils.isEmpty(sessionTopic.firstPageId)) {
                        val firstPageModel = UpdateTopicPageModel()
                        firstPageModel.pageTitle = sessionTopic.firstPageTitle
                        firstPageModel.setPageId(sessionTopic.firstPageId)
                        firstPageModel.setSectionId(sessionTopic.sectionId)
                        firstPageModel.isActionsFreezed = isUpdateDisabled
                        listItems.add(firstPageModel)
                    }

                    if (!TextUtils.isEmpty(sessionTopic.secondPageId)) {
                        val secondPageModel = UpdateTopicPageModel()
                        secondPageModel.pageTitle = sessionTopic.secondPageTitle
                        secondPageModel.setPageId(sessionTopic.secondPageId)
                        secondPageModel.setSectionId(sessionTopic.sectionId)
                        secondPageModel.isActionsFreezed = isUpdateDisabled
                        listItems.add(secondPageModel)
                    }
                    synchronized(sessionPages) {
                        sessionPages.add(sessionTopic.firstPageId)
                        sessionPages.add(sessionTopic.secondPageId)
                    }
                }
                return@map listItems
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(Consumer {
                updateSessionListAdapter.setmItems(it)
                loadPageProgress(sessionPages)
            })
    }

    override fun onItemClick(pos: Int) {}

    override fun onItemLongClickListener(pos: Int, view: View) {}

    override fun onButtonClickOnItem(identifier: Int, pos: Int) {
        val db = ObjectBox.get()
        val pageId = updateSessionListAdapter.getItem(pos).objectId
        val page = db.pageDao().getByPageIdSync(pageId) ?: return
        val pageStoryPoints = page.pageStoryPoints
        val progress: PageCumulativeProgress = pageProgressMap[pageId] ?: return
        progress.pageId = pageId
        if (Math.round((progress.totalStoryPointsCovered / page.pageStoryPoints) * 100) == 100) {
            Toast.makeText(context, "Can't update 100% learnt Topic", Toast.LENGTH_SHORT).show()
            return
        }
        when (identifier) {
            UpdateSessionAdapter.ACTION_READ -> {
                if (progress.readStatus == PageActionStatus.PENDING) {
                    progress.readStatus = PageActionStatus.COMPLETE
                    progress.readStatusRecordedOn = System.currentTimeMillis()
                    progress.totalStoryPointsCovered += pageStoryPoints * 0.1f
                }
            }
            UpdateSessionAdapter.ACTION_TAKE_NOTES -> {
                if (progress.notesTakenStatus == PageActionStatus.PENDING) {
                    progress.notesTakenStatus = PageActionStatus.COMPLETE
                    progress.notesStatusRecordedOn = System.currentTimeMillis()
                    progress.totalStoryPointsCovered += pageStoryPoints * 0.2f
                }
            }
            UpdateSessionAdapter.ACTION_MEMORIZE -> {
                if (progress.memorizedStatus == PageActionStatus.PENDING) {
                    progress.memorizedStatus = PageActionStatus.COMPLETE
                    progress.memorizedStatusRecordedOn = System.currentTimeMillis()
                    progress.totalStoryPointsCovered += pageStoryPoints * 0.15f
                }
            }
            UpdateSessionAdapter.ACTION_REVIEW -> {
                if (progress.reviewCount < GlobalConstants.MINIMUM_REVIEW_COUNT) {
                    progress.reviewCount += 1
                    progress.lastReviewedOn = System.currentTimeMillis()
                    progress.totalStoryPointsCovered += (pageStoryPoints * 0.2f) / GlobalConstants.MINIMUM_REVIEW_COUNT
                } else {
                    Toast.makeText(context, "Exhausted Review Counts", Toast.LENGTH_SHORT).show()
                }
            }
            UpdateSessionAdapter.ACTION_PRACTICE -> {
                if (progress.practiceCount < GlobalConstants.MINIMUM_PRACTICE_COUNT) {
                    progress.practiceCount += 1
                    progress.lastPracticedOn = System.currentTimeMillis()
                    progress.totalStoryPointsCovered += (pageStoryPoints * 0.35f) / GlobalConstants.MINIMUM_PRACTICE_COUNT
                } else {
                    Toast.makeText(context, "Exhausted Practice Counts", Toast.LENGTH_SHORT).show()
                }
            }
        }
        pageProgressMap[pageId] = progress
        db.pageCumulativeProgressDao().update(progress)
        updateSessionListItems()
    }

    private fun loadPageProgress(pageIds: List<String>) {
        ObjectBox.get().pageCumulativeProgressDao().getByPageIds(pageIds)
            .subscribeOn(Schedulers.io())
            .map { progressList ->
                val map = LinkedHashMap<String, PageCumulativeProgress>()
                for (progress in progressList) {
                    map[progress.pageId] = progress
                    storyPointsCoveredInThisSession += progress.totalStoryPointsCovered
                }
                return@map map
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(Consumer {
                pageProgressMap = LinkedHashMap()
                pageProgressMap.putAll(it)
                updateSessionListItems()
                val sessionProgress = Math.round((storyPointsCoveredInThisSession / session.sessionStoryPoints) * 100)
            })
    }

    private fun updateSessionListItems() {
        val items = updateSessionListAdapter.getmItemsCopy() ?: return
        val newListItems = ArrayList<BaseExpandableListAdapter.ExpandableListItem>()
        for (item in items) {
            if (item.type == BaseExpandableListAdapter.CHILD_TYPE) {
                val pageModel = item as UpdateTopicPageModel
                val progress = pageProgressMap[pageModel.objectId] as PageCumulativeProgress
                pageModel.isRead = progress.readStatus == PageActionStatus.COMPLETE
                pageModel.isNotesTaken = progress.notesTakenStatus == PageActionStatus.COMPLETE
                pageModel.isMemorized = progress.memorizedStatus == PageActionStatus.COMPLETE
                pageModel.reviewCount = progress.reviewCount
                pageModel.practiceCount = progress.practiceCount
                newListItems.add(pageModel)
            } else {
                newListItems.add(item)
            }
        }
        updateSessionListAdapter.setmItems(newListItems)
        binding.updateSessionList.adapter = updateSessionListAdapter
    }
}
