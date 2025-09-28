package com.example.casasync

import androidx.fragment.app.FragmentTransaction

enum class TransitionType {
    SLIDE, FADE
}

fun FragmentTransaction.setCustomTransition(type: TransitionType): FragmentTransaction {
    return when (type) {
        TransitionType.SLIDE -> setCustomAnimations(
            R.anim.slide_in_right,
            R.anim.slide_out_left,
            R.anim.slide_in_left,
            R.anim.slide_out_right
        )
        TransitionType.FADE -> setCustomAnimations(
            R.anim.slide_in_left,
            R.anim.slide_out_right,
            R.anim.slide_in_right,
            R.anim.slide_out_left
        )
    }
}
