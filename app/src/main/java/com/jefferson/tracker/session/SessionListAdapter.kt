package com.jefferson.tracker.session

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jefferson.tracker.databinding.SessionItemBinding

class SessionListAdapter(val context: Context) :
    ListAdapter<Session, SessionItemViewHolder>(SessionDiffCallback()) {

    override fun onBindViewHolder(holder: SessionItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionItemViewHolder {
        val layoutInflater = LayoutInflater
            .from(parent.context)

        val binding = SessionItemBinding.inflate(layoutInflater, parent, false)

        return SessionItemViewHolder(binding)
    }
}

class SessionDiffCallback : DiffUtil.ItemCallback<Session>() {
    override fun areItemsTheSame(oldItem: Session, newItem: Session): Boolean {
        return oldItem.sessionId == newItem.sessionId
    }

    override fun areContentsTheSame(oldItem: Session, newItem: Session): Boolean {
        return oldItem == newItem
    }

}

class SessionItemListener(val clickListener: (sessionId: Long) -> Unit) {
    fun onClick(session: Session) = clickListener(session.sessionId)
}

class SessionItemViewHolder(val binding: SessionItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(item: Session) {
        binding.session = item
        binding.executePendingBindings()
    }
}