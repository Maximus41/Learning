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
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.irshulx.Editor
import com.github.irshulx.models.EditorTextStyle
import com.agn.corea.models.session.*
import com.agn.corea.models.subjects.Para
import com.agn.corea.models.subjects.Para_
import com.agn.studytracker.R
import com.agn.studytracker.common.adapter.HorizontalSpacingItemDecoration
import com.agn.studytracker.common.adapter.OnItemClickListener
import com.agn.studytracker.common.objectbox.ObjectBox
import com.agn.studytracker.common.uicontrollers.MainActivity
import com.agn.studytracker.databinding.FragmentAssessSessionBinding
import com.agn.studytracker.sessions.adapters.AssessPageContentListAdapter
import com.agn.studytracker.sessions.models.AssessmentPageContentModel
import io.objectbox.rx.RxQuery
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers


class AssessSessionFragment : Fragment() , OnItemClickListener {
    lateinit var session: Session
    private var sessionId: String? = ""
    lateinit var binding : FragmentAssessSessionBinding
    lateinit var assessmentAdapter : AssessPageContentListAdapter

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_assess_session, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionId = arguments?.getString("session_id", "")
        binding.pageContentList.layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
        )
        binding.pageContentList.addItemDecoration(
                HorizontalSpacingItemDecoration(
                        activity?.resources?.getDimensionPixelOffset(
                                R.dimen.dp_8
                        )!!
                )
        )

        binding.btnAssess.setOnClickListener {
            submitAssessment()
        }
        val headingTypeface = getHeadingTypeface()
        val contentTypeface = getContentface()
        binding.sessionSummary.headingTypeface = headingTypeface
        binding.sessionSummary.contentTypeface = contentTypeface
        binding.questions.headingTypeface = headingTypeface
        binding.questions.contentTypeface = contentTypeface
        binding.todos.headingTypeface = headingTypeface
        binding.todos.contentTypeface = contentTypeface
        binding.planning.headingTypeface = headingTypeface
        binding.planning.contentTypeface = contentTypeface
        binding.sessionSummary.setNormalTextSize(16)
        binding.planning.setNormalTextSize(16)
        binding.questions.setNormalTextSize(16)
        binding.planning.setNormalTextSize(16)
        initializeEditorTools()
        assessmentAdapter = AssessPageContentListAdapter(this)
        binding.pageContentList.adapter = assessmentAdapter
        loadSession(sessionId)
    }

    private fun loadSession(sessionId: String?) {
        RxQuery.single(
                ObjectBox.store.boxFor(Session::class.java).query().equal(
                        Session_.sessionId,
                        sessionId
                ).build()
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    session = it[0]
                    val myactivity = activity as MainActivity
                    if (!session.isSessionAssessed) {
                        myactivity.setTitle("Assess ${session.sessionTitle}")
                        binding.btnAssess.visibility = View.VISIBLE
                        loadSessionTopics()
                    } else {
                        myactivity.setTitle("View Assessment")
                        binding.pageContentList.visibility = View.GONE
                        binding.btnAssess.visibility = View.GONE
                        loadAssessments()
                        binding.tools.visibility = View.GONE
                        binding.sessionSummary.visibility = View.GONE
                        binding.planning.visibility = View.GONE
                        binding.questions.visibility = View.GONE
                        binding.todos.visibility = View.GONE
                    }
                })
    }

    private fun loadAssessments() {
        RxQuery.single(
                ObjectBox.store.boxFor(SessionAssessment::class.java).query().equal(
                        SessionAssessment_.sessionId,
                        sessionId
                ).build()
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    binding.viewSummary.visibility = View.VISIBLE
                    binding.viewPlanning.visibility = View.VISIBLE
                    binding.viewQuestions.visibility = View.VISIBLE
                    binding.viewTodo.visibility = View.VISIBLE
                    binding.viewSummary.text = Html.fromHtml(it[0].sessionSummary)
                    binding.viewPlanning.text = Html.fromHtml(it[0].nextSessionPlan)
                    binding.viewQuestions.text = Html.fromHtml(it[0].questions ?: "...")
                    binding.viewTodo.text = Html.fromHtml(it[0].todos ?: "...")
                })
    }

    private fun loadSessionTopics() {
        val disposable = RxQuery.single(
                ObjectBox.store.boxFor(SessionTopic::class.java).query().equal(
                        SessionTopic_.sessionId,
                        sessionId
                ).build()
        )
                .subscribeOn(Schedulers.io())
                .map {
                    val pageContentList = ArrayList<AssessmentPageContentModel>()
                    for(topic in it) {
                        if(!TextUtils.isEmpty(topic.firstPageId)) {
                            val pageContent1 = createPageContent(topic.firstPageId, topic.firstPageTitle)
                            pageContentList.add(pageContent1)
                        }
                        if(!TextUtils.isEmpty(topic.secondPageId)) {
                            val pageContent2 = createPageContent(
                                    topic.secondPageId,
                                    topic.secondPageTitle
                            )
                            pageContentList.add(pageContent2)
                        }
                    }
                    return@map pageContentList
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    assessmentAdapter.setmItems(it)
                    binding.pageContentList.adapter = assessmentAdapter
                })
    }

    private fun createPageContent(pageId: String, pageTitle: String) : AssessmentPageContentModel {
        val pageContent = AssessmentPageContentModel()
        if(!TextUtils.isEmpty(pageId)) {
            pageContent.pageTitle = pageTitle
            val paraList = ObjectBox.store
                    .boxFor(Para::class.java)
                    .query()
                    .equal(Para_.pageId, pageId)
                    .build()
                    .find()
            if(paraList != null && !paraList.isEmpty()) {
                var count = 0
                val sb = StringBuilder()
                for(para in paraList) {
                    count += 1
                    sb.append("\n    $count -> ${para.paraTitle}")
                }
                pageContent.paraContent = sb.toString()
            }
        }
        return pageContent
    }

    override fun onItemClick(pos: Int) {

    }

    override fun onItemLongClickListener(pos: Int, view: View) {
    }

    override fun onButtonClickOnItem(identifier: Int, pos: Int) = Unit

    private fun initializeEditorTools() {
        binding.root.findViewById<Button>(R.id.action_h1).setOnClickListener {
            getEditor().updateTextStyle(
                EditorTextStyle.H1
            )
        }
        binding.root.findViewById<Button>(R.id.action_h2).setOnClickListener {
            getEditor().updateTextStyle(
                EditorTextStyle.H2
            )
        }
        binding.root.findViewById<Button>(R.id.action_h3).setOnClickListener {
            getEditor().updateTextStyle(
                EditorTextStyle.H3
            )
        }
        binding.root.findViewById<AppCompatImageButton>(R.id.action_bold).setOnClickListener {
            getEditor().updateTextStyle(
                EditorTextStyle.BOLD
            )
        }
        binding.root.findViewById<AppCompatImageButton>(R.id.action_Italic).setOnClickListener {
            getEditor().updateTextStyle(
                EditorTextStyle.ITALIC
            )
        }
        binding.root.findViewById<AppCompatImageButton>(R.id.action_indent).setOnClickListener {
            getEditor().updateTextStyle(
                EditorTextStyle.INDENT
            )
        }
        binding.root.findViewById<AppCompatImageButton>(R.id.action_outdent).setOnClickListener {
            getEditor().updateTextStyle(
                EditorTextStyle.OUTDENT
            )
        }
        binding.root.findViewById<AppCompatImageButton>(R.id.action_bulleted).setOnClickListener {
            getEditor().insertList(
                false
            )
        }
        binding.root.findViewById<AppCompatImageButton>(R.id.action_color).setOnClickListener {
            getEditor().updateTextColor(
                "#FF3333"
            )
        }
        binding.root.findViewById<AppCompatImageButton>(R.id.action_unordered_numbered).setOnClickListener {
            getEditor().insertList(
                true
            )
        }
        binding.root.findViewById<AppCompatImageButton>(R.id.action_hr).setOnClickListener { getEditor().insertDivider() }
        binding.root.findViewById<AppCompatImageButton>(R.id.action_insert_image).setOnClickListener { getEditor().openImagePicker() }
        binding.root.findViewById<AppCompatImageButton>(R.id.action_insert_link).setOnClickListener { getEditor().insertLink() }
        binding.root.findViewById<AppCompatImageButton>(R.id.action_erase).setOnClickListener { getEditor().clearAllContents() }
        binding.root.findViewById<AppCompatImageButton>(R.id.action_blockquote).setOnClickListener {
            getEditor().updateTextStyle(
                EditorTextStyle.BLOCKQUOTE
            )
        }

//        editor.render()
    }

    private fun getEditor() : Editor {
        return if(binding.sessionSummary.hasFocus())
            binding.sessionSummary
        else if(binding.planning.hasFocus())
            binding.planning
        else if(binding.questions.hasFocus())
            binding.questions
        else
            binding.todos
    }

    private fun submitAssessment() {
        val assessment = SessionAssessment()
        if(validateAssessment()) {
            assessment.sessionId = sessionId
            assessment.sessionSummary = binding.sessionSummary.contentAsHTML
            assessment.nextSessionPlan = binding.planning.contentAsHTML
            assessment.questions = binding.questions.contentAsHTML
            assessment.todos = binding.todos.contentAsHTML
            val id = ObjectBox.store.boxFor(SessionAssessment::class.java).put(assessment)
            if(id > 0L) {
                session.isSessionAssessed = true
                ObjectBox.store.boxFor(Session::class.java).put(session)
                Toast.makeText(context, "Assessment Submitted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Assessment couldn't be submitted ! Please try again later", Toast.LENGTH_SHORT).show()
            }
            NavHostFragment.findNavController(this).popBackStack()
        }
    }

    private fun validateAssessment(): Boolean {
        if(TextUtils.isEmpty(binding.sessionSummary.contentAsHTML) || TextUtils.isEmpty(binding.planning.contentAsHTML) || TextUtils.isEmpty(binding.questions.contentAsHTML) || TextUtils.isEmpty(binding.todos.contentAsHTML)) {
            Toast.makeText(context, "Assessment fields cannot be empty", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    fun getHeadingTypeface(): Map<Int, String>? {
        val typefaceMap: MutableMap<Int, String> = HashMap()
        typefaceMap[Typeface.NORMAL] = "fonts/GreycliffCF-Bold.ttf"
        typefaceMap[Typeface.BOLD] = "fonts/GreycliffCF-Heavy.ttf"
        typefaceMap[Typeface.ITALIC] = "fonts/GreycliffCF-Heavy.ttf"
        typefaceMap[Typeface.BOLD_ITALIC] = "fonts/GreycliffCF-Bold.ttf"
        return typefaceMap
    }

    fun getContentface(): Map<Int, String>? {
        val typefaceMap: MutableMap<Int, String> = HashMap()
        typefaceMap[Typeface.NORMAL] = "fonts/Lato-Medium.ttf"
        typefaceMap[Typeface.BOLD] = "fonts/Lato-Bold.ttf"
        typefaceMap[Typeface.ITALIC] = "fonts/Lato-MediumItalic.ttf"
        typefaceMap[Typeface.BOLD_ITALIC] = "fonts/Lato-BoldItalic.ttf"
        return typefaceMap
    }

}