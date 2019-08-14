package jp.yama.addressimportapp

import android.app.AlertDialog
import android.content.DialogInterface
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
    private lateinit var debugBtn: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.main_fragment, container, false)
        importBtn = view.findViewById(R.id.buttonA)
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
        debugBtn.setOnClickListener {
            getDebugMenu()
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

    private fun getDebugMenu() {
        val items = listOf<String>("read contacts", "put contacts")
        AlertDialog.Builder(this.context)
            .setTitle("debug menu")
            .setItems(items.toTypedArray(), DialogInterface.OnClickListener { _, index ->
                when (index) {
                    0 -> { fetchContacts() }
                    1 -> { pushContacts() }
                }
            }).show()
    }


    private fun fetchContacts() {
        val util = ContactsUtil(this.context!!)
        util.fetchContacts().forEach { e -> Log.d("yama", e.toString()) }
    }

    private fun pushContacts() {
        val address = Address(-1, "山崎 義", "ヤマザキ タダシ", "hoge",
            "000-0000-0000", "000-0000-0000",
            "hoge@hogemail.com", "hoge@hogemail.com",
            "1234567890", SectionKeys.KAIHATSU.label
        )
        val util = ContactsUtil(this.context!!)
        util.insertValue(address)
    }

}
