package com.devminds.casasync.fragments

import com.devminds.casasync.FirestoreHelper
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.TextView
import com.devminds.casasync.R
import com.devminds.casasync.TransitionType
import com.devminds.casasync.parts.User
import com.devminds.casasync.utils.DialogUtils
import com.devminds.casasync.utils.JsonStorageManager
import com.devminds.casasync.utils.PopupMenu
import com.google.android.material.appbar.MaterialToolbar
import androidx.fragment.app.activityViewModels
import com.devminds.casasync.views.UserViewModel

class RecoveryFragment : BaseFragment(R.layout.fragment_recovery) {

    private lateinit var promptChangePassword: TextView
    private lateinit var btnChangePassword: TextView
    private lateinit var txtLoginPromptRecovery: TextView
    private lateinit var btnRecovery: TextView
    private lateinit var btnCreateAccount: TextView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var menu: Menu
    private lateinit var menuItemView: View

    private val userViewModel: UserViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()

        promptChangePassword = view.findViewById(R.id.promptChangePassword)
        promptChangePassword.visibility = View.INVISIBLE // esconde o prompt para troca de senha
        txtLoginPromptRecovery = view.findViewById(R.id.txtLoginPromptRecovery)

        // variável para armazenar o usuário encontrado
        var userFound: User? = null

        btnRecovery = view.findViewById(R.id.btnRecovery)
        btnRecovery.setOnClickListener {
            val email = txtLoginPromptRecovery.text.toString()

            // verifica o email
            if (email.isNotEmpty()) {
                FirestoreHelper.getUserByEmail(email) { exists ->
                    if (exists) {
                        DialogUtils.showMessage(context, getString(R.string.recovery_login_found_message))

                        promptChangePassword.visibility =
                            View.VISIBLE // mostra o prompt para troca de senha
                        btnChangePassword.visibility =
                            View.VISIBLE // mostra o botão para troca de senha
                    } else {
                        DialogUtils.showMessage(context, getString(R.string.recovery_login_not_found_message))

                        promptChangePassword.visibility =
                            View.INVISIBLE // esconde o prompt para troca de senha
                        btnChangePassword.visibility =
                            View.INVISIBLE // esconde o botão para troca de senha
                    }
                }
            } else {
                DialogUtils.showMessage(context, getString(R.string.recovery_login_empty_message))
            }
        }

        // botão para troca de senha
        btnChangePassword = view.findViewById(R.id.btnChangePassword)
        btnChangePassword.visibility = View.INVISIBLE
        btnChangePassword.setOnClickListener {
            val password = promptChangePassword.text.toString()
            if (password.isNotEmpty()) {
                // persiste o usuário com a nova senha
                userViewModel.persistUserPassword(context, userFound, password)

                DialogUtils.showMessage(context, getString(R.string.recovery_password_changed_message))
                replaceFragment( LoginFragment(), TransitionType.SLIDE)
            } else {
                DialogUtils.showMessage(context, getString(R.string.recovery_password_empty_message))
            }
        }

        // botão para criar conta
        btnCreateAccount = view.findViewById(R.id.btnCreatAccount)
        btnCreateAccount.setOnClickListener {
            replaceFragment( CadastroFragment(), TransitionType.SLIDE)
        }

        // cabeçalho
        toolbar = view.findViewById(R.id.topBar)
        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack() // botão de voltar
        }
        // menu suspenso (3 pontos)
        toolbar.inflateMenu(R.menu.topbar_menu)
        menu = toolbar.menu // para controlar a visibilidade dos itens
        menu.findItem(R.id.action_homepage).isVisible = false

        // lógica do menu de opções
        menuItemView = toolbar.findViewById(R.id.more_options)

        // botão de menu suspenso
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.more_options -> {
                    val menuPopup = PopupMenu.show(context, menuItemView, this)

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
