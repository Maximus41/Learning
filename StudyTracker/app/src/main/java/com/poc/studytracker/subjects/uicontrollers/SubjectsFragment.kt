package com.poc.studytracker.subjects.uicontrollers

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.poc.corea.models.session.Section
import com.poc.corea.models.session.Session
import com.poc.corea.models.session.Session_
import com.poc.corea.models.subjects.Subject
import com.poc.corea.models.subjects.SubjectSection
import com.poc.studytracker.R
import com.poc.studytracker.common.adapter.OnItemClickListener
import com.poc.studytracker.common.objectbox.ObjectBox
import com.poc.studytracker.databinding.FragmentSubjectsBinding
import com.poc.studytracker.subjects.adapters.SubjectsAdapter
import io.objectbox.rx.RxQuery
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.functions.Predicate
import io.reactivex.schedulers.Schedulers
import java.util.*


class SubjectsFragment : Fragment(), OnItemClickListener {

    lateinit var binding: FragmentSubjectsBinding
    lateinit var subjectsAdapter: SubjectsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate<FragmentSubjectsBinding>(inflater,
            R.layout.fragment_subjects, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.subjectsList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.subjectsList.adapter = SubjectsAdapter(this)
        binding.createSubjectBtn.setOnClickListener(View.OnClickListener {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_create_subject, null)
            val dialog = AlertDialog.Builder(activity).setView(dialogView).create()
            val subject = dialogView.findViewById<EditText>(R.id.etCreateSubject).editableText.toString()
            dialogView.findViewById<Button>(R.id.btnCreate).setOnClickListener(View.OnClickListener {
                createSubject(subject)
                dialog.dismiss()
            })
            dialog.show()
        })

        loadSubjects()
    }

    private fun createSubject(subject: String) {
        var sub = Subject()
        sub.subjectId = UUID.randomUUID().toString()
        sub.subjectTitle = subject
        ObjectBox.store.boxFor(Subject::class.java).put(sub)
        loadSubjects()
    }

    private fun loadSubjects() {
        val query = ObjectBox.store.boxFor(Subject::class.java).query().build()
        RxQuery.observable(query)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(Consumer {
                subjectsAdapter = binding.subjectsList.adapter as SubjectsAdapter
                subjectsAdapter.setmItems(it)
                binding.subjectsList.adapter = subjectsAdapter
            })
    }

    private fun countSessions(subjectId : String) {
        val query = ObjectBox.store.boxFor(Session::class.java).query().equal(Session_.subjectId, subjectId).build()
        RxQuery.observable(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    if(it.isEmpty())
                        createFirstSession(subjectId)
                    gotoSession(subjectId)
                })
    }

    private fun createFirstSession(subjectId: String) : Long {
        val session = Session()
        session.sessionId = UUID.randomUUID().toString()
        session.subjectId = subjectId
        session.sessionCreatedOn = System.currentTimeMillis()
        session.sessionTitle = "First Session"
        session.sessionOrder = 1
        session.sessionStoryPoints = 0
        return ObjectBox.store.boxFor(Session::class.java).put(session)
    }

    private fun gotoSession(subjectId : String) {
        val bundle = Bundle()
        bundle.putString("subject_id", subjectId)
        NavHostFragment.findNavController(this).navigate(R.id.sessionsFragment, bundle)
    }


    override fun onItemClick(pos: Int) {
        val item = subjectsAdapter.getItem(pos)
    }

    override fun onButtonClickOnItem(identifier: Int, pos : Int) {
        when(identifier) {
            SubjectsAdapter.GOTO_SESSION_BTN -> {
                countSessions(subjectsAdapter.getItem(pos).subjectId)
            }
            SubjectsAdapter.GOTO_SUMMARY_BTN -> {}
        }
    }

}