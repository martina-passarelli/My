package com.example.myapplication.ui.home;


import android.view.View;
import android.widget.TextView;
import com.example.myapplication.ui.fragment_ricetta.MyAdapter;
import com.example.myapplication.ui.fragment_ricetta.Ricetta;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
/*
    Questa classe di utilità viene utilizzata per gestire le ricette suggerite mostrate nella MainActivity.
 */
public class UtilitaSuggeriti {
    //si è supposto che ci siano delle parole comuni che l'utente potrebbe utilizzare per ottenere una ricetta
    //oltre che alle categorie già esistenti
    private static String [] parole = {"torta","ciambellone","crostata","cheesecake","biscotti","mousse",
     "cioccolato","crema","nutella","caffè","mele","limone"};

   private static ArrayList<String> paroleChiavi = new ArrayList<>( Arrays.asList(parole));

   /*
     Questo metodo serve per verificare che l'utente abbia una lista dei suggeriti
    */
    public static void cercaRicette(MyAdapter myAdapter, ArrayList<Ricetta> lista, TextView testoSugg){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("suggeriti").document(mAuth.getUid())
                 .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {

             @Override
             public void onSuccess(DocumentSnapshot documentSnapshot) {
                 //nella collection di firebase si ha un array di long
                 ArrayList <Long> suggeriti = (ArrayList<Long>) documentSnapshot.get("suggeriti");
                 //è stato usato uno strem che scandisce l'array dei suggeriti e restituisce
                 //true se è stato trovato nella lista un valore >0

                 boolean pieno = suggeriti.stream().anyMatch(integer ->integer>0);
                 //se non ha ancora effettuato ricerche rendiamo invisibile il testo dei suggeriti.
                 //non è necessario rendere invisibile la recycle view che contiene la lista delle ricette
                 //suggerite in quanto, essendo un contenitore, se non ha elementi non è visibile
                 if(! pieno){
                     testoSugg.setVisibility(View.GONE);
                     return;
                 }
                 //se ha effettuato ricerche, sono mostrati i suggeriti
                 else{
                     testoSugg.setVisibility(View.VISIBLE);
                     trovaSuggeriti(suggeriti,lista,myAdapter);
                 }
             }
         });
    }
    /*
        Questo metodo viene impiegato per trovare le ricette da suggerire all'utente.
        MyAdapter è la classe adapter che si occupa della gestione della lista di ricette.
        Si veda fragment_ricetta.MyAdapter.java
     */

    private static void trovaSuggeriti( ArrayList<Long> suggeriti, ArrayList<Ricetta> lista, MyAdapter myAdapter) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        // la lista delle ricerche dell'utente viene ordinata ottenendo gli indici in modo da risalire
        //alla stringa della ricerca
        int[] sortedIndices = IntStream.range(0, suggeriti.size())
                .boxed().sorted((i, j) -> suggeriti.get(j).compareTo(suggeriti.get(i)))
                .mapToInt(ele -> ele).toArray();
        lista.clear();
        firestore.collection("ricette").
                get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                    //si vogliono al più 4 ricette suggerite
                    for (int indici = 0; indici < 4; indici++) {
                        //se non ci sono click su quella key non interpello il db
                        if(suggeriti.get(sortedIndices[indici])>0) {
                            for (DocumentSnapshot d : list) {
                                Ricetta ricetta = d.toObject(Ricetta.class);
                                if (ricetta.getNome().toLowerCase().trim().contains(parole[sortedIndices[indici]]) ||
                                        ricetta.getDescrizione().toLowerCase().trim().contains(parole[sortedIndices[indici]]) ||
                                        ricetta.getIngredienti().toLowerCase().trim().contains(parole[sortedIndices[indici]]) ||
                                        ricetta.getCategoria().toLowerCase().trim().contains(parole[sortedIndices[indici]])) {
                                    String id = d.getId();
                                    ricetta.setId_ricetta(id);
                                    // aggiunge solo se la lista non ha già il dolce trovato
                                    if (!lista.contains(ricetta)) {
                                        lista.add(ricetta);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    //si notifica all'adapter che la lista è cambiata
                    myAdapter.notifyDataSetChanged();
                }

            }
        });
    }
    /*
        Questo metodo viene utilizzato per aggiornare le ricerche dell'utente
        dopo aver cliccato su una categoria oppure dopo aver fatto una ricerca

     */
    public static void updateStringaSuggeriti(String ricerca){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("suggeriti").document(mAuth.getUid())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if(paroleChiavi.contains(ricerca)){
                        int pos = paroleChiavi.indexOf(ricerca);
                        ArrayList<Long> sugg = (ArrayList<Long>) documentSnapshot.get("suggeriti");
                        sugg.set(pos,sugg.get(pos)+1);
                        Map<String,ArrayList<Long>> up =  new HashMap<>();
                        up.put("suggeriti",sugg);
                        firestore.collection("suggeriti").document(mAuth.getUid()).set(up);
                    }
                }

        });
    }
}
