package com.devminds.casasync.utils

import android.content.Context
import android.view.Menu
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.devminds.casasync.R
import com.devminds.casasync.TransitionType
import com.devminds.casasync.fragments.AboutFragment
import com.devminds.casasync.fragments.AppConfigFragment
import com.devminds.casasync.fragments.BaseFragment
import com.devminds.casasync.fragments.UserConfigFragment

object PopupMenu {

    fun show(context: Context, anchor: View, baseFragment: BaseFragment): Menu {
        val popup = PopupMenu(context, anchor)
        popup.menuInflater.inflate(R.menu.popup_menu_layout, popup.menu)

        popup.setOnMenuItemClickListener { popupItem ->
            when (popupItem.itemId) {
                R.id.app_settings -> {
                    baseFragment.replaceFragment(AppConfigFragment(), TransitionType.INFLATE_RIGHT_TOP)
                    true
                }
                R.id.user_settings -> {
                    baseFragment.replaceFragment(UserConfigFragment(), TransitionType.INFLATE_RIGHT_TOP)
                    true
                }
                R.id.about -> {
                    baseFragment.replaceFragment(AboutFragment(), TransitionType.INFLATE_RIGHT_TOP)
                    true
                }
                else -> false
            }
        }

        popup.show()
        return popup.menu
    }

}
