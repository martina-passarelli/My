package com.example.myapplication.ui.fragment_partecipanti;
import android.accounts.Account;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.ui.fragment_evento.Evento;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import android.os.AsyncTask;
import android.widget.Toast;

public class Fragment_ListaPartecipanti extends Fragment {
    private ArrayList<String> list=new ArrayList<>();
    private RecyclerView recyclerView;
    private View myView;
    private FirebaseFirestore ff= FirebaseFirestore.getInstance();
    private Adapter_Partecipanti tutorAdapter;

    private TextView label_part;
    private Button iscriviti;
    private String id_evento;
    private String utente_corrente=FirebaseAuth.getInstance().getUid();

    private GoogleSignInClient mGoogleSignInClient;
    private Account mAccount;

    private String KEY_ACCOUNT="861844783919-jasqne4771rcfkau3hrbh0r9jrelbpra.apps.googleusercontent.com";
    private static final String CALENDAR_SCOPE ="https://www.googleapis.com/auth/calendar.events";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tutorAdapter = new Adapter_Partecipanti(list);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        myView = inflater.inflate(R.layout.fragment_lista_partecipanti, container, false);
        recyclerView = myView.findViewById(R.id.lista_partecipanti);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.startLayoutAnimation();
        recyclerView.setItemAnimator(new SlideInUpAnimator());
        recyclerView.startLayoutAnimation();
        recyclerView.setAdapter(tutorAdapter);
        return myView;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        if(savedInstanceState!=null) {
            mAccount = savedInstanceState.getParcelable(KEY_ACCOUNT);
        }
        Bundle bundle=this.getArguments();
        id_evento=bundle.getString("id");
        label_part=(TextView) view.findViewById(R.id.text_part);
        iscriviti=(Button) view.findViewById(R.id.button_iscrizione);

