package com.devminds.casasync.utils

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
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

    fun clearBackStack(fragmentManager: FragmentManager) {
        if (fragmentManager.backStackEntryCount > 0) {
            val first = fragmentManager.getBackStackEntryAt(0)
            fragmentManager.popBackStack(first.id, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
    }
}
