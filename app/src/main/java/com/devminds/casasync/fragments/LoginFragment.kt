package com.devminds.casasync.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
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
import com.devminds.casasync.parts.Dependent
import com.google.firebase.auth.FirebaseUser
import com.devminds.casasync.parts.House
import com.devminds.casasync.utils.Animations
import com.devminds.casasync.views.DependentViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Suppress("DEPRECATION") // @(TODO até o google implentar o CM completo...)
class LoginFragment : BaseFragment(R.layout.fragment_login) {

    private val userViewModel: UserViewModel by activityViewModels()
    private val dependentViewModel: DependentViewModel by activityViewModels()
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001
    private lateinit var txtLoginPrompt: TextView
    private lateinit var txtPasswordPrompt: TextView
    private lateinit var btnGoogleLogin: LinearLayout
    private lateinit var btnLogin: TextView
    private lateinit var btnCreateAccount: TextView
    private lateinit var btnForgotPassword: TextView
    private lateinit var btnBiometricLogin: LinearLayout
    private lateinit var startAppOverlay: View
    private lateinit var loadingImage: ImageView

    fun startingAppLogo(show: Boolean) {
        if (show) {
            setStatusBarColor(requireActivity().window, statusBarColor("white"))
            startAppOverlay.visibility = View.VISIBLE
            Animations.startInflateAndShrink(loadingImage)
        } else {
            Animations.stopInflateAnimation()
            startAppOverlay.visibility = View.GONE
        }
    }

