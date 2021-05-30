package com.poc.studytracker.sessions.uicontrollers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.poc.corea.constants.PageActionStatus
import com.poc.corea.models.session.Session
import com.poc.corea.models.session.SessionTopic
import com.poc.corea.models.session.SessionTopic_
import com.poc.corea.models.session.Session_
import com.poc.corea.models.subjects.PageCumulativeProgress
import com.poc.corea.models.subjects.PageCumulativeProgress_
import com.poc.studytracker.R
import com.poc.studytracker.common.adapter.BaseExpandableListAdapter
import com.poc.studytracker.common.adapter.OnItemClickListener
import com.poc.studytracker.common.objectbox.ObjectBox
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
                    updateSessionListAdapter = UpdateSessionAdapter(this)
                    binding.updateSessionList.adapter = updateSessionListAdapter
                    loadTopics(sessionId, (session.hasSessionEnded || session.hasSessionExpired))
                })
    }

    private fun loadTopics(sessionId: String?, isUpdateDisabled : Boolean) {
        val disposable =RxQuery.single(ObjectBox.store.boxFor(SessionTopic::class.java).query().equal(SessionTopic_.sessionId, sessionId).build())
                .subscribeOn(Schedulers.io())
                .map {
                    val listItems = ArrayList<BaseExpandableListAdapter.ExpandableListItem>()
                    for(sessionTopic in it) {
                        val topicSectionModel = UpdateTopicSectionModel()
                        topicSectionModel.sectionTitle = sessionTopic.sectionTitle
                        topicSectionModel.setSectionId(sessionTopic.sectionId)
                        listItems.add(topicSectionModel)

                        val firsPageModel = UpdateTopicPageModel()
                        firsPageModel.pageTitle = sessionTopic.firstPageTitle
                        firsPageModel.setPageId(sessionTopic.firstPageId)
                        firsPageModel.setSectionId(sessionTopic.sectionId)
                        firsPageModel.isActionsFreezed = isUpdateDisabled
                        listItems.add(firsPageModel)

                        val secondPageModel = UpdateTopicPageModel()
                        secondPageModel.pageTitle = sessionTopic.secondPageTitle
                        secondPageModel.setPageId(sessionTopic.secondPageId)
                        secondPageModel.setSectionId(sessionTopic.sectionId)
                        secondPageModel.isActionsFreezed = isUpdateDisabled
                        listItems.add(secondPageModel)
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

    override fun onButtonClickOnItem(identifier: Int, pos: Int) {
        val pageId = updateSessionListAdapter.getItem(pos).objectId
        val progress : PageCumulativeProgress = pageProgressMap[pageId]!!
        progress.pageId = pageId
        when(identifier) {
            UpdateSessionAdapter.ACTION_READ -> {
                if(progress.readStatus == PageActionStatus.PENDING) {
                    progress.readStatus = PageActionStatus.COMPLETE
                    progress.readStatusRecordedOn = System.currentTimeMillis()
                }
            }
            UpdateSessionAdapter.ACTION_TAKE_NOTES -> {
                if(progress.notesTakenStatus == PageActionStatus.PENDING) {
                    progress.notesTakenStatus = PageActionStatus.COMPLETE
                    progress.notesStatusRecordedOn = System.currentTimeMillis()
                }
            }
            UpdateSessionAdapter.ACTION_MEMORIZE -> {
                if(progress.memorizedStatus == PageActionStatus.PENDING) {
                    progress.memorizedStatus = PageActionStatus.COMPLETE
                    progress.memorizedStatusRecordedOn = System.currentTimeMillis()
                }
            }
            UpdateSessionAdapter.ACTION_REVIEW -> {
                progress.reviewCount =+ 1
                progress.lastReviewedOn = System.currentTimeMillis()
            }
            UpdateSessionAdapter.ACTION_PRACTICE -> {
                progress.practiceCount += 1
                progress.lastPracticedOn = System.currentTimeMillis()
            }
        }
        pageProgressMap.put(pageId, progress)
        ObjectBox.store.boxFor(PageCumulativeProgress::class.java).put(progress)
        updateSessionListItems()
    }

    private fun loadPageProgress(pageIds : List<String>) {
       val disposable = RxQuery.single(ObjectBox.store.boxFor(PageCumulativeProgress::class.java)
                .query().inValues(PageCumulativeProgress_.pageId, pageIds.toTypedArray()).build())
                .subscribeOn(Schedulers.io())
                .map {
                    val pageProgressMap = LinkedHashMap<String, PageCumulativeProgress>()
                    for(progress in it)
                        pageProgressMap.put(progress.pageId, progress)
                    return@map pageProgressMap
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    pageProgressMap = LinkedHashMap()
                    pageProgressMap.putAll(it)
                    updateSessionListItems()
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