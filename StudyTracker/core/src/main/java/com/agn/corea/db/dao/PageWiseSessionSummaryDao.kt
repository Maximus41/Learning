package com.agn.corea.db.dao

import androidx.room.*
import com.agn.corea.models.session.summary.PageWiseSessionSummary

@Dao
interface PageWiseSessionSummaryDao {

    @Insert
    fun insert(summary: PageWiseSessionSummary): Long
}
