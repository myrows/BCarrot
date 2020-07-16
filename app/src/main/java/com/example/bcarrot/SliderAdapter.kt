package com.example.bcarrot

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager.widget.PagerAdapter
import com.example.bcarrot.common.MyApp.Companion.context


class SliderAdapter : PagerAdapter() {

    lateinit var layoutInflater: LayoutInflater

    //Arrays
    var slideImages = intArrayOf(
        R.drawable.ic_car,
        R.drawable.ic_bluetooth,
        R.drawable.ic_conectado,
        R.drawable.ic_zanahoria
    )

    var slideHeading = arrayOf(
        "Tu vehículo",
        "Vincular dispositivos",
        "Conexión",
        "Bcoins"
    )

    var slideDesc = arrayOf(
        "Entra en el apartado 'Tu vehículo' y añade los datos correspondiente al vehículo que estás usando en este instante",
        "Pulsa sobre el icono y elige uno de los dispositivos bluetooth y dale a 'vincular', cuando salgas de ese apartado saldrá en el listado aquellos dispositivos vinculados",
        "En el listado de dispositivos, pulsa sobre aquel que quieres establecer conexión, saldrá un mensaje en la parte inferior 'conectado', en este momento el otro usuario puede pulsar en tu dispositivo e interactuar contigo",
        "En tu perfíl puedes ver los Bcoins conseguidos, puedes añadir más viendo vídeos, estos al terminar te recompensarán con 100 Bcoins. Son necesarios para mandar mensajes de voz si no eres premium"
    )

    override fun getCount(): Int {
        return slideHeading.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        layoutInflater = (context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)!!
        val view: View = layoutInflater.inflate(R.layout.slide_layout, container, false)
        val slideImageView: ImageView = view.findViewById(R.id.slideImages)
        val slideHeadingText: TextView = view.findViewById(R.id.slideHeading)
        val slideDescText: TextView = view.findViewById(R.id.slideDesc)
        slideImageView.setImageResource(slideImages[position])
        slideHeadingText.text = slideHeading[position]
        slideDescText.text = slideDesc[position]
        container.addView(view)
        return view
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as ConstraintLayout)
    }
}