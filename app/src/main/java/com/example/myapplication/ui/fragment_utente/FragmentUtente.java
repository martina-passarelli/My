package com.example.myapplication.ui.fragment_utente;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.UtilitaEliminaAccount;
import com.example.myapplication.UtilityImage;
import com.example.myapplication.ui.fragment_seguiti.ListSeguiti;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;
import static androidx.core.content.ContextCompat.checkSelfPermission;

/*
    Questa classe rappresenta il frammento del profilo utente
 */
public class FragmentUtente extends Fragment {
    private String currentId;
    private static final int SELECT_PICTURE = 100;
    private TextView nickname, label_pass, città;
    //campi del profilo
    private EditText nomeUtente;
    private EditText biografia;
    private EditText mail;
    private EditText nuova_password;
    private EditText vecchia_password;
    private EditText telefono;

    private Context context;
    //bottoni per vedere la lista dei seguiti e per eliminare il profilo
    private Button seguiti, eliminaProfilo;
    private ImageButton loc;
    //immagine del profilo
    private CircleImageView img;
    //bottoni per modificare la foto e il profilo
    private FloatingActionButton modificaFoto;
    private  FloatingActionButton modificaProfilo;
    //id user correntemente loggato
    private String currentUsermail;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private Utente utente;
    private   String  nome, nick, bio, pass,tel;

