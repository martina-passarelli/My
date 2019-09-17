package com.example.myapplication;

import android.net.Uri;

public class Utente {

    private String nome,nick, bio, tel,password;
    private String email;
    private String imageProf;
    private boolean rot;

    public Utente() {
    }

    public Utente(String email,String password){
        this.password=password;
        this.email=email;
        this.nome= "";
        this.nick = "";
        this.bio = "";
        this.tel="";
        this.imageProf=null;
        this.rot=false;
    }

    public Utente(String nome, String email, String nick, String bio, String tel, String password, String imageProf,boolean rot) {
        this.nome = nome;
        this.bio=bio;
        this.nick = nick;
        this.email = email;
        this.tel = tel;
        this.password=password;
        this.imageProf=imageProf;
        this.rot=rot;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRot(boolean rot) {
        this.rot = rot;
    }

    public boolean getRot() {
        return rot;
    }
}