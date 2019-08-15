package jp.yama.addressimportapp

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.*
import java.lang.Exception

object DebugFuns {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    private fun removeContacts(ctx: Context) {
        val start = SystemClock.uptimeMillis()
        scope.launch {
            val util = ContactsUtil(ctx)
            val results = util.removeContactsAsync().awaitAll()
            Log.d("yama", results.size.toString())
            val end = SystemClock.uptimeMillis()
            Log.d("yama", "elapse: ${end - start}")
        }
    }

    private fun fetchContacts(ctx: Context) {
        val util = ContactsUtil(ctx)
        util.fetchContacts().forEach { e -> Log.d("yama", e.toString()) }
    }

    private fun putBatch(ctx: Context) {
        scope.launch {
            val address = Address(
                -1, "山崎 義", "ヤマザキ タダシ", "hoge",
                "000-0000-0000", "000-0000-0000",
                "hoge@hogemail.com", "hoge@hogemail.com",
                "1234567890", SectionKeys.KAIHATSU.label
            )
            val util = ContactsUtil(ctx)
            val result = util.batchInsertContactAsync(listOf(address)).awaitAll()
            Log.d("yama", result.toString())
        }
    }

    fun getAlert(ctx: Context): AlertDialog {
        val items = listOf<String>("read contacts", "put batch", "remove contacts")
        val dialog = AlertDialog.Builder(ctx)
            .setTitle("debug menu")
            .setItems(items.toTypedArray(), DialogInterface.OnClickListener { _, index ->
                when (index) {
                    0 -> { DebugFuns.fetchContacts(ctx) }
                    1 -> { DebugFuns.putBatch(ctx) }
                    2 -> { DebugFuns.removeContacts(ctx) }
                    else -> {}
                }
            }).create()
        return dialog
    }

}