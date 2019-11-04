package com.example.myapplication.ui.fragment_cuoco;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.UtilitaEliminaAccount;
import com.example.myapplication.UtilityImage;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static androidx.core.content.ContextCompat.checkSelfPermission;


public class FragmentCuoco extends Fragment {
    //*****eliminaProfilo
    private Button eliminaProfilo;
    private FirebaseAuth mAuth;
    private Context context;
    private String currentId;
    private TextView nomeCuoco,emailCuoco,follower;
    private EditText password;
    private CircleImageView foto_cuoco;
    private Button ricette,eventi,segui;
    private FloatingActionButton add;
    private String utente_corrente=FirebaseAuth.getInstance().getUid();

    //****per la modifica del profilo
    private FloatingActionButton modificaProfilo, modificaFoto;
    private boolean modificaAbilitata;
    private EditText nuovaPassword, vecchiaPassword;
    private Uri imageUri;
    private static final int SELECT_PICTURE = 100;
    private String id_utente_corrente=FirebaseAuth.getInstance().getUid();

    private Cuoco cuoco;
    private FirebaseFirestore db;
    private StorageReference storage=FirebaseStorage.getInstance().getReference();
    private boolean sezione_eventi=false;
    private Bundle bundle= new Bundle();

    //bottone che mostra le informazioni sul come poter eliminare una ricetta o un evento
    //viene reso invisibile se l'utente corrente non è il possessore del profilo
    private Button showInfo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //NEL MOMENTO IN CUI SI RITORNA AL FRAMMENTO, BISOGNA RIAPRIRE IL GIUSTO FRAMMENTO FIGLIO.
        if(!sezione_eventi) {
            //SEZIONE RICETTE
            ListaRicette_Fragment ricette_fragment = new ListaRicette_Fragment();
            bundle.putString("id",currentId);
            ricette_fragment.setArguments(bundle);
            ricette_fragment.ottieni_lista(currentId);
            getChildFragmentManager().beginTransaction().replace(R.id.frame_cuoco, ricette_fragment).commit();
        }else
            //SEZIONE EVENTI
            crea_lista_eventi(currentId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context=inflater.getContext();
        View view= inflater.inflate(R.layout.fragment_cuoco, container, false);
        return view;
    }


    //QUANDO SI RITORNA AL BOTTONE PRECEDENTE BISGNA TORNARE IN SEZIONE EVENTI, QUINDI SETTARLO COME TRUE
    @SuppressLint({"ResourceAsColor", "RestrictedApi"})
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        nomeCuoco=(TextView)view.findViewById(R.id.nome_cuoco);
        nomeCuoco.setClickable(false);
        emailCuoco=(TextView)view.findViewById(R.id.email_cuoco);
        nuovaPassword=(EditText)view.findViewById(R.id.edit_pass_cuoco);
        foto_cuoco=(CircleImageView)view.findViewById(R.id.image_cuoco);
        ricette=(Button)view.findViewById(R.id.button_lista);
        follower=(TextView)view.findViewById(R.id.numFollw);
        eventi=(Button)view.findViewById(R.id.button_eventi);
        add=(FloatingActionButton)view.findViewById(R.id.add_cuoco);
        segui=(Button)view.findViewById(R.id.button_segui);
        showInfo= (Button) view.findViewById(R.id.info);

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

        if(!currentId.equals(FirebaseAuth.getInstance().getUid())){
            //DISABILITA I TASTI DI MODIFICA PROFILO.
            add.setVisibility(View.INVISIBLE);
            add.setClickable(false);
            //disabilita lo showInfo
            showInfo.setVisibility(View.GONE);
            /*
            VERIFICA SE L'UTENTE SEGUE IL CUOCO NEL CASO IN CUI NON E' IL CUOCO STESSO AD ENTRARE
            NEL PROFILO.
             */
            sei_seguace();
        }

        //PER MANTENERE I COLORI UGUALI DURANTE I CLICK, VENGONO SALVATI I COLORI.
        ColorStateList click= ricette.getBackgroundTintList();
        ColorStateList no_click= eventi.getBackgroundTintList();

