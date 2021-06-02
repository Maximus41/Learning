package com.poc.studytracker.sessions.uicontrollers

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
import com.poc.corea.models.session.*
import com.poc.corea.models.subjects.Para
import com.poc.corea.models.subjects.Para_
import com.poc.studytracker.R
import com.poc.studytracker.common.adapter.HorizontalSpacingItemDecoration
import com.poc.studytracker.common.adapter.OnItemClickListener
import com.poc.studytracker.common.objectbox.ObjectBox
import com.poc.studytracker.common.uicontrollers.MainActivity
import com.poc.studytracker.databinding.FragmentAssessSessionBinding
import com.poc.studytracker.sessions.adapters.AssessPageContentListAdapter
import com.poc.studytracker.sessions.models.AssessmentPageContentModel
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

        binding.btnAssess.setOnClickListener(View.OnClickListener {
            submitAssessment()
        })
        val headingTypeface = getHeadingTypeface()
        val contentTypeface = getContentface()
        binding.sessionSummary.headingTypeface = headingTypeface
        binding.sessionSummary.contentTypeface = contentTypeface
        binding.planning.headingTypeface = headingTypeface
        binding.planning.contentTypeface = contentTypeface
        binding.sessionSummary.setNormalTextSize(16)
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
                binding.viewSummary.text = Html.fromHtml(it[0].sessionSummary)
                binding.viewPlanning.text = Html.fromHtml(it[0].nextSessionPlan)
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
                    val pageContent1 = createPageContent(topic.firstPageId, topic.firstPageTitle)
                    val pageContent2 = createPageContent(
                        topic.secondPageId,
                        topic.secondPageTitle
                    )
                    pageContentList.add(pageContent1)
                    pageContentList.add(pageContent2)
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

    override fun onButtonClickOnItem(identifier: Int, pos: Int) = Unit

    private fun initializeEditorTools() {
        binding.root.findViewById<Button>(R.id.action_h1).setOnClickListener(View.OnClickListener {
            getEditor().updateTextStyle(
                EditorTextStyle.H1
            )
        })
        binding.root.findViewById<Button>(R.id.action_h2).setOnClickListener(View.OnClickListener {
            getEditor().updateTextStyle(
                EditorTextStyle.H2
            )
        })
        binding.root.findViewById<Button>(R.id.action_h3).setOnClickListener(View.OnClickListener {
            getEditor().updateTextStyle(
                EditorTextStyle.H3
            )
        })
        binding.root.findViewById<AppCompatImageButton>(R.id.action_bold).setOnClickListener(View.OnClickListener {
            getEditor().updateTextStyle(
                EditorTextStyle.BOLD
            )
        })
        binding.root.findViewById<AppCompatImageButton>(R.id.action_Italic).setOnClickListener(View.OnClickListener {
            getEditor().updateTextStyle(
                EditorTextStyle.ITALIC
            )
        })
        binding.root.findViewById<AppCompatImageButton>(R.id.action_indent).setOnClickListener(View.OnClickListener {
            getEditor().updateTextStyle(
                EditorTextStyle.INDENT
            )
        })
        binding.root.findViewById<AppCompatImageButton>(R.id.action_outdent).setOnClickListener(View.OnClickListener {
            getEditor().updateTextStyle(
                EditorTextStyle.OUTDENT
            )
        })
        binding.root.findViewById<AppCompatImageButton>(R.id.action_bulleted).setOnClickListener(
            View.OnClickListener {
                getEditor().insertList(
                    false
                )
            })
        binding.root.findViewById<AppCompatImageButton>(R.id.action_color).setOnClickListener(View.OnClickListener {
            getEditor().updateTextColor(
                "#FF3333"
            )
        })
        binding.root.findViewById<AppCompatImageButton>(R.id.action_unordered_numbered).setOnClickListener(
            View.OnClickListener {
                getEditor().insertList(
                    true
                )
            })
        binding.root.findViewById<AppCompatImageButton>(R.id.action_hr).setOnClickListener(View.OnClickListener { getEditor().insertDivider() })
        binding.root.findViewById<AppCompatImageButton>(R.id.action_insert_image).setOnClickListener(
            View.OnClickListener { getEditor().openImagePicker() })
        binding.root.findViewById<AppCompatImageButton>(R.id.action_insert_link).setOnClickListener(
            View.OnClickListener { getEditor().insertLink() })
        binding.root.findViewById<AppCompatImageButton>(R.id.action_erase).setOnClickListener(View.OnClickListener { getEditor().clearAllContents() })
        binding.root.findViewById<AppCompatImageButton>(R.id.action_blockquote).setOnClickListener(
            View.OnClickListener {
                getEditor().updateTextStyle(
                    EditorTextStyle.BLOCKQUOTE
                )
            })

//        editor.render()
    }

    private fun getEditor() : Editor {
        return if(binding.sessionSummary.hasFocus())
            binding.sessionSummary
        else
            binding.planning
    }

    private fun submitAssessment() {
        val assessment = SessionAssessment()
        if(validateAssessment()) {
            assessment.sessionId = sessionId
            assessment.sessionSummary = binding.sessionSummary.contentAsHTML
            assessment.nextSessionPlan = binding.planning.contentAsHTML
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
        if(TextUtils.isEmpty(binding.sessionSummary.contentAsHTML) || TextUtils.isEmpty(binding.planning.contentAsHTML)) {
            Toast.makeText(context, "Session summary/planning cannot be empty", Toast.LENGTH_SHORT).show()
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