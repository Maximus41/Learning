package com.poc.studytracker.sessions.uicontrollers

import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.poc.corea.constants.GlobalConstants
import com.poc.corea.constants.PageActionStatus
import com.poc.corea.models.session.Session
import com.poc.corea.models.session.SessionTopic
import com.poc.corea.models.session.SessionTopic_
import com.poc.corea.models.session.Session_
import com.poc.corea.models.subjects.*
import com.poc.studytracker.R
import com.poc.studytracker.common.adapter.BaseExpandableListAdapter
import com.poc.studytracker.common.adapter.OnItemClickListener
import com.poc.studytracker.common.adapter.VerticalSpacingItemDecoration
import com.poc.studytracker.common.objectbox.ObjectBox
import com.poc.studytracker.common.uicontrollers.MainActivity
import com.poc.studytracker.databinding.FragmentEditSessionBinding
import com.poc.studytracker.sessions.adapters.EditSessionListAdapter
import com.poc.studytracker.sessions.models.TopicPageModel
import com.poc.studytracker.sessions.models.TopicSectionModel
import io.objectbox.rx.RxQuery
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.lang.StringBuilder
import kotlin.math.roundToInt


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
                binding.topicList.addItemDecoration(VerticalSpacingItemDecoration(activity?.resources?.getDimensionPixelOffset(R.dimen.dp_1)!!))
                binding.topicList.adapter = EditSessionListAdapter(this)
                loadTopics(sessionId)
                val myactivity = activity as MainActivity
                myactivity.setTitle("Edit ${session.sessionTitle}")
            })
    }

    private fun loadExistingSections(autoCompleteTextView: AutoCompleteTextView, page1AutoCompleteTextView: AutoCompleteTextView, page2AutoCompleteTextView: AutoCompleteTextView ,dialog: Dialog) {
        val disposable = RxQuery.single(ObjectBox.store.boxFor(Section::class.java).query().equal(Section_.subjectId, session.subjectId).build())
            .subscribeOn(Schedulers.io())
            .map {
                val sectionList = ArrayList<String>()
                val pageList = ArrayList<String>()
                val map = HashMap<String, List<String>>()
                for(section in  it) {
                    sectionList.add(section.sectionTitle)
                    val pages = section.pages
                    for(page in pages)
                        pageList.add(page.pageTitle)
                }
                map["section"] = sectionList
                map["page"] = pageList
                return@map map
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(Consumer {
                val arrayAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, it["section"] as List<String>)
                val pageArrayAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, it["page"] as List<String>)
                autoCompleteTextView.threshold = 1
                page1AutoCompleteTextView.threshold = 1
                page2AutoCompleteTextView.threshold = 1
                page1AutoCompleteTextView.setAdapter(pageArrayAdapter)
                page2AutoCompleteTextView.setAdapter(pageArrayAdapter)
                autoCompleteTextView.setAdapter(arrayAdapter)
                dialog.show()
            })
    }

    private fun loadTopics(sessionId: String?) {
        val disposable = RxQuery.single(ObjectBox.store.boxFor(SessionTopic::class.java).query().equal(SessionTopic_.sessionId, sessionId).build())
            .subscribeOn(Schedulers.io())
            .map {
                val listItems = ArrayList<BaseExpandableListAdapter.ExpandableListItem>()
                val updatedSessionTopicList = ArrayList<SessionTopic>()
                for(sessionTopic in it) {

                    val topicSectionModel = TopicSectionModel()
                    topicSectionModel.sectionTitle = sessionTopic.sectionTitle
                    topicSectionModel.setSectionId(sessionTopic.sectionId)
                    topicSectionModel.topicId = sessionTopic.topicId
                    topicSectionModel.topicObId = sessionTopic.obId
                    listItems.add(topicSectionModel)

                    if(!TextUtils.isEmpty(sessionTopic.firstPageId)) {
                        val firsPageModel = TopicPageModel()
                        firsPageModel.pageTitle = sessionTopic.firstPageTitle
                        firsPageModel.setPageId(sessionTopic.firstPageId)
                        firsPageModel.setSectionId(sessionTopic.sectionId)
                        firsPageModel.sessionTopicId = sessionTopic.topicId
                        firsPageModel.paraformattedContent = fetchAndFormatParaList(sessionTopic.firstPageId)
                        listItems.add(firsPageModel)
                    }

                    if(!TextUtils.isEmpty(sessionTopic.secondPageId)) {
                        val secondPageModel = TopicPageModel()
                        secondPageModel.pageTitle = sessionTopic.secondPageTitle
                        secondPageModel.setPageId(sessionTopic.secondPageId)
                        secondPageModel.setSectionId(sessionTopic.sectionId)
                        secondPageModel.sessionTopicId = sessionTopic.topicId
                        secondPageModel.paraformattedContent = fetchAndFormatParaList(sessionTopic.secondPageId)
                        listItems.add(secondPageModel)
                    }
                }
                return@map listItems
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(Consumer {
                val sessionAproxTime = (session.sessionStoryPoints * GlobalConstants.ONE_STORY_POINT_IN_HOURS).roundToInt()
                if(!it.isEmpty()) {
                    binding.approxTime.visibility = View.VISIBLE
                    binding.approxTime.text = "Approx Effort Needed : $sessionAproxTime hrs"
                }
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
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_create_session_topic)
        val sectionTitle = dialog.findViewById(R.id.topicSectionTitle) as AutoCompleteTextView
        val firstPageTitle = dialog.findViewById(R.id.topicFirstPageTitle) as AutoCompleteTextView
        val secondPageTitle = dialog.findViewById(R.id.topicSecondPageTitle) as AutoCompleteTextView
        val dialogButton: Button = dialog.findViewById(R.id.btnSubmit) as Button
        dialogButton.setOnClickListener(View.OnClickListener {
            if(TextUtils.isEmpty(sectionTitle.editableText.toString()) || TextUtils.isEmpty(firstPageTitle.editableText.toString())) {
                Toast.makeText(context, "Section/1st topic mandatory", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            createSessionTopic(sectionTitle.editableText.toString(),
                firstPageTitle.editableText.toString(),
                secondPageTitle.editableText.toString())
            dialog.dismiss()
        })
        loadExistingSections(sectionTitle, firstPageTitle, secondPageTitle, dialog)
    }

    fun showParagraphCreateDialog(topicId : String, pageId : String) {
        val paraCount =  ObjectBox.store.boxFor(Para::class.java).query().equal(Para_.pageId, pageId).build().count()
        if(paraCount > 0)
            return
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
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
                createParas(topicId, pageId, paralist)
            dialog.dismiss()
        })
        dialog.show()
    }

    private fun createParas(topicId: String, pageId: String, paralist: ArrayList<String>) {
        val paraList = ArrayList<Para>()
        var totalParaStoryPoints = 0.0f
        for(para in paralist) {
            val paragraph = Para()
            paragraph.createdOn = System.currentTimeMillis()
            paragraph.paraTitle = para
            paragraph.pageId = pageId
            paraList.add(paragraph)

            totalParaStoryPoints += paragraph.storyPoint
        }
        ObjectBox.store.boxFor(Para::class.java).put(paraList)

        //Code to update story points in Section,Page, Session and SessionTopic tables after paragraph has been added
        val page = ObjectBox.store.boxFor(Page::class.java).query().equal(Page_.pageId, pageId).build().findFirst()
        val section = ObjectBox.store.boxFor(Section::class.java).query().equal(Section_.sectionId, page!!.sectionId).build().findFirst()
        val sessionTopic = ObjectBox.store.boxFor(SessionTopic::class.java).query().equal(SessionTopic_.topicId, topicId).build().findFirst()
        page.pageStoryPoints += totalParaStoryPoints
        section!!.totalStoryPoints += totalParaStoryPoints
        if(sessionTopic!!.firstPageId == page.pageId)
            sessionTopic.firstPageStoryPoints = page.pageStoryPoints
        else
            sessionTopic.secondPageStoryPoints = page.pageStoryPoints
        sessionTopic!!.topicStoryPoints += totalParaStoryPoints
        session.sessionStoryPoints += totalParaStoryPoints
        ObjectBox.store.boxFor(Page::class.java).put(page)
        ObjectBox.store.boxFor(Section::class.java).put(section)
        ObjectBox.store.boxFor(SessionTopic::class.java).put(sessionTopic)
        ObjectBox.store.boxFor(Session::class.java).put(session)

        //Load Topics
        loadTopics(sessionId)
    }


    private fun createSessionTopic(sectiontitle : String, firstPage: String, secondPage: String) {
        if(TextUtils.isEmpty(sectiontitle) || firstPage == secondPage)
            return
        var sessionTopic = ObjectBox.store.boxFor(SessionTopic::class.java).query().equal(SessionTopic_.sessionId, sessionId)
            .and().equal(SessionTopic_.sectionTitle, sectiontitle).build().findFirst()
        if(sessionTopic != null) {
            Toast.makeText(context, "You have already added $sectiontitle to this session", Toast.LENGTH_SHORT).show()
            return
        }
        var section = ObjectBox.store.boxFor(Section::class.java).query().equal(Section_.sectionTitle, sectiontitle).
            and().equal(Section_.subjectId, session.subjectId).build().findFirst()
        if(section == null) {
            section = Section()
            section.createdOn = System.currentTimeMillis()
            section.sectionTitle = sectiontitle
            section.subjectId = session.subjectId
        }

        sessionTopic = SessionTopic()

        if(!TextUtils.isEmpty(firstPage)) {
            var page1 = ObjectBox.store.boxFor(Page::class.java).query().equal(Page_.pageTitle, firstPage)
                .and().equal(Page_.sectionId, section.sectionId).build().findFirst()
            if(page1 != null && page1.sectionId == section.sectionId) {
                Toast.makeText(context, "You have already learnt $firstPage", Toast.LENGTH_SHORT).show()
                return
            }
            page1 = Page()
            val page1Progress = PageCumulativeProgress()
            page1.createdOn = System.currentTimeMillis()
            page1.pageTitle = firstPage
            page1.sectionId = section.sectionId

            page1Progress.pageId = page1.pageId
            page1Progress.memorizedStatus = PageActionStatus.PENDING
            page1Progress.readStatus = PageActionStatus.PENDING
            page1Progress.notesTakenStatus = PageActionStatus.PENDING
            page1Progress.reviewCount = 0;
            page1Progress.practiceCount = 0;
            page1.progress.target = page1Progress
            section.pages.add(page1)
            section.totalStoryPoints += page1.pageStoryPoints

            sessionTopic.firstPageId = page1.pageId
            sessionTopic.firstPageStoryPoints = page1.pageStoryPoints
            sessionTopic.firstPageTitle = page1.pageTitle
            sessionTopic.topicStoryPoints += page1.pageStoryPoints
        }

        if(!TextUtils.isEmpty(secondPage)) {
            var page2 = ObjectBox.store.boxFor(Page::class.java).query().equal(Page_.pageTitle, secondPage)
                .and().equal(Page_.sectionId, section.sectionId).build().findFirst()
            if(page2 != null && page2.sectionId == section.sectionId) {
                Toast.makeText(context, "You have already learnt $secondPage", Toast.LENGTH_SHORT).show()
                return
            }
            page2 = Page()
            val page2Progress = PageCumulativeProgress()
            page2.createdOn = System.currentTimeMillis()
            page2.pageTitle = secondPage
            page2.sectionId = section.sectionId

            page2Progress.pageId = page2.pageId
            page2Progress.memorizedStatus = PageActionStatus.PENDING
            page2Progress.readStatus = PageActionStatus.PENDING
            page2Progress.notesTakenStatus = PageActionStatus.PENDING
            page2Progress.reviewCount = 0;
            page2Progress.practiceCount = 0;
            page2.progress.target = page2Progress
            section.pages.add(page2)
            section.totalStoryPoints += page2.pageStoryPoints

            sessionTopic.secondPageId = page2.pageId
            sessionTopic.secondPageStoryPoints = page2.pageStoryPoints
            sessionTopic.secondPageTitle = page2.pageTitle
            sessionTopic.topicStoryPoints += page2.pageStoryPoints
        }

        section.noOfPages = section.pages.size

        sessionTopic.sectionId = section.sectionId
        sessionTopic.sectionTitle = section.sectionTitle
        sessionTopic.sessionId = sessionId
        sessionTopic.createdOn = System.currentTimeMillis()

        session.sessionStoryPoints += sessionTopic.topicStoryPoints
        session.topics.add(sessionTopic)

        val secId = ObjectBox.store.boxFor(Section::class.java).put(section)
        if(secId > 0)
            ObjectBox.store.boxFor(Session::class.java).put(session)
        //            ObjectBox.store.boxFor(SessionTopic::class.java).put(sessionTopic)
        loadTopics(sessionId)
    }

    override fun onItemClick(pos: Int) {
        val topicPageModel = editSessionListAdapter.items[pos] as TopicPageModel
        showParagraphCreateDialog(topicPageModel.sessionTopicId, topicPageModel.objectId)
    }

    override fun onItemLongClickListener(pos: Int, view: View) {
        val popUpMenu = PopupMenu(context, view)
        popUpMenu.inflate(R.menu.item_delete_menu)
        popUpMenu.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.deleteItem -> {
                    val sessionTopicBox = ObjectBox.store.boxFor(SessionTopic::class.java)
                    val sectionBox = ObjectBox.store.boxFor(Section::class.java)
                    val pageBox = ObjectBox.store.boxFor(Page::class.java)
                    val paraBox = ObjectBox.store.boxFor(Para::class.java)
                    val progressBox = ObjectBox.store.boxFor(PageCumulativeProgress::class.java)
                    val topicSectionModel = editSessionListAdapter.getItem(pos) as TopicSectionModel
                    val sessionTopic = sessionTopicBox.get(topicSectionModel.topicObId)
                    val section = sectionBox.query().equal(Section_.sectionId, sessionTopic.sectionId).build().findFirst()
                    val firstPage = pageBox.query().equal(Page_.pageId, sessionTopic.firstPageId).build().findFirst()
                    val paras = paraBox.query().equal(Para_.pageId, firstPage!!.pageId).build().find()
                    val progress = progressBox.query().equal(PageCumulativeProgress_.pageId, firstPage.pageId).build().findFirst()
                    progressBox.remove(progress)
                    paraBox.remove(paras)
                    pageBox.remove(firstPage)
                    if(!TextUtils.isEmpty(sessionTopic.secondPageId)) {
                        val secondPage = pageBox.query().equal(Page_.pageId, sessionTopic.secondPageId).build().findFirst()
                        val paras = paraBox.query().equal(Para_.pageId, secondPage!!.pageId).build().find()
                        val progress2 = progressBox.query().equal(PageCumulativeProgress_.pageId, secondPage.pageId).build().findFirst()
                        progressBox.remove(progress2)
                        paraBox.remove(paras)
                        pageBox.remove(secondPage)
                    }
                    sessionTopicBox.remove(sessionTopic)
                    sectionBox.remove(section)
                    loadTopics(sessionId)
                    return@setOnMenuItemClickListener true
                }
                else -> return@setOnMenuItemClickListener false
            }
        }
        popUpMenu.show()
    }

    override fun onButtonClickOnItem(identifier: Int, pos: Int) {
    }

}