package com.poc.studytracker.subjects.uicontrollers

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.poc.corea.constants.PageActionStatus
import com.poc.corea.models.subjects.Para
import com.poc.corea.models.subjects.Para_
import com.poc.corea.models.subjects.Section
import com.poc.corea.models.subjects.Section_
import com.poc.studytracker.R
import com.poc.studytracker.common.adapter.BaseExpandableListAdapter
import com.poc.studytracker.common.adapter.OnItemClickListener
import com.poc.studytracker.common.adapter.VerticalSpacingItemDecoration
import com.poc.studytracker.common.objectbox.ObjectBox
import com.poc.studytracker.common.uicontrollers.MainActivity
import com.poc.studytracker.databinding.FragmentSubjectDetailsBinding
import com.poc.studytracker.subjects.adapters.SubjectDetailsAdapter
import com.poc.studytracker.subjects.models.SubjectPageModel
import com.poc.studytracker.subjects.models.SubjectSectionModel
import io.objectbox.rx.RxQuery
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers


class SubjectDetailsFragment : Fragment(), OnItemClickListener {

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
        val myactivity = activity as MainActivity
        myactivity.setTitle("${arguments?.getString("subject_name", "") as String} Progress")
        subjectId = arguments?.getString("subject_id", "")
        binding.sectionsList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.sectionsList.addItemDecoration(VerticalSpacingItemDecoration(activity?.resources?.getDimensionPixelOffset(R.dimen.dp_1)!!))
        subjectDetailsAdapter = SubjectDetailsAdapter(this)
        binding.sectionsList.adapter = subjectDetailsAdapter
        loadSections()
    }

    private fun loadSections() {
        val disposable = RxQuery.observable(ObjectBox.store.boxFor(Section::class.java).query().equal(Section_.subjectId, subjectId).build())
                .subscribeOn(Schedulers.io())
                .map {
                    val listItems = ArrayList<BaseExpandableListAdapter.ExpandableListItem>()
                    for(section in it) {
                        val sectionModel = SubjectSectionModel()
                        sectionModel.sectionTitle = section.sectionTitle
                        sectionModel.setSectionId(section.sectionId)


                        listItems.add(sectionModel)
                        var totalStoryPointsCoveredPerSection = 0.0f
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
                                totalStoryPointsCoveredPerSection += pageProgress.totalStoryPointsCovered
                                listItems.add(pageModel)
                            }
                        }
                        sectionModel.sectionProgressPercent = Math.round((totalStoryPointsCoveredPerSection / section.totalStoryPoints) * 100)
                    }
                    return@map listItems
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    subjectDetailsAdapter.setItems(it)
                    binding.sectionsList.adapter = subjectDetailsAdapter
                })
    }

    override fun onItemClick(pos: Int) {
        val pageModel = subjectDetailsAdapter.getItem(pos) as SubjectPageModel
        RxQuery.single(ObjectBox.store.boxFor(Para::class.java).query().equal(Para_.pageId, pageModel.objectId).build())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    displayParagraphs(pageModel, it)
                })
    }

    override fun onItemLongClickListener(pos: Int, view: View) {

    }

    private fun displayParagraphs(pageModel: SubjectPageModel, paraList: List<Para>) {
        val dialogView = LayoutInflater.from(context).inflate(
                R.layout.dialog_display_paragraphs,
                null
        )
        val dialog = AlertDialog.Builder(activity).setView(dialogView).create()
        dialog.setCancelable(true)
        dialogView.findViewById<TextView>(R.id.pageTitle).text = pageModel.pageTitle
        dialogView.findViewById<ImageView>(R.id.imgReadStatus).isSelected = pageModel.isRead
        dialogView.findViewById<ImageView>(R.id.imgNotesStatus).isSelected = pageModel.isNotesTaken
        dialogView.findViewById<ImageView>(R.id.imgMemorizeStatus).isSelected = pageModel.isMemorized
        dialogView.findViewById<ImageView>(R.id.imgReviewStatus).isSelected = pageModel.reviewCount > 0
        dialogView.findViewById<ImageView>(R.id.imgPracticeStatus).isSelected = pageModel.practiceCount > 0
        dialogView.findViewById<TextView>(R.id.practiceCount).visibility = if (pageModel.practiceCount > 0) View.VISIBLE else View.GONE
        dialogView.findViewById<TextView>(R.id.reviewCount).visibility = if (pageModel.reviewCount > 0) View.VISIBLE else View.GONE
        dialogView.findViewById<TextView>(R.id.reviewCount).text = pageModel.reviewCount.toString()
        dialogView.findViewById<TextView>(R.id.practiceCount).text = pageModel.practiceCount.toString()

        val paraArr = paraList.toTypedArray()
        dialogView.findViewById<TextView>(R.id.para1).text = if(paraArr.isNotEmpty()) paraArr[0].paraTitle else "N/A"
        dialogView.findViewById<TextView>(R.id.para2).text = if(paraArr.size >= 2) paraArr[1].paraTitle else "N/A"
        dialogView.findViewById<TextView>(R.id.para3).text = if(paraArr.size >= 3) paraArr[2].paraTitle else "N/A"
        dialogView.findViewById<TextView>(R.id.para4).text = if(paraArr.size >= 4) paraArr[3].paraTitle else "N/A"
        dialogView.findViewById<TextView>(R.id.para5).text = if(paraArr.size == 5) paraArr[4].paraTitle else "N/A"
        dialog.show()
    }

    override fun onButtonClickOnItem(identifier: Int, pos: Int) {
    }

}