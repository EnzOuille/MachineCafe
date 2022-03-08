import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class MachineCafe {

    private ArrayList<Boisson> boissons;
    private Stock stock;
    private boolean condition = true;
    private int maxBoissonsSize = 5;
    private final String noBoissons = "--- Aucune boisson n'est disponible dans la machine ---";
    public MachineCafe() {
        this.boissons = new ArrayList<>();
        this.stock = new Stock(0, 0, 0, 0, 0);
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
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();
            FileWriter file = new FileWriter("machine.json");
            String jsonString = gson.toJson(machine);
            file.write(jsonString);
            file.flush();
            file.close();
            scanner.close();
        } catch (IllegalStateException | NoSuchElementException e) {
            println("System.in was closed; exiting");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void println(String str) {
        System.out.println(str);
    }

    private static void mainMachine(Scanner scanner, MachineCafe machine, String line) {
        try {
            switch (Integer.parseInt(line)) {
                case 1:
                    if (machine.boissons.isEmpty()) {
                        machine.acheterBoisson(scanner);
                    } else {
                        println(machine.noBoissons);
                    }
                    break;
                case 2:
                    if (machine.boissons.size() >= machine.maxBoissonsSize) {
                        println("--- Déjà 3 boissons dans la machine. Impossible d'en ajouter ---");
                    } else {
                        machine.ajouterBoisson(scanner);
                    }
                    break;
                case 3:
                    if (machine.boissons.isEmpty()) {
                        machine.modifierBoisson(scanner);
                    } else {
                        println(machine.noBoissons);
                    }
                    break;
                case 4:
                    if (machine.boissons.isEmpty()) {
                        machine.supprimerBoisson(scanner);
                    } else {
                        println(machine.noBoissons);
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
        } catch (IntStockException | IntPriceException | NameException | NameDontExist | BadComposition | CustomException e) {
            println(e.getMessage());
        }
    }

    private void acheterBoisson(Scanner scanner) throws NameException, NameDontExist, IntStockException, CustomException {
        this.listBoissons();
        println("--- Saississez le nom de la boisson à acheter ---");
        String nom_boisson = scanner.nextLine();
        if (nom_boisson.isBlank() || nom_boisson.isEmpty() || nom_boisson.length() > 100) {
            throw new NameException();
        }
        if (!this.checkBoissonExist(nom_boisson)) {
            throw new NameDontExist();
        }
        println("--- Saisissez votre nombre de sucre ---");
        int sucre = Integer.parseInt(scanner.nextLine());
        if (!(sucre >= 0 && sucre <= 5 && this.stock.getSucre() >= sucre)) {
            throw new CustomException("--- Vous avez mis trop de sucre ou alors il n'y en a plus ---");
        }
        println("--- Insérer votre monnaie (entier) ---");
        int montant = Integer.parseInt(scanner.nextLine());
        if (verifIntegerPrice(montant)) {
            throw new IntStockException();
        }
        for (Boisson boisson : this.boissons) {
            if (boisson.getNom().equalsIgnoreCase(nom_boisson)) {
                if (boisson.getPrix() < montant) {
                    if (this.stock.getCafe() > boisson.getNbCafe() && this.stock.getSucre() > sucre && this.stock.getChocolat() > boisson.getNbChocolat() && this.stock.getLait() > boisson.getNbLait() && this.stock.getThe() > boisson.getNbThe()) {
                        println("-- Voici votre " + boisson.getNom() + " --");
                        if (montant != boisson.getPrix()) {
                            println("-- Voici votre monnaie : " + (montant - boisson.getPrix()) + " € --");
                        }
                        this.reduceStock(boisson.getNbCafe(), sucre, boisson.getNbChocolat(), boisson.getNbLait(), boisson.getNbThe());
                    } else {
                        println("-- Il n'y a plus assez d'ingrédients --");
                    }
                } else {
                    println("-- Vous n'avez pas assez d'argent --");
                }
            }
        }
    }

    private void reduceStock(int cafe, int sucre, int chocolat, int lait, int the) {
        this.stock.setCafe(this.stock.getCafe() - cafe);
        this.stock.setSucre(this.stock.getSucre() - sucre);
        this.stock.setChocolat(this.stock.getChocolat() - chocolat);
        this.stock.setLait(this.stock.getLait() - lait);
        this.stock.setThe(this.stock.getThe() - the);
    }

    private void ajouterBoisson(Scanner scanner) throws NameException, IntPriceException, BadComposition, CustomException {
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
        HashMap<String, Integer> ingredients = this.parseIngredients(scanner);
        boolean isBoisson = true;
        for (Boisson boisson : this.boissons) {
            if (boisson.getNom().equalsIgnoreCase(nom)) {
                isBoisson = false;
                break;
            }
        }
        if (isBoisson) {
            Boisson temp_boisson = new Boisson(nom, prix, ingredients.get("cafe"), ingredients.get("lait"), ingredients.get("chocolat"), ingredients.get("the"));
            this.boissons.add(temp_boisson);
            println("-- L'ajout de la boisson " + temp_boisson.getNom() + " est fini --");
        } else {
            println("-- Il existe déjà une boisson du même nom dans la machine --");
        }
    }

    private void modifierBoisson(Scanner scanner) throws NameException, NameDontExist, BadComposition, CustomException {
        println("--- Saisissez le nom de la boisson dont la composition doit etre modifiée ---");
        this.listBoissons();
        String nom_modif = scanner.nextLine();
        if (nom_modif.isEmpty() || nom_modif.isBlank() || nom_modif.length() > 100) {
            throw new NameException();
        }
        if (!this.checkBoissonExist(nom_modif)) {
            throw new NameDontExist();
        }
        HashMap<String, Integer> ingredients = this.parseIngredients(scanner);
        for (Boisson boisson : this.boissons) {
            if (boisson.getNom().equalsIgnoreCase(nom_modif)) {
                boisson.setNbCafe(ingredients.get("cafe"));
                boisson.setNbChocolat(ingredients.get("chocolat"));
                boisson.setNbLait(ingredients.get("lait"));
                boisson.setNbThe(ingredients.get("the"));
            }
        }
        println("--- La composition de la boisson a bien étée modifiée ---");
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

    private void ajoutStockIngredient(Scanner scanner) throws IntStockException, BadComposition, CustomException {
        this.consulterStock();
        println("--- Rentrez dans l'ordre et séparé par un ';'' le stock à ajouter : Cafe Sucre Chocolat Lait The ---");
        String[] ingredients_stock = scanner.nextLine().split(";");
        if (ingredients_stock.length != 5) {
            throw new CustomException("--- Mauvaise saisie des ingrédients ---");
        }
        int cafe_stock = Integer.parseInt(ingredients_stock[0]);
        int sucre_stock = Integer.parseInt(ingredients_stock[2]);
        int chocolat_stock = Integer.parseInt(ingredients_stock[3]);
        int lait_stock = Integer.parseInt(ingredients_stock[1]);
        int the_stock = Integer.parseInt(ingredients_stock[4]);
        if (verifIntegerStock(cafe_stock) && verifIntegerStock(sucre_stock) && verifIntegerStock(chocolat_stock) && verifIntegerStock(lait_stock) && verifIntegerStock(the_stock)) {
            this.stock.setCafe(this.stock.getCafe() + cafe_stock);
            this.stock.setSucre(this.stock.getSucre() + sucre_stock);
            this.stock.setChocolat(this.stock.getChocolat() + chocolat_stock);
            this.stock.setLait(this.stock.getLait() + lait_stock);
            this.stock.setThe(this.stock.getThe() + the_stock);
            println("-- La modification du stock a bien été prise en compte --");
        } else {
            throw new IntStockException();
        }
    }

    private void consulterStock() {
        println(this.stock.toString());
    }

    private void listBoissons() {
        this.boissons.sort(Comparator.comparing(Boisson::getNom));
        for (Boisson boisson : this.boissons) {
            println(boisson.shortToString());
        }
    }

    private void listBoissonsDetails() {
        if (this.boissons.size() > 0) {
            this.boissons.sort(Comparator.comparing(Boisson::getNom));
            for (Boisson boisson : this.boissons) {
                println(boisson.toString());
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

    private boolean verifComposition(int cafe, int lait, int chocolat, int the) {
        if (cafe > 0 && the > 0) {
            return false;
        }
        return cafe != 0 || lait != 0 || chocolat != 0 || the != 0;
    }

    private HashMap<String, Integer> parseIngredients(Scanner scanner) throws BadComposition, CustomException {
        println("--- Rentrer dans l'ordre et séparé par un ';' la composition : Cafe Lait Chocolat The ---");
        String[] ingredients = scanner.nextLine().split(";");
        if (ingredients.length != 4) {
            throw new CustomException("--- Mauvaise saisie des ingrédients ---");
        }
        int cafe = Integer.parseInt(ingredients[0]);
        int lait = Integer.parseInt(ingredients[1]);
        int chocolat = Integer.parseInt(ingredients[2]);
        int the = Integer.parseInt(ingredients[3]);
        if (!verifComposition(cafe, lait, chocolat, the)) {
            throw new BadComposition();
        }
        HashMap<String, Integer> res = new HashMap<>();
        res.put("cafe", cafe);
        res.put("lait", lait);
        res.put("chocolat", chocolat);
        res.put("the", the);
        return res;
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
            super("--- La composition de votre boisson n'est pas correcte ---");
        }
    }

    static class CustomException extends Exception {

        public CustomException(String message) {
            super(message);
        }
    }
}