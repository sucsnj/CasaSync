package com.example.casasync

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.casasync.SnackbarUtils
import com.example.casasync.SnackbarUtils.showMessage

// lógica de inflação de fragmento já na declaração de classe
class LoginFragment : BaseFragment(R.layout.fragment_login) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val txtLoginPrompt = view.findViewById<TextView>(R.id.txtLoginPrompt)
        val txtPasswordPrompt = view.findViewById<TextView>(R.id.txtPasswordPrompt)

        val btnLogin = view.findViewById<TextView>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val login = txtLoginPrompt.text.toString()
            val password = txtPasswordPrompt.text.toString()

            if (login.isNotEmpty() && password.isNotEmpty()) {
                val userFound = CadastroFragment.users.find {
                    it.login == login && it.password == password
                }

                if (userFound != null) {
                    showMessage(requireContext(),
                        getString(R.string.login_success_message),
                        R.drawable.casasync)

                    parentFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                    )
                    .replace(R.id.fragment_container, HomeFragment()) // ou qualquer outro fragmento
                    .addToBackStack(null)
                    .commit()
                } else {
                    showMessage(requireContext(),
                        getString(R.string.login_error_message),
                        R.drawable.casasync)
                }
            } else {
                showMessage(requireContext(),
                    getString(R.string.login_empty_message),
                    R.drawable.casasync)
            }
        }

        val btnCreateAccount = view.findViewById<TextView>(R.id.btnCreatAccount)

        btnCreateAccount.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, CadastroFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}
