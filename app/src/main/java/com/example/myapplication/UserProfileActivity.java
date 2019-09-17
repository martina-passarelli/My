package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity<modifica_abilitata> extends AppCompatActivity {
    private static final int SELECT_PICTURE = 100;
    //label nickname
    private TextView nickname;
    //campi del profilo
    private EditText nomeUtente;
    private EditText biografia;
    private EditText mail;
    private EditText nuova_password;
    private EditText vecchia_password;
    private EditText telefono;
    private EditText password;
    //immagine del profilo
    private CircleImageView img;
    //bottoni
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

    private String id;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userprofile);
        //--------------------------COMPONENTI GRAFICHE--------------------------------------
        nomeUtente=(EditText)findViewById(R.id.nomeCompleto);
        nickname= (TextView) findViewById(R.id.nick);
        password=(EditText) findViewById(R.id.password);
        telefono=(EditText) findViewById(R.id.telefono);
        vecchia_password=(EditText) findViewById(R.id.vecchia_password);
        nuova_password=(EditText) findViewById(R.id.nuova_password);
        nuova_password.setText("");
        vecchia_password.setText("");
        mail=(EditText) findViewById(R.id.mail);
        biografia=(EditText) findViewById(R.id.bio);
        img= (CircleImageView) findViewById(R.id.imageMenu);
       //------------------------------------------------------------------------------------
        //-----------------------BOTTONI-----------------------------------------------------
        modificaFoto=(FloatingActionButton) findViewById(R.id.modificaFoto);
        modificaFoto.setVisibility(View.GONE);

        modificaProfilo= (FloatingActionButton) findViewById(R.id.modificaProfilo);
        //-----------------------------------------------------------------------------------
        //----------------------UTENTE LOGGATO-----------------------------------------------
        mAuth=FirebaseAuth.getInstance();

        currentUsermail = mAuth.getCurrentUser().getEmail();
        //-----------------------------------------------------------------------------------
        db= FirebaseFirestore.getInstance();
        //recupero le informazioni dell'utente corrente
        Task<QuerySnapshot> col= db.collection("utenti").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot col) {
                if(!col.isEmpty()){
                    List<DocumentSnapshot> list=col.getDocuments();
                    for(DocumentSnapshot d:list){
                        utente = d.toObject(Utente.class);
                        id=d.getId();
                        if (utente.getEmail().equals(currentUsermail)) {
                            //settaggio del profilo
                            costruisciProfilo(utente);
                            break;
                        }
                    }
                }
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

        //-------------MODIFICA PROFILO--------------------------------------------------------------
        modificaProfilo.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View view) {
                //se non è in modalità di modifica
                if(modifica_abilitata==false) {
                    nuova_password.setText("");
                    vecchia_password.setText("");
                    modificaFoto.setVisibility(View.VISIBLE);
                    vecchia_password.setVisibility(View.VISIBLE);
                    nuova_password.setVisibility(View.VISIBLE);
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
                                            Toast.makeText(getApplicationContext(), "File Uploaded ", Toast.LENGTH_LONG).show();
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
                                                                Toast.makeText(getApplicationContext(),text, duration).show();
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                password.setText(nuova_pass);
                                utente.setPassword(nuova_pass);
                            } else{
                                CharSequence text = "Inserisci correttamente le password, ricorda che " +
                                        "la nuova password deve avere almeno 6 caratteri!";
                                int duration = Toast.LENGTH_SHORT;
                                Toast.makeText(getApplicationContext(),text, duration).show();
                            }
                        }
                        //salvataggio delle informazioni dell'utente
                        Utente up = new Utente(nome,utente.getEmail(),nick,bio,tel, utente.getPassword(),currentUsermail+".jpg",utente.getRot());
                        db.collection("utenti").document(id).set(up);
                        modifica_abilitata=false;
                        nuova_password.setText("");
                        vecchia_password.setText("");
                        nuova_password.setVisibility(View.GONE);
                        vecchia_password.setVisibility(View.GONE);

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });

        }

    private void cambiaPassword(String nuova_pass) {
        password.setText(nuova_pass);
        utente.setPassword(nuova_pass);
    }

    //metodo per scegliere l'immagine dalla galleria
    private void chooseImage() {
        ActivityCompat.requestPermissions(UserProfileActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, SELECT_PICTURE);
    }
    //activity result del bottone immagine
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SELECT_PICTURE && resultCode == RESULT_OK
                && data != null && data.getData() != null )
            imageUri = data.getData();
            utente.setImageProf(currentUsermail+".jpg");
        if (imageUri != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                //se l'immagine ha un'orientazione la giro
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();

                if (width > height) {
                    bitmap = rotate(bitmap, 90);
                    utente.setRot(true);
                }
                else utente.setRot(false);
                img.setImageBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

    public void costruisciProfilo(Utente utente) {
        nome=utente.getNome();
        nick=utente.getNick();
        //se il nome o il nick sono null vengono impostati come l'inizio della mail
        if(nome==""){
            nome=currentUsermail.substring(0,currentUsermail.indexOf("@"));
            System.out.println("nomeeeee"+nome);
        }
        if(nick==""){
            nick=currentUsermail.substring(0,currentUsermail.indexOf("@"));
        }
        pass=utente.getPassword();
        bio=utente.getBio();
        tel=utente.getTel();

        //--------------SETTARE I DATI NELLA COMPONENTE GRAFICA---------------------------------------

        nomeUtente.setText(nome);
        password.setText(pass);
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
                       if(utente.getRot())
                            Picasso.with(UserProfileActivity.this).load(uri).rotate(90).fit().centerCrop().into(img);
                       else
                        Picasso.with(UserProfileActivity.this).load(uri).fit().centerCrop().into(img);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //------------------LABEL NON MODIFICABILI---------------------------------------------------
        nomeUtente.setEnabled(false);
        biografia.setEnabled(false);
        telefono.setEnabled(false);
        mail.setEnabled(false);
        password.setEnabled(false);
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(UserProfileActivity.this, MainActivity.class);
        startActivity(intent);

    }

}
