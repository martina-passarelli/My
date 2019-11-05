package com.example.myapplication.ui.notifiche;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.ProfiloActivity;
import com.example.myapplication.R;
import com.example.myapplication.ui.fragment_evento.Evento;
import com.example.myapplication.ui.fragment_evento.Fragment_Evento;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/*
    Questa classe rappresenta l'adpter che gestisce la lista delle notifiche
 */
public class Adapter_Notifica extends RecyclerView.Adapter <Adapter_Notifica.ViewHolder> {
    private ArrayList<Notifica> lista_notifiche;
    private FirebaseFirestore ff=FirebaseFirestore.getInstance();
    private View view;

    public Adapter_Notifica(ArrayList<Notifica> lista_notifiche){
        this.lista_notifiche=lista_notifiche;
    }

    @NonNull
    @Override
    public Adapter_Notifica.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notifica, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Adapter_Notifica.ViewHolder holder, int position) {

        if(getItemCount()!=0){
            Notifica notifica= lista_notifiche.get(position);
            ff.collection("utenti2").document(""+notifica.getFrom()).get().
                    addOnSuccessListener((documentSnapshot) -> {
                        holder.nome.setText(documentSnapshot.getString("nome"));
                    });
            holder.notifica.setText(notifica.getMessage());
            holder.card_notifica.setOnClickListener((view)->{
                if(notifica.getProfilo()==0){
                    //bisogna aprire un fragment evento
                    apri_evento(notifica.getId());
                }else{
                    apri_profilo(notifica.getId());
                }
            });
        }
    }

    /*
        Questo metodo consente di caricare il profilo dell'utente/cuoco della notifica
     */
    private void apri_profilo(String id){
        FirebaseFirestore.getInstance().collection("utenti2").document(""+id).get().addOnSuccessListener((documentSnapshot) -> {
            if(documentSnapshot!=null){
                int tipo= documentSnapshot.getDouble("tipo").intValue();
                Intent myIntent = new Intent(view.getContext(), ProfiloActivity.class);
                myIntent.putExtra("tipo", "commento");
                myIntent.putExtra("utente", id);

                if(tipo==0) myIntent.putExtra("tipo_utente","utente");
                else myIntent.putExtra("tipo_utente","cuoco");

                myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                view.getContext().startActivity(myIntent);
            }
        });

    }

    /*
    Questo metodo carica l'evento della notifica
     */
    public void apri_evento(String id_evento){
        CollectionReference collEventi =FirebaseFirestore.getInstance().collection("eventi");
        collEventi.document(""+id_evento).get().addOnSuccessListener((documentSnapshot) -> {

            if(documentSnapshot!=null){
                Evento evento=documentSnapshot.toObject(Evento.class);
                Bundle bundle=new Bundle();
                if(evento!=null) {
                    bundle.putString("id_evento", id_evento);
                    bundle.putString("id_cuoco", evento.getId_cuoco());
                    bundle.putDouble("longitudine", evento.getLongitudine());
                    bundle.putDouble("latitudine", evento.getLatitudine());

                    Fragment_Evento fragment_evento = new Fragment_Evento();
                    fragment_evento.setArguments(bundle);
                    AppCompatActivity activity = (AppCompatActivity) view.getContext();
                    activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment, fragment_evento).commit();
                }

            }

        });
    }

    @Override
    public int getItemCount() {
        return lista_notifiche.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView nome, notifica;
        private ConstraintLayout card_notifica;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nome=(TextView)itemView.findViewById(R.id.mittente_notif);
            notifica=(TextView)itemView.findViewById(R.id.testo_notifica);
            card_notifica=(ConstraintLayout) itemView.findViewById(R.id.card_notification);

        }
    }
}
