package com.agn.corea.db.dao

import androidx.room.*
import com.agn.corea.models.subjects.Para

@Dao
interface ParaDao {

    @Query("SELECT * FROM paras WHERE pageId = :pageId")
    suspend fun getByPageId(pageId: String): List<Para>

    @Query("SELECT * FROM paras WHERE pageId = :pageId")
    fun getByPageIdSync(pageId: String): List<Para>

    @Query("SELECT COUNT(*) FROM paras WHERE pageId = :pageId")
    fun countByPageId(pageId: String): Long

    @Insert
    suspend fun insertList(paras: List<Para>)

    @Delete
    suspend fun deleteList(paras: List<Para>)
}
