package com.agn.studytracker.subjects.uicontrollers

import android.app.AlertDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.agn.corea.models.session.Session
import com.agn.corea.models.session.SessionAssessment
import com.agn.corea.models.session.SessionTopic
import com.agn.corea.models.subjects.*
import com.agn.studytracker.R
import com.agn.studytracker.common.adapter.OnItemClickListener
import com.agn.studytracker.common.adapter.VerticalSpacingItemDecoration
import com.agn.studytracker.common.objectbox.ObjectBox
import com.agn.studytracker.common.uicontrollers.MainActivity
import com.agn.studytracker.databinding.FragmentSubjectsBinding
import com.agn.studytracker.subjects.adapters.SubjectsAdapter
import com.agn.studytracker.subjects.models.SubjectUiModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers


class SubjectsFragment : Fragment(), OnItemClickListener {

    lateinit var binding: FragmentSubjectsBinding
    lateinit var subjectsAdapter: SubjectsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate<FragmentSubjectsBinding>(
            inflater,
            R.layout.fragment_subjects, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.subjectsList.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.subjectsList.addItemDecoration(
            VerticalSpacingItemDecoration(
                activity?.resources?.getDimensionPixelOffset(R.dimen.dp_3)!!
            )
        )
        binding.subjectsList.adapter = SubjectsAdapter(this)
        binding.createSubjectBtn.setOnClickListener {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_create_subject, null)
            val dialog = AlertDialog.Builder(activity).setView(dialogView).create()
            val subject = dialogView.findViewById<EditText>(R.id.etCreateSubject)
            dialogView.findViewById<Button>(R.id.btnCreate).setOnClickListener {
                createSubject(subject.editableText.toString())
                dialog.dismiss()
            }
            dialog.show()
        }
        loadSubjects()
    }

    override fun onStart() {
        super.onStart()
        val myactivity = activity as MainActivity
        myactivity.setTitle("Subjects")
    }

    private fun createSubject(subject: String) {
        val sub = Subject()
        sub.subjectTitle = subject
        sub.createdOn = System.currentTimeMillis()
        ObjectBox.get().subjectDao().insert(sub)
        loadSubjects()
    }

    private fun loadSubjects() {
        val db = ObjectBox.get()
        db.subjectDao().getAll()
            .subscribeOn(Schedulers.io())
            .map {
                val itemList = ArrayList<SubjectUiModel>()
                for (subject in it) {
                    val subjectUiModel = SubjectUiModel()
                    subjectUiModel.noOfSessions = db.sessionDao().countStartedBySubjectId(subject.subjectId)
                    subjectUiModel.isLastSessionActive = db.sessionDao().getFirstActiveBySubjectIdSync(subject.subjectId) != null
                    subjectUiModel.subject = subject
                    itemList.add(subjectUiModel)
                }
                return@map itemList
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(Consumer {
                subjectsAdapter = binding.subjectsList.adapter as SubjectsAdapter
                subjectsAdapter.setmItems(it)
                binding.subjectsList.adapter = subjectsAdapter
            })
    }

    private fun countSessions(pos: Int) {
        val item: Subject = subjectsAdapter.getItem(pos)
        ObjectBox.get().sessionDao().getBySubjectId(item.subjectId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(Consumer {
                if (it == null || it.isEmpty())
                    createFirstSession(item.subjectId)
                gotoSession(item.subjectId)
            })
    }

    private fun createFirstSession(subjectId: String): Long {
        val session = Session()
        session.subjectId = subjectId
        session.createdOn = System.currentTimeMillis()
        session.sessionSerialNo = 1
        session.sessionTitle = "First Session"
        return ObjectBox.get().sessionDao().insert(session)
    }

    private fun gotoSession(subjectId: String) {
        val bundle = Bundle()
        bundle.putString("subject_id", subjectId)
        NavHostFragment.findNavController(this).navigate(R.id.sessionsFragment, bundle)
    }

    override fun onItemClick(pos: Int) {
        val bundle = Bundle()
        bundle.putString("subject_id", subjectsAdapter.getItem(pos).subjectId)
        bundle.putString("subject_name", subjectsAdapter.getItem(pos).subjectTitle)
        NavHostFragment.findNavController(this).navigate(R.id.subjectDetailsFragment, bundle)
    }

    override fun onItemLongClickListener(pos: Int, view: View?) {
        val popUpMenu = PopupMenu(context, view)
        popUpMenu.inflate(R.menu.item_delete_menu)
        popUpMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.deleteItem -> {
                    Thread {
                        val db = ObjectBox.get()
                        val subjectId = subjectsAdapter.getItem(pos).subjectId
                        val sessionList = db.sessionDao().getBySubjectIdSync(subjectId)
                        val sectionList = db.sectionDao().getBySubjectIdSync(subjectId)
                        val topList = ArrayList<SessionTopic>()
                        val pageList = ArrayList<Page>()
                        val progressList = ArrayList<PageCumulativeProgress>()
                        val paraList = ArrayList<Para>()
                        val assessmentList = ArrayList<SessionAssessment>()

                        if (sessionList.isNotEmpty()) {
                            for (session in sessionList) {
                                val topicList = db.sessionTopicDao().getBySessionIdSync(session.sessionId)
                                if (topicList.isNotEmpty()) {
                                    for (topic in topicList) {
                                        val firstPage = db.pageDao().getByPageIdSync(topic.firstPageId)
                                        if (firstPage != null) {
                                            pageList.add(firstPage)
                                            paraList.addAll(db.paraDao().getByPageIdSync(firstPage.pageId))
                                            db.pageCumulativeProgressDao().getByPageIdSync(firstPage.pageId)?.let { progressList.add(it) }
                                        }
                                        if (!TextUtils.isEmpty(topic.secondPageId)) {
                                            val secondPage = db.pageDao().getByPageIdSync(topic.secondPageId)
                                            if (secondPage != null) {
                                                pageList.add(secondPage)
                                                paraList.addAll(db.paraDao().getByPageIdSync(secondPage.pageId))
                                                db.pageCumulativeProgressDao().getByPageIdSync(secondPage.pageId)?.let { progressList.add(it) }
                                            }
                                        }
                                    }
                                    topList.addAll(topicList)
                                }
                                if (session.isSessionAssessed) {
                                    db.sessionAssessmentDao().getBySessionIdSync(session.sessionId)?.let { assessmentList.add(it) }
                                }
                            }
                        }

                        db.subjectDao().delete(subjectsAdapter.getItem(pos))
                        db.sectionDao().deleteList(sectionList)
                        db.sessionDao().deleteList(sessionList)
                        db.sessionTopicDao().deleteList(topList)
                        db.pageDao().deleteList(pageList)
                        db.pageCumulativeProgressDao().deleteList(progressList)
                        db.paraDao().deleteList(paraList)
                        db.sessionAssessmentDao().deleteList(assessmentList)

                        requireActivity().runOnUiThread { loadSubjects() }
                    }.start()
                    return@setOnMenuItemClickListener true
                }
                else -> return@setOnMenuItemClickListener false
            }
        }
        popUpMenu.show()
    }

    override fun onButtonClickOnItem(identifier: Int, pos: Int) {
        when (identifier) {
            SubjectsAdapter.GOTO_SESSION_BTN -> countSessions(pos)
            SubjectsAdapter.GOTO_SUMMARY_BTN -> { }
        }
    }
}
