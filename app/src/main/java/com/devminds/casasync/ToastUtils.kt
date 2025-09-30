package com.devminds.casasync

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.devminds.casasync.R
import com.google.android.material.snackbar.Snackbar

object ToastUtils {

    fun show(
        context: Context,
        message: String,
        iconResId: Int? = null,
        duration: Int = Snackbar.LENGTH_LONG
    ) {

        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.notification_banner, null)

        val textView = layout.findViewById<TextView>(R.id.notification_text)
        val iconView = layout.findViewById<ImageView>(R.id.notification_icon)

        textView.text = message
        if (iconResId != null) {
            iconView.setImageResource(iconResId)
            iconView.visibility = View.VISIBLE
        }
        iconView.setImageResource(R.drawable.casasync)
        iconView.visibility = View.VISIBLE

        val toast = Toast(context)
        toast.view = layout
        toast.duration = Toast.LENGTH_LONG
        toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 280)
        toast.show()
    }
}
