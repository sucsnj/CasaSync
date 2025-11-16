package com.devminds.casasync.utils

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.widget.ImageView

object Animations {

    private var pulseSet: AnimatorSet? = null

    fun startPulseAnimation(loadingImage: ImageView) {

        val scaleX = ObjectAnimator.ofFloat(loadingImage, "scaleX", 1f, 1.2f).apply {
            duration = 600
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = ObjectAnimator.INFINITE
        }

        val scaleY = ObjectAnimator.ofFloat(loadingImage, "scaleY", 1f, 1.2f).apply {
            duration = 600
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = ObjectAnimator.INFINITE
        }

        pulseSet = AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            start()
        }
    }

    fun stopPulseAnimation() {
        pulseSet?.cancel()
    }
}