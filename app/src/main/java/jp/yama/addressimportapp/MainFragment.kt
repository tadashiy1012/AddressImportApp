package jp.yama.addressimportapp

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateVMFactory
import kotlinx.android.synthetic.main.main_fragment.*
import kotlinx.coroutines.*
import java.lang.Exception
import kotlin.coroutines.CoroutineContext


class MainFragment : Fragment(), CoroutineScope {

    companion object {
        fun newInstance() = MainFragment()
    }

    private val job = SupervisorJob()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private lateinit var viewModel: MainViewModel
    private lateinit var importBtn: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var debugBtn: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.main_fragment, container, false)
        importBtn = view.findViewById(R.id.buttonA)
        progressBar = view.findViewById(R.id.progressBar)
        debugBtn = view.findViewById(R.id.debugBtn)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val factory = SavedStateVMFactory(this)
        viewModel = ViewModelProviders.of(this, factory).get(MainViewModel::class.java)
        viewModel.loaded.observe(this, Observer {
            Log.d("yama", it.toString())
            val filtered = it.filter { e -> !e.second }
            if (filtered.size == 0 || AppState.instance.loaded.value!!) {
                Log.d("yama", "loaded!")
                buttonA.isEnabled = true
                AppState.instance.loaded.value = true
            }
        })
        AppState.instance.version.observe(this, Observer {
            viewModel.toggleLoaded(AppKeys.VERSION)
        })
        AppState.instance.urls.observe(this, Observer {
            viewModel.toggleLoaded(AppKeys.SECTION_URLS)
        })
        importBtn.setOnClickListener {
            importBtn.isEnabled = false
            progressBar.visibility = ProgressBar.VISIBLE
            fetchAddressData(this.context!!)
            Log.d("yama", "continue..")
        }
        debugBtn.setOnClickListener {
            DebugFuns.getAlert(this.context!!).show()
        }
    }

    private fun fetchAddressData(ctx: Context) = launch {
        val start = SystemClock.uptimeMillis()
        try {
            val tasks = AppState.instance.urls.value?.map { e ->
                HttpClient.get(e.second, e.first)
            }
            val ls = tasks?.awaitAll()?.map {
                Log.d("yama", it.toString())
                val csv = CsvUtil.parseCsv(it.second.body?.string()!!)
                csv.ary.filterIndexed { idx, _ -> idx > 0 }.map { e ->
                    Address.builder(it.first!!, e)
                }
            }?.flatten()
            Log.d("yama", "size:${ls?.size}")
            val util = ContactsUtil(ctx)
            val tasks2 = ls?.take(100)?.map { e ->
                util.insertContactAsync(e)
            }
            val results = tasks2?.awaitAll()
            Log.d("yama", results?.size.toString())
            progressBar.visibility = ProgressBar.INVISIBLE
            importBtn.isEnabled = true
        } catch (e: Exception) {
            Log.e("yama", "error!", e)
        }
        val end = SystemClock.uptimeMillis()
        Log.d("yama", "elapse: ${end - start}")
    }

}
