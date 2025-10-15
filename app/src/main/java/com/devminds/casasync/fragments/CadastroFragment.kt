package com.devminds.casasync.fragments

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.devminds.casasync.R
import com.devminds.casasync.TransitionType
import com.devminds.casasync.parts.User
import com.devminds.casasync.setCustomTransition
import com.devminds.casasync.utils.JsonStorageManager
import com.devminds.casasync.utils.Utils.safeShowDialog
import com.google.android.material.appbar.MaterialToolbar
import java.util.UUID
import com.devminds.casasync.utils.PopupMenu

// declaração de classe com fragmento para o cadastro
class CadastroFragment : BaseFragment(R.layout.fragment_cadastro) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // guarda os dados de cadastro
        val newUserPrompt = view.findViewById<TextView>(R.id.newUserPrompt) // nome
        val newLoginPrompt = view.findViewById<TextView>(R.id.newLoginPrompt) // login (email)
        val newPasswordPrompt = view.findViewById<TextView>(R.id.newPasswordPrompt) // senha

        var userFound: User?

        val btnCadastro = view.findViewById<TextView>(R.id.btnCadastro) // botão de cadastro

        btnCadastro.setOnClickListener {

            // transforma os dados em string
            val name = newUserPrompt.text.toString()
            val login = newLoginPrompt.text.toString()
            val password = newPasswordPrompt.text.toString()

            userFound = JsonStorageManager.recoveryUser(requireContext(), login)

            // verifica se o login já existe
            // se o login já existe, mostra a mensagem de erro
            if (userFound != null) {
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
                    .replace(R.id.fragment_container_main, LoginFragment())
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
                .replace(R.id.fragment_container_main, LoginFragment())
                .addToBackStack(null)
                .commit()
        }

        val toolbar = view.findViewById<MaterialToolbar>(R.id.topBar)

        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack() // volta para login
        }

        toolbar.inflateMenu(R.menu.topbar_menu)
        val menu = toolbar.menu // para controlar a visibilidade dos itens em toolbar
        menu.findItem(R.id.action_homepage).isVisible = false

        // lógica do menu de opções
        val menuItemView = toolbar.findViewById<View>(R.id.more_options)

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.more_options -> {
                    val menuPopup = PopupMenu.show(requireContext(), menuItemView)

                    // visibilidade dos itens em submenu
                    menuPopup.findItem(R.id.user_settings).isVisible = false
                    true
                }
                else -> false
            }
        }
    }
}
