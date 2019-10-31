package com.example.myapplication.ui.fragment_evento;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.ActivityMappa;
import com.example.myapplication.R;
import com.example.myapplication.ui.SwipeToDeleteCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.getDefaultSize;
import static androidx.constraintlayout.widget.Constraints.TAG;
/*
LA CLASSE SI OCCUPA DI GESTIRE IL FRAMMENTO CONTENENTE LA LISTA DI EVENTI NEI DIVERSI CASI POSSIBILI:
1. LISTA DEGLI EVENTI A CUI PARTECIPA UN UTENTE;
2. LISTA DEGLI EVENTI TOTALI.
3. LISTA DEGLI EVENTI DI UN CUOCO.
 */

public class Lista_Fragment_Evento extends Fragment {
    private ArrayList<Evento> list=new ArrayList<>();
    private RecyclerView recyclerView;
    private View myView;
    private FirebaseFirestore ff= FirebaseFirestore.getInstance();
    private Adapter_Evento tutorAdapter;
    public String id_utente="null";
    private TextView text_etichettaVista;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tutorAdapter = new Adapter_Evento(list);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_evento, container, false);
        recyclerView = myView.findViewById(R.id.lista_eventi);
        text_etichettaVista=getActivity().findViewById(R.id.etichetta_vista);

        //BISOGNA RICEVERE VIA BUNDLE
        Bundle bundle=this.getArguments();
        if(bundle!=null) {
            id_utente = bundle.getString("id"); //Serve per l'abilitazione di eliminazione eventi
        }

        if(id_utente.equals(FirebaseAuth.getInstance().getUid())){
            enableSwipeToDeleteAndUndo();
        }

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(tutorAdapter);
        return myView;
    }



    /*
    IL METODO SERVE AD ATTIVARE LA SCROLL PER ELIMINARE GLI EVENTI
     */
    private void enableSwipeToDeleteAndUndo() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this.getContext()) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                final int position = viewHolder.getAdapterPosition();
                final Evento item = tutorAdapter.getData().get(position);
                tutorAdapter.removeItem(item.getId(),position);
                ConstraintLayout linear_listaEventi= myView.findViewById(R.id.linear_listaEventi);
                Snackbar snackbar = Snackbar.make(linear_listaEventi, "Item was removed from the list.", Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        tutorAdapter.restoreItem(item, position);
                        recyclerView.scrollToPosition(position);
                    }
                });
                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.show();
            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(recyclerView);
    }

/*------------------------IN Fragment_Cuoco---------------------------------------------------------
IL METODOO doSomething(String id_cuoco) VIENE RICHIAMATO NEL PROFILO DI UN CUOCO, QUANDO SI DESIDERA
VISUALIZZARE GLI EVENTI DA ESSO CREATI. POICHE' ALL'INTERNO DI OGNI EVENTO NEL FIREBASE E' PRESENTE
L'ID DEL CUOCO, BASTA EFFETUARE UNA QUERY SU ESSO PER PRELEVARE GLI EVENTI DI INTERESSE.
 */

    public void doSomething(String id_cuoco){
        //PRENDIAMO TUTTI GLI EVENTI COLLEGATI ALL'UTENTE IN INPUT
        ff.collection("eventi").whereEqualTo("id_cuoco",id_cuoco).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                     for (QueryDocumentSnapshot document : task.getResult()) {
                        Evento evento = document.toObject(Evento.class);
                        if(ActivityMappa.nonScaduto(evento.getData(), evento.getOra())){
                            list.add(evento);
                        }

                    }
                    tutorAdapter.notifyDataSetChanged();
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }
    //----------------------------------------------------------------------------------------------
    /*---------------------IN I miei eventi---------------------------------------------------------
    ALL'INTERNO DEL FIRESTORE, NELLA SEZIONE UTENTE, E' PRESENTE UNA LISTA DI STRINGHE CORRISPONDENTI
     AGLI ID DEGLI EVENTI A CUI ESSO PARTECIPA.
     PER PRELEVARE QUESTA LISTA VIENE USATO IL METODO eventi_utente(), MENTRE PER PRELEVARE I DATI
     DELL'EVENTO E SISTEMARE LA VISTA, VIENE USATO add_evento(String s). RICORDIAMO CHE E' POSSIBILE
     VISUALIZZARE SOLO GLI EVENTI NON SCADUTI.

     */
    public void eventi_utente(){
        //PRENDIAMO TUTTI GLI EVENTI A CUI PARTECIPERA' L'UTENTE CORRENTE

        ff.collection("utenti2").document(""+FirebaseAuth.getInstance().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                ArrayList<String> lista_eventi =new ArrayList<>();
                text_etichettaVista.setText("Non sei iscritto a nessun evento!\nInizia a guardarti intorno!");
                if(documentSnapshot.get("lista_eventi")!=null) {
                    lista_eventi = (ArrayList<String>) documentSnapshot.get("lista_eventi");
                    list.clear();
                    if(lista_eventi.size()!=0) {
                        text_etichettaVista.setText("I tuoi eventi");

                        for (String s : lista_eventi) {
                            add_evento(s);
                        }

                    }
                }
            }
        });
    }


    public void add_evento(String s){
        ff.collection("eventi").document(""+s).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Evento evento=documentSnapshot.toObject(Evento.class);
                if(evento!=null) {
                    list.add(evento);
                    tutorAdapter.notifyDataSetChanged();
                }
            }
        });
    }


    //----------------------------------------------------------------------------------------------
    /*----------------NELLA PAGE DELL'HOME: EVENTI--------------------------------------------------
    IL METODO eventi_Totali() VIENE RICHIAMATO PER PRELEVARE TUTTI GLI EVENTI PRESENTI NEL FIREBASE
    CHE NON SONO SCADUTI.
     */

    public void eventiTotali(){
        ff.collection("eventi").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    List<DocumentSnapshot> lista = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot d : lista) {
                        Evento evento= d.toObject(Evento.class);
                        if(ActivityMappa.nonScaduto(evento.getData(), evento.getOra())){
                            list.add(evento);
                        }
                    }
                    tutorAdapter.notifyDataSetChanged();
                }
            }

        });

    }

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
}
