import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object ContactStorage {

    private val contactList = mutableListOf<Contact>()
    private const val PREFS_NAME = "trusted_contacts"
    private const val KEY_CONTACTS = "contacts_json"

    data class Contact(val name: String, val phone: String)

    fun addContact(contact: Contact) {
        contactList.add(contact)
    }

    fun loadContacts(): List<Contact> = contactList

    fun removeContact(contact: Contact) {
        contactList.remove(contact)
    }

    fun clearContacts() {
        contactList.clear()
    }

    fun saveContacts(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = Gson().toJson(contactList)
        prefs.edit().putString(KEY_CONTACTS, json).apply()
    }

    fun loadContactsFromPrefs(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_CONTACTS, null)
        if (json != null) {
            val type = object : TypeToken<MutableList<Contact>>() {}.type
            val loadedList: MutableList<Contact> = Gson().fromJson(json, type)
            contactList.clear()
            contactList.addAll(loadedList)
        }
    }
}
