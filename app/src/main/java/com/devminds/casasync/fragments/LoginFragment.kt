package com.devminds.casasync.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.devminds.casasync.HomeActivity
import com.devminds.casasync.R
import com.devminds.casasync.TransitionType
import com.devminds.casasync.parts.User
import com.devminds.casasync.views.UserViewModel
import android.widget.LinearLayout
import androidx.fragment.app.FragmentActivity
import com.devminds.casasync.utils.Biometric
import com.devminds.casasync.utils.BiometricAuthManager
import com.devminds.casasync.utils.DialogUtils
import com.devminds.casasync.utils.JsonStorageManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import android.os.Handler
import android.os.Looper
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

class LoginFragment : BaseFragment(R.layout.fragment_login) {
    private val userViewModel: UserViewModel by activityViewModels()

    private lateinit var firebaseAuth: FirebaseAuth
    private val TAG = "GoogleSignIn"
    private lateinit var txtLoginPrompt: TextView
    private lateinit var txtPasswordPrompt: TextView
    private lateinit var btnGoogleLogin: LinearLayout
    private lateinit var btnLogin: TextView
    private lateinit var btnCreateAccount: TextView
    private lateinit var btnForgotPassword: TextView
    private lateinit var btnBiometricLogin: LinearLayout

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

    // chama o menu de biometria
    private fun biometricCaller(context: Context, delay: Long) {
        // delay para chamar a biometria
        Handler(Looper.getMainLooper()).postDelayed({
            // chama a biometria
            if (BiometricAuthManager.canUseBiometric(context)) { // se puder usar biometria, então...
                BiometricAuthManager.tryBiometricLogin(
                    context,
                    (context as? Activity ?: return@postDelayed) as FragmentActivity,
                    onSuccess = { userId ->
                        loginWithUserId(userId)
                    },
                    onError = { errorMessage ->
                        DialogUtils.showMessage(context, errorMessage)
                    }
                )
            }
        }, delay)
    }

    private fun checkAndSaveUserInFirestore(firebaseUser: com.google.firebase.auth.FirebaseUser) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(firebaseUser.uid)

        userRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                Log.d(TAG, "Usuário não encontrado. Criando novo registro no Firestore.")
                val newUser = User(
                    id = firebaseUser.uid,
                    name = firebaseUser.displayName ?: "Usuário",
                    login = firebaseUser.email ?: "",
                    password = "", // Senha não é necessária
                    houses = mutableListOf()
                )
                userRef.set(newUser)
                    .addOnSuccessListener {
                        Log.d(TAG, "Novo usuário salvo no Firestore.")
                        navigateToHome(newUser)
                        JsonStorageManager.saveUser(requireContext(), newUser)
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Erro ao salvar novo usuário no Firestore", e)
                    }
            } else {
                Log.d(TAG, "Usuário já existe no Firestore.")
                val existingUser = document.toObject(User::class.java)!!
                navigateToHome(existingUser)
                JsonStorageManager.saveUser(requireContext(), existingUser)
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "Erro ao buscar usuário no Firestore", e)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = firebaseAuth.currentUser
                    Log.d(TAG, "Sucesso ao autenticar no Firebase: ${firebaseUser?.displayName}")
                    checkAndSaveUserInFirestore(firebaseUser!!)
                } else {
                    Log.w(TAG, "Falha na autenticação com Firebase", task.exception)
                    Toast.makeText(requireContext(), "Falha na autenticação.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    private fun navigateToHome(user: User) {
        userViewModel.setUser(user)
        val intent = Intent(requireContext(), HomeActivity::class.java)
        intent.putExtra("userId", user.id)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()
        firebaseAuth = Firebase.auth

        txtLoginPrompt = view.findViewById(R.id.txtLoginPrompt)
        txtPasswordPrompt = view.findViewById(R.id.txtPasswordPrompt)

        clearNavHistory()
        biometricCaller(requireActivity(), 800)

        btnGoogleLogin = view.findViewById(R.id.btnGoogleLogin)
        btnGoogleLogin.setOnClickListener {
            val credentialManager = CredentialManager.create(requireContext())

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            lifecycleScope.launch {
                try {
                    val result = credentialManager.getCredential(requireActivity(), request)
                    val credential = result.credential

                    // CORREÇÃO PRINCIPAL: Use GoogleIdTokenCredential
                    // e o metodo de fábrica createFrom()
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken

                        Log.d(TAG, "Token do Google obtido com sucesso")
                        firebaseAuthWithGoogle(idToken)

                    } else {
                        // Lida com outros tipos de credenciais ou erros, se necessário
                        Log.w(TAG, "Tipo de credencial inesperado: ${credential.type}")
                        Toast.makeText(
                            requireContext(),
                            "Erro: Tipo de credencial inesperado.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Erro na autenticação com CredentialManager", e)
                    Toast.makeText(
                        requireContext(),
                        "Erro na autenticação com Google.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
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
                val userFound =
                    JsonStorageManager.authenticateUser(requireContext(), login, password)

                // se o usuário for encontrado
                if (userFound != null) {
                    DialogUtils.dismissActiveBanner() // elimina qualquer banner ativo

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
                    DialogUtils.showMessage(
                        requireContext(),
                        getString(R.string.login_error_message)
                    )
                }
            } else {
                DialogUtils.showMessage(context, getString(R.string.login_empty_message))
            }
        }

        // botão de criar conta
        btnCreateAccount = view.findViewById(R.id.btnCreatAccount)
        btnCreateAccount.setOnClickListener {
            replaceFragment(CadastroFragment(), TransitionType.SLIDE)
        }

        // botão de recuperar senha
        btnForgotPassword = view.findViewById(R.id.txtForgotPassword)
        btnForgotPassword.setOnClickListener {
            replaceFragment(RecoveryFragment(), TransitionType.SLIDE)
        }

        // botão para biometria
        btnBiometricLogin = view.findViewById(R.id.btnBiometricLogin)
        btnBiometricLogin.setOnClickListener {
            biometricCaller(context, 100)
        }
    }
}