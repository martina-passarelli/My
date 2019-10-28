package com.example.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.myapplication.ui.fragment_evento.Evento;
import com.example.myapplication.ui.fragment_utente.Utente;

import com.example.myapplication.ui.home_page.ActivityHomePage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
/*
    Classe di utilitÃ  contenente metodi di gestione dell'eliminazione di un account
 */
public class UtilitaEliminaAccount {

    /*
        Metodo utilizzato per eliminare i commenti di un utente eliminato dalle ricette
    */
    public static void eliminaCommenti (String idUtente){
        FirebaseFirestore mDatabase = FirebaseFirestore.getInstance();
        CollectionReference commenti = mDatabase.collection("commenti");
        commenti.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot d : list) {
                        if (d.getString("id_utente").equals(idUtente))
                            commenti.document(d.getId()).delete();
                    }
                }
            }
        });
    }

    /*
        Metodo utilizzato per eliminare gli eventi creati da un utente cuoco oppure
        la sua partecipazione agli eventi
     */

    public static void eliminaEventiePartecipanti(String idUtente){
        FirebaseFirestore mDatabase = FirebaseFirestore.getInstance();
        CollectionReference eventi = mDatabase.collection("eventi");
        eventi.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(!queryDocumentSnapshots.isEmpty()){
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot d : list){
                        if(d.getString("id_cuoco").equals(idUtente))
                            eventi.document(d.getId()).delete();
                        else {
                            ArrayList<String> partecipanti = (ArrayList<String>) d.get("lista_part");
                            if(partecipanti!=null) {
                                if (partecipanti.contains(idUtente)) {
                                    partecipanti.remove(idUtente);
                                    Evento e = d.toObject(Evento.class);
                                    e.setLista_part(partecipanti);
                                    eventi.document(d.getId()).set(e);
                                }
                            }
                        }
                    }
                }
            }
        });
    }
    /*
        Metodo utilizzato per eliminare le ricette di un cuoco eliminato
     */

    public static void eliminaRicette(String idUtente){
        FirebaseFirestore mDatabase = FirebaseFirestore.getInstance();
        CollectionReference ricette = mDatabase.collection("ricette");
        ricette.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot d : list) {
                        if (d.getString("id_cuoco").equals(idUtente)) {
                            ricette.document(d.getId()).delete();
                        }
                    }
                }
            }
        });
    }

    /*
        metodo per eliminare la lista dei suggeriti
     */
    public static  void eliminaSuggeriti(String idUtente){
        FirebaseFirestore mDatabase = FirebaseFirestore.getInstance();
        CollectionReference ricette = mDatabase.collection("suggeriti");
        ricette.document(idUtente).delete();
    }

        /*
            metodo che richiama tutte le operazioni di eliminazione
         */

    public static void operazioniDiEliminazione(String idUtente, Context context) {
        FirebaseFirestore mDatabase = FirebaseFirestore.getInstance();
        DocumentReference docRef = mDatabase.collection("utenti2").document(idUtente);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String password = documentSnapshot.toObject(Utente.class).getPassword();
                String mail =documentSnapshot.toObject(Utente.class).getEmail();
                elimina(mail,password,idUtente, context);
                UtilitaEliminaAccount.eliminaFotoUtenteStorage(mail+".jpg");
            }
        });

    }

    /*
        metodo che effettua l'eliminazione dell'utente cuoco o normale dal db e ritorna alla
        schermata iniziale
     */

    private static void elimina(String mail, String password,String idUtente, Context context) {
        FirebaseFirestore mDatabase = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider.getCredential(mail, password);
        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task){
                        if (task.isSuccessful()) {
                            mDatabase.collection("utenti2").document(idUtente)
                                    .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(
                                            context, "Eliminazione avvenuta", Toast.LENGTH_LONG).show();
                                }
                            });
                            Intent i = new Intent(context, ActivityHomePage.class);
                            context.startActivity(i);
                        }
                    }
                });

            }
        });
    }
    /*
        metodo che mostra un dialog nel quale si chiede se si vuole realmente eliminare l account
     */
    public static void showDialog(Context context, String idUtente, int tipo) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle("");
        alertDialog.setMessage("Vuoi veramente eliminare l'account?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "SI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                UtilitaEliminaAccount.operazioniDiEliminazione(idUtente,context);
                // UtilitaEliminaAccount.eliminaCommenti(id);
                UtilitaEliminaAccount.eliminaEventiePartecipanti(idUtente);
                UtilitaEliminaAccount.eliminaSuggeriti(idUtente);

                if(tipo==1){
                    UtilitaEliminaAccount.eliminaRicette(idUtente);
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

    /*
        metodo che elimina la foto dell'utente dallo storage
     */
    public static void eliminaFotoUtenteStorage(String foto){
        StorageReference storage= FirebaseStorage.getInstance().getReference();
        if(foto !=null) {
            try {
                storage.child(foto).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                });
            } catch (Exception e) {

            }
        }
    }
}
