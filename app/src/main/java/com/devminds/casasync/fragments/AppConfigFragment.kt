package com.devminds.casasync.fragments

import android.os.Bundle
import android.view.View
import com.devminds.casasync.R
import com.google.android.material.appbar.MaterialToolbar

class AppConfigFragment : BaseFragment(R.layout.fragment_config_app) {

    private lateinit var toolbar: MaterialToolbar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar = view.findViewById(R.id.topBar)
        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}
