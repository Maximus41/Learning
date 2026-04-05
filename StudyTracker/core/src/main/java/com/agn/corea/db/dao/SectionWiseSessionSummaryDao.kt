package com.agn.corea.db.dao

import androidx.room.*
import com.agn.corea.models.session.summary.SectionWiseSessionSummary

@Dao
interface SectionWiseSessionSummaryDao {

    @Insert
    suspend fun insert(summary: SectionWiseSessionSummary): Long
}
