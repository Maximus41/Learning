package com.agn.studytracker.sessions.uicontrollers

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.agn.corea.constants.GlobalConstants
import com.agn.corea.models.session.Session
import com.agn.studytracker.R
import com.agn.studytracker.common.adapter.OnItemClickListener
import com.agn.studytracker.common.adapter.VerticalSpacingItemDecoration
import com.agn.studytracker.common.objectbox.ObjectBox
import com.agn.studytracker.common.uicontrollers.MainActivity
import com.agn.studytracker.databinding.FragmentSessionsBinding
import com.agn.studytracker.sessions.adapters.SessionsAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class SessionsFragment : Fragment(), OnItemClickListener {

    lateinit var binding: FragmentSessionsBinding
    lateinit var sessionsAdapter: SessionsAdapter
    private var subjectId: String? = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate<FragmentSessionsBinding>(inflater, R.layout.fragment_sessions, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).setTitle("Sessions")
        subjectId = arguments?.getString("subject_id", "")
        binding.sessionsList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.sessionsList.addItemDecoration(VerticalSpacingItemDecoration(activity?.resources?.getDimensionPixelOffset(R.dimen.dp_1)!!))
        binding.sessionsList.adapter = SessionsAdapter(this)
        loadSessions(subjectId)
    }

    private fun loadSessions(subjectId: String?) {
        viewLifecycleOwner.lifecycleScope.launch {
            val sessions = withContext(Dispatchers.IO) { ObjectBox.get().sessionDao().getBySubjectId(subjectId) }
            sessionsAdapter = binding.sessionsList.adapter as SessionsAdapter
            sessionsAdapter.setmItems(sessions)
            binding.sessionsList.adapter = sessionsAdapter
            endExpiredSessions()
            createNewSessionWhenLastSessionAssessed()
        }
    }

    private fun createNewSessionWhenLastSessionAssessed() {
        val current = sessionsAdapter.items[sessionsAdapter.items.size - 1]
        if (!current.isSessionActive && current.isSessionAssessed)
            createNewSession(current.sessionSerialNo + 1)
    }

    private fun endExpiredSessions() {
        val current = sessionsAdapter.items[sessionsAdapter.items.size - 1]
        if (current.expiresOn < System.currentTimeMillis() && current.isSessionActive) {
            current.hasSessionExpired = true
            endCurrentSession(current)
        }
    }

    private fun endCurrentSession(current: Session) {
        current.endedOn = System.currentTimeMillis()
        current.hasSessionEnded = true
        current.isSessionActive = false
        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) { ObjectBox.get().sessionDao().update(current) }
            loadSessions(subjectId)
        }
    }

    private fun createNewSession(slno: Int) {
        val sectionTitleArray = resources.getStringArray(R.array.session_title)
        if (slno <= 0 || slno > sectionTitleArray.size) return
        val session = Session().apply {
            this.subjectId = subjectId
            createdOn = System.currentTimeMillis()
            sessionSerialNo = slno
            sessionTitle = sectionTitleArray[slno - 1]
        }
        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) { ObjectBox.get().sessionDao().insert(session) }
            Toast.makeText(context, "New Session Created", Toast.LENGTH_SHORT).show()
            loadSessions(subjectId)
        }
    }

    override fun onItemClick(pos: Int) {
        val bundle = Bundle().apply { putString("session_id", sessionsAdapter.getItem(pos).sessionId) }
        NavHostFragment.findNavController(this).navigate(R.id.updateSessionFragment, bundle)
    }

    override fun onItemLongClickListener(pos: Int, view: View) {}

    private fun stopSessionDialog(current: Session) {
        AlertDialog.Builder(context)
            .setMessage("Are you sure you want to stop the session?")
            .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, _ ->
                endCurrentSession(current)
                dialog?.dismiss()
            })
            .setNegativeButton("No", DialogInterface.OnClickListener { dialog, _ -> dialog.dismiss() })
            .show()
    }

    override fun onButtonClickOnItem(identifier: Int, pos: Int) {
        val db = ObjectBox.get()
        when (identifier) {
            SessionsAdapter.EDIT_SESSION_BTN -> {
                val bundle = Bundle().apply { putString("session_id", sessionsAdapter.getItem(pos).sessionId) }
                NavHostFragment.findNavController(this).navigate(R.id.editSessionFragment, bundle)
            }
            SessionsAdapter.START_SESSION_BTN -> {
                val session = sessionsAdapter.getItem(pos)
                viewLifecycleOwner.lifecycleScope.launch {
                    val topicsCount = withContext(Dispatchers.IO) { db.sessionTopicDao().countBySessionId(session.sessionId) }
                    if (topicsCount <= 0) {
                        Toast.makeText(context, "Topics not defined", Toast.LENGTH_LONG).show()
                        return@launch
                    }
                    val topicsList = withContext(Dispatchers.IO) { db.sessionTopicDao().getBySessionIdSync(session.sessionId) }
                    var totalStoryPoints = 0.0f
                    for (topic in topicsList) totalStoryPoints += topic.topicStoryPoints
                    val days = (totalStoryPoints / GlobalConstants.DAILY_STUDY_HOURS_IN_STORY_POINTS).roundToInt()
                    session.startedOn = System.currentTimeMillis()
                    session.isSessionActive = true
                    session.expiresOn = session.startedOn + days * 24L * 60 * 60 * 1000
                    withContext(Dispatchers.IO) { db.sessionDao().update(session) }
                    Toast.makeText(context, "This session has started", Toast.LENGTH_SHORT).show()
                    loadSessions(subjectId)
                }
            }
            SessionsAdapter.STOP_SESSION_BTN -> stopSessionDialog(sessionsAdapter.getItem(pos))
            SessionsAdapter.ASSESS_SESSION_BTN -> {
                val bundle = Bundle().apply { putString("session_id", sessionsAdapter.getItem(pos).sessionId) }
                NavHostFragment.findNavController(this).navigate(R.id.assessSessionFragment, bundle)
            }
        }
    }
}
