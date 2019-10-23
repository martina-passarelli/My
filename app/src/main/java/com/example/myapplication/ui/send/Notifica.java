package com.example.myapplication.ui.send;

public class Notifica {
    private String from, message,id;
    private int profilo;

    /*
    LA NOTIFICA PUO' RIGUARDARE UN EVENTO OPPURE UN PROFILO. NEL MOMENTO IN CUI SI CLICCA SU DI ESSA
    BISOGNA CAPIRE DI CHE TIPO TRATTA.
    profilo=0 -> SI TRATTA DI UN RIFERIMENTO AD EVENTO
    profilo=1 -> SI TRATTA DI UN RIFERIMENTO AD UN PROFILO
     */



    public Notifica(){}

    public Notifica(String from, String message) {
        this.from = from;
        this.message = message;
    }

    public Notifica(String from, String message, String id, int profilo) {
        this.from = from;
        this.message = message;
        this.id = id;
        this.profilo = profilo;
    }

    public String getId() {
        return id;
    }


    public int getProfilo() {
        return profilo;
    }

    public void setProfilo(int profilo) {
        this.profilo = profilo;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Notifica{" +
                "from='" + from + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
