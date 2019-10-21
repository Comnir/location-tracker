package com.jefferson.tracker.session

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jefferson.tracker.R
import com.jefferson.tracker.persistance.Session

class SessionListAdapter(val context: Context) : RecyclerView.Adapter<SessionItemViewHolder>() {
    var data = listOf<Session>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: SessionItemViewHolder, position: Int) {
        val item = data[position]
        holder.textView.text = context.getString(R.string.session_item_text, item.sessionId)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionItemViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.session_item, parent, false) as TextView
        return SessionItemViewHolder(view)
    }
}