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
            val url = "https://drive.google.com/uc?id=1A2DCTrtixpUxZfG_ydfjmM1NlsDqp8lI"
            val result = HttpClient.get(url).await()
            payload.value = Pair(AppKeys.VERSION, result.let {
                CsvUtil.parseCsv(it.string())
            }.get(1, 1))
            val url2 = "https://drive.google.com/uc?id=1Q4urg3mSEyRnr_ygaxxHeSFP6QEGpAAB"
            val result2 = HttpClient.get(url2).await()
            val secLs = CsvUtil.parseCsv(result2.string()).ary.filterIndexed { idx, _ -> idx > 0 }.map {
                val url = "https://drive.google.com/uc?id=" + it[3]
                val key = SectionKeys.valueOf(it[2].toUpperCase())
                Pair(key, url)
            }
            payload.value = Pair(AppKeys.SECTIONS, secLs)
            val results = secLs.map {
                Pair(it.first, HttpClient.get(it.second))
            }.map {
                Pair(it.first, it.second.await())
            }
            payload.value = Pair(AppKeys.DATALIST, results.map {
                Pair(it.first, CsvUtil.parseCsv(it.second.string()))
            })
        } catch (e: Exception) {
            Log.w("yama", "error", e)
        }
    }

}
