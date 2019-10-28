package com.example.myapplication.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.ListaActivity;
import com.example.myapplication.R;
import com.example.myapplication.ui.fragment_ricetta.MyAdapter;
import com.example.myapplication.ui.fragment_ricetta.Ricetta;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

import static com.example.myapplication.ui.home.UtilitaSuggeriti.cercaRicette;
import static com.example.myapplication.ui.home.UtilitaSuggeriti.updateStringaSuggeriti;

/*
    Questa classe rappresenta il fragment della home principale che contiene le CardView che rappresentano le
    categorie dei dolci dell'app e la recycle view orizzontale delle ricette che vengono suggerite all'utente

 */
public class HomeFragment extends Fragment {
    private RecyclerView horizontalList;
    private ArrayList<Ricetta> ricette = new ArrayList<>();
    private MyAdapter myAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myAdapter=new MyAdapter(ricette);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        //cardView della categoria "tutte le ricette"
        CardView cardView_tutteCateg= root.findViewById(R.id.id_card_tutti);
        //quando l'utente clicca su questa categoria (come per le altre), viene caricata la lista dei dolci
        //che ne fanno parte tramite il metodo choose(categoria)
        cardView_tutteCateg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choose("tutti");
            }
        });
        CardView cardView_torte= root.findViewById(R.id.id_card_torta);
        cardView_torte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choose("Torte");
                //tramite questo metodo viene salvato il click dell'utente su quella categoria
                //in modo da portergli mostrare tra i suggeriti i dolci che più si
                //avvicinano alle sue ricerche e quindi ai suoi gusti
                updateStringaSuggeriti("torta");
            }
        });

        CardView cardView_ciamb= root.findViewById(R.id.id_card_ciambellone);
        cardView_ciamb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choose("Ciambelloni");
                updateStringaSuggeriti("ciambellone");
            }
        });
        CardView cardView_bisc= root.findViewById(R.id.id_card_biscotti);
        cardView_bisc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choose("Biscotti");
                updateStringaSuggeriti("biscotti");
            }
        });
        CardView cardView_mousse= root.findViewById(R.id.id_card_mousse);
        cardView_mousse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choose("Mousse");
                updateStringaSuggeriti("mousse");
            }
        });
        CardView cardView_chess= root.findViewById(R.id.id_card_chees);
        cardView_chess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choose("Cheesecake");
                updateStringaSuggeriti("cheesecake");
            }
        });
        CardView cardView_crostata= root.findViewById(R.id.id_card_crostata);
        cardView_crostata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choose("Crostate");
                updateStringaSuggeriti("crostata");
            }
        });

        //label della ricerca
        TextInputLayout text_input;
        text_input=root.findViewById(R.id.input_ricerca);

        //textView "In base alle tue ricerche ti proponiamo"
        TextView testoSugg = root.findViewById(R.id.testoSugg);
        horizontalList=root.findViewById(R.id.horizontal_recycler);

        //inizializzazione della recycle view delle ricerche suggerite
        LinearLayoutManager horizontalManager= new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        horizontalList.setLayoutManager(horizontalManager);
        horizontalList.setAdapter(myAdapter);

        //cerca le ricette da mostrare tra i suggeriti
        cercaRicette(myAdapter,ricette,testoSugg);

        //---------------------QUERY PER LA RICERCA-------------------------------------------------
        FloatingActionButton fab = root.findViewById(R.id.fab_search);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String testo= text_input.getEditText().getText().toString().toLowerCase().trim();
                Intent i = new Intent(getActivity(), ListaActivity.class);
                i.putExtra("testo",testo);
                i.putExtra("preferiti","no");
                //con la ricerca viene aggiornata la lista dei suggeriti
                updateStringaSuggeriti(testo);
                startActivity(i);
            }
        });
        return root;
    }

    /*
        Questo metodo lancia la nuova activity passandogli la categoria di dolci da mostare
     */
    private  void choose (String s){
        Intent i= new Intent(getActivity(), ListaActivity.class);
        i.putExtra("categoria", s);
        startActivity(i);
    }

}
