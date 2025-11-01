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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toDrawable
import com.devminds.casasync.R

object DialogUtils {

    private var currentDialog: Dialog? = null

    private fun getBannerDurations(): Triple<Long, Long, Long> {
        return Triple(2200L, 3000L, 3700L)
    }

    fun dismissActiveBanner() {
        currentDialog?.let {
            if (it.isShowing && (it.context as? Activity)?.isFinishing == false && (it.context as? Activity)?.isDestroyed == false) {
                it.dismiss()
            }
        }
        currentDialog = null
    }

    fun show(context: Context, message: String, iconResId: Int? = null) {

        currentDialog?.dismiss()
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.notification_banner)
        dialog.setCancelable(false)

        val window = dialog.window
        window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL)
        window?.addFlags(
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        )
        window?.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        )

        // remove a sombra do dialog
        window?.setDimAmount(0f)

        // animação/suavidade
        window?.attributes?.windowAnimations = R.style.DialogToastAnimation

        val screenWidth = context.resources.displayMetrics.widthPixels
        val targetWidth = (screenWidth * 0.60).toInt()
        val sideMargin = (screenWidth * 0.20).toInt()

        val layout = dialog.findViewById<ConstraintLayout>(R.id.notification_layout)

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

        val (_, medium, _) = getBannerDurations()

        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing && (context as? Activity)?.isFinishing == false && !context.isDestroyed) {
                dialog.dismiss()
            }
        }, medium)

        dialog.show()
    }

    fun showMessage(context: Context, message: String) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // se for android 14 ou inferior
            // chama o dialog
            if (context is Activity) {
                show(context, message, R.drawable.casasync)
            } else {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show() // mostra o toast padrão
        }
    }
}
