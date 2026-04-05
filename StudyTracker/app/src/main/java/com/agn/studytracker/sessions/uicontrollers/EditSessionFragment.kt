package com.agn.studytracker.sessions.uicontrollers

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
import com.agn.corea.constants.GlobalConstants
import com.agn.corea.constants.PageActionStatus
import com.agn.corea.models.session.Session
import com.agn.corea.models.session.SessionTopic
import com.agn.corea.models.subjects.*
import com.agn.studytracker.R
import com.agn.studytracker.common.adapter.BaseExpandableListAdapter
import com.agn.studytracker.common.adapter.OnItemClickListener
import com.agn.studytracker.common.adapter.VerticalSpacingItemDecoration
import com.agn.studytracker.common.objectbox.ObjectBox
import com.agn.studytracker.common.uicontrollers.MainActivity
import com.agn.studytracker.databinding.FragmentEditSessionBinding
import com.agn.studytracker.sessions.adapters.EditSessionListAdapter
import com.agn.studytracker.sessions.models.TopicPageModel
import com.agn.studytracker.sessions.models.TopicSectionModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlin.math.roundToInt


class EditSessionFragment : Fragment(), OnItemClickListener {

    lateinit var binding: FragmentEditSessionBinding
    lateinit var editSessionListAdapter: EditSessionListAdapter
    lateinit var session: Session
    private var sessionId: String? = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_session, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionId = arguments?.getString("session_id", "")
        loadSession(sessionId)
        binding.btnCreateSessionTopic.setOnClickListener { showDialog() }
    }

    private fun loadSession(sessionId: String?) {
        ObjectBox.get().sessionDao().getBySessionId(sessionId)
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

    private fun loadExistingSections(
        autoCompleteTextView: AutoCompleteTextView,
        page1AutoCompleteTextView: AutoCompleteTextView,
        page2AutoCompleteTextView: AutoCompleteTextView,
        dialog: Dialog
    ) {
        ObjectBox.get().sectionDao().getBySubjectId(session.subjectId)
            .subscribeOn(Schedulers.io())
            .map { sections ->
                val sectionList = ArrayList<String>()
                val pageList = ArrayList<String>()
                val map = HashMap<String, List<String>>()
                for (section in sections) {
                    sectionList.add(section.sectionTitle)
                    val pages = ObjectBox.get().pageDao().getBySectionIdSync(section.sectionId)
                    for (page in pages)
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
        ObjectBox.get().sessionTopicDao().getBySessionId(sessionId)
            .subscribeOn(Schedulers.io())
            .map { topics ->
                val listItems = ArrayList<BaseExpandableListAdapter.ExpandableListItem>()
                for (sessionTopic in topics) {
                    val topicSectionModel = TopicSectionModel()
                    topicSectionModel.sectionTitle = sessionTopic.sectionTitle
                    topicSectionModel.setSectionId(sessionTopic.sectionId)
                    topicSectionModel.topicId = sessionTopic.topicId
                    topicSectionModel.topicObId = sessionTopic.obId
                    listItems.add(topicSectionModel)

                    if (!TextUtils.isEmpty(sessionTopic.firstPageId)) {
                        val firstPageModel = TopicPageModel()
                        firstPageModel.pageTitle = sessionTopic.firstPageTitle
                        firstPageModel.setPageId(sessionTopic.firstPageId)
                        firstPageModel.setSectionId(sessionTopic.sectionId)
                        firstPageModel.sessionTopicId = sessionTopic.topicId
                        firstPageModel.paraformattedContent = fetchAndFormatParaList(sessionTopic.firstPageId)
                        listItems.add(firstPageModel)
                    }

                    if (!TextUtils.isEmpty(sessionTopic.secondPageId)) {
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
                val sessionApproxTime = (session.sessionStoryPoints * GlobalConstants.ONE_STORY_POINT_IN_HOURS).roundToInt()
                if (it.isNotEmpty()) {
                    binding.approxTime.visibility = View.VISIBLE
                    binding.approxTime.text = "Approx Effort Needed : $sessionApproxTime hrs"
                }
                editSessionListAdapter = binding.topicList.adapter as EditSessionListAdapter
                editSessionListAdapter.setmItems(it)
                binding.topicList.adapter = editSessionListAdapter
            })
    }

    private fun fetchAndFormatParaList(pageId: String): String {
        val list = ObjectBox.get().paraDao().getByPageIdSync(pageId)
        if (list.isNotEmpty()) {
            val sb = StringBuilder()
            sb.append("<ul>\n")
            for (para in list)
                sb.append("<li>      ${para.paraTitle}</li>\n")
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
        dialogButton.setOnClickListener {
            if (TextUtils.isEmpty(sectionTitle.editableText.toString()) || TextUtils.isEmpty(firstPageTitle.editableText.toString())) {
                Toast.makeText(context, "Section/1st topic mandatory", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            createSessionTopic(
                sectionTitle.editableText.toString(),
                firstPageTitle.editableText.toString(),
                secondPageTitle.editableText.toString()
            )
            dialog.dismiss()
        }
        loadExistingSections(sectionTitle, firstPageTitle, secondPageTitle, dialog)
    }

    fun showParagraphCreateDialog(topicId: String, pageId: String) {
        if (ObjectBox.get().paraDao().countByPageId(pageId) > 0) return
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
        dialogButton.setOnClickListener {
            val paralist = ArrayList<String>()
            if (!TextUtils.isEmpty(para1.editableText.toString())) paralist.add(para1.editableText.toString())
            if (!TextUtils.isEmpty(para2.editableText.toString())) paralist.add(para2.editableText.toString())
            if (!TextUtils.isEmpty(para3.editableText.toString())) paralist.add(para3.editableText.toString())
            if (!TextUtils.isEmpty(para4.editableText.toString())) paralist.add(para4.editableText.toString())
            if (!TextUtils.isEmpty(para5.editableText.toString())) paralist.add(para5.editableText.toString())
            if (paralist.isNotEmpty()) createParas(topicId, pageId, paralist)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun createParas(topicId: String, pageId: String, paralist: ArrayList<String>) {
        val db = ObjectBox.get()
        val paraList = ArrayList<Para>()
        var totalParaStoryPoints = 0.0f
        for (para in paralist) {
            val paragraph = Para()
            paragraph.createdOn = System.currentTimeMillis()
            paragraph.paraTitle = para
            paragraph.pageId = pageId
            paraList.add(paragraph)
            totalParaStoryPoints += paragraph.storyPoint
        }
        db.paraDao().insertList(paraList)

        val page = db.pageDao().getByPageIdSync(pageId) ?: return
        val section = db.sectionDao().getBySectionIdSync(page.sectionId) ?: return
        val sessionTopic = db.sessionTopicDao().getByTopicIdSync(topicId) ?: return

        page.pageStoryPoints += totalParaStoryPoints
        section.totalStoryPoints += totalParaStoryPoints
        if (sessionTopic.firstPageId == page.pageId)
            sessionTopic.firstPageStoryPoints = page.pageStoryPoints
        else
            sessionTopic.secondPageStoryPoints = page.pageStoryPoints
        sessionTopic.topicStoryPoints += totalParaStoryPoints
        session.sessionStoryPoints += totalParaStoryPoints

        db.pageDao().update(page)
        db.sectionDao().update(section)
        db.sessionTopicDao().update(sessionTopic)
        db.sessionDao().update(session)

        loadTopics(sessionId)
    }

    private fun createSessionTopic(sectiontitle: String, firstPage: String, secondPage: String) {
        if (TextUtils.isEmpty(sectiontitle) || firstPage == secondPage) return
        val db = ObjectBox.get()

        val existingTopic = db.sessionTopicDao().getBySessionAndSectionTitleSync(sessionId!!, sectiontitle)
        if (existingTopic != null) {
            Toast.makeText(context, "You have already added $sectiontitle to this session", Toast.LENGTH_SHORT).show()
            return
        }

        var section = db.sectionDao().getByTitleAndSubjectSync(sectiontitle, session.subjectId)
        if (section == null) {
            section = Section()
            section.createdOn = System.currentTimeMillis()
            section.sectionTitle = sectiontitle
            section.subjectId = session.subjectId
        }

        val sessionTopic = SessionTopic()

        if (!TextUtils.isEmpty(firstPage)) {
            val existing = db.pageDao().getByTitleAndSectionSync(firstPage, section.sectionId)
            if (existing != null) {
                Toast.makeText(context, "You have already learnt $firstPage", Toast.LENGTH_SHORT).show()
                return
            }
            val page1 = Page()
            page1.createdOn = System.currentTimeMillis()
            page1.pageTitle = firstPage
            page1.sectionId = section.sectionId

            val page1Progress = PageCumulativeProgress()
            page1Progress.pageId = page1.pageId
            page1Progress.memorizedStatus = PageActionStatus.PENDING
            page1Progress.readStatus = PageActionStatus.PENDING
            page1Progress.notesTakenStatus = PageActionStatus.PENDING
            page1Progress.reviewCount = 0
            page1Progress.practiceCount = 0

            section.totalStoryPoints += page1.pageStoryPoints
            sessionTopic.firstPageId = page1.pageId
            sessionTopic.firstPageStoryPoints = page1.pageStoryPoints
            sessionTopic.firstPageTitle = page1.pageTitle
            sessionTopic.topicStoryPoints += page1.pageStoryPoints

            db.pageDao().insert(page1)
            db.pageCumulativeProgressDao().insert(page1Progress)
        }

        if (!TextUtils.isEmpty(secondPage)) {
            val existing = db.pageDao().getByTitleAndSectionSync(secondPage, section.sectionId)
            if (existing != null) {
                Toast.makeText(context, "You have already learnt $secondPage", Toast.LENGTH_SHORT).show()
                return
            }
            val page2 = Page()
            page2.createdOn = System.currentTimeMillis()
            page2.pageTitle = secondPage
            page2.sectionId = section.sectionId

            val page2Progress = PageCumulativeProgress()
            page2Progress.pageId = page2.pageId
            page2Progress.memorizedStatus = PageActionStatus.PENDING
            page2Progress.readStatus = PageActionStatus.PENDING
            page2Progress.notesTakenStatus = PageActionStatus.PENDING
            page2Progress.reviewCount = 0
            page2Progress.practiceCount = 0

            section.totalStoryPoints += page2.pageStoryPoints
            sessionTopic.secondPageId = page2.pageId
            sessionTopic.secondPageStoryPoints = page2.pageStoryPoints
            sessionTopic.secondPageTitle = page2.pageTitle
            sessionTopic.topicStoryPoints += page2.pageStoryPoints

            db.pageDao().insert(page2)
            db.pageCumulativeProgressDao().insert(page2Progress)
        }

        section.noOfPages = db.pageDao().getBySectionIdSync(section.sectionId).size

        sessionTopic.sectionId = section.sectionId
        sessionTopic.sectionTitle = section.sectionTitle
        sessionTopic.sessionId = sessionId
        sessionTopic.createdOn = System.currentTimeMillis()

        session.sessionStoryPoints += sessionTopic.topicStoryPoints

        val secId = db.sectionDao().insert(section)
        if (secId > 0) {
            db.sessionTopicDao().insert(sessionTopic)
            db.sessionDao().update(session)
        }
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
            when (it.itemId) {
                R.id.deleteItem -> {
                    val db = ObjectBox.get()
                    val topicSectionModel = editSessionListAdapter.getItem(pos) as TopicSectionModel
                    val sessionTopic = db.sessionTopicDao().getByObIdSync(topicSectionModel.topicObId) ?: return@setOnMenuItemClickListener true
                    val section = db.sectionDao().getBySectionIdSync(sessionTopic.sectionId)
                    val firstPage = db.pageDao().getByPageIdSync(sessionTopic.firstPageId)
                    if (firstPage != null) {
                        db.pageCumulativeProgressDao().getByPageIdSync(firstPage.pageId)?.let { db.pageCumulativeProgressDao().delete(it) }
                        db.paraDao().deleteList(db.paraDao().getByPageIdSync(firstPage.pageId))
                        db.pageDao().delete(firstPage)
                    }
                    if (!TextUtils.isEmpty(sessionTopic.secondPageId)) {
                        val secondPage = db.pageDao().getByPageIdSync(sessionTopic.secondPageId)
                        if (secondPage != null) {
                            db.pageCumulativeProgressDao().getByPageIdSync(secondPage.pageId)?.let { db.pageCumulativeProgressDao().delete(it) }
                            db.paraDao().deleteList(db.paraDao().getByPageIdSync(secondPage.pageId))
                            db.pageDao().delete(secondPage)
                        }
                    }
                    db.sessionTopicDao().delete(sessionTopic)
                    if (section != null) db.sectionDao().deleteList(listOf(section))
                    loadTopics(sessionId)
                    return@setOnMenuItemClickListener true
                }
                else -> return@setOnMenuItemClickListener false
            }
        }
        popUpMenu.show()
    }

    override fun onButtonClickOnItem(identifier: Int, pos: Int) {}
}
