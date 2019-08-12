package jp.yama.addressimportapp

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel;

class MainViewModel(
    private val state: SavedStateHandle
) : ViewModel() {

    val loaded = state.getLiveData<List<Pair<AppKeys, Boolean>>>(AppKeys.LOADED.name)
    val version = state.getLiveData<String>(AppKeys.VERSION.name)
    val sectionList = state.getLiveData<List<Pair<SectionKeys, String>>>(AppKeys.SECTIONS.name)
    val dataList = state.getLiveData<List<Pair<SectionKeys, Csv>>>(AppKeys.DATALIST.name)

    init {
        loaded.value = listOf(
            Pair(AppKeys.VERSION, false),
            Pair(AppKeys.SECTIONS, false),
            Pair(AppKeys.DATALIST, false)
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
