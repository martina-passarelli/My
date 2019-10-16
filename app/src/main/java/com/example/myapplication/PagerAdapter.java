package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.myapplication.ui.fragment_cuoco.Griglia_Cuochi;
import com.example.myapplication.ui.fragment_evento.Lista_Fragment_Evento;
import com.example.myapplication.ui.home.Fragment_TabEventi;
import com.example.myapplication.ui.home.HomeFragment;

public class PagerAdapter extends FragmentPagerAdapter {

    private int num_tab;

    public PagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm,behavior);
        num_tab=behavior;
    }


    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new HomeFragment();
            case 1:
                return new Griglia_Cuochi();
            case 2: {
                Fragment_TabEventi fragment_tabEventi=new Fragment_TabEventi();
                return fragment_tabEventi; //da cambiare
            }
            default:
               return null;
        }
    }

    @Override
    public int getCount() {
        return num_tab;
    }


}
