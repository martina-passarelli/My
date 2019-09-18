package com.example.myapplication.ui.fragment_commenti;

public class Commento {

    private String id,id_commento, id_utente, testo_commento;

    public Commento(){}

    public Commento(String id_commento, String id_utente, String testo_commento) {
        this.id_commento = id_commento;
        this.id_utente = id_utente;
        this.testo_commento = testo_commento;
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
