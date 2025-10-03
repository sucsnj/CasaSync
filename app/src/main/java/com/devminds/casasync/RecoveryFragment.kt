package com.devminds.casasync

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.devminds.casasync.Utils.safeShowDialog
import com.devminds.casasync.R

// declaração de classe para recuperação de senha
class RecoveryFragment : BaseFragment(R.layout.fragment_recovery) {

    val userList = User.users

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // esconde o prompt para trocar senha
        val promptChangePassword = view.findViewById<TextView>(R.id.promptChangePassword)
        val btnChangePassword = view.findViewById<TextView>(R.id.btnChangePassword)
        promptChangePassword.visibility = View.INVISIBLE
        btnChangePassword.visibility = View.INVISIBLE

        val txtLoginPromptRecovery = view.findViewById<TextView>(R.id.txtLoginPromptRecovery)
        val btnRecovery = view.findViewById<TextView>(R.id.btnRecovery)

        var loginEncontrado: User? = null

        btnRecovery.setOnClickListener {

            val login = txtLoginPromptRecovery.text.toString()
            loginEncontrado = userList.find { it.login == login }

            if (login.isNotEmpty()) {
                if (loginEncontrado != null) {
                    safeShowDialog(getString(R.string.login_found_recovery))

                    promptChangePassword.visibility =
                        View.VISIBLE // mostra o prompt para troca de senha
                    btnChangePassword.visibility =
                        View.VISIBLE // mostra o botão para troca de senha
                } else {
                    safeShowDialog(getString(R.string.login_not_found_recovery))

                    promptChangePassword.visibility =
                        View.INVISIBLE // esconde o prompt para troca de senha
                    btnChangePassword.visibility =
                        View.INVISIBLE // esconde o botão para troca de senha
                }
            } else {
                safeShowDialog(getString(R.string.login_empty_recovery))

                promptChangePassword.visibility = View.INVISIBLE
                btnChangePassword.visibility = View.INVISIBLE
            }
        }

        // lógica para trocar senha
        btnChangePassword.setOnClickListener {

            val password = promptChangePassword.text.toString()
            if (password.isNotEmpty()) {
                loginEncontrado?.password = password
                safeShowDialog(getString(R.string.password_changed))

                parentFragmentManager.beginTransaction()
                    .setCustomTransition(TransitionType.SLIDE)
                    .replace(R.id.fragment_container, LoginFragment())
                    .addToBackStack(null)
                    .commit()
            } else {
                safeShowDialog(getString(R.string.password_empty_recovery))
            }
        }

        // lógica para criar conta
        val btnCreateAccount = view.findViewById<TextView>(R.id.btnCreatAccount)

        btnCreateAccount.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomTransition(TransitionType.SLIDE)
                .replace(R.id.fragment_container, CadastroFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}
