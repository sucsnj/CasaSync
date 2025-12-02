package com.devminds.casasync.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.devminds.casasync.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import androidx.appcompat.app.AppCompatDelegate
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class AppConfigFragment : BaseFragment(R.layout.fragment_config_app) {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var btnChangeTheme: MaterialButton
    private lateinit var btnChangeLanguage: MaterialButton
    private lateinit var switchNotifications: SwitchMaterial
    private lateinit var switchAlarms: SwitchMaterial
    private lateinit var switchBiometrics: SwitchMaterial

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // toolbar - cabeçalho
        toolbar = view.findViewById(R.id.topBarSettings)
        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // botões
        btnChangeTheme = view.findViewById(R.id.btnChangeTheme)
        btnChangeLanguage = view.findViewById(R.id.btnChangeLanguage)

        // switches
        switchNotifications = view.findViewById(R.id.switchNotifications)
        switchAlarms = view.findViewById(R.id.switchAlarms)
        switchBiometrics = view.findViewById(R.id.switchBiometrics)

        // listeners
        btnChangeTheme.setOnClickListener {
            Toast.makeText(requireContext(), "Alterar tema", Toast.LENGTH_SHORT).show()
            // TODO: implementar lógica de troca de tema (ex: AppCompatDelegate)
        }

        btnChangeLanguage.setOnClickListener {
            Toast.makeText(requireContext(), "Alterar idioma", Toast.LENGTH_SHORT).show()
            // TODO: abrir diálogo ou tela de seleção de idioma
        }

        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(requireContext(),
                if (isChecked) "Notificações ativadas" else "Notificações desativadas",
                Toast.LENGTH_SHORT).show()
            // TODO: pedir permissão de notificações (NotificationManager)
        }

        switchAlarms.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(requireContext(),
                if (isChecked) "Alarmes ativados" else "Alarmes desativados",
                Toast.LENGTH_SHORT).show()
            // TODO: configurar permissões de alarmes (AlarmManager / ExactAlarmPermission)
        }

        switchBiometrics.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(requireContext(),
                if (isChecked) "Biometria ativada" else "Biometria desativada",
                Toast.LENGTH_SHORT).show()
            // TODO: integrar com BiometricPrompt para autenticação
        }
    }
}
