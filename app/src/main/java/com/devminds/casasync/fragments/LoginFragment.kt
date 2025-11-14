package com.devminds.casasync.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
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
import com.devminds.casasync.utils.Auth
import com.devminds.casasync.utils.Utils
import kotlinx.coroutines.launch
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import androidx.core.content.edit
import com.devminds.casasync.FirestoreHelper
import com.google.firebase.auth.FirebaseUser
import com.devminds.casasync.parts.House
import com.devminds.casasync.parts.Dependent
import com.devminds.casasync.parts.Task

class LoginFragment : BaseFragment(R.layout.fragment_login) {
    private val userViewModel: UserViewModel by activityViewModels()

    private lateinit var firebaseAuth: FirebaseAuth
    private val tag = "GoogleSignIn"
    private lateinit var txtLoginPrompt: TextView
    private lateinit var txtPasswordPrompt: TextView
    private lateinit var btnGoogleLogin: LinearLayout
    private lateinit var btnLogin: TextView
    private lateinit var btnCreateAccount: TextView
    private lateinit var btnForgotPassword: TextView
    private lateinit var btnBiometricLogin: LinearLayout

    fun loginWithUserId(userId: String) {
        FirestoreHelper.getUserById(userId) { user ->
            if (user != null) {
                userViewModel.setUser(user)

                val intent = Intent(requireContext(), HomeActivity::class.java)
                intent.putExtra("userId", user.id)
                startActivity(intent)
                requireActivity().finish()
            } else {
                DialogUtils.showMessage(requireContext(), getString(R.string.login_error_message))
            }
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
                        userViewModel.persistAndSyncUser(requireContext())

                        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                        prefs.edit {
                            putString(
                                "logged_user_id",
                                userId)
                        }
                    },
                    onError = { errorMessage ->
                        DialogUtils.showMessage(context, errorMessage)
                    }
                )
            }
        }, delay)
    }

    // checa se o usuário já existe no firestore
    private fun checkAndSaveUserInFirestore(firebaseUser: FirebaseUser) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(firebaseUser.uid)

        userRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                val newUser = User(
                    id = firebaseUser.uid,
                    name = firebaseUser.displayName ?: "Usuário",
                    email = firebaseUser.email ?: "",
                    password = "",
                    houses = mutableListOf()
                )
                userRef.set(newUser).addOnSuccessListener {
                    userViewModel.setUser(newUser)
                    navigateToHome(newUser)
                }
            } else {
                val user = document.toObject(User::class.java) ?: return@addOnSuccessListener

                // salva depois de carregar as casas
                userRef.collection("houses").get().addOnSuccessListener { querySnapshot ->
                    val houses = querySnapshot.documents.mapNotNull { doc ->
                        doc.toObject(House::class.java)
                    }
                    user.houses.clear()
                    user.houses.addAll(houses)

                    // salva com casas
                    userViewModel.setUser(user)
                    userViewModel.persistAndSyncUser(requireContext())
                    navigateToHome(user)
                }
            }
        }
    }

    // autentica o usuário com o firebase
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = firebaseAuth.currentUser
                    checkAndSaveUserInFirestore(firebaseUser!!)
                    DialogUtils.showMessage(requireContext(), "Autenticação bem-sucedida!")
                    DialogUtils.dismissActiveBanner()
                } else {
                    DialogUtils.showMessage(requireContext(), "Autenticação falhou.")
                }
            }
    }

    // vai para a home
    private fun navigateToHome(user: User) {
        userViewModel.setUser(user) // insere o usuário no viewmodel
        val intent = Intent(requireContext(), HomeActivity::class.java) // leva um intent para a home
        intent.putExtra("userId", user.id) // coloca o id do usuário na intent
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
        Utils.saveUserToPrefs(requireContext(), user)
    }

    fun login(context: Context, userViewModel: UserViewModel, user: User) {
        DialogUtils.dismissActiveBanner() // elimina qualquer banner ativo

        userViewModel.setUser(user)
        userViewModel.persistAndSyncUser(context)

        val intent = Intent(context, HomeActivity::class.java)
        intent.putExtra("userId", user.id)
        startActivity(intent)
        requireActivity().finish()

        // adiciona o usuário a lista da biometria
        val biometric = Biometric()
        biometric.saveBiometricAuthUser(requireContext(), user.id)
        biometric.lastLoggedUser(requireContext(), user.id)

        Utils.saveUserToPrefs(context, user)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()
        firebaseAuth = Firebase.auth // inicializa o firebase auth

        txtLoginPrompt = view.findViewById(R.id.txtLoginPrompt)
        txtPasswordPrompt = view.findViewById(R.id.txtPasswordPrompt)

        clearNavHistory() // limpa o histórico de navegação

        Utils.checkIfUserIsLoggedIn(context)

        biometricCaller(requireActivity(), 800) // biometria

        // faz login com google
        btnGoogleLogin = view.findViewById(R.id.btnGoogleLogin)
        btnGoogleLogin.setOnClickListener {
            val credentialManager = CredentialManager.create(requireContext()) // gerenciador de credenciais

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false) // permite perguntar qual conta logar sempre (false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .build()

            // requisição de credenciais
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            // inicia a atividade de login com google
            lifecycleScope.launch {
                try {
                    val result = credentialManager.getCredential(requireActivity(), request)
                    val credential = result.credential

                    // verifica o tipo de credencial
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken // pega o token do google
                        firebaseAuthWithGoogle(idToken) // autentica o usuário com o firebase

                    } else {
                        // Lida com outros tipos de credenciais ou erros, se necessário
                        DialogUtils.showMessage(
                            requireContext(),
                            "Erro: Tipo de credencial inesperado."
                        )
                    }
                } catch (_: Exception) {
                    DialogUtils.showMessage(
                        requireContext(),
                        "Erro na autenticação com Google."
                    )
                }
            }
        }

        // botão de login por senha
        btnLogin = view.findViewById(R.id.btnLogin)
        btnLogin.setOnClickListener {

            // dados de login
            val email = txtLoginPrompt.text.toString()
            val password = txtPasswordPrompt.text.toString()

            // se estiver tudo preenchido
            if (email.isNotEmpty() && password.isNotEmpty()) {
                // usuário local
                val isConnected = Utils.isConnected(requireContext()) // verifica conexão

                // se houver conexão
                if (isConnected) {
                    Auth().authenticateWithFirestore(requireContext(), email, password) { user ->
                        if (user != null) {
                            login(context, userViewModel, user)
                            DialogUtils.showMessage(context, getString(R.string.login_success_message))
                        } else {
                            DialogUtils.showMessage(
                                requireContext(),
                                getString(R.string.login_error_message)
                            )
                        }
                    }
                } else {
                    DialogUtils.showMessage(requireContext(), "Sem conexão com a internet.")
                }
            } else {
                DialogUtils.showMessage(context, getString(R.string.login_empty_message))
            }
        }

        // vai pra tela de criar conta
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