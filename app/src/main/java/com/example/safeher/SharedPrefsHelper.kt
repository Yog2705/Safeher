package com.example.safeher
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

val Context.contactsPrefs: SharedPreferences
    get() = getSharedPreferences("trusted_contacts_prefs", Context.MODE_PRIVATE)

fun Context.saveContacts(contacts: List<TrustedContact>) {
    val json = Gson().toJson(contacts)
    contactsPrefs.edit {
        putString("contacts_list", json)
    }
}

fun Context.loadContacts(): List<TrustedContact> {
    val json = contactsPrefs.getString("contacts_list", null) ?: return emptyList()
    return Gson().fromJson(json, object : TypeToken<List<TrustedContact>>() {}.type)

}

