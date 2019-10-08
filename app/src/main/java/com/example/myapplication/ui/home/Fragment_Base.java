package com.example.myapplication.ui.home;

import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.myapplication.PagerAdapter;
import com.example.myapplication.R;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

public class Fragment_Base extends Fragment {


    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        View view= inflater.inflate(R.layout.fragment_base, parent, false);
        TabLayout tabLayout=view.findViewById(R.id.tab_layout);
        TabItem tab_ricette=view.findViewById(R.id.id_tab_ricette);
        TabItem tab_eventi=view.findViewById(R.id.id_tab_eventi);
        TabItem tab_chef=view.findViewById(R.id.id_tab_chef);
        ViewPager viewPager=view.findViewById(R.id.view_pager);
        PagerAdapter pageAdapter = new PagerAdapter(getChildFragmentManager(), tabLayout.getTabCount());

        viewPager.setAdapter(pageAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));


        return view;
    }

    @Override
    public void onPause() {

        super.onPause();
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
