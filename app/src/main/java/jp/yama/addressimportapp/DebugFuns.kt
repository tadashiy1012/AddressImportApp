package jp.yama.addressimportapp

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import kotlinx.coroutines.*
import java.lang.Exception

object DebugFuns {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    fun findContact(ctx: Context) {
        val address = Address(
            -1, "山崎 義", "ヤマザキ タダシ", "hoge",
            "000-0000-0000", "000-0000-0000",
            "hoge@hogemail.com", "hoge@hogemail.com",
            "1234567890", SectionKeys.KAIHATSU.label
        )
        val util = ContactsUtil(ctx)
        val resultId = util.findContactId(address)
        val result = util.findContact(resultId)
        Log.d("yama", result.toString())
    }

    fun removeContacts(ctx: Context) {
        val util = ContactsUtil(ctx)
        util.removeContacts()
    }

    fun fetchContacts(ctx: Context) {
        val util = ContactsUtil(ctx)
        util.fetchContacts().forEach { e -> Log.d("yama", e.toString()) }
    }

    fun pushContacts(ctx: Context) {
        val address = Address(-1, "山崎 義", "ヤマザキ タダシ", "hoge",
            "000-0000-0000", "000-0000-0000",
            "hoge@hogemail.com", "hoge@hogemail.com",
            "1234567890", SectionKeys.KAIHATSU.label
        )
        val util = ContactsUtil(ctx)
        util.insertContact(address)
    }

    fun putAsync(ctx: Context) = scope.launch {
        try {
            val deferred = async {
                Log.d("yama", "pending..")
                val address = Address(
                    -1, "山崎 義", "ヤマザキ タダシ", "hoge",
                    "000-0000-0000", "000-0000-0000",
                    "hoge@hogemail.com", "hoge@hogemail.com",
                    "1234567890", SectionKeys.KAIHATSU.label
                )
                val util = ContactsUtil(ctx)
                util.insertContact(address)
                true
            }
            Log.d("yama", "continue")
            deferred.await().let {
                Log.d("yama", "compl!")
            }
        } catch (e: Exception) {
            Log.e("yama", "error!", e)
        }
    }

    fun getAlert(ctx: Context): AlertDialog {
        val items = listOf<String>("read contacts", "put contacts", "put async", "remove contacts", "find contact")
        val dialog = AlertDialog.Builder(ctx)
            .setTitle("debug menu")
            .setItems(items.toTypedArray(), DialogInterface.OnClickListener { _, index ->
                when (index) {
                    0 -> { DebugFuns.fetchContacts(ctx) }
                    1 -> { DebugFuns.pushContacts(ctx) }
                    2 -> { DebugFuns.putAsync(ctx) }
                    3 -> { DebugFuns.removeContacts(ctx) }
                    4 -> { DebugFuns.findContact(ctx) }
                    else -> {}
                }
            }).create()
        return dialog
    }

}