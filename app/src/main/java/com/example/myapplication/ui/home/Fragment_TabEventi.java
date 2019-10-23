package com.example.myapplication.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.ActivityMappa;
import com.example.myapplication.R;
import com.example.myapplication.ui.fragment_evento.Adapter_Evento;
import com.example.myapplication.ui.fragment_evento.Evento;
import com.example.myapplication.ui.fragment_evento.Lista_Fragment_Evento;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/*
  LA CLASSE E' CORRELATA CON LA PAGE EVENTI NELLA HOME. QUESTA SERVE A MOSTRARE TUTTI GLI EVENTI
  DISPONIBILI ED A SUGGERIRE ALL'UTENTE QUALI SONO GLI EVENTI CHE AVVENGONO NELLA SUA CITTA' O, IN
  CASO DI ASSENZA DI QUESTA INFORMAZIONE, QUALI SONO GLI EVENTI DEI CUOCHI PIU' POPOLARI.

 */

public class Fragment_TabEventi extends Fragment {
    private RecyclerView recyclerView;
    private Adapter_Evento tutorAdapter;
    private FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();
    private ArrayList<Evento> eventi_consigliati=new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tutorAdapter=new Adapter_Evento(eventi_consigliati);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.tab_eventi, parent, false);

        //SI OTTENGONO GLI EVENTI TOTALI
        Lista_Fragment_Evento fragment_lista = new Lista_Fragment_Evento();
        fragment_lista.eventiTotali();
        getChildFragmentManager().beginTransaction().replace(R.id.frame_lista_eventi,fragment_lista).commit();

        //recycleView SI OCCUPA DEGLI EVENTI SUGGERITI
        recyclerView=view.findViewById(R.id.lista_eventi_suggeriti);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(tutorAdapter);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        FirebaseFirestore.getInstance().collection("suggeriti").document(""+FirebaseAuth.getInstance().getUid()).
                get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String luogo_interesse = documentSnapshot.getString("città_eventi");
                if(luogo_interesse!=null)
                    trova_eventi(luogo_interesse);
                else {
                    trova_eventi();
                }
            }
                }).addOnFailureListener(new OnFailureListener() {
                    //IN suggeriti NON E' PRESENTE ALCUN RIFERIMENTO ALL'UTENTE
                @Override
                public void onFailure(@NonNull Exception e) {
                    trova_eventi();
                }

            });
    }



    /*
    NON E' PRESENTE IL LUOGO DI INTERESSE, QUINDI SUGGERIAMO GLI EVENTI DI TRE CUOCHI PIU' NOTI. SI
    USA L'ITEM follower, IL QUALE INDICA IL NUMERO DI SEGUACI DEL CUOCO. TRAMITE orderBy() VENGONO
    PRELEVATI I TRE CUOCHI CON follower PIU' ALTO.
     */
    private void trova_eventi() {
        firebaseFirestore.collection("utenti2").whereEqualTo("tipo",1).orderBy("follower", Query.Direction.DESCENDING).get().
                addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            eventi_consigliati.clear();
                           List<DocumentSnapshot> lista= task.getResult().getDocuments();
                                for(int i=0; i<lista.size() && i<3; i++){
                                    preleva_evento(lista.get(i).getId());
                                }
                            }
                    }
                });
    }

    private void preleva_evento(String id_cuoco){
        firebaseFirestore.collection("eventi").whereEqualTo("id_cuoco", id_cuoco).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for(DocumentSnapshot documentSnapshot: task.getResult()) {
                        Evento evento = documentSnapshot.toObject(Evento.class);
                        if (ActivityMappa.nonScaduto(evento.getData(), evento.getOra())) {
                            eventi_consigliati.add(evento);
                            tutorAdapter.notifyDataSetChanged();
                            break;
                        }
                    }

                }
            }
        });
    }

    //EVENTI CONSIGLIATI SECONDO IL LUOGO DI INTERESSE: VENGONO SELEZIONATI AL PIU' 3 EVENTI
    private void trova_eventi(String luogo_interesse) {
       firebaseFirestore.collection("eventi").whereEqualTo("città", luogo_interesse).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()  && !task.getResult().isEmpty() ){

                    int i=0;
                    eventi_consigliati.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        if(i<3) {
                            Evento evento = document.toObject(Evento.class);
                            if (ActivityMappa.nonScaduto(evento.getData(), evento.getOra())) {
                                eventi_consigliati.add(evento);
                                i++;
                            }
                        }else break;
                    }
                    tutorAdapter.notifyDataSetChanged();
                } else {
                    /*
                     NON E' PRESENTE ALCUN EVENTO NELLA CITTA' INDICATA, QUINDI SI SUGGERISCONO GLI
                     EVENTI DEGLI CHEF PIU' POPOLARI.
                     */
                   trova_eventi();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {

        super.onPause();
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
