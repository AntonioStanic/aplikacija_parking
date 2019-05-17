package ba.sum.fpmoz.m_parking;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import ba.sum.fpmoz.m_parking.model.osoba;
import ba.sum.fpmoz.m_parking.model.placeno;


public class Pocetna_aktivnost extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseAuth auth;
    public String trenutni_datum_iz_baze;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                //dohvatiti sliku, ime,prezime i email korisnika

                DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference("osoba");
                final Query query = mDatabaseRef.orderByChild("email").equalTo(auth.getCurrentUser().getEmail());

                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot data : dataSnapshot.getChildren()) {


                            osoba models = data.getValue(osoba.class);
                            TextView ime_i_prezime_korisnika = findViewById(R.id.ime_prezime_korisnika);
                            ime_i_prezime_korisnika.setText(models.getIme() + " " + models.getPrezime());

                            TextView email_korisnika_header = findViewById(R.id.email_korisnika_header);
                            email_korisnika_header.setText(models.getEmail());

                            ImageView slika_korisnika_image = findViewById(R.id.slika_korisnika);
                            Picasso.get().load(models.getSlika()).into(slika_korisnika_image);


                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });


            }
        };

        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        getSupportFragmentManager().beginTransaction().replace(R.id.app_bar_main, new Pocetni_fragment()).commit();
        navigationView.setCheckedItem(R.id.pocetna);


        //trenutno brisanje iz firebase
        //brisanje na osnovi provjere datuma
        DatabaseReference mmDatabaseRef = FirebaseDatabase.getInstance().getReference("placeno");
        Query za_datum = mmDatabaseRef.orderByChild("trenutni_datum");
        trenutni_datum = DateFormat.getDateInstance().format(new Date());
        za_datum.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    placeno placeno = itemSnapshot.getValue(placeno.class);
                    trenutni_datum_iz_baze = placeno.getTrenutni_datum();
                    if(trenutni_datum.equals(trenutni_datum_iz_baze)){}
                    else{itemSnapshot.getRef().removeValue();}
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        //brisanje na osnovu proracuna vremena
        long trenutno_vrijeme_u_milis = System.currentTimeMillis() + (2 * 60 * 60 * 1000);
        long s = (trenutno_vrijeme_u_milis / 1000) % 60;
        long m = (trenutno_vrijeme_u_milis / 60000) % 60;
        long h = (trenutno_vrijeme_u_milis / (3600000)) % 24;
        String s_string = String.valueOf(s);
        String m_string = String.valueOf(m);
        String h_string = String.valueOf(h);
        String trenutno_vrijeme = h_string + ":" + m_string + ":" + s_string;
        Query proslo_vrijeme = mmDatabaseRef.orderByChild("vrijedi_do").endAt(trenutno_vrijeme);

        proslo_vrijeme.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    itemSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw databaseError.toException();
            }
        });
        //zavrseno brisanje
    }

    public String trenutni_datum;
    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Molimo kliknite 2x natrag za izlazak", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.odjavi_se) {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(100);
            Intent i = new Intent(Pocetna_aktivnost.this, Prijava_aktivnost.class);
            finish();
            FirebaseAuth.getInstance().signOut();
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }


    public Vibrator vibrator;
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
         vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        switch (item.getItemId()) {
            case R.id.pocetna:
                vibrator.vibrate(100);
                getSupportFragmentManager().beginTransaction().replace(R.id.app_bar_main, new Pocetni_fragment()).commit();
                break;
            case R.id.slikaj_i_plati:
                vibrator.vibrate(100);
                getSupportFragmentManager().beginTransaction().replace(R.id.app_bar_main, new Slikaj_i_plati_fragment()).commit();
                break;
            case R.id.slikaj_i_provjeri:
                vibrator.vibrate(100);
                getSupportFragmentManager().beginTransaction().replace(R.id.app_bar_main, new Slikaj_i_provjeri_fragment()).commit();
                break;
            case R.id.galerija:
                vibrator.vibrate(100);
                getSupportFragmentManager().beginTransaction().replace(R.id.app_bar_main, new Galerija_fragment()).commit();
                break;
            case R.id.napisi_i_plati:
                vibrator.vibrate(100);
                getSupportFragmentManager().beginTransaction().replace(R.id.app_bar_main, new Napisi_i_plati_fragment()).commit();
                break;
            case R.id.napisi_i_provjeri:
                vibrator.vibrate(100);
                getSupportFragmentManager().beginTransaction().replace(R.id.app_bar_main, new Napisi_i_provjeri_fragment()).commit();
                break;
            case R.id.postavke:
                vibrator.vibrate(100);
                getSupportFragmentManager().beginTransaction().replace(R.id.app_bar_main, new Postavke_fragment()).commit();
                break;
            case R.id.podijeli:
                vibrator.vibrate(100);
                getSupportFragmentManager().beginTransaction().replace(R.id.app_bar_main, new Podijeli_fragment()).commit();
                break;
            case R.id.o_aplikaciji:
                vibrator.vibrate(100);
                getSupportFragmentManager().beginTransaction().replace(R.id.app_bar_main, new O_aplikaciji_fragment()).commit();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
