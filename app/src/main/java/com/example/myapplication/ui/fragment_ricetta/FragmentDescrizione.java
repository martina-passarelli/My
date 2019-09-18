package com.example.myapplication.ui.fragment_ricetta;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.myapplication.R;

public class FragmentDescrizione extends Fragment {
    private String descr,ingred;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        View view= inflater.inflate(R.layout.fragment_descr, parent, false);


        return view;
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Bundle bundle = this.getArguments();
        if(bundle != null){

            descr=bundle.get("descr").toString();
            ingred=bundle.get("info").toString();

        }

        TextView textDesc=(TextView)view.findViewById(R.id.tRicetta);
        textDesc.setText(descr);

        TextView textIngred=(TextView) view.findViewById(R.id.tIngredienti);
        textIngred.setText(ingred);
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
    }
    @Override
    public void onDetach(){
        super.onDetach();
    }
}
