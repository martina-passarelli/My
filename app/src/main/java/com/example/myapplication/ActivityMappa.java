package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.myapplication.ui.fragment_evento.Evento;
import com.example.myapplication.ui.fragment_evento.Fragment_Evento;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.ui.IconGenerator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/*
    Quesat classe rappresenta l'activity che contiene la mappa "intorno a te"
    Sono state usate le API di googleMaps. Per accedere alla posizione corrente si chiede il permesso
    all'utente per poter prelevare i dati dal suo GPS
 */
public class ActivityMappa extends AppCompatActivity implements OnMapReadyCallback {
    SupportMapFragment mapFragment ;
    private GoogleMap mMap;
    private boolean mLocationPermissionsGranted=false;
    //zoom della mappa
    private static final float DEFAULT_ZOOM = 15f;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;

    private static final String COURSE_LOCATION= Manifest.permission.ACCESS_COARSE_LOCATION;
    private static  final int LOCATION_PERMISSION_REQUEST_CODE=1234;
    //riferimento ad database
    private FirebaseFirestore mDatabase;
    //***per ricaricare la mappa
    private Button reloadMap;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_mappa);
        mDatabase = FirebaseFirestore.getInstance();
        reloadMap = findViewById(R.id.reloadMap);
        reloadMap.setEnabled(false);
        reloadMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ActivityMappa.this, ActivityMappa.class));
            }
        });

        //Per le API
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), String.valueOf(R.string.google_maps_API_key));
        }
        PlacesClient placesClient = Places.createClient(this);
        mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);

        getLocationPermission();
    }

    public void isGPSEnable(){
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

    }


    /*
        Questo metodo chiede il permesso di accedere alla posizione se non è stato ancora concesso
        oppure accede direttamente ai dati del GPS se quest'ultimo è acceso.
    */
    private void getLocationPermission(){
        //se il gps è acceso
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        //enabled è true se il GPS è acceso, false altrimenti
        boolean enabled = service
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        //se il GPS è acceso
        if(enabled) {
            String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION};
            //controlla che il permesso sia stato dato
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                        COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionsGranted = true;
                    //init mappa
                    getDeviceLocation();
                    mapFragment.getMapAsync(ActivityMappa.this);
                } else {
                    ActivityCompat.requestPermissions(this, permission, LOCATION_PERMISSION_REQUEST_CODE);
                }

            } else {
                ActivityCompat.requestPermissions(this, permission, LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
        //se il GPS è spento si rende visibile il tasto di reload. Dopo che l'utente ha acceso il GPS può infatti
        //cliccare il tasto per ricaricare la sua posizione
        else {
            reloadMap.setEnabled(true);
            reloadMap.setVisibility(View.VISIBLE);

            Toast.makeText(this,"Accendi il GPS per usufruire del servizio!",Toast.LENGTH_LONG).show();

        }
    }

    /*
        Metodo invocato col caricamento della mappa
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Toast.makeText(this,"La mappa Ã¨ pronta",Toast.LENGTH_SHORT).show();
        mMap=googleMap;
        //se il permesso è stato dato
        if(mLocationPermissionsGranted){
            getDeviceLocation();
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            //popolamento della mappa
            aggiungiEventiMappa();
            aggiungiListener();


        }
    }

    /*
        Tramite questo metodo si ottiene la posizione dell'utente
     */
    private void getDeviceLocation(){
        mFusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);
        try {
            if(mLocationPermissionsGranted){
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            try {
                                Location currentLocation= (Location) task.getResult();
                                //aggiunta di un marker con una label per indicare la posizione dell'utente
                                mMap.addMarker(new MarkerOptions().position(new LatLng(currentLocation.getLatitude(),
                                        currentLocation.getLongitude())).title("Tu sei qui")).showInfoWindow();

                                moveCamera(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()),DEFAULT_ZOOM);
                            }catch (Exception e){
                                Location currentLocation= (Location) task.getResult();
                                mMap.addMarker(new MarkerOptions().position(new LatLng(currentLocation.getLatitude(),
                                        currentLocation.getLongitude())).title("Tu sei qui")).showInfoWindow();
                                moveCamera(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()),DEFAULT_ZOOM);
                            }

                        }else{
                            Toast.makeText(ActivityMappa.this, "impossibile accedere alla posizione", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        }catch (SecurityException e){
        }
    }


    /*
        Questo metodo muove la visuale sul punto selezionato
     */
    private void moveCamera(LatLng latLng, float zoom){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMap != null) {
            mMap.clear();
        }
    }

    /*
        Questo metodo aggiunge gli eventi del db alla mappa prestando attenzione a quelli scaduti che
        non vengono caricati
     */
    private void aggiungiEventiMappa(){

        CollectionReference eventi = mDatabase.collection("eventi");
        eventi.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot d : list) {
                        Evento e= d.toObject(Evento.class);
                        //se l'evento è scaduto non inseriamo il marker
                        if(nonScaduto(e.getData(), e.getOra())) {
                            IconGenerator iconGen = new IconGenerator(ActivityMappa.this);
                            Marker m = mMap.addMarker(new MarkerOptions().position(new LatLng(e.getLatitudine(),
                                    e.getLongitudine())).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                                    .icon(BitmapDescriptorFactory.fromBitmap(iconGen.makeIcon(e.getNome()))).
                                            anchor(iconGen.getAnchorU(), iconGen.getAnchorV()).title(e.getNome()));
                            m.setTag(e);
                        }
                    }
                }
            }
        });
    }





    public static boolean nonScaduto(String dataEvento, String ora){
        Date dataOggi = new Date();
        int giorno =dataOggi.getDate();
        int mese = dataOggi.getMonth()+1;
        int anno = dataOggi.getYear()+1900;
        String [] data = dataEvento.split("/",5);
        int giornoEvento = Integer.parseInt(data[0]);
        int meseEvento=Integer.parseInt(data[1]);
        int annoEvento = Integer.parseInt(data[2]);
        String [] oraEvento= ora.split(":",5);
        int oraE = Integer.parseInt(oraEvento[0]);
        int minE = Integer.parseInt(oraEvento[1]);
        //per l'ora corrente
        Calendar cal = Calendar.getInstance();
        Date date=cal.getTime();
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        String formattedDate=dateFormat.format(date);
        String [] oraCorrente= formattedDate.split(":",5);
        int oraCorr = Integer.parseInt(oraCorrente[0]);
        int minCorr = Integer.parseInt(oraCorrente[1]);
        //se le date sono uguali allora controllo l'ora
        if(mese==meseEvento && anno==annoEvento && giorno == giornoEvento){
            if(oraE>oraCorr) return true;
            else if (oraE<oraCorr) return false;
            if(minE > minCorr) return true;
            else if (minE < minCorr) return false;
        }
        if(anno< annoEvento){
            return true;
        }else if (anno>annoEvento){
            return false;
        }else{
            if(mese> meseEvento){
                return false;
            }else if(mese< meseEvento){
                return true;
            }else{
                return giorno< giornoEvento;
            }
        }
    }

    /*
        Questo metodo aggiunge ai marker i listener in modo che cliccando su una bandierina si possa
        andare sulla descrizione dell'evento
     */

    private void aggiungiListener() {
        System.out.println("mappa="+mMap);
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (!marker.getTitle().equals("Tu sei qui")) {
                    Evento e = (Evento) marker.getTag();
                    Bundle bundle = new Bundle();
                    bundle.putString("id_evento", e.getId());
                    bundle.putString("id_cuoco", e.getId_cuoco());
                    bundle.putDouble("longitudine", e.getLongitudine());
                    bundle.putDouble("latitudine", e.getLatitudine());
                    Fragment_Evento fev = new Fragment_Evento();

                    fev.setArguments(bundle);

                    setContentView(R.layout.fragment_tools);
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.add(R.id.sost, fev);
                    //fragmentTransaction.hide(getSupportFragmentManager().findFragmentById(R.id.map));
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();


                    return false;
                }
                return true;
            }
        });
    }

    @Override
    public void onBackPressed(){
        Intent i = new Intent(this,MainActivity.class);
        startActivity(i);
    }
    /*
        Questo metodo viene richiamato dopo che l'utente ha deciso se consentire o meno
        l'accesso al GPS
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        mLocationPermissionsGranted=false;
        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionsGranted = true;
                    //inizializza la mappa
                    getDeviceLocation();
                    mapFragment.getMapAsync( ActivityMappa.this);
                }
            }
        }
    }




}
