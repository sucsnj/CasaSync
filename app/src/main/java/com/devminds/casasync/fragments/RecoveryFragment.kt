package com.devminds.casasync.fragments

import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.TextView
import com.devminds.casasync.R
import com.devminds.casasync.TransitionType
import com.devminds.casasync.parts.User
import com.devminds.casasync.utils.JsonStorageManager
import com.devminds.casasync.utils.PopupMenu
import com.devminds.casasync.utils.Utils
import com.devminds.casasync.utils.Utils.safeShowDialog
import com.google.android.material.appbar.MaterialToolbar

// declaração de classe para recuperação de senha
class RecoveryFragment : BaseFragment(R.layout.fragment_recovery) {

    private lateinit var promptChangePassword: TextView
    private lateinit var btnChangePassword: TextView
    private lateinit var txtLoginPromptRecovery: TextView
    private lateinit var btnRecovery: TextView
    private lateinit var btnCreateAccount: TextView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var menu: Menu
    private lateinit var menuItemView: View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        promptChangePassword = view.findViewById(R.id.promptChangePassword)
        promptChangePassword.visibility = View.INVISIBLE

        txtLoginPromptRecovery = view.findViewById(R.id.txtLoginPromptRecovery)

        var userFound: User? = null
        btnRecovery = view.findViewById(R.id.btnRecovery)
        btnRecovery.setOnClickListener {

            val login = txtLoginPromptRecovery.text.toString()
            userFound = JsonStorageManager.recoveryUser(requireContext(), login)

            if (login.isNotEmpty()) {
                if (userFound != null) {
                    safeShowDialog(getString(R.string.recovery_login_found_message))

                    promptChangePassword.visibility =
                        View.VISIBLE // mostra o prompt para troca de senha
                    btnChangePassword.visibility =
                        View.VISIBLE // mostra o botão para troca de senha
                } else {
                    safeShowDialog(getString(R.string.recovery_login_not_found_message))

                    promptChangePassword.visibility =
                        View.INVISIBLE // esconde o prompt para troca de senha
                    btnChangePassword.visibility =
                        View.INVISIBLE // esconde o botão para troca de senha
                }
            } else {
                safeShowDialog(getString(R.string.recovery_login_empty_message))

                promptChangePassword.visibility = View.INVISIBLE
                btnChangePassword.visibility = View.INVISIBLE
            }
        }

        btnChangePassword = view.findViewById(R.id.btnChangePassword)
        btnChangePassword.visibility = View.INVISIBLE
        btnChangePassword.setOnClickListener {

            val password = promptChangePassword.text.toString()
            if (password.isNotEmpty()) {

                userFound?.let {
                    it.password = password
                    JsonStorageManager.saveUser(requireContext(), it)
                }

                safeShowDialog(getString(R.string.recovery_password_changed_message))

                Utils.replaceFragment(parentFragmentManager, LoginFragment(), TransitionType.SLIDE)
            } else {
                safeShowDialog(getString(R.string.recovery_password_empty_message))
            }
        }

        // lógica para criar conta
        btnCreateAccount = view.findViewById(R.id.btnCreatAccount)
        btnCreateAccount.setOnClickListener {
            Utils.replaceFragment(parentFragmentManager, CadastroFragment(), TransitionType.SLIDE)
        }

        toolbar = view.findViewById(R.id.topBar)
        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack() // volta para login
        }

        toolbar.inflateMenu(R.menu.topbar_menu)
        menu = toolbar.menu // para controlar a visibilidade dos itens
        menu.findItem(R.id.action_homepage).isVisible = false

        // lógica do menu de opções
        menuItemView = toolbar.findViewById(R.id.more_options)

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
