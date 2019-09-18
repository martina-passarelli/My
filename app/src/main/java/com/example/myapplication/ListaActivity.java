package com.example.myapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.example.myapplication.ui.fragment_ricetta.ListaRicette_Fragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import android.view.View;

public class ListaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);

        ListaRicette_Fragment fragment=new ListaRicette_Fragment();
       // fragment.onAttach(this.getApplicationContext());
        fragment.doSomething("tutte");
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment,fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextInputLayout text_input; text_input=findViewById(R.id.input_ricerca);


        //---------------------QUERY PER LA RICERCA-------------------------------------------------

        FloatingActionButton fab = findViewById(R.id.fab_search);
        fab.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View view) {
                String testo=text_input.getEditText().getText().toString().toLowerCase().trim();
                ListaRicette_Fragment fragment=new ListaRicette_Fragment();
                fragment.search(testo);
                text_input.setVisibility(View.GONE);
                fab.setVisibility(View.GONE);
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment,fragment);
                fragmentTransaction.addToBackStack("Lista ricette");
                fragmentTransaction.commit();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
