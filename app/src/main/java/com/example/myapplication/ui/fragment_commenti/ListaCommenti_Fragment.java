package com.example.myapplication.ui.fragment_commenti;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

import static androidx.constraintlayout.widget.Constraints.TAG;

/*
    Questa classe rappresenta il frammento che si occupa della lista dei commenti
    presenti sotto ad una ricetta

 */
public class ListaCommenti_Fragment extends Fragment {
    //adapter che si occupa di gestire i commenti
    private MyItemAdapterCommento tutorAdapter;
    //recyclerView della vista dei commenti
    private RecyclerView recyclerView;
    private View myView;
    //lista dei commenti sotto ad una ricetta
    private List<Commento> lista_commenti = new ArrayList<Commento>();
    private String id_ricetta;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tutorAdapter = new MyItemAdapterCommento(lista_commenti);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_commenti,container, false);
        recyclerView = myView.findViewById(R.id.view_commenti);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setItemAnimator(new SlideInUpAnimator());
        recyclerView.startLayoutAnimation();
        recyclerView.setAdapter(tutorAdapter);
        return myView;
    }


    /*
        doSomething(String parms) si occupa di caricare i commenti della ricetta per cui si
        intende visualizzare la sezione dei commenti.
        params corrisponde all'id della ricetta.
        La query si preoccupa di riordinare i commenti secondo il parametro "date" che contiene sia
        la data che l'ora del commento.
     */

    public void doSomething(String parms){
        FirebaseFirestore ff= FirebaseFirestore.getInstance();
        CollectionReference colR=ff.collection("commenti");
        id_ricetta=parms;

        colR.orderBy("date",Query.Direction.ASCENDING).
                whereEqualTo("id_commento",parms).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Commento di=document.toObject(Commento.class);
                                lista_commenti.add(di);
                            }
                            recyclerView.scrollToPosition(lista_commenti.size()-1);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                        tutorAdapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        TextInputLayout text_input=(TextInputLayout)view.findViewById(R.id.input_commento);
        Button condividi= (Button)view.findViewById(R.id.button_commenta);
        //cliccando sul bottone condividi, il commento viene pubblicato
        condividi.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                String testo= text_input.getEditText().getText().toString().trim();
                if(!testo.isEmpty()){
                    condividi_commento(testo);
                    text_input.getEditText().setText("");

                }else{
                    String error="COMMENTO VUOTO, RIPROVA!";
                    Toast.makeText(view.getContext(), error,Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    /*
        Questo metodo si occupa di condividere il commento e salvarlo sul firestore
     */
    public void condividi_commento(String testo){

            //AGGIUNGERE A COMMENTI CON ID DELLA RICETTA CORRENTE
            FirebaseAuth firebaseAuth= FirebaseAuth.getInstance();
            FirebaseFirestore ff= FirebaseFirestore.getInstance();


            //dati del commento
            String utente=FirebaseAuth.getInstance().getUid();
            //creazione del commento
            Commento comm=new Commento(id_ricetta,firebaseAuth.getUid(),testo);

            CollectionReference colR=ff.collection("commenti");
            //aggiunta del commento alla collection "commenti"
            colR.document().set(comm);

            //viene effettuata una query per ottenere l'id del commento e settarlo nel commento stesso
            colR.whereEqualTo("id_commento",id_ricetta).whereEqualTo("id_utente",utente).
                    whereEqualTo("testo_commento",testo).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshot= task.getResult();
                        DocumentSnapshot document= snapshot.getDocuments().get(0);
                        String id=document.getId();
                        comm.setId(id);
                        colR.document(""+id).set(comm);
                        lista_commenti.add(lista_commenti.size(),comm);
                        recyclerView.scrollToPosition(tutorAdapter.getItemCount()-1);
                        tutorAdapter.notifyItemInserted(lista_commenti.size()-1);
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
