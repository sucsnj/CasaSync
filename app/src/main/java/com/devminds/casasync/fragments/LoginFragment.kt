package com.devminds.casasync.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.devminds.casasync.utils.Utils.safeShowDialog
import androidx.fragment.app.activityViewModels
import com.devminds.casasync.FirestoreHelper
import com.devminds.casasync.HomeActivity
import com.devminds.casasync.R
import com.devminds.casasync.TransitionType
import com.devminds.casasync.parts.User
import com.devminds.casasync.views.UserViewModel
import com.devminds.casasync.setCustomTransition
import com.devminds.casasync.utils.JsonStorageManager
import com.devminds.casasync.utils.Utils

class LoginFragment : BaseFragment(R.layout.fragment_login) {
    private val userViewModel: UserViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // testando o firestore
//        FirestoreHelper.escreverUsuario()
//        FirestoreHelper.lerUsuarios()

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

                // carrega o usuário do json
                val userFound = JsonStorageManager.authenticateUser(requireContext(), login, password)

                // se o usuário for encontrado
                if (userFound != null) {

                    userViewModel.setUser(userFound)

                    val intent = Intent(requireContext(), HomeActivity::class.java)
                    intent.putExtra("userId", userFound.id)
                    startActivity(intent)
                    requireActivity().finish()

                } else {
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
                .replace(R.id.fragment_container_main, CadastroFragment())
                .addToBackStack(null) // guarda a tela atual para voltar quando precisar
                .commit() // finaliza a transação
        }

        // representa o botão de recuperar senha
        val btnForgotPassword = view.findViewById<TextView>(R.id.txtForgotPassword)

        // o que fazer ao clicar no botão de recuperar senha
        btnForgotPassword.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomTransition(TransitionType.SLIDE) // animação de troca de tela
                .replace(R.id.fragment_container_main, RecoveryFragment())
                .addToBackStack(null) // guarda a tela atual para voltar quando precisar
                .commit() // finaliza a transação
        }
    }
}
