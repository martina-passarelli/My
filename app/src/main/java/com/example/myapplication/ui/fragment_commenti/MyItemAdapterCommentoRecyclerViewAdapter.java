package com.example.myapplication.ui.fragment_commenti;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.ProfiloActivity;
import com.example.myapplication.R;
import com.example.myapplication.ui.fragment_utente.Utente;
import com.example.myapplication.ui.fragment_cuoco.Cuoco;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class MyItemAdapterCommentoRecyclerViewAdapter extends RecyclerView.Adapter<MyItemAdapterCommentoRecyclerViewAdapter.ViewHolder> {

    private final List<Commento> mValues;
    private FirebaseFirestore ff= FirebaseFirestore.getInstance();
    private CollectionReference colR=ff.collection("commenti");
    private FirebaseStorage storage=FirebaseStorage.getInstance();

    public MyItemAdapterCommentoRecyclerViewAdapter(List<Commento> items){
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_commento, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Commento commento;
        if(getItemCount()!=0){
            commento = mValues.get(position);
            holder.testo_commento=commento.getTesto_commento();
            holder.commento.setText(holder.testo_commento);
            holder.id_utente=commento.getId_utente();
            Date date=commento.date;
            String format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm", Locale.getDefault()).format(date);
            holder.data.setText(format);

            //-------------------------------OTTENIAMO I DATI DELL'UTENTE------------------------------------------------------------
            if(holder.id_utente!=null){
                DocumentReference doc_utente= ff.collection("utenti2").document(""+holder.id_utente);
                doc_utente.get().addOnSuccessListener((documentSnapshot) -> {
                    if(documentSnapshot.toObject(Object.class)!=null) {
                        double ob =documentSnapshot.getDouble("tipo");
                        ottieni_dati(holder,documentSnapshot);
                        if (ob==0) {// SE UTENTE
                            holder.tipo="utente";
                        } else {// SE CUOCO
                            holder.icon.setVisibility(View.VISIBLE);
                            holder.tipo="cuoco";
                        }
                        //-----------------------------------------------------------------------------------------------------------------------
                        //---------------CLICK SU PROFILO UTENTE---------------------------------------------------------------------------------
                        // E' possibile cliccare sul profilo utente solo se esso è presente nel db.
                        holder.profilo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Context context = view.getContext();
                                vai_profilo(holder,context);
                            }
                        });

                    }
                    else holder.utente.setText("Account Eliminato");
                });
            }



            //-----------------------------------------------------------------------------------------------------------------------
            //-----------------------RIMOZIONE COMMENTO----------------------------------------------------------------------------------------
            if(holder.id_utente.equals( FirebaseAuth.getInstance().getUid())){
                holder.rimuovi.setOnClickListener(new View.OnClickListener(){
                @Override
                    public void onClick(View view){
                        rimuovi_commento(holder, commento);
                    }
                });
            }else holder.rimuovi.setVisibility(View.INVISIBLE);

        }
    }

    public void vai_profilo(ViewHolder holder, Context context){
        Intent myIntent = new Intent(context, ProfiloActivity.class);
        myIntent.putExtra("tipo", "commento");//Optional parameters
        myIntent.putExtra("utente", holder.id_utente);
        myIntent.putExtra("tipo_utente",holder.tipo);
        context.startActivity(myIntent);
    }


    public void ottieni_dati(ViewHolder holder, DocumentSnapshot documentSnapshot){
        //A PARTIRE DAL DOCUMENT SNAPSHOT RICAVIAMO I DATI: I CAMPI CHE SI VOGLIONO OTTENERE SONO COMUNI SIA AL CUOCO CHE ALL'UTENTE.
        holder.email =documentSnapshot.getString("email");
        if (documentSnapshot.getString("imageProf")!= null) {
            try {
                storage.getReference().child(holder.email + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {//DA SISTEMARE ROTAZIONE IMMAGINE
                        Picasso.with(holder.image_utente.getContext()).load(uri).rotate(documentSnapshot.getDouble("rot").intValue()).fit().centerCrop().into(holder.image_utente);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //----------------------------------------------------------------------------------------------------------------
        holder.nome_utente = documentSnapshot.getString("nome");
        holder.utente.setText(holder.nome_utente);

    }


    public void rimuovi_commento(ViewHolder holder, Commento commento){
        colR.document(""+commento.getId())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        removeAt(holder.getAdapterPosition());

                        Log.d(TAG, "Commento rimosso!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });
    }

    public void removeAt(int position) {
        mValues.remove(position);
        notifyItemRemoved(position);
    }




    @Override
    public int getItemCount() {
        return mValues.size();
    }

//-------------------------------CONTENUTO DI UN ITEMSET------------------------------------------------------------------------------
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView utente,data;
        public final TextView commento;
        public final ImageView icon;
        public final Button rimuovi;
        public final LinearLayout profilo;
        public final ImageView image_utente;
        public String testo_commento;
        public String nome_utente;
        public String id_utente;
        public String tipo;
        public String email;
        public ViewHolder(View view) {
            super(view);
            mView = view;
            utente = (TextView) view.findViewById(R.id.nome_utente_commento);
            icon= (ImageView) view.findViewById(R.id.icon_chef);
            data=(TextView) view.findViewById(R.id.text_data);
            rimuovi= (Button)view.findViewById(R.id.button_elimina_commento);
            commento = (TextView) view.findViewById(R.id.id_commento_principale);
            profilo=(LinearLayout)view.findViewById(R.id.id_layout_utente);
            image_utente=(ImageView)view.findViewById(R.id.image_utente_commento);
            nome_utente=""; //Contiene il nome dell'utente dal campo "nickname"
            testo_commento=""; //Contiene il testo del commento
            id_utente=""; //Contiene l'id dell'utente
            email="";
            tipo="";
        }

        @Override
        public String toString() {
            return super.toString() + " '" + commento.getText() + "'";
        }
    }
}
