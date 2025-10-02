package com.devminds.casasync

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.devminds.casasync.Utils.safeShowDialog
import com.google.firebase.firestore.FirebaseFirestore

class LoginFragment : BaseFragment(R.layout.fragment_login) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val txtLoginPrompt = view.findViewById<TextView>(R.id.txtLoginPrompt)

        val btnLogin = view.findViewById<TextView>(R.id.btnLogin)
        btnLogin.setOnClickListener {

            val login = txtLoginPrompt.text.toString().trim()

            if (login.isNotEmpty()) {

                val db = FirebaseFirestore.getInstance()
                val usersCollection = db.collection("usuarios")

                usersCollection.whereEqualTo("nome", login).get()
                    .addOnSuccessListener { result ->
                        if (!result.isEmpty) {
                            safeShowDialog(getString(R.string.login_found_message))

                            parentFragmentManager.beginTransaction()
                                .setCustomTransition(TransitionType.FADE)
                                .replace(R.id.fragment_container, HomeFragment())
                                .addToBackStack(null)
                                .commit()
                        } else {
                            safeShowDialog(getString(R.string.login_error_message))
                        }
                    }
                    .addOnFailureListener { e ->
                        safeShowDialog(getString(R.string.login_error_message)) // TODO strings
                    }
            } else {
                safeShowDialog(getString(R.string.login_empty_message))
            }


            val btnCreateAccount = view.findViewById<TextView>(R.id.btnCreatAccount)
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
}