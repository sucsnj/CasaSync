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
            bind = { itemView, house, position, viewHolder ->
                val editText = itemView.findViewById<EditText>(R.id.itemName)
                editText.setText(house.name)
                editText.isEnabled = false
                editText.isFocusable = false

                itemView.setOnLongClickListener {
                    val options = arrayOf("Renomear", "Apagar")

                    AlertDialog.Builder(itemView.context)
                        .setTitle("Opções para a casa \"${house.name}\"")
                        .setItems(options) { _, which ->
                            when (which) {
                                0 -> {

                                    // infla um layout de diálogo para editar o nome da casa
                                    val dialogView = LayoutInflater.from(itemView.context)
                                        .inflate(R.layout.dialog_rename_item, null)
                                    val editText =
                                        dialogView.findViewById<EditText>(R.id.newNameItem)
                                    editText.setText(house.name)
                                    editText.setSelection(0, house.name.length)

                                    // cria o diálogo para editar o nome da casa
                                    val dialog = AlertDialog.Builder(itemView.context)
                                        .setTitle("Renomear Casa")
                                        .setView(dialogView)
                                        .setPositiveButton("Aceitar") { _, _ ->
                                            val newName = editText.text.toString().trim()
                                            if (newName.isNotEmpty()) {
                                                house.name = newName
                                                JsonStorageManager.saveUser(itemView.context, userViewModel.user.value!!)
                                                adapter.notifyItemChanged(position)
                                                Toast.makeText(itemView.context, "Casa renomeada", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .setNegativeButton("Cancelar", null)
                                        .create()

                                    dialog.show()

                                    editText.post {
                                        editText.requestFocus()
                                        val imm = itemView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
                                    }
                                }
                                1 -> {
                                    // Apagar
                                    AlertDialog.Builder(itemView.context)
                                        .setTitle("Apagar Casa")
                                        .setMessage("Tem certeza que deseja apagar a casa \"${house.name}\"?")
                                        .setPositiveButton("Apagar") { _, _ ->
                                            val index = houseList.indexOfFirst { it.id == house.id }
                                            if (index != -1) {
                                                houseList.removeAt(index)
                                                adapter.notifyItemRemoved(index)

                                                userViewModel.user.value?.let {
                                                    JsonStorageManager.saveUser(
                                                        itemView.context,
                                                        it
                                                    )
                                                }

                                                Toast.makeText(
                                                    itemView.context,
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
            }
        )

        recyclerHouses.adapter = adapter
    }
}
