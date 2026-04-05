package com.agn.studytracker.sessions.uicontrollers

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UpdateSessionFragment : Fragment(), OnItemClickListener {

    lateinit var binding: FragmentUpdateSessionBinding
    lateinit var updateSessionListAdapter: UpdateSessionAdapter
    lateinit var pageProgressMap: LinkedHashMap<String, PageCumulativeProgress>
    lateinit var session: Session
    private var sessionId: String? = ""
    private val sessionPages = ArrayList<String>()
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
        viewLifecycleOwner.lifecycleScope.launch {
            val sessions = withContext(Dispatchers.IO) { ObjectBox.get().sessionDao().getBySessionId(sessionId) }
            session = sessions[0]
            binding.updateSessionList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            binding.updateSessionList.addItemDecoration(VerticalSpacingItemDecoration(activity?.resources?.getDimensionPixelOffset(R.dimen.dp_1)!!))
            updateSessionListAdapter = UpdateSessionAdapter(this@UpdateSessionFragment)
            binding.updateSessionList.adapter = updateSessionListAdapter
            (activity as MainActivity).setTitle("Update ${session.sessionTitle}")
            loadTopics(sessionId, (!session.isSessionActive || session.hasSessionEnded || session.hasSessionExpired))
        }
    }

    private fun loadTopics(sessionId: String?, isUpdateDisabled: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch {
            val listItems = withContext(Dispatchers.IO) {
                val topics = ObjectBox.get().sessionTopicDao().getBySessionId(sessionId)
                val items = ArrayList<BaseExpandableListAdapter.ExpandableListItem>()
                for (topic in topics) {
                    items.add(UpdateTopicSectionModel().apply {
                        sectionTitle = topic.sectionTitle
                        setSectionId(topic.sectionId)
                    })
                    if (!TextUtils.isEmpty(topic.firstPageId)) {
                        items.add(UpdateTopicPageModel().apply {
                            pageTitle = topic.firstPageTitle; setPageId(topic.firstPageId)
                            setSectionId(topic.sectionId); isActionsFreezed = isUpdateDisabled
                        })
                        synchronized(sessionPages) { sessionPages.add(topic.firstPageId) }
                    }
                    if (!TextUtils.isEmpty(topic.secondPageId)) {
                        items.add(UpdateTopicPageModel().apply {
                            pageTitle = topic.secondPageTitle; setPageId(topic.secondPageId)
                            setSectionId(topic.sectionId); isActionsFreezed = isUpdateDisabled
                        })
                        synchronized(sessionPages) { sessionPages.add(topic.secondPageId) }
                    }
                }
                items
            }
            updateSessionListAdapter.setmItems(listItems)
            loadPageProgress(sessionPages)
        }
    }

    override fun onItemClick(pos: Int) {}
    override fun onItemLongClickListener(pos: Int, view: View) {}

    override fun onButtonClickOnItem(identifier: Int, pos: Int) {
        val db = ObjectBox.get()
        val pageId = updateSessionListAdapter.getItem(pos).objectId
        val page = db.pageDao().getByPageIdSync(pageId) ?: return
        val progress: PageCumulativeProgress = pageProgressMap[pageId] ?: return
        if (Math.round((progress.totalStoryPointsCovered / page.pageStoryPoints) * 100) == 100) {
            Toast.makeText(context, "Can't update 100% learnt Topic", Toast.LENGTH_SHORT).show()
            return
        }
        when (identifier) {
            UpdateSessionAdapter.ACTION_READ -> if (progress.readStatus == PageActionStatus.PENDING) {
                progress.readStatus = PageActionStatus.COMPLETE
                progress.readStatusRecordedOn = System.currentTimeMillis()
                progress.totalStoryPointsCovered += page.pageStoryPoints * 0.1f
            }
            UpdateSessionAdapter.ACTION_TAKE_NOTES -> if (progress.notesTakenStatus == PageActionStatus.PENDING) {
                progress.notesTakenStatus = PageActionStatus.COMPLETE
                progress.notesStatusRecordedOn = System.currentTimeMillis()
                progress.totalStoryPointsCovered += page.pageStoryPoints * 0.2f
            }
            UpdateSessionAdapter.ACTION_MEMORIZE -> if (progress.memorizedStatus == PageActionStatus.PENDING) {
                progress.memorizedStatus = PageActionStatus.COMPLETE
                progress.memorizedStatusRecordedOn = System.currentTimeMillis()
                progress.totalStoryPointsCovered += page.pageStoryPoints * 0.15f
            }
            UpdateSessionAdapter.ACTION_REVIEW -> if (progress.reviewCount < GlobalConstants.MINIMUM_REVIEW_COUNT) {
                progress.reviewCount += 1
                progress.lastReviewedOn = System.currentTimeMillis()
                progress.totalStoryPointsCovered += (page.pageStoryPoints * 0.2f) / GlobalConstants.MINIMUM_REVIEW_COUNT
            } else Toast.makeText(context, "Exhausted Review Counts", Toast.LENGTH_SHORT).show()
            UpdateSessionAdapter.ACTION_PRACTICE -> if (progress.practiceCount < GlobalConstants.MINIMUM_PRACTICE_COUNT) {
                progress.practiceCount += 1
                progress.lastPracticedOn = System.currentTimeMillis()
                progress.totalStoryPointsCovered += (page.pageStoryPoints * 0.35f) / GlobalConstants.MINIMUM_PRACTICE_COUNT
            } else Toast.makeText(context, "Exhausted Practice Counts", Toast.LENGTH_SHORT).show()
        }
        pageProgressMap[pageId] = progress
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            db.pageCumulativeProgressDao().update(progress)
        }
        updateSessionListItems()
    }

    private fun loadPageProgress(pageIds: List<String>) {
        viewLifecycleOwner.lifecycleScope.launch {
            val map = withContext(Dispatchers.IO) {
                val progressList = ObjectBox.get().pageCumulativeProgressDao().getByPageIds(pageIds)
                val result = LinkedHashMap<String, PageCumulativeProgress>()
                for (p in progressList) {
                    result[p.pageId] = p
                    storyPointsCoveredInThisSession += p.totalStoryPointsCovered
                }
                result
            }
            pageProgressMap = LinkedHashMap<String, PageCumulativeProgress>().also { it.putAll(map) }
            updateSessionListItems()
        }
    }

    private fun updateSessionListItems() {
        val items = updateSessionListAdapter.getmItemsCopy() ?: return
        val newItems = items.map { item ->
            if (item.type == BaseExpandableListAdapter.CHILD_TYPE) {
                val pageModel = item as UpdateTopicPageModel
                val progress = pageProgressMap[pageModel.objectId] ?: return@map item
                pageModel.apply {
                    isRead = progress.readStatus == PageActionStatus.COMPLETE
                    isNotesTaken = progress.notesTakenStatus == PageActionStatus.COMPLETE
                    isMemorized = progress.memorizedStatus == PageActionStatus.COMPLETE
                    reviewCount = progress.reviewCount
                    practiceCount = progress.practiceCount
                }
            } else item
        }
        updateSessionListAdapter.setmItems(ArrayList(newItems))
        binding.updateSessionList.adapter = updateSessionListAdapter
    }
}
