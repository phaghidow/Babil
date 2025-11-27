package Models;

import java.io.*;
import java.util.*;

/**
 * Gestionnaire des favoris/wishlist (stocké localement en JSON)
 */
public class FavoritesManager {
    private static final String FAVORITES_FILE = "data/favorites.txt";
    private static Set<Integer> favorites = new HashSet<>();
    
    static {
        loadFavorites();
    }
    
    /**
     * Charger les favoris du fichier
     */
    private static void loadFavorites() {
        try {
            File file = new File(FAVORITES_FILE);
            if (file.exists()) {
                Scanner scanner = new Scanner(file);
                while (scanner.hasNextLine()) {
                    try {
                        int bookId = Integer.parseInt(scanner.nextLine().trim());
                        favorites.add(bookId);
                    } catch (NumberFormatException e) {
                        // Ignorer les lignes invalides
                    }
                }
                scanner.close();
            }
        } catch (FileNotFoundException e) {
            System.out.println("Fichier de favoris non trouvé, création nouvelle liste");
        }
    }
    
    /**
     * Sauvegarder les favoris dans le fichier
     */
    private static void saveFavorites() {
        try {
            File file = new File(FAVORITES_FILE);
            file.getParentFile().mkdirs();
            
            FileWriter writer = new FileWriter(file);
            for (Integer bookId : favorites) {
                writer.write(bookId + "\n");
            }
            writer.close();
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde des favoris: " + e.getMessage());
        }
    }
    
    /**
     * Ajouter un livre aux favoris
     */
    public static void addFavorite(int bookId) {
        if (favorites.add(bookId)) {
            saveFavorites();
        }
    }
    
    /**
     * Supprimer un livre des favoris
     */
    public static void removeFavorite(int bookId) {
        if (favorites.remove(bookId)) {
            saveFavorites();
        }
    }
    
    /**
     * Vérifier si un livre est dans les favoris
     */
    public static boolean isFavorite(int bookId) {
        return favorites.contains(bookId);
    }
    
    /**
     * Toggle favoris
     */
    public static void toggleFavorite(int bookId) {
        if (isFavorite(bookId)) {
            removeFavorite(bookId);
        } else {
            addFavorite(bookId);
        }
    }
    
    /**
     * Obtenir le nombre de favoris
     */
    public static int getFavoriteCount() {
        return favorites.size();
    }
    
    /**
     * Obtenir tous les IDs de favoris
     */
    public static Set<Integer> getAllFavorites() {
        return new HashSet<>(favorites);
    }
    
    /**
     * Vider tous les favoris
     */
    public static void clearAll() {
        favorites.clear();
        saveFavorites();
    }
}