    //scegliere l'immagine
    private Uri imageUri;
    private StorageReference storage;
    private boolean modifica_abilitata=false;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        context=inflater.getContext();
        return inflater.inflate(R.layout.fragment_utente, parent, false);

    }

    @SuppressLint({"ResourceAsColor", "RestrictedApi"})
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        //--------------------------COMPONENTI GRAFICHE--------------------------------------
        label_pass=(TextView) view.findViewById(R.id.label_pass);
        nomeUtente=(EditText)view.findViewById(R.id.nomeCompleto);
        nickname= (TextView) view.findViewById(R.id.nick);

        telefono=(EditText) view.findViewById(R.id.telefono);
        vecchia_password=(EditText) view.findViewById(R.id.vecchia_password);
        nuova_password=(EditText) view.findViewById(R.id.nuova_password);
        nuova_password.setText("");
        vecchia_password.setText("");
        mail=(EditText) view.findViewById(R.id.mail);
        biografia=(EditText) view.findViewById(R.id.bio);
        img= (CircleImageView) view.findViewById(R.id.imageMenu);

        //Button per la localizzazione
        loc=(ImageButton) view.findViewById(R.id.button);
        loc.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                getLocationPermission();
            }
        });

        //Set di città nel caso in cui è presente già una localizzazione
        città=(TextView) view.findViewById(R.id.text_loc);
        FirebaseFirestore.getInstance().collection("suggeriti").document(""+FirebaseAuth.getInstance().getUid()).
                get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot!=null){
                        String s= documentSnapshot.getString("città_eventi");
                        if(s!=null){
                            città.setText(s);
                        }
                }
            }
                });


        CardView card=(CardView)view.findViewById(R.id.card_seguiti);

        ScrollView scrollView = (ScrollView) view.findViewById(R.id.scroll);
        Drawable dr=scrollView.getBackground();
        seguiti=(Button)view.findViewById(R.id.button_seguiti);
        seguiti.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                scrollView.setBackgroundColor(Color.parseColor("#7E858080"));
                modificaProfilo.setClickable(false);
                load_seguiti();
                card.setVisibility(View.VISIBLE);
            }
        });


        FloatingActionButton close=view.findViewById(R.id.id_close);
        close.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                scrollView.setBackground(dr);
                getChildFragmentManager().popBackStack();
                modificaProfilo.setClickable(true);
                card.setVisibility(View.INVISIBLE);
            }
        });

        //------------------------------------------------------------------------------------
        //-----------------------BOTTONI-----------------------------------------------------
        modificaFoto=(FloatingActionButton) view.findViewById(R.id.modificaFoto);
        modificaFoto.setVisibility(View.GONE);
        modificaProfilo= (FloatingActionButton) view.findViewById(R.id.modificaProfilo);

        mAuth= FirebaseAuth.getInstance();
        eliminaProfilo= view.findViewById(R.id.elimina_account);
        eliminaProfilo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( mAuth.getCurrentUser()!=null) {
                    //***** uso questo metodo anche in profilo cuoco che avrà tipo = 1
                    UtilitaEliminaAccount.showDialog(context,mAuth.getUid(),0);
                }
            }
        });
        if(!currentId.equals(FirebaseAuth.getInstance().getUid())){
            //NEL CASO IN CUI SONO SUL PROFILO DI UN UTENTE CHE NON SONO IO, BISOGNA DISABILITARE ALCUNE VISTE.
            modificaProfilo.setVisibility(View.GONE);
            loc.setVisibility(View.INVISIBLE);
            loc.setClickable(false);
            view.findViewById(R.id.text_loc).setEnabled(false);
            view.findViewById(R.id.label_geoloc).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.view).setVisibility(View.INVISIBLE);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone((ConstraintLayout) view.findViewById(R.id.layout_nome));
            constraintSet.connect(R.id.nome,ConstraintSet.TOP,R.id.button_seguiti,ConstraintSet.BOTTOM,0);
            constraintSet.applyTo((ConstraintLayout) view.findViewById(R.id.layout_nome));
        }
        //----------------------TROVA UTENTE----------------------------------------------------
        db= FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("utenti2").document("" + currentId);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                utente = documentSnapshot.toObject(Utente.class);
                costruisciProfilo(utente);
            }
        });

        //riferimento allo storage
        storage = FirebaseStorage.getInstance().getReference();
        //--------------------BOTTONE MODIFICA FOTO-----------------------------------
        modificaFoto.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });
        label_pass.setVisibility(View.INVISIBLE);

        //-------------MODIFICA PROFILO--------------------------------------------------------------
        modificaProfilo.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View view) {
                //se non è in modalità di modifica
                if(modifica_abilitata==false) {
                    nuova_password.setText("");
                    vecchia_password.setText("");
                    label_pass.setVisibility(View.VISIBLE);
                    modificaFoto.setVisibility(View.VISIBLE);
                    vecchia_password.setVisibility(View.VISIBLE);
                    nuova_password.setVisibility(View.VISIBLE);
                    eliminaProfilo.setVisibility(View.VISIBLE);
                    modificaProfilo.setImageResource(R.drawable.modifica_abilitata);
                    nomeUtente.setEnabled(true);
                    biografia.setEnabled(true);
                    telefono.setEnabled(true);
                    //se clicca sul bottone
                    modifica_abilitata=true;

                }else{
                    nome = nomeUtente.getText().toString();
                    bio= biografia.getText().toString();
                    tel = telefono.getText().toString();

                    modificaProfilo.setImageResource(R.drawable.modifica);
                    nick = nomeUtente.getText().toString();
                    nickname.setText(nick);
                    nomeUtente.setEnabled(false);
                    biografia.setEnabled(false);
                    telefono.setEnabled(false);
                    modificaFoto.setVisibility(View.GONE);

                    //quando riclicca sul bottone aggiorno le informazioni dell'utente
                    try {
                        //se ha scelto un'immagine
                        if (imageUri != null) {
                            //riferimento allo storage
                            StorageReference sRef = storage.child(currentUsermail+ "." + "jpg");
                            //inserimento dell'immagine
                            sRef.putFile(imageUri)
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            Toast.makeText(getContext(), "File Uploaded ", Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }

                        if( !"".equals(vecchia_password.getText().toString()) ){
                            System.out.println(vecchia_password.getText()+"VECCHIA");
                            if(vecchia_password.getText().toString().equals(pass) && !nuova_password.getText().equals("")
                                    && nuova_password.getText().length()>=6
                                    && !vecchia_password.getText().equals(nuova_password.getText())){
                                String nuova_pass=nuova_password.getText().toString();

                                AuthCredential credential = EmailAuthProvider
                                        .getCredential(currentUsermail, utente.getPassword());
                                mAuth.getCurrentUser().reauthenticate(credential)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {

                                                    mAuth.getCurrentUser().updatePassword(nuova_pass).addOnCompleteListener(new OnCompleteListener<Void>() {
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

                                utente.setPassword(nuova_pass);
                            } else{
                                CharSequence text = "Inserisci correttamente le password, ricorda che " +
                                        "la nuova password deve avere almeno 6 caratteri!";
                                int duration = Toast.LENGTH_SHORT;
                                Toast.makeText(getContext(),text, duration).show();
                            }
                        }
                        //salvataggio delle informazioni dell'utente
                        Utente up = new Utente(nome,utente.getEmail(),nick,bio,tel, utente.getPassword(),currentUsermail+".jpg",utente.getRot());
                        db.collection("utenti2").document(currentId).set(up);

                        nuova_password.setText("");
                        vecchia_password.setText("");

                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    nuova_password.setVisibility(View.INVISIBLE);
                    vecchia_password.setVisibility(View.INVISIBLE);
                    label_pass.setVisibility(View.INVISIBLE);
                    eliminaProfilo.setVisibility(View.INVISIBLE);
                    modifica_abilitata=false;
                }
            }
        });
    }



    private void load_seguiti() {
        ListSeguiti lista = new ListSeguiti();
        Bundle bundle=new Bundle();
        bundle.putString("id",currentId);
        lista.setArguments(bundle);
        getChildFragmentManager().beginTransaction().replace(R.id.fragment,lista).commit();

    }


    public void doSomething(String currentID) {

        this.currentId=currentID;

    }
    public void costruisciProfilo(Utente utente) {
        nome=utente.getNome();
        nick=utente.getNick();
        currentUsermail=utente.getEmail();//DA VERIFICARE
        //se il nome o il nick sono null vengono impostati come l'inizio della mail
        if(nome==""){
            nome=currentUsermail.substring(0,currentUsermail.indexOf("@"));
        }
        if(nick==""){
            nick=currentUsermail.substring(0,currentUsermail.indexOf("@"));
        }

        pass=utente.getPassword();
        bio=utente.getBio();
        tel=utente.getTel();

        //--------------SETTARE I DATI NELLA COMPONENTE GRAFICA-------------------------------------

        nomeUtente.setText(nome);
        if(tel!=null) telefono.setText(tel);
        mail.setText(currentUsermail);
        if(bio!=null) biografia.setText(bio);
        nickname.setText(nick);

        //--------------------CARICA L'IMMAGINE NELLA VIEW------------------------------------------
        if(utente.getImageProf() !=null){
            try {
                storage.child(currentUsermail+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.with(getActivity()).load(uri).
                                networkPolicy(NetworkPolicy.OFFLINE).
                                rotate(utente.getRot()).fit()
                                .centerCrop().into(img,new Callback() {
                            @Override
                            public void onSuccess() {

                            }
                            @Override
                            public void onError() {
                                System.out.println("on error");
                                Picasso.with(getActivity()).load(uri)
                                        .rotate(utente.getRot())
                                        .fit().centerCrop().into(img);
                            }
                        });
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //------------------LABEL NON MODIFICABILI--------------------------------------------------
        nomeUtente.setEnabled(false);
        biografia.setEnabled(false);
        telefono.setEnabled(false);
        mail.setEnabled(false);

    }
    private void cambiaPassword(String nuova_pass) {

        utente.setPassword(nuova_pass);
    }


    //PER IMMAGINE DEL PROFILO
    private static int RESULT_LOAD_IMAGE = 1;
    //metodo per scegliere l'immagine dalla galleria
    private void chooseImage() {
        //controlla che sia stato già dato il permesso per accedere alla galleria dell'utente
        if (checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        //se il permesso è stato dato, viene fatta scegliere la galleria da dove prendere l'immagine
        else {
            Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, RESULT_LOAD_IMAGE);
        }
    }
    /*
          Questo metodo viene invocato dopo che l'utente ha dato o meno il consenso di accedere
          alla sua galleria
       */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        //se il permesso è stato dato, viene fatta scegliere la galleria da dove prendere l'immagine
        if (checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, RESULT_LOAD_IMAGE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //se ha scelto un'immagine e non è vuota, si ottiene il suo URI
        if(requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK
                && data != null && data.getData() != null )
            imageUri = data.getData();
        //se l'URI ottenuto non è null
        utente.setImageProf(currentUsermail+".jpg");
        System.out.println("PRINTA IMG " + imageUri);
        if (imageUri != null) {
            //si ottine il suo path
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
                //l'immagine potrebbe avere il tag orientation != 0 , in questo caso bisogna girare l'immagine
                int rotation = Integer.parseInt(exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION));
                int rotationInDegrees = UtilityImage.exifToDegrees(rotation);
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                //se l'immagine ha un'orientazione la giro
                utente.setRot(rotationInDegrees);
                bitmap= UtilityImage.rotate(bitmap,rotationInDegrees);
                img.setImageBitmap(bitmap);
                System.out.println("STAMPA BITMAP "+ bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }






    //PER LA GEOLOCALIZZAZIONE
    private boolean mLocationPermissionsGranted=false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION= Manifest.permission.ACCESS_COARSE_LOCATION;
    private static  final int LOCATION_PERMISSION_REQUEST_CODE=1234;

    private void getLocationPermission(){
        String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};
        if(checkSelfPermission(this.getContext(), FINE_LOCATION )== PackageManager.PERMISSION_GRANTED){
            if(checkSelfPermission(this.getContext(), COURSE_LOCATION )== PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted=true;
                //init
                getDeviceLocation();
            }else {
                ActivityCompat.requestPermissions(this.getActivity(),permission,LOCATION_PERMISSION_REQUEST_CODE);
            }

        }else {
            ActivityCompat.requestPermissions(this.getActivity(),permission,LOCATION_PERMISSION_REQUEST_CODE);
        }
    }


    private void getDeviceLocation(){
        mFusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this.getActivity());
        try {
            if(mLocationPermissionsGranted){
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            try {
                                Location currentLocation= (Location) task.getResult();
                                double longitudine= currentLocation.getLongitude();
                                double latitudine=currentLocation.getLatitude();
                                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                                List<Address> addresses = null;

                                //Preleva nome città
                                addresses = geocoder.getFromLocation(latitudine, longitudine, 1);
                                String cityName = addresses.get(0).getLocality();
                                String city_view=addresses.get(0).getAddressLine(0);
                                città.setText(city_view);
                                //Posizioniamo il luogo scelto in firebase
                                FirebaseFirestore.getInstance().collection("suggeriti").document(""+FirebaseAuth.getInstance().getUid()).update("città_eventi", cityName);
                            } catch (Exception e) {
                                Toast.makeText(getActivity(), "Impossibile accedere alla posizione. Accendi il GPS.", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(getActivity(), "Impossibile accedere alla posizione. Accendi il GPS.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        }catch (Exception e){
            Toast.makeText(getActivity(), "Impossibile accedere alla tua posizione, riprova più tardi.", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        context = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        context = null;
    }

}
