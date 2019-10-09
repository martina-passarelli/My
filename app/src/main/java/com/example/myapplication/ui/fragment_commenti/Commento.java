package com.example.myapplication.ui.fragment_commenti;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.ServerTimestamp;


import java.io.Serializable;

import java.util.Date;
import java.util.Locale;

public class Commento implements Serializable {

    public String id,id_commento, id_utente, testo_commento;

    @ServerTimestamp
    public Date date=new Date();

    public Commento(){
    }


    public Commento(String id_commento, String id_utente, String testo_commento){
        this.id_commento = id_commento;
        this.id_utente = id_utente;
        this.testo_commento = testo_commento;
    }


    public void setId_commento(String id_commento) {
        this.id_commento = id_commento;
    }


    public void setId_utente(String id_utente) {
        this.id_utente = id_utente;
    }

    public String getId_commento() {
        return id_commento;
    }

    public String getId_utente() {
        return id_utente;
    }

    public String getTesto_commento() {
        return testo_commento;
    }

    public void setTesto_commento(String testo_commento) {
        this.testo_commento = testo_commento;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Commento{" +
                "id_commento='" + id_commento + '\'' +
                ", id_utente='" + id_utente + '\'' +
                ", testo_commento='" + testo_commento + '\'' +
                '}';
    }
}

