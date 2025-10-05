package com.devminds.casasync.fragments

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
        val userId = userViewModel.user.value?.id ?: "devminds"
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
            val input = EditText(context).apply {
                hint = "Nome da casa"
            }

            // dialogo para dar nome a casa
            AlertDialog.Builder(context)
                .setTitle("Adicionar nova casa")
                .setView(input)
                .setPositiveButton("Adicionar") { _, _ ->
                    val houseName = input.text.toString().trim()
                    if (houseName.isNotEmpty()) {
                        val newHouse = House(
                            id = UUID.randomUUID().toString(),
                            name = houseName,
                            ownerId = userViewModel.user.value?.id ?: "devminds"
                        )
                        userViewModel.user.value?.houses?.add(newHouse)
                        adapter.notifyItemInserted(houseList.size - 1)

                        // persiste o usuário em json
                        val user = userViewModel.user.value
                        user?.let {
                            JsonStorageManager.saveUser(requireContext(), it)
                        }
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
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
                Toast.makeText(requireContext(), "Clique longo detectado (implementação)!", Toast.LENGTH_SHORT)
                    .show()
                true
            }
        )

        recyclerHouses.adapter = adapter
    }
}