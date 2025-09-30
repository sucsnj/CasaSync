package com.devminds.casasync

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.devminds.casasync.Utils.safeShowDialog
import com.devminds.casasync.R

// declaração de classe com fragmento para o cadastro
class CadastroFragment : BaseFragment(R.layout.fragment_cadastro) {

    // banco de dados (em memória)
    companion object {
        var users = mutableListOf<User>() // lista de usuários (arrayList)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // lógica para guardar os cadastros (em memória)

        // guarda os dados de cadastro
        val newUserPrompt = view.findViewById<TextView>(R.id.newUserPrompt) // nome
        val newLoginPrompt = view.findViewById<TextView>(R.id.newLoginPrompt) // login (email)
        val newPasswordPrompt = view.findViewById<TextView>(R.id.newPasswordPrompt) // senha

        val btnCadastro = view.findViewById<TextView>(R.id.btnCadastro) // botão de cadastro

        btnCadastro.setOnClickListener {

            // transforma os dados em string
            val name = newUserPrompt.text.toString()
            val login = newLoginPrompt.text.toString()
            val password = newPasswordPrompt.text.toString()

            // verificação contra duplicidade de cadastro

            // verifica se o login já existe
            val loginEncontrado = users.find { it.login == login }

            // se o login já existe, mostra a mensagem de erro
            if (loginEncontrado != null) {
                safeShowDialog(getString(R.string.login_found_message))
                return@setOnClickListener // sai da função, impedindo o cadastro
            }

            // se estiver tudo preenchido, cadastra o usuário
            if (name.isNotEmpty() && login.isNotEmpty() && password.isNotEmpty()) {
                users.add(User(name, login, password)) // adiciona o usuário à lista
                safeShowDialog(getString(R.string.cadastro_success_message))

                parentFragmentManager.beginTransaction() // troca de tela para o login
                    .setCustomTransition(TransitionType.FADE)
                    .replace(R.id.fragment_container, LoginFragment())
                    .addToBackStack(null)
                    .commit()

            } else {
                safeShowDialog(getString(R.string.cadastro_error_message))
            }
        }

        // lógica da troca de tela para o login
        val btnLoginAccount = view.findViewById<TextView>(R.id.btnLoginAccount) // botão de login

        btnLoginAccount.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomTransition(TransitionType.FADE)
                .replace(R.id.fragment_container, LoginFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}
