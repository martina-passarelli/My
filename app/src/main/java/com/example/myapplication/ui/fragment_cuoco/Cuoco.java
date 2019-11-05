package com.example.myapplication.ui.fragment_cuoco;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/*
    Questa classe rappresenta il cuoco.
 */

public class Cuoco {

    private ArrayList<String> seguaci;
    private String nome,password;
    private String email;
    private String token_id;
    private String imageProf;
    private int rot;
    private ArrayList<String> lista_preferiti;
    private ArrayList<String> lista_eventi;
    private ArrayList<String> lista_cuochi;
    private double tipo=1;
    private double follower;


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

    public double getFollower() {
        return follower;
    }

    public void setFollower(double follower) {
        this.follower = follower;
    }

    public ArrayList<String> getSeguaci() {
        return seguaci;
    }

    public void setSeguaci(ArrayList<String> seguaci) {
        this.seguaci = seguaci;
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

    public String getToken_id() {
        return token_id;
    }

    public void setToken_id(String token_id) {
        this.token_id = token_id;
    }

    public ArrayList<String> getLista_preferiti() {
        return lista_preferiti;
    }

    public void setLista_preferiti(ArrayList<String> lista_preferiti) {
        this.lista_preferiti = lista_preferiti;
    }

    public ArrayList<String> getLista_eventi() {
        return lista_eventi;
    }

    public void setLista_eventi(ArrayList<String> lista_eventi) {
        this.lista_eventi = lista_eventi;
    }

    public ArrayList<String> getLista_cuochi() {
        return lista_cuochi;
    }

    public void setLista_cuochi(ArrayList<String> lista_cuochi) {
        this.lista_cuochi = lista_cuochi;
    }

    public void setTipo(double tipo) {
        this.tipo = tipo;
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
