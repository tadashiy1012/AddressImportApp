package jp.yama.addressimportapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import java.lang.Exception
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {

    private val job = SupervisorJob()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    val payload = MutableLiveData<Pair<AppKeys, Any>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            val fragment = MainFragment.newInstance()
            val bundle = Bundle().apply {
                this.putString(AppKeys.VERSION.name, " ")
            }
            fragment.arguments = bundle
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.container, fragment)
            transaction.commit()
            loadData()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancelChildren()
    }

    private fun loadData() = launch {
        try {
            var urls = mutableListOf<Pair<SectionKeys, String>>()
            val url1 = "https://drive.google.com/uc?id=1A2DCTrtixpUxZfG_ydfjmM1NlsDqp8lI"
            val url2 = "https://drive.google.com/uc?id=1Q4urg3mSEyRnr_ygaxxHeSFP6QEGpAAB"
            val tasksA = listOf(HttpClient.get(url1, AppKeys.VERSION), HttpClient.get(url2, AppKeys.SECTIONS))
            tasksA.awaitAll().forEach { e ->
                when (e.first) {
                    AppKeys.VERSION -> {
                        payload.value = Pair(AppKeys.VERSION, e.second.let {
                            CsvUtil.parseCsv(it.body?.string()!!).get(1, 1)
                        })
                    }
                    AppKeys.SECTIONS -> {
                        urls.addAll(getUrls(CsvUtil.parseCsv(e.second.body?.string()!!)))
                        payload.value = Pair(AppKeys.SECTIONS, urls)
                    }
                }
            }
            val tasksB = urls.map {
                HttpClient.get(it.second, it.first)
            }
            tasksB.awaitAll().map { e ->
                Pair(e.first, CsvUtil.parseCsv(e.second.body?.string()!!))
            }.let {
                payload.value = Pair(AppKeys.DATALIST, it)
            }
        } catch (e: Exception) {
            Log.w("yama", "error", e)
        }
    }

    private fun getUrls(csv: Csv): List<Pair<SectionKeys, String>> {
        return csv.ary.filterIndexed { idx, _ -> idx > 0 }.map {
            val url = "https://drive.google.com/uc?id=" + it[3]
            val key = SectionKeys.valueOf(it[2].toUpperCase())
            Pair(key, url)
        }
    }

}
