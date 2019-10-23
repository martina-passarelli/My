package com.example.myapplication.ui.fragment_preferiti;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.myapplication.R;
import com.example.myapplication.ui.fragment_ricetta.ListaRicette_Fragment;
import com.google.firebase.auth.FirebaseAuth;

public class FragmentPreferiti extends Fragment{

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        View view= inflater.inflate(R.layout.content_lista, parent, false);
        return view;
    }
    //Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        ListaRicette_Fragment fragment=new ListaRicette_Fragment();
        fragment.trovaPreferiti(FirebaseAuth.getInstance().getUid());
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment,fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

}
