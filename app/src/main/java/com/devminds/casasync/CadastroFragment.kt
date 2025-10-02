package com.devminds.casasync

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.devminds.casasync.Utils.safeShowDialog
import com.google.firebase.firestore.FirebaseFirestore

class CadastroFragment : BaseFragment(R.layout.fragment_cadastro) {

    // permanece aqui para evitar erros de inicialização (será removido em breve)
    companion object {
        val users = mutableListOf<User>()
    }

    // lógica real
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // guarda os dados de cadastro
        val nomeEditText = view.findViewById<TextView>(R.id.newUserPrompt)

        val btnCadastro = view.findViewById<TextView>(R.id.btnCadastro)
        btnCadastro.setOnClickListener {

            // o trim() remove espaços em branco nas extremidades da string
            val nomeDigitado = nomeEditText.text.toString().trim()

//                return@setOnClickListener // sai da função, impedindo o cadastro

            if (nomeDigitado.isNotEmpty()) {
                val db = FirebaseFirestore.getInstance() // cria instância do banco de dados
                val usuario = hashMapOf("nome" to nomeDigitado)

                db.collection("usuarios").add(usuario)
                    .addOnSuccessListener { documentReference ->
                        safeShowDialog(getString(R.string.cadastro_success_message))

                        parentFragmentManager.beginTransaction() // troca de tela para o login
                            .setCustomTransition(TransitionType.FADE)
                            .replace(R.id.fragment_container, LoginFragment())
                            .addToBackStack(null)
                            .commit()
                    }
                    .addOnFailureListener { e ->
                        safeShowDialog(getString(R.string.cadastro_error_message))
                    }

            } else {
                safeShowDialog(getString(R.string.cadastro_empty_message))
            }

            val btnLoginAccount = view.findViewById<TextView>(R.id.btnLoginAccount)
            btnLoginAccount.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .setCustomTransition(TransitionType.FADE)
                    .replace(R.id.fragment_container, LoginFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }
    }
}