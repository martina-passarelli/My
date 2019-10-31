package com.example.myapplication.ui.fragment_evento;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.myapplication.ProfiloActivity;
import com.example.myapplication.R;
import com.example.myapplication.ui.fragment_cuoco.Cuoco;
import com.example.myapplication.ui.fragment_partecipanti.Fragment_ListaPartecipanti;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
/*

LA CLASSE SI OCCUPA DELLA VISTA DEL PROFILO DI UN EVENTO. SONO PRESENTI DUE SEZIONI, QUELLA RIFERITA
ALLA DESCRIZIONE E QUELLA MOSTRANTE LA LISTA DEI PARTECIPANTI.

 */
public class Fragment_Evento extends Fragment {
    private Bundle bundle;
    private MapView mappa;
    private GoogleMap gmap;
    private TextView cuoco,nome_evento,ora,data,luogo;
    private FloatingActionButton add_part;
    private String id_evento,id_cuoco;
    private double longitudine,latitudine;
    public Evento e;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_home_evento, parent, false);
        /*
        INVIAMO LE INFO DELL'EVENTO SENZA ACCEDERE NUOVAMENTE AL FIREBASE
         */
        bundle = this.getArguments();
        if(bundle != null){
            id_evento=bundle.getString("id_evento");
            id_cuoco=bundle.getString("id_cuoco");
            longitudine=bundle.getDouble("longitudine");
            latitudine=bundle.getDouble("latitudine");
        }
        preleva_evento(id_evento);
        return view;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        nome_evento=(TextView)view.findViewById(R.id.nome_home_evento);
        ora=(TextView)view.findViewById(R.id.home_ora);
        data=(TextView)view.findViewById(R.id.home_data);
        luogo=(TextView)view.findViewById(R.id.home_luogo);

        //CLICK SUL NOME DEL CUOCO, APRE IL PROFILO
        cuoco=(TextView)view.findViewById(R.id.cuoco_home);
        cuoco.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                apri_profilo();
            }
        });

        Button descrizione= (Button)view.findViewById(R.id.button_descr_evento);
        descrizione.setClickable(false);//Si apre direttamente nella sezione descrizione

        Button partecipanti=(Button)view.findViewById(R.id.button_partec);

        //PRELEVO I COLORI DEI BUTTON IN MODO DA SETTARLI CORRETTAMENTE DI VOLTA IN VOLTA
        ColorStateList click= descrizione.getBackgroundTintList();
        ColorStateList no_click=partecipanti.getBackgroundTintList();

        descrizione.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                RIPRENDIAMO LA SEZIONE DESCRIZIONE: SE SI E' ANDATI IN PARTECIPANTI, SIAMO SICURI
                CHE ESSA CI SIA, IN QUANTO E' IL PRIMO FRAMMENTO FIGLIO A CREARSI APPENA SI APRE
                L'EVENTO.
                 */
               getChildFragmentManager().popBackStack("DESCRIZIONE_EVENTO",FragmentManager.POP_BACK_STACK_INCLUSIVE);
               descrizione.setClickable(false);
               descrizione.setBackgroundTintList(click);
               partecipanti.setClickable(true);
               partecipanti.setBackgroundTintList(no_click);
            }
        });


        partecipanti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //CREAZIONE DELLA LISTA PARTECIPATI A QELL'EVENTO
                Fragment_ListaPartecipanti fragment_partecipanti = new Fragment_ListaPartecipanti();

                Bundle b=new Bundle();
                b.putString("id",id_evento);
                b.putString("id_cuoco",id_cuoco);
                fragment_partecipanti.setArguments(b);
                fragment_partecipanti.doSomething(id_evento);

                getChildFragmentManager().beginTransaction().replace(R.id.frame_home_ricetta, fragment_partecipanti)
                .addToBackStack("DESCRIZIONE_EVENTO").commit();

                descrizione.setClickable(true);
                descrizione.setBackgroundTintList(no_click);
                partecipanti.setClickable(false);
                partecipanti.setBackgroundTintList(click);
            }
        });

        //-----------------------------INIZIALIZZAZIONE MAPPA---------------------------------------

        mappa=(MapView)view.findViewById(R.id.mapView);
        mappa.onCreate(savedInstanceState);
        mappa.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                inserisciBandierina(googleMap);
            }
        });
    }


    //--------------------------------METODI MAPPA--------------------------------------------------
    private void inserisciBandierina(GoogleMap googleMap) {
        gmap = googleMap;
        gmap.addMarker(new MarkerOptions().position(new LatLng(latitudine, longitudine))).showInfoWindow();
        gmap.getUiSettings().setMapToolbarEnabled(false);
        gmap.getUiSettings().setScrollGesturesEnabled(false);
        moveCamera(new LatLng(latitudine,longitudine),17f);
    }
    private void moveCamera(LatLng latLng, float zoom){
        gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));
    }
    //----------------------------------------------------------------------------------------------

    /*
    E' POSSIBILE CLICCARE SUL NOME DEL CUOCO PER APRIRE IL SUO PROFILO. IL METODO SOTTOSTANTE SI
    OCCUPA DI CIO'.
     */

    private void apri_profilo(){
        Intent myIntent = new Intent(getActivity().getBaseContext(), ProfiloActivity.class);
        myIntent.putExtra("tipo", "commento");
        myIntent.putExtra("utente", id_cuoco);
        myIntent.putExtra("tipo_utente","cuoco");
        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getActivity().getBaseContext().startActivity(myIntent);
    }

    private void set_cuoco(String id_cuoco) {
        FirebaseFirestore ff= FirebaseFirestore.getInstance();
        DocumentReference doc=ff.collection("utenti2").document("" + id_cuoco);
        doc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.toObject(Object.class)!=null) {
                    cuoco.setText(documentSnapshot.getString("nome"));
                }
            }
        });
    }

    /*
    IL METODO preleva_evento(String id_evento) SI OCCUPA DI SETTARE I CAMPI DELL'EVENTO CORRENTE:
    PRELEVA I DATI DAL DATABASE.
     */
    private void preleva_evento(String id_evento) {
        FirebaseFirestore ff= FirebaseFirestore.getInstance();
        DocumentReference doc=ff.collection("eventi").document("" + id_evento);
        doc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot!=null) {
                    e = documentSnapshot.toObject(Evento.class);
                    set_profilo(e);
                }
            }
        });
    }


    /*
    IL METODO set_profilo(Evento e) ASSEGNA I VALORI CORRETTI ALLA VISTA.
     */
    public void set_profilo(Evento e){
        nome_evento.setText(e.getNome());
        ora.setText(e.getOra());
        data.setText(e.getData());
        luogo.setText(e.getCittà()+", "+e.getLuogo());
        set_cuoco(e.getId_cuoco());
        aggiungiFragmentDescr(e);
    }

    public void aggiungiFragmentDescr(Evento e){
        bundle.putString("descrizione", e.getDescrizione());
        bundle.putInt("num_max", e.getMax_partecipanti());
        FragmentDescrEvento descrizioneFragment = new FragmentDescrEvento();
        descrizioneFragment.setArguments(bundle);
        getChildFragmentManager().beginTransaction().add(R.id.frame_home_ricetta,descrizioneFragment).addToBackStack(null).commit();
    }


    private Context context;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        context = context;
    }


    @Override
    public void onResume() {
        super.onResume();
        mappa.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mappa.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mappa.onDestroy();
        context=null;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mappa.onLowMemory();
    }
}
