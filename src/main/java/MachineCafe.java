import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class MachineCafe {

    private ArrayList<Boisson> boissons;
    private HashMap<String, Integer> stock;
    private boolean condition = true;

    public MachineCafe() {
        this.boissons = new ArrayList<>();
        this.stock = new HashMap<>();
        this.stock.put("cafe", 0);
        this.stock.put("sucre", 0);
        this.stock.put("lait", 0);
        this.stock.put("chocolat", 0);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        MachineCafe machine = new MachineCafe();
        try {
            Gson gson = new Gson();
            machine = gson.fromJson(new FileReader("machine.json"), machine.getClass());
        } catch (FileNotFoundException ignored) {
        }
        try {
            while (machine.condition) {
                machine.printOptions();
                String line = scanner.nextLine();
                mainMachine(scanner, machine, line);
            }
            machine.condition = true;
            try {
                Gson gson = new GsonBuilder()
                        .setPrettyPrinting()
                        .create();
                FileWriter file = new FileWriter("machine.json");
                String jsonString = gson.toJson(machine);
                file.write(jsonString);
                file.flush();
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IllegalStateException | NoSuchElementException e) {
            println("System.in was closed; exiting");
        }
    }

    private static void print(String str) {
        System.out.print(str);
    }

    private static void println(String str) {
        System.out.println(str);
    }

    private static void mainMachine(Scanner scanner, MachineCafe machine, String line) {
        try {
            switch (Integer.parseInt(line)) {
                case 1:
                    if (machine.boissons.size() > 0) {
                        machine.acheterBoisson(scanner);
                    } else {
                        println("--- Aucune boisson n'est disponible dans la machine ---");
                    }
                    break;
                case 2:
                    if (machine.boissons.size() >= 3) {
                        println("--- Déjà 3 boissons dans la machine. Impossible d'en ajouter ---");
                    } else {
                        machine.ajouterBoisson(scanner);
                    }
                    break;
                case 3:
                    if (machine.boissons.size() > 0) {
                        machine.modifierBoisson(scanner);
                    } else {
                        println("--- Aucune boisson n'est disponible dans la machine ---");
                    }
                    break;
                case 4:
                    if (machine.boissons.size() > 0) {
                        machine.supprimerBoisson(scanner);
                    } else {
                        println("--- Aucune boisson n'est disponible dans la machine ---");
                    }
                    break;
                case 5:
                    machine.ajoutStockIngredient(scanner);
                    break;
                case 6:
                    machine.consulterStock();
                    break;
                case 7:
                    machine.listBoissonsDetails();
                    break;
                case 8:
                    machine.condition = false;
                    break;
                default:
                    println("Cette option n'existe pas");
                    break;
            }
        } catch (NumberFormatException e) {
            println("--- Votre entier n'est pas correct, il doit être positif et compris entre 0 et 10000 ---");
        } catch (IntStockException | IntPriceException | NameException | NameDontExist | BadComposition e) {
            println(e.getMessage());
        }
    }

    private void acheterBoisson(Scanner scanner) throws NameException, NameDontExist, IntStockException {
        this.listBoissons();
        println("--- Saississez le nom de la boisson à acheter ---");
        String nom_boisson = scanner.nextLine();
        if (nom_boisson.isBlank() || nom_boisson.isEmpty() || nom_boisson.length() > 100) {
            throw new NameException();
        }
        if (!this.checkBoissonExist(nom_boisson)) {
            throw new NameDontExist();
        }
        println("--- Insérer votre monnaie (entier) ---");
        int montant = Integer.parseInt(scanner.nextLine());
        if (verifIntegerPrice(montant)) {
            throw new IntStockException();
        }
        for (Boisson boisson : this.boissons) {
            if (boisson.getNom().equalsIgnoreCase(nom_boisson)) {
                if (boisson.getPrix() < montant) {
                    if (this.stock.get("cafe") > boisson.getNbCafe() && this.stock.get("sucre") > boisson.getNbSucre() && this.stock.get("chocolat") > boisson.getNbChocolat() && this.stock.get("lait") > boisson.getNbLait()) {
                        println("-- Voici votre " + boisson.getNom() + " --");
                        if (montant != boisson.getPrix()) {
                            println("-- Voici votre monnaie : " + (montant - boisson.getPrix()) + " € --");
                        }
                        this.reduceStock(boisson.getNbCafe(), boisson.getNbSucre(), boisson.getNbChocolat(), boisson.getNbLait());
                    } else {
                        println("-- Il n'y a plus assez d'ingrédients --");
                    }
                } else {
                    println("-- Vous n'avez pas assez d'argent --");
                }
            }
        }
    }

    private void reduceStock(int cafe, int sucre, int chocolat, int lait) {
        this.stock.replace("cafe", this.stock.get("cafe") - cafe);
        this.stock.replace("sucre", this.stock.get("sucre") - sucre);
        this.stock.replace("chocolat", this.stock.get("chocolat") - chocolat);
        this.stock.replace("lait", this.stock.get("lait") - lait);
    }

    private void ajouterBoisson(Scanner scanner) throws NameException, IntPriceException, BadComposition {
        println("--- Rentrer le nom de la boisson à créer ---");
        String nom = scanner.nextLine();
        if (nom.isEmpty() || nom.isBlank() || nom.length() > 100) {
            throw new NameException();
        }
        println("--- Rentrer le prix de la boisson (entier) ---");
        int prix = Integer.parseInt(scanner.nextLine());
        if (verifIntegerPrice(prix)) {
            throw new IntPriceException();
        }
        println("--- Rentrer dans l'ordre et séparé par un ';' la composition : Cafe Lait Sucre Chocolat ---");
        String[] ingredients = scanner.nextLine().split(";");
        if (!(ingredients.length == 4)) {
            throw new BadComposition();
        }
        int cafe = Integer.parseInt(ingredients[0]);
        int lait = Integer.parseInt(ingredients[1]);
        int sucre = Integer.parseInt(ingredients[2]);
        int chocolat = Integer.parseInt(ingredients[3]);
        if (verifComposition(cafe, lait, sucre, chocolat)) {
            throw new BadComposition();
        }
        boolean condition = true;
        for (Boisson boisson : this.boissons) {
            if (boisson.getNom().equalsIgnoreCase(nom)) {
                condition = false;
                break;
            }
        }
        if (condition) {
            Boisson temp_boisson = new Boisson(nom, prix, cafe, lait, sucre, chocolat);
            this.boissons.add(temp_boisson);
            println("-- L'ajout de la boisson " + temp_boisson.getNom() + " est fini --");
        } else {
            println("-- Il existe déjà une boisson du même nom dans la machine --");
        }
    }

    private void modifierBoisson(Scanner scanner) throws NameException, NameDontExist, BadComposition {
        println("--- Saisissez le nom de la boisson dont la composition doit etre modifiée ---");
        this.listBoissons();
        String nom_modif = scanner.nextLine();
        if (nom_modif.isEmpty() || nom_modif.isBlank() || nom_modif.length() > 100) {
            throw new NameException();
        }
        if (!this.checkBoissonExist(nom_modif)) {
            throw new NameDontExist();
        }
        println("--- Rentrer dans l'ordre et séparé par un ';' la  nouvelle composition : Cafe Lait Sucre Chocolat ---");
        String[] ingredients_modif = scanner.nextLine().split(";");
        if (!(ingredients_modif.length == 4)) {
            throw new BadComposition();
        }
        int cafe_modif = Integer.parseInt(ingredients_modif[0]);
        int lait_modif = Integer.parseInt(ingredients_modif[1]);
        int sucre_modif = Integer.parseInt(ingredients_modif[2]);
        int chocolat_modif = Integer.parseInt(ingredients_modif[3]);
        if (verifComposition(cafe_modif, sucre_modif, lait_modif, chocolat_modif)) {
            throw new BadComposition();
        }
        for (Boisson boisson : this.boissons) {
            if (boisson.getNom().equalsIgnoreCase(nom_modif)) {
                boisson.setNbCafe(cafe_modif);
                boisson.setNbSucre(sucre_modif);
                boisson.setNbChocolat(chocolat_modif);
                boisson.setNbLait(lait_modif);
            }
        }
        println("--- La compisition de la boisson a bien étée modifiée ---");
    }

    private void supprimerBoisson(Scanner scanner) throws NameException, NameDontExist {
        println("--- Saisissez le nom de la boisson a supprimer ---");
        this.listBoissons();
        String temp = scanner.nextLine();
        if (temp.isBlank() || temp.isEmpty() || temp.length() > 100) {
            throw new NameException();
        }
        if (this.checkBoissonExist(temp)) {
            throw new NameDontExist();
        }
        this.boissons.removeIf(boisson -> boisson.getNom().equalsIgnoreCase(temp));
    }

    private void ajoutStockIngredient(Scanner scanner) throws IntStockException, BadComposition {
        this.consulterStock();
        println("--- Rentrez dans l'ordre et séparé par un ';'' le stock à ajouter : Cafe Sucre Chocolat Lait ---");
        String[] ingredients_stock = scanner.nextLine().split(";");
        if (!(ingredients_stock.length == 4)) {
            throw new BadComposition();
        }
        int cafe_stock = Integer.parseInt(ingredients_stock[0]);
        int sucre_stock = Integer.parseInt(ingredients_stock[2]);
        int chocolat_stock = Integer.parseInt(ingredients_stock[3]);
        int lait_stock = Integer.parseInt(ingredients_stock[1]);
        if (verifIntegerStock(cafe_stock) && verifIntegerStock(sucre_stock) && verifIntegerStock(chocolat_stock) && verifIntegerStock(lait_stock)) {
            this.stock.replace("cafe", this.stock.get("cafe") + cafe_stock);
            this.stock.replace("sucre", this.stock.get("sucre") + sucre_stock);
            this.stock.replace("chocolat", this.stock.get("chocolat") + chocolat_stock);
            this.stock.replace("lait", this.stock.get("lait") + lait_stock);
            println("-- La modification du stock a bien été prise en compte --");
        } else {
            throw new IntStockException();
        }
    }

    private void consulterStock() {
        this.stock.forEach((k, v) -> {
            print(k.toUpperCase() + " : " + v + " ");
        });
        print("\n");
    }

    private void listBoissons() {
        this.boissons.sort(Comparator.comparing(Boisson::getNom));
        for (Boisson boisson : this.boissons) {
            println(boisson.getNom() + " : " + boisson.getPrix() + " €");
        }
    }

    private void listBoissonsDetails() {
        if (this.boissons.size() > 0) {
            this.boissons.sort(Comparator.comparing(Boisson::getNom));
            for (Boisson boisson : this.boissons) {
                print(boisson.getNom() + " : " + boisson.getPrix() + " €");
                print(" Composition : Cafe : " + boisson.getNbCafe() + " Sucre : " + boisson.getNbSucre() + " Chocolat : " + boisson.getNbChocolat() + " Lait : " + boisson.getNbLait());
                print("\n");
            }
        } else {
            println("--- Aucune boissons dans la machine actuellement ---");
        }
    }

    private boolean checkBoissonExist(String nom) {
        for (Boisson boisson : this.boissons) {
            if (boisson.getNom().equalsIgnoreCase(nom)) {
                return true;
            }
        }
        return false;
    }

    private void printOptions() {
        println("--- Veuillez choisir le numéro de l'action que vous souhaitez effectuer ---");
        println("[1] - Acheter une boisson");
        println("[2] - Ajouter une boisson");
        println("[3] - Modifier la composition d'une boisson");
        println("[4] - Supprimer une boisson");
        println("[5] - Ajouter au stock");
        println("[6] - Consulter Stock");
        println("[7] - Afficher le détail des boissons");
        println("[8] - Quitter le programme et sauvegarder l'état de la machine");
    }

    private boolean verifIntegerStock(int test) {
        return test >= 0 && test < 10000;
    }

    private boolean verifIntegerPrice(int test) {
        return test <= 0 || test >= 10000;
    }

    private boolean verifComposition(int cafe, int sucre, int lait, int chocolat) {
        if (cafe == 0 && sucre == 0 && lait == 0 && chocolat == 0) {
            return true;
        } else return sucre > 0 && cafe == 0 && lait == 0 && chocolat == 0;
    }

    static class IntStockException extends Exception {

        public IntStockException() {
            super("--- L'entier doit être >=0 et <10000 ! ---");
        }
    }

    static class IntPriceException extends Exception {

        public IntPriceException() {
            super("--- L'entier doit être >0 et <10000 ! ---");
        }
    }

    static class NameException extends Exception {

        public NameException() {
            super("--- Le nom saisi n'est pas correct. Doit être non vide et <100 caractères ! ---");
        }
    }

    static class NameDontExist extends Exception {

        public NameDontExist() {
            super("--- Aucune boisson avec ce nom connue ! ---");
        }
    }

    static class BadComposition extends Exception {

        public BadComposition() {
            super("--- Vous ne pouvez pas faire de l'eau sucrée ou une composition vide ---");
        }
    }

}