public class Boisson {

    private String nom;

    private int prix;

    private int nbCafe;

    private int nbLait;

    private int nbChocolat;

    private int nbThe;

    public Boisson(String nom, int prix, int nbCafe, int nbLait, int nbChocolat, int nbThe) {
        this.nom = nom;
        this.prix = prix;
        this.nbCafe = nbCafe;
        this.nbLait = nbLait;
        this.nbChocolat = nbChocolat;
        this.nbThe = nbThe;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String shortToString(){
        return this.nom + " - Prix : " + this.prix;
    }

    public String toString() {
        return this.nom + " - Prix : " + this.prix + " - Cafe : " + this.nbCafe + " - Lait : " + this.nbLait + " - Chocolat : " + this.nbChocolat + " - The : " + this.nbThe;
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

    public int getNbChocolat() {
        return nbChocolat;
    }

    public void setNbChocolat(int nbChocolat) {
        this.nbChocolat = nbChocolat;
    }

    public int getNbThe() {
        return nbThe;
    }

    public void setNbThe(int nbThe) {
        this.nbThe = nbThe;
    }
}
