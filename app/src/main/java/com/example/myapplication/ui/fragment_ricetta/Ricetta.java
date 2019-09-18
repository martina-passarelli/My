package com.example.myapplication.ui.fragment_ricetta;

public class Ricetta {

    private String  nome, ingredienti, id_cuoco, descrizione, foto;
    private String id_ricetta="";

    public Ricetta(){
    }

    @Override
    public String toString() {
        return "Ricetta{" +
                "nome='" + nome + '\'' +
                ", ingredienti='" + ingredienti + '\'' +
                ", id_cuoco='" + id_cuoco + '\'' +
                ", descrizione='" + descrizione + '\'' +
                ", foto='" + foto + '\'' +
                '}';
    }

    public Ricetta(String titolo, String ingredienti, String id_cuoco , String descrizione, String foto) {
        this.ingredienti=ingredienti;
        this.id_cuoco = id_cuoco;
        this.nome = titolo;
        this.descrizione = descrizione;
        this.foto = foto;

    }
    public void setId_ricetta(String id){this.id_ricetta=id;}
    public String getId_ricetta(){return id_ricetta;}

    public String getIngredienti() {
        return ingredienti;
    }

    public void setIngredienti(String ingredienti) {
        this.ingredienti = ingredienti;
    }


    public String getId_cuoco() {
        return id_cuoco;
    }

    public void setId_cuoco(String id_cuoco) {
        this.id_cuoco = id_cuoco;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String titolo) {
        this.nome = titolo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }
}
