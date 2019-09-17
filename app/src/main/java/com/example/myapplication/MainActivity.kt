package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

import com.google.android.gms.tasks.Task
import androidx.annotation.NonNull
import androidx.core.view.GravityCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    //image view di nav_header_main.xml
    private lateinit var imageMenu: CircleImageView

    //riferimento all'utente loggato
    private lateinit var mAuth : FirebaseAuth;

    private lateinit var firestore : FirebaseFirestore

    private  lateinit var utente : Utente

    private lateinit var mailMenu : TextView

    private lateinit var nomeMenu: TextView
    private lateinit var storage: StorageReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance().reference
        //per collegare all'image view il metodo del login
        val navigationView = findViewById(R.id.nav_view) as NavigationView
        val hview = navigationView.getHeaderView(0)
        mailMenu = hview.findViewById(R.id.mailMenu) as TextView
        nomeMenu = hview.findViewById(R.id.nomeMenu) as TextView
        imageMenu = hview.findViewById(R.id.imageMenu) as CircleImageView


        if(mAuth.currentUser!=null) {
            val docRef = firestore.collection("utenti").document("" + mAuth.uid)
            docRef.get().addOnSuccessListener { documentSnapshot ->
                utente = documentSnapshot.toObject(Utente::class.java)!!
                if (utente.imageProf != null) {
                try {
                    storage.child(utente.email + ".jpg").getDownloadUrl()
                        .addOnSuccessListener(OnSuccessListener<Uri> { uri ->
                            if (utente.rot)
                                Picasso.with(this@MainActivity).load(uri).rotate(90f).fit().centerCrop().into(
                                    imageMenu
                                )
                            else
                                Picasso.with(this@MainActivity).load(uri).fit().centerCrop().into(
                                    imageMenu
                                )
                        })
                    mailMenu.setText(utente.email)
                    nomeMenu.setText(utente.nome)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            }
        }

        //cliccando sull'image view si apre l'activity login
        imageMenu.setOnClickListener(View.OnClickListener {

            if(mAuth.currentUser!=null){
                val i = Intent(this@MainActivity,UserProfileActivity::class.java)
                startActivity(i)
            }else {
                val i = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(i)
            }

        })


        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow,
                R.id.nav_tools, R.id.nav_share, R.id.nav_send
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    //i tre puntini al lato aprono un menu con logout ed elimina profilo
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.impostazione_menu, menu)

        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId;
        if(id==R.id.esci && mAuth.currentUser!=null) {
            FirebaseAuth.getInstance().signOut();

            val i = Intent(this@MainActivity, MainActivity::class.java)
            startActivity(i)
            Toast.makeText(
                applicationContext,
                "Logout avvenuto con successo",
                Toast.LENGTH_LONG
            ).show()
            return true;
        }
        if(id==R.id.elimina_account && mAuth.currentUser!=null) {
            val user =mAuth.currentUser

            val credential = EmailAuthProvider
                .getCredential(user?.email.toString(),utente.password )

            // Prompt the user to re-provide their sign-in credentials
            user!!.reauthenticate(credential)
                .addOnCompleteListener {
                    user.delete()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {

                            }
                        }
                    val docRef = firestore.collection("utenti").document(""+mAuth.uid)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(
                                applicationContext,
                                "Eliminazione avvenuta",
                                Toast.LENGTH_LONG
                            ).show()  }
                        .addOnFailureListener {  }
                }
            return true
        }

        return super.onOptionsItemSelected(item);
    }


}
