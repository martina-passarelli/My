package com.example.myapplication.ui.fragment_cuoco;

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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/*
    Questa classe si occupa di gestire la vista degli items della griglia dei cuochi presenti nella
    home page.
 */
public class Adapter_Griglia extends RecyclerView.Adapter <Adapter_Griglia.ViewHolder> {
    private ArrayList<String> lista_cuochi;
    private   FirebaseStorage storage=FirebaseStorage.getInstance();
    private AppCompatActivity activity;

    public Adapter_Griglia(ArrayList<String> lista_cuochi){
        this.lista_cuochi=lista_cuochi;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_griglia, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if(getItemCount()!=0){
                String id_utente=lista_cuochi.get(position);
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


    /*
        il metodo crea_item(ViewHolder holder, String id_utente) provvede a settare la vista della card
        dell'item corrente.
        Preleva i dati del cuoco per caricare sia l'immagine del profilo che il suo nome.
     */
    public void crea_item(ViewHolder holder, String id_utente){

        FirebaseFirestore db= FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("utenti2").document("" + id_utente);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.toObject(Object.class)!=null){
                    Cuoco cuoco=documentSnapshot.toObject(Cuoco.class);
                    holder.nome.setText(cuoco.getNome());
                    if(cuoco.getImageProf() !=null){
                        setImage(cuoco.getRot(),cuoco.getEmail(),holder);
                }
            }}
        });
    }

    /*
        Questo metodo carica l'immagine del cuoco dallo storage tramite un meccanismo di caching
     */

    public void setImage(int rot,String email,ViewHolder holder){
        try {
            storage.getReference().child(email+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {

                    activity = (AppCompatActivity) holder.mView.getContext();
                    Picasso.with(activity).load(uri)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .rotate(rot).fit().centerCrop().into(holder.image,new Callback() {
                        @Override
                        public void onSuccess() {}
                        @Override
                        public void onError() {
                            System.out.println("on error");
                            Picasso.with(activity).load(uri)
                                    .rotate(rot)
                                    .fit().centerCrop().into(holder.image);
                        }
                    });

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /*
        Questo metodo consente di passare al profilo del cuoco selezionato.
     */

    public void vai_profilo(String id_utente, Context context){
        Intent myIntent = new Intent(context, ProfiloActivity.class);
        myIntent.putExtra("tipo", "commento");//Optional parameters
        myIntent.putExtra("utente", id_utente);
        myIntent.putExtra("tipo_utente","cuoco");
        context.startActivity(myIntent);
    }

    @Override
    public int getItemCount() {
        return lista_cuochi.size();
    }

    //---------------------------------------VIEW HOLDER--------------------------------------------

    public class ViewHolder extends RecyclerView.ViewHolder {
        private View mView;
        private final CardView card;
        private final TextView nome;
        private final CircleImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView=itemView;
            card=(CardView)itemView.findViewById(R.id.id_card_cuoco);
            nome=(TextView)itemView.findViewById(R.id.id_nome_cuoco);
            image=(CircleImageView)itemView.findViewById(R.id.image_card_cuoco);
        }
    }
}
