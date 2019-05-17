package ba.sum.fpmoz.m_parking;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import ba.sum.fpmoz.m_parking.model.osoba;
import ba.sum.fpmoz.m_parking.model.placeno;

public class Napisi_i_plati_fragment extends Fragment {

    private Button plati_parking_btn;
    private TextInputLayout unesi_tablice_txt;
    FirebaseAuth auth;
    private DatabaseReference databaseReference;
    public String ime;
    public String prezime;
    public String vrijeme_parkinga = null;
    public String trenutni_datum_iz_baze;
    public String trenutni_datum;
    public Spinner vrijeme_parkinga_spinner;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.napisi_i_plati_fragment, container, false);

        plati_parking_btn = view.findViewById(R.id.plati_parking_btn);
        unesi_tablice_txt = view.findViewById(R.id.provjeri_tablice_txt);
        vrijeme_parkinga_spinner = view.findViewById(R.id.vrijeme_parkinga_spinner);
        auth = FirebaseAuth.getInstance();

        //spiner dropdown
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.vrijeme_parkinga_dropdown, R.layout.izgled_spinner);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        vrijeme_parkinga_spinner.setAdapter(adapter);
        vrijeme_parkinga_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                vrijeme_parkinga = parent.getItemAtPosition(position).toString();
                if (vrijeme_parkinga.equals("Odaberi vrijeme parkinga")) {
                    vrijeme_parkinga = null;
                } else {
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });




        //Traži se skruktura imena placeno i u nju ćemo dodavati novi plaćeni parking
        this.databaseReference = FirebaseDatabase.getInstance().getReference("placeno");


        //dohvatiti sliku, ime,prezime i email korisnika

        DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference("osoba");
        final Query query = mDatabaseRef.orderByChild("email").equalTo(auth.getCurrentUser().getEmail());

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot data : dataSnapshot.getChildren()) {

                    osoba models = data.getValue(osoba.class);
                    ime = models.getIme();
                    prezime = models.getPrezime();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        plati_parking_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(100);
                String provjeri_tablice = unesi_tablice_txt.getEditText().getText().toString().toUpperCase();
                provjeri_unesene_tablice(unesi_tablice_txt);
                if (provjeri_tablice.equals("")) {
                } else if (vrijeme_parkinga == null) {
                    Toast.makeText(getContext(), "Odaberite vremenski period parkinga", Toast.LENGTH_LONG).show();
                } else {
                    //spremi u bazu u tablicu placeno
                    dodaj_placeno();
                    long s = (vrijedi_do_u_milis / 1000) % 60;
                    long m = (vrijedi_do_u_milis / 60000) % 60;
                    long h = (vrijedi_do_u_milis / (3600000)) % 24;
                    Toast.makeText(getContext(), "Uspiješno plaćen parking, vrijedi do:" + h + ":" + m + ":" + s, Toast.LENGTH_LONG).show();
                }

            }


        });

        //trenutno brisanje iz firebase
        //ovo je za datum
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

        //ovo je za vrijeme
        long trenutno_vrijeme_u_milis = System.currentTimeMillis() + (2 * 60 * 60 * 1000);
        long s = (trenutno_vrijeme_u_milis / 1000) % 60;
        long m = (trenutno_vrijeme_u_milis / 60000) % 60;
        long h = (trenutno_vrijeme_u_milis / (3600000)) % 24;
        String s_string = String.valueOf(s);
        String m_string = String.valueOf(m);
        String h_string = String.valueOf(h);
        String trenutno_vrijeme = h_string + ":" + m_string + ":" + s_string;
        //DatabaseReference mmDatabaseRef = FirebaseDatabase.getInstance().getReference("placeno");
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

    public long vrijedi_do_u_milis;

    private void dodaj_placeno() {
        if (unesi_tablice_txt != null && vrijeme_parkinga != null) {

            String trenutni_datum = DateFormat.getDateInstance().format(new Date());

            long trenutno_vrijeme_u_milis = System.currentTimeMillis() + (2 * 60 * 60 * 1000);
            long s = (trenutno_vrijeme_u_milis / 1000) % 60;
            long m = (trenutno_vrijeme_u_milis / 60000) % 60;
            long h = (trenutno_vrijeme_u_milis / (3600000)) % 24;
            String s_string = String.valueOf(s);
            String m_string = String.valueOf(m);
            String h_string = String.valueOf(h);
            String trenutno_vrijeme = h_string + ":" + m_string + ":" + s_string;

            long vremenski_period_u_minutama = 0;// get vrijeme koje se odabere u spinneru

            //za spinner što je odabrano
            if (vrijeme_parkinga.equals("30 minuta")) {
                vremenski_period_u_minutama = 30;
            } else if (vrijeme_parkinga.equals("1 sat")) {
                vremenski_period_u_minutama = 60;
            } else if (vrijeme_parkinga.equals("2 sata")) {
                vremenski_period_u_minutama = 120;
            } else if (vrijeme_parkinga.equals("3 sata")) {
                vremenski_period_u_minutama = 180;
            } else if (vrijeme_parkinga.equals("1 dan")) {
                vremenski_period_u_minutama = 600;
            }

            vrijedi_do_u_milis = trenutno_vrijeme_u_milis + (vremenski_period_u_minutama * 60000);
            long s_do = (vrijedi_do_u_milis / 1000) % 60;
            long m_do = (vrijedi_do_u_milis / 60000) % 60;
            long h_do = (vrijedi_do_u_milis / (3600000)) % 24;
            String s_string_do = String.valueOf(s_do);
            String m_string_do = String.valueOf(m_do);
            String h_string_do = String.valueOf(h_do);
            String vrijedi_do = h_string_do + ":" + m_string_do + ":" + s_string_do;


            placeno placeno = new placeno(
                    ime, //ime
                    prezime, //prezime
                    unesi_tablice_txt.getEditText().getText().toString().trim().toUpperCase(), //tablice
                    trenutni_datum,
                    trenutno_vrijeme, //placeno u
                    vremenski_period_u_minutama, //vremenski period
                    vrijedi_do); //vrijedi do
            String placeno_id = databaseReference.push().getKey();
            databaseReference.child(placeno_id).setValue(placeno);

            unesi_tablice_txt.getEditText().setText("");
            trenutni_datum.equals("");
            trenutno_vrijeme.equals("");
            vrijedi_do.equals("");
            vrijeme_parkinga_spinner.setSelection(0, true);

        }

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
