package com.devminds.casasync.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.devminds.casasync.R
import com.devminds.casasync.TransitionType
import com.devminds.casasync.setCustomTransition
import android.widget.EditText
import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat

// passa um fragmento como parâmetro para a classe
abstract class BaseFragment(@param:LayoutRes private val layoutRes: Int) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(layoutRes, container, false)
    }

    // substitui o fragmento atual
    fun replaceFragment(fragment: Fragment, transitionType: TransitionType) {
        val transaction = parentFragmentManager.beginTransaction()
            .setCustomTransition(transitionType)
        transaction.replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    // limpa o histórico de navegação
    fun clearNavHistory() {
        requireActivity().supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    // teclado com delay num edittext
    fun delayEditText(editText: EditText, context: Context, delay: Long = 500) {
        editText.postDelayed({
            editText.requestFocus() // traz o foco

            // levanta o teclado
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
            editText.setSelection(0, editText.length()) // texto selecionado
        }, delay)
    }

    fun statusBar(color: Int) {
        requireActivity().window.statusBarColor = color
    }

    fun statusBarColor(color: String): Int {
        var statusBarColor = 0
        if (color == "white") {
            statusBarColor = ContextCompat.getColor(requireContext(), R.color.white)
        } else if (color == "notch") {
            statusBarColor = ContextCompat.getColor(requireContext(), R.color.notch)
        } else {
            statusBarColor = ContextCompat.getColor(requireContext(), R.color.black)
        }
        return statusBarColor
    }
}
