package com.example.safeher

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class AddContactActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etPhoneNumber: EditText
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_contact)

        etName = findViewById(R.id.et_contact_name)
        etPhoneNumber = findViewById(R.id.et_contact_phone)
        btnSave = findViewById(R.id.btn_save_contact)

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val phone = etPhoneNumber.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please enter both name and phone", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Use only ContactStorage.Contact
            val contact = ContactStorage.Contact(name, phone)
            ContactStorage.addContact(contact)

            setResult(RESULT_OK)
            finish()
        }
    }
}
