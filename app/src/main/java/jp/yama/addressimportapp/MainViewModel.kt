package jp.yama.addressimportapp

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel;

class MainViewModel(
    private val state: SavedStateHandle
) : ViewModel() {

    val loaded = state.getLiveData<List<Pair<AppKeys, Boolean>>>(AppKeys.LOADED.name)
    var version: String?
        get() = state.get<String>(AppKeys.VERSION.name)
        set(value) = state.set(AppKeys.VERSION.name, value)
    var urls: List<Pair<SectionKeys, String>>?
        get() = state.get<List<Pair<SectionKeys, String>>>(AppKeys.SECTION_URLS.name)
        set(value) = state.set(AppKeys.SECTION_URLS.name, value)

    init {
        loaded.value = listOf(
            Pair(AppKeys.VERSION, false),
            Pair(AppKeys.SECTION_URLS, false)
        )
    }

    fun toggleLoaded(key: AppKeys) {
        val subject = Pair(key, true)
        loaded.value?.let {
            val filtered = it.filter { e -> e.first !== key }.toMutableList()
            filtered.add(subject)
            loaded.value = filtered.toList()
        }
    }

}
