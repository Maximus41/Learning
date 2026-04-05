package com.agn.corea.db.dao

import androidx.room.*
import com.agn.corea.models.subjects.Para
import io.reactivex.Single

@Dao
interface ParaDao {

    @Query("SELECT * FROM paras WHERE pageId = :pageId")
    fun getByPageId(pageId: String): Single<List<Para>>

    @Query("SELECT * FROM paras WHERE pageId = :pageId")
    fun getByPageIdSync(pageId: String): List<Para>

    @Query("SELECT COUNT(*) FROM paras WHERE pageId = :pageId")
    fun countByPageId(pageId: String): Long

    @Insert
    fun insertList(paras: List<Para>)

    @Delete
    fun deleteList(paras: List<Para>)
}
