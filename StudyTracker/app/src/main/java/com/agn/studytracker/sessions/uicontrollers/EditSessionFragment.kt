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
import androidx.lifecycle.lifecycleScope
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        viewLifecycleOwner.lifecycleScope.launch {
            val sessions = withContext(Dispatchers.IO) { ObjectBox.get().sessionDao().getBySessionId(sessionId) }
            session = sessions[0]
            binding.topicList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            binding.topicList.addItemDecoration(VerticalSpacingItemDecoration(activity?.resources?.getDimensionPixelOffset(R.dimen.dp_1)!!))
            binding.topicList.adapter = EditSessionListAdapter(this@EditSessionFragment)
            loadTopics(sessionId)
            (activity as MainActivity).setTitle("Edit ${session.sessionTitle}")
        }
    }

    private fun loadExistingSections(
        sectionView: AutoCompleteTextView,
        page1View: AutoCompleteTextView,
        page2View: AutoCompleteTextView,
        dialog: Dialog
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            val (sectionList, pageList) = withContext(Dispatchers.IO) {
                val sections = ObjectBox.get().sectionDao().getBySubjectId(session.subjectId)
                val sNames = sections.map { it.sectionTitle }
                val pNames = sections.flatMap { ObjectBox.get().pageDao().getBySectionIdSync(it.sectionId).map { p -> p.pageTitle } }
                Pair(sNames, pNames)
            }
            val secAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, sectionList)
            val pageAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, pageList)
            sectionView.threshold = 1; sectionView.setAdapter(secAdapter)
            page1View.threshold = 1; page1View.setAdapter(pageAdapter)
            page2View.threshold = 1; page2View.setAdapter(pageAdapter)
            dialog.show()
        }
    }

    private fun loadTopics(sessionId: String?) {
        viewLifecycleOwner.lifecycleScope.launch {
            val listItems = withContext(Dispatchers.IO) {
                val topics = ObjectBox.get().sessionTopicDao().getBySessionId(sessionId)
                val items = ArrayList<BaseExpandableListAdapter.ExpandableListItem>()
                for (topic in topics) {
                    items.add(TopicSectionModel().apply {
                        sectionTitle = topic.sectionTitle
                        setSectionId(topic.sectionId)
                        topicId = topic.topicId
                        topicObId = topic.obId
                    })
                    if (!TextUtils.isEmpty(topic.firstPageId))
                        items.add(TopicPageModel().apply {
                            pageTitle = topic.firstPageTitle
                            setPageId(topic.firstPageId)
                            setSectionId(topic.sectionId)
                            sessionTopicId = topic.topicId
                            paraformattedContent = fetchAndFormatParaList(topic.firstPageId)
                        })
                    if (!TextUtils.isEmpty(topic.secondPageId))
                        items.add(TopicPageModel().apply {
                            pageTitle = topic.secondPageTitle
                            setPageId(topic.secondPageId)
                            setSectionId(topic.sectionId)
                            sessionTopicId = topic.topicId
                            paraformattedContent = fetchAndFormatParaList(topic.secondPageId)
                        })
                }
                items
            }
            val approxTime = (session.sessionStoryPoints * GlobalConstants.ONE_STORY_POINT_IN_HOURS).roundToInt()
            if (listItems.isNotEmpty()) {
                binding.approxTime.visibility = View.VISIBLE
                binding.approxTime.text = "Approx Effort Needed : $approxTime hrs"
            }
            editSessionListAdapter = binding.topicList.adapter as EditSessionListAdapter
            editSessionListAdapter.setmItems(listItems)
            binding.topicList.adapter = editSessionListAdapter
        }
    }

    private fun fetchAndFormatParaList(pageId: String): String {
        val list = ObjectBox.get().paraDao().getByPageIdSync(pageId)
        if (list.isEmpty()) return ""
        return buildString {
            append("<ul>\n")
            list.forEach { append("<li>      ${it.paraTitle}</li>\n") }
            append("</ul>\n")
        }
    }

    fun showDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_create_session_topic)
        val sectionTitle = dialog.findViewById(R.id.topicSectionTitle) as AutoCompleteTextView
        val firstPageTitle = dialog.findViewById(R.id.topicFirstPageTitle) as AutoCompleteTextView
        val secondPageTitle = dialog.findViewById(R.id.topicSecondPageTitle) as AutoCompleteTextView
        (dialog.findViewById(R.id.btnSubmit) as Button).setOnClickListener {
            if (TextUtils.isEmpty(sectionTitle.text) || TextUtils.isEmpty(firstPageTitle.text)) {
                Toast.makeText(context, "Section/1st topic mandatory", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            createSessionTopic(sectionTitle.text.toString(), firstPageTitle.text.toString(), secondPageTitle.text.toString())
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
        (dialog.findViewById(R.id.btnSubmit) as Button).setOnClickListener {
            val paralist = listOfNotNull(
                para1.text.toString().takeIf { it.isNotEmpty() },
                para2.text.toString().takeIf { it.isNotEmpty() },
                para3.text.toString().takeIf { it.isNotEmpty() },
                para4.text.toString().takeIf { it.isNotEmpty() },
                para5.text.toString().takeIf { it.isNotEmpty() }
            )
            if (paralist.isNotEmpty()) createParas(topicId, pageId, paralist)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun createParas(topicId: String, pageId: String, paralist: List<String>) {
        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val db = ObjectBox.get()
                val paraList = paralist.map { Para().apply { createdOn = System.currentTimeMillis(); paraTitle = it; this.pageId = pageId } }
                var totalPoints = paraList.sumOf { it.storyPoint.toDouble() }.toFloat()
                db.paraDao().insertList(paraList)

                val page = db.pageDao().getByPageIdSync(pageId) ?: return@withContext
                val section = db.sectionDao().getBySectionIdSync(page.sectionId) ?: return@withContext
                val topic = db.sessionTopicDao().getByTopicIdSync(topicId) ?: return@withContext

                page.pageStoryPoints += totalPoints
                section.totalStoryPoints += totalPoints
                if (topic.firstPageId == page.pageId) topic.firstPageStoryPoints = page.pageStoryPoints
                else topic.secondPageStoryPoints = page.pageStoryPoints
                topic.topicStoryPoints += totalPoints
                session.sessionStoryPoints += totalPoints

                db.pageDao().update(page)
                db.sectionDao().update(section)
                db.sessionTopicDao().update(topic)
                db.sessionDao().update(session)
            }
            loadTopics(sessionId)
        }
    }

    private fun createSessionTopic(sectiontitle: String, firstPage: String, secondPage: String) {
        if (TextUtils.isEmpty(sectiontitle) || firstPage == secondPage) return
        viewLifecycleOwner.lifecycleScope.launch {
            val db = ObjectBox.get()
            val result = withContext(Dispatchers.IO) {
                val existing = db.sessionTopicDao().getBySessionAndSectionTitleSync(sessionId!!, sectiontitle)
                if (existing != null) return@withContext "duplicate_section"

                var section = db.sectionDao().getByTitleAndSubjectSync(sectiontitle, session.subjectId)
                    ?: Section().apply { createdOn = System.currentTimeMillis(); sectionTitle = sectiontitle; subjectId = session.subjectId }

                val topic = SessionTopic()

                if (!TextUtils.isEmpty(firstPage)) {
                    if (db.pageDao().getByTitleAndSectionSync(firstPage, section.sectionId) != null)
                        return@withContext "duplicate_page:$firstPage"
                    val page1 = Page().apply { createdOn = System.currentTimeMillis(); pageTitle = firstPage; sectionId = section.sectionId }
                    val prog1 = PageCumulativeProgress().apply {
                        pageId = page1.pageId; memorizedStatus = PageActionStatus.PENDING
                        readStatus = PageActionStatus.PENDING; notesTakenStatus = PageActionStatus.PENDING
                    }
                    section.totalStoryPoints += page1.pageStoryPoints
                    topic.firstPageId = page1.pageId; topic.firstPageStoryPoints = page1.pageStoryPoints; topic.firstPageTitle = page1.pageTitle
                    topic.topicStoryPoints += page1.pageStoryPoints
                    db.pageDao().insert(page1); db.pageCumulativeProgressDao().insert(prog1)
                }

                if (!TextUtils.isEmpty(secondPage)) {
                    if (db.pageDao().getByTitleAndSectionSync(secondPage, section.sectionId) != null)
                        return@withContext "duplicate_page:$secondPage"
                    val page2 = Page().apply { createdOn = System.currentTimeMillis(); pageTitle = secondPage; sectionId = section.sectionId }
                    val prog2 = PageCumulativeProgress().apply {
                        pageId = page2.pageId; memorizedStatus = PageActionStatus.PENDING
                        readStatus = PageActionStatus.PENDING; notesTakenStatus = PageActionStatus.PENDING
                    }
                    section.totalStoryPoints += page2.pageStoryPoints
                    topic.secondPageId = page2.pageId; topic.secondPageStoryPoints = page2.pageStoryPoints; topic.secondPageTitle = page2.pageTitle
                    topic.topicStoryPoints += page2.pageStoryPoints
                    db.pageDao().insert(page2); db.pageCumulativeProgressDao().insert(prog2)
                }

                section.noOfPages = db.pageDao().getBySectionIdSync(section.sectionId).size
                topic.apply { sectionId = section.sectionId; sectionTitle = section.sectionTitle; this.sessionId = this@EditSessionFragment.sessionId; createdOn = System.currentTimeMillis() }
                session.sessionStoryPoints += topic.topicStoryPoints

                val secId = db.sectionDao().insert(section)
                if (secId > 0) { db.sessionTopicDao().insert(topic); db.sessionDao().update(session) }
                "ok"
            }
            when {
                result == "duplicate_section" -> Toast.makeText(context, "You have already added $sectiontitle to this session", Toast.LENGTH_SHORT).show()
                result.startsWith("duplicate_page:") -> Toast.makeText(context, "You have already learnt ${result.substringAfter(":")}", Toast.LENGTH_SHORT).show()
                else -> loadTopics(sessionId)
            }
        }
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
                    viewLifecycleOwner.lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            val db = ObjectBox.get()
                            val topicSectionModel = editSessionListAdapter.getItem(pos) as TopicSectionModel
                            val topic = db.sessionTopicDao().getByObIdSync(topicSectionModel.topicObId) ?: return@withContext
                            db.pageDao().getByPageIdSync(topic.firstPageId)?.let { page ->
                                db.pageCumulativeProgressDao().getByPageIdSync(page.pageId)?.let { db.pageCumulativeProgressDao().delete(it) }
                                db.paraDao().deleteList(db.paraDao().getByPageIdSync(page.pageId))
                                db.pageDao().delete(page)
                            }
                            if (!TextUtils.isEmpty(topic.secondPageId)) {
                                db.pageDao().getByPageIdSync(topic.secondPageId)?.let { page ->
                                    db.pageCumulativeProgressDao().getByPageIdSync(page.pageId)?.let { db.pageCumulativeProgressDao().delete(it) }
                                    db.paraDao().deleteList(db.paraDao().getByPageIdSync(page.pageId))
                                    db.pageDao().delete(page)
                                }
                            }
                            db.sessionTopicDao().delete(topic)
                            db.sectionDao().getBySectionIdSync(topic.sectionId)?.let { db.sectionDao().deleteList(listOf(it)) }
                        }
                        loadTopics(sessionId)
                    }
                    return@setOnMenuItemClickListener true
                }
                else -> return@setOnMenuItemClickListener false
            }
        }
        popUpMenu.show()
    }

    override fun onButtonClickOnItem(identifier: Int, pos: Int) {}
}
