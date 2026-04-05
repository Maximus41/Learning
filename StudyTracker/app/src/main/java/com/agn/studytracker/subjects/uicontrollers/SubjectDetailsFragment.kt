package com.agn.studytracker.subjects.uicontrollers

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.agn.corea.constants.PageActionStatus
import com.agn.corea.models.subjects.Para
import com.agn.studytracker.R
import com.agn.studytracker.common.adapter.BaseExpandableListAdapter
import com.agn.studytracker.common.adapter.OnItemClickListener
import com.agn.studytracker.common.adapter.VerticalSpacingItemDecoration
import com.agn.studytracker.common.objectbox.ObjectBox
import com.agn.studytracker.common.uicontrollers.MainActivity
import com.agn.studytracker.databinding.FragmentSubjectDetailsBinding
import com.agn.studytracker.subjects.adapters.SubjectDetailsAdapter
import com.agn.studytracker.subjects.models.SubjectPageModel
import com.agn.studytracker.subjects.models.SubjectSectionModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SubjectDetailsFragment : Fragment(), OnItemClickListener {

    private var subjectId: String? = ""
    lateinit var binding: FragmentSubjectDetailsBinding
    lateinit var subjectDetailsAdapter: SubjectDetailsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_subject_details, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).setTitle("${arguments?.getString("subject_name", "")} Progress")
        subjectId = arguments?.getString("subject_id", "")
        binding.sectionsList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.sectionsList.addItemDecoration(VerticalSpacingItemDecoration(activity?.resources?.getDimensionPixelOffset(R.dimen.dp_1)!!))
        subjectDetailsAdapter = SubjectDetailsAdapter(this)
        binding.sectionsList.adapter = subjectDetailsAdapter
        loadSections()
    }

    private fun loadSections() {
        val db = ObjectBox.get()
        viewLifecycleOwner.lifecycleScope.launch {
            db.sectionDao().observeBySubjectId(subjectId)
                .map { sections ->
                    val listItems = ArrayList<BaseExpandableListAdapter.ExpandableListItem>()
                    for (section in sections) {
                        val sectionModel = SubjectSectionModel().apply {
                            sectionTitle = section.sectionTitle
                            setSectionId(section.sectionId)
                        }
                        listItems.add(sectionModel)
                        var totalCovered = 0.0f
                        val pages = db.pageDao().getBySectionIdSync(section.sectionId)
                        for (page in pages) {
                            val pageModel = SubjectPageModel().apply {
                                setPageId(page.pageId)
                                setSectionId(section.sectionId)
                                pageTitle = page.pageTitle
                            }
                            db.pageCumulativeProgressDao().getByPageIdSync(page.pageId)?.let { p ->
                                pageModel.isRead = p.readStatus == PageActionStatus.COMPLETE
                                pageModel.isNotesTaken = p.notesTakenStatus == PageActionStatus.COMPLETE
                                pageModel.isMemorized = p.memorizedStatus == PageActionStatus.COMPLETE
                                pageModel.reviewCount = p.reviewCount
                                pageModel.practiceCount = p.practiceCount
                                totalCovered += p.totalStoryPointsCovered
                            }
                            listItems.add(pageModel)
                        }
                        if (section.totalStoryPoints > 0)
                            sectionModel.sectionProgressPercent = Math.round((totalCovered / section.totalStoryPoints) * 100)
                    }
                    listItems
                }
                .flowOn(Dispatchers.IO)
                .collect { listItems ->
                    subjectDetailsAdapter.setItems(listItems)
                    binding.sectionsList.adapter = subjectDetailsAdapter
                }
        }
    }

    override fun onItemClick(pos: Int) {
        val pageModel = subjectDetailsAdapter.getItem(pos) as SubjectPageModel
        viewLifecycleOwner.lifecycleScope.launch {
            val paras = withContext(Dispatchers.IO) { ObjectBox.get().paraDao().getByPageId(pageModel.objectId) }
            displayParagraphs(pageModel, paras)
        }
    }

    override fun onItemLongClickListener(pos: Int, view: View) {}

    private fun displayParagraphs(pageModel: SubjectPageModel, paraList: List<Para>) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_display_paragraphs, null)
        val dialog = AlertDialog.Builder(activity).setView(dialogView).create()
        dialog.setCancelable(true)
        dialogView.findViewById<TextView>(R.id.pageTitle).text = pageModel.pageTitle
        dialogView.findViewById<ImageView>(R.id.imgReadStatus).isSelected = pageModel.isRead
        dialogView.findViewById<ImageView>(R.id.imgNotesStatus).isSelected = pageModel.isNotesTaken
        dialogView.findViewById<ImageView>(R.id.imgMemorizeStatus).isSelected = pageModel.isMemorized
        dialogView.findViewById<ImageView>(R.id.imgReviewStatus).isSelected = pageModel.reviewCount > 0
        dialogView.findViewById<ImageView>(R.id.imgPracticeStatus).isSelected = pageModel.practiceCount > 0
        dialogView.findViewById<TextView>(R.id.reviewCount).visibility = if (pageModel.reviewCount > 0) View.VISIBLE else View.GONE
        dialogView.findViewById<TextView>(R.id.practiceCount).visibility = if (pageModel.practiceCount > 0) View.VISIBLE else View.GONE
        dialogView.findViewById<TextView>(R.id.reviewCount).text = pageModel.reviewCount.toString()
        dialogView.findViewById<TextView>(R.id.practiceCount).text = pageModel.practiceCount.toString()
        val paraArr = paraList.toTypedArray()
        dialogView.findViewById<TextView>(R.id.para1).text = if (paraArr.isNotEmpty()) paraArr[0].paraTitle else "N/A"
        dialogView.findViewById<TextView>(R.id.para2).text = if (paraArr.size >= 2) paraArr[1].paraTitle else "N/A"
        dialogView.findViewById<TextView>(R.id.para3).text = if (paraArr.size >= 3) paraArr[2].paraTitle else "N/A"
        dialogView.findViewById<TextView>(R.id.para4).text = if (paraArr.size >= 4) paraArr[3].paraTitle else "N/A"
        dialogView.findViewById<TextView>(R.id.para5).text = if (paraArr.size == 5) paraArr[4].paraTitle else "N/A"
        dialog.show()
    }

    override fun onButtonClickOnItem(identifier: Int, pos: Int) {}
}
