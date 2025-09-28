package com.example.casasync

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.example.casasync.SnackbarUtils.showMessage

class CadastroFragment : BaseFragment(R.layout.fragment_cadastro) {

    // banco de dados (em memória)
    companion object {
        var users = mutableListOf<User>()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // lógica para guardar os cadastros (em memória)
        val newUserPrompt = view.findViewById<TextView>(R.id.newUserPrompt)
        val newLoginPrompt = view.findViewById<TextView>(R.id.newLoginPrompt)
        val newPasswordPrompt = view.findViewById<TextView>(R.id.newPasswordPrompt)

        val btnCadastro = view.findViewById<TextView>(R.id.btnCadastro)

        btnCadastro.setOnClickListener {
            val name = newUserPrompt.text.toString()
            val login = newLoginPrompt.text.toString()
            val password = newPasswordPrompt.text.toString()

            // verificação contra duplicidade de cadastro
            // se newLoginPrompt for encontrado em User
            val loginEncontrado = users.find { it.login == login }
            if (loginEncontrado != null) {
                showMessage(requireContext(),
                    getString(R.string.login_found_message),
                    R.drawable.casasync)
                return@setOnClickListener
            }

            if (name.isNotEmpty() && login.isNotEmpty() && password.isNotEmpty()) {
                users.add(User(name, login, password))
                showMessage(requireContext(),
                    getString(R.string.cadastro_success_message),
                    R.drawable.casasync)

                parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_left,
                    R.anim.slide_out_right,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
                .replace(R.id.fragment_container, LoginFragment())
                .addToBackStack(null)
                .commit()

            } else {
               showMessage(requireContext(),
                    getString(R.string.cadastro_error_message),
                    R.drawable.casasync)
            }
        }

        // lógica da troca de tela para o login
        val btnLoginAccount = view.findViewById<TextView>(R.id.btnLoginAccount)

        btnLoginAccount.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_left,
                    R.anim.slide_out_right,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
                .replace(R.id.fragment_container, LoginFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}
