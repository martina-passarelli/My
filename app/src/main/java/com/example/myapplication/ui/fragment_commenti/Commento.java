package com.example.myapplication.ui.fragment_commenti;

import android.os.health.TimerStat;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

public class Commento {

    private String id,id_commento, id_utente, testo_commento;
    private int giorno, mese, ora;

    public Commento(){

    }

    public Commento(String id_commento, String id_utente, String testo_commento) {
        this.id_commento = id_commento;
        this.id_utente = id_utente;
        this.testo_commento = testo_commento;
        giorno = Calendar.DAY_OF_MONTH;
        mese= Calendar.MONTH;
        ora= Calendar.HOUR_OF_DAY;
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

