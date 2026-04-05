package com.agn.corea.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.agn.corea.db.dao.*
import com.agn.corea.models.session.Session
import com.agn.corea.models.session.SessionAssessment
import com.agn.corea.models.session.SessionTopic
import com.agn.corea.models.session.summary.PageWiseSessionSummary
import com.agn.corea.models.session.summary.SectionWiseSessionSummary
import com.agn.corea.models.session.summary.SessionSummary
import com.agn.corea.models.subjects.*

@Database(
    entities = [
        Subject::class,
        Section::class,
        Page::class,
        Para::class,
        PageCumulativeProgress::class,
        Session::class,
        SessionTopic::class,
        SessionAssessment::class,
        SessionSummary::class,
        PageWiseSessionSummary::class,
        SectionWiseSessionSummary::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun subjectDao(): SubjectDao
    abstract fun sectionDao(): SectionDao
    abstract fun pageDao(): PageDao
    abstract fun paraDao(): ParaDao
    abstract fun pageCumulativeProgressDao(): PageCumulativeProgressDao
    abstract fun sessionDao(): SessionDao
    abstract fun sessionTopicDao(): SessionTopicDao
    abstract fun sessionAssessmentDao(): SessionAssessmentDao
    abstract fun sessionSummaryDao(): SessionSummaryDao
    abstract fun pageWiseSessionSummaryDao(): PageWiseSessionSummaryDao
    abstract fun sectionWiseSessionSummaryDao(): SectionWiseSessionSummaryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "study_tracker.db"
                ).build().also { INSTANCE = it }
            }
    }
}
