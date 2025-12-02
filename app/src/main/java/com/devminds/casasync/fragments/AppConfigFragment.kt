package com.devminds.casasync.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.devminds.casasync.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import androidx.appcompat.app.AppCompatDelegate
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.preference.PreferenceManager
import androidx.core.content.edit
import java.util.Locale
import androidx.appcompat.widget.PopupMenu


class AppConfigFragment : BaseFragment(R.layout.fragment_config_app) {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var btnChangeTheme: MaterialButton
    private lateinit var btnChangeLanguage: MaterialButton
    private lateinit var switchNotifications: SwitchMaterial
    private lateinit var switchAlarms: SwitchMaterial
    private lateinit var switchBiometrics: SwitchMaterial

    private fun toggleTheme() {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val isDarkMode = prefs.getBoolean("dark_mode", false)

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            prefs.edit { putBoolean("dark_mode", false) }
            Toast.makeText(requireContext(), "Tema claro ativado", Toast.LENGTH_SHORT).show()
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            prefs.edit { putBoolean("dark_mode", true) }
            Toast.makeText(requireContext(), "Tema escuro ativado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveLanguage(languageCode: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        prefs.edit {
            putString("app_language", languageCode)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // toolbar - cabeçalho
        toolbar = view.findViewById(R.id.topBar)
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
            toggleTheme()
        }

        btnChangeLanguage.setOnClickListener { anchor ->
            val popup = PopupMenu(requireContext(), anchor)
            popup.menuInflater.inflate(R.menu.language_selector, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.lang_pt -> {
                        saveLanguage("pt-BR")
                        Toast.makeText(requireContext(), "Idioma alterado para Português", Toast.LENGTH_SHORT).show()
                        requireActivity().recreate()
                        true
                    }
                    R.id.lang_en -> {
                        saveLanguage("en")
                        Toast.makeText(requireContext(), "Language changed to English", Toast.LENGTH_SHORT).show()
                        requireActivity().recreate()
                        true
                    }
                    R.id.lang_default -> {
                        saveLanguage("") // vazio = padrão do sistema
                        Toast.makeText(requireContext(), "Idioma padrão do sistema", Toast.LENGTH_SHORT).show()
                        requireActivity().recreate()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
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
