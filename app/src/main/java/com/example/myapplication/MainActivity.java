package com.example.myapplication;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.myapplication.ui.fragment_cuoco.Cuoco;
import com.example.myapplication.ui.fragment_utente.Utente;
import com.example.myapplication.ui.home_page.ActivityHomePage;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/*
    Questa classe rappresenta l'activity principale che verrà aperta con l'apertura dell'app.
 */
public class MainActivity extends AppCompatActivity {
    private AppBarConfiguration appBarConfiguration;

    //imageview di nav_header_main.xml (immagine del profilo dell'utente loggato nel menu a lato
    private CircleImageView imageMenu;
    //textView della mail dell'utente loggato nel menu a lato
    private TextView mailMenu;
    //textView del nome dell'utente nel menu a lato
    private TextView nomeMenu;

    //riferimento all'utente loggato
    private FirebaseAuth mAuth;
    //riferimento al firestore
    private FirebaseFirestore firestore;
    //riferimento allo storage
    private StorageReference storage;

    private Utente utente;
    private Cuoco cuoco;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));

        //inizializzazione delle componenti
        mAuth= FirebaseAuth.getInstance();
        firestore=FirebaseFirestore.getInstance();
        storage= FirebaseStorage.getInstance().getReference();

        NavigationView navigationView =findViewById(R.id.nav_view);
        View hview = navigationView.getHeaderView(0);
        mailMenu= hview.findViewById(R.id.mailMenu);
        nomeMenu= hview.findViewById(R.id.nomeMenu);
        imageMenu= hview.findViewById(R.id.imageMenu);

        //inizializzazione della ricezione delle notifiche
        FirebaseMessaging.getInstance().subscribeToTopic("pushNotifications");
        FirebaseMessaging.getInstance().unsubscribeFromTopic("pushNotifications");

        //Per sicurezza si controlla che l'utente sia loggato(dovrebbe esserlo)
        if(mAuth.getCurrentUser()!=null){
            //Viene controllato che la mail dell'utente sia stata verificata. Questo controllo lo si fa
            //perché, all'atto dell'iscrizione, dopo aver mandato la mail di verifica,
            //firebase salva comunque come loggato l'utente. Se la mail non è stata verificata
            //si riporta l'utente al di login e gli si mostra uno showDialog per ricordargli
            //di controllare la mail
            if(mAuth.getCurrentUser().isEmailVerified()){
                //viene preso il riferimento all'utente per caricarne le informazioni
                DocumentReference docRef = firestore.collection("utenti2").document(mAuth.getUid());
                docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        //sia il tipo Utente che il tipo Cuoco hanno un campo "tipo". Se tipo == 0 allora
                        //il profilo è di un utente
                        if(documentSnapshot.getDouble("tipo")==0){
                            utente=documentSnapshot.toObject(Utente.class);
                            //verifico che l'utente abbia l'immagine del profilo per caricarla
                            if(utente.getImageProf()!=null){
                                storage.child(utente.getImageProf()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Picasso.with(MainActivity.this).load(uri).networkPolicy(NetworkPolicy.OFFLINE)
                                                .rotate(utente.getRot()).fit().centerCrop().into(imageMenu, new Callback() {
                                            @Override
                                            public void onSuccess() {

                                            }
                                            //se l'immagine non è in cache la carica
                                            @Override
                                            public void onError() {
                                                Picasso.with(MainActivity.this).load(uri).rotate(utente.getRot())
                                                        .fit().centerCrop().into(imageMenu);
                                            }
                                        });
                                    }
                                });
                            }
                            mailMenu.setText(utente.getEmail());
                            nomeMenu.setText(utente.getNome());
                        }
                        //se tipo==1 è un cuoco
                        else {
                            cuoco = documentSnapshot.toObject(Cuoco.class);
                            if (cuoco.getImageProf() != null) {
                                storage.child(cuoco.getImageProf()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Picasso.with(MainActivity.this).load(uri).networkPolicy(NetworkPolicy.OFFLINE)
                                                .rotate(cuoco.getRot()).fit().centerCrop().into(imageMenu, new Callback() {
                                            @Override
                                            public void onSuccess() {

                                            }

                                            //se l'immagine non è in cache la carica
                                            @Override
                                            public void onError() {
                                                Picasso.with(MainActivity.this).load(uri).rotate(cuoco.getRot())
                                                        .fit().centerCrop().into(imageMenu);
                                            }
                                        });
                                    }
                                });
                            }
                            mailMenu.setText(cuoco.getEmail());
                            nomeMenu.setText(cuoco.getNome());
                        }
                    }
                });
            }
            //se la mail non è stata verificata lo si riporta al login
            else{
                Intent intent = new Intent(MainActivity.this, ActivityHomePage.class);
                startActivity(intent);
            }

        }

        //cliccando sull'imageView del menu si va al profilo utente
        imageMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mAuth.getCurrentUser()!=null){
                    DocumentReference docRef= firestore.collection("utenti2").document(mAuth.getUid());
                    docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if(documentSnapshot.getDouble("tipo")==0)
                                vai_profilo("utente");
                            else
                                vai_profilo("cuoco");
                        }
                    });
                }
            }
        });

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navView = findViewById(R.id.nav_view);

        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow,
                R.id.nav_tools, R.id.nav_send)
                .setDrawerLayout(drawerLayout)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

    }

    /*
        Questo metodo provvede a caricare il giusto profilo
     */
    private void vai_profilo(String s) {
        Intent i = new Intent(MainActivity.this,ProfiloActivity.class);
        i.putExtra("tipo", "login");
        i.putExtra("utente", "");
        i.putExtra("tipo_utente", s);
        startActivity(i);
    }

    /*
        Questo metodo imposta il menu con i 3 puntini al lato, contenente il logout
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.impostazione_menu, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.fragment);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }


    /*
        Questo metodo gestisce il logout mostrando un dialog
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.esci && mAuth.getCurrentUser()!=null){
            showDialogLogOut();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDialogLogOut() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("");
        alertDialog.setMessage("Vuoi veramente uscire?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "SI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
               FirebaseAuth.getInstance().signOut();
               firestore.collection("utenti2").document("" +mAuth.getCurrentUser()).update("token_id","");
              try{
                  Intent intent= new Intent(MainActivity.this, ActivityHomePage.class);
                  startActivity(intent);
                  Toast.makeText(MainActivity.this, "Logout avveuto con successo", Toast.LENGTH_SHORT).show();
              }finally {
                  finish();
              }


            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
            }
        });
        alertDialog.show();
    }


}
