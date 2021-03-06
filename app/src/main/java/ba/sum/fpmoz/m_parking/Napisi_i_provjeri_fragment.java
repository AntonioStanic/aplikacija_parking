package ba.sum.fpmoz.m_parking;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import ba.sum.fpmoz.m_parking.model.placeno;


public class Napisi_i_provjeri_fragment extends Fragment {

    private Button provjeri_parking_btn;
    private TextInputLayout provjeri_tablice_txt;
    FirebaseAuth auth;
    private DatabaseReference databaseReference;
    public String provjera_da_li_postoji;
    public String vrijedi_do;
    public String trenutni_datum_iz_baze;
    public String trenutni_datum;
    public ImageView placeno_da_ne_img;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.napisi_i_provjeri_fragment, container, false);

        provjeri_parking_btn = view.findViewById(R.id.provjeri_parking_btn);
        provjeri_tablice_txt = view.findViewById(R.id.provjeri_tablice_txt);
        placeno_da_ne_img = view.findViewById(R.id.placeno_da_ne_img);



        provjeri_parking_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(100);
                //upit iz baze za ove tablice
                provjera_da_li_postoji = null;
                String tablice_koje_pretrazujemo = provjeri_tablice_txt.getEditText().getText().toString().toUpperCase();
                provjeri_unesene_tablice(provjeri_tablice_txt);
                if (tablice_koje_pretrazujemo.equals("")) {
                } else {
                    //Traži se skruktura imena placeno i iz nje ćemo dohvaćati parkinge
                    databaseReference = FirebaseDatabase.getInstance().getReference("placeno");
                    final Query query = databaseReference.orderByChild("tablice").equalTo(tablice_koje_pretrazujemo);//equalto-pretrazene tablice
                    query.addValueEventListener(new ValueEventListener() {

                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                placeno placeno = data.getValue(placeno.class);
                                provjera_da_li_postoji = placeno.getTablice();
                                vrijedi_do = placeno.getVrijedi_do();
                            }
                            if (provjera_da_li_postoji == null) {
                                Toast.makeText(getContext(), "Ne postoji parking za ove tablice", Toast.LENGTH_LONG).show();
                                Picasso.get().load(R.mipmap.ic_wrong).into(placeno_da_ne_img);
                            } else {
                                Toast.makeText(getContext(), "Parking je plaćen i vrijedi do: " + vrijedi_do, Toast.LENGTH_LONG).show();
                                Picasso.get().load(R.mipmap.ic_success).into(placeno_da_ne_img);
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

    private boolean provjeri_unesene_tablice(TextInputLayout text) {
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
