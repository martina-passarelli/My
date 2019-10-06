package com.example.myapplication.ui.fragment_utente;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.myapplication.R;
import com.example.myapplication.ui.fragment_evento.Lista_Fragment_Evento;
import com.example.myapplication.ui.fragment_ricetta.ListaRicette_Fragment;
import com.google.firebase.auth.FirebaseAuth;

public class Fragment_IMieiEventi extends Fragment {

    private Lista_Fragment_Evento fragment_evento;
    public Bundle bundle;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        View view= inflater.inflate(R.layout.fragment_slideshow, parent, false);
        bundle=new Bundle();
        bundle.putString("id","null");
        bundle.putBoolean("do",false);
        fragment_evento= new Lista_Fragment_Evento();
        fragment_evento.setArguments(bundle);
        fragment_evento.eventi_utente();
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();

        fragmentTransaction.replace(R.id.fragment,fragment_evento);
        fragmentTransaction.commit();



        return view;
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

    }


    @Override
    public void onAttach(Context context){
        super.onAttach(context);
    }
    @Override
    public void onDetach(){
        super.onDetach();
    }}
