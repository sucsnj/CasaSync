package com.devminds.casasync.utils

import android.content.Context
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import com.devminds.casasync.R

object PopupMenu {

    fun show(context: Context, anchor: View): Menu {

        val popup = PopupMenu(context, anchor)
        popup.menuInflater.inflate(R.menu.popup_menu_layout, popup.menu)
        popup.setOnMenuItemClickListener { popupItem ->
            when (popupItem.itemId) {
                R.id.app_settings -> {
                    Toast.makeText(context, "implementando configurações do app", Toast.LENGTH_SHORT).show()
                    true }
                R.id.user_settings -> {
                    Toast.makeText(context, "implementando configurações do usuário", Toast.LENGTH_SHORT).show()
                    true }
                R.id.about -> {
                    Toast.makeText(context, "implementando sobre", Toast.LENGTH_SHORT).show()
                    true }
                else -> false
            }
        }
        popup.show()
        return popup.menu
    }
}
