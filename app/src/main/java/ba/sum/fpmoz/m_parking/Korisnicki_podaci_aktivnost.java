package ba.sum.fpmoz.m_parking;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import ba.sum.fpmoz.m_parking.model.osoba;

public class Korisnicki_podaci_aktivnost extends AppCompatActivity {

    private ImageButton imageButton;
    private TextInputLayout ime_txt;
    private TextInputLayout prezime_txt;
    private TextInputLayout datum_rodjenja_txt;
    private TextInputLayout broj_kartice_txt;
    CardView spremi_osobu;
    private ProgressBar progressBar;
    private DatabaseReference databaseReference;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri = null; //sluzi za prikazivanje slike u image view i da mozemo onda prenijeti u storage na firebase
    private StorageReference storageReference;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_korisnicki_podaci_aktivnost);

        auth = FirebaseAuth.getInstance();

        this.ime_txt = findViewById(R.id.ime_txt);
        this.prezime_txt = findViewById(R.id.prezime_txt);
        this.datum_rodjenja_txt = findViewById(R.id.datum_rodjenja_txt);
        this.broj_kartice_txt = findViewById(R.id.broj_kartice_txt);
        this.spremi_osobu = findViewById(R.id.spremi_osobu);
        this.imageButton = findViewById(R.id.odaberi_sliku);
        this.progressBar = findViewById(R.id.progress_bar);

        this.databaseReference = FirebaseDatabase.getInstance().getReference("osoba");
        //Traži se skruktura imena osoba i u nju ćemo dodavati novog korisnika.
        this.storageReference = FirebaseStorage.getInstance().getReference("slike_osoba");
        //mapa u koju ćemo spremiti slike koje korisnici odaberu i koje će se prikazivati u lijevom izborniku.


        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });


        this.spremi_osobu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(100);
                provjeripraznapolja(ime_txt);
                provjeripraznapolja(prezime_txt);
                provjeripraznapolja(datum_rodjenja_txt);
                provjeripraznapolja(broj_kartice_txt);

                dodaj_osobu();

            }
        });

    }

    private boolean provjeripraznapolja(TextInputLayout text) {
        String provjeri = text.getEditText().getText().toString().trim();

        if (provjeri.isEmpty()) {
            text.setError("Oops, čini se da nešto nedostaje");
            return false;
        } else {
            text.setError(null);
            return true;
        }
    }

    private String dohvatiEkstenziju(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {//ako je korisnik uzeo sliku i ispunio sva polja onda izvrsavamo naredbe
            imageUri = data.getData();
            imageButton.setImageURI(imageUri);
        }
    }

    private void dodaj_osobu() {
        final String email = auth.getCurrentUser().getEmail();
        if (imageUri != null){
            if(ime_txt.getEditText().getText().toString().equals("") ||
                prezime_txt.getEditText().getText().toString().equals("") ||
                datum_rodjenja_txt.getEditText().getText().toString().equals("") ||
                broj_kartice_txt.getEditText().getText().toString().equals("")) {}else{
                StorageReference fileReference = storageReference.child(System.currentTimeMillis()//currentTimeMillis sluzi da nam nazivi slika u bazi budu jedinstveni
                        + "." + dohvatiEkstenziju(imageUri));
                fileReference.putFile(imageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressBar.setProgress(0);
                                    }
                                }, 500);
                                Toast.makeText(Korisnicki_podaci_aktivnost.this, "Prijenos uspješan!", Toast.LENGTH_SHORT).show();
                                osoba osoba = new osoba(
                                        ime_txt.getEditText().getText().toString().trim(),
                                        prezime_txt.getEditText().getText().toString().trim(),
                                        datum_rodjenja_txt.getEditText().getText().toString().trim(),
                                        taskSnapshot.getDownloadUrl().toString(),
                                        email,
                                        broj_kartice_txt.getEditText().getText().toString().trim());
                                String osobaId = databaseReference.push().getKey();
                                databaseReference.child(osobaId).setValue(osoba);

                                imageUri = null;
                                ime_txt.getEditText().setText("");
                                prezime_txt.getEditText().setText("");
                                datum_rodjenja_txt.getEditText().setText("");
                                broj_kartice_txt.getEditText().setText("");
                                imageButton.setImageResource(R.mipmap.ic_add);

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Korisnicki_podaci_aktivnost.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                progressBar.setProgress((int) progress);
                            }
                        });
                Intent i = new Intent(Korisnicki_podaci_aktivnost.this, Pocetna_aktivnost.class);
                startActivity(i);
            }

        } else {
            Toast.makeText(getApplicationContext(), "Oops, čini se da nešto nedostaje", Toast.LENGTH_SHORT).show();
        }
    }


}
