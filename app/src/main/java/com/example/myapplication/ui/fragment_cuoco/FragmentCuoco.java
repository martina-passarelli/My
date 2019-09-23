package com.example.myapplication.ui.fragment_cuoco;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.ui.fragment_evento.Lista_Fragment_Evento;
import com.example.myapplication.ui.fragment_ricetta.ListaRicette_Fragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FragmentCuoco extends Fragment {
   private String currentId;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_cuoco, container, false);
        //OTTENIAMO LA LISTA DI RICETTE DA LUI FORNITO: RICORDIAMO CHE IN RICETTE E' PRESENTE IL CAMPO
        //ID_CUOCO CHE MANTIENE L'ID DEL CUOCO CHE L'HA CREATO
        ListaRicette_Fragment ricette_fragment=new ListaRicette_Fragment();
        ricette_fragment.ottieni_lista(currentId);
        getChildFragmentManager().beginTransaction().add(R.id.frame_cuoco,ricette_fragment).addToBackStack(null).commit();
        return view;

    }

    private TextView nomeCuoco,emailCuoco;
    private EditText password;
    private CircleImageView foto_cuoco;
    private Button ricette,eventi;
    private FloatingActionButton add;

    private Cuoco cuoco;
    private FirebaseFirestore db;
    private StorageReference storage=FirebaseStorage.getInstance().getReference();
    private boolean sezione_eventi=false;
    @SuppressLint({"ResourceAsColor", "RestrictedApi"})
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        nomeCuoco=(TextView)view.findViewById(R.id.nome_cuoco);
        emailCuoco=(TextView)view.findViewById(R.id.email_cuoco);
        password=(EditText)view.findViewById(R.id.edit_pass_cuoco);
        foto_cuoco=(CircleImageView)view.findViewById(R.id.image_cuoco);
        ricette=(Button)view.findViewById(R.id.button_lista);
        eventi=(Button)view.findViewById(R.id.button_eventi);
        add=(FloatingActionButton)view.findViewById(R.id.add_cuoco);
        //PRELEVIAMO I DATI APPARTENENTI AL CUOCO E SETTIAMO LE LABEL
        ottieni_dati();

        //OTTENIAMO LA LISTA DI EVENTI A CUI PARTECIPA
        eventi.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if(sezione_eventi==false) {
                    crea_lista_eventi(currentId);
                    ricette.setClickable(true);
                    eventi.setClickable(false);
                    sezione_eventi = true;
                }
            }
        });


        ricette.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if(sezione_eventi==true) {
                    getChildFragmentManager().popBackStackImmediate();
                    ricette.setClickable(false);
                    eventi.setClickable(true);
                    sezione_eventi = false;
                }
            }
        });

        //SE NON E' IL CUOCO CORRENTE NON PUO' MODIFICARE IL PROPRIO PROFILO
        if(!currentId.equals(FirebaseAuth.getInstance().getUid())){
            add.setVisibility(View.INVISIBLE);
            add.setClickable(false);
        }

        add.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                add.setVisibility(View.INVISIBLE);
                if(sezione_eventi==true){
                    //aggiungi_evento();
                }else {
                    aggiungi_ricetta();
                }
            }
        });
    }

    private void crea_lista_eventi(String currentId) {
        Lista_Fragment_Evento fragment_evento= new Lista_Fragment_Evento();
        fragment_evento.doSomething(currentId);
        getChildFragmentManager().beginTransaction().replace(R.id.frame_cuoco,fragment_evento).addToBackStack(null).commit();

    }

    public void aggiungi_ricetta(){
        Fragment_CreaRicetta fragment_creaRicetta=new Fragment_CreaRicetta();
        getChildFragmentManager().beginTransaction().replace(R.id.frame_cuoco,fragment_creaRicetta).addToBackStack("FRAG_CUOCO").commit();
    }

    @SuppressLint("RestrictedApi")
    public void changeVisibility(){
        add.setVisibility(View.VISIBLE);
    }

    private void ottieni_dati() {
        db= FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("utenti2").document("" + currentId);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {

            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                cuoco = documentSnapshot.toObject(Cuoco.class);
                nomeCuoco.setText(cuoco.getNome());
                emailCuoco.setText(cuoco.getEmail());

                if(currentId.equals(FirebaseAuth.getInstance().getUid()))
                    password.setText(cuoco.getPassword());
                else
                    password.setVisibility(View.INVISIBLE);
                    if(cuoco.getImageProf() !=null){
                    try {
                        storage.child(cuoco.getEmail()+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Picasso.with(getActivity()).load(uri).rotate(cuoco.getRot()).fit().centerCrop().into(foto_cuoco);
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
    public void onAttach(Context context) {

        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void doSomething(String currentID) {
        this.currentId=currentID;
    }

}
