package jp.yama.addressimportapp

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.DialogInterface
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import java.lang.Exception
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {

    private val job = SupervisorJob()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val MY_REQUEST_RESULT = 1

    val payload = MutableLiveData<Pair<AppKeys, Any>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            val fragment = MainFragment.newInstance()
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.container, fragment)
            transaction.commit()
            loadData()
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                AlertDialog.Builder(this)
                    .setTitle("パーミッションについて")
                    .setMessage("連絡先にアクセスするために許可が必要です")
                    .setPositiveButton(
                        "OK",
                        DialogInterface.OnClickListener { _, i ->
                            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), MY_REQUEST_RESULT)
                        }
                    ).show()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), MY_REQUEST_RESULT)
            }
        } else {
            //Toast.makeText(this, "permission granted!", Toast.LENGTH_LONG).show()
            pushContacts()
            fetchContacts()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_REQUEST_RESULT -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(this, "permission granted!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "permission not granted!", Toast.LENGTH_LONG).show()
                }
            }
            else -> {
                Toast.makeText(this, "permission not granted!", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancelChildren()
    }

    private fun loadData() = launch {
        try {
            val url1 = "https://drive.google.com/uc?id=1A2DCTrtixpUxZfG_ydfjmM1NlsDqp8lI"
            val url2 = "https://drive.google.com/uc?id=1Q4urg3mSEyRnr_ygaxxHeSFP6QEGpAAB"
            val tasksA = listOf(HttpClient.get(url1, AppKeys.VERSION), HttpClient.get(url2, AppKeys.SECTION_URLS))
            tasksA.awaitAll().forEach { e ->
                when (e.first) {
                    AppKeys.VERSION -> {
                        payload.value = Pair(AppKeys.VERSION, e.second.let {
                            CsvUtil.parseCsv(it.body?.string()!!).get(1, 1)
                        })
                    }
                    AppKeys.SECTION_URLS -> {
                        val urls = getUrls(CsvUtil.parseCsv(e.second.body?.string()!!))
                        payload.value = Pair(AppKeys.SECTION_URLS, urls)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("yama", "error!", e)
        }
    }

    private fun getUrls(csv: Csv): List<Pair<SectionKeys, String>> {
        return csv.ary.filterIndexed { idx, _ -> idx > 0 }.map {
            val url = "https://drive.google.com/uc?id=" + it[3]
            val key = SectionKeys.valueOf(it[2].toUpperCase())
            Pair(key, url)
        }
    }

    private fun fetchContacts() {
        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            null
            )
        cursor?.let {
            while (it.moveToNext()) {
                val id = it.getString(it.getColumnIndex(ContactsContract.Contacts._ID))
                val name = it.getString(it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                Log.d("yama", id + ":" + name)
            }
        }
    }

    private fun pushContacts() {
        val address = Address(-1, "山崎 義", "ヤマザキ タダシ", "hoge",
            "000-0000-0000", "000-0000-0000",
            "hoge@hogemail.com", "hoge@hogemail.com",
            "1234567890", SectionKeys.KAIHATSU.label
        )
        val util = ContactsUtil(this)
        util.insertValue(address)
    }

}
