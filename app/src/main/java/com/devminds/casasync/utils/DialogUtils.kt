package com.devminds.casasync.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import com.devminds.casasync.R

object DialogUtils {

    fun show(context: Context, message: String, iconResId: Int? = null, duration: Long = 1800L) {

        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.notification_banner)
        dialog.setCancelable(false)

        val window = dialog.window
        window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL)
        window?.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // remove a sombra do dialog
        window?.setDimAmount(0f)

        val screenWidth = context.resources.displayMetrics.widthPixels
        val targetWidth = (screenWidth * 0.7).toInt()
        val sideMargin = (screenWidth * 0.15).toInt()

        val layout = dialog.findViewById<LinearLayout>(R.id.notification_layout)

        val params = FrameLayout.LayoutParams(targetWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.leftMargin = sideMargin
        params.rightMargin = sideMargin

        val bottomMarginPx = Utils.dpToPx(context, 100)
        params.bottomMargin = bottomMarginPx

        layout.layoutParams = params

        val textView = dialog.findViewById<TextView>(R.id.notification_text)
        val iconView = dialog.findViewById<ImageView>(R.id.notification_icon)

        textView.text = message
        if (iconResId != null) {
            iconView.setImageResource(iconResId)
            iconView.visibility = View.VISIBLE
        } else {
            iconView.visibility = View.GONE
        }

        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({ dialog.dismiss() }, duration)
    }

    fun showMessage(context: Context, message: String) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // se for android 14 ou inferior
            show(context as Activity, message, R.drawable.casasync) // chama a função "show"
        } else {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show() // mostra o toast padrão
        }
    }
}
