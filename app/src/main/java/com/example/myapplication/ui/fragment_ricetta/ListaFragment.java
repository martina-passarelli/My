package com.example.myapplication.ui.fragment_ricetta;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.myapplication.R;

public class ListaFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_item, parent, false);
    }
    // DA SISTEMARE

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

    }

}
