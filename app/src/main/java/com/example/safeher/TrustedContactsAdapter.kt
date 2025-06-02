package com.example.safeher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TrustedContactsAdapter(
    private var contacts: MutableList<TrustedContact>,
    private val onRemoveClick: (TrustedContact) -> Unit
) : RecyclerView.Adapter<TrustedContactsAdapter.ContactViewHolder>() {

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName: TextView = itemView.findViewById(R.id.textViewName)
        val textPhone: TextView = itemView.findViewById(R.id.textViewPhone)
        val btnRemove: ImageButton = itemView.findViewById(R.id.buttonRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trusted_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.textName.text = contact.name
        holder.textPhone.text = contact.phoneNumber
        holder.btnRemove.setOnClickListener {
            onRemoveClick(contact)
        }
    }

    override fun getItemCount(): Int = contacts.size

    fun updateContacts(newContacts: List<TrustedContact>) {
        contacts = newContacts.toMutableList()
        notifyDataSetChanged()
    }
}
