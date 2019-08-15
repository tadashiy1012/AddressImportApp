package jp.yama.addressimportapp

import android.content.ContentProviderOperation
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

class ContactsUtil(private val ctx: Context) {

    private val EMPTY = "[EMPTY]"

    fun findContactId(address: Address): Long {
        var result = -1L
        val projection = null
        val selection = ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME + " = ?"
        val args = arrayOf(address.name)
        val cursor = ctx.contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            projection,
            selection,
            args,
            null
        )
        cursor?.let {
            if (it.moveToFirst()) {
                result = it.getLong(it.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID))
            }
        }
        return result
    }

    fun findContact(id: Long): Address {
        Log.d("yama", "find:${id}")
        val names = fetchName(id)
        val phones = fetchPhone(id)
        val mails = fetchMail(id)
        val org = fetchOrg(id)
        val note = fetchNote(id)
        val company = org.split(" ")[0]
        val section = org.split(" ").let {
            if (it.size >= 2) { it[1] }
            else { EMPTY }
        }
        return Address(id, names["name"]!!, names["kana"]!!, company,
            phones[0], phones[1], mails[0], mails[1],
            note, section)
    }

    fun fetchContacts(): List<Address> {
        var result = mutableListOf<Address>()
        val cursor = ctx.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            null
        )
        cursor?.let {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndex(ContactsContract.Contacts._ID))
                val names = fetchName(id)
                val phones = fetchPhone(id)
                val mails = fetchMail(id)
                val org = fetchOrg(id)
                val num = fetchNote(id)
                val company = org.split(" ")[0]
                val section = org.split(" ").let {
                    if (it.size >= 2) {
                        it[1]
                    } else {
                        EMPTY
                    }
                }
                result.add(Address(id,
                    names["name"]!!, names["kana"]!!, company,
                    phones[0], phones[1],
                    mails[0], mails[1],
                    num, section))
            }
            it.close()
        }
        return result
    }

    private fun fetchName(id: Long): Map<String, String> {
        var map = mutableMapOf<String, String>()
        val cursor = ctx.contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            null,
            ContactsContract.Data.MIMETYPE + " = ? AND " +
                    ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID  + " = " + id,
            arrayOf(
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
            ),
            null
        )
        cursor?.let {
            if (it.moveToFirst()) {
                map.put("display", it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME)) ?: EMPTY)
                map.put("name", it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME)) ?: EMPTY)
                map.put("kana", it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.PHONETIC_GIVEN_NAME)) ?: EMPTY)
            }
            it.close()
        }
        return map
    }

    private fun fetchPhone(id: Long): List<String> {
        var result = mutableListOf<String>()
        val cursor = ctx.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
            null,
            null
        )
        cursor?.let {
            while (it.moveToNext()) {
                result.add(it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA)) ?: EMPTY)
            }
            it.close()
        }
        result.apply {
            if (this.size < 2) {
                for (i in 1..(2 - this.size)) {
                    this.add(EMPTY)
                }
            }
        }
        return result
    }

    private fun fetchMail(id: Long): List<String> {
        var result = mutableListOf<String>()
        val cursor = ctx.contentResolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            null,
            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + id,
            null,
            null
        )
        cursor?.let {
            while (it.moveToNext()) {
                result.add(it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)) ?: EMPTY)
            }
            it.close()
        }
        result.apply {
            if (this.size < 2) {
                for (i in 1..(2 - this.size)) {
                    this.add(EMPTY)
                }
            }
        }
        return result
    }

    private fun fetchOrg(id: Long): String {
        var result = EMPTY
        val cursor = ctx.contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            null,
            ContactsContract.Data.MIMETYPE + " = ? AND " +
                    ContactsContract.CommonDataKinds.Organization.CONTACT_ID  + " = " + id,
            arrayOf(ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE),
            null
        )
        cursor?.let {
            if (it.moveToFirst()) {
                result = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY)) ?: EMPTY
            }
            it.close()
        }
        return result
    }


    private fun fetchNote(id: Long): String {
        var result = EMPTY
        val cursor = ctx.contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            null,
            ContactsContract.Data.MIMETYPE + " = ? AND " +
                    ContactsContract.CommonDataKinds.Note.CONTACT_ID  + " = " + id,
            arrayOf(ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE),
            null
        )
        cursor?.let {
            if (it.moveToFirst()) {
                result = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE)) ?: EMPTY
            }
            it.close()
        }
        return result
    }

    suspend fun batchInsertContactAsync(addressList: List<Address>): MutableList<Deferred<Long>> {
        var result = mutableListOf<Deferred<Long>>()
        addressList.forEach { e ->
            val ope = getOperations(e);
            result.add(execBatchInsertAsync(ope))
        }
        return result
    }

    private suspend fun execBatchInsertAsync(operations: ArrayList<ContentProviderOperation>) = coroutineScope {
        async(Dispatchers.Default) {
            ctx.contentResolver.applyBatch(ContactsContract.AUTHORITY, operations).let {
                ContentUris.parseId(it.first().uri)
            }
        }
    }

    private fun getOperations(address: Address): ArrayList<ContentProviderOperation> {
        val phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_WORK
        val mailType = ContactsContract.CommonDataKinds.Email.TYPE_WORK
        val operations = arrayListOf<ContentProviderOperation>(
            ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValues(ContentValues())
                .build(),
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                )
                .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, address.name)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.PHONETIC_GIVEN_NAME, address.kana).build(),
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, address.phone)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, phoneType).build(),
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, address.phone2)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, phoneType).build(),
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, address.mail)
                .withValue(ContactsContract.CommonDataKinds.Email.TYPE, mailType).build(),
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, address.mail2)
                .withValue(ContactsContract.CommonDataKinds.Email.TYPE, mailType).build(),
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
                )
                .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, address.org).build(),
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Note.NOTE, address.number).build()
        )
        return operations
    }

    private suspend fun execDeleteAsync(uri: Uri) = coroutineScope {
        async(Dispatchers.Default) {
            ctx.contentResolver.delete(uri, null, null)
        }
    }

    suspend fun removeContactsAsync(): MutableList<Deferred<Int>> {
        var result = mutableListOf<Deferred<Int>>()
        ctx.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            null
        )?.let {
            while (it.moveToNext()) {
                val key = it.getString(it.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY))
                val uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, key)
                result.add(execDeleteAsync(uri))
            }
        }
        return result
    }

}