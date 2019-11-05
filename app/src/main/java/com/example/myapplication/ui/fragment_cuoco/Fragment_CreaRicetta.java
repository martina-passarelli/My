package com.example.myapplication.ui.fragment_cuoco;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.myapplication.R;
import com.example.myapplication.UtilityImage;
import com.example.myapplication.ui.fragment_ricetta.ListaRicette_Fragment;
import com.example.myapplication.ui.fragment_ricetta.Ricetta;
import com.example.myapplication.ui.fragment_utente.Utente;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static androidx.constraintlayout.widget.Constraints.TAG;
import static androidx.core.content.ContextCompat.checkSelfPermission;

/*
    Questa classe rappresenta il framento che consente di creare una ricetta.
    Una ricetta può essere creata solo da un cuoco e non da un utente "normale".
 */
public class Fragment_CreaRicetta extends Fragment {
    private String id_cuoco,categoria;
    private EditText edit_nome, edit_descr, edit_ingr, edit_ricetta;
    private FloatingActionButton fatto;
    private final int SELECT_PICTURE = 100;
    private int rotazioneImg;
    private StorageReference storage;
    private ImageView image_foto;
    private Uri imageUri;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        View view= inflater.inflate(R.layout.frag_crea_ricetta, parent, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        id_cuoco=FirebaseAuth.getInstance().getUid();
        storage= FirebaseStorage.getInstance().getReference();
        edit_nome=(EditText)view.findViewById(R.id.edit_nome);
        edit_descr=(EditText)view.findViewById(R.id.edit_descrizione);
        edit_ingr=(EditText)view.findViewById(R.id.edit_ingredienti);
        edit_ricetta=(EditText)view.findViewById(R.id.edit_ricetta);
        fatto=(FloatingActionButton)view.findViewById(R.id.fatto);

        image_foto=(ImageView)view.findViewById(R.id.foto_ricetta);
        //PRELEVARE LA FOTO DELLA RICETTA DALLA GALLERIA DELLE IMMAGINI
        image_foto.setOnClickListener((view1) -> {
            chooseImage();
        });

        //tramite uno spinner, sono mostrate tutte le possibili categorie dolciarie.
        //ad un dolce creato corrisponde una sola categoria

        List<String> lista=new ArrayList<>();
        lista.add("Torte");
        lista.add("Ciambelloni");
        lista.add("Crostate");
        lista.add("Mousse");
        lista.add("Cheesecake");
        lista.add("Biscotti");
        Spinner sp=(Spinner) view.findViewById(R.id.spinner);
        ArrayAdapter adapter=new ArrayAdapter(this.getContext(),android.R.layout.simple_spinner_dropdown_item,lista);
        sp.setAdapter(adapter);
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView parent, View view, int position, long id)  {
                categoria= (String) sp.getItemAtPosition(position);
                Log.d("SPINNER",""+categoria);
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {   //DEFAULT: categoria torte
                categoria=(String)sp.getItemAtPosition(0);
            }
        });

        //terminata la creazione della ricetta, si provvede a caricare il frammento precedente della
        //lista delle ricette.
        fatto.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String ricetta=edit_ricetta.getText().toString();
                String descr=edit_descr.getText().toString();
                String ingr=edit_ingr.getText().toString();
                String nome=edit_nome.getText().toString();
                if(!ricetta.equals("") && !descr.equals("") && !ingr.equals("") && !nome.equals("")) {
                    //si è scelto di salvare la foto della ricetta nello storage con nome_ricetta+id_cuoco
                    Ricetta r=new Ricetta(nome,ingr,id_cuoco,descr,nome+id_cuoco+".jpg",ricetta,categoria);
                    //rotazioneImg è ottenuto all'atto del caricamento della foto tramite
                    //il tag orientation dell'immagine caricata
                    r.setRot(rotazioneImg);
                    ottieni_nomeCuoco(r,id_cuoco);
                    aggiungi_immagine_storage(nome, id_cuoco);

                }
                //se la ricetta ha tutti i campi vuoti, premendo ok si ritorna al frammento precedente
                // UNA SORTA DI 'CLOSE'.
                else if(ricetta.equals("") && descr.equals("") && ingr.equals("") && nome.equals("")){
                    ricaricaFrammento();
                }
                else{
                    //Se anche solo un campo è vuoto non viene inserita la ricetta
                    Toast.makeText(v.getContext(), "Inserisci tutti i campi oppure svuota tutti i campi!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    /*
     IL METODO ottieni_nomeCuoco(Ricetta r,String id_cuoco) è impiegato per settare il nome del cuoco
     all'interno della ricetta. Solo dopo aver settato questo campo la ricetta viene inserita nel
     firestore.
     */

    public void ottieni_nomeCuoco(Ricetta r,String id_cuoco){
        FirebaseFirestore.getInstance().collection("utenti2").document(""+id_cuoco).get().
                addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                     @Override
                     public void onSuccess(DocumentSnapshot documentSnapshot) {
                         if(documentSnapshot.toObject(Utente.class)!=null) {
                             r.setNome_cuoco(documentSnapshot.getString("nome"));
                             //E' POSSIBILE INSERIRE LA RICETTA NEL DATABASE.
                             aggiungi_inFirestore(r);
                         }
                     }
                });
    }

    /*
    Questo metodo si occupa di inserire la foto della ricetta nello storage.
     */

    private void aggiungi_immagine_storage(String nome, String id_cuoco) {
        try {
            //IMMAGINE SCELTA
            if (imageUri != null) {
                //riferimento allo storage
                StorageReference sRef = storage.child(nome+id_cuoco + "." + "jpg");
                //inserimento dell'immagine
                sRef.putFile(imageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                ricaricaFrammento();
                            }
                        });
            }
        } catch (Exception e) { }
    }

    /*
    ricaricaFrammento() ricarica la vista del frammento genitore con la nuova
         */
    public void ricaricaFrammento(){
        FragmentCuoco frag=(FragmentCuoco)getParentFragment();
        frag.changeVisibility(); //BISOGNA SETTARE LA VISIBILITA' DI UN TASTO PRESENTE NEL FRAMMENTO PADRE.

        Bundle bundle= new Bundle();
        bundle.putString("id",id_cuoco);
        ListaRicette_Fragment ricette_fragment=new ListaRicette_Fragment();
        ricette_fragment.setArguments(bundle);
        ricette_fragment.ottieni_lista(id_cuoco);
        getFragmentManager().beginTransaction().replace(R.id.frame_cuoco,ricette_fragment).commit();
    }


    /*
    questo metodo aggiunge la ricetta al firestore nella collection ricette
     */
    public void aggiungi_inFirestore(Ricetta r){
        FirebaseFirestore.getInstance().collection("ricette").document().set(r);
    }


    //---------------------------------------METODI PER CATTURARE L'IMMAGINE------------------------
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
            Intent i = new Intent(
                    Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, RESULT_LOAD_IMAGE);
        }
    }


    /*
    Questo metodo viene invocato dopo che l'utente ha scelto l'immagine dalla galleria
     */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //se ha scelto un'immagine e non è vuota, si ottiene il suo URI
        if(requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK
                && data != null && data.getData() != null )
            imageUri = data.getData();
        //se l'URI ottenuto non è null
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
                rotazioneImg= rotationInDegrees;
                bitmap = UtilityImage.rotate(bitmap, rotationInDegrees);
                image_foto.setImageBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}

