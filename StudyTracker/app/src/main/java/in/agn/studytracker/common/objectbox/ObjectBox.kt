package `in`.agn.studytracker.common.objectbox

import android.content.Context
import android.util.Log
import com.agn.corea.models.session.MyObjectBox
import com.agn.studytracker.BuildConfig
import io.objectbox.BoxStore
import io.objectbox.android.AndroidObjectBrowser




object ObjectBox {

    lateinit var store : BoxStore

    public fun init(context: Context) {
        store = MyObjectBox.builder()
            .androidContext(context)
            .build()
        if (BuildConfig.DEBUG) {
            val started: Boolean = AndroidObjectBrowser(store).start(context)
            Log.i("ObjectBrowser", "Started: $started")
        }
    }
}