package com.devminds.casasync.fragments

import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.TextView
import com.devminds.casasync.R
import com.devminds.casasync.TransitionType
import com.devminds.casasync.parts.User
import com.devminds.casasync.utils.JsonStorageManager
import com.devminds.casasync.utils.Utils.safeShowDialog
import com.google.android.material.appbar.MaterialToolbar
import java.util.UUID
import com.devminds.casasync.utils.PopupMenu

// declaração de classe com fragmento para o cadastro
class CadastroFragment : BaseFragment(R.layout.fragment_cadastro) {

    private lateinit var newUserPrompt: TextView
    private lateinit var newLoginPrompt: TextView
    private lateinit var newPasswordPrompt: TextView
    private lateinit var btnCadastro: TextView
    private lateinit var btnLoginAccount: TextView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var menu: Menu
    private lateinit var menuItemView: View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // guarda os dados de cadastro
        newUserPrompt = view.findViewById(R.id.newUserPrompt) // nome
        newLoginPrompt = view.findViewById(R.id.newLoginPrompt) // login (email)
        newPasswordPrompt = view.findViewById(R.id.newPasswordPrompt) // senha

        var userFound: User?
        btnCadastro = view.findViewById(R.id.btnCadastro) // botão de cadastro
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

                replaceFragment( LoginFragment(), TransitionType.SLIDE)

            } else {
                safeShowDialog(getString(R.string.new_account_error_message))
            }
        }

        // lógica da troca de tela para o login
        btnLoginAccount = view.findViewById(R.id.btnLoginAccount) // botão de login
        btnLoginAccount.setOnClickListener {
            replaceFragment( LoginFragment(), TransitionType.SLIDE)
        }

        toolbar = view.findViewById(R.id.topBar)
        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack() // volta para login
        }

        toolbar.inflateMenu(R.menu.topbar_menu)

        menu = toolbar.menu // para controlar a visibilidade dos itens em toolbar
        menu.findItem(R.id.action_homepage).isVisible = false

        // lógica do menu de opções
        menuItemView = toolbar.findViewById(R.id.more_options)

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.more_options -> {
                    val menuPopup = PopupMenu.show(requireContext(), menuItemView)

                    // visibilidade dos itens em submenu
                    menuPopup.findItem(R.id.user_settings).isVisible = false
                    menuPopup.findItem(R.id.app_settings).isVisible = false
                    true
                }
                else -> false
            }
        }
    }
}
