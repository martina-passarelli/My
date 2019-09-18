package com.example.myapplication.ui.fragment_ricetta;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
import com.example.myapplication.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ListaRicette_Fragment extends Fragment{
    private MyAdapter tutorAdapter;
    private RecyclerView recyclerView;
    private View myView;
    private CollectionReference colR;
    private FirebaseFirestore ff= FirebaseFirestore.getInstance();
    private ArrayList<Ricetta> ricettaList=new ArrayList<>();
    private View view;
    private Bundle savedInstanceState;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tutorAdapter = new MyAdapter(ricettaList);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_item_list, container, false);
        recyclerView = myView.findViewById(R.id.list_ricetta);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(tutorAdapter);
        return myView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        TextInputLayout text_input=(TextInputLayout)view.findViewById(R.id.input_ricerca);
    }


    public void doSomething(String parms){

        if(parms.equals("search")){

            colR=ff.collection("ricette");
            Client client = new Client("348522f0fb1c5e16852ff83238805714", "fc1c214d14331aa60c3b706f5f725ee5");
            Index index = client.getIndex("index_object");


        }else{//DA SISTEMARE PER CATEGORIE
            colR = ff.collection("ricette"); //collezione riferita a ricett
            colR.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot d : list) {
                            String id = d.getId();
                            Ricetta ricetta = d.toObject(Ricetta.class);
                            ricetta.setId_ricetta(id);
                            ricettaList.add(ricetta);
                        }
                    }
                    tutorAdapter.notifyDataSetChanged();
                }
            });
        }

    }

    private ArrayList<String> creaPreferiti() {
        //SOSTITUIAMO UN FRAGMENT CON LE RICETTE PREFERITE DELL'UTENTE
        /*String utente= new Session(mContext).getusename();
        ArrayList<String> list = new ArrayList<>();

        if(utente.equals(null)==false) {
            UtenteRicetta ur = new UtenteRicetta(mContext);
            ur.open();
            Cursor c = ur.ottieniTutteRicette(utente);
            while (c.moveToNext()) {
                list.add(c.getString(2));
            }
            ur.close();
        }
        return list;
  */return new ArrayList<>();  }


    public void creaList(Cursor cursor){
       /* while(cursor.moveToNext()){
            String id=cursor.getString(0);
            String nome=cursor.getString(1);
            String categoria=cursor.getString(2);
            String descrizione=cursor.getString(3);
            String foto=cursor.getString(4);
            String ingredienti=cursor.getString(5);
            String ric=cursor.getString(6);
            Ricetta ricetta=new Ricetta(id,nome,categoria,descrizione,foto,ingredienti,ric);
            ricettaList.add(ricetta);
        }*/}

    private Context mContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }
   /* @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_item_list, container, false);
    }*/

}
