package com.jefferson.tracker.session

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.jefferson.tracker.R

@BindingAdapter("sessionItemTitle")
fun TextView.setSessionItemTitle(item: Session) {
    text = context.getString(R.string.session_item_text, item.sessionId)
}

