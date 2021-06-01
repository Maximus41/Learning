package com.poc.studytracker.sessions.uicontrollers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.poc.corea.constants.GlobalConstants
import com.poc.corea.models.session.Session
import com.poc.corea.models.session.SessionTopic
import com.poc.corea.models.session.SessionTopic_
import com.poc.corea.models.session.Session_
import com.poc.studytracker.R
import com.poc.studytracker.common.adapter.OnItemClickListener
import com.poc.studytracker.common.adapter.VerticalSpacingItemDecoration
import com.poc.studytracker.common.objectbox.ObjectBox
import com.poc.studytracker.common.uicontrollers.MainActivity
import com.poc.studytracker.databinding.FragmentSessionsBinding
import com.poc.studytracker.sessions.adapters.SessionsAdapter
import io.objectbox.rx.RxQuery
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlin.math.roundToInt

class SessionsFragment : Fragment(), OnItemClickListener {

    lateinit var binding: FragmentSessionsBinding
    lateinit var sessionsAdapter: SessionsAdapter
    private var subjectId : String? = ""

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
        val myactivity = activity as MainActivity
        myactivity.setTitle("Sessions")
        subjectId = arguments?.getString("subject_id", "")
        binding.sessionsList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.sessionsList.addItemDecoration(VerticalSpacingItemDecoration(activity?.resources?.getDimensionPixelOffset(R.dimen.dp_1)!!))
        binding.sessionsList.adapter = SessionsAdapter(this)
        loadSessions(subjectId)
    }

    private fun loadSessions(subjectId: String?) {
        RxQuery.single(ObjectBox.store.boxFor(Session::class.java).query().equal(Session_.subjectId, subjectId).build())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    sessionsAdapter = binding.sessionsList.adapter as SessionsAdapter
                    sessionsAdapter.setmItems(it)
                    binding.sessionsList.adapter = sessionsAdapter
                    endExpiredSessions()
                    createNewSessionWhenLastSessionAssessed()
                })
    }

    private fun createNewSessionWhenLastSessionAssessed() {
        val currentSession = sessionsAdapter.items[sessionsAdapter.items.size - 1]
        if(!currentSession.isSessionActive && currentSession.isSessionAssessed)
            createNewSession(currentSession.sessionSerialNo + 1)
    }

    private fun endExpiredSessions() {
        val currentSession = sessionsAdapter.items[sessionsAdapter.items.size - 1]
        if(currentSession.expiresOn < System.currentTimeMillis() && currentSession.isSessionActive) {
            currentSession.hasSessionExpired = true
            endCurrentSession(currentSession)
        }
    }

    private fun endCurrentSession(currentSession : Session) {
        currentSession.endedOn = System.currentTimeMillis()
        currentSession.hasSessionEnded = true
        currentSession.isSessionActive = false
        ObjectBox.store.boxFor(Session::class.java).put(currentSession)
        loadSessions(subjectId)
//        createNewSession(currentSession.sessionSerialNo + 1)
    }

    private fun createNewSession(slno : Int) {
        val session = Session()
        session.subjectId = subjectId
        session.createdOn = System.currentTimeMillis()
        session.sessionSerialNo = slno
        val sectionTitleArray = resources.getStringArray(R.array.session_title)
        if(slno > 0 && slno <= sectionTitleArray.size) {
            session.sessionTitle = resources.getStringArray(R.array.session_title)[slno - 1]

            //Todo:Add incomplete session topic from previous sessions

            ObjectBox.store.boxFor(Session::class.java).put(session);
            loadSessions(subjectId)
        }
    }

    override fun onItemClick(pos: Int) {
        val bundle = Bundle()
        bundle.putString("session_id", sessionsAdapter.getItem(pos).sessionId)
        NavHostFragment.findNavController(this).navigate(R.id.updateSessionFragment, bundle)
    }

    override fun onButtonClickOnItem(identifier: Int, pos: Int) {
        when(identifier) {
            SessionsAdapter.EDIT_SESSION_BTN -> {
                val bundle = Bundle()
                bundle.putString("session_id", sessionsAdapter.getItem(pos).sessionId)
                NavHostFragment.findNavController(this).navigate(R.id.editSessionFragment, bundle)
            }
            SessionsAdapter.START_SESSION_BTN -> {
                val session = sessionsAdapter.getItem(pos)
                val sessionTopicsCount = ObjectBox.store.boxFor(SessionTopic::class.java)
                        .query()
                        .equal(SessionTopic_.sessionId, session.sessionId)
                        .build()
                        .count()
                if(sessionTopicsCount <= 0) {
                    Toast.makeText(context, "Topics not defined", Toast.LENGTH_LONG).show()
                    return
                }
                session.startedOn = System.currentTimeMillis()
                session.isSessionActive = true
                //Calculate Session Expiration Date
                val topicsList = ObjectBox.store.boxFor(SessionTopic::class.java).query().equal(SessionTopic_.sessionId, session.sessionId).build().find()
                var totalStoryPoints = 0.0f
                for(topic in topicsList)
                    totalStoryPoints += topic.topicStoryPoints
                val storyPointsTranslatedIntoDays = (totalStoryPoints / GlobalConstants.DAILY_STUDY_HOURS_IN_STORY_POINTS).roundToInt()
                val totalDaysInMillis = storyPointsTranslatedIntoDays * 24 * 60 * 60 * 1000
                session.expiresOn = session.startedOn + totalDaysInMillis

                ObjectBox.store.boxFor(Session::class.java).put(session)
                loadSessions(subjectId)
            }
            SessionsAdapter.STOP_SESSION_BTN -> {
                endCurrentSession(sessionsAdapter.getItem(pos))
            }
            SessionsAdapter.ASSESS_SESSION_BTN -> {
                val bundle = Bundle()
                bundle.putString("session_id", sessionsAdapter.getItem(pos).sessionId)
                NavHostFragment.findNavController(this).navigate(R.id.assessSessionFragment, bundle)
            }
        }
    }

}