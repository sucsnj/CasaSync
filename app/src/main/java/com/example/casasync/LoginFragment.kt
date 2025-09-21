package com.example.casasync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.TextView
import android.widget.Toast

// lógica de inflação de fragmento já na declaração de classe
class LoginFragment : BaseFragment(R.layout.fragment_login) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val txtLoginPrompt = view.findViewById<TextView>(R.id.txtLoginPrompt)
        val txtPasswordPrompt = view.findViewById<TextView>(R.id.txtPasswordPrompt)

        val btnLogin = view.findViewById<TextView>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val login = txtLoginPrompt.text.toString()
            val password = txtPasswordPrompt.text.toString()

            if (login.isNotEmpty() && password.isNotEmpty()) {
                val userFound = CadastroFragment.users.find {
                    it.login == login && it.password == password
                }

                if (userFound != null) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.login_success_message),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.login_error_message),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        val btnCreateAccount = view.findViewById<TextView>(R.id.btnCreatAccount)

        btnCreateAccount.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, CadastroFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}
