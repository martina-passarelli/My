package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import com.example.myapplication.ui.fragment_ricetta.ListaRicette_Fragment;
import com.google.firebase.auth.FirebaseAuth;

/*
    Questa classe è l'activity che conterrà la lista dei preferiti
 */
public class ListaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent= getIntent();
        String categoria=intent.getStringExtra("categoria");
        String not_pref=intent.getStringExtra("preferiti");
        if(categoria!=null) {
            ListaRicette_Fragment fragment = new ListaRicette_Fragment();
            fragment.doSomething(categoria);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            fragmentTransaction.replace(R.id.fragment, fragment);
            fragmentTransaction.commit();
        }else if(not_pref!=null){
            String testo=intent.getStringExtra("testo");
            ListaRicette_Fragment fragment = new ListaRicette_Fragment();
            fragment.search(testo);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            fragmentTransaction.replace(R.id.fragment, fragment);
            fragmentTransaction.commit();
        }else{
            ListaRicette_Fragment fragment=new ListaRicette_Fragment();
            fragment.trovaPreferiti(FirebaseAuth.getInstance().getUid());
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment,fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
