package com.devminds.casasync

import android.content.Context

// classe utilitária
object Utils {

    // função para converter dp para pixels
    fun dpToPx(context: Context, dp: Int): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale).toInt()
    }
}
