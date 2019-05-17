package ba.sum.fpmoz.m_parking;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import ba.sum.fpmoz.m_parking.model.osoba;

import static android.app.Activity.RESULT_OK;

public class Postavke_fragment extends Fragment {

    private CardView resetiraj_lozinku_btn;
    private CardView obrisi_korisnika_btn;
    private CardView spremi_promjene_btn;
    public TextInputLayout ime;
    public TextInputLayout prezime;
    public TextInputLayout datum_rodjenja;
    public TextInputLayout broj_kartice;
    FirebaseAuth auth;
    public String trenutni_email;
    public String trenutni_email_iz_baze;
    private ProgressBar progressBar;
    private DatabaseReference databaseReference;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri = null; //sluzi za prikazivanje slike u image view i da mozemo onda prenijeti u storage na firebase
    private StorageReference storageReference;
    private ImageButton imageButton;
    public String stara_slika;
    public String slika;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.postavke_fragment, container, false);

        auth = FirebaseAuth.getInstance();

        resetiraj_lozinku_btn = view.findViewById(R.id.resetiraj_lozinku_btn);
        obrisi_korisnika_btn = view.findViewById(R.id.obrisi_korisnika_btn);
        spremi_promjene_btn = view.findViewById(R.id.spremi_promjene_btn);
        ime = view.findViewById(R.id.ime_txt2);
        prezime = view.findViewById(R.id.prezime_txt2);
        datum_rodjenja = view.findViewById(R.id.datum_rodjenja_txt2);
        broj_kartice = view.findViewById(R.id.broj_kartice_txt2);
        imageButton = view.findViewById(R.id.slika_img2);
        progressBar = view.findViewById(R.id.progress_bar2);


        //dohvatiti korisnikove podatke
        DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference("osoba");
        final Query query = mDatabaseRef.orderByChild("email").equalTo(auth.getCurrentUser().getEmail());

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot data : dataSnapshot.getChildren()) {

                    osoba models = data.getValue(osoba.class);

                    TextInputEditText ime_txt = view.findViewById(R.id.postavi_ime_txt);
                    ime_txt.setText(models.getIme());

                    TextInputEditText prezime_txt = view.findViewById(R.id.postavi_prezime_txt);
                    prezime_txt.setText(models.getPrezime());

                    TextInputEditText datum_rodjenja_txt = view.findViewById(R.id.postavi_datum_rodjenja_txt);
                    datum_rodjenja_txt.setText(models.getDatumrodjenja());

                    TextInputEditText broj_kartice_txt = view.findViewById(R.id.postavi_broj_kartice_txt);
                    broj_kartice_txt.setText(models.getBrojkartice());

                    ImageView slika_img = view.findViewById(R.id.slika_img2);
                    Picasso.get().load(models.getSlika()).into(slika_img);

                    stara_slika = models.getSlika();

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        resetiraj_lozinku_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(100);
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Želite li potvrditi slanje e-mail poruke putem koje ćete ponovno postaviti lozinku?");
                builder.setCancelable(true);
                builder.setNegativeButton("Odustani", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.setPositiveButton("Potvrdi", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        auth.sendPasswordResetEmail(auth.getCurrentUser().getEmail())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getContext(), "E-mail poslan", Toast.LENGTH_SHORT).show();
                                            Intent i = new Intent(getContext(), Prijava_aktivnost.class);
                                            auth.signOut();
                                            startActivity(i);
                                        } else {
                                            Toast.makeText(getContext(), "Greška prilikom slanja", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });


                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
                Button nbutton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                nbutton.setTextColor(Color.RED);
                Button pbutton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                pbutton.setTextColor(Color.RED);
            }
        });

        obrisi_korisnika_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(100);
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Zaista želite obrisati račun?");
                builder.setCancelable(true);
                builder.setNegativeButton("Odustani", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.setPositiveButton("Obriši", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        DatabaseReference mmDatabaseRef = FirebaseDatabase.getInstance().getReference("osoba");
                        Query email = mmDatabaseRef.orderByChild("email");
                        trenutni_email = auth.getCurrentUser().getEmail();
                        email.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                                    osoba model = itemSnapshot.getValue(osoba.class);
                                    trenutni_email_iz_baze = model.getEmail();
                                    if (trenutni_email.equals(trenutni_email_iz_baze)) {
                                        itemSnapshot.getRef().removeValue();
                                    } else {
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                        auth.getCurrentUser().delete();
                        auth.signOut();

                        Intent i = new Intent(getContext(), Prijava_aktivnost.class);
                        startActivity(i);


                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
                Button nbutton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                nbutton.setTextColor(Color.RED);
                Button pbutton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                pbutton.setTextColor(Color.RED);
            }
        });
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

        spremi_promjene_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(100);

                provjeripodatke(ime);
                provjeripodatke(prezime);
                provjeripodatke(datum_rodjenja);
                provjeripodatke(broj_kartice);


                dodaj_osobu();


            }
        });

        return view;
    }

    private boolean provjeripodatke(TextInputLayout text) {
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
        ContentResolver contentResolver = getActivity().getContentResolver();
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {//ako je korisnik uzeo sliku i ispunio sva polja onda izvrsavamo naredbe
            imageUri = data.getData();
            imageButton.setImageURI(imageUri);
        }
    }

    public void obrisi_postojeceg_korisnika() {
        //pronadji tog korisnika i obriši ga
        DatabaseReference mmDatabaseRef = FirebaseDatabase.getInstance().getReference("osoba");
        Query email2 = mmDatabaseRef.orderByChild("email");
        trenutni_email = auth.getCurrentUser().getEmail();
        email2.addListenerForSingleValueEvent(new ValueEventListener() {

            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    osoba model = itemSnapshot.getValue(osoba.class);
                    trenutni_email_iz_baze = model.getEmail();
                    if (trenutni_email.equals(trenutni_email_iz_baze)) {
                        itemSnapshot.getRef().removeValue();
                    } else {
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void dodaj_osobu() {
        final String email = auth.getCurrentUser().getEmail();
        //if (imageUri != null) {
        if (ime.getEditText().getText().toString().equals("") ||
                prezime.getEditText().getText().toString().equals("") ||
                datum_rodjenja.getEditText().getText().toString().equals("") ||
                broj_kartice.getEditText().getText().toString().equals("")) {
        } else {
           obrisi_postojeceg_korisnika();
            //dodaj istog korisnika s novim parametrima
            if (imageUri == null) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    osoba osoba = new osoba(
                            ime.getEditText().getText().toString().trim(),
                            prezime.getEditText().getText().toString().trim(),
                            datum_rodjenja.getEditText().getText().toString().trim(),
                            stara_slika,
                            email,
                            broj_kartice.getEditText().getText().toString().trim());
                    String osobaId = databaseReference.push().getKey();
                    databaseReference.child(osobaId).setValue(osoba);

                    imageUri = null;
                    ime.getEditText().setText("");
                    prezime.getEditText().setText("");
                    datum_rodjenja.getEditText().setText("");
                    broj_kartice.getEditText().setText("");
                    imageButton.setImageResource(R.mipmap.ic_add);

                }
            }, 5000);
            } else {
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
                                }, 5000);
                                Toast.makeText(getContext(), "Prijenos uspješan!", Toast.LENGTH_SHORT).show();

                                osoba osoba = new osoba(
                                        ime.getEditText().getText().toString().trim(),
                                        prezime.getEditText().getText().toString().trim(),
                                        datum_rodjenja.getEditText().getText().toString().trim(),
                                        taskSnapshot.getDownloadUrl().toString(),
                                        email,
                                        broj_kartice.getEditText().getText().toString().trim());
                                String osobaId = databaseReference.push().getKey();
                                databaseReference.child(osobaId).setValue(osoba);

                                imageUri = null;
                                ime.getEditText().setText("");
                                prezime.getEditText().setText("");
                                datum_rodjenja.getEditText().setText("");
                                broj_kartice.getEditText().setText("");
                                imageButton.setImageResource(R.mipmap.ic_add);

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                progressBar.setProgress((int) progress);
                            }
                        });
            }
        }

        //} else {
        //  Toast.makeText(getContext(), "Oops, čini se da nešto nedostaje", Toast.LENGTH_SHORT).show();
        //}
    }
}
