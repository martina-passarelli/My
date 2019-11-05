package com.example.myapplication.ui.fragment_nuovo_evento;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.myapplication.R;
import com.example.myapplication.ui.fragment_cuoco.FragmentCuoco;
import com.example.myapplication.ui.fragment_evento.Evento;
import com.example.myapplication.ui.fragment_evento.Lista_Fragment_Evento;
import com.example.myapplication.ui.fragment_ricetta.ListaRicette_Fragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static androidx.constraintlayout.widget.Constraints.TAG;

/*
    Questa classe rappresenta il frammento che si occupa della creazione
    di un nuovo evento
 */
public class FragmentNuovoEvento extends Fragment {

    //widget grafici
    private SearchableSpinner spinnerCitta, spinnerLuoghi;
    private View myView;
    //riferimento ad database
    private FirebaseFirestore mDatabase;
    private String idAuth= FirebaseAuth.getInstance().getUid();
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

    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

        //aggiunge l'evento nel database
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

    /*
        questo metodo manipola i dati ottenuti dai data e ora picker e dagli spinner
        crea l'evento e lo aggiunge al db
     */
    private void aggiungiEvento() {
        String nome=editTextNome.getText().toString();

        int partecipanti;

        if(!editTextPartecipanti.getText().toString().equals(""))
            partecipanti= Integer.parseInt(editTextPartecipanti.getText().toString());
        else
            partecipanti=0;

        String descrizione= editTextDescrizione.getText().toString();

        int ora = oraPicker.getHour();
        int min = oraPicker.getMinute();

        String orario="";
        if (ora<10 && min==0) orario="0"+ora+":"+"00"+min;
        else if(ora<10 && min<10) orario="0"+ora+":"+"0"+min;
        else if(ora<10) orario="0"+ora+":"+min;
        else if(min<10)orario=ora+":"+"0"+min;
        else orario=ora+":"+min;

        int giorno = dataPicker.getDayOfMonth();
        int mese = dataPicker.getMonth()+1;
        int anno = dataPicker.getYear();

        String data="";
        if(mese<10 && giorno<10) data="0"+giorno+"/"+"0"+mese+"/"+anno;
        else if(mese<10) data=giorno+"/"+"0"+mese+"/"+anno;
        else if(giorno<10)data="0"+giorno+"/"+mese+"/"+anno;
        else data=giorno+"/"+mese+"/"+anno;

        String luogo;
        if(spinnerLuoghi.getSelectedItem()!=null)
            luogo = spinnerLuoghi.getSelectedItem().toString();
        else luogo="";

        String citta;
        if(spinnerCitta.getSelectedItem()!=null)
            citta   =spinnerCitta.getSelectedItem().toString();
        else citta="";

        if (luogo=="" || luogo==null|| nome=="" ||nome==null ||
                partecipanti==0 || descrizione=="" || descrizione==null ||
                orario==""|| data==""|| citta=="" || citta==null ){
            Toast.makeText(this.getContext(), "Devi inserire tutti i campi!!!",Toast.LENGTH_SHORT).show();
            return;
        }

        CollectionReference riferimento = mDatabase.collection("Luoghi");
        String finalOrario = orario;
        String finalData = data;
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
                            //*************************l'evento esiste????
                            CollectionReference riferimento = mDatabase.collection("eventi");
                            riferimento.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    boolean aggiungi =true;
                                    if (!queryDocumentSnapshots.isEmpty()) {
                                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                                        for (DocumentSnapshot d : list) {
                                            Evento evento = d.toObject(Evento.class);
                                            if( evento!=null &&evento.getLuogo().equals(luogoF.getNome() )&&
                                                    //evento.getCittà().equals(citta) &&
                                                    evento.getData().equals(finalData)  ) {
                                                //mostro un avviso
                                                showDialog();
                                                aggiungi=false;
                                                break;
                                            }
                                        }
                                        if(aggiungi)
                                            aggiungiEventoFirebase(nome, partecipanti, descrizione, finalOrario, finalData,
                                                    luogoF.getNome(), latitudine, longitudine, citta);

                                    }else{
                                        aggiungiEventoFirebase(nome, partecipanti, descrizione, finalOrario, finalData,
                                                luogoF.getNome(), latitudine, longitudine, citta);
                                    }
                                }
                            });
                            break;
                        }
                    }

                }
            }
        });



    }


    public void inviaNotifica(String nome_evento, String città,String id_evento){
        /*
       La notifica viene inviata a coloro che seguono il cuoco e che ultimamente hanno partecipato
       ad un evento nella città in cui si sta creando l'evento.
       */
        //INVIA SEGUACI
        firestore.collection("utenti2").document(""+idAuth).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
               if(documentSnapshot!=null){
                   ArrayList<String> seguaci=(ArrayList<String>)documentSnapshot.get("seguaci");
                   if(seguaci!=null){
                       //PRESA LA LISTA DEI SEGUACI, AD OGNUNO AGGIUNGIAMO LA NOTIFICA NELLA LISTA
                       String mess= "Non perderti l'evento '"+nome_evento+"'!" ;
                       HashMap<String,Object> notificationMessage= new HashMap<>();
                       notificationMessage.put("message", mess);
                       notificationMessage.put("from",idAuth);
                       notificationMessage.put("id",id_evento);
                       notificationMessage.put("profilo",0);
                       inserisci_notifica(seguaci, notificationMessage);
                   }
               }
            }
        });

        //INVIA UTENTI CON LOCALIZZAZIONE LA STESSA CITTA'
        firestore.collection("suggeriti").whereEqualTo("città_eventi", città).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ArrayList <String> lista_utenti=new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        lista_utenti.add(document.getId());

                    }
                    if(lista_utenti.size()!=0){
                        String mess= "C'è un nuovo evento nella tua città, non perdertelo. Partecipa a '"+nome_evento+"'!" ;
                        HashMap<String,Object> notificationMessage= new HashMap<>();
                        notificationMessage.put("message", mess);
                        notificationMessage.put("from",idAuth);
                        notificationMessage.put("id",id_evento);
                        notificationMessage.put("profilo",0);
                        inserisci_notifica(lista_utenti,notificationMessage);
                    }
                }
            }
        });

    }

    /*
        Questo metodo aggiunge la notifica all'utente
     */
    private void inserisci_notifica(ArrayList<String> seguaci, HashMap<String, Object> notificationMessage) {
        for(String s: seguaci) {
            firestore.collection("utenti2/"+s+"/Notifications").add(notificationMessage);
        }
    }

    private void showDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .create();
        alertDialog.setTitle("");
        alertDialog.setMessage("Evento già esistente in quel luogo");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.BLACK);
            }
        });
        alertDialog.show();
    }

    private void ricaricaFrammentoListaEventi() {
        FragmentCuoco frag=(FragmentCuoco)getParentFragment();
        frag.changeVisibility();
        Bundle bundle= new Bundle();
        String currentId= FirebaseAuth.getInstance().getUid();
        bundle.putString("id",currentId);
        Lista_Fragment_Evento list_eventi=new Lista_Fragment_Evento();
        list_eventi.doSomething(currentId);
        list_eventi.setArguments(bundle);
        getFragmentManager().beginTransaction().replace(R.id.frame_cuoco,list_eventi).commit();
    }


    /*
        Questo metodo inserisce l'evento creato nel firebase
     */
    private void aggiungiEventoFirebase(String nome, int partecipanti, String descrizione, String orario, String data, String luogo,
                                        double latitudine, double longitudine, String città) {
        String idCuoco = mAuth.getUid();
        Evento nuovoEvento = new Evento(nome,idCuoco,descrizione,data,orario,luogo,partecipanti,latitudine,longitudine, new ArrayList<String>(),città);

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
                           inviaNotifica(nome,e.getCittà(),e.getId());
                        }
                    }
                }
                ricaricaFrammentoListaEventi();
            }
        });
    }
    /*
    Questo metodo popola lo spinner dei luoghi
    */
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        context = context;
    }
    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        context=null;
    }
}
