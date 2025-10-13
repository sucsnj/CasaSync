package com.devminds.casasync.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devminds.casasync.GenericAdapter
import com.devminds.casasync.R
import com.devminds.casasync.parts.House
import com.devminds.casasync.utils.JsonStorageManager
import com.devminds.casasync.utils.Utils
import com.devminds.casasync.views.UserViewModel
import com.google.android.material.appbar.MaterialToolbar
import java.util.UUID

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var adapter: GenericAdapter<House>
    private val userViewModel: UserViewModel by activityViewModels()
    private val houseList: MutableList<House>
        get() = userViewModel.user.value?.houses ?: mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.topBar)

        // carrega o usuário do json
        val userId =
            activity?.intent?.getStringExtra("userId") ?: userViewModel.user.value?.id ?: getString(
                R.string.devminds_text
            )
        val user = JsonStorageManager.loadUser(requireContext(), userId)
        user?.let {
            userViewModel.setUser(it)
        }

        userViewModel.user.observe(viewLifecycleOwner) { user ->
            val welcome = getString(R.string.welcome_text) + (user?.name ?: "Usuário")
            toolbar.title = welcome
        }

        toolbar.inflateMenu(R.menu.topbar_menu)
        val menu = toolbar.menu // para controlar a visibilidade dos itens
        menu.findItem(R.id.action_homepage).isVisible = false

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_settings -> {
                    Toast.makeText(
                        context,
                        "implementando settings",
                        Toast.LENGTH_SHORT
                    ).show()
                    true
                }
                R.id.action_help -> {
                    Toast.makeText(
                        context,
                        "implementando ajuda",
                        Toast.LENGTH_SHORT
                    ).show()
                    true
                }
                else -> false
            }
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
                            ownerId = userViewModel.user.value?.id ?: getString(R.string.devminds_text)
                        )
                        userViewModel.user.value?.houses?.add(newHouse)
                        adapter.notifyItemInserted(houseList.size - 1)

                        userViewModel.user.value?.let {
                            JsonStorageManager.saveUser(requireContext(), it)
                        }

                        dialog.dismiss()
                    } else {
                        input.error = context.getString(R.string.invalid_house_name)
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

        adapter = Utils.createHouseAdapter(
            recycler = recyclerHouses,
            list = houseList,
            fragmentFactory = { houseId ->
                HouseFragment().apply {
                    arguments = Bundle().apply {
                        putString("houseId", houseId)
                    }
                }
            },
            fragmentManager = parentFragmentManager,
            itemOptions = getString(R.string.house_options),
            successRenameToast = getString(R.string.rename_success_house_toast),
            userViewModel = userViewModel,
            context = requireContext()
        )

        recycler.adapter = adapter
    }
}
