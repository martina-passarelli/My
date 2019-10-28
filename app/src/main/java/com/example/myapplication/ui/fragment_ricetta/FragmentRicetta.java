package com.example.myapplication.ui.fragment_ricetta;

import android.accounts.Account;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.myapplication.R;
import com.example.myapplication.ui.fragment_commenti.ListaCommenti_Fragment;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class FragmentRicetta extends Fragment {
    private String id,nome,ricetta,descr,foto,info,id_cuoco;
    private boolean isPref=false;
    private Context mContext;
    private int rot;
    private boolean sezione_commenti=false;

    //PER FACEBOOK
    private Uri uriCondiv;
    private CallbackManager callbackManager;
    private Button share;
    private ShareDialog shareDialog;
    Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            if (ShareDialog.canShow(SharePhotoContent.class)) {
                ShareLinkContent linkContent = new ShareLinkContent.Builder().setContentUrl(uriCondiv)
                        .setContentDescription(descr).setQuote(descr)
                        .build();
                shareDialog.show(linkContent);
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    //-----------------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


    }

    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_home_ricetta, parent, false);
        Bundle bundle = this.getArguments();
        if(bundle != null){
           id_cuoco=bundle.get("id_cuoco").toString();
           id = bundle.get("id").toString();
           nome=bundle.get("nome").toString();
           rot=bundle.getInt("rot");
           ricetta=bundle.get("ricetta").toString();
           descr=bundle.get("descr").toString();
           info=bundle.get("info").toString();
           foto=bundle.get("foto").toString();
        }
        bundle.putString("descr",descr);
        bundle.putString("ricetta", ricetta);
        bundle.putString("info",info);
        bundle.putString("id_ricetta",id);

        FragmentDescrizione descrizioneFragment = new FragmentDescrizione();
        descrizioneFragment.setArguments(bundle);
        getChildFragmentManager().beginTransaction().replace(R.id.id_frame_layout,descrizioneFragment).addToBackStack(null).commit();
        //-----------------PER INIZIALIZZARE FACEBOOK-----------------------------------------------
        FacebookSdk.sdkInitialize(inflater.getContext());
        share = view.findViewById(R.id.share_btn);
        callbackManager=CallbackManager.Factory.create();
        shareDialog= new ShareDialog(this);

        return view;
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        //VA AGGIUNTO TASTO PREFERITI
        TextView textView=(TextView) view.findViewById(R.id.tNomeRicetta);
        textView.setText(nome);

        TextView textDesc=(TextView) view.findViewById(R.id.tDescrizione);
        textDesc.setText(descr);

        ImageView img= (ImageView) view.findViewById(R.id.imageHomeRicetta);
        caricaImg(foto,rot,img);


        Button commenti= (Button)view.findViewById(R.id.button_commenti);
        ColorStateList no_click = commenti.getBackgroundTintList();

        Button descrizione_button= (Button)view.findViewById(R.id.button_descr);
       ColorStateList click=descrizione_button.getBackgroundTintList();

        commenti.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if(sezione_commenti==false) {

                    descrizione_button.setBackgroundTintList(no_click);
                    commenti.setBackgroundTintList(click);
                    //ENTRA IN GIOCO IL FRAMMENTO DEI COMMENTI
                    ListaCommenti_Fragment fragment_commenti = new ListaCommenti_Fragment();
                    //IL METODO doSomething(String value) E' USATO PER PRENDERE SOLO I COMMENTI
                    // DI QUELLA RICETTA
                    fragment_commenti.doSomething(id);
                    FragmentManager manager = getChildFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.setCustomAnimations(R.anim.anim_left,R.anim.nav_default_exit_anim);
                    transaction.replace(R.id.id_frame_layout, fragment_commenti);
                    transaction.addToBackStack("DESCRIZIONE");
                    transaction.commit();

                    sezione_commenti = true;
                }
            }
        });



        descrizione_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if(sezione_commenti==true) {
                    commenti.setBackgroundTintList(no_click);
                    descrizione_button.setBackgroundTintList(click);

                    getChildFragmentManager().popBackStack("DESCRIZIONE", FragmentManager.POP_BACK_STACK_INCLUSIVE);

                    sezione_commenti = false;
                }
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //creare call back

                shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
                    @Override
                    public void onSuccess(Sharer.Result result) {
                        Toast.makeText(getContext(),"OK",Toast.LENGTH_SHORT);
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(getContext(),"NO",Toast.LENGTH_SHORT);

                    }

                    @Override
                    public void onError(FacebookException error) {
                        Toast.makeText(getContext(),error.getMessage(),Toast.LENGTH_SHORT);

                    }
                });
                StorageReference storage= FirebaseStorage.getInstance().getReference();
                if(foto !=null){
                    try {
                        storage.child(foto).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                //la foto prelevata dallo storage viene inserita in un oggetto Target
                                //questo oggetto, avvenuto il caricamento, richiama il metodo onBitmapLoaded
                                //che si preoccupa di creare il post di facebook
                                Picasso.with(getActivity()).load(uri).rotate(rot).into(target);

                                uriCondiv=uri;
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });


    }


    private void caricaImg(String foto, int rot, ImageView img) {
        StorageReference storage= FirebaseStorage.getInstance().getReference();
        if(foto !=null){
            try {
                storage.child(foto).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        Picasso.with(getActivity()).load(uri).networkPolicy(NetworkPolicy.OFFLINE).
                                rotate(rot).fit().centerCrop().into(img, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(getActivity()).load(uri).
                                        rotate(rot).fit().centerCrop().into(img);
                            }
                        });
                        uriCondiv=uri;


                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * metodo di risposta alla condivisione del post su facebook
     */
    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onPause() {
        super.onPause();

    }
}
