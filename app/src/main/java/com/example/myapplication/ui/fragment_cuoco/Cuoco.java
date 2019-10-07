package com.example.myapplication.ui.fragment_cuoco;

import org.json.JSONException;
import org.json.JSONObject;

public class Cuoco {
    private String nome,password;
    private String email;
    private String imageProf;
    private int rot;
    private double tipo=1;


    public Cuoco(String nome, String password, String email, String imageProf, int rot, double tipo) {
        this.nome = nome;
        this.password = password;
        this.email = email;
        this.imageProf = imageProf;
        this.rot = rot;
        this.tipo = tipo;
    }

    public Cuoco (String email, String password){
        this.email=email;
        this.password=password;
    }

    public Cuoco(){}


    public Cuoco(String nome, String password, String email, String imageProf, int rot) {
        this.nome = nome;
        this.password = password;
        this.email = email;
        this.imageProf = imageProf;
        this.rot = rot;
    }

    public String getNome() {
        return nome;
    }

    public double getTipo() {
        return tipo;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageProf() {
        return imageProf;
    }

    public void setImageProf(String imageProf) {
        this.imageProf = imageProf;
    }

    public int getRot() {
        return rot;
    }

    public void setRot(int rot) {
        this.rot = rot;
    }

    public JSONObject toJSON() throws JSONException {

        JSONObject jo = new JSONObject();
        jo.put("nome", nome);
        jo.put("email", email);
        jo.put("imageProf",imageProf);

        jo.put("password",password);
        jo.put("rot",rot);
        jo.put("tipo",tipo);
        return jo;
    }
}
