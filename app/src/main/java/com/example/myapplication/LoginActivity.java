package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    //riferimento al database
    private FirebaseAuth mAuth;
    //label della mail del login
    private AutoCompleteTextView mEmailView;
    //label della password del login
    private EditText mPasswordView;
    //textview di activity_login.xml
    private TextView t;
    //bottone login
    private Button mEmailSignInButton;

    private DatabaseReference mDatabase;
    private FirebaseUser user;

    private FirebaseFirestore firestore;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        firestore = FirebaseFirestore.getInstance();
        //inizializza i componenti della grafica di activity_login.xml
        initializeUI();

        //aggiunge un'azione al bottone di login
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUserAccount();
            }
        });
    }

    private void loginUserAccount() {
        final String email, password;
        mDatabase = FirebaseDatabase.getInstance().getReference();

        email = mEmailView.getText().toString();
        password = mPasswordView.getText().toString();
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(mEmailView.getText().toString());
        //se l'utente non ha inserito i campi appare un messaggio
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Please enter email...", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Please enter password!", Toast.LENGTH_LONG).show();
            return;
        }
        //controllo formato email, il formato della password è controllato già dal sistema
        if (!matcher.matches()) {
            Toast.makeText(getApplicationContext(), "Formato email non corretta", Toast.LENGTH_LONG).show();
            return;
        }


        // prova a fare l'accesso, se l'account non esiste, lo crea
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        //se la password e la mail sono corrette, viene effettuato il login
                        if (task.isSuccessful()) {
                            //APRI USER PROFILE
                            Toast.makeText(getApplicationContext(), "Login avvenuto con successo", Toast.LENGTH_LONG).show();
                             user= FirebaseAuth.getInstance().getCurrentUser() ;
                            Intent intent = new Intent(LoginActivity.this, UserProfileActivity.class);
                           startActivity(intent);




                        }

                        //se la password e la mail non corrispondono a nessun profilo, viene creato un account
                        else {
                            mAuth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                FirebaseUser user = mAuth.getCurrentUser();
                                                Toast.makeText(getApplicationContext(), "Registratione avvenuta!", Toast.LENGTH_LONG).show();
                                                user = FirebaseAuth.getInstance().getCurrentUser();


                                                CollectionReference utenti= firestore.collection("utenti");
                                                Utente nuovoUtente = new Utente(user.getEmail(),password);
                                                //la key dell'utente è quella del suo identificativo
                                                //negli utenti loggati
                                                utenti.document(""+mAuth.getUid()).set(nuovoUtente);

                                                //vai a USER PROFILE

                                                Intent intent = new Intent(LoginActivity.this, UserProfileActivity.class);

                                                 startActivity(intent);



                                             }

                                         else

                                        {

                                            Toast.makeText(getApplicationContext(), "User Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    });
                        }
                    }

                });
    }

    //prende i riferimenti alle label della gui
    private void initializeUI() {
        mEmailView = findViewById(R.id.username);
        mPasswordView = findViewById(R.id.password);
        mEmailSignInButton = findViewById(R.id.login);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);

    }


}

