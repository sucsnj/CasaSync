package com.devminds.casasync

import android.app.Activity
import android.content.res.Resources
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import android.content.Context
import android.os.Build
import android.widget.Toast
import com.example.casasync.R

// objeto auxiliar para substituir o Toast do android
object SnackbarUtils {

    // função base para a chamada do snackbar
    fun show(activity: Activity, message: String, iconResId: Int? = null, duration: Int = Snackbar.LENGTH_LONG) {

        // pega a view raiz para colocar o snackbar
        val rootView = activity.window.decorView

        // cria um snackbar padrão sem nada
        val snackbar = Snackbar.make(rootView, "", duration)

        // infla o snackbar "custom_snackbar.xml"
        val customView = LayoutInflater.from(activity).inflate(R.layout.custom_snackbar, null)

        // variáveis para acessar os elementos do snackbar "texto e ícone"
        val textView = customView.findViewById<TextView>(R.id.snackbar_text)
        val iconView = customView.findViewById<ImageView>(R.id.snackbar_icon)

        // define o texto e o ícone
        textView.text = message
        if (iconResId != null) {
            iconView.setImageResource(iconResId)
            iconView.visibility = View.VISIBLE
        }

        // pega a largura da tela e define o tamanho do snackbar
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val targetWidth = (screenWidth * 0.7).toInt() // 70% de snackbar
        val sideMargin = (screenWidth * 0.15).toInt() // 30% de margem "15 + 15"

        // cria os parâmetros para o snackbar
        val params = FrameLayout.LayoutParams(targetWidth, FrameLayout.LayoutParams.WRAP_CONTENT)
        params.leftMargin = sideMargin // margem à esquerda
        params.rightMargin = sideMargin // margem à direita
        params.gravity = Gravity.BOTTOM // posiciona no canto inferior

        // converte 100dp para pixels
        val bottomMarginDp = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            100f,
            Resources.getSystem().displayMetrics
        ).toInt()
        params.bottomMargin = bottomMarginDp // margem inferior "100f"

        // aplica os parâmetros ao snackbar
        customView.layoutParams = params

        // define o layout do snackbar
        val snackbarLayout = snackbar.view as ViewGroup
        snackbarLayout.setBackgroundColor(android.graphics.Color.TRANSPARENT) // remove o fundo padrão
        snackbarLayout.setPadding(0, 0, 0, 0) // remove o padding padrão
        snackbarLayout.removeAllViews() // remove todas as views padrão
        snackbarLayout.addView(customView) // adiciona a view customizada

        // mostra o snackbar
        snackbar.show()
    }

    // função para mostrar um toast "snackbar" com base na versão do android
    fun showMessage(context: Context, message: String, iconResId: Int? = null) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // se for android 14 ou inferior
            SnackbarUtils.show(context as Activity, message, iconResId) // chama a função "show"
        } else {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show() // mostra o toast padrão
        }
    }
}
