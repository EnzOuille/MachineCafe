public class Boisson {

    private String nom;

    private int prix;

    private int nbCafe;

    private int nbLait;

    private int nbSucre;

    private int nbChocolat;

    public Boisson(String nom, int prix, int nbCafe, int nbLait, int nbSucre, int nbChocolat) {
        this.nom = nom;
        this.prix = prix;
        this.nbCafe = nbCafe;
        this.nbLait = nbLait;
        this.nbSucre = nbSucre;
        this.nbChocolat = nbChocolat;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getPrix() {
        return prix;
    }

    public void setPrix(int prix) {
        this.prix = prix;
    }

    public int getNbCafe() {
        return nbCafe;
    }

    public void setNbCafe(int nbCafe) {
        this.nbCafe = nbCafe;
    }

    public int getNbLait() {
        return nbLait;
    }

    public void setNbLait(int nbLait) {
        this.nbLait = nbLait;
    }

    public int getNbSucre() {
        return nbSucre;
    }

    public void setNbSucre(int nbSucre) {
        this.nbSucre = nbSucre;
    }

    public int getNbChocolat() {
        return nbChocolat;
    }

    public void setNbChocolat(int nbChocolat) {
        this.nbChocolat = nbChocolat;
    }
}
