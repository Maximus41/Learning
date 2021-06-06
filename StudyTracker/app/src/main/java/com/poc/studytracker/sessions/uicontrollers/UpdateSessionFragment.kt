package com.poc.studytracker.sessions.uicontrollers

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.poc.corea.constants.GlobalConstants
import com.poc.corea.constants.PageActionStatus
import com.poc.corea.models.session.Session
import com.poc.corea.models.session.SessionTopic
import com.poc.corea.models.session.SessionTopic_
import com.poc.corea.models.session.Session_
import com.poc.corea.models.subjects.Page
import com.poc.corea.models.subjects.PageCumulativeProgress
import com.poc.corea.models.subjects.PageCumulativeProgress_
import com.poc.corea.models.subjects.Page_
import com.poc.studytracker.R
import com.poc.studytracker.common.adapter.BaseExpandableListAdapter
import com.poc.studytracker.common.adapter.OnItemClickListener
import com.poc.studytracker.common.adapter.VerticalSpacingItemDecoration
import com.poc.studytracker.common.objectbox.ObjectBox
import com.poc.studytracker.common.uicontrollers.MainActivity
import com.poc.studytracker.databinding.FragmentUpdateSessionBinding
import com.poc.studytracker.sessions.adapters.UpdateSessionAdapter
import com.poc.studytracker.sessions.models.UpdateTopicPageModel
import com.poc.studytracker.sessions.models.UpdateTopicSectionModel
import io.objectbox.kotlin.inValues
import io.objectbox.rx.RxQuery
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers

class UpdateSessionFragment : Fragment(), OnItemClickListener {

