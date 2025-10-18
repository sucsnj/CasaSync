package com.devminds.casasync.fragments

import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.TextView
import com.devminds.casasync.R
import com.devminds.casasync.TransitionType
import com.devminds.casasync.parts.User
import com.devminds.casasync.utils.DialogUtils
import com.devminds.casasync.utils.JsonStorageManager
import com.google.android.material.appbar.MaterialToolbar
import java.util.UUID
import com.devminds.casasync.utils.PopupMenu

class CadastroFragment : BaseFragment(R.layout.fragment_cadastro) {

    // variáveis de classe
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

        // variáveis locais
        var userFound: User?

        // inicialização das variáveis
        newUserPrompt = view.findViewById(R.id.newUserPrompt)
        newLoginPrompt = view.findViewById(R.id.newLoginPrompt)
        newPasswordPrompt = view.findViewById(R.id.newPasswordPrompt)
        btnCadastro = view.findViewById(R.id.btnCadastro)
        btnLoginAccount = view.findViewById(R.id.btnLoginAccount)
        toolbar = view.findViewById(R.id.topBar) // cabeçalho
        menu = toolbar.menu
        menuItemView = toolbar.findViewById(R.id.more_options) // menu suspenso (3 pontos)

        // cadastro de usuário
        btnCadastro.setOnClickListener {
            // guarda os dados de cadastro
            val name = newUserPrompt.text.toString()
            val login = newLoginPrompt.text.toString()
            val password = newPasswordPrompt.text.toString()

            // verifica se o login já existe
            userFound = JsonStorageManager.recoveryUser(requireContext(), login)
            // se já existe
            if (userFound != null) {
                DialogUtils.showMessage(requireContext(), getString(R.string.login_exists_message))
                return@setOnClickListener // sai da função, impedindo o cadastro
            }

            // verifica se todos os campos obrigatórios estão preenchidos
            if (name.isNotEmpty() && login.isNotEmpty() && password.isNotEmpty()) {
                // cria o novo usuário
                val newUser = User(
                    id = UUID.randomUUID().toString(), // ID gerado aleatóriamente
                    name = name,
                    login = login,
                    password = password
                )
                DialogUtils.showMessage(requireContext(), getString(R.string.new_account_success_message))

                // persiste o usuário em json
                JsonStorageManager.saveUser(requireContext(), newUser)
                // redireciona para a página de login
                replaceFragment( LoginFragment(), TransitionType.SLIDE)
            } else {
                DialogUtils.showMessage(requireContext(), getString(R.string.new_account_error_message))
            }
        }

        // login de usuário
        btnLoginAccount.setOnClickListener {
            replaceFragment( LoginFragment(), TransitionType.SLIDE)
        }
        
        // botão de voltar
        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // infla o menu suspenso
        toolbar.inflateMenu(R.menu.topbar_menu)

        // dentro do menu, esconde o item de voltar para o início
        menu.findItem(R.id.action_homepage).isVisible = false

        // cabeçalho
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.more_options -> {
                    // exibe o menu suspenso
                    val menuPopup = PopupMenu.show(requireContext(), menuItemView, this)

                    // esconde os itens do menu suspenso
                    menuPopup.findItem(R.id.user_settings).isVisible = false // configurações do usuário
                    menuPopup.findItem(R.id.app_settings).isVisible = false // configurações do app
                    true
                }
                else -> false
            }
        }
    }
}
