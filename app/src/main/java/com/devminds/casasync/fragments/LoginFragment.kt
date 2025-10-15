package com.devminds.casasync.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.devminds.casasync.HomeActivity
import com.devminds.casasync.R
import com.devminds.casasync.TransitionType
import com.devminds.casasync.utils.JsonStorageManager
import com.devminds.casasync.utils.Utils.safeShowDialog
import com.devminds.casasync.views.UserViewModel
import android.widget.LinearLayout
import androidx.fragment.app.FragmentManager
import com.devminds.casasync.utils.Utils

class LoginFragment : BaseFragment(R.layout.fragment_login) {
    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var txtLoginPrompt: TextView
    private lateinit var txtPasswordPrompt: TextView
    private lateinit var btnGoogleLogin: LinearLayout
    private lateinit var btnLogin: TextView
    private lateinit var btnCreateAccount: TextView
    private lateinit var btnForgotPassword: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // limpa o histórico de navegação
        requireActivity().supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

        // testando o firestore
//        FirestoreHelper.writeUser()
//        FirestoreHelper.readUsers()

        txtLoginPrompt = view.findViewById(R.id.txtLoginPrompt)
        txtPasswordPrompt = view.findViewById(R.id.txtPasswordPrompt)

        btnGoogleLogin = view.findViewById(R.id.btnGoogleLogin)
        btnGoogleLogin.setOnClickListener {
            Toast.makeText(
                context,
                "implementando google login",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnLogin = view.findViewById(R.id.btnLogin)
        btnLogin.setOnClickListener {

            // transforma os dados em string
            val login = txtLoginPrompt.text.toString()
            val password = txtPasswordPrompt.text.toString()

            // se estiver tudo preenchido
            if (login.isNotEmpty() && password.isNotEmpty()) {

                // carrega o usuário do json
                val userFound = JsonStorageManager.authenticateUser(requireContext(), login, password)

                // se o usuário for encontrado
                if (userFound != null) {

                    userViewModel.setUser(userFound)

                    val intent = Intent(requireContext(), HomeActivity::class.java)
                    intent.putExtra("userId", userFound.id)
                    startActivity(intent)
                    requireActivity().finish()
                    Toast.makeText(context, "Logado com sucesso!", Toast.LENGTH_SHORT).show()

                } else {
                    safeShowDialog(getString(R.string.login_error_message))
                }
            } else {
                safeShowDialog(
                    getString(R.string.login_empty_message)
                )
            }
        }

        btnCreateAccount = view.findViewById(R.id.btnCreatAccount)
        btnCreateAccount.setOnClickListener {
            Utils.replaceFragment(parentFragmentManager, CadastroFragment(), TransitionType.SLIDE)
        }

        btnForgotPassword = view.findViewById(R.id.txtForgotPassword)
        btnForgotPassword.setOnClickListener {
            Utils.replaceFragment(parentFragmentManager, RecoveryFragment(), TransitionType.SLIDE)
        }
    }
}
