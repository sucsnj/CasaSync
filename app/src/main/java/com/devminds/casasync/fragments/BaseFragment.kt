package com.devminds.casasync.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

// classe abstrata para os fragmentos

// passa um fragmento como parâmetro para a classe
// herda de fragment
abstract class BaseFragment(@param:LayoutRes private val layoutRes: Int) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(layoutRes, container, false)
    }
}