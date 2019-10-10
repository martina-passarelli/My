package com.example.myapplication;

import com.example.myapplication.ui.fragment_evento.Evento;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UtilitaEliminaAccount {
    ///***classe di utilità per eliminare i riferimenti degli account eliminati



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
}
