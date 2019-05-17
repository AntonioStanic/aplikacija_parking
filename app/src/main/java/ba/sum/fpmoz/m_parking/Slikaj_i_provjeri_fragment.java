package ba.sum.fpmoz.m_parking;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import ba.sum.fpmoz.m_parking.model.osoba;
import ba.sum.fpmoz.m_parking.model.placeno;

import static android.app.Activity.RESULT_OK;


public class Slikaj_i_provjeri_fragment extends Fragment {

    private Button slikaj_tablice_btn;
    private Button provjeri_parking_btn;
    private ImageView slika_slikanih_tablica;
    private TextView provjeri_tablice_txt;
    private Bitmap imageBitmap;
    Bitmap bmap;

    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 1001;
    String cameraPermission[];
    String storagePermission[];
    public String trenutni_datum_iz_baze;
    public String trenutni_datum;
    FirebaseAuth auth;
    private DatabaseReference databaseReference;
    public String provjera_da_li_postoji;
    public String vrijedi_do;

    Uri slika_uri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.slikaj_i_provjeri_fragment, container, false);

        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        slikaj_tablice_btn = view.findViewById(R.id.slikaj_tablice_btn);
        provjeri_parking_btn = view.findViewById(R.id.provjeri_parking_btn);
        provjeri_tablice_txt = view.findViewById(R.id.provjeri_tablice_txt);
        slika_slikanih_tablica = view.findViewById(R.id.slika_slikanih_tablica);

        slikaj_tablice_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(100);
                if (!checkCameraPermission()) {
                    //nije odobreno koristenje kamere, zatrazi dopustenje
                    requestCameraPermission();
                } else {
                    //dopustena je upotreba kamere, moze se uslikati slika
                    pickCamera();
                }
            }
        });




        provjeri_parking_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(100);
                //upit iz baze za ove tablice
                provjera_da_li_postoji = null;
                String tablice_koje_pretrazujemo = provjeri_tablice_txt.getText().toString();
                if (tablice_koje_pretrazujemo.equals("")) {
                    Toast.makeText(getContext(), "Nedostaju tablice za pretragu", Toast.LENGTH_LONG).show();
                } else {
                    //Traži se skruktura imena placeno i iz nje ćemo dohvaćati parkinge
                    databaseReference = FirebaseDatabase.getInstance().getReference("placeno");
                    final Query query = databaseReference.orderByChild("tablice").equalTo(tablice_koje_pretrazujemo);//equalto-pretrazene tablice
                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                placeno placeno = data.getValue(placeno.class);
                                provjera_da_li_postoji=placeno.getTablice();
                                vrijedi_do=placeno.getVrijedi_do();
                            }
                            if(provjera_da_li_postoji == null){
                                Toast.makeText(getContext(), "Ne postoji parking za ove tablice", Toast.LENGTH_LONG).show();
                                ImageView slika = view.findViewById(R.id.slika_slikanih_tablica);
                                Picasso.get().load(R.mipmap.ic_wrong).into(slika);
                            }else{
                                Toast.makeText(getContext(), "Parking je plaćen i vrijedi do: "+vrijedi_do, Toast.LENGTH_LONG).show();
                                ImageView slika = view.findViewById(R.id.slika_slikanih_tablica);
                                Picasso.get().load(R.mipmap.ic_success).into(slika);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
            }
        });

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
                    if (trenutni_datum.equals(trenutni_datum_iz_baze)) {
                    } else {
                        itemSnapshot.getRef().removeValue();
                    }
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


        return view;
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //imamo sliku iz kamere
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //imamo sliku iz galerije i ovdje ju mozemo izrezati
                CropImage.activity(data.getData())
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(getContext(), this);  // ovdje se omogucuje okvir za rezanje
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //imamo sliku iz kamere i ovdje ju mozemo izrezati
                CropImage.activity(slika_uri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(getContext(), this);
            }
        }

        //dohvati izrezanu sliku
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri rezultatUri = result.getUri(); //get image uri, 36:18

                //postavljanje slike u image view
                slika_slikanih_tablica.setImageURI(rezultatUri);

                slika_slikanih_tablica.buildDrawingCache();
                bmap = slika_slikanih_tablica.getDrawingCache();


                //dohvati crtajuci bitmap za prepoznavanje teksta
                BitmapDrawable bitmapDrawable = (BitmapDrawable) slika_slikanih_tablica.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();
                detectTxt();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                //ako postoji neka greska, prikazi ju
                Exception greska = result.getError();
                Toast.makeText(this.getContext(), "" + greska, Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void detectTxt() {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bmap);
        FirebaseVisionTextDetector detector = FirebaseVision.getInstance().getVisionTextDetector();
        detector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                processTxt(firebaseVisionText);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void processTxt(FirebaseVisionText text) {
        List<FirebaseVisionText.Block> blocks = text.getBlocks();
        if (blocks.size() == 0) {
            Toast.makeText(this.getContext(), " Slika je loša, pokušajte ponovno ", Toast.LENGTH_LONG).show();
            return;
        }
        for (FirebaseVisionText.Block block : text.getBlocks()) {
            String txt = block.getText();
            provjeri_tablice_txt.setTextSize(30);
            provjeri_tablice_txt.setText(txt.trim().replace(" ", ""));
        }
    }


    private void pickCamera() {
        //uzimanje slike iz kamere, takoder ce se slika sacuvati u galeriju da bi se dobila bolja kvaliteta
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Nova slika"); // naziv slike
        values.put(MediaStore.Images.Media.DESCRIPTION, "Sa slike u tekst"); // opis slike
        slika_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, slika_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }


    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this.getActivity(), cameraPermission, CAMERA_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        //provjeravamo je li dopustena upotreba kamere i vracamo rezultat
        // da bi dobili dobru kvalitetu slike, pozeljno je da ju spremimo  vanjski spremnik prije umetanja u imageview i zbog toga nam treba dopustenje
        boolean rezultat = ContextCompat.checkSelfPermission(this.getContext(),
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean rezultat1 = ContextCompat.checkSelfPermission(this.getContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return rezultat && rezultat1;
    }

    //rukovanje rezultatom dozvole

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean kameraPrihvacena = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean spremanjePrihvaceno = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (kameraPrihvacena && spremanjePrihvaceno) {
                        pickCamera();
                    } else {
                        Toast.makeText(this.getContext(), "Dopustenje odbijeno!", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }
}
