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
import com.poc.studytracker.R
import com.poc.studytracker.common.adapter.OnItemClickListener
import com.poc.studytracker.databinding.FragmentSubjectsBinding
import com.poc.studytracker.subjects.adapters.SubjectsAdapter


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
    }

    private fun loadSubjects() {

    }

    private fun countSessions(subjectId : String) {

    }

    private fun createFirstSession(subjectId: String) : Long {
       return 0L;
    }

    private fun gotoSession(subjectId : String) {
        val bundle = Bundle()
        bundle.putString("subject_id", subjectId)
        NavHostFragment.findNavController(this).navigate(R.id.sessionsFragment, bundle)
    }


    override fun onItemClick(pos: Int) {
    }

    override fun onButtonClickOnItem(identifier: Int, pos : Int) {
        when(identifier) {
            SubjectsAdapter.GOTO_SESSION_BTN -> {
            }
            SubjectsAdapter.GOTO_SUMMARY_BTN -> {}
        }
    }

}