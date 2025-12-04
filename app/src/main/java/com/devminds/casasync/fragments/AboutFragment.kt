package com.devminds.casasync.fragments

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import com.devminds.casasync.R
import com.devminds.casasync.views.EasterEggViewModel
import com.google.android.material.appbar.MaterialToolbar

class AboutFragment : BaseFragment(R.layout.fragment_about) {

    private lateinit var toolbar: MaterialToolbar

    private val viewModel: EasterEggViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.easterEggActive.observe(viewLifecycleOwner) { easterEgg ->
            if (easterEgg) {
                view.findViewById<TextView>(R.id.txtAboutDevs).text = getString(R.string.ainulindale)
                view.findViewById<TextView>(R.id.txtAboutApp).text = getString(R.string.valaquenta)
                viewModel.easterEggActive.value = false
            }
        }

        // toolbar - cabeçalho
        toolbar = view.findViewById(R.id.topBar)
        // botão de voltar
        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}
