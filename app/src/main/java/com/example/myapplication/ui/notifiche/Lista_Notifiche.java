package com.example.myapplication.ui.notifiche;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
/*
    Questa classe rappresenta il frammento della lista delle notifiche
 */
public class Lista_Notifiche extends Fragment {
    private ArrayList<Notifica> lista_notifiche=new ArrayList<>();
    private Adapter_Notifica tutorAdapter;
    private View myView;
    private RecyclerView recyclerView;
    private TextView etichetta_vista;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tutorAdapter = new Adapter_Notifica(lista_notifiche);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_send, container, false);
        recyclerView = myView.findViewById(R.id.lista_notifiche);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setAdapter(tutorAdapter);
        ottieni_notifiche();
        etichetta_vista= getActivity().findViewById(R.id.etichetta_vista);
        return myView;
    }

    /*
        Tramite questo metodo si ottengono tutte le notifiche relative all'utente
     */
    public void ottieni_notifiche(){

        String currentID= FirebaseAuth.getInstance().getUid();
        FirebaseFirestore.getInstance().collection("utenti2").document(""+currentID).collection("Notifications").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                    if(list.size()!=0){
                        etichetta_vista.setText("Le tue notifiche");
                        for (DocumentSnapshot d : list) {
                            String id = d.getId();
                            Notifica notifica= d.toObject(Notifica.class);
                            if(notifica!=null) {
                                lista_notifiche.add(notifica);
                            }
                        }
                        tutorAdapter.notifyDataSetChanged();
                    }
                    else
                        etichetta_vista.setText("Non sono presenti notifiche per te!");
                }
            }
        });

    }


}
