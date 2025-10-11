package com.devminds.casasync.utils

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.devminds.casasync.R
import com.devminds.casasync.TransitionType
import com.devminds.casasync.fragments.HomeFragment
import com.devminds.casasync.setCustomTransition

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

    // chamar teclado com delay
    fun TextView.keyboardDelay(context: Context, delay: Long) {
        if (context is Activity) {
            this.requestFocus()
            this.postDelayed({
                val imm =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
            }, delay)
        }
    }

    fun renameDialogItem(
        activity: Activity,
        itemName: String
    ): Pair<View, EditText> {
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_rename_item, null)
        val editText = dialogView.findViewById<EditText>(R.id.newNameItem)
        editText.setText(itemName)
        editText.setSelection(0, itemName.length)
        return Pair(dialogView, editText)
    }
}
