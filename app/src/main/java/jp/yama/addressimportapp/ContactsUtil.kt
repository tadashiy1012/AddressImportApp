package jp.yama.addressimportapp

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.provider.ContactsContract
import android.util.Log

class ContactsUtil(private val ctx: Context) {

    companion object {
        enum class PhoneTypes(val value: Int) {
            HOME(ContactsContract.CommonDataKinds.Phone.TYPE_HOME),
            WORK(ContactsContract.CommonDataKinds.Phone.TYPE_WORK),
            FAX_HOME(ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME),
            FAX_WORK(ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK),
            MOBILE(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE),
            OTHER(ContactsContract.CommonDataKinds.Phone.TYPE_OTHER)
        }
        enum class MailTypes(val value: Int) {
            HOME(ContactsContract.CommonDataKinds.Email.TYPE_HOME),
            WORK(ContactsContract.CommonDataKinds.Email.TYPE_WORK),
            MOBILE(ContactsContract.CommonDataKinds.Email.TYPE_MOBILE),
            OTHER(ContactsContract.CommonDataKinds.Email.TYPE_OTHER)
        }
    }

    private var resolver: ContentResolver = ctx.contentResolver

    fun fetchContacts(): List<Address> {
        var result = mutableListOf<Address>()
        val cursor = resolver.query(
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
                val org = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY)).split(" ")
                val num = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE))
                result.add(Address(id, names[0], names[1], org[0], phones[0], phones[1], mails[0], mails[1], num, org[1]))
            }
            it.close()
        }
        return result
    }

    private fun fetchName(id: Long): List<String> {
        var result = mutableListOf<String>()
        val cursor = resolver.query(
            ContactsContract.Data.CONTENT_URI,
            null,
            ContactsContract.Data.MIMETYPE + " = ?",
            arrayOf(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE),
            ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME
        )
        cursor?.let {
            if (cursor.moveToFirst()) {
                result.add(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME)))
                result.add(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.PHONETIC_GIVEN_NAME)))
            }
        }
        return result
    }

    private fun fetchPhone(id: Long): List<String> {
        var result = mutableListOf<String>()
        val cursor = resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
            null,
            null
        )
        cursor?.let {
            while (it.moveToNext()) {
                result.add(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA)))
            }
            it.close()
        }
        return result
    }

    private fun fetchMail(id: Long): List<String> {
        var result = mutableListOf<String>()
        val cursor = resolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            null,
            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + id,
            null,
            null
        )
        cursor?.let {
            while (it.moveToNext()) {
                result.add(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)))
            }
            it.close()
        }
        return result
    }

    private fun fetchEtc(id: Long): List<String> {
        var result = mutableListOf<String>()
        // TODO ここから
        return result
    }

    fun insertValue(address: Address) {
        val id = getId()
        insertName(id, address.name, address.kana)
        insertPhone(id, address.phone, Companion.PhoneTypes.HOME)
        insertPhone(id, address.phone2, Companion.PhoneTypes.WORK)
        insertMail(id, address.mail, Companion.MailTypes.HOME)
        insertMail(id, address.mail2, Companion.MailTypes.WORK)
        insertOrg(id, address.org + " " + address.section)
        insertNumber(id, address.number)
    }

    private fun getId(): Long {
        val contentVal = ContentValues()
        val uri = resolver.insert(ContactsContract.RawContacts.CONTENT_URI, contentVal)
        return ContentUris.parseId(uri)
    }

    private fun insertName(id: Long, name: String, kana: String) {
        val contentVal = ContentValues()
        contentVal.put(ContactsContract.Data.RAW_CONTACT_ID, id)
        contentVal.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
        contentVal.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, name)
        contentVal.put(ContactsContract.CommonDataKinds.StructuredName.PHONETIC_GIVEN_NAME, kana)
        resolver.insert(ContactsContract.Data.CONTENT_URI, contentVal)
    }

    private fun insertPhone(id: Long, phone: String, type: PhoneTypes) {
        val contentVal = ContentValues()
        contentVal.put(ContactsContract.Data.RAW_CONTACT_ID, id)
        contentVal.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
        contentVal.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
        contentVal.put(ContactsContract.CommonDataKinds.Phone.TYPE, type.value)
        resolver.insert(ContactsContract.Data.CONTENT_URI, contentVal)
    }

    private fun insertMail(id: Long, mail: String, type: MailTypes) {
        val contentVal = ContentValues()
        contentVal.put(ContactsContract.Data.RAW_CONTACT_ID, id)
        contentVal.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
        contentVal.put(ContactsContract.CommonDataKinds.Email.ADDRESS, mail)
        contentVal.put(ContactsContract.CommonDataKinds.Email.TYPE, type.value)
        resolver.insert(ContactsContract.Data.CONTENT_URI, contentVal)
    }

    private fun insertOrg(id: Long, org: String) {
        val contentVal = ContentValues()
        contentVal.put(ContactsContract.Data.RAW_CONTACT_ID, id)
        contentVal.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
        contentVal.put(ContactsContract.CommonDataKinds.Organization.COMPANY, org)
        resolver.insert(ContactsContract.Data.CONTENT_URI, contentVal)
    }

    private fun insertNumber(id: Long, num: String) {
        val contentVal = ContentValues()
        contentVal.put(ContactsContract.Data.RAW_CONTACT_ID, id)
        contentVal.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE)
        contentVal.put(ContactsContract.CommonDataKinds.Note.NOTE, num)
        resolver.insert(ContactsContract.Data.CONTENT_URI, contentVal)
    }

}