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

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var adapter: GenericAdapter<House>
    private val userViewModel: UserViewModel by activityViewModels()
    private val houseList: MutableList<House>
        get() = userViewModel.user.value?.houses ?: mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val txtWelcome = view.findViewById<TextView>(R.id.welcomeText)

        // carrega o usuário do json
        val userId = activity?.intent?.getStringExtra("userId") ?: userViewModel.user.value?.id ?: "devminds"
        val user = JsonStorageManager.loadUser(requireContext(), userId)
        user?.let {
            userViewModel.setUser(it)
        }

        userViewModel.user.observe(viewLifecycleOwner) { user ->
            txtWelcome.text = "Bem-vindo, ${user?.name ?: "Usuário"}"
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
                .setPositiveButton("Adicionar", null)
                .setNegativeButton("Cancelar", null)
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

        adapter = GenericAdapter(
            items = houseList,
            layoutResId = R.layout.item_generic,
            bind = { itemView, house ->
                itemView.findViewById<TextView>(R.id.itemName).text = house.name
            },
            onItemClick = { selectedHouse ->
                val fragment = HouseFragment().apply {
                    arguments = Bundle().apply {
                        putString("houseId", selectedHouse.id)
                    }
                }

                parentFragmentManager.beginTransaction()
                    .setCustomTransition(TransitionType.SLIDE)
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            },
            onItemLongClick = { selectedHouse ->
                val options = arrayOf("Renomear", "Apagar") // opções de menu de contexto

                AlertDialog.Builder(requireContext())
                    .setTitle("Opções para a casa \"${selectedHouse.name}\"")
                    .setItems(options) { _, which ->
                        when (which) {
                            0 -> {
                                // Renomear (ainda não implementado)
                                Toast.makeText(
                                    requireContext(),
                                    "Renomear casa (em breve)",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            1 -> {
                                // Apagar
                                AlertDialog.Builder(requireContext())
                                    .setTitle("Apagar Casa")
                                    .setMessage("Tem certeza que deseja apagar a casa \"${selectedHouse.name}\"?")
                                    .setPositiveButton("Apagar") { _, _ ->
                                        userViewModel.user.value?.let { user ->
                                            user.houses.remove(selectedHouse)
                                            adapter.notifyDataSetChanged()
                                            JsonStorageManager.saveUser(requireContext(), user)
                                            Toast.makeText(
                                                requireContext(),
                                                "Casa apagada com sucesso",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                    .setNegativeButton("Cancelar", null)
                                    .show()
                            }
                        }
                    }
                    .show()
                true
            }
        )

        recyclerHouses.adapter = adapter
    }
}
