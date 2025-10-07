package com.devminds.casasync.fragments

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import com.devminds.casasync.utils.Utils.safeShowDialog
import com.devminds.casasync.R
import com.devminds.casasync.TransitionType
import com.devminds.casasync.parts.User
import com.devminds.casasync.setCustomTransition
import com.devminds.casasync.utils.JsonStorageManager
import com.devminds.casasync.views.UserViewModel
import java.util.UUID
import kotlin.getValue

// declaração de classe para recuperação de senha
class AboutFragment : BaseFragment(R.layout.fragment_about) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // // fragmento para controlar o "sobre" da app
        // val txtIntegrante = view.findViewById<TextView>(R.id.txtIntegrante)
        // val txtGithub = view.findViewById<TextView>(R.id.txtGithub)

        // // lógica para exibir o nome do integrante
        // txtIntegrante.text = "Integrante: Google"

        // // lógica para exibir o link do github
        // txtGithub.text = "Github: https://https://www.google.com/"
    }
}
