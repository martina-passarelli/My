package com.example.myapplication.ui.fragment_ricetta;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private ArrayList<Ricetta> mDataset;
//SI OCCUPA DELLA GESTIONE DEI SINGOLI ELEMENTI DELLA LISTA
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nome_ricetta,descr_ricetta;
        public TextView nome_cuoco;
        public ImageView image;
        public CardView card;
        public String nom,ricetta,info,descr,foto, id_cuoco;
        public String id_ricetta;
        public int rot;
        public AppCompatActivity activity;
        @SuppressLint("RestrictedApi")
        public ViewHolder(View v){
            super(v);
            activity = (AppCompatActivity) v.getContext();
            nome_ricetta= (TextView)v.findViewById(R.id.nome_ricetta);
            nome_cuoco=(TextView)v.findViewById(R.id.nome_cuoco);
            image=(ImageView)v.findViewById(R.id.image_dolce);
            descr_ricetta=(TextView)v.findViewById(R.id.ricetta_card);
            card=(CardView) v.findViewById(R.id.id_card__ricetta);
            FloatingActionButton floatingActionButton=(FloatingActionButton)v.findViewById(R.id.fab_search);

            card.setOnClickListener((view)->{
                Bundle bundle = new Bundle();
                bundle.putString("ricetta",ricetta);
                bundle.putString("nome",nom);
                bundle.putString("descr",descr);
                bundle.putString("foto",foto);
                bundle.putString("info",info);
                bundle.putString("id_cuoco",id_cuoco);
                bundle.putString("id",id_ricetta);
                bundle.putInt("rot",rot);
                FragmentRicetta ricettaFragment = new FragmentRicetta();
                ricettaFragment.setArguments(bundle);
                ricettaFragment.onAttach(v.getContext());
                FragmentTransaction transiction = activity.getSupportFragmentManager().beginTransaction();
                transiction.setCustomAnimations(R.anim.nav_default_enter_anim,R.anim.nav_default_exit_anim);
                transiction.replace(R.id.fragment, ricettaFragment).addToBackStack(null).commit();
            });
        }
    }

    public MyAdapter(ArrayList<Ricetta> myDataset) {
        mDataset = myDataset;
    }

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        Log.d("myTag", mDataset.toString());

        if(getItemCount()!=0){
            Ricetta tmp=mDataset.get(position);
            holder.descr=tmp.getDescrizione();
            holder.nom=tmp.getNome();
            holder.foto=tmp.getFoto();
            holder.ricetta=tmp.getRicetta();
            holder.info=tmp.getIngredienti();
            holder.id_cuoco=tmp.getId_cuoco();
            holder.id_ricetta=tmp.getId_ricetta();
            holder.rot=tmp.getRot();

            holder.nome_ricetta.setText(holder.nom);
            holder.nome_cuoco.setText(tmp.getNome_cuoco());

            holder.descr_ricetta.setText(holder.descr);
            String immagine= tmp.getFoto();
            caricaImg(tmp.getFoto(), tmp.getRot(),holder);
        }
    }
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public ArrayList<Ricetta> getData(){
        return mDataset;
    }

    private FirebaseFirestore ff=FirebaseFirestore.getInstance();
    public void restoreItem(Ricetta ricetta, int position){
        ff.collection("ricette").document(""+ricetta.getId_ricetta()).set(ricetta).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                restoreAt(ricetta,position);
                Log.d(TAG, "Evento ripristinato!");
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private void restoreAt(Ricetta ricetta, int position) {
        mDataset.add(position,ricetta);
        notifyItemInserted(position);
    }

    public void removeItem(String id,int position,String foto){
        ff.collection("ricette").document(""+id).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        removeAt(position);
                        rimuoviImgDaStorage(foto);
                        Log.d(TAG, "Evento rimosso!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });
    }

    private void removeAt(int position) {
        mDataset.remove(position);
        notifyItemRemoved(position);
    }

    /*
        Questo metodo serve per eliminare l'immagine della ricetta dal
        firestore.
     */
    private void rimuoviImgDaStorage(String foto) {
        StorageReference storage= FirebaseStorage.getInstance().getReference();
        if(foto !=null) {
            try {
                storage.child(foto).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                });
            } catch (Exception e) {

            }
        }
    }
    private void caricaImg(String foto,int rot,ViewHolder holder){
        StorageReference storage= FirebaseStorage.getInstance().getReference();
        if(foto !=null){
            try {
                storage.child(foto).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        Picasso.with(holder.activity).load(uri).networkPolicy(NetworkPolicy.OFFLINE)
                                .rotate(rot).fit().centerCrop().into(holder.image, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                System.out.println("on error");
                                Picasso.with(holder.activity).load(uri).
                                        rotate(rot).fit().centerCrop().into(holder.image);
                            }
                        });

                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

