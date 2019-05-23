package ba.sum.fpmoz.m_parking;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;

import ba.sum.fpmoz.m_parking.model.placeno;

public class O_aplikaciji_fragment extends Fragment {

    public String trenutni_datum_iz_baze;
    public String trenutni_datum;
    
    @Nullable
    @Override


    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.o_aplikaciji_fragment, container, false);

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
}
