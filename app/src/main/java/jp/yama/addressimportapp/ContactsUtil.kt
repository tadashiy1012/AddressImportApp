package jp.yama.addressimportapp

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.provider.ContactsContract

class ContactsUtil(private val ctx: Context) {

    private lateinit var resolver: ContentResolver

    init {
        resolver = ctx.contentResolver
    }

    fun insertValue() {
    }

    private fun getId(): Long {
        val contentVal = ContentValues()
        val uri = resolver.insert(ContactsContract.RawContacts.CONTENT_URI, contentVal)
        return ContentUris.parseId(uri)
    }

    private fun insertName(id: Long, name: String) {
        val contentVal = ContentValues()
        contentVal.put(ContactsContract.Data.RAW_CONTACT_ID, id)
        contentVal.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
        contentVal.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, name)
        resolver.insert(ContactsContract.Data.CONTENT_URI, contentVal)
    }


}