package com.poc.studytracker.sessions.uicontrollers

import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.poc.corea.constants.PageActionStatus
import com.poc.corea.models.session.Session
import com.poc.corea.models.session.SessionTopic
import com.poc.corea.models.session.SessionTopic_
import com.poc.corea.models.session.Session_
import com.poc.corea.models.subjects.*
import com.poc.studytracker.R
import com.poc.studytracker.common.adapter.BaseExpandableListAdapter
import com.poc.studytracker.common.adapter.OnItemClickListener
import com.poc.studytracker.common.objectbox.ObjectBox
import com.poc.studytracker.databinding.FragmentEditSessionBinding
import com.poc.studytracker.sessions.adapters.EditSessionListAdapter
import com.poc.studytracker.sessions.models.TopicPageModel
import com.poc.studytracker.sessions.models.TopicSectionModel
import io.objectbox.rx.RxQuery
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.lang.StringBuilder


class EditSessionFragment : Fragment() , OnItemClickListener{

    lateinit var binding: FragmentEditSessionBinding
    lateinit var editSessionListAdapter : EditSessionListAdapter
    lateinit var session :Session
    private var sessionId : String? = ""

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_edit_session, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionId = arguments?.getString("session_id", "")
        loadSession(sessionId)
        binding.btnCreateSessionTopic.setOnClickListener(View.OnClickListener {
            showDialog()
        })
    }

    private fun loadSession(sessionId: String?) {
        RxQuery.single(ObjectBox.store.boxFor(Session::class.java).query().equal(Session_.sessionId, sessionId).build())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    session = it[0]
                    binding.topicList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                    binding.topicList.adapter = EditSessionListAdapter(this)
                    loadTopics(sessionId)
                })
    }

    private fun loadTopics(sessionId: String?) {
        val disposable = RxQuery.single(ObjectBox.store.boxFor(SessionTopic::class.java).query().equal(SessionTopic_.sessionId, sessionId).build())
                .subscribeOn(Schedulers.io())
                .map {
                    val listItems = ArrayList<BaseExpandableListAdapter.ExpandableListItem>()
                    for(sessionTopic in it) {
                        val topicSectionModel = TopicSectionModel()
                        topicSectionModel.sectionTitle = sessionTopic.sectionTitle
                        topicSectionModel.setSectionId(sessionTopic.sectionId)
                        listItems.add(topicSectionModel)

                        val firsPageModel = TopicPageModel()
                        firsPageModel.pageTitle = sessionTopic.firstPageTitle
                        firsPageModel.setPageId(sessionTopic.firstPageId)
                        firsPageModel.setSectionId(sessionTopic.sectionId)
                        firsPageModel.paraformattedContent = fetchAndFormatParaList(sessionTopic.firstPageId)
                        listItems.add(firsPageModel)

                        val secondPageModel = TopicPageModel()
                        secondPageModel.pageTitle = sessionTopic.secondPageTitle
                        secondPageModel.setPageId(sessionTopic.secondPageId)
                        secondPageModel.setSectionId(sessionTopic.sectionId)
                        secondPageModel.paraformattedContent = fetchAndFormatParaList(sessionTopic.secondPageId)
                        listItems.add(secondPageModel)
                    }
                    return@map listItems
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    editSessionListAdapter = binding.topicList.adapter as EditSessionListAdapter
                    editSessionListAdapter.setmItems(it)
                    binding.topicList.adapter = editSessionListAdapter
                })
    }

    private fun fetchAndFormatParaList(pageId : String): String? {
        val list = ObjectBox.store.boxFor(Para::class.java).query().equal(Para_.pageId, pageId).build().find()
        if(!list.isEmpty()) {
            val sb = StringBuilder()
            sb.append("<ul>\n")
            for(para in list) {
                sb.append("<li>      ${para.paraTitle}</li>\n")
            }
            sb.append("</ul>\n")
            return sb.toString()
        }
        return ""
    }

    fun showDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_create_session_topic)
        val sectionTitle = dialog.findViewById(R.id.topicSectionTitle) as EditText
        val firstPageTitle = dialog.findViewById(R.id.topicFirstPageTitle) as EditText
        val secondPageTitle = dialog.findViewById(R.id.topicSecondPageTitle) as EditText

        val dialogButton: Button = dialog.findViewById(R.id.btnSubmit) as Button
        dialogButton.setOnClickListener(View.OnClickListener {
            createSessionTopic(sectionTitle.editableText.toString(),
            firstPageTitle.editableText.toString(),
            secondPageTitle.editableText.toString())
            dialog.dismiss()
        })
        dialog.show()
    }

    fun showParagraphCreateDialog(pageId : String) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_create_paras)
        val para1 = dialog.findViewById(R.id.para1) as EditText
        val para2 = dialog.findViewById(R.id.para2) as EditText
        val para3 = dialog.findViewById(R.id.para3) as EditText
        val para4 = dialog.findViewById(R.id.para4) as EditText
        val para5 = dialog.findViewById(R.id.para5) as EditText


        val dialogButton: Button = dialog.findViewById(R.id.btnSubmit) as Button
        dialogButton.setOnClickListener(View.OnClickListener {
            val paralist = ArrayList<String>()
            if(!TextUtils.isEmpty(para1.editableText.toString()))
                paralist.add(para1.editableText.toString())
            if(!TextUtils.isEmpty(para2.editableText.toString()))
                paralist.add(para2.editableText.toString())
            if(!TextUtils.isEmpty(para3.editableText.toString()))
                paralist.add(para3.editableText.toString())
            if(!TextUtils.isEmpty(para4.editableText.toString()))
                paralist.add(para4.editableText.toString())
            if(!TextUtils.isEmpty(para5.editableText.toString()))
                paralist.add(para5.editableText.toString())
            if(!paralist.isEmpty())
                createParas(pageId, paralist)
            dialog.dismiss()
        })
        dialog.show()
    }

    private fun createParas(pageId: String, paralist: ArrayList<String>) {
        val paraList = ArrayList<Para>()
        var totalParaStoryPoints = 0.0f
        for(para in paralist) {
            val paragraph = Para()
            paragraph.createdOn = System.currentTimeMillis()
            paragraph.paraTitle = para
            paragraph.pageId = pageId
            paraList.add(paragraph)

            totalParaStoryPoints =+ paragraph.storyPoint
        }
        ObjectBox.store.boxFor(Para::class.java).put(paraList)
        loadTopics(sessionId)
    }


    private fun createSessionTopic(sectiontitle : String, firstPage: String, secondPage: String) {
        val section = Section()
        val page1 = Page()
        val page2 = Page()

        val page1Progress = PageCumulativeProgress()
        val page2Progress = PageCumulativeProgress()

        section.createdOn = System.currentTimeMillis()
        section.noOfPages = 2
        section.sectionTitle = sectiontitle
        section.subjectId = session.subjectId

        page1.createdOn = System.currentTimeMillis()
        page1.pageStoryPoints = 3.0f
        page1.pageTitle = firstPage
        page1.sectionId = section.sectionId

        page1Progress.pageId = page1.pageId
        page1Progress.memorizedStatus = PageActionStatus.PENDING
        page1Progress.readStatus = PageActionStatus.PENDING
        page1Progress.notesTakenStatus = PageActionStatus.PENDING
        page1Progress.reviewCount = 0;
        page1Progress.practiceCount = 0;
        page1.progress.target = page1Progress

        page2.createdOn = System.currentTimeMillis()
        page2.pageStoryPoints = 3.0f
        page2.pageTitle = secondPage
        page2.sectionId = section.sectionId

        page2Progress.pageId = page2.pageId
        page2Progress.memorizedStatus = PageActionStatus.PENDING
        page2Progress.readStatus = PageActionStatus.PENDING
        page2Progress.notesTakenStatus = PageActionStatus.PENDING
        page2Progress.reviewCount = 0;
        page2Progress.practiceCount = 0;
        page2.progress.target = page2Progress

        section.pages.add(page1)
        section.pages.add(page2)

        val sessionTopic = SessionTopic()
        sessionTopic.firstPageId = page1.pageId
        sessionTopic.firstPageStoryPoints = page1.pageStoryPoints
        sessionTopic.firstPageTitle = page1.pageTitle
        sessionTopic.secondPageId = page2.pageId
        sessionTopic.secondPageStoryPoints = page2.pageStoryPoints
        sessionTopic.secondPageTitle = page2.pageTitle
        sessionTopic.sectionId = section.sectionId
        sessionTopic.sectionTitle = section.sectionTitle
        sessionTopic.sessionId = sessionId
        sessionTopic.topicStoryPoints = page1.pageStoryPoints + page2.pageStoryPoints
        sessionTopic.createdOn = System.currentTimeMillis()

        section.totalStoryPoints += page1.pageStoryPoints
        section.totalStoryPoints += page2.pageStoryPoints

        val secId = ObjectBox.store.boxFor(Section::class.java).put(section)
        if(secId > 0)
            ObjectBox.store.boxFor(SessionTopic::class.java).put(sessionTopic)
        loadTopics(sessionId)
    }

    override fun onItemClick(pos: Int) {
        val topicPageModel = editSessionListAdapter.items[pos] as TopicPageModel
        showParagraphCreateDialog(topicPageModel.objectId)
    }

    override fun onButtonClickOnItem(identifier: Int, pos: Int) {
    }

}