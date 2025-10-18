package com.devminds.casasync.fragments

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
import com.devminds.casasync.utils.DialogUtils

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

        clearNavHistory()

        // testando o firestore
//        FirestoreHelper.writeUser()
//        FirestoreHelper.readUsers()

        txtLoginPrompt = view.findViewById(R.id.txtLoginPrompt)
        txtPasswordPrompt = view.findViewById(R.id.txtPasswordPrompt)

        btnGoogleLogin = view.findViewById(R.id.btnGoogleLogin)
        btnGoogleLogin.setOnClickListener {
            DialogUtils.showMessage(requireContext(), "implementando google login")
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
                    DialogUtils.showMessage(requireContext(), getString(R.string.login_success_message))

                    val loadingDialog = LoadingDialogFragment()
                    loadingDialog.show(parentFragmentManager, "loading")

                    Handler(Looper.getMainLooper()).postDelayed({
                    loadingDialog.dismiss()

                    val intent = Intent(requireContext(), HomeActivity::class.java)
                    intent.putExtra("userId", userFound.id)
                    startActivity(intent)
                    requireActivity().finish()
                    }, 2000)


                } else {
                    DialogUtils.showMessage(requireContext(), getString(R.string.login_error_message))
                }
            } else {
                DialogUtils.showMessage(requireContext(), getString(R.string.login_empty_message))
            }
        }

        btnCreateAccount = view.findViewById(R.id.btnCreatAccount)
        btnCreateAccount.setOnClickListener {
            replaceFragment( CadastroFragment(), TransitionType.SLIDE)
        }

        btnForgotPassword = view.findViewById(R.id.txtForgotPassword)
        btnForgotPassword.setOnClickListener {
            replaceFragment( RecoveryFragment(), TransitionType.SLIDE)
        }
    }
}
