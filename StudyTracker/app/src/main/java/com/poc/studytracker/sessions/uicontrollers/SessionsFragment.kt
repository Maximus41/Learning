package com.poc.studytracker.sessions.uicontrollers

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.poc.corea.models.session.Pages
import com.poc.corea.models.session.Section
import com.poc.corea.models.session.Session
import com.poc.corea.models.session.Session_
import com.poc.corea.models.subjects.Subject
import com.poc.corea.models.subjects.SubjectSection
import com.poc.studytracker.R
import com.poc.studytracker.common.adapter.OnItemClickListener
import com.poc.studytracker.common.objectbox.ObjectBox
import com.poc.studytracker.databinding.FragmentSessionsBinding
import com.poc.studytracker.databinding.FragmentSubjectsBinding
import com.poc.studytracker.sessions.adapters.SessionsAdapter
import com.poc.studytracker.subjects.adapters.SubjectsAdapter
import io.objectbox.rx.RxQuery
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.util.*

class SessionsFragment : Fragment(), OnItemClickListener {

    lateinit var binding: FragmentSessionsBinding
    lateinit var sessionsAdapter: SessionsAdapter
    var subjectId : String? = ""

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate<FragmentSessionsBinding>(inflater,
                R.layout.fragment_sessions, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subjectId = arguments?.getString("subject_id", "")
        binding.sessionsList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.sessionsList.adapter = SessionsAdapter(this)
        loadSessions()
    }

    private fun countSections(pos : Int) {
       if(sessionsAdapter.getItem(pos).sessionStudyTopics.target.sectionsCount == 0)
           openSectionTitleDialog(sessionsAdapter.getItem(pos).sessionId)

    }

    private fun openSectionTitleDialog(sessionId : String) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_create_first_session_section, null)
        val dialog = AlertDialog.Builder(activity).setView(dialogView).create()
        val sectionTitle = dialogView.findViewById<EditText>(R.id.etCreateSessionSection).editableText.toString()
        dialogView.findViewById<Button>(R.id.btnCreateSessionSection).setOnClickListener(View.OnClickListener {
            createFirstSection(sessionId, sectionTitle)
            dialog.dismiss()
        })
        dialog.show()
    }

    private fun createFirstSection(sessionId : String, sectionTitle : String) {
        val section = Section()
        val subjectSection = SubjectSection()
        val pages = Pages()
        section.pages = pages
        section.sectionId = UUID.randomUUID().toString()
        section.
    }

    private fun gotoEditSession() {

    }

    private fun loadSessions() {
        val query = ObjectBox.store.boxFor(Session::class.java).query().build()
        RxQuery.observable(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    sessionsAdapter = binding.sessionsList.adapter as SessionsAdapter
                    sessionsAdapter.setmItems(it)
                    binding.sessionsList.adapter = sessionsAdapter
                })
    }

    override fun onItemClick(pos: Int) {
        val item = sessionsAdapter.getItem(pos)
    }

    override fun onButtonClickOnItem(identifier: Int, pos: Int) {
        when(identifier) {
            SessionsAdapter.EDIT_SESSION_BTN -> {}
        }
    }

}