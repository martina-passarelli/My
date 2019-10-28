package com.example.myapplication.ui.home_page;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.myapplication.ProfiloActivity;
import com.example.myapplication.R;
import com.example.myapplication.ui.fragment_cuoco.Cuoco;
import com.example.myapplication.ui.fragment_utente.Utente;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/*
    Questa classe rappresenta il frammento del login
 */
public class FragmentLogin extends Fragment {
    private Context context;
    private Bundle bundle;
    private String tipoUtente;
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    //riferimento al database
    private FirebaseAuth mAuth;
    //label della mail del login
    private AutoCompleteTextView mEmailView;
    //label della password del login
    private EditText mPasswordView;

    //bottone login
    private Button mEmailSignInButton;

    private DatabaseReference mDatabase;
    private FirebaseUser user;

    private FirebaseFirestore firestore;
    private View myView;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

    }


    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment

        myView= inflater.inflate(R.layout.fragment_login, parent, false);
        context=inflater.getContext();
        return myView ;

    }

    @SuppressLint({"ResourceAsColor", "RestrictedApi"})
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        bundle = this.getArguments();

        if(bundle != null){
            tipoUtente=bundle.getString("tipo");
        }
        firestore = FirebaseFirestore.getInstance();

        //inizializza i componenti della grafica di activity_login.xml
        initializeUI(myView);

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
        //la mail che ha inserito l'utente viene valutata tramite una regex
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(mEmailView.getText().toString());
        //se l'utente non ha inserito correttamente i campi appare un messaggio
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(context, "Per favore inserisci una mail...", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(context, "Per favore inserisci la password!", Toast.LENGTH_LONG).show();
            return;
        }
        //controllo del formato email, il formato della password è controllato già dal sistema
        if (!matcher.matches()) {
            Toast.makeText(context, "Formato email non corretta", Toast.LENGTH_LONG).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //se la password e la mail sono corrette, viene effettuato il login
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Login avvenuto con successo", Toast.LENGTH_LONG).show();
                            user= FirebaseAuth.getInstance().getCurrentUser() ;
                            DocumentReference docRef = firestore.collection("utenti2").document(""+user.getUid());
                            String token= FirebaseInstanceId.getInstance().getToken();
                            docRef.update("token_id",token);
                            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    //sia utenti che cuochi sono in una stessa collection. Entrambi possiedono
                                    //un double "tipo" tramite il quale riuscire ad indentificare il tipo
                                    //dell'utente e aprire il giusto profilo
                                    //+++++++++++
                                    if(user.isEmailVerified()) {
                                        if (documentSnapshot.getDouble("tipo") == 0) {
                                            vaiProfilo("utente");
                                        } else {
                                            vaiProfilo("cuoco");
                                        }
                                    }else {
                                        showDialogConfermaEmail();
                                        // Toast.makeText(context, "verifica la mail " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }

                        //se la password e la mail non corrispondono a nessun profilo, viene creato un account
                        else {
                            mAuth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                //Toast.makeText(context, "Registratione avvenuta!", Toast.LENGTH_LONG).show();
                                                user = FirebaseAuth.getInstance().getCurrentUser();
                                                //invia la mail di conferma registrazione


                                                //+++++++++++++++++
                                                user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        CollectionReference utenti = firestore.collection("utenti2");
                                                        //se è un cuoco
                                                        if (tipoUtente.equals("cuoco")){
                                                            Cuoco nuovoCuoco = new Cuoco(user.getEmail(),password);
                                                            nuovoCuoco.setEmail(user.getEmail());
                                                            //non conoscendo all'inizio il nome dell'utente, lo si setta
                                                            //come la prima parte della sua mail
                                                            nuovoCuoco.setNome(user.getEmail().substring(0,user.getEmail().indexOf("@")));
                                                            //il nome della foto del profilo, una volta che l'utente la sceglierà
                                                            //durante la modifica del suo profilo, è la sua mail.jpg
                                                            nuovoCuoco.setImageProf(user.getEmail()+".jpg");
                                                            //la key dell'utente è quella del suo identificativo
                                                            //negli utenti loggati
                                                            String token= FirebaseInstanceId.getInstance().getToken();
                                                            nuovoCuoco.setToken(token);
                                                            utenti.document(""+mAuth.getUid()).set(nuovoCuoco);
                                                            //vai al profilo del cuoco
                                                            // vaiProfilo("cuoco");
                                                            //ogni utente, cuoco o utente normale che sia, ha una propria lista
                                                            //tramite la quale riuscire a capire i suoi gusti e suggerire
                                                            //le cose a cui più è interessato
                                                            creaListaSuggeriti();
                                                        }
                                                        //se è un utente
                                                        else {
                                                            Utente nuovoUtente = new Utente();
                                                            nuovoUtente.setEmail(user.getEmail());
                                                            nuovoUtente.setPassword(password);
                                                            nuovoUtente.setNome(user.getEmail().substring(0, user.getEmail().indexOf("@")));
                                                            nuovoUtente.setNick(user.getEmail().substring(0, user.getEmail().indexOf("@")));
                                                            nuovoUtente.setImageProf(user.getEmail() + ".jpg");
                                                            nuovoUtente.setBio("");
                                                            String token= FirebaseInstanceId.getInstance().getToken();
                                                            nuovoUtente.setToken(token);
                                                            utenti.document("" + mAuth.getUid()).set(nuovoUtente);
                                                            //vai a profilo utente
                                                            // vaiProfilo("utente");
                                                            creaListaSuggeriti();
                                                        }
                                                        //+++++
                                                        showDialog();
                                                        mEmailView.setText("");
                                                        mPasswordView.setText("");
                                                    }
                                                });

                                            }
                                            else
                                            {
                                                Toast.makeText(context, "User Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }



                });
    }
    /*
        Questo metodo provvede a creare nella collection suggeriti, una entry che ha come chiave
        l'identificativo dell'utente loggato e come valore una lista.
        Per semplicità si sono impostati dei campi di default che verranno usati per monitorare
        l'utente.
        Si veda nel dettaglio la classe home.UtilitaSuggeriti.java
     */
    private void creaListaSuggeriti() {
        CollectionReference suggeriti = firestore.collection("suggeriti");
        ArrayList<Long> sugg = new ArrayList<>();
        for(int i=0; i<12;i++) sugg.add((long) 0);
        HashMap<String, ArrayList<Long>> map = new HashMap<>();
        map.put("suggeriti",sugg);
        suggeriti.document(mAuth.getUid()).set(map);
    }

    /*
        Questo metodo inizializza i campi della view
     */
    private void initializeUI(View view) {
        mEmailView = view.findViewById(R.id.username);
        mPasswordView = view.findViewById(R.id.password);
        mEmailSignInButton = view.findViewById(R.id.login);
    }

    /*
        Questo metodo permette di andare nel profilo dell'utente cuoco o utente normale
     */

    public void vaiProfilo(String tipo_utente){
        Intent intent = new Intent(context, ProfiloActivity.class);
        intent.putExtra("tipo","login");
        intent.putExtra("utente","");
        intent.putExtra("tipo_utente",tipo_utente);
        startActivity(intent);
    }

    /*
        Questo metodo serve per mostrare un avviso all'utente per informarlo che non potrà
        accede alla sua area privata fintanto che non ha confermato la sua mail
     */
    private void showDialogConfermaEmail() {
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .create();
        alertDialog.setTitle("");
        alertDialog.setMessage("Non hai ancora confermato la tua mail");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Ho capito",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.BLACK);
            }
        });
        alertDialog.show();
    }

    /*
        Questo metodo serve per mostare un avviso all'utente nel quale viene
        informato che gli è stata inviata una mail per confermare il suo account
     */
    private void showDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .create();
        alertDialog.setTitle("");
        alertDialog.setMessage("Controlla la tua posta, ti abbiamo inviato una mail di conferma");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Va bene",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.BLACK);
            }
        });
        alertDialog.show();
    }


}
