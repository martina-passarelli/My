package com.example.myapplication;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

        public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
                Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        private FirebaseAuth mAuth;
        private AutoCompleteTextView mEmailView;
        private EditText mPasswordView;
        private View mProgressView;
        private View mLoginFormView;
        private TextView t;
        private Button mEmailSignInButton;



        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_login);

            mAuth = FirebaseAuth.getInstance();

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
            final String email,password;

            email = mEmailView.getText().toString();
            password = mPasswordView.getText().toString();
            Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(mEmailView.getText().toString());
            //se l'utente non ha inserito i campi appare un messaggio
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(getApplicationContext(), "Please enter email...", Toast.LENGTH_LONG).show();
                return;
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(getApplicationContext(), "Please enter password!", Toast.LENGTH_LONG).show();
                return;
            }
            //controllo formato email e password
            if (!matcher.matches()) {
                Toast.makeText(getApplicationContext(), "Formato email non corretta", Toast.LENGTH_LONG).show();
                return;
            }
            if (password.length() < 5) {
                Toast.makeText(getApplicationContext(), "La password deve essere di almeno 5" +
                        "caratteri", Toast.LENGTH_LONG).show();
                return;
            }



            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            //se la password e la mail sono corrette, viene effettuato il login
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Login avvenuto con successo", Toast.LENGTH_LONG).show();

                                //APRI USER PROFILE

                                // Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                                //startActivity(intent);
                            }

                            //se la password e la mail non corrispondono a nessun profilo, viene creato un account
                            else {
                                mAuth.createUserWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(getApplicationContext(), "Registratione avvenuta!", Toast.LENGTH_LONG).show();
                                                    //vai a USER PROFILE

                                                    //  Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                                                    // startActivity(intent);
                                                } else {
                                                    Toast.makeText(getApplicationContext(), "Registration failed! Please try again later", Toast.LENGTH_LONG).show();

                                                }
                                            }
                                        });
                            }
                        }

                    });
        }

        private void initializeUI() {
            mEmailView = findViewById(R.id.username);
            mPasswordView = findViewById(R.id.password);

            mEmailSignInButton = findViewById(R.id.login);

        }
    }

