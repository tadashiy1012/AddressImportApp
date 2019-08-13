package jp.yama.addressimportapp

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateVMFactory
import kotlinx.android.synthetic.main.main_fragment.*
import kotlinx.coroutines.*
import okhttp3.Response
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.main_fragment, container, false)
        importBtn = view.findViewById(R.id.buttonA)
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
            val filtered = it.filter { e -> e.second !== true }
            if (filtered.size == 0) {
                Log.d("yama", "loaded!")
                buttonA.isEnabled = true
            }
        })
        (activity as MainActivity).payload.observe(this, Observer {
            when(it.first) {
                AppKeys.VERSION -> viewModel.version = (it.second as String)
                AppKeys.SECTION_URLS -> viewModel.urls = (it.second as List<Pair<SectionKeys, String>>)
            }
            viewModel.toggleLoaded(it.first)
        })
        importBtn.setOnClickListener {
            importBtn.isEnabled = false
            fetchAddressData()
            Log.d("yama", "continue..")
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
            importBtn.isEnabled = true
        } catch (e: Exception) {
            Log.e("yama", "error!", e)
        }
    }

}
