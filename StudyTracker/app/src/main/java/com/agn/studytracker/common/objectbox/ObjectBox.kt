package com.agn.studytracker.common.objectbox

import android.content.Context
import com.agn.corea.db.AppDatabase

object ObjectBox {

    private lateinit var db: AppDatabase

    fun init(context: Context) {
        db = AppDatabase.getInstance(context)
    }

    fun get(): AppDatabase = db
}
