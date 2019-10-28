package com.example.myapplication.ui.home_page;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
/*
    Questa classe rappresenta l'activity principale dove l'utente che userà l'applicazione decidere
    di iscriversi come utente "normale" oppure come un cuoco.

 */

public class ActivityHomePage extends AppCompatActivity {
    private FirebaseAuth mAuth;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_login);
        mAuth = FirebaseAuth.getInstance();


        // all'apertura dell'app, se c'è già un utente loggato, verrà aperta
        //direttamente la main activity

        if (mAuth.getCurrentUser() != null &&  mAuth.getCurrentUser().isEmailVerified() ) {
            try {
                Intent i = new Intent(ActivityHomePage.this, MainActivity.class);
                startActivity(i);
            }finally {
                finish();
            }
        }
        //se invece non c'è nessun utente loggato, verra aperta la schermata principale dove
        //l'utente potrà fare l accesso o iscriversi
        //stessa cosa se l'utente non ha ancora verificato la sua mail

        else if ( mAuth.getCurrentUser()==null || ! mAuth.getCurrentUser().isEmailVerified()){
            FragmentHomeWithLink fragment = new FragmentHomeWithLink();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_homePage, fragment);
            fragmentTransaction.commit();
        }
    }

}
