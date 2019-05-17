package ba.sum.fpmoz.m_parking.model;

public class osoba {
    public String ime;
    public String prezime;
    public String datumrodjenja;
    public String slika;
    public String email;
    public String brojkartice;

    public String getDatumrodjenja() {
        return datumrodjenja;
    }

    public void setDatumrodjenja(String datumrodjenja) {
        this.datumrodjenja = datumrodjenja;
    }

    public osoba(String ime, String prezime, String datumrodjenja, String slika, String email, String brojkartice) {
        this.ime = ime;
        this.prezime = prezime;
        this.datumrodjenja = datumrodjenja;
        this.slika = slika;
        this.email = email;

        this.brojkartice = brojkartice;
    }

    public osoba(String ime, String prezime, String slika, String email, String brojkartice) {
        this.ime = ime;
        this.prezime = prezime;
        this.slika = slika;
        this.email = email;

        this.brojkartice = brojkartice;
    }

    public osoba() {
    }

    public String getIme() {
        return ime;
    }

    public void setIme(String ime) {
        this.ime = ime;
    }

    public String getPrezime() {
        return prezime;
    }

    public void setPrezime(String prezime) {
        this.prezime = prezime;
    }

    public String getSlika() {
        return slika;
    }

    public void setSlika(String slika) {
        this.slika = slika;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getBrojkartice() {
        return brojkartice;
    }

    public void setBrojkartice(String brojkartice) {
        this.brojkartice = brojkartice;
    }
}
