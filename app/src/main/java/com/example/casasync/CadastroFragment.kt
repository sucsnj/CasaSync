package com.example.casasync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.TextView
import android.widget.Toast

class CadastroFragment : BaseFragment(R.layout.fragment_cadastro) {

    // banco de dados (em memória)
    companion object {
        var users = mutableListOf<User>()
    }

    // classe que representa um usuário (em memória)
    data class User(val name: String, val login: String, val password: String)

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

            if (name.isNotEmpty() && login.isNotEmpty() && password.isNotEmpty()) {
                users.add(User(name, login, password))
                Toast.makeText(requireContext(), getString(R.string.cadastro_success_message), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), getString(R.string.cadastro_error_message), Toast.LENGTH_SHORT).show()
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
