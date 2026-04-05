package com.agn.corea.db.dao

import androidx.room.*
import com.agn.corea.models.session.summary.SectionWiseSessionSummary

@Dao
interface SectionWiseSessionSummaryDao {

    @Insert
    fun insert(summary: SectionWiseSessionSummary): Long
}
