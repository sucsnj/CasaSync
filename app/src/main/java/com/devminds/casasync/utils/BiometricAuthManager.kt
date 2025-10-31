package com.devminds.casasync.utils

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

// verifica se o aparelho suporte autenticação por biometria
object BiometricAuthManager {

    // retorna um booleano indicando se o aparelho suporta autenticação por biometria
    fun canUseBiometric(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL

        val result = biometricManager.canAuthenticate(authenticators)
        Log.d("BiometricCheck", "canAuthenticate result: $result")

        return biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
    }

    // faz o processo de biometria para o último usuário logado
    fun tryBiometricLogin(
        context: Context, // contexto da activity
        activity: FragmentActivity, // é necessário uma activity para usar o BiometricPrompt
        onSuccess: (String) -> Unit, // quando for sucesso, retorna o id do usuário
        onError: (String) -> Unit // quando for erro, retorna a mensagem de erro
    ) {
        val biometric = Biometric() // instancia da classe Biometric
        val lastUser = biometric.getLastLoggedUser(context) // ultimo usuário logado
        val biometricUsers = biometric.getBiometricAuthUsers(context) // lista  de usuário da biometria (o xml)

        // se houver um último usuário logado e ele está na lista...
        if (lastUser != null && biometricUsers.contains(lastUser)) {
            val executor = ContextCompat.getMainExecutor(context) // retorna o que roda na thread principal
            // também é ele que permite a interação com a tela do usuário, como modificações e mensagens...

            // instancia do BiometricPrompt
            val biometricPrompt = BiometricPrompt(
                activity, executor,
                object : BiometricPrompt.AuthenticationCallback() {

                    // quando há uma biometria reconhecida
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        onSuccess(lastUser)
                    }

                    // quando não há autenticação, seja por cancelamento, erro ou falta de hardware
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        onError(errString.toString())
                    }

                    // quando a autenticação não é reconhecida
                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        onError("Autenticação falhou")
                    }
                })

            // a visualização do prompt de biometria
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Autenticação biométrica")
                .setSubtitle("Use sua digital para entrar")
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build()

            biometricPrompt.authenticate(promptInfo) // retorna o prompt pronto
        }
    }
}
