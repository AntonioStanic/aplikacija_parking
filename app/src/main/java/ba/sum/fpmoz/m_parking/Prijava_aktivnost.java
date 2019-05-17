package ba.sum.fpmoz.m_parking;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import ba.sum.fpmoz.m_parking.model.osoba;
import ba.sum.fpmoz.m_parking.model.placeno;


public class Prijava_aktivnost extends AppCompatActivity {

    public TextInputLayout e_mail_txt;
    public TextInputLayout lozinka_txt;
    CardView prijavi_se_btn;
    CardView registriraj_se_btn;
    public DatabaseReference databaseReference;
    public String provjera_da_li_postoji;

    FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prijava_aktivnost);

        auth = FirebaseAuth.getInstance();


        if (auth.getCurrentUser() != null) {
            //povuc iz baze usera i ako postoji ovaj email onda da se odma
            //prijavi a ako ne postoji onda ide standardna provjera na prijava_aktivnosti
            databaseReference = FirebaseDatabase.getInstance().getReference("osoba");
            String trenutni_korisnik = auth.getCurrentUser().getEmail();
            final Query query = databaseReference.orderByChild("email").equalTo(trenutni_korisnik);
            query.addValueEventListener(new ValueEventListener() {
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        osoba models = data.getValue(osoba.class);
                        provjera_da_li_postoji = models.getEmail();
                        if (auth.getCurrentUser().getEmail().equals(provjera_da_li_postoji)) {
                            Intent i = new Intent(Prijava_aktivnost.this, Pocetna_aktivnost.class);
                            startActivity(i);
                            finish();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }


        this.e_mail_txt = findViewById(R.id.provjeri_tablice_txt);
        this.lozinka_txt = findViewById(R.id.lozinka_txt);
        this.prijavi_se_btn = findViewById(R.id.nastavi_btn);
        this.registriraj_se_btn = findViewById(R.id.registriraj_se_btn);


        this.registriraj_se_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Prijava_aktivnost.this, Registracija_aktivnost.class);
                startActivity(i);
            }
        });


        this.prijavi_se_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(100);

                provjerimaililozinku(e_mail_txt);
                provjerimaililozinku(lozinka_txt);

                final String email = e_mail_txt.getEditText().getText().toString();
                String lozinka = lozinka_txt.getEditText().getText().toString();

                if (email.equals("") || lozinka.equals("")) {
                } else {

                    auth.signInWithEmailAndPassword(email, lozinka).addOnCompleteListener(Prijava_aktivnost.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                if (user.isEmailVerified()) {
                                    databaseReference = FirebaseDatabase.getInstance().getReference("osoba");
                                    final Query query = databaseReference.orderByChild("email").equalTo(email);
                                    query.addValueEventListener(new ValueEventListener() {
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                                osoba models = data.getValue(osoba.class);
                                                provjera_da_li_postoji = models.getEmail();
                                                if (email.equals(provjera_da_li_postoji)) {
                                                    Toast.makeText(getApplicationContext(), "Uspiješna prijava", Toast.LENGTH_SHORT).show();
                                                    Intent i = new Intent(Prijava_aktivnost.this, Pocetna_aktivnost.class);
                                                    startActivity(i);
                                                }
                                            }

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                        }
                                    });
                                    Intent i = new Intent(Prijava_aktivnost.this, Korisnicki_podaci_aktivnost.class);
                                    startActivity(i);

                                } else {
                                    Toast.makeText(Prijava_aktivnost.this, "Verificirajte e-mail za nastavak", Toast.LENGTH_SHORT).show();
                                    Intent i = new Intent(Prijava_aktivnost.this, Potvrdi_email.class);
                                    startActivity(i);
                                }

                            } else {
                                Toast.makeText(getApplicationContext(), "Korisnički podaci su neispravni", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }
            }
        });
    }


    private boolean provjerimaililozinku(TextInputLayout text) {
        String provjeri = text.getEditText().getText().toString().trim();
        if (provjeri.isEmpty()) {
            text.setError("Oops, čini se da nešto nedostaje");
            return false;
        } else {
            text.setError(null);
            return true;
        }
    }


}

