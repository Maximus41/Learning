package com.poc.studytracker.subjects.uicontrollers

import android.app.AlertDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.poc.corea.models.session.*
import com.poc.corea.models.subjects.*
import com.poc.studytracker.R
import com.poc.studytracker.common.adapter.OnItemClickListener
import com.poc.studytracker.common.adapter.VerticalSpacingItemDecoration
import com.poc.studytracker.common.objectbox.ObjectBox
import com.poc.studytracker.common.uicontrollers.MainActivity
import com.poc.studytracker.databinding.FragmentSubjectsBinding
import com.poc.studytracker.sessions.models.TopicSectionModel
import com.poc.studytracker.subjects.adapters.SubjectsAdapter
import io.objectbox.rx.RxQuery
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
                activity?.resources?.getDimensionPixelOffset(
                    R.dimen.dp_3
                )!!
            )
        )
        binding.subjectsList.adapter = SubjectsAdapter(this)
        binding.createSubjectBtn.setOnClickListener(View.OnClickListener {
            val dialogView = LayoutInflater.from(context).inflate(
                R.layout.dialog_create_subject,
                null
            )
            val dialog = AlertDialog.Builder(activity).setView(dialogView).create()
            val subject = dialogView.findViewById<EditText>(R.id.etCreateSubject)
            dialogView.findViewById<Button>(R.id.btnCreate)
                .setOnClickListener(View.OnClickListener {
                    createSubject(subject.editableText.toString())
                    dialog.dismiss()
                })
            dialog.show()
        })

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
        ObjectBox.store.boxFor(Subject::class.java).put(sub)
        loadSubjects()
    }

    private fun loadSubjects() {
        val disposable = RxQuery.single(ObjectBox.store.boxFor(Subject::class.java).query().build())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    subjectsAdapter = binding.subjectsList.adapter as SubjectsAdapter
                    subjectsAdapter.setmItems(it)
                    binding.subjectsList.adapter = subjectsAdapter
                })
    }

    private fun countSessions(pos: Int) {
        val item : Subject = subjectsAdapter.getItem(pos)
        val disposable = RxQuery.single(
            ObjectBox.store.boxFor(Session::class.java).query().equal(
                Session_.subjectId,
                item.subjectId
            ).build()
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    if (it == null || it.isEmpty())
                        createFirstSession(item.subjectId)
                    gotoSession(item.subjectId)
                })
    }

    private fun createFirstSession(subjectId: String) : Long {
        val session = Session()
        session.subjectId = subjectId
        session.createdOn = System.currentTimeMillis()
        session.sessionSerialNo = 1
        session.sessionTitle = "First Session"
       return ObjectBox.store.boxFor(Session::class.java).put(session);
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
            when(it.itemId) {
                R.id.deleteItem -> {
                    Thread(Runnable {
                        val sessionBox  = ObjectBox.store.boxFor(Session::class.java)
                        val sectionBox = ObjectBox.store.boxFor(Section::class.java)
                        val pageBox = ObjectBox.store.boxFor(Page::class.java)
                        val paraBox = ObjectBox.store.boxFor(Para::class.java)
                        val sessionTopicBox = ObjectBox.store.boxFor(SessionTopic::class.java)
                        val progressBox = ObjectBox.store.boxFor(PageCumulativeProgress::class.java)
                        val assessmentBox = ObjectBox.store.boxFor(SessionAssessment::class.java)
                        val sessionList = sessionBox.query().equal(Session_.subjectId, subjectsAdapter.getItem(pos).subjectId).build().find()
                        val sectionList = sectionBox.query().equal(Section_.subjectId, subjectsAdapter.getItem(pos).subjectId).build().find()
                        val sessList = ArrayList<Session>()
                        val topList = ArrayList<SessionTopic>()
                        val pageList = ArrayList<Page>()
                        val progressList = ArrayList<PageCumulativeProgress>()
                        val paraList = ArrayList<Para>()
                        val assessmentList = ArrayList<SessionAssessment>()

                        if(!sessionList.isEmpty()) {
                            for (session in sessionList) {
                                val topicList = sessionTopicBox.query()
                                    .equal(SessionTopic_.sessionId, session.sessionId).build().find()
                                if(!topicList.isEmpty()) {
                                    for (topic in topicList) {
                                        val firstPage = pageBox.query().equal(Page_.pageId, topic.firstPageId).build().findFirst()
                                        val paras = paraBox.query().equal(Para_.pageId, firstPage!!.pageId).build().find()
                                        val progress = progressBox.query().equal(PageCumulativeProgress_.pageId, firstPage.pageId).build().findFirst()
                                        pageList.add(firstPage)
                                        paraList.addAll(paras)
                                        progressList.add(progress!!)
                                        if (!TextUtils.isEmpty(topic.secondPageId)) {
                                            val secondPage = pageBox.query().equal(Page_.pageId, topic.secondPageId).build().findFirst()
                                            val paras2 = paraBox.query().equal(Para_.pageId, secondPage!!.pageId).build().find()
                                            val progress2 = progressBox.query().equal(PageCumulativeProgress_.pageId, secondPage.pageId).build().findFirst()
                                            pageList.add(secondPage)
                                            paraList.addAll(paras2)
                                            progressList.add(progress2!!)
                                        }
                                    }
                                    topList.addAll(topicList)
                                }
                                if(session.isSessionAssessed) {
                                    val sessionAssessment = assessmentBox.query().equal(SessionAssessment_.sessionId, session.sessionId).build().findFirst()
                                    assessmentList.add(sessionAssessment!!)
                                }
                            }
                            sessList.addAll(sessionList)
                        }
                        ObjectBox.store.boxFor(Subject::class.java).remove(subjectsAdapter.getItem(pos))
                        sectionBox.remove(sectionList)
                        sessionBox.remove(sessList)
                        sessionTopicBox.remove(topList)
                        pageBox.remove(pageList)
                        progressBox.remove(progressList)
                        paraBox.remove(paraList)
                        assessmentBox.remove(assessmentList)
                        requireActivity().runOnUiThread(Runnable {
                            loadSubjects()
                        })

                    }).start()

                    return@setOnMenuItemClickListener true
                }
                else -> return@setOnMenuItemClickListener false
            }
        }
        popUpMenu.show()
    }

    override fun onButtonClickOnItem(identifier: Int, pos: Int) {
        when(identifier) {
            SubjectsAdapter.GOTO_SESSION_BTN -> {
                countSessions(pos)
            }
            SubjectsAdapter.GOTO_SUMMARY_BTN -> {
            }
        }
    }

}