    lateinit var binding: FragmentUpdateSessionBinding
    lateinit var updateSessionListAdapter : UpdateSessionAdapter
    lateinit var pageProgressMap : LinkedHashMap<String, PageCumulativeProgress>
    lateinit var session : Session
    private var sessionId : String? = ""
    private var sessionPages = ArrayList<String>()
    private var storyPointsCoveredInThisSession = 0.0f

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_update_session, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionId = arguments?.getString("session_id", "")
        loadSession(sessionId)
    }

    private fun loadSession(sessionId: String?) {
        val disposable = RxQuery.single(ObjectBox.store.boxFor(Session::class.java).query().equal(Session_.sessionId, sessionId).build())
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

    private fun loadTopics(sessionId: String?, isUpdateDisabled : Boolean) {
        val disposable = RxQuery.single(ObjectBox.store.boxFor(SessionTopic::class.java).query().equal(SessionTopic_.sessionId, sessionId).build())
                .subscribeOn(Schedulers.io())
                .map {
                    val listItems = ArrayList<BaseExpandableListAdapter.ExpandableListItem>()
                    for(sessionTopic in it) {
                        val topicSectionModel = UpdateTopicSectionModel()
                        topicSectionModel.sectionTitle = sessionTopic.sectionTitle
                        topicSectionModel.setSectionId(sessionTopic.sectionId)
                        listItems.add(topicSectionModel)

                        if(!TextUtils.isEmpty(sessionTopic.firstPageId)) {
                            val firsPageModel = UpdateTopicPageModel()
                            firsPageModel.pageTitle = sessionTopic.firstPageTitle
                            firsPageModel.setPageId(sessionTopic.firstPageId)
                            firsPageModel.setSectionId(sessionTopic.sectionId)
                            firsPageModel.isActionsFreezed = isUpdateDisabled
                            listItems.add(firsPageModel)
                        }

                        if(!TextUtils.isEmpty(sessionTopic.secondPageId)) {
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

    override fun onItemClick(pos: Int) {
    }

    override fun onItemLongClickListener(pos: Int, view: View) {

    }

    override fun onButtonClickOnItem(identifier: Int, pos: Int) {
        val pageId = updateSessionListAdapter.getItem(pos).objectId
        val page = ObjectBox.store.boxFor(Page::class.java).query().equal(Page_.pageId, pageId).build().findFirst()
        val pageStoryPoints = page!!.pageStoryPoints
        val progress : PageCumulativeProgress = pageProgressMap[pageId]!!
        progress.pageId = pageId
        if(Math.round((progress.totalStoryPointsCovered / page.pageStoryPoints) * 100) == 100) {
            Toast.makeText(context, "Can't update 100% learnt Topic", Toast.LENGTH_SHORT).show()
            return
        }
        when(identifier) {
            UpdateSessionAdapter.ACTION_READ -> {
                if(progress.readStatus == PageActionStatus.PENDING) {
                    progress.readStatus = PageActionStatus.COMPLETE
                    progress.readStatusRecordedOn = System.currentTimeMillis()
                    progress.totalStoryPointsCovered += pageStoryPoints * 0.1f //10% effort
                }
            }
            UpdateSessionAdapter.ACTION_TAKE_NOTES -> {
                if(progress.notesTakenStatus == PageActionStatus.PENDING) {
                    progress.notesTakenStatus = PageActionStatus.COMPLETE
                    progress.notesStatusRecordedOn = System.currentTimeMillis()
                    progress.totalStoryPointsCovered += pageStoryPoints * 0.2f //20% effort
                }
            }
            UpdateSessionAdapter.ACTION_MEMORIZE -> {
                if(progress.memorizedStatus == PageActionStatus.PENDING) {
                    progress.memorizedStatus = PageActionStatus.COMPLETE
                    progress.memorizedStatusRecordedOn = System.currentTimeMillis()
                    progress.totalStoryPointsCovered += pageStoryPoints * 0.15f //15% effort
                }
            }
            UpdateSessionAdapter.ACTION_REVIEW -> {
                if(progress.reviewCount < GlobalConstants.MINIMUM_REVIEW_COUNT) {
                    progress.reviewCount += 1
                    progress.lastReviewedOn = System.currentTimeMillis()
                    progress.totalStoryPointsCovered += (pageStoryPoints * 0.2f) / GlobalConstants.MINIMUM_REVIEW_COUNT  //20% effort
                } else {
                    Toast.makeText(context, "Exhausted Review Counts", Toast.LENGTH_SHORT).show()
                }
            }
            UpdateSessionAdapter.ACTION_PRACTICE -> {
                if(progress.practiceCount < GlobalConstants.MINIMUM_PRACTICE_COUNT) {
                    progress.practiceCount += 1
                    progress.lastPracticedOn = System.currentTimeMillis()
                    progress.totalStoryPointsCovered += (pageStoryPoints * 0.35f) / GlobalConstants.MINIMUM_PRACTICE_COUNT //35% effort
                } else {
                    Toast.makeText(context, "Exhausted Practice Counts", Toast.LENGTH_SHORT).show()
                }
            }
        }
        pageProgressMap[pageId] = progress
        ObjectBox.store.boxFor(PageCumulativeProgress::class.java).put(progress)
        updateSessionListItems()
    }

    private fun loadPageProgress(pageIds : List<String>) {
        val disposable = RxQuery.single(ObjectBox.store.boxFor(PageCumulativeProgress::class.java)
                .query().inValues(PageCumulativeProgress_.pageId, pageIds.toTypedArray()).build())
                .subscribeOn(Schedulers.io())
                .map {
                    val pageProgressMap = LinkedHashMap<String, PageCumulativeProgress>()
                    for(progress in it) {
                        pageProgressMap[progress.pageId] = progress
                        storyPointsCoveredInThisSession += progress.totalStoryPointsCovered
                    }
                    return@map pageProgressMap
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
        val items = updateSessionListAdapter.getmItemsCopy()
        val newListItems = ArrayList<BaseExpandableListAdapter.ExpandableListItem>()
        if(items == null)
            return
        for(item in items) {
            if(item.type == BaseExpandableListAdapter.CHILD_TYPE) {
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