        iscriviti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                set_button(); // SET A SECONDA SE SEI GIA' ISCRITTO O NO ALL'EVENTO
            }
        });


        // Configure sign-in to request the user's ID, email address, basic profile,
        // and readonly access to contacts.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(CALENDAR_SCOPE))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this.getActivity(), gso);

    }


    public void set_button(){
        ff.collection("eventi").document(""+id_evento).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Evento e= documentSnapshot.toObject(Evento.class);
                int num=e.getMax_partecipanti();
                ArrayList<String> lista_p=new ArrayList<>();

                if(e.getLista_part()!=null) lista_p=(ArrayList<String>) e.getLista_part();

                if(!lista_p.contains(utente_corrente) && lista_p.size()<num) {
                    add_partecipante(lista_p,num);
                    iscriviti.setText("Esci");
               }
                else {
                    remove_partecipante(lista_p,num);
                    iscriviti.setText("Iscriviti");
                }
            }
        });
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  public void inserisci_inCalendar(Evento evento){
      GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this.getActivity());
      Date formatter = null;
      try {
          formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm").parse(evento.getData() + " " + evento.getOra());
      } catch (ParseException e) {
          e.printStackTrace();
      }
      DateTime dataTime = new DateTime(formatter);
      DateTime dataTimeEnd = new DateTime(formatter);

      if (GoogleSignIn.hasPermissions(account, new Scope(CALENDAR_SCOPE))) {
          try {
              insertEvent(evento.getNome(), evento.getLuogo(), evento.getDescrizione(), dataTime, dataTimeEnd, account);
          } catch (IOException e) {
              e.printStackTrace();
          } catch (GeneralSecurityException e) {
              e.printStackTrace();
          }
      }else{
          Toast.makeText(getActivity(), "Ops! Permesso negato!", Toast.LENGTH_SHORT).show();

      }
  }



    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_ACCOUNT, mAccount);
    }

    private static final int RC_SIGN_IN = 9001;

    private Calendar service;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();




    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                // Store the account from the result
                mAccount = account.getAccount();
            } catch (ApiException e) {
                // Clear the local account
                mAccount = null;
            }
        }

    }



    public void doSomething(String id_evento){
        ff.collection("eventi").document(""+id_evento).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Evento evento=documentSnapshot.toObject(Evento.class);
                ArrayList<String> lista_p= new ArrayList<>();
                if(evento.getLista_part()!=null)
                    lista_p=(ArrayList<String>) evento.getLista_part();
                int num=evento.getMax_partecipanti();
                label_part.setText(lista_p.size()+"/"+num);
                list.addAll(lista_p);
                tutorAdapter.notifyDataSetChanged();

                //SETTIAMO IL BOTTONE DI ISCRIZIONE
                if (list.contains(utente_corrente))
                    iscriviti.setText("Esci");
                else if(list.size()==num){
                    iscriviti.setClickable(false);
                    iscriviti.setBackgroundColor(R.color.common_google_signin_btn_text_light_disabled);
                }
            }
        });
    }


    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    public void remove_partecipante(ArrayList<String> list_p, int num){
        list.clear();
        list.addAll(list_p);
        list.remove(utente_corrente);
        recyclerView.scrollToPosition(tutorAdapter.getItemCount());
        tutorAdapter.notifyDataSetChanged();
        label_part.setText(list.size()+"/"+num);

        ff.collection("eventi").document(""+id_evento).update("lista_part", list).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        });
        modifica_inUtente(false);
    }



    public void add_partecipante(ArrayList<String> list_p, int num){
        list.clear();
        list.addAll(list_p);
        list.add(list.size(),utente_corrente);
        tutorAdapter.notifyItemInserted(list.size()-1);
        recyclerView.scrollToPosition(tutorAdapter.getItemCount()-1);
        label_part.setText(list.size()+"/"+num);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this.getActivity());
        ff.collection("eventi").document(""+id_evento).update("lista_part", list).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                ff.collection("eventi").document(""+id_evento).get().addOnSuccessListener(
                        (new OnSuccessListener<DocumentSnapshot>() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @SuppressLint("ResourceAsColor")
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot != null){
                                    if (!GoogleSignIn.hasPermissions(account, new Scope(CALENDAR_SCOPE)))
                                        signIn();
                                    showDialogCalendar(documentSnapshot.toObject(Evento.class));
                                    }
                            }}
                            ));

            }
        });
        modifica_inUtente(true);

    }


    public void modifica_inUtente(boolean aggiungi){
        DocumentReference doc= ff.collection("utenti2").document(""+utente_corrente);
            doc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if(aggiungi)doc.update("lista_eventi", FieldValue.arrayUnion(id_evento));
                else doc.update("lista_eventi", FieldValue.arrayRemove(id_evento));

            }
        });
    }

    private Context mContext;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }



    //------------------------------------UTILI?----------------------------------------------------

    private  Event event;
    public void insertEvent(String summary, String location, String des, DateTime startDate, DateTime endDate, GoogleSignInAccount account)throws IOException, GeneralSecurityException {

        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(this.getActivity().getApplicationContext(),
                Collections.singleton(CALENDAR_SCOPE)).setBackOff(new ExponentialBackOff());
        credential.setSelectedAccount(account.getAccount());

        service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY,credential)
                .setApplicationName("REST API sample")
                .build();

        event = new Event().setSummary(summary).setLocation(location).setDescription(des);
        EventDateTime start = new EventDateTime().setDateTime(startDate).setTimeZone("Europe/London");
        event.setStart(start);

        EventDateTime end = new EventDateTime().setDateTime(endDate).setTimeZone("Europe/London");
        event.setEnd(end);

        String[] recurrence = new String[] {"RRULE:FREQ=DAILY;COUNT=1"};
        event.setRecurrence(Arrays.asList());
        event.setAttendees(Arrays.asList());
    /*  EventReminder[] reminderOverrides = new EventReminder[] {
            new EventReminder().setMethod("email").setMinutes(24 * 60),
            new EventReminder().setMethod("popup").setMinutes(10),
    };*/
    Event.Reminders reminders = new Event.Reminders()
            .setUseDefault(false)
            .setOverrides(Arrays.asList());
    event.setReminders(reminders);
    task.execute();

    }

    private void showDialogCalendar(Evento evento) {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle("");
        alertDialog.setMessage("Vuoi inserire l'evento nel tuo calendario?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "SI", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                inserisci_inCalendar(evento);
                Toast.makeText(getActivity(), "Evento inserito con successo", Toast.LENGTH_SHORT).show();

            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
            }
        });
        alertDialog.show();
    }

    AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
        @Override
        protected String doInBackground(Void... params) {
            String calendarId = "primary";
            try {
                service.events().insert(calendarId, event).setSendNotifications(true).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "ok";
        }

        @Override
        protected void onPostExecute(String token) {
      //      Log.i(TAG, "Access token retrieved:" + token);
        }

    };



}
