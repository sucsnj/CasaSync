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

        val list = houseList
        val itemOptions = getString(R.string.house_options)
        val renameItem  = getString(R.string.rename_house)
        val successRenameToast = getString(R.string.rename_success_house_toast)

        adapter = GenericAdapter(
            items = list,
            layoutResId = R.layout.item_generic,
            bind = { itemView, item, position, viewHolder ->

                val textView = itemView.findViewById<TextView>(R.id.itemName)
                textView.text = item.name

                itemView.setOnLongClickListener {
                    val context = itemView.context
                    if (context !is Activity) return@setOnLongClickListener false
                    val activity = context

                    val options = arrayOf(getString(R.string.rename_dialog), getString(R.string.delete_dialog))

                    AlertDialog.Builder(activity)
                        .setTitle("$itemOptions ${item.name}")
                        .setItems(options) { _, which ->
                            when (which) {
                                0 -> {
                                    val (dialogView, editTextDialog) = Utils.renameDialogItem(activity, item.name)
                                    // infla um layout de diálogo para editar o item
//                                    val dialogView = LayoutInflater.from(activity)
//                                        .inflate(R.layout.dialog_rename_item, null)
//                                    val editTextDialog = dialogView.findViewById<EditText>(R.id.newNameItem)
//                                    editTextDialog.setText(item.name)
//                                    editTextDialog.setSelection(0, item.name.length)

                                    // cria o diálogo para editar o nome da casa
                                    val dialogNameEdit = AlertDialog.Builder(activity)
                                        .setTitle(renameItem)
                                        .setView(dialogView)
                                        .setPositiveButton(getString(R.string.accept_dialog)) { _, _ ->
                                            val newName = editTextDialog.text.toString().trim()
                                            if (newName.isNotEmpty()) {
                                                item.name = newName
                                                JsonStorageManager.saveUser(activity, userViewModel.user.value!!)
                                                adapter.notifyItemChanged(position)
                                                Toast.makeText(activity, successRenameToast, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .setNegativeButton(getString(R.string.cancel_dialog), null)
                                        .create()
                                    dialogNameEdit.setOnShowListener {

                                        editTextDialog.requestFocus()
                                        editTextDialog.keyboardDelay(activity, 100)
                                    }
                                    dialogNameEdit.show()
                                    true
                                }
                                1 -> {
                                    // Apagar
                                    AlertDialog.Builder(itemView.context)
                                        .setTitle("Apagar Casa")
                                        .setMessage("Tem certeza que deseja apagar a casa \"${item.name}\"?")
                                        .setPositiveButton("Apagar") { _, _ ->
                                            val index = list.indexOfFirst { it.id == item.id }
                                            if (index != -1) {
                                                list.removeAt(index)
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
            onItemClick = { selectedItem ->
                val fragment = HouseFragment().apply {
                    arguments = Bundle().apply {
                        putString("houseId", selectedItem.id)
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
