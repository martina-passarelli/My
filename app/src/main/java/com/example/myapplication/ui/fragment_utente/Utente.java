package com.example.myapplication.ui.fragment_utente;

import java.util.ArrayList;

/*
    Questa classe rappresenta l'utente
 */
public class Utente {

    private String nome,nick, bio, tel,password;
    private String email;
    private String imageProf;
    private String token_id;
    private ArrayList<String> lista_preferiti;
    private ArrayList<String> lista_eventi;
    private ArrayList<String> lista_cuochi;
    private int rot;
    private double tipo=0;

    public Utente() {
    }


    public Utente(String nome, String email, String nick, String bio, String tel, String password, String imageProf,int rot) {
        this.nome = nome;
        this.bio=bio;
        this.nick = nick;
        this.email = email;
        this.tel = tel;
        this.password=password;
        this.imageProf=imageProf;
        this.rot=rot;
    }

    public double getTipo() {
        return tipo;
    }

    public String getNome() {
        return nome;
    }

    public String getNick() {
        return nick;
    }

    public String getBio() {
        return bio;
    }

    public String getTel() {
        return tel;
    }

    public String getEmail() {
        return email;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setTel(String tel) {
        this.tel = tel;
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

    @Override
    public String toString() {
        return "Utente{" +
                "nome='" + nome + '\'' +
                ", nick='" + nick + '\'' +
                ", bio='" + bio + '\'' +
                ", tel='" + tel + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", imageProf='" + imageProf + '\'' +
                ", rot=" + rot +
                '}';
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRot(int rot) {
        this.rot = rot;
    }

    public int getRot() {
        return rot;
    }

    public ArrayList<String> getLista_eventi() {
        return lista_eventi;
    }

    public ArrayList<String> getLista_preferiti() {
        return lista_preferiti;
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

    public String getToken_id() {
        return token_id;
    }

    public void setToken_id(String token_id) {
        this.token_id = token_id;
    }
}