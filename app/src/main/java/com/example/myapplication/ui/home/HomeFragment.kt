package com.example.myapplication.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.myapplication.ListaActivity
import com.example.myapplication.R
import com.example.myapplication.ui.fragment_ricetta.ListaRicette_Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(

        inflater: LayoutInflater,

        container: ViewGroup?,

        savedInstanceState: Bundle?

    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        val cardView_tutteCateg= root.findViewById(R.id.id_card_tutti) as CardView
        cardView_tutteCateg.setOnClickListener(View.OnClickListener {
            choose("tutti") })

        val cardView_torte= root.findViewById(R.id.id_card_torta) as CardView
        cardView_torte.setOnClickListener(View.OnClickListener {
            choose("torte") })

        val cardView_ciamb= root.findViewById(R.id.id_card_ciambellone) as CardView
        cardView_ciamb.setOnClickListener(View.OnClickListener {
            choose("ciamb") })

        val cardView_bisc= root.findViewById(R.id.id_card_biscotti) as CardView
        cardView_bisc.setOnClickListener(View.OnClickListener {
            choose("biscotti") })

        val cardView_mousse= root.findViewById(R.id.id_card_mousse) as CardView
        cardView_mousse.setOnClickListener(View.OnClickListener {
            choose("mousse") })

        val cardView_chess= root.findViewById(R.id.id_card_chees) as CardView
        cardView_chess.setOnClickListener(View.OnClickListener {
            choose("chess") })

        val cardView_crostata= root.findViewById(R.id.id_card_crostata) as CardView
        cardView_crostata.setOnClickListener(View.OnClickListener {
            choose("crostata") })


        val text_input: TextInputLayout
        text_input = root.findViewById<TextInputLayout>(R.id.input_ricerca)


        //---------------------QUERY PER LA RICERCA-------------------------------------------------

        val fab = root.findViewById<FloatingActionButton>(R.id.fab_search)
        fab.setOnClickListener(View.OnClickListener {
            val testo = text_input.editText!!.text.toString().toLowerCase().trim { it <= ' ' }
            val i = Intent(this.activity, ListaActivity::class.java)
            i.putExtra("testo",testo)
            startActivity(i)

        })

        return root
    }

    fun choose(s: String) {
        val i = Intent(this.activity, ListaActivity::class.java)
        i.putExtra("categoria",s)
        startActivity(i)
    }//METODO CHE VA INSERITO PER RICHIAMARE IL FRAGMENT
}