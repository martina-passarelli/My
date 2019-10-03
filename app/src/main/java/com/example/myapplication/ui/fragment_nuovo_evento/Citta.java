package com.example.myapplication.ui.fragment_nuovo_evento;

import java.util.ArrayList;

public class Citta {
    private String nome;
    private double latitudine;
    private double longitudine;
    private ArrayList<String> luoghi;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public double getLatitudine() {
        return latitudine;
    }

    public void setLatitudine(double latitudine) {
        this.latitudine = latitudine;
    }

    public double getLongitudine() {
        return longitudine;
    }

    public void setLongitudine(double longitudine) {
        this.longitudine = longitudine;
    }

    public ArrayList<String> getLuoghi() {
        return luoghi;
    }

    public void setLuoghi(ArrayList<String> IDluoghi) {
        this.luoghi = IDluoghi;
    }
}
