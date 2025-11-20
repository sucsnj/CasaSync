package com.devminds.casasync.fragments

import android.util.Log
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.devminds.casasync.FirestoreHelper
import com.devminds.casasync.GenericAdapter
import com.devminds.casasync.R
import com.devminds.casasync.TransitionType
import com.devminds.casasync.parts.House
import com.devminds.casasync.utils.Animations
import com.devminds.casasync.utils.DialogUtils
import com.devminds.casasync.utils.Utils
import com.devminds.casasync.views.UserViewModel
import com.google.android.material.appbar.MaterialToolbar
import java.util.UUID
import com.devminds.casasync.utils.PopupMenu

class HomeFragment : BaseFragment(R.layout.fragment_home) {

    private val userViewModel: UserViewModel by activityViewModels()
    private val houseList = mutableListOf<House>()
    private lateinit var adapter: GenericAdapter<House>
    private lateinit var toolbar: MaterialToolbar
    private lateinit var menu: Menu
    private lateinit var menuItemView: View
    private lateinit var btnNewHouse: TextView
    private lateinit var recyclerHouses: RecyclerView
    private lateinit var recycler: RecyclerView
    private lateinit var userPhoto: ImageView
    private lateinit var title: TextView
    private lateinit var subtitle: TextView
    private lateinit var loadingOverlay: View
    private lateinit var loadingImage: ImageView
    private lateinit var swipeRefresh: SwipeRefreshLayout

    private fun openUserPerfil() { // permite abrir a tela de perfil do usuário
        userPhoto = view?.findViewById(R.id.userPhoto)!! // foto
        title = view?.findViewById(R.id.title)!! // nome
        subtitle = view?.findViewById(R.id.subtitle)!! // perfil
        userPhoto.setOnClickListener {
            replaceFragment(UserConfigFragment(), TransitionType.FADE)
        }
        title.setOnClickListener {
            replaceFragment(UserConfigFragment(), TransitionType.FADE)
        }
        subtitle.setOnClickListener {
            replaceFragment(UserConfigFragment(), TransitionType.FADE)
        }
    }

    fun resolveUserId(): String {
        return activity?.intent?.getStringExtra("userId")
            ?: userViewModel.user.value?.id
            ?: getString(R.string.devminds_text)
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            setStatusBarColor(requireActivity().window, statusBarColor("notch"))
            loadingOverlay.visibility = View.VISIBLE
            Animations.startPulseAnimation(loadingImage)
        } else {
            Animations.stopPulseAnimation()
            loadingOverlay.visibility = View.GONE
        }
    }

    fun syncFirestoreToApp() {
        FirestoreHelper.getUserById(resolveUserId()) { user ->
            user?.let {
                val casasRef = FirestoreHelper.getDb()
                    .collection("users")
                    .document(it.id)
                    .collection("houses")

                casasRef.get().addOnSuccessListener { snapshot ->
                    val houses = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(House::class.java)
                    }

                    it.houses.clear()
                    it.houses.addAll(houses)

                    userViewModel.setUser(it)

                    houseList.clear()
                    houseList.addAll(houses)
                    adapter.notifyItemRangeChanged(0, houseList.size)
                }
            }
        }
    }

    fun refreshPage(swipeRefresh: SwipeRefreshLayout) {
        swipeRefresh.setOnRefreshListener {
            syncFirestoreToApp()
            // encerra o efeito de refresh
            swipeRefresh.isRefreshing = false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("HomeFragment", "onViewCreated()")

        val context = requireContext()

        loadingOverlay = view.findViewById(R.id.loadingOverlay)
        loadingImage = view.findViewById(R.id.loadingImage)

        showLoading(true) // mostra loading

        clearNavHistory()
        openUserPerfil()

        syncFirestoreToApp()

        toolbar = view.findViewById(R.id.topBar)
        title = view.findViewById(R.id.title)

        // cabeçalho
        userViewModel.user.observe(viewLifecycleOwner) { user ->
            // nome do usuário
            val welcome = getString(R.string.welcome_text) + (user?.name ?: "Usuário")
            title.text = welcome

            // foto do usuário
            user?.photoUrl?.let { url ->
                Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.user_photo) // imagem padrão
                    .error(R.drawable.user_photo_error) // em caso de erro
                    .circleCrop() // arredonda a imagem
                    .into(userPhoto)
            }

            if (user != null) {
                Handler(Looper.getMainLooper()).postDelayed({
                    showLoading(false)
                }, 200)
            }
        }

        toolbar.inflateMenu(R.menu.topbar_menu)
        menu = toolbar.menu
        // esconde o item de voltar para o início
        menu.findItem(R.id.action_homepage).isVisible = false

        // menu suspenso (3 pontos)
        menuItemView = toolbar.findViewById(R.id.more_options)

        // botão de menu suspenso
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.more_options -> {
                    // mostra o menu suspenso
                    PopupMenu.show(context, menuItemView, this)
                    true
                }
                else -> false
            }
        }

        // cria uma casa
        btnNewHouse = view.findViewById(R.id.btnAddHouse)
        btnNewHouse.setOnClickListener {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_house, null)
            val input = dialogView.findViewById<EditText>(R.id.inputHouse)

            // teclado com delay
            delayEditText(input, context)

            // diálogo para criar uma nova casa
            val dialog = AlertDialog.Builder(ContextThemeWrapper(context, R.style.CustomDialog))
                .setView(dialogView)
                .setPositiveButton(getString(R.string.button_add), null)
                .setNegativeButton(getString(R.string.button_cancel), null)
                .setCancelable(false)
                .create()
            dialog.show()

            // estilo do botão "Adicionar"
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
                            ownerId = userViewModel.user.value?.id ?: getString(R.string.devminds_text) // id do usuário atual
                        )
                        Log.d("NovaCasa", "ID gerado: ${newHouse.id}")

                        // adiciona a casa à lista
                        houseList.add(newHouse)

                        // adiciona a casa à lista e notifica o adapter
                        userViewModel.user.value?.houses?.add(newHouse)
                        adapter.notifyItemInserted(houseList.size - 1)

                        // persiste o usuário
                        userViewModel.persistAndSyncUser()

                        dialog.dismiss()
                        DialogUtils.showMessage(context, context.getString(R.string.created_house))
                    } else {
                        input.error = context.getString(R.string.invalid_house_name)
                    }
                }
            }

            // estilos do botão "Cancelar"
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.apply {
                setBackgroundResource(R.drawable.button_secondary)
                setTextColor(Color.BLACK)
                setPadding(40, 12, 40, 12)
                setOnClickListener {
                    dialog.dismiss()
                }
            }
        }

        // atualiza o adapter
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        refreshPage(swipeRefresh)

        // lista das casas
        recyclerHouses = view.findViewById(R.id.recyclerHouses)
        recyclerHouses.layoutManager = LinearLayoutManager(context)
        recycler = recyclerHouses

        // manuseia a lista de casas
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
            context = context
        )
        recycler.adapter = adapter
    }
}
