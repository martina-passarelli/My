package com.example.myapplication.ui.fragment_cuoco;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.algolia.search.saas.Client;
import com.algolia.search.saas.Index;
import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
//IL FRAMMENTO GESTISCE LA GRIGLIA  DI CUOCHI NELLA HOME PAGE
public class Griglia_Cuochi extends Fragment {
    private ArrayList<String> list=new ArrayList<>();
    private Adapter_Griglia tutorAdapter;
    private View myView;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager mLayoutManager;

    private FirebaseFirestore ff=FirebaseFirestore.getInstance();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tutorAdapter = new Adapter_Griglia(list);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.grid_cuochi, container, false);
        recyclerView = myView.findViewById(R.id.griglia);
        mLayoutManager=new GridLayoutManager(getContext(),2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(mLayoutManager);
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setAdapter(tutorAdapter);
        return myView;
    }




    public void onViewCreated(View view, Bundle savedInstanceState) {
        TextInputLayout textEdit=(TextInputLayout)view.findViewById(R.id.insert_testo);
        FloatingActionButton floatingActionButton=(FloatingActionButton)view.findViewById(R.id.fab_search_cuoco);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String testo= textEdit.getEditText().getText().toString().toLowerCase();
                search(testo);
            }
        });


        //DEVO FARE UNA QUERY CHE PRENDE TUTTI I CUOCHI -> TIPO=1
        ff.collection("utenti2").whereEqualTo("tipo",1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    list.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String id = document.getId();
                        list.add(id);
                    }
                    tutorAdapter.notifyDataSetChanged();
                }
            }
        });

    }

    //CERCA I CUOCHI INDICATI.
    public void search(String testo){
        Client client = new Client("348522f0fb1c5e16852ff83238805714", "fc1c214d14331aa60c3b706f5f725ee5");
        Index index = client.getIndex("utenti2");
        CollectionReference colR = ff.collection("utenti2"); //collezione riferita a utenti
        colR.whereEqualTo("tipo",1).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    List<DocumentSnapshot> lista = queryDocumentSnapshots.getDocuments();
                    list.clear();
                    for (DocumentSnapshot d : lista) {
                        Cuoco cuoco = d.toObject(Cuoco.class);
                        if( cuoco.getNome().toLowerCase().trim().contains(testo)){
                            String id = d.getId();
                            list.add(id);
                        }
                    }
                    tutorAdapter.notifyDataSetChanged();
                }

            }
        });
    }



    private Context context;
    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        context=context;
    }
    @Override
    public void onDetach() {
        super.onDetach();
    }
    @Override
    public void onPause() {

        super.onPause();
    }
}
