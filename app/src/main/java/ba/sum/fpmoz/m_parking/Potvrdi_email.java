package ba.sum.fpmoz.m_parking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

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

public class Potvrdi_email extends AppCompatActivity {

    FirebaseAuth auth;
    CardView nastavi_btn;
    private TextInputLayout lozinka_txt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_potvrdi_email);

        auth = FirebaseAuth.getInstance();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        this.nastavi_btn = findViewById(R.id.nastavi_btn);
        this.lozinka_txt = findViewById(R.id.lozinka_txt);

        nastavi_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                provjerilozinku(lozinka_txt);


                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(100);
                final String email = auth.getCurrentUser().getEmail();
                String lozinka = lozinka_txt.getEditText().getText().toString();

                if (email.equals("") || lozinka.equals("")) {
                } else {

                    auth.signInWithEmailAndPassword(email, lozinka).addOnCompleteListener(Potvrdi_email.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                if (user.isEmailVerified()) {
                                    finish();
                                    Toast.makeText(getApplicationContext(), "Uspiješna prijava", Toast.LENGTH_SHORT).show();
                                    Intent i = new Intent(Potvrdi_email.this, Korisnicki_podaci_aktivnost.class);
                                    startActivity(i);
                                } else {
                                    Toast.makeText(Potvrdi_email.this, "Verificirajte e-mail za nastavak", Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                Toast.makeText(getApplicationContext(), "Lozinka nije točna", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }

            }
        });


    }

    private boolean provjerilozinku(TextInputLayout text) {
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
