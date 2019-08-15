package jp.yama.addressimportapp

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
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
        val factory = SavedStateVMFactory(this, Bundle().apply {
            arguments?.getString(AppKeys.VERSION.name)?.let {
                this.putString(AppKeys.VERSION.name, it)
            }
        })
        viewModel = ViewModelProviders.of(this, factory).get(MainViewModel::class.java)
        viewModel.loaded.observe(this, Observer {
            Log.d("yama", it.toString())
            val filtered = it.filter { e -> !e.second }
            if (filtered.size == 0) {
                Log.d("yama", "loaded!")
                buttonA.isEnabled = true
            }
        })
        AppState.instance.payload.observe(this, Observer {
            when(it.first) {
                AppKeys.VERSION -> {
                    val value = it.second
                    viewModel.version = if (value is String) { value } else { null }
                }
                AppKeys.SECTION_URLS -> {
                    viewModel.urls = (it.second as? List<Pair<SectionKeys, String>>)
                }
                else -> {}
            }
            viewModel.toggleLoaded(it.first)
        })
        importBtn.setOnClickListener {
            importBtn.isEnabled = false
            progressBar.visibility = ProgressBar.VISIBLE
            fetchAddressData()
            Log.d("yama", "continue..")
        }
        debugBtn.setOnClickListener {
            DebugFuns.getAlert(this.context!!).show()
        }
    }

    private fun fetchAddressData() = launch {
        try {
            val tasks = viewModel.urls?.map { e ->
                HttpClient.get(e.second, e.first)
            }
            tasks?.awaitAll()?.map {
                Log.d("yama", it.toString())
            }
            progressBar.visibility = ProgressBar.INVISIBLE
            importBtn.isEnabled = true
        } catch (e: Exception) {
            Log.e("yama", "error!", e)
        }
    }

}
