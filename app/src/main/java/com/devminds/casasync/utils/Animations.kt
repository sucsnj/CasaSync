package com.devminds.casasync.utils

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.widget.ImageView

object Animations {

    private var pulseSet: AnimatorSet? = null
    private var inflateSet: AnimatorSet? = null

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

    fun startInflateAndShrink(image: ImageView) {
        // faz a imagem inflar
        val inflateX = ObjectAnimator.ofFloat(image, "scaleX", 0.5f, 1.2f).apply {
            duration = 1500
        }
        val inflateY = ObjectAnimator.ofFloat(image, "scaleY", 0.5f, 1.2f).apply {
            duration = 1500
        }

        val inflatePhase = AnimatorSet().apply {
            playTogether(inflateX, inflateY)
        }

        // faz a imagem murchar
        val shrinkX = ObjectAnimator.ofFloat(image, "scaleX", 1.2f, 0f).apply {
            duration = 800
        }
        val shrinkY = ObjectAnimator.ofFloat(image, "scaleY", 1.2f, 0f).apply {
            duration = 800
        }
        val fadeOut = ObjectAnimator.ofFloat(image, "alpha", 1f, 0f).apply {
            duration = 800
        }

        // calcular centro do parent
        val parent = image.parent as View
        val centerX = (parent.width - image.width) / 2f
        val centerY = (parent.height - image.height) / 2f

        // usar translation em vez de x/y
        val moveX = ObjectAnimator.ofFloat(
            image,
            "translationX",
            0f,
            centerX - image.x
        ).apply { duration = 800 }

        val moveY = ObjectAnimator.ofFloat(
            image,
            "translationY",
            0f,
            centerY - image.y
        ).apply { duration = 800 }

        val shrinkPhase = AnimatorSet().apply {
            playTogether(shrinkX, shrinkY, fadeOut, moveX, moveY)
        }

        // 3. encadear: inflar → murchar
        inflateSet = AnimatorSet().apply {
            playSequentially(inflatePhase, shrinkPhase)
            start()
        }
    }

    fun stopInflateAnimation() {
        inflateSet?.cancel()
    }
}
