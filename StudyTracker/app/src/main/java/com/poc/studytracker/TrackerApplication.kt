package com.poc.studytracker

import android.app.Application
import com.poc.studytracker.common.objectbox.ObjectBox

class TrackerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ObjectBox.init(this)
    }
}