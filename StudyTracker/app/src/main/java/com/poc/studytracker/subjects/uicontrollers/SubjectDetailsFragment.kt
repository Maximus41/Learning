package com.poc.studytracker.subjects.uicontrollers

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.poc.corea.constants.PageActionStatus
import com.poc.corea.models.subjects.Section
import com.poc.corea.models.subjects.Section_
import com.poc.studytracker.R
import com.poc.studytracker.common.adapter.BaseExpandableListAdapter
import com.poc.studytracker.common.objectbox.ObjectBox
import com.poc.studytracker.databinding.FragmentSubjectDetailsBinding
import com.poc.studytracker.sessions.adapters.UpdateSessionAdapter
import com.poc.studytracker.subjects.adapters.SubjectDetailsAdapter
import com.poc.studytracker.subjects.models.SubjectPageModel
import com.poc.studytracker.subjects.models.SubjectSectionModel
import io.objectbox.rx.RxQuery
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers


class SubjectDetailsFragment : Fragment() {

    private var subjectId: String? = ""
    lateinit var binding: FragmentSubjectDetailsBinding
    lateinit var subjectDetailsAdapter : SubjectDetailsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_subject_details, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subjectId = arguments?.getString("subject_id", "")
        binding.sectionsList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        subjectDetailsAdapter = SubjectDetailsAdapter()
        binding.sectionsList.adapter = subjectDetailsAdapter
        loadSections()
    }

    private fun loadSections() {
        RxQuery.observable(ObjectBox.store.boxFor(Section::class.java).query().equal(Section_.subjectId, subjectId).build())
                .subscribeOn(Schedulers.io())
                .map {
                    val listItems = ArrayList<BaseExpandableListAdapter.ExpandableListItem>()
                    for(section in it) {
                        val sectionModel = SubjectSectionModel()
                        sectionModel.sectionTitle = section.sectionTitle
                        sectionModel.setSectionId(section.sectionId)
                        sectionModel.sectionProgressPercent = section.totalStoryPoints

                        listItems.add(sectionModel)

                        if(!section.pages.isEmpty()) {
                            for(page in section.pages) {
                                val pageModel = SubjectPageModel()
                                pageModel.setPageId(page.pageId)
                                pageModel.setSectionId(section.sectionId)
                                pageModel.pageTitle = page.pageTitle
                                val pageProgress = page.progress.target
                                pageModel.isRead = pageProgress.readStatus == PageActionStatus.COMPLETE
                                pageModel.isNotesTaken = pageProgress.notesTakenStatus == PageActionStatus.COMPLETE
                                pageModel.isMemorized = pageProgress.memorizedStatus == PageActionStatus.COMPLETE
                                pageModel.reviewCount = pageProgress.reviewCount
                                pageModel.practiceCount = pageProgress.practiceCount
                                listItems.add(pageModel)
                            }
                        }

                    }
                    return@map listItems
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    subjectDetailsAdapter.setItems(it)
                    binding.sectionsList.adapter =  subjectDetailsAdapter
                })
    }

}