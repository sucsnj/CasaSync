package com.devminds.casasync.fragments

import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.TextView
import com.devminds.casasync.FirestoreHelper
import com.devminds.casasync.R
import com.devminds.casasync.TransitionType
import com.devminds.casasync.parts.User
import com.devminds.casasync.utils.Auth
import com.devminds.casasync.utils.Biometric
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

        val context = requireContext()

        // inicialização das variáveis
        newUserPrompt = view.findViewById(R.id.newUserPrompt)
        newLoginPrompt = view.findViewById(R.id.newLoginPrompt)
        newPasswordPrompt = view.findViewById(R.id.newPasswordPrompt)
        btnLoginAccount = view.findViewById(R.id.btnLoginAccount)
        toolbar = view.findViewById(R.id.topBar) // cabeçalho

        // cadastro de usuário
        btnCadastro = view.findViewById(R.id.btnCadastro)
        btnCadastro.setOnClickListener {
            // dados de cadastro com google

            val name = newUserPrompt.text.toString()
            val email = newLoginPrompt.text.toString()
            val password = newPasswordPrompt.text.toString()

            // cria um hash 256 para a senha
            val hashedPassword = Auth().hashPassword(password)

            // vai no firestore procurar um email igual
            FirestoreHelper.getUserByEmail(email) { exists ->
                if (exists) {
                    DialogUtils.showMessage(context, "Email já cadastrado")
                } else {
                    // verifica se todos os campos obrigatórios estão preenchidos
                    if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                        // cria o novo usuário
                        FirestoreHelper.createUser(name, email, hashedPassword) { newUser ->
                            if (newUser != null) {
                                DialogUtils.showMessage(
                                    context,
                                    getString(R.string.new_account_success_message)
                                )
                                // persiste o usuário no firestore
                                FirestoreHelper.syncUserToFirestore(newUser)
                                // redireciona para a página de login
                                replaceFragment( LoginFragment(), TransitionType.SLIDE)
                            } else {
                                DialogUtils.showMessage(
                                    context,
                                    getString(R.string.new_account_error_message)
                                )
                            }
                        }
                    } else {
                        DialogUtils.showMessage(context, "Preencha todos os campos")
                    }
                }
            }
        }

        // volta pra tela de login
        btnLoginAccount.setOnClickListener {
            replaceFragment( LoginFragment(), TransitionType.SLIDE)
        }
        
        // botão de voltar
        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        toolbar.inflateMenu(R.menu.topbar_menu) // infla o menu suspenso
        menu = toolbar.menu

        // dentro do menu, esconde o item de voltar para o início
        menu.findItem(R.id.action_homepage).isVisible = false

        menuItemView = toolbar.findViewById(R.id.more_options) // menu suspenso (3 pontos)

        // cabeçalho
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.more_options -> {
                    // exibe o menu suspenso
                    val menuPopup = PopupMenu.show(context, menuItemView, this)

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
