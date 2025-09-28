package com.example.casasync

import android.app.Activity
import android.content.res.Resources
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import android.content.Context
import android.os.Build
import android.widget.Toast

object SnackbarUtils {

    fun show(
        activity: Activity,
        message: String,
        iconResId: Int? = null,
        duration: Int = Snackbar.LENGTH_LONG
    ) {
        val rootView = activity.window.decorView
        val snackbar = Snackbar.make(rootView, "", duration)

        val customView = LayoutInflater.from(activity).inflate(R.layout.custom_snackbar, null)
        val textView = customView.findViewById<TextView>(R.id.snackbar_text)
        val iconView = customView.findViewById<ImageView>(R.id.snackbar_icon)

        textView.text = message
        if (iconResId != null) {
            iconView.setImageResource(iconResId)
            iconView.visibility = View.VISIBLE
        }

        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val targetWidth = (screenWidth * 0.7).toInt()
        val sideMargin = (screenWidth * 0.15).toInt()

        val params = FrameLayout.LayoutParams(targetWidth, FrameLayout.LayoutParams.WRAP_CONTENT)
        params.leftMargin = sideMargin
        params.rightMargin = sideMargin
        params.gravity = Gravity.BOTTOM
        val bottomMarginDp = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            100f,
            Resources.getSystem().displayMetrics
        ).toInt()
        params.bottomMargin = bottomMarginDp

        customView.layoutParams = params

        val snackbarLayout = snackbar.view as ViewGroup
        snackbarLayout.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        snackbarLayout.setPadding(0, 0, 0, 0)
        snackbarLayout.removeAllViews()
        snackbarLayout.addView(customView)

        snackbar.show()
    }

    fun showMessage(context: Context, message: String, iconResId: Int? = null) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            SnackbarUtils.show(context as Activity, message, iconResId)
        } else {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}
