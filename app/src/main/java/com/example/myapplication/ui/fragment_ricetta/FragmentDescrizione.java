package com.example.myapplication.ui.fragment_ricetta;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import static androidx.constraintlayout.widget.Constraints.TAG;

/*
    Questa classe rappresenta il frammento con la descrizione della ricetta
 */
public class FragmentDescrizione extends Fragment {
    private String descr,ingred,id_ricetta,id_utente,ricetta;
    private FirebaseFirestore ff=FirebaseFirestore.getInstance();
    //bottone per aggiungere la ricetta tra i preferiti
    private  FloatingActionButton pref;
    private ArrayList<String> preferiti = new ArrayList<>();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        View view= inflater.inflate(R.layout.fragment_descr, parent, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Bundle bundle = this.getArguments();
        if(bundle != null){
            ricetta=bundle.getString("ricetta");
            descr=bundle.get("descr").toString();
            ingred=bundle.get("info").toString();
            id_ricetta=bundle.get("id_ricetta").toString();
        }

        id_utente=FirebaseAuth.getInstance().getUid();

        TextView textDesc=(TextView)view.findViewById(R.id.tRicetta);
        textDesc.setText(ricetta);

        TextView textIngred=(TextView) view.findViewById(R.id.tIngredienti);
        textIngred.setText(ingred);

        verifica();

        pref=(FloatingActionButton)view.findViewById(R.id.floating_pref);
        pref.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                ff.collection("utenti2").document(""+id_utente).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot!=null) {
                            preferiti=(ArrayList<String>) documentSnapshot.get("lista_preferiti");
                            if(preferiti!=null && preferiti.contains(id_ricetta)){
                                rimuoviPreferiti(preferiti);
                            }else if(preferiti!=null){
                                aggiungiPreferiti(preferiti);
                            }else aggiungiPreferiti(new ArrayList<String>());
                        }
                    }
                });
            }
        });
    }

    /*
        Questo metodo verifica che non compaia la stessa ricetta due volte nella lista
     */
    public void verifica(){
        ff.collection("utenti2").document(""+id_utente).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot!=null) {
                    ArrayList<String> lista_preferiti = (ArrayList<String>) documentSnapshot.get("lista_preferiti");
                    if(lista_preferiti!=null && lista_preferiti.contains(id_ricetta)){
                        pref.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.favorite));
                    }else{
                        pref.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.add_pref));
                    }
                }
            }
        });
    }

    /*
        Questo metodo rimuove la ricetta dai preferiti
     */
    public void rimuoviPreferiti(ArrayList<String> lista_preferiti){
        lista_preferiti.remove(id_ricetta);
        aggiorna_utente(lista_preferiti);
        pref.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.add_pref));
    }

    /*
            Questo metodo aggiunge la ricetta tra i preferiti
         */
    public void aggiungiPreferiti(ArrayList<String> lista_preferiti){
        lista_preferiti.add(id_ricetta);
        aggiorna_utente(lista_preferiti);
        pref.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.favorite));
    }

    /*
        Questo metodo aggiorna i preferiti dell'utente nel db
     */

    private void aggiorna_utente(ArrayList<String> lista_preferiti) {

        ff.collection("utenti2").document(""+id_utente).update("lista_preferiti",lista_preferiti)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "DocumentSnapshot successfully updated!");
            }
        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });
    }


    @Override
    public void onAttach(Context context){
        super.onAttach(context);
    }
    @Override
    public void onDetach(){
        super.onDetach();
    }
}
