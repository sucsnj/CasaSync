package com.devminds.casasync // Seu pacote

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Seu layout com o container

        // Garante que o fragmento só seja adicionado na primeira criação
        // e não em recriações (ex: rotação de tela)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment()) // Use o ID do seu container
                .commitNow() // Use commitNow se precisar que seja síncrono, ou commit()
        }
    }
}
    