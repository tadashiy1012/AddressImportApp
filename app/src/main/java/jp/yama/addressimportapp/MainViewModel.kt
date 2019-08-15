package jp.yama.addressimportapp

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel;

class MainViewModel(
    private val state: SavedStateHandle
) : ViewModel() {

    val loaded = state.getLiveData<List<Pair<AppKeys, Boolean>>>(AppKeys.LOADED.name)

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
