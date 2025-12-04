package com.devminds.casasync.fragments

import android.Manifest
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.devminds.casasync.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import androidx.appcompat.app.AppCompatDelegate
import android.content.SharedPreferences
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.PreferenceManager
import androidx.core.content.edit
import androidx.appcompat.widget.PopupMenu
import com.devminds.casasync.utils.DialogUtils
import com.devminds.casasync.utils.PermissionHelper
import android.content.Intent
import android.provider.Settings
import androidx.core.net.toUri

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

    // registra o pedido de permissão para notificações
    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                // permissão concedida
                DialogUtils.showMessage(requireContext(), getString(R.string.notification_permission_granted))
            } else {
                // Permissão negada
                DialogUtils.showMessage(requireContext(), getString(R.string.notification_permission_denied))
            }
        }

    // registra o pedido de permissão para alarmes
    private val requestAlarmPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                // permissão concedida
                DialogUtils.showMessage(requireContext(), "Permissão de alarmes concedida")
            } else {
                // Permissão negada
                DialogUtils.showMessage(requireContext(), "Permissão de alarmes negada")
            }
        }

    override fun onResume() {
        super.onResume()
        // muda o switch de notificações conforme a permissão atual
        switchNotifications.isChecked = PermissionHelper.hasNotificationPermission(requireContext())
        // muda o swtich de alarmes conforme a permissão atual
        switchAlarms.isChecked = PermissionHelper.hasExactAlarmPermission(requireContext())
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

        // muda o tema do app (claro/escuro)
        btnChangeTheme.setOnClickListener {
            toggleTheme()
        }

        btnChangeLanguage.setOnClickListener { anchor ->
            var easterEgg = false
            val easterEggChance = (1..10).random()
            if (easterEggChance == 7) {
                easterEgg = true
            }

            val messageEasterEggChance = (1..10).random()
            if (messageEasterEggChance > 7 && easterEgg) {
                DialogUtils.showMessage(
                    requireContext(),
                    "Os ventos de Aman sussurram: o idioma dos Elfos antigos está ao teu alcance. Quenya se revela."
                )
            } else if (messageEasterEggChance < 7 && easterEgg) {
                DialogUtils.showMessage(
                    requireContext(),
                    "A voz dos Valar ecoa: o idioma dos Primogênitos desperta. Quenya está revelado a ti."
                )
            } else if (messageEasterEggChance == 7 && easterEgg) {
                DialogUtils.showMessage(
                    requireContext(),
                    "Das terras imortais de Aman, concedemos-te o dom: fala agora a língua dos elfos, o Quenya."
                )
            }

            // visiblidade do item de idioma quenya no menu
            val popup = PopupMenu(requireContext(), anchor)
            popup.menuInflater.inflate(R.menu.language_selector, popup.menu)
            
            val quenyaItem = popup.menu.findItem(R.id.lang_qy)
            quenyaItem.isVisible = easterEgg

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.lang_pt -> {
                        saveLanguage("pt-BR")
                        DialogUtils.showMessage(requireContext(), "Idioma alterado para Português")
                        requireActivity().recreate()
                        true
                    }
                    R.id.lang_en -> {
                        saveLanguage("en")
                        DialogUtils.showMessage(requireContext(), "Language changed to English")
                        requireActivity().recreate()
                        true
                    }
                    R.id.lang_es -> {
                        saveLanguage("es")
                        DialogUtils.showMessage(requireContext(), "Idioma cambiado a español")
                        requireActivity().recreate()
                        true
                    }
                    R.id.lang_zh -> {
                        saveLanguage("zh")
                        DialogUtils.showMessage(requireContext(), "语言已更改为简体中文")
                        requireActivity().recreate()
                        true
                    }
                    R.id.lang_ja -> {
                        saveLanguage("ja")
                        DialogUtils.showMessage(requireContext(), "言語がポルトガル語に変更されました")
                        requireActivity().recreate()
                        true
                    }
                    R.id.lang_qy -> {
                        saveLanguage("qy")
                        DialogUtils.showMessage(requireContext(), "Texto em Quenya")
                        requireActivity().recreate()
                        true
                    }
                    R.id.lang_default -> {
                        saveLanguage("") // vazio = padrão do sistema
                        DialogUtils.showMessage(requireContext(), "Idioma padrão do sistema")
                        requireActivity().recreate()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        // aqui fica o estado salvo do switch de notificações, alarmes // e biometria
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val enabledNotifications = prefs.getBoolean("notifications_enabled", false)
        val enableAlarms = prefs.getBoolean("alarms_enabled", false)
        // val enableBiometrics = prefs.getBoolean("biometrics_enabled", false)

        // verifica se a permissão foi dada 
        val hasPermission = PermissionHelper.hasNotificationPermission(requireContext())
        val hasAlarmPermission = PermissionHelper.hasExactAlarmPermission(requireContext())

        // muda o estado do switch
        switchNotifications.isChecked = enabledNotifications && hasPermission

        // botão para mudar o estado do switch de notificações
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // tem que pedir permissão para android 13+
                    if (PermissionHelper.hasNotificationPermission(requireContext())) {
                        // DialogUtils.showMessage(requireContext(), getString(R.string.notification_permission_granted))
                    } else {
                        requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                } else {
                    // DialogUtils.showMessage(requireContext(), getString(R.string.notification_permission_granted))
                }
            } else {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                }
                startActivity(intent)

                DialogUtils.showMessage(requireContext(), getString(R.string.notification_permission_denied))
            }
        }

        switchAlarms.isChecked = enableAlarms && hasAlarmPermission

        // botão para mudar o estado do switch de alarmes
        switchAlarms.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (PermissionHelper.hasExactAlarmPermission(requireContext())) {
                    DialogUtils.showMessage(requireContext(), "Alarmes ativados")
                } else {
                    PermissionHelper.checkAndRequestExactAlarmPermission(requireContext())
                }
            } else {
                PermissionHelper.checkAndRequestExactAlarmPermission(requireContext())
            }
        }

        @Suppress("unused")
        switchBiometrics.setOnCheckedChangeListener { _, isChecked ->
            DialogUtils.showMessage(requireContext(), "Funcionalidade de biometria ainda não implementada.")

            // DialogUtils.showMessage(requireContext(),
            //     if (isChecked) "Biometria ativada" else "Biometria desativada")
            // @TODO: integrar com BiometricPrompt para autenticação
        }
    }
}
