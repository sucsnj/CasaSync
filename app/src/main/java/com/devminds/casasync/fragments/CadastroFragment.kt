package com.devminds.casasync.fragments

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.devminds.casasync.Utils.safeShowDialog
import com.google.firebase.firestore.FirebaseFirestore
import androidx.fragment.app.activityViewModels
import com.devminds.casasync.utils.Utils.safeShowDialog
import com.devminds.casasync.R
import com.devminds.casasync.TransitionType
import com.devminds.casasync.parts.House
import com.devminds.casasync.parts.User
import com.devminds.casasync.setCustomTransition
import com.devminds.casasync.utils.JsonStorageManager
import com.devminds.casasync.views.UserViewModel
import java.util.UUID
import kotlin.getValue

// declaração de classe com fragmento para o cadastro
class CadastroFragment : BaseFragment(R.layout.fragment_cadastro) {

    private val userViewModel: UserViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

            // verifica se o login já existe
            val loginEncontrado = userViewModel.user.value?.login

            // se o login já existe, mostra a mensagem de erro
            if (loginEncontrado != null) {
                safeShowDialog(getString(R.string.login_exists_message))
                return@setOnClickListener // sai da função, impedindo o cadastro
            }

            // se estiver tudo preenchido, cadastra o usuário
            if (name.isNotEmpty() && login.isNotEmpty() && password.isNotEmpty()) {
                val newUser = User(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    login = login,
                    password = password
                ) // adiciona o usuário à lista
                safeShowDialog(getString(R.string.new_account_success_message))

                // persiste o usuário em json
                JsonStorageManager.saveUser(requireContext(), newUser)

                parentFragmentManager.beginTransaction() // troca de tela para o login
                    .setCustomTransition(TransitionType.FADE)
                    .replace(R.id.fragment_container, LoginFragment())
                    .addToBackStack(null)
                    .commit()

            } else {
                safeShowDialog(getString(R.string.new_account_error_message))
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
