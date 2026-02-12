public class ProgramObrazovanja {
    private Integer id;
    private String naziv;
    private int CSVET;

    public ProgramObrazovanja(Integer id, String naziv, int CSVET) {
        this.id = id;
        this.naziv = naziv;
        this.CSVET = CSVET;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public int getCSVET() {
        return CSVET;
    }

    public void setCSVET(int CSVET) {
        this.CSVET = CSVET;
    }
}
