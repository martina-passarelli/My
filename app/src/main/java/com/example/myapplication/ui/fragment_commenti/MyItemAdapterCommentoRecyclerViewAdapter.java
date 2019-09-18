package com.example.myapplication.ui.fragment_commenti;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

import com.example.myapplication.R;
import com.example.myapplication.UserProfileActivity;
import com.example.myapplication.Utente;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.List;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class MyItemAdapterCommentoRecyclerViewAdapter extends RecyclerView.Adapter<MyItemAdapterCommentoRecyclerViewAdapter.ViewHolder> {
    private Utente utente;
    private final List<Commento> mValues;
    //private final OnListFragmentInteractionListener mListener;
    private FirebaseAuth firebaseAuth= FirebaseAuth.getInstance();
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

            //-------------------------------OTTENIAMO I DATI DELL'UTENTE------------------------------------------------------------
            if(holder.id_utente!=null){
                DocumentReference doc_utente= ff.collection("utenti").document(""+holder.id_utente);

                doc_utente.get().addOnSuccessListener((documentSnapshot) -> {
                    utente =documentSnapshot.toObject(Utente.class);
                    // SETTA IMMAGINE DELL'UTENTE
                    holder.email=utente.getEmail();
                    if(utente.getImageProf()!=null){
                     try{
                        storage.getReference().child(utente.getEmail()+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                           // if(utente.getRot())
                             //   Picasso.with(holder.image_utente.getContext()).load(uri).rotate(90f).fit().centerCrop().into(holder.image_utente);
                            //else
                                Picasso.with(holder.image_utente.getContext()).load(uri).fit().centerCrop().into(holder.image_utente);
                        }
                        });
                     }catch(Exception e){
                         e.printStackTrace();
                     }
                    }
                    //----------------------------------------------------------------------------------------------------------------
                    holder.nome_utente=utente.getNome();
                    holder.utente.setText(holder.nome_utente);
                    });
            }

            //-----------------------------------------------------------------------------------------------------------------------
            //---------------CLICK SU PROFILO UTENTE---------------------------------------------------------------------------------
            holder.profilo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Context context = view.getContext();
                    Intent myIntent = new Intent(context, UserProfileActivity.class);
                    myIntent.putExtra("tipo", "commento");//Optional parameters
                    System.out.println("UTENTE INRERESSATO: "+utente.toString());
                    myIntent.putExtra("utente", holder.email);
                    context.startActivity(myIntent);
                }
            });

            //-----------------------------------------------------------------------------------------------------------------------
            //-----------------------RIMOZIONE COMMENTO----------------------------------------------------------------------------------------
            if(holder.id_utente.equals( FirebaseAuth.getInstance().getUid())){
                holder.rimuovi.setOnClickListener(new View.OnClickListener(){
                @Override
                    public void onClick(View view){
                    //Bisogna rimuovere l'oggetto dalla lista

                    //Bisogna rimuovere dal firestore il commento
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
                });
            }else holder.rimuovi.setVisibility(View.INVISIBLE);

        }
    }
    public void removeAt(int position) {
        mValues.remove(position);
        notifyItemRemoved(position);
        //notifyItemRangeChanged(position, mValues.size());

    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

//-------------------------------CONTENUTO DI UN ITEMSET------------------------------------------------------------------------------
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView utente;
        public final TextView commento;
        public final Button rimuovi;
        public final LinearLayout profilo;
        public final ImageView image_utente;
        public String testo_commento;
        public String nome_utente;
        public String id_utente;
        public String email;
        public ViewHolder(View view) {
            super(view);
            mView = view;
            utente = (TextView) view.findViewById(R.id.nome_utente_commento);
            rimuovi= (Button)view.findViewById(R.id.button_elimina_commento);
            commento = (TextView) view.findViewById(R.id.id_commento_principale);
            profilo=(LinearLayout)view.findViewById(R.id.id_layout_utente);
            image_utente=(ImageView)view.findViewById(R.id.image_utente_commento);
            nome_utente=""; //Contiene il nome dell'utente dal campo "nickname"
            testo_commento=""; //Contiene il testo del commento
            id_utente=""; //Contiene l'id dell'utente
            email="";
        }

        @Override
        public String toString() {
            return super.toString() + " '" + commento.getText() + "'";
        }
    }
}
