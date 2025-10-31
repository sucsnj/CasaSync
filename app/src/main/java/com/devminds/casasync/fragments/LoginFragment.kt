package com.devminds.casasync.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import com.devminds.casasync.HomeActivity
import com.devminds.casasync.R
import com.devminds.casasync.TransitionType
import com.devminds.casasync.utils.JsonStorageManager
import com.devminds.casasync.views.UserViewModel
import android.widget.LinearLayout
import com.devminds.casasync.utils.Biometric
import com.devminds.casasync.utils.BiometricAuthManager
import com.devminds.casasync.utils.DialogUtils

class LoginFragment : BaseFragment(R.layout.fragment_login) {
    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var txtLoginPrompt: TextView
    private lateinit var txtPasswordPrompt: TextView
    private lateinit var btnGoogleLogin: LinearLayout
    private lateinit var btnLogin: TextView
    private lateinit var btnCreateAccount: TextView
    private lateinit var btnForgotPassword: TextView

    private fun loginWithUserId(userId: String) {
        val userFound = JsonStorageManager.getUserById(requireContext(), userId)

        if (userFound != null) {
            userViewModel.setUser(userFound)

            val intent = Intent(requireContext(), HomeActivity::class.java)
            intent.putExtra("userId", userFound.id)
            startActivity(intent)
            requireActivity().finish()

        } else {
            DialogUtils.showMessage(requireContext(), getString(R.string.login_error_message))
        }
    }

    private fun biometricCaller(context: Context) {
        // delay para chamar a biometria
        Handler(Looper.getMainLooper()).post {
            // chama a biometria
            if (BiometricAuthManager.canUseBiometric(context)) { // se puder usar biometria, então...
                BiometricAuthManager.tryBiometricLogin(
                    requireContext(),
                    requireActivity(),
                    onSuccess = { userId ->
                        loginWithUserId(userId)
                    },
                    onError = { errorMessage ->
                        DialogUtils.showMessage(context, errorMessage)
                    }
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()
        // limpa o histórico de navegação
        clearNavHistory()
        biometricCaller(context)

        // botão para biometria TODO

        txtLoginPrompt = view.findViewById(R.id.txtLoginPrompt)
        txtPasswordPrompt = view.findViewById(R.id.txtPasswordPrompt)
        // botão de login com google
        btnGoogleLogin = view.findViewById(R.id.btnGoogleLogin)
        btnGoogleLogin.setOnClickListener {
            DialogUtils.showMessage(context, "implementando google login")
        }
        // botão de login
        btnLogin = view.findViewById(R.id.btnLogin)
        btnLogin.setOnClickListener {

            // transforma os dados em string
            val login = txtLoginPrompt.text.toString()
            val password = txtPasswordPrompt.text.toString()

            // se estiver tudo preenchido
            if (login.isNotEmpty() && password.isNotEmpty()) {

                // carrega o usuário do json
                val userFound = JsonStorageManager.authenticateUser(context, login, password)

                // se o usuário for encontrado
                if (userFound != null) {
                    userViewModel.setUser(userFound)

                    val intent = Intent(context, HomeActivity::class.java)
                    intent.putExtra("userId", userFound.id)
                    startActivity(intent)
                    requireActivity().finish()

                    // adiciona o usuário a lista da biometria
                    val biometric = Biometric()
                    biometric.saveBiometricAuthUser(requireContext(), userFound.id)
                    biometric.lastLoggedUser(requireContext(), userFound.id)

                } else {
                    DialogUtils.showMessage(context, getString(R.string.login_error_message))
                }
            } else {
                DialogUtils.showMessage(context, getString(R.string.login_empty_message))
            }
        }
        // botão de criar conta
        btnCreateAccount = view.findViewById(R.id.btnCreatAccount)
        btnCreateAccount.setOnClickListener {
            replaceFragment( CadastroFragment(), TransitionType.SLIDE)
        }
        // botão de recuperar senha
        btnForgotPassword = view.findViewById(R.id.txtForgotPassword)
        btnForgotPassword.setOnClickListener {
            replaceFragment( RecoveryFragment(), TransitionType.SLIDE)
        }
    }
}
