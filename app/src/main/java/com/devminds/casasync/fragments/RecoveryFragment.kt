package com.devminds.casasync.fragments

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import com.devminds.casasync.utils.Utils.safeShowDialog
import com.devminds.casasync.R
import com.devminds.casasync.TransitionType
import com.devminds.casasync.parts.User
import com.devminds.casasync.setCustomTransition
import com.devminds.casasync.utils.JsonStorageManager
import com.devminds.casasync.views.UserViewModel
import java.util.UUID
import kotlin.getValue

// declaração de classe para recuperação de senha
class RecoveryFragment : BaseFragment(R.layout.fragment_recovery) {

    private val userViewModel: UserViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // esconde o prompt para trocar senha
        val promptChangePassword = view.findViewById<TextView>(R.id.promptChangePassword)
        val btnChangePassword = view.findViewById<TextView>(R.id.btnChangePassword)
        promptChangePassword.visibility = View.INVISIBLE
        btnChangePassword.visibility = View.INVISIBLE

        val txtLoginPromptRecovery = view.findViewById<TextView>(R.id.txtLoginPromptRecovery)
        val btnRecovery = view.findViewById<TextView>(R.id.btnRecovery)

        var userFound: User? = null

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

        // lógica para trocar senha
        btnChangePassword.setOnClickListener {

            val password = promptChangePassword.text.toString()
            if (password.isNotEmpty()) {

                userFound?.let {
                    it.password = password
                    JsonStorageManager.saveUser(requireContext(), it)
                }

                safeShowDialog(getString(R.string.recovery_password_changed_message))

                parentFragmentManager.beginTransaction()
                    .setCustomTransition(TransitionType.SLIDE)
                    .replace(R.id.fragment_container_main, LoginFragment())
                    .addToBackStack(null)
                    .commit()
            } else {
                safeShowDialog(getString(R.string.recovery_password_empty_message))
            }
        }

        // lógica para criar conta
        val btnCreateAccount = view.findViewById<TextView>(R.id.btnCreatAccount)

        btnCreateAccount.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomTransition(TransitionType.SLIDE)
                .replace(R.id.fragment_container_main, CadastroFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}
