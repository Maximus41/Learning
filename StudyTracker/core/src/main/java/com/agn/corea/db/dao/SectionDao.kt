package com.agn.corea.db.dao

import androidx.room.*
import com.agn.corea.models.subjects.Section
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface SectionDao {

    @Query("SELECT * FROM sections WHERE subjectId = :subjectId")
    fun getBySubjectId(subjectId: String?): Single<List<Section>>

    @Query("SELECT * FROM sections WHERE subjectId = :subjectId")
    fun observeBySubjectId(subjectId: String?): Flowable<List<Section>>

    @Query("SELECT * FROM sections WHERE subjectId = :subjectId")
    fun getBySubjectIdSync(subjectId: String): List<Section>

    @Query("SELECT * FROM sections WHERE sectionId = :sectionId LIMIT 1")
    fun getBySectionIdSync(sectionId: String): Section?

    @Query("SELECT * FROM sections WHERE sectionTitle = :title AND subjectId = :subjectId LIMIT 1")
    fun getByTitleAndSubjectSync(title: String, subjectId: String): Section?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(section: Section): Long

    @Update
    fun update(section: Section)

    @Delete
    fun deleteList(sections: List<Section>)
}
