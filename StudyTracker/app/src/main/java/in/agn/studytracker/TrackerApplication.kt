package `in`.agn.studytracker

import android.app.Application
import `in`.agn.studytracker.common.objectbox.ObjectBox

class TrackerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ObjectBox.init(this)
    }
}