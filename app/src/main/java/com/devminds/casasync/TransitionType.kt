package com.devminds.casasync

import androidx.fragment.app.FragmentTransaction

enum class TransitionType {
    SLIDE, FADE, INFLATE, INFLATE_RIGHT_TOP
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
        TransitionType.INFLATE -> setCustomAnimations(
            R.anim.inflate_in,     // enter
            R.anim.inflate_out,    // exit
            R.anim.inflate_in,     // popEnter
            R.anim.inflate_out     // popExit
        )
        TransitionType.INFLATE_RIGHT_TOP -> setCustomAnimations(
            R.anim.inflate_right_top_in,
            R.anim.inflate_right_top_out,
            R.anim.inflate_right_top_in,
            R.anim.inflate_right_top_out
        )
    }
}
