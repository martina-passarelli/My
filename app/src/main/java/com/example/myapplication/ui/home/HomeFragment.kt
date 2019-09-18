package com.example.myapplication.ui.home

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

        val cardView_tutteCateg= root.findViewById(R.id.id_card_home) as CardView
        cardView_tutteCateg.setOnClickListener(View.OnClickListener {
            //choose("tutti") })
            //View.OnClickListener{
            val i = Intent(this.activity, ListaActivity::class.java)
            startActivity(i)

            /*val fragment= com.example.myapplication.ui.fragment_ricetta.ListaRicette_Fragment()
            this.context?.let { fragment.onAttach(it) }
            fragment.doSomething("")
            val ft = this.fragmentManager!!.beginTransaction()
            ft.addToBackStack(null)
            val replace = ft.replace(R.id.nav_host_fragment, fragment)
            ft.commit()
*/
        })

        return root
    }

   /* fun choose(s: String) {
        val fragment = com.example.myapplication.ui.fragment_ricetta.ListaRicette_Fragment()
        this.context?.let { fragment.onAttach(it) }
        fragment.doSomething(s)
        val fragmentTransaction = activity!!.getSupportFragmentManager().beginTransaction()
        fragmentTransaction.replace(R.id.nav_host_fragment, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }*///METODO CHE VA INSERITO PER RICHIAMARE IL FRAGMENT
}