    // faz login com o id do usuário
    fun loginWithUserId(userId: String) {
        FirestoreHelper.getUserById(userId) { user ->
            if (user != null) {
                userViewModel.setUser(user)

                val intent = Intent(requireContext(), HomeActivity::class.java)
                intent.putExtra("userId", user.id) // manda o id do usuário
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
                        if (Utils.isConnected(requireContext())) {
                            loginWithUserId(userId)
                            userViewModel.persistAndSyncUser()

                            val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                            prefs.edit {
                                putString(
                                    "logged_user_id",
                                    userId)
                            }
                        } else {
                            DialogUtils.showMessage(context, getString(R.string.no_connection))
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
        val usersRef = db.collection("users")

        usersRef.whereEqualTo("email", firebaseUser.email).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {

                    val existingUserDoc = querySnapshot.documents.first()
                    val existingUser = existingUserDoc.toObject(User::class.java)

                    existingUser?.apply {
                        name = firebaseUser.displayName ?: name
                        photoUrl = firebaseUser.photoUrl?.toString() ?: photoUrl
                    }

                    usersRef.document(existingUserDoc.id).set(existingUser!!)
                    userViewModel.setUser(existingUser)
                    navigateToHome(existingUser)

                    val biometric = Biometric()
                    biometric.saveBiometricAuthUser(requireContext(), existingUser.id)
                    biometric.lastLoggedUser(requireContext(), existingUser.id)

                } else {

                    val newUser = User(
                        id = firebaseUser.uid,
                        name = firebaseUser.displayName ?: "Usuário",
                        email = firebaseUser.email ?: "",
                        password = "",
                        photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                        houses = mutableListOf()
                    )
                    usersRef.document(firebaseUser.uid).set(newUser)
                    userViewModel.setUser(newUser)
                    navigateToHome(newUser)

                    val userRef = db.collection("users").document(firebaseUser.uid)

                    // salva depois de carregar as casas
                    userRef.collection("houses").get().addOnSuccessListener { querySnapshot ->
                        val houses = querySnapshot.documents.mapNotNull { doc ->
                            doc.toObject(House::class.java)
                        }
                        newUser.houses.clear()
                        newUser.houses.addAll(houses)

                        // salva com casas
                        userViewModel.setUser(newUser)
                        userViewModel.persistAndSyncUser()
                    }

                    val biometric = Biometric()
                    biometric.saveBiometricAuthUser(requireContext(), newUser.id)
                    biometric.lastLoggedUser(requireContext(), newUser.id)
                }
            }
    }

    // autentica o usuário com o firebase
    private fun firebaseAuthWithGoogle(idToken: String) {

        Log.d("LoginFragment", "Chegou aqui")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = firebaseAuth.currentUser
                    checkAndSaveUserInFirestore(firebaseUser!!)
                    DialogUtils.showMessage(requireContext(), getString(R.string.auth_success))
                    DialogUtils.dismissActiveBanner()
                } else {
                    DialogUtils.showMessage(requireContext(), getString(R.string.auth_fail))
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
        userViewModel.persistAndSyncUser()

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

    fun loginDependent(context: Context, dependentViewModel: DependentViewModel, dependent: Dependent) {
        DialogUtils.dismissActiveBanner()

        dependentViewModel.setDependent(dependent)
        dependentViewModel.persistAndSyncDependent()

        val intent = Intent(context, HomeActivity::class.java)
        intent.putExtra("dependentId", dependent.id)
        startActivity(intent)
        requireActivity().finish()

        // adiciona o usuário a lista da biometria
        val biometric = Biometric()
        biometric.saveBiometricAuthUser(requireContext(), dependent.id)
        biometric.lastLoggedUser(requireContext(), dependent.id)

//        Utils.saveDependentToPrefs(context, dependent)
    }

    // login com google, forma antiga
    fun loginWithGoogleGSO() {
        // Configuração do Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN) // escolhe a opção de login
            .requestIdToken(getString(R.string.default_web_client_id)) // mesmo client_id usado no Firebase
            .requestEmail() // requisição de email
            .build() // monta

        // pega o login com google
        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
    }

    // realiza o login com google
    @Suppress("unused")
    fun loginWithGoogle() {
        val credentialManager = CredentialManager.create(requireContext()) // gerenciador de credenciais

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false) // permite perguntar qual conta logar sempre (false)
            .setAutoSelectEnabled(false) // força a escolher uma conta
            .setServerClientId(getString(R.string.default_web_client_id)) // mesmo client_id usado no Firebase
            .build() // monta

        // requisição de credenciais
        val request = GetCredentialRequest.Builder() // pega a credencial e monta
            .addCredentialOption(googleIdOption) // adiciona as informações de login do google
            .build()

        // inicia a atividade de login com google
        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(requireActivity(), request)
                val credential = result.credential

                // verifica o tipo de credencial
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken // pega o token do google

                    // autentica o usuário com o firebase
                    firebaseAuthWithGoogle(idToken)

                } else {
                    // Lida com outros tipos de credenciais ou erros, se necessário
                    DialogUtils.showMessage(
                        requireContext(),
                        getString(R.string.unexpected_credential)
                    )
                }
            } catch (_: Exception) {
                DialogUtils.showMessage(
                    requireContext(),
                    getString(R.string.auth_google_error)
                )
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // se o login foi bem sucedido
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data) // conta do google aqui
            try {
                val account = task.getResult(ApiException::class.java) // recupera a conta do google
                val idToken = account.idToken // recupera o token
                if (idToken != null) {
                    firebaseAuthWithGoogle(idToken) // faz a autenticação com o firestore
                }
            } catch (_: ApiException) {
                DialogUtils.showMessage(requireContext(), getString(R.string.auth_google_error))
            }
        }
    }

    fun loginAsUser(context: Context, email: String, password: String) {
        Auth().authenticateWithFirestore(email, password) { user ->
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
    }

    fun loginAsDependent(context: Context, email: String, password: String) {
        Auth().authenticateDepWithFirestore(email, password) { dependent ->
            if (dependent != null) {
                loginDependent(context, dependentViewModel, dependent)
            } else {
                DialogUtils.showMessage(
                    requireContext(),
                    getString(R.string.login_error_message)
                )
            }
        }

        Log.d("LoginFragment", "Login de Dependente realizado")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startAppOverlay = view.findViewById(R.id.startAppOverlay)
        loadingImage = view.findViewById(R.id.loadingImage)
        startingAppLogo(true)
        Handler(Looper.getMainLooper()).postDelayed({
            startingAppLogo(false)
        }, 2500)

        val context = requireContext()
        firebaseAuth = Firebase.auth // inicializa o firebase auth
        clearNavHistory() // limpa o histórico de navegação
        Utils.checkIfUserIsLoggedIn(context)
        biometricCaller(requireActivity(), 2700) // biometria

        txtLoginPrompt = view.findViewById(R.id.txtLoginPrompt)
        txtPasswordPrompt = view.findViewById(R.id.txtPasswordPrompt)

        // faz login com google
        loginWithGoogleGSO()
        btnGoogleLogin = view.findViewById(R.id.btnGoogleLogin)
        btnGoogleLogin.setOnClickListener {
            if (Utils.isConnected(requireContext())) {
                googleSignInClient.signOut().addOnCompleteListener {
                    val signInIntent = googleSignInClient.signInIntent
                    startActivityForResult(signInIntent, RC_SIGN_IN)
                }
//                loginWithGoogle() // usando credential manager
            } else {
                DialogUtils.showMessage(requireContext(), getString(R.string.no_connection))
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

                // se houver conexão
                if (Utils.isConnected(requireContext())) {
                    FirestoreHelper.getUserByEmail(email) { userFound ->
                        if (userFound != null) { // existe um usuário em 'users', ou seja, é admin
                            FirestoreHelper.getDependentByEmail(email) { dependentFound ->
                                if (dependentFound != null) { // existe um admin que é dependente também
                                    val options = arrayOf("Administrador", "Dependente")
                                    val dialog = AlertDialog.Builder(requireContext())
                                        .setTitle("Selecione o tipo de usuário")
                                        .setItems(options) { _, which ->
                                            when (which) {
                                                0 -> loginAsUser(context, email, password)
                                                1 -> loginAsDependent(context, email, password)  // Dependente
                                            }
                                        }
                                        dialog.show()
                                } else {
                                    loginAsUser(context, email, password)
                                }
                            }
                        } else { // não tem user, procura um dependente
                            FirestoreHelper.getDependentByEmail(email) { dependentFound ->
                                if (dependentFound != null) { // existe um admin que é dependente também
                                    loginAsDependent(context, email, password)
                                } else {
                                    DialogUtils.showMessage(requireContext(), getString(R.string.user_not_found))
                                }
                            }
                        }
                    }
                } else {
                    DialogUtils.showMessage(requireContext(), getString(R.string.no_connection))
                }
            } else {
                DialogUtils.showMessage(context, getString(R.string.login_empty_message))
            }
        }

        // vai pra tela de criar conta
        btnCreateAccount = view.findViewById(R.id.btnCreatAccount)
        btnCreateAccount.setOnClickListener {
            if (Utils.isConnected(requireContext())) {
                replaceFragment(CadastroFragment(), TransitionType.SLIDE)
            } else {
                DialogUtils.showMessage(requireContext(), getString(R.string.no_connection))
            }
        }

        // botão de recuperar senha
        btnForgotPassword = view.findViewById(R.id.txtForgotPassword)
        btnForgotPassword.setOnClickListener {
            if (Utils.isConnected(requireContext())) {
                replaceFragment(RecoveryFragment(), TransitionType.SLIDE)
            } else {
                DialogUtils.showMessage(requireContext(), getString(R.string.no_connection))
            }
        }

        // botão para biometria
        btnBiometricLogin = view.findViewById(R.id.btnBiometricLogin)
        btnBiometricLogin.setOnClickListener {
            biometricCaller(context, 100)
        }
    }
}
