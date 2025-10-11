package com.devminds.casasync.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devminds.casasync.GenericAdapter
import com.devminds.casasync.parts.House
import com.devminds.casasync.R
import com.devminds.casasync.TransitionType
import com.devminds.casasync.setCustomTransition
import com.devminds.casasync.utils.JsonStorageManager
import com.devminds.casasync.views.UserViewModel
import java.util.UUID
import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.EditorInfo
import android.app.Activity
import androidx.annotation.StringRes
import com.devminds.casasync.utils.Utils
import com.devminds.casasync.utils.Utils.keyboardDelay

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var adapter: GenericAdapter<House>
    private val userViewModel: UserViewModel by activityViewModels()
    private val houseList: MutableList<House>
        get() = userViewModel.user.value?.houses ?: mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val txtWelcome = view.findViewById<TextView>(R.id.welcomeText)

        // carrega o usuário do json
        val userId = userViewModel.user.value?.id ?: "devminds"
        val user = JsonStorageManager.loadUser(requireContext(), userId)
        user?.let {
            userViewModel.setUser(it)
        }

        userViewModel.user.observe(viewLifecycleOwner) { user ->
            val welcome = getString(R.string.welcome_text) + (user?.name ?: "Usuário")
            txtWelcome.text = welcome
        }

        // cria uma casa
        val btnNewHouse = view.findViewById<TextView>(R.id.btnAddHouse)
        btnNewHouse.setOnClickListener {
            val context = requireContext()
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_house, null)
            val input = dialogView.findViewById<EditText>(R.id.inputHouse)

            // diálogo para criar uma nova casa
            val dialog = AlertDialog.Builder(
                ContextThemeWrapper(context, R.style.CustomDialog)
            )
                .setView(dialogView)
                .setPositiveButton(getString(R.string.button_add), null)
                .setNegativeButton(getString(R.string.button_cancel), null)
                .create()

            dialog.show()

            // estilos dos botões
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
                setBackgroundResource(R.drawable.button_primary)
                setTextColor(Color.BLACK)
                setPadding(40, 12, 40, 12)
                setOnClickListener {
                    val houseName = input.text.toString().trim()
                    if (houseName.isNotEmpty()) {
                        val newHouse = House(
                            id = UUID.randomUUID().toString(),
                            name = houseName,
                            ownerId = userViewModel.user.value?.id ?: "devminds"
                        )
                        userViewModel.user.value?.houses?.add(newHouse)
                        adapter.notifyItemInserted(houseList.size - 1)

                        userViewModel.user.value?.let {
                            JsonStorageManager.saveUser(requireContext(), it)
                        }

                        dialog.dismiss()
                    } else {
                        input.error = "Digite um nome válido"
                    }
                }
            }

            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.apply {
                setBackgroundResource(R.drawable.button_secondary)
                setTextColor(Color.BLACK)
                setPadding(40, 12, 40, 12)
                setOnClickListener {
                    dialog.dismiss()
                }
            }
        }

        // lista das casas
        val recyclerHouses = view.findViewById<RecyclerView>(R.id.recyclerHouses)
        recyclerHouses.layoutManager = LinearLayoutManager(requireContext())

        val recycler = recyclerHouses

        adapter =Utils.createHouseAdapter(
            recycler = recyclerHouses,
            list = houseList,
            fragment = HouseFragment(),
            itemId = "houseId",
            itemOptions = getString(R.string.house_options),
            successRenameToast = getString(R.string.rename_success_house_toast),
            userViewModel = userViewModel,
            context = requireContext()
        )

        recycler.adapter = adapter
    }
}
