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
import com.poc.studytracker.R
import com.poc.studytracker.common.adapter.OnItemClickListener
import com.poc.studytracker.databinding.FragmentSessionsBinding
import com.poc.studytracker.sessions.adapters.SessionsAdapter
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

    }

    private fun gotoEditSession() {

    }

    private fun loadSessions() {

    }

    override fun onItemClick(pos: Int) {
    }

    override fun onButtonClickOnItem(identifier: Int, pos: Int) {
        when(identifier) {
            SessionsAdapter.EDIT_SESSION_BTN -> {}
        }
    }

}