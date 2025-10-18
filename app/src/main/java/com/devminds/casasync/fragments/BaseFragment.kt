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
}