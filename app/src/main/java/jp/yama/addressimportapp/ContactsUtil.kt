package jp.yama.addressimportapp

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.provider.ContactsContract

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

    fun insertValue(address: Address) {
        val id = getId()
        insertName(id, address.name, address.kana)
        insertPhone(id, address.phone, Companion.PhoneTypes.HOME)
        insertPhone(id, address.phone2, Companion.PhoneTypes.WORK)
        insertMail(id, address.mail, Companion.MailTypes.HOME)
        insertMail(id, address.mail2, Companion.MailTypes.WORK)
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

}