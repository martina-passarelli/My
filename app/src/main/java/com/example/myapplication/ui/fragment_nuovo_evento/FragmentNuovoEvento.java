package com.example.myapplication.ui.fragment_nuovo_evento;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.ui.fragment_cuoco.FragmentCuoco;
import com.example.myapplication.ui.fragment_evento.Evento;
import com.example.myapplication.ui.fragment_evento.Lista_Fragment_Evento;
import com.example.myapplication.ui.fragment_ricetta.ListaRicette_Fragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.util.ArrayList;
import java.util.List;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class FragmentNuovoEvento extends Fragment {

    //widget grafici
    private SearchableSpinner spinnerCitta, spinnerLuoghi;
    private View myView;
    //riferimento ad database
    private FirebaseFirestore mDatabase;
    //liste di supporto agli spinner
    private ArrayList<String> cittaList;
    private ArrayList<String> luoghiList;
    //lista dei luoghi della città selezionata
    private ArrayList<String> lista;

    private ArrayAdapter<String> adapter2;
    private ArrayAdapter<String> adapter;

    private Button aggiungi ;
    private EditText editTextNome;
    private EditText editTextPartecipanti;
    private EditText editTextDescrizione;
    private DatePicker dataPicker;
    private TimePicker oraPicker;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.frag_crea_evento, container, false);
        return myView;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        spinnerCitta = view.findViewById(R.id.spinnerCitta);
        spinnerLuoghi = view.findViewById(R.id.spinnerLuogo);
        mDatabase = FirebaseFirestore.getInstance();
        cittaList = new ArrayList<>();
        luoghiList = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        editTextNome = view.findViewById(R.id.editNomeEvento);
        editTextDescrizione = view.findViewById(R.id.editDescrizione);
        editTextPartecipanti=  view.findViewById(R.id.editNumeroP);
        dataPicker = view.findViewById(R.id.data);
        oraPicker = view.findViewById(R.id.time);
        oraPicker.setIs24HourView(true);
        aggiungi=view.findViewById(R.id.bott_creaEvento);


        lista=new ArrayList<>();
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, cittaList);
        adapter2 = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, luoghiList);

        aggiungi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aggiungiEvento();
            }
        });

        //popolo lo spinner città
        CollectionReference citta = mDatabase.collection("Citta");
        citta.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {

                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot d : list) {
                        String city = (String) d.get("nome");
                        cittaList.add(city);
                        //notifico all'adapter che la lista è cambiata
                        adapter.notifyDataSetChanged();
                    }

                    adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
                    spinnerCitta.setAdapter(adapter);
                }
            }

        });

        //--------------------------scelta città e aggiornamento secondo spinner---------------------------------------------------
        spinnerCitta.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //elemento della lista selezionato
                String city = adapterView.getItemAtPosition(i).toString();
                luoghiList.clear();
                citta.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            ArrayList<String> luoghi = new ArrayList<>();
                            //città che seleziona l'utente
                            Citta cittasel;
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot d : list) {
                                String city2 = (String) d.get("nome");
                                if (city.equals(city2)) {
                                    cittasel = d.toObject(Citta.class);
                                    //lista dei luoghi associati a quella città
                                    lista = cittasel.getLuoghi();
                                    try { //la lista è null
                                        int size = lista.size();
                                        //se la città ha dei luoghi allora popolo il secondo spinner
                                        if (size != 0) {
                                            aggiungiLuoghi(lista);
                                            break;
                                        }
                                        else{
                                            luoghi.clear();
                                        }
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            }
                            //popolo il secondo spinner
                            adapter2.notifyDataSetChanged();
                            spinnerLuoghi.setAdapter(adapter2);
                        }
                    }
                });

            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        spinnerLuoghi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                System.out.println("luogo selezionato: "+ luoghiList.get(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void aggiungiEvento() {
        String nome=editTextNome.getText().toString();
        int partecipanti= Integer.parseInt(editTextPartecipanti.getText().toString());
        String descrizione= editTextDescrizione.getText().toString();
        int ora = oraPicker.getHour();
        int min = oraPicker.getMinute();
        String orario= ora+":"+min;
        int giorno = dataPicker.getDayOfMonth();
        int mese = dataPicker.getMonth()+1;
        int anno = dataPicker.getYear();
        String data = giorno+"-"+mese+"-"+anno;
        String luogo = spinnerLuoghi.getSelectedItem().toString();

        System.out.println("luogoooo="+luogo);

        String citta =spinnerCitta.getSelectedItem().toString();

        if (luogo=="" || nome=="" || partecipanti==0 || descrizione=="" ||
        orario==""|| data==""|| citta==""){
            Toast.makeText(this.getContext(), "Devi inserire tutti i campi!!!",Toast.LENGTH_SHORT).show();
            return;
        }

        CollectionReference riferimento = mDatabase.collection("Luoghi");
        riferimento.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot d : list) {
                        if(d.get("nome").toString().equals(luogo)){
                            Luogo luogoF=d.toObject(Luogo.class);
                            double latitudine= luogoF.getLatitudine();
                            double longitudine = luogoF.getLongitudine();
                            aggiungiEventoFirebase(nome,partecipanti,descrizione,orario,data,luogoF.getNome(),latitudine,longitudine);
                            break;
                        }
                    }

                }
            }
        });
        ricaricaFrammentoListaEventi();

    }

    private void ricaricaFrammentoListaEventi() {
        FragmentCuoco frag=(FragmentCuoco)getParentFragment();
        frag.changeVisibility();
        String currentId= FirebaseAuth.getInstance().getUid();
        Bundle bundle= new Bundle();
        bundle.putString("id",currentId);
        bundle.putBoolean("doS",true);
        Lista_Fragment_Evento list_eventi=new Lista_Fragment_Evento();
        list_eventi.doSomething(currentId);
        getFragmentManager().beginTransaction().replace(R.id.frame_cuoco,list_eventi).commit();
    }


    private void aggiungiEventoFirebase(String nome, int partecipanti, String descrizione, String orario, String data, String luogo,
                                        double latitudine, double longitudine) {
        String idCuoco = mAuth.getUid();
        Evento nuovoEvento = new Evento(nome,idCuoco,descrizione,data,orario,luogo,partecipanti,latitudine,longitudine, new ArrayList<String>());

        FirebaseFirestore.getInstance().collection("eventi").document().set(nuovoEvento)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Evento inserito!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Errore inserimento fallito", e);
                    }
                });
        CollectionReference riferimento = mDatabase.collection("eventi");
        riferimento.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot d : list) {
                        if(d.get("nome").toString().equals(nome)){
                           Evento e= d.toObject(Evento.class);
                           e.setId(d.getId());
                           riferimento.document(d.getId()).set(e);
                        }
                    }
                }
            }
        });
    }

    private void aggiungiLuoghi(ArrayList<String> lista) {
        for (int i = 0; i < lista.size(); i++) {
            DocumentReference docRef = mDatabase.collection("Luoghi").document(lista.get(i));
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    luoghiList.add(documentSnapshot.getString("nome"));
                    adapter2.notifyDataSetChanged();
                }
            });
        }
    }
}
