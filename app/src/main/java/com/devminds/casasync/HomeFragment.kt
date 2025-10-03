package com.devminds.casasync

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.UUID

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var adapter: GenericAdapter<House>

    // casas mockadas
    class House(
        val id: String,
        val name: String
    )

    private val houseList = mutableListOf<House>(
        House("1", "Casa 1"),
        House("2", "Casa 2"),
        House("3", "Casa 3")
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnNewHouse = view.findViewById<TextView>(R.id.btnAddHouse)
        btnNewHouse.setOnClickListener {
            val context = requireContext()
            val input = EditText(context).apply {
                hint = "Nome da casa"
            }

            AlertDialog.Builder(context)
                .setTitle("Adicionar nova casa")
                .setView(input)
                .setPositiveButton("Adicionar") { _, _ ->
                    val houseName = input.text.toString().trim()
                    if (houseName.isNotEmpty()) {
                        val newHouse = House(
                            id = UUID.randomUUID().toString(),
                            name = houseName
                        )
                        adapter.addItem(newHouse)
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

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
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        )

        recyclerHouses.adapter = adapter
    }
}




