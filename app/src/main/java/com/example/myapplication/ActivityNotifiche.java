package com.example.myapplication;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.ui.notifiche.Lista_Notifiche;
import com.google.firebase.messaging.FirebaseMessaging;

public class ActivityNotifiche extends AppCompatActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FirebaseMessaging.getInstance().subscribeToTopic("pushNotifications");
        FirebaseMessaging.getInstance().unsubscribeFromTopic("pushNotifications");

        Lista_Notifiche fragment= new Lista_Notifiche();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment,fragment).commit();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
