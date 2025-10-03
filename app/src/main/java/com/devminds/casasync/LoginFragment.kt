package com.devminds.casasync

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.devminds.casasync.Utils.safeShowDialog

class LoginFragment : BaseFragment(R.layout.fragment_login) {

    val userList = User.users

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // guarda os dados de login e senha
        val txtLoginPrompt = view.findViewById<TextView>(R.id.txtLoginPrompt)
        val txtPasswordPrompt = view.findViewById<TextView>(R.id.txtPasswordPrompt)

        // representa o botão de login
        val btnLogin = view.findViewById<TextView>(R.id.btnLogin)

        // o que fazer ao clicar no botão de login
        btnLogin.setOnClickListener {

            // transforma os dados em string
            val login = txtLoginPrompt.text.toString()
            val password = txtPasswordPrompt.text.toString()

            // se estiver tudo preenchido
            if (login.isNotEmpty() && password.isNotEmpty()) {

                // pega o usuário com os dados de login e senha
                val userFound = userList.find {
                    it.login == login && it.password == password
                }

                // se o usuário for encontrado
                if (userFound != null) {

                    context?.let {
                        safeShowDialog(getString(R.string.login_success_message))
                    }

                    // leva informação do usuário logado para a HomeFragment()
                    val bundle = Bundle().apply {
                        putString("username", userFound.name)
                    }

                    val fragment = HomeFragment()
                    fragment.arguments = bundle

                    parentFragmentManager.beginTransaction()
                        .setCustomTransition(TransitionType.SLIDE)

                        .replace(R.id.fragment_container, fragment)
                        .commit() // finaliza a transação
                } else {
                    // mostra a mensagem de erro
                    safeShowDialog(getString(R.string.login_error_message))
                }
            } else {
                safeShowDialog(
                    getString(R.string.login_empty_message)
                )
            }
        }

        // representa o botão de criar conta
        val btnCreateAccount = view.findViewById<TextView>(R.id.btnCreatAccount)

        // o que fazer ao clicar no botão de criar conta
        btnCreateAccount.setOnClickListener {

            // troca de tela para o cadastro
            parentFragmentManager.beginTransaction()
                .setCustomTransition(TransitionType.SLIDE)

                // muda o fragmento (tela)
                .replace(R.id.fragment_container, CadastroFragment())
                .addToBackStack(null) // guarda a tela atual para voltar quando precisar
                .commit() // finaliza a transação
        }

        // representa o botão de recuperar senha
        val btnForgotPassword = view.findViewById<TextView>(R.id.txtForgotPassword)

        // o que fazer ao clicar no botão de recuperar senha
        btnForgotPassword.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomTransition(TransitionType.SLIDE) // animação de troca de tela
                .replace(R.id.fragment_container, RecoveryFragment())
                .addToBackStack(null) // guarda a tela atual para voltar quando precisar
                .commit() // finaliza a transação
        }
    }
}
