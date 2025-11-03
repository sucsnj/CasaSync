package com.devminds.casasync.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import com.devminds.casasync.HomeActivity
import com.devminds.casasync.R
import com.devminds.casasync.models.User
import com.devminds.casasync.views.UserViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class LoginFragment : BaseFragment(R.layout.fragment_login) {

    private val userViewModel: UserViewModel by activityViewModels()

    // --- Variáveis de Autenticação ---
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth
    private val TAG = "GoogleSignIn"

    // --- Componentes da UI ---
    private lateinit var txtLoginPrompt: TextView
    private lateinit var txtPasswordPrompt: TextView
    private lateinit var btnGoogleLogin: LinearLayout
    private lateinit var btnLogin: TextView
    private lateinit var btnCreateAccount: TextView
    private lateinit var btnForgotPassword: TextView

    // Lançador para o resultado da tela de login do Google
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "Sucesso no login com Google. Autenticando com Firebase...")
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w(TAG, "Falha no login com Google: ${e.statusCode}", e)
                Toast.makeText(requireContext(), "Falha ao autenticar com Google.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.w(TAG, "Login com Google cancelado ou falhou. Código: ${result.resultCode}")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clearNavHistory()
        setupFirebaseAndGoogle()
        initializeUI(view)
        setupClickListeners()
    }

    private fun setupFirebaseAndGoogle() {
        firebaseAuth = Firebase.auth
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    private fun initializeUI(view: View) {
        txtLoginPrompt = view.findViewById(R.id.txtLoginPrompt)
        txtPasswordPrompt = view.findViewById(R.id.txtPasswordPrompt)
        btnGoogleLogin = view.findViewById(R.id.btnGoogleLogin)
        btnLogin = view.findViewById(R.id.btnLogin)
        btnCreateAccount = view.findViewById(R.id.btnCreatAccount)
        btnForgotPassword = view.findViewById(R.id.txtForgotPassword)
    }

    private fun setupClickListeners() {
        btnGoogleLogin.setOnClickListener {
            Log.d(TAG, "Iniciando fluxo de login com Google...")
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

        btnLogin.setOnClickListener {
            Toast.makeText(requireContext(), "Login tradicional a ser implementado.", Toast.LENGTH_LONG).show()
        }

        btnCreateAccount.setOnClickListener {
            replaceFragment(CadastroFragment(), TransitionType.SLIDE)
        }

        btnForgotPassword.setOnClickListener {
            replaceFragment(RecoveryFragment(), TransitionType.SLIDE)
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
                    Toast.makeText(requireContext(), "Falha na autenticação.", Toast.LENGTH_SHORT).show()
                }
            }
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
                    email = firebaseUser.email ?: "",
                    password = "" // Senha não é necessária
                )
                userRef.set(newUser)
                    .addOnSuccessListener {
                        Log.d(TAG, "Novo usuário salvo no Firestore.")
                        navigateToHome(newUser)
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Erro ao salvar novo usuário no Firestore", e)
                    }
            } else {
                Log.d(TAG, "Usuário já existe no Firestore.")
                val existingUser = document.toObject(User::class.java)!!
                navigateToHome(existingUser)
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "Erro ao buscar usuário no Firestore", e)
        }
    }

    private fun navigateToHome(user: User) {
        userViewModel.setUser(user)
        val intent = Intent(requireContext(), HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
}





