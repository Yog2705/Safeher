package com.example.safeher

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TrustedContactsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrustedContactsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trusted_contacts)

        recyclerView = findViewById(R.id.recyclerViewContacts)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Load contacts from SharedPreferences
        val contacts = applicationContext.loadContacts()

        if (contacts.isEmpty()) {
            Toast.makeText(this, "No trusted contacts found", Toast.LENGTH_SHORT).show()
        }

        adapter = TrustedContactsAdapter(contacts.toMutableList()) { contact ->
            // Optional: handle remove contact click here if you want
            removeContact(contact)
        }
        recyclerView.adapter = adapter
    }

    private fun removeContact(contact: TrustedContact) {
        val currentContacts = applicationContext.loadContacts().toMutableList()
        currentContacts.remove(contact)
        applicationContext.saveContacts(currentContacts)
        adapter.updateContacts(currentContacts)
        Toast.makeText(this, "Contact removed", Toast.LENGTH_SHORT).show()
    }
}
