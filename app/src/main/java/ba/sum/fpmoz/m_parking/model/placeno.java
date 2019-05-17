package ba.sum.fpmoz.m_parking.model;



public class placeno {

    String ime;
    String prezime;
    String tablice;
    String trenutni_datum;
    String placeno_u;
    Long vremenski_period;
    String vrijedi_do;

    public placeno(String ime, String prezime, String tablice, String trenutni_datum, String placeno_u, Long vremenski_period, String vrijedi_do) {
        this.ime = ime;
        this.prezime = prezime;
        this.tablice = tablice;
        this.trenutni_datum = trenutni_datum;
        this.placeno_u = placeno_u;
        this.vremenski_period = vremenski_period;
        this.vrijedi_do = vrijedi_do;
    }

    public placeno() {
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

    public String getTablice() {
        return tablice;
    }

    public void setTablice(String tablice) {
        this.tablice = tablice;
    }

    public String getTrenutni_datum() {
        return trenutni_datum;
    }

    public void setTrenutni_datum(String trenutni_datum) {
        this.trenutni_datum = trenutni_datum;
    }

    public String getPlaceno_u() {
        return placeno_u;
    }

    public void setPlaceno_u(String placeno_u) {
        this.placeno_u = placeno_u;
    }

    public Long getVremenski_period() {
        return vremenski_period;
    }

    public void setVremenski_period(Long vremenski_period) {
        this.vremenski_period = vremenski_period;
    }

    public String getVrijedi_do() {
        return vrijedi_do;
    }

    public void setVrijedi_do(String vrijedi_do) {
        this.vrijedi_do = vrijedi_do;
    }
}