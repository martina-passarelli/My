package com.example.myapplication.ui.fragment_seguiti;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.ProfiloActivity;
import com.example.myapplication.R;
import com.example.myapplication.ui.fragment_cuoco.Cuoco;
import com.example.myapplication.ui.fragment_utente.Utente;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class Adapter_Seguiti extends RecyclerView.Adapter <Adapter_Seguiti.ViewHolder> {
    private List<String> seguitiList;
    private FirebaseFirestore ff=FirebaseFirestore.getInstance();
    private AppCompatActivity activity;

    public Adapter_Seguiti(List<String> list){
        seguitiList=list;
    }


    @Override
    public Adapter_Seguiti.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_seguito, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(getItemCount()!=0){
            String id_utente=seguitiList.get(position);
            crea_item(holder,id_utente);

            holder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Context context = view.getContext();
                    vai_profilo(id_utente,context);
                }
            });
        }
    }

    public void vai_profilo(String id_utente, Context context){
        Intent myIntent = new Intent(context, ProfiloActivity.class);
        myIntent.putExtra("tipo", "commento");//Optional parameters
        myIntent.putExtra("utente", id_utente);
        myIntent.putExtra("tipo_utente","cuoco");
        context.startActivity(myIntent);
    }



    public void crea_item(ViewHolder holder, String id_utente){
        FirebaseFirestore db= FirebaseFirestore.getInstance();
        StorageReference storage = FirebaseStorage.getInstance().getReference();
        DocumentReference docRef = db.collection("utenti2").document("" + id_utente);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Cuoco utente = documentSnapshot.toObject(Cuoco.class);
                //SETTA NOME UTENTE
                holder.nome_cuoco.setText(utente.getNome());
                // SETTA IMMAGINE DELL'UTENTE
                if(utente.getImageProf() !=null){
                    try {
                        storage.child(utente.getEmail()+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                activity = (AppCompatActivity) holder.mView.getContext();
                                Picasso.with(activity).load(uri).rotate(utente.getRot()).fit().centerCrop().into(holder.foto_cuoco);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return seguitiList.size();
    }

    //-----------------------------------------------VIEW HOLDER------------------------------------

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final View mView;
        private final CardView card;
        private final CircleImageView foto_cuoco;
        private final TextView nome_cuoco;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView=itemView;
            nome_cuoco=(TextView)itemView.findViewById(R.id.id_nome_seguito);
            foto_cuoco=(CircleImageView) itemView.findViewById(R.id.image_card_cuoco);
            card=(CardView)itemView.findViewById(R.id.id_card_seguito);

        }
    }
}