        /*-----------------------------------------------------------------------------------------
             APRE LA SEZIONE EVENTI: ESSA CONTIENE GLI EVENTI DA LUI CREATI.
         -----------------------------------------------------------------------------------------*/
        eventi.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if(sezione_eventi==false) {
                    crea_lista_eventi(currentId);
                    ricette.setClickable(true);
                    ricette.setBackgroundTintList(no_click);
                    eventi.setClickable(false);
                    eventi.setBackgroundTintList(click);
                    sezione_eventi = true;
                }
            }
        });
        //------------------------------------------------------------------------------------------

        /*-----------------------------------------------------------------------------------------
             APRE LA SEZIONE RICETTE: ESSA CONTIENE LE RICETTE CREATE DAL CUOCO.
         ----------------------------------------------------------------------------------------*/
        ricette.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if(sezione_eventi==true) {
                    /*
                    SICURAMENTE SE L'UTENTE NON SI TROVA NELLA SEZIONE RICETTE SARA' IN QUELLA EVENTI,
                    MA PER ARRIVARCI, HA LASCIATO NELLO STACK IL FRAMMENTO DELLA SEZIONE RICETTE.
                    INOLTRE, RIMUOVIAMO IL FRAMMENTO PRECEDENTE, TANTO OGNI VOLTA VIENE RIGENERATO.
                     */
                    getFragmentManager().popBackStackImmediate();
                    getChildFragmentManager().popBackStack("LISTA_RICETTE", FragmentManager.POP_BACK_STACK_INCLUSIVE);

                    ricette.setClickable(false);
                    ricette.setBackgroundTintList(click);
                    eventi.setClickable(true);
                    eventi.setBackgroundTintList(no_click);
                    sezione_eventi = false;
                }
            }
        });
        //------------------------------------------------------------------------------------------



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
                    //view.findViewById(R.id.add_cuoco).setVisibility(View.VISIBLE);
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
                segui_cuoco();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        eliminaProfilo=view.findViewById(R.id.elimina_profilo_cuoco);
        eliminaProfilo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){

                UtilitaEliminaAccount.showDialog(context,mAuth.getUid(),1);

            }
        });

        showInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                alertDialog.setTitle("Informazioni sull'eliminazione");
                alertDialog.setMessage("Per eliminare una ricetta o un evento che hai creato ti basta" +
                        " fare swipe left!");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Ho capito!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.BLACK);
                    }
                });
                alertDialog.show();

            }
        });
    }






    @SuppressLint("RestrictedApi")
    private void abilitaModifica() {
        nomeCuoco.setClickable(true);
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
        eliminaProfilo.setVisibility(View.VISIBLE);
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
        nomeCuoco.setClickable(false);
        nuovaPassword.setEnabled(false);
        vecchiaPassword.setEnabled(false);
        eliminaProfilo.setVisibility(View.INVISIBLE);
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



    /*
    IL METODO sei_seguace() POVVEDE A VERIFICARE SE L'UTENTE DI RIFERIMENTO E' UN SEGUACE O NO DI UN
    CUOCO. IN QUESTO MODO VIENE SETTATO IL TASTO SEGUI.
     */
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

    /*
    crea_lista_eventi(String currentId) APRE LA LISTA DEGLI EVENTI DEL CUOCO CORRENTE.
     */
    private void crea_lista_eventi(String currentId) {
        Bundle bundle= new Bundle();
        bundle.putString("id",currentId);
        Lista_Fragment_Evento fragment_evento= new Lista_Fragment_Evento();
        fragment_evento.setArguments(bundle);
        fragment_evento.doSomething(currentId);
        getChildFragmentManager().beginTransaction().replace(R.id.frame_cuoco,fragment_evento).
                addToBackStack("LISTA_RICETTE").commit();
    }

    /*
    aggiungi_ricetta() APRE IL FRAMMENTO PER LA CREAZIONE DI UNA NUOVA RICETTA.
     */
    public void aggiungi_ricetta(){
        Fragment_CreaRicetta fragment_creaRicetta=new Fragment_CreaRicetta();
        getChildFragmentManager().beginTransaction().replace(R.id.frame_cuoco,fragment_creaRicetta).addToBackStack(null).commit();
    }

    @SuppressLint("RestrictedApi")
    public void changeVisibility(){
        add.setVisibility(View.VISIBLE);
    }

    /*
    ottieni_dati() PROVVEDE A PRELEVARE I DATI DEL CUOCO CORRENTE DAL FIREBASE.
     */
    private void ottieni_dati() {
        db= FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("utenti2").document("" + currentId);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {

            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                cuoco = documentSnapshot.toObject(Cuoco.class);
                nomeCuoco.setText(cuoco.getNome());
                emailCuoco.setText(cuoco.getEmail());
                int d= (int) cuoco.getFollower();
                follower.setText(""+d);

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


    /*

    QUANDO INIZI A SEGUIRE/ NON SEGUIRE IL CUOCO, PUO' DARSI CHE QUALCUN ALTRO CONTEMPORANEAMENTE
    DECIDA DI SEGUIRLO, QUINDI SI HA ACCESSO CONCORRENTE ALLA LISTA DEI SEGUACI NEL CUOCO. QUESTA
    LISTA SERVE PER L'INVIO DELLE NOTIFICHE AI SEGUACI. PER EVITARE DEI DATI DANNEGGIATI FIREBASE
    UTILIZZA LA TRANSAZIONE. QUESTA OPERAZIONE VIENE ESEGUITA NEL METODO aggiorna(String id_user)

     */

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
                    aggiorna(lista_cuochi,false);
                    segui.setBackgroundColor( -3355444);
                    segui.setText("Segui");
                    int foll=Integer.parseInt(follower.getText().toString());
                    foll=foll-1;
                    follower.setText(""+foll);

                }else {
                    lista_cuochi.add(currentId);
                    aggiorna(lista_cuochi,true);
                    segui.setBackgroundColor(-7829368);
                    segui.setText("Non seguire più");
                    int foll=Integer.parseInt(follower.getText().toString());
                    foll=foll+1;
                    follower.setText(""+foll);
                }
            }
        });

    }

    private void aggiorna(ArrayList<String> lista_cuochi, boolean aggiungi) {
        CollectionReference docRef = FirebaseFirestore.getInstance().collection("utenti2");
        DocumentReference ref=docRef.document(""+currentId);
        docRef.document(""+utente_corrente).update("lista_cuochi", lista_cuochi).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //DA VERIFICARE SE FUNZIONA
                FirebaseFirestore.getInstance().runTransaction(new Transaction.Function<Void>() {
                    @Override
                    public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                        DocumentSnapshot snapshot = transaction.get(ref);
                        if(aggiungi) {
                            int d=snapshot.getDouble("follower").intValue()+1;
                            transaction.update(ref,"follower",d);
                            transaction.update(ref,"seguaci", FieldValue.arrayUnion(utente_corrente));
                            //INVIO DELLA NOTIFICA AL CUOCO QUANDO SI COMINCIA A SEGUIRE.
                            HashMap<String,Object> notificationMessage= new HashMap<>();
                            notificationMessage.put("message", "Ha iniziato a seguirti!");
                            notificationMessage.put("from",utente_corrente);
                            notificationMessage.put("id",utente_corrente);
                            notificationMessage.put("profilo",1);
                            ref.collection("Notifications").add(notificationMessage);

                        }
                        else {
                            int d=snapshot.getDouble("follower").intValue()-1;
                            transaction.update(ref,"follower",d);
                            transaction.update(ref,"seguaci", FieldValue.arrayRemove(utente_corrente));

                        }
                        return null;
                    }
                });
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


    /*
    METODI PER MODIFICAARE L'IMMAGINE DEL PROFILO DEL CUOCO.
     */

    private static int RESULT_LOAD_IMAGE = 1;


    private void chooseImage() {
        if (checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            // ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
            //       Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        }
        // ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        else {
            Intent i = new Intent(
                    Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, RESULT_LOAD_IMAGE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        System.out.println("entro");
        if (checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            System.out.println("entro2");
            Intent i = new Intent(
                    Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, RESULT_LOAD_IMAGE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null )
            imageUri = data.getData();

        cuoco.setImageProf(cuoco.getEmail()+".jpg");
        if (imageUri != null) {
            try {
                String imagePath;
                if (data.toString().contains("content:")) {
                    imagePath = UtilityImage.getRealPathFromURI(imageUri,getActivity());
                } else if (data.toString().contains("file:")) {
                    imagePath = imageUri.getPath();
                } else {
                    imagePath = null;
                }

                ExifInterface exifInterface = new ExifInterface(imagePath);
                int rotation = Integer.parseInt(exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION));
                int rotationInDegrees = UtilityImage.exifToDegrees(rotation);
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                //se l'immagine ha un'orientazione la giro
                cuoco.setRot(rotationInDegrees);
                bitmap= UtilityImage.rotate(bitmap,rotationInDegrees);
                foto_cuoco.setImageBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }




}
