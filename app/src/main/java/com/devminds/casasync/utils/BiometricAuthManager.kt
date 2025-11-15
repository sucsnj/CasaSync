package com.devminds.casasync.utils

import android.content.Context
import android.os.Build
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

        // verifica a versão do android
        val authenticators = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        } else {
            BiometricManager.Authenticators.BIOMETRIC_WEAK
        }

        val result = biometricManager.canAuthenticate(authenticators)
        Log.d("BiometricCheck", "canAuthenticate result: $result")

        return result == BiometricManager.BIOMETRIC_SUCCESS
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
            val promptBuilder = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Autenticação biométrica")
                .setSubtitle("Use sua digital para entrar")

            // para android 11+ (DEVICE_CREDENTIAL)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                promptBuilder.setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
            } else {
                // o android 10- precisa do botão de negação
                promptBuilder
                    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                    .setNegativeButtonText("Cancelar")
            }

            val promptInfo = promptBuilder.build()
            biometricPrompt.authenticate(promptInfo) // retorna o prompt pronto
        }
    }
}
