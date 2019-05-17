package ba.sum.fpmoz.m_parking;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class Podijeli_fragment extends Fragment {

    public Button podijeli;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.podijeli_fragment, container, false);

        podijeli = view.findViewById(R.id.podijeli_btn);

        String poruka = " Ukoliko želite koristiti mobilnu aplikaciju \"m-parking\"" +
                " molim da kliknete na link koji se nalazi ispod.. \n" +
                " https://trgovinaplay.m-parking.apk \n" +
                " VELIKO HVALA, m-parking team ";
        Intent shareText = new Intent(android.content.Intent.ACTION_SEND);
        shareText .setType("text/plain");
        shareText .putExtra(Intent.EXTRA_TEXT, poruka);
        startActivity(Intent.createChooser(shareText , "Pošalji poruku i pozovi u aplikaciju \"m-parking\""));

        podijeli.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(100);
                String poruka = " Ukoliko želite koristiti mobilnu aplikaciju \"m-parking\"" +
                        " molim da kliknete na link koji se nalazi ispod.. \n" +
                        " https://trgovinaplay.m-parking.apk \n" +
                        " VELIKO HVALA, m-parking team ";
                Intent shareText = new Intent(android.content.Intent.ACTION_SEND);
                shareText .setType("text/plain");
                shareText .putExtra(Intent.EXTRA_TEXT, poruka);
                startActivity(Intent.createChooser(shareText , "Pošalji poruku i pozovi u aplikaciju \"m-parking\""));
            }
        });

        return view;
    }
}
