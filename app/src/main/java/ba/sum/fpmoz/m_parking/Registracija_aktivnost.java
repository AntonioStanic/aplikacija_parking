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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class Registracija_aktivnost extends AppCompatActivity {

    private TextInputLayout email_txt;
    private TextInputLayout lozinka_txt;
    private TextInputLayout lozinka2_txt;
    CardView registrirajse_btn;

    FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registracija_aktivnost);

        getSupportActionBar().hide();

        auth = FirebaseAuth.getInstance();


        this.email_txt = findViewById(R.id.email_txt);
        this.lozinka_txt = findViewById(R.id.lozinka_txt);
        this.lozinka2_txt = findViewById(R.id.lozinka2_txt);
        this.registrirajse_btn = findViewById(R.id.registrirajse_btn);

        this.registrirajse_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(100);

                provjerimaililozinku(email_txt);
                provjerimaililozinku(lozinka_txt);
                provjerimaililozinku(lozinka2_txt);


                String email = email_txt.getEditText().getText().toString();
                String lozinka = lozinka_txt.getEditText().getText().toString();
                String lozinka2 = lozinka2_txt.getEditText().getText().toString();

                if (email.equals("") || lozinka.equals("") || lozinka2.equals("")) {
                } else {

                    if (lozinka2.equals(lozinka)) {

                        lozinka2_txt.setError("");

                        auth.createUserWithEmailAndPassword(email, lozinka)
                                .addOnCompleteListener(Registracija_aktivnost.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            posalji_verifikacijski_emai();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "E-mail adresa se koristi ili je neispravna", Toast.LENGTH_LONG).show();
                                            auth.signOut();
                                            Intent i = new Intent(Registracija_aktivnost.this, Prijava_aktivnost.class);
                                            startActivity(i);
                                        }

                                    }
                                });
                    } else {
                        lozinka2_txt.setError("");
                        lozinka2_txt.setError("Lozinke se ne podudaraju");
                    }
                }

            }
        });


    }

    private void posalji_verifikacijski_emai() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Verifikacijski e-mail je poslan", Toast.LENGTH_LONG).show();
                            //proslijedi na aktivnost da se potvrdi mail i nastsvi dalje...
                            Intent i = new Intent(Registracija_aktivnost.this, Potvrdi_email.class);
                            startActivity(i);
                        } else {
                            Toast.makeText(getApplicationContext(), "email nije poslan", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    private boolean provjerimaililozinku(TextInputLayout text) {
        String provjeri = text.getEditText().getText().toString().trim();
        if (provjeri.isEmpty()) {
            text.setError("");
            text.setError("Oops, čini se da nešto nedostaje");
            return false;
        } else {
            text.setError(null);
            return true;
        }
    }


}
