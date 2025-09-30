package com.devminds.casasync

import android.content.Context
import androidx.fragment.app.Fragment
import com.devminds.casasync.R

// classe utilitária
object Utils {

    // função para converter dp para pixels
    fun dpToPx(context: Context, dp: Int): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale).toInt()
    }

    // garantia de chamar a função apenas se o fragmento estiver adicionado ao container
    fun Fragment.safeShowDialog(message: String, iconResId: Int? = null, duration: Long = 3000L) {
        if (isAdded) {
            DialogUtils.show(requireContext(), message, R.drawable.casasync, duration)
        }
    }
}
