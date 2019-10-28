package com.example.myapplication.ui.i_tuoi_eventi_menu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import com.example.myapplication.R;
import com.example.myapplication.ui.fragment_evento.Lista_Fragment_Evento;

/*
    Questa classe si occupa della view listaEventi al lato della mainActivity
 */
public class EventiMenuFragment extends Fragment {
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_evento, container, false);
        Lista_Fragment_Evento fragment = new Lista_Fragment_Evento();
        fragment.eventi_utente();
        FragmentTransaction fragmentTransaction=getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment,fragment);
        fragmentTransaction.commit();
        return root;
    }
}
