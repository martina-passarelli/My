package com.example.myapplication.ui.fragment_evento;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.ui.fragment_cuoco.Cuoco;
import com.example.myapplication.ui.fragment_cuoco.FragmentCuoco;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import static androidx.constraintlayout.widget.Constraints.TAG;
/*
    Classe che rappresenta il gestore dell'evento
 */
public class Adapter_Evento extends RecyclerView.Adapter <Adapter_Evento.ViewHolder>{

    private ArrayList<Evento> eventoList;
    private FirebaseFirestore ff=FirebaseFirestore.getInstance();

    public Adapter_Evento(ArrayList<Evento> list){
        eventoList=list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_evento, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(getItemCount()!=0){
            Evento evento=eventoList.get(position);
            holder.id_evento=evento.getId();
            holder.label_nome.setText(evento.getNome());
            holder.label_luogo.setText(evento.getLuogo());
            holder.label_data.setText(evento.getData());
            holder.label_ora.setText(evento.getOra());
            holder.longitudine=evento.getLongitudine();
            holder.latitudine=evento.getLatitudine();
            //settiamo il nome del cuoco dal suo id
            DocumentReference doc_cuoco=ff.collection("utenti2").document(""+evento.getId_cuoco());
            doc_cuoco.get().addOnSuccessListener((documentSnapshot) -> {

                Cuoco cuoco =documentSnapshot.toObject(Cuoco.class);
                if(cuoco!=null) {
                    holder.id_cuoco = evento.getId_cuoco();
                    holder.label_cuoco.setText(cuoco.getNome());
                }

            });
        }

    }

    public ArrayList<Evento> getData(){
        return eventoList;
    }

    @Override
    public int getItemCount() {
        return eventoList.size();
    }

    public void removeItem(String id,int position){
        ff.collection("eventi").document(""+id).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        removeAt(position);
                        Log.d(TAG, "Evento rimosso!");
                    }
                });
    }

    public void restoreItem(Evento evento, int position){
        ff.collection("eventi").document(""+evento.getId()).set(evento).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                restoreAt(evento,position);
                Log.d(TAG, "Evento ripristinato!");
            }
        });
    }


    private void removeAt(int position) {
        eventoList.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreAt(Evento item, int position) {
        eventoList.add(position,item);
        notifyItemInserted(position);
    }


    //---------------------------VIEW HOLDER--------------------------------------------------------
    public class ViewHolder extends RecyclerView.ViewHolder {

        private final View mView;
        private final CardView card_evento;
        private String  id_cuoco,id_evento;
        private final TextView label_nome, label_cuoco, label_luogo, label_data, label_ora;
        private double longitudine,latitudine;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView=itemView;
            label_nome=(TextView)itemView.findViewById(R.id.id_nome_evento);
            label_cuoco=(TextView)itemView.findViewById(R.id.id_cuoco_evento);
            label_luogo=(TextView)itemView.findViewById(R.id.luogo_evento);
            label_ora=(TextView)itemView.findViewById(R.id.ora_evento);
            label_data=(TextView)itemView.findViewById(R.id.data_evento);
            card_evento=(CardView)itemView.findViewById(R.id.id_card_evento);
            card_evento.setOnClickListener((view)->{
                Bundle bundle=new Bundle();
                bundle.putString("id_evento", id_evento);
                bundle.putString("id_cuoco",id_cuoco);
                bundle.putDouble("latitudine", latitudine);
                bundle.putDouble("longitudine", longitudine);
                //AVVIARE IL FRAMMENTO
                Fragment_Evento fragment_evento=new Fragment_Evento();
                fragment_evento.setArguments(bundle);
                AppCompatActivity activity = (AppCompatActivity) itemView.getContext();
                fragment_evento.onAttach(itemView.getContext());
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment, fragment_evento).addToBackStack(null).commit();
            });
        }
    }
}
