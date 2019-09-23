package com.example.myapplication.ui.fragment_ricetta;

import android.annotation.SuppressLint;
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
import com.example.myapplication.ui.fragment_preferiti.Preferiti;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class FragmentDescrizione extends Fragment {
    private String descr,ingred,id_ricetta,id_utente;
    private FirebaseFirestore ff=FirebaseFirestore.getInstance();
    private  FloatingActionButton pref;
    private String id_pref;

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

            descr=bundle.get("descr").toString();
            ingred=bundle.get("info").toString();
            id_ricetta=bundle.get("id_ricetta").toString();

        }
        id_utente=FirebaseAuth.getInstance().getUid();

        TextView textDesc=(TextView)view.findViewById(R.id.tRicetta);
        textDesc.setText(descr);

        TextView textIngred=(TextView) view.findViewById(R.id.tIngredienti);
        textIngred.setText(ingred);

        verifica();

        pref=(FloatingActionButton)view.findViewById(R.id.floating_pref);
        pref.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                verifica();
                if(id_pref!=null)
                    rimuoviPreferiti();
                else
                    aggiungiPreferiti();
            }
        });
    }


    public void verifica(){

        ff.collection("preferiti").whereEqualTo("id_utente",id_utente).whereEqualTo("id_ricetta",id_ricetta).get().
                addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot snapshot= task.getResult();
                    if(!snapshot.getDocuments().isEmpty()){
                        id_pref=snapshot.getDocuments().get(0).getId();
                        pref.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.favorite));
                    }else pref.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.add_pref));
                }
            }
        });

    }

    public void rimuoviPreferiti(){
        ff.collection("preferiti").document(""+id_pref)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        pref.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.add_pref));
                        id_pref=null;
                        Log.d(TAG, "Elemento rimosso!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });

    }

    public void aggiungiPreferiti(){
        Preferiti preferiti=new Preferiti(id_ricetta,id_utente);
        ff.collection("preferiti").document().set(preferiti)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        pref.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.favorite));
                        Log.d(TAG, "Elemento aggiunto!");
                        id_pref=null;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
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
