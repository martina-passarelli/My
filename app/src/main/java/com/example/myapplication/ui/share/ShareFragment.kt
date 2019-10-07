package com.example.myapplication.ui.share

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.myapplication.R
import com.example.myapplication.ui.fragment_cuoco.Griglia_Cuochi
import com.example.myapplication.ui.fragment_seguiti.ListSeguiti
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ShareFragment : Fragment() {

    private lateinit var shareViewModel: ShareViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        shareViewModel =
            ViewModelProviders.of(this).get(ShareViewModel::class.java)
        val root = inflater.inflate(R.layout.grid_cuochi, container, false)



        val fragment = Griglia_Cuochi()
        val fragmentTransaction = activity!!.supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.nav_host_fragment, fragment)
        fragmentTransaction.commit()



        //---------------------QUERY PER LA RICERCA-------------------------------------------------


        return root
    }
}