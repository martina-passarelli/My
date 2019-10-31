package com.example.myapplication.ui.fragment_seguiti;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

public class ListSeguiti extends Fragment {
    private ArrayList<String> list=new ArrayList<>();
    private Adapter_Seguiti tutorAdapter;
    private View myView;
    private RecyclerView recyclerView;
    private CollectionReference colR;
    private FirebaseFirestore ff= FirebaseFirestore.getInstance();
    private Bundle savedInstanceState;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tutorAdapter = new Adapter_Seguiti(list);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.lista_seguiti, container, false);
        recyclerView = myView.findViewById(R.id.id_lista_seguiti);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.startLayoutAnimation();
        recyclerView.setAdapter(tutorAdapter);
        return myView;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        Bundle bundle=this.getArguments();
        String id=bundle.getString("id");
        //PRELEVO LA LISTA DI CUOCHI PREFERITI DELL'UTENTE ED AVVISO L'ADAPTER
        doSomething(id);

    }

    public void doSomething(String id){
        ff.collection("utenti2").document(""+ id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                ArrayList<String> lista_provv= (ArrayList<String>) documentSnapshot.get("lista_cuochi");
                if(lista_provv!=null){
                    list.addAll(lista_provv);
                    tutorAdapter.notifyDataSetChanged();
                }


            }});

    }
}
