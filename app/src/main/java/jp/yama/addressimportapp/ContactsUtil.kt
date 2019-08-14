package jp.yama.addressimportapp

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import java.util.*

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

    fun insertContact(address: Address) {
        val id = getId()
        insertName(id, address.name, address.kana)
        insertPhone(id, address.phone, ContactsContract.CommonDataKinds.Phone.TYPE_WORK)
        insertPhone(id, address.phone2, ContactsContract.CommonDataKinds.Phone.TYPE_WORK)
        insertMail(id, address.mail, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
        insertMail(id, address.mail2, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
        insertOrg(id, address.org + " " + address.section)
        insertNumber(id, address.number)
    }

    private fun getId(): Long {
        val contentVal = ContentValues()
        val uri = ctx.contentResolver.insert(ContactsContract.RawContacts.CONTENT_URI, contentVal)
        return ContentUris.parseId(uri)
    }

    private fun insertName(id: Long, name: String, kana: String) {
        val contentVal = ContentValues()
        contentVal.put(ContactsContract.Data.RAW_CONTACT_ID, id)
        contentVal.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
        contentVal.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, name)
        contentVal.put(ContactsContract.CommonDataKinds.StructuredName.PHONETIC_GIVEN_NAME, kana)
        ctx.contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentVal)
    }

    private fun insertPhone(id: Long, phone: String, type: Int) {
        val contentVal = ContentValues()
        contentVal.put(ContactsContract.Data.RAW_CONTACT_ID, id)
        contentVal.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
        contentVal.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
        contentVal.put(ContactsContract.CommonDataKinds.Phone.TYPE, type)
        ctx.contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentVal)
    }

    private fun insertMail(id: Long, mail: String, type: Int) {
        val contentVal = ContentValues()
        contentVal.put(ContactsContract.Data.RAW_CONTACT_ID, id)
        contentVal.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
        contentVal.put(ContactsContract.CommonDataKinds.Email.ADDRESS, mail)
        contentVal.put(ContactsContract.CommonDataKinds.Email.TYPE, type)
        ctx.contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentVal)
    }

    private fun insertOrg(id: Long, org: String) {
        val contentVal = ContentValues()
        contentVal.put(ContactsContract.Data.RAW_CONTACT_ID, id)
        contentVal.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
        contentVal.put(ContactsContract.CommonDataKinds.Organization.COMPANY, org)
        ctx.contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentVal)
    }

    private fun insertNumber(id: Long, num: String) {
        val contentVal = ContentValues()
        contentVal.put(ContactsContract.Data.RAW_CONTACT_ID, id)
        contentVal.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE)
        contentVal.put(ContactsContract.CommonDataKinds.Note.NOTE, num)
        ctx.contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentVal)
    }

    fun removeContacts() {
        val cursor = ctx.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            null
        )
        cursor?.let {
            while (it.moveToNext()) {
                val key = it.getString(it.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY))
                val uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, key)
                ctx.contentResolver.delete(uri, null, null)
            }
        }
    }

    fun updateContact(address: Address) {
        // TODO: 作る
    }

}