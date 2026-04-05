package com.agn.studytracker.sessions.uicontrollers

import android.graphics.Typeface
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.irshulx.Editor
import com.github.irshulx.models.EditorTextStyle
import com.agn.corea.models.session.Session
import com.agn.corea.models.session.SessionAssessment
import com.agn.studytracker.R
import com.agn.studytracker.common.adapter.HorizontalSpacingItemDecoration
import com.agn.studytracker.common.adapter.OnItemClickListener
import com.agn.studytracker.common.objectbox.ObjectBox
import com.agn.studytracker.common.uicontrollers.MainActivity
import com.agn.studytracker.databinding.FragmentAssessSessionBinding
import com.agn.studytracker.sessions.adapters.AssessPageContentListAdapter
import com.agn.studytracker.sessions.models.AssessmentPageContentModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AssessSessionFragment : Fragment(), OnItemClickListener {

    lateinit var session: Session
    private var sessionId: String? = ""
    lateinit var binding: FragmentAssessSessionBinding
    lateinit var assessmentAdapter: AssessPageContentListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_assess_session, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionId = arguments?.getString("session_id", "")
        binding.pageContentList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.pageContentList.addItemDecoration(HorizontalSpacingItemDecoration(activity?.resources?.getDimensionPixelOffset(R.dimen.dp_8)!!))
        binding.btnAssess.setOnClickListener { submitAssessment() }
        val heading = getHeadingTypeface()
        val content = getContentface()
        listOf(binding.sessionSummary, binding.questions, binding.todos, binding.planning).forEach {
            it.headingTypeface = heading; it.contentTypeface = content; it.setNormalTextSize(16)
        }
        initializeEditorTools()
        assessmentAdapter = AssessPageContentListAdapter(this)
        binding.pageContentList.adapter = assessmentAdapter
        loadSession(sessionId)
    }

    private fun loadSession(sessionId: String?) {
        viewLifecycleOwner.lifecycleScope.launch {
            val sessions = withContext(Dispatchers.IO) { ObjectBox.get().sessionDao().getBySessionId(sessionId) }
            session = sessions[0]
            val myactivity = activity as MainActivity
            if (!session.isSessionAssessed) {
                myactivity.setTitle("Assess ${session.sessionTitle}")
                binding.btnAssess.visibility = View.VISIBLE
                loadSessionTopics()
            } else {
                myactivity.setTitle("View Assessment")
                binding.pageContentList.visibility = View.GONE
                binding.btnAssess.visibility = View.GONE
                binding.tools.visibility = View.GONE
                binding.sessionSummary.visibility = View.GONE
                binding.planning.visibility = View.GONE
                binding.questions.visibility = View.GONE
                binding.todos.visibility = View.GONE
                loadAssessments()
            }
        }
    }

    private fun loadAssessments() {
        viewLifecycleOwner.lifecycleScope.launch {
            val assessments = withContext(Dispatchers.IO) { ObjectBox.get().sessionAssessmentDao().getBySessionId(sessionId) }
            binding.viewSummary.visibility = View.VISIBLE
            binding.viewPlanning.visibility = View.VISIBLE
            binding.viewQuestions.visibility = View.VISIBLE
            binding.viewTodo.visibility = View.VISIBLE
            binding.viewSummary.text = Html.fromHtml(assessments[0].sessionSummary, Html.FROM_HTML_MODE_COMPACT)
            binding.viewPlanning.text = Html.fromHtml(assessments[0].nextSessionPlan, Html.FROM_HTML_MODE_COMPACT)
            binding.viewQuestions.text = Html.fromHtml(assessments[0].questions ?: "...", Html.FROM_HTML_MODE_COMPACT)
            binding.viewTodo.text = Html.fromHtml(assessments[0].todos ?: "...", Html.FROM_HTML_MODE_COMPACT)
        }
    }

    private fun loadSessionTopics() {
        viewLifecycleOwner.lifecycleScope.launch {
            val pageContentList = withContext(Dispatchers.IO) {
                val topics = ObjectBox.get().sessionTopicDao().getBySessionId(sessionId)
                val list = ArrayList<AssessmentPageContentModel>()
                for (topic in topics) {
                    if (!TextUtils.isEmpty(topic.firstPageId)) list.add(createPageContent(topic.firstPageId, topic.firstPageTitle))
                    if (!TextUtils.isEmpty(topic.secondPageId)) list.add(createPageContent(topic.secondPageId, topic.secondPageTitle))
                }
                list
            }
            assessmentAdapter.setmItems(pageContentList)
            binding.pageContentList.adapter = assessmentAdapter
        }
    }

    private fun createPageContent(pageId: String, pageTitle: String): AssessmentPageContentModel {
        val pageContent = AssessmentPageContentModel()
        if (!TextUtils.isEmpty(pageId)) {
            pageContent.pageTitle = pageTitle
            val paraList = ObjectBox.get().paraDao().getByPageIdSync(pageId)
            if (paraList.isNotEmpty()) {
                pageContent.paraContent = buildString {
                    paraList.forEachIndexed { i, para -> append("\n    ${i + 1} -> ${para.paraTitle}") }
                }
            }
        }
        return pageContent
    }

    override fun onItemClick(pos: Int) {}
    override fun onItemLongClickListener(pos: Int, view: View) {}
    override fun onButtonClickOnItem(identifier: Int, pos: Int) = Unit

    private fun initializeEditorTools() {
        binding.root.findViewById<Button>(R.id.action_h1).setOnClickListener { getEditor().updateTextStyle(EditorTextStyle.H1) }
        binding.root.findViewById<Button>(R.id.action_h2).setOnClickListener { getEditor().updateTextStyle(EditorTextStyle.H2) }
        binding.root.findViewById<Button>(R.id.action_h3).setOnClickListener { getEditor().updateTextStyle(EditorTextStyle.H3) }
        binding.root.findViewById<AppCompatImageButton>(R.id.action_bold).setOnClickListener { getEditor().updateTextStyle(EditorTextStyle.BOLD) }
        binding.root.findViewById<AppCompatImageButton>(R.id.action_Italic).setOnClickListener { getEditor().updateTextStyle(EditorTextStyle.ITALIC) }
        binding.root.findViewById<AppCompatImageButton>(R.id.action_indent).setOnClickListener { getEditor().updateTextStyle(EditorTextStyle.INDENT) }
        binding.root.findViewById<AppCompatImageButton>(R.id.action_outdent).setOnClickListener { getEditor().updateTextStyle(EditorTextStyle.OUTDENT) }
        binding.root.findViewById<AppCompatImageButton>(R.id.action_bulleted).setOnClickListener { getEditor().insertList(false) }
        binding.root.findViewById<AppCompatImageButton>(R.id.action_color).setOnClickListener { getEditor().updateTextColor("#FF3333") }
        binding.root.findViewById<AppCompatImageButton>(R.id.action_unordered_numbered).setOnClickListener { getEditor().insertList(true) }
        binding.root.findViewById<AppCompatImageButton>(R.id.action_hr).setOnClickListener { getEditor().insertDivider() }
        binding.root.findViewById<AppCompatImageButton>(R.id.action_insert_image).setOnClickListener { getEditor().openImagePicker() }
        binding.root.findViewById<AppCompatImageButton>(R.id.action_insert_link).setOnClickListener { getEditor().insertLink() }
        binding.root.findViewById<AppCompatImageButton>(R.id.action_erase).setOnClickListener { getEditor().clearAllContents() }
        binding.root.findViewById<AppCompatImageButton>(R.id.action_blockquote).setOnClickListener { getEditor().updateTextStyle(EditorTextStyle.BLOCKQUOTE) }
    }

    private fun getEditor(): Editor = when {
        binding.sessionSummary.hasFocus() -> binding.sessionSummary
        binding.planning.hasFocus() -> binding.planning
        binding.questions.hasFocus() -> binding.questions
        else -> binding.todos
    }

    private fun submitAssessment() {
        if (!validateAssessment()) return
        val assessment = SessionAssessment().apply {
            sessionId = this@AssessSessionFragment.sessionId
            sessionSummary = binding.sessionSummary.contentAsHTML
            nextSessionPlan = binding.planning.contentAsHTML
            questions = binding.questions.contentAsHTML
            todos = binding.todos.contentAsHTML
        }
        viewLifecycleOwner.lifecycleScope.launch {
            val id = withContext(Dispatchers.IO) {
                val insertId = ObjectBox.get().sessionAssessmentDao().insert(assessment)
                if (insertId > 0L) {
                    session.isSessionAssessed = true
                    ObjectBox.get().sessionDao().update(session)
                }
                insertId
            }
            if (id > 0L) {
                Toast.makeText(context, "Assessment Submitted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Assessment couldn't be submitted! Please try again later", Toast.LENGTH_SHORT).show()
            }
            NavHostFragment.findNavController(this@AssessSessionFragment).popBackStack()
        }
    }

    private fun validateAssessment(): Boolean {
        if (TextUtils.isEmpty(binding.sessionSummary.contentAsHTML) ||
            TextUtils.isEmpty(binding.planning.contentAsHTML) ||
            TextUtils.isEmpty(binding.questions.contentAsHTML) ||
            TextUtils.isEmpty(binding.todos.contentAsHTML)) {
            Toast.makeText(context, "Assessment fields cannot be empty", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    fun getHeadingTypeface(): Map<Int, String> = mapOf(
        Typeface.NORMAL to "fonts/GreycliffCF-Bold.ttf",
        Typeface.BOLD to "fonts/GreycliffCF-Heavy.ttf",
        Typeface.ITALIC to "fonts/GreycliffCF-Heavy.ttf",
        Typeface.BOLD_ITALIC to "fonts/GreycliffCF-Bold.ttf"
    )

    fun getContentface(): Map<Int, String> = mapOf(
        Typeface.NORMAL to "fonts/Lato-Medium.ttf",
        Typeface.BOLD to "fonts/Lato-Bold.ttf",
        Typeface.ITALIC to "fonts/Lato-MediumItalic.ttf",
        Typeface.BOLD_ITALIC to "fonts/Lato-BoldItalic.ttf"
    )
}
