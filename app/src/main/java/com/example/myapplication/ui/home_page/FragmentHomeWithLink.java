package com.example.myapplication.ui.home_page;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

/*
    Questa classe rappresenta il frammento principale che contiene il link per iscriversi/loggarsi come
    cuoco oppure per iscriversi/loggarsi come utente
 */
public class FragmentHomeWithLink extends Fragment {
    private Context context;
    private TextView link1,linkUtente;
    private EditText codice;
    private Button verifica;
    private FirebaseFirestore firestore;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        context=inflater.getContext();
        return inflater.inflate(R.layout.fragment_home_link, parent, false);

    }
    @SuppressLint({"ResourceAsColor", "RestrictedApi"})
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        firestore = FirebaseFirestore.getInstance();
        link1 = view.findViewById(R.id.link);
        linkUtente = view.findViewById(R.id.linkUtente);
        codice = view.findViewById(R.id.codice);
        codice.setVisibility(View.GONE);
        verifica = view.findViewById(R.id.verifica);
        verifica.setVisibility(View.GONE);
//-------------------------clicca sul link di cuoco------------------------------------------------------------
        link1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                codice.setVisibility(View.VISIBLE);
                verifica.setVisibility(View.VISIBLE);
                linkUtente.setVisibility(View.GONE);
            }
        });
        verifica.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CollectionReference col = firestore.collection("codiciCuochi");
                col.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                //se il codice inserito è valido l'utente può ora fare l'accesso
                                //come un cuoco
                                if (doc.get("codice").equals(codice.getText().toString().trim())) {
                                    Toast.makeText(context, "Codice valido!", Toast.LENGTH_SHORT).show();
                                    Bundle bundle= new Bundle();
                                    //al frammento di login viene passato un bundle nel quale
                                    //è inserita l'informazione del fatto che a loggarsi è un cuoco
                                    //è importante per sapere il tipo di profilo che dovrà
                                    //essere caricato in seguito e la collection di firestore nel
                                    //quale salvare le informazioni relative al cuoco
                                    bundle.putString("tipo","cuoco");
                                    FragmentLogin fragment = new FragmentLogin();
                                    fragment.setArguments(bundle);
                                    getFragmentManager().beginTransaction().replace(R.id.fragment_homePage,fragment)
                                            .commit();
                                }
                            }
                        }
                    }
                });
            }
        });
//------------------------clicca sul link utente----------------------------------------------------------------------
        linkUtente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // se a voler accedere è un utente, viene creato un bundle con l'informazione
                //e si passa al fragmento di login
                Bundle bundle= new Bundle();
                bundle.putString("tipo","utente");
                FragmentLogin fragment = new FragmentLogin();
                fragment.setArguments(bundle);
                getFragmentManager().beginTransaction().replace(R.id.fragment_homePage,fragment).commit();
            }
        });
    }

}
