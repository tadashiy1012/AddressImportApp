package jp.yama.addressimportapp

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateVMFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var version: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.main_fragment, container, false)
        version = view.findViewById(R.id.version)
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
        viewModel.version.observe(this, Observer {
            version.text = "ver " + it
        })
        viewModel.sectionList.observe(this, Observer {
            Log.d("yama", it.toString())
        })
        viewModel.loaded.observe(this, Observer {
            Log.d("yama", it.toString())
            val filtered = it.filter { e -> e.second !== true }
            if (filtered.size == 0) {
                Log.d("yama", "loaded!")
            }
        })
        (activity as MainActivity).payload.observe(this, Observer {
            when(it.first) {
                AppKeys.VERSION -> viewModel.version.value = (it.second as String)
                AppKeys.SECTIONS -> viewModel.sectionList.value = (it.second as List<Pair<SectionKeys, String>>)
                AppKeys.DATALIST -> viewModel.dataList.value = (it.second as List<Pair<SectionKeys, Csv>>)
            }
            viewModel.toggleLoaded(it.first)
        })
    }

}
