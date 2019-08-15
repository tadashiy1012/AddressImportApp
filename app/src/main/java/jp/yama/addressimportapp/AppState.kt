package jp.yama.addressimportapp

import android.content.Context
import androidx.lifecycle.MutableLiveData
import java.lang.RuntimeException

class AppState private constructor(ctx: Context) {

    val payload = MutableLiveData<Pair<AppKeys, Any>>()

    companion object {
        private var _instance: AppState? = null
        fun onCreateApplication(appCtx: Context) {
            _instance = AppState(appCtx)
        }
        val instance: AppState
            get() {
                _instance?.let {
                    return it
                } ?: run {
                    throw RuntimeException("AppState should be initialized!")
                }
            }
    }

}