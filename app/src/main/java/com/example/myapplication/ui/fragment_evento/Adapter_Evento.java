package com.example.myapplication.ui.fragment_evento;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.ui.fragment_cuoco.Cuoco;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class Adapter_Evento extends RecyclerView.Adapter <Adapter_Evento.ViewHolder>{
    private List<Evento> eventoList;
    private FirebaseFirestore ff=FirebaseFirestore.getInstance();

    public Adapter_Evento(List<Evento> list){
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
            System.out.println(evento.toString());
            holder.label_nome.setText(evento.getNome());
            holder.label_luogo.setText(evento.getLuogo());
            holder.label_data.setText(evento.getData());
            holder.label_ora.setText(evento.getOra());
            System.out.println("ID CUOCOOOO:"+evento.getId_cuoco());
            //settiamo il nome del cuoco dal suo id
            DocumentReference doc_cuoco=ff.collection("utenti2").document(""+evento.getId_cuoco());
            System.out.println("ID CUOCOOOO:"+evento.getId_cuoco());
            doc_cuoco.get().addOnSuccessListener((documentSnapshot) -> {
                Cuoco cuoco =documentSnapshot.toObject(Cuoco.class);
                System.out.println("CUOCO:"+cuoco.toString());
                holder.label_cuoco.setText(cuoco.getNome());
            });
        }

    }

    @Override
    public int getItemCount() {
        return eventoList.size();
    }

    //-------------------------------------------------------VIEW HOLDER-----------------------------------------------------------
    public class ViewHolder extends RecyclerView.ViewHolder {

        private final View mView;
        private final CardView card_evento;
        private String  id_cuoco;
        private final TextView label_nome, label_cuoco, label_luogo, label_data, label_ora;

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

                //TO DO!

            });
        }
    }
}
