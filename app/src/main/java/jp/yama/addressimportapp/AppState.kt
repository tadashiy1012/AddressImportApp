package jp.yama.addressimportapp

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.lang.RuntimeException
import java.net.URI

class AppState private constructor(ctx: Context) {

    val version = MutableLiveData<String>()
    val urls = MutableLiveData<List<Pair<SectionKeys, String>>>()
    val loaded = MutableLiveData<Boolean>()

    init {
        loaded.value = false
    }

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