package Models;

import java.sql.*;
import java.util.*;

/**
 * PurchaseDAO - Data Access Object for purchases (sans authentification utilisateur)
 * Gère les achats avec email et date
 */
public class PurchaseDAO {
    private String dbPath;
    private static final String DB_URL_PREFIX = "jdbc:sqlite:";
    
    public PurchaseDAO(String dbPath) {
        this.dbPath = dbPath;
        initializeDatabase();
    }
    
    private void initializeDatabase() {
        String createTableSQL = 
            "CREATE TABLE IF NOT EXISTS purchases (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  email TEXT NOT NULL," +
            "  bookId INTEGER NOT NULL," +
            "  title TEXT NOT NULL," +
            "  price REAL NOT NULL," +
            "  quantity INTEGER DEFAULT 1," +
            "  totalPrice REAL NOT NULL," +
            "  purchaseDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createTableSQL);
            System.out.println("Purchases table initialized successfully");
        } catch (SQLException e) {
            System.err.println("Error initializing purchases table: " + e.getMessage());
        }
    }
    
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL_PREFIX + dbPath);
    }
    
    /**
     * Enregistrer un achat
     */
    public boolean recordPurchase(String email, int bookId, String title, double price, int quantity) {
        String sql = "INSERT INTO purchases (email, bookId, title, price, quantity, totalPrice) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setInt(2, bookId);
            pstmt.setString(3, title);
            pstmt.setDouble(4, price);
            pstmt.setInt(5, quantity);
            pstmt.setDouble(6, price * quantity);
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error recording purchase: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Récupérer l'historique des achats par email
     */
    public List<Map<String, Object>> getPurchasesByEmail(String email) {
        List<Map<String, Object>> purchases = new ArrayList<>();
        String sql = "SELECT * FROM purchases WHERE email = ? ORDER BY purchaseDate DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> purchase = new HashMap<>();
                    purchase.put("id", rs.getInt("id"));
                    purchase.put("email", rs.getString("email"));
                    purchase.put("bookId", rs.getInt("bookId"));
                    purchase.put("title", rs.getString("title"));
                    purchase.put("price", rs.getDouble("price"));
                    purchase.put("quantity", rs.getInt("quantity"));
                    purchase.put("totalPrice", rs.getDouble("totalPrice"));
                    purchase.put("purchaseDate", rs.getTimestamp("purchaseDate"));
                    purchases.add(purchase);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving purchases: " + e.getMessage());
        }
        return purchases;
    }
    
    /**
     * Obtenir le total dépensé pour un email
     */
    public double getTotalSpentByEmail(String email) {
        String sql = "SELECT SUM(totalPrice) as total FROM purchases WHERE email = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calculating total: " + e.getMessage());
        }
        return 0.0;
    }
}
