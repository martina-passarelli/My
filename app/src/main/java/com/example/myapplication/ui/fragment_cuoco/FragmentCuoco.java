package com.example.myapplication.ui.fragment_cuoco;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.ui.fragment_nuovo_evento.FragmentNuovoEvento;
import com.example.myapplication.ui.fragment_utente.Utente;
import com.example.myapplication.ui.fragment_evento.Lista_Fragment_Evento;
import com.example.myapplication.ui.fragment_ricetta.ListaRicette_Fragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class FragmentCuoco extends Fragment {
   private String currentId;
    private TextView nomeCuoco,emailCuoco;
    private EditText password;
    private CircleImageView foto_cuoco;
    private Button ricette,eventi,segui;
    private FloatingActionButton add;
    private String utente_corrente=FirebaseAuth.getInstance().getUid();

    //****per la modifica del profilo
    private FloatingActionButton modificaProfilo;
    private FloatingActionButton modificaFoto;
    private boolean modificaAbilitata;
    private EditText nuovaPassword;
    private EditText vecchiaPassword;
    private Uri imageUri;
    private static final int SELECT_PICTURE = 100;
    private String id_utente_corrente=FirebaseAuth.getInstance().getUid();

    private Cuoco cuoco;
    private FirebaseFirestore db;
    private StorageReference storage=FirebaseStorage.getInstance().getReference();
    private boolean sezione_eventi=false;
    private Bundle bundle= new Bundle();

    @Override
    public void onCreate(Bundle savedInstanceState) {        super.onCreate(savedInstanceState);
        if(!sezione_eventi) {
            ListaRicette_Fragment ricette_fragment = new ListaRicette_Fragment();
            bundle.putString("id",currentId);
            ricette_fragment.setArguments(bundle);
            ricette_fragment.ottieni_lista(currentId);
            getChildFragmentManager().beginTransaction().replace(R.id.frame_cuoco, ricette_fragment).commit();
        }else crea_lista_eventi(currentId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_cuoco, container, false);
        return view;
    }


    //QUANDO SI RITORNA AL BOTTONE PRECEDENTE BISGNA TORNARE IN SEZIONE EVENTI, QUINDI SETTARLO COME TRUE
    @SuppressLint({"ResourceAsColor", "RestrictedApi"})
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        nomeCuoco=(TextView)view.findViewById(R.id.nome_cuoco);
        emailCuoco=(TextView)view.findViewById(R.id.email_cuoco);
        nuovaPassword=(EditText)view.findViewById(R.id.edit_pass_cuoco);
        foto_cuoco=(CircleImageView)view.findViewById(R.id.image_cuoco);
        ricette=(Button)view.findViewById(R.id.button_lista);

        eventi=(Button)view.findViewById(R.id.button_eventi);
        add=(FloatingActionButton)view.findViewById(R.id.add_cuoco);
        segui=(Button)view.findViewById(R.id.button_segui);

        //**********modifica profilo*****************
        modificaFoto = view.findViewById(R.id.modificaFotoCuoco);
        modificaFoto.setVisibility(View.GONE);


        nuovaPassword.setVisibility(View.GONE);
        vecchiaPassword=view.findViewById(R.id.vecchiaPass);
        vecchiaPassword.setVisibility(View.GONE);

        modificaProfilo = view.findViewById(R.id.modificaCuoco);
        if(! currentId.equals(id_utente_corrente)) {
            modificaProfilo.setVisibility(View.GONE);
            modificaProfilo.setClickable(false);
        }


        //PRELEVIAMO I DATI APPARTENENTI AL CUOCO E SETTIAMO LE LABEL
        ottieni_dati();

        //VERIFICA SE L'UTENTE SEGUE IL CUOCO
        sei_seguace();

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
                    getChildFragmentManager().popBackStack("LISTA", FragmentManager.POP_BACK_STACK_INCLUSIVE);
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
                    aggiungi_evento();
                    modificaProfilo.setVisibility(View.GONE);
                }else {
                    aggiungi_ricetta();
                    modificaProfilo.setVisibility(View.GONE);
                }
            }
        });

        modificaProfilo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!modificaAbilitata){
                    abilitaModifica();
                }else {
                    aggiornaDati();
                }
            }
        });

        modificaFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });

        segui.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                //QUANDO SEGUI UN UTENTE LO DEVI INSERIRE NELLA SUA LISTA DI CUOCHI SEGUITI.
                segui_cuoco();
            }
        });
    }

    private void chooseImage() {
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, SELECT_PICTURE);
    }

    @SuppressLint("RestrictedApi")
    private void abilitaModifica() {
        modificaFoto.setVisibility(View.VISIBLE);
        ricette.setVisibility(View.GONE);
        eventi.setVisibility(View.GONE);
        add.setVisibility(View.GONE);
        modificaProfilo.setImageResource(R.drawable.modifica_abilitata);
        nomeCuoco.setEnabled(true);
        nuovaPassword.setEnabled(true);
        vecchiaPassword.setEnabled(true);
        nuovaPassword.setVisibility(View.VISIBLE);
        vecchiaPassword.setVisibility(View.VISIBLE);
        modificaAbilitata=true;

    }

    @SuppressLint("RestrictedApi")
    private void aggiornaDati(){
        String nome,pass1,pass2;
        nome=nomeCuoco.getText().toString();
        pass1=vecchiaPassword.getText().toString();
        pass2=nuovaPassword.getText().toString();
        modificaProfilo.setImageResource(R.drawable.modifica);
        nomeCuoco.setEnabled(false);
        nuovaPassword.setEnabled(false);
        vecchiaPassword.setEnabled(false);
        modificaFoto.setVisibility(View.GONE);
        nuovaPassword.setVisibility(View.GONE);
        vecchiaPassword.setVisibility(View.GONE);
        ricette.setVisibility(View.VISIBLE);
        eventi.setVisibility(View.VISIBLE);
        add.setVisibility(View.VISIBLE);
        vecchiaPassword.setText("");
        nuovaPassword.setText("");
        try {
            //se ha scelto un'immagine
            if (imageUri != null) {
                //riferimento allo storage
                StorageReference sRef = storage.child(cuoco.getEmail() + "." + "jpg");
                //inserimento dell'immagine
                sRef.putFile(imageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Toast.makeText(getContext(), "File Uploaded ", Toast.LENGTH_LONG).show();
                            }
                        });
            }
            if( !"".equals(pass1) ){
                if(pass1.equals(cuoco.getPassword()) && !pass2.equals("")
                        && pass2.length()>=6
                        && !pass1.equals(pass2)){
                    AuthCredential credential = EmailAuthProvider
                            .getCredential(cuoco.getEmail(), cuoco.getPassword());
                    FirebaseAuth.getInstance().getCurrentUser().reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {FirebaseAuth.getInstance().getCurrentUser().updatePassword(pass2).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                            } else {
                                                CharSequence text = "Inserisci correttamente la tua vecchia  password!";
                                                int duration = Toast.LENGTH_SHORT;
                                                Toast.makeText(getContext(),text, duration).show();
                                            }
                                        }
                                    });
                                    }
                                }
                            });
                    cuoco.setPassword(pass2);
                } else{
                    CharSequence text = "Inserisci correttamente le password, ricorda che " +
                            "la nuova password deve avere almeno 6 caratteri!";
                    int duration = Toast.LENGTH_SHORT;
                    Toast.makeText(getContext(),text, duration).show();
                }
            }
            //salvataggio delle informazioni dell'utente
            Cuoco up = new Cuoco(nome,cuoco.getPassword(),cuoco.getEmail(),cuoco.getEmail()+".jpg",cuoco.getRot());
            db.collection("utenti2").document(currentId).set(up);
            modificaAbilitata=false;
        }catch (Exception e){

        }



    }



    private void sei_seguace() {
        FirebaseFirestore.getInstance().collection("utenti2").document(""+utente_corrente).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                ArrayList<String> lista=(ArrayList<String>)documentSnapshot.get("lista_cuochi");
                if(lista!=null &&lista.contains(currentId)){
                    segui.setBackgroundColor(-7829368);
                    segui.setText("Non seguire più");
                }
            }
        });
    }

    private void crea_lista_eventi(String currentId) {
        Bundle bundle= new Bundle();
        bundle.putString("id",currentId);
        bundle.putBoolean("do",true);
        Lista_Fragment_Evento fragment_evento= new Lista_Fragment_Evento();
        fragment_evento.setArguments(bundle);
        fragment_evento.doSomething(currentId);
        getChildFragmentManager().beginTransaction().replace(R.id.frame_cuoco,fragment_evento).addToBackStack("LISTA").commit();
    }

    public void aggiungi_ricetta(){
        Fragment_CreaRicetta fragment_creaRicetta=new Fragment_CreaRicetta();
        getChildFragmentManager().beginTransaction().replace(R.id.frame_cuoco,fragment_creaRicetta).commit();
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

                if(currentId.equals(FirebaseAuth.getInstance().getUid())) {
                    //Il cuoco non può seguire se stesso
                    segui.setVisibility(View.INVISIBLE);
                    segui.setClickable(false);
                }

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



    private void aggiungi_evento(){
        FragmentNuovoEvento fragmentNuovoEvento = new FragmentNuovoEvento();
        getChildFragmentManager().beginTransaction().replace(R.id.frame_cuoco,fragmentNuovoEvento).commit();
    }


    public void segui_cuoco(){
        FirebaseFirestore.getInstance().collection("utenti2").document(""+utente_corrente).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Utente utente= documentSnapshot.toObject(Utente.class);
                ArrayList<String> lista_cuochi=new ArrayList<>();

                if(utente.getLista_cuochi()!=null){
                    lista_cuochi=utente.getLista_cuochi();
                }
                if(lista_cuochi.contains(currentId)){
                    lista_cuochi.remove(currentId);
                    aggiorna(lista_cuochi);
                    segui.setBackgroundColor( -3355444);
                    segui.setText("Segui");
                }else {
                    lista_cuochi.add(currentId);
                    aggiorna(lista_cuochi);
                    segui.setBackgroundColor(-7829368);
                    segui.setText("Non seguire più");
                }
            }
        });

    }

    private void aggiorna(ArrayList<String> lista_cuochi) {
        FirebaseFirestore.getInstance().collection("utenti2").document(""+utente_corrente).update("lista_cuochi", lista_cuochi).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {}
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SELECT_PICTURE && resultCode == RESULT_OK
                && data != null && data.getData() != null )
            imageUri = data.getData();
        cuoco.setImageProf(cuoco.getEmail()+".jpg");
        if (imageUri != null) {
            try {
                String imagePath;
                if (data.toString().contains("content:")) {
                    imagePath = getRealPathFromURI(imageUri);
                } else if (data.toString().contains("file:")) {
                    imagePath = imageUri.getPath();
                } else {
                    imagePath = null;
                }

                ExifInterface exifInterface = new ExifInterface(imagePath);
                int rotation = Integer.parseInt(exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION));
                int rotationInDegrees = exifToDegrees(rotation);
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                //se l'immagine ha un'orientazione la giro
                cuoco.setRot(rotationInDegrees);
                bitmap=rotate(bitmap,rotationInDegrees);
                foto_cuoco.setImageBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = getActivity().getContentResolver().query(contentUri, proj, null, null,
                    null);
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            System.out.println("rota");
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    private Bitmap rotate(Bitmap bm, int rotation) {
        if (rotation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            try {
                Bitmap bmOut = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
                return bmOut;
            }catch (Exception e){}


        }
        return bm;
    }

}
