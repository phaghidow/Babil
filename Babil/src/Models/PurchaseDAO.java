package Models;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PurchaseDAO {
    private static final String DB_URL_PREFIX = "jdbc:sqlite:";
    private final String dbPath;

    public enum CheckoutResult {
        SUCCESS,
        ALREADY_OWNED,
        FAILED
    }

    public static final class PurchaseItem {
        private final int bookId;
        private final String title;
        private final double price;
        private final String paymentMethod;

        public PurchaseItem(int bookId, String title, double price) {
            this(bookId, title, price, "Demo");
        }

        public PurchaseItem(int bookId, String title, double price, String paymentMethod) {
            this.bookId = bookId;
            this.title = title;
            this.price = price;
            this.paymentMethod = paymentMethod;
        }
    }

    public PurchaseDAO(String dbPath) {
        this.dbPath = dbPath;
        initializeDatabase();
    }

    private void initializeDatabase() {
        String purchasesTable =
                "CREATE TABLE IF NOT EXISTS ebook_purchases (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "email TEXT NOT NULL COLLATE NOCASE," +
                "bookId INTEGER NOT NULL," +
                "title TEXT NOT NULL," +
                "price REAL NOT NULL," +
                "paymentMethod TEXT NOT NULL DEFAULT 'Demo'," +
                "quantity INTEGER NOT NULL DEFAULT 1 CHECK (quantity = 1)," +
                "totalPrice REAL NOT NULL," +
                "purchaseDate TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)";
        String entitlementsTable =
                "CREATE TABLE IF NOT EXISTS ebook_entitlements (" +
                "email TEXT NOT NULL COLLATE NOCASE," +
                "bookId INTEGER NOT NULL," +
                "purchaseId INTEGER NOT NULL UNIQUE," +
                "PRIMARY KEY (email, bookId)," +
                "FOREIGN KEY (purchaseId) REFERENCES ebook_purchases(id))";

        try (Connection conn = getConnection();
             Statement statement = conn.createStatement()) {
            statement.executeUpdate(purchasesTable);
            ensurePaymentMethodColumn(conn);
            statement.executeUpdate(entitlementsTable);
            statement.executeUpdate(
                    "INSERT OR IGNORE INTO ebook_entitlements (email, bookId, purchaseId) " +
                    "SELECT email, bookId, MIN(id) FROM ebook_purchases " +
                    "GROUP BY email COLLATE NOCASE, bookId");
        } catch (SQLException e) {
            throw new IllegalStateException("Could not initialize the e-book purchase database.", e);
        }
    }

    private void ensurePaymentMethodColumn(Connection conn) throws SQLException {
        boolean columnExists = false;
        try (Statement statement = conn.createStatement();
             ResultSet columns = statement.executeQuery(
                     "PRAGMA table_info(ebook_purchases)")) {
            while (columns.next()) {
                if ("paymentMethod".equalsIgnoreCase(columns.getString("name"))) {
                    columnExists = true;
                    break;
                }
            }
        }
        if (!columnExists) {
            try (Statement statement = conn.createStatement()) {
                statement.executeUpdate(
                        "ALTER TABLE ebook_purchases ADD COLUMN " +
                        "paymentMethod TEXT NOT NULL DEFAULT 'Demo'");
            }
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL_PREFIX + dbPath);
    }

    public CheckoutResult recordPurchases(String email, List<PurchaseItem> items) {
        if (email == null || email.isBlank() || items == null || items.isEmpty()) {
            return CheckoutResult.FAILED;
        }

        Set<Integer> itemIds = new HashSet<>();
        for (PurchaseItem item : items) {
            if (item == null || item.bookId <= 0 || item.title == null
                    || !Double.isFinite(item.price) || item.price < 0
                    || item.paymentMethod == null || item.paymentMethod.isBlank()
                    || !itemIds.add(item.bookId)) {
                return CheckoutResult.FAILED;
            }
        }

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (PurchaseItem item : items) {
                    if (isOwned(conn, email.trim(), item.bookId)) {
                        conn.rollback();
                        return CheckoutResult.ALREADY_OWNED;
                    }
                    long purchaseId = insertPurchase(conn, email.trim(), item);
                    insertEntitlement(conn, email.trim(), item.bookId, purchaseId);
                }
                conn.commit();
                return CheckoutResult.SUCCESS;
            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                System.err.println("Could not complete e-book checkout: " + e.getMessage());
                for (PurchaseItem item : items) {
                    if (hasPurchasedBook(email.trim(), item.bookId)) {
                        return CheckoutResult.ALREADY_OWNED;
                    }
                }
                return CheckoutResult.FAILED;
            }
        } catch (SQLException e) {
            System.err.println("Could not open e-book checkout transaction: " + e.getMessage());
            return CheckoutResult.FAILED;
        }
    }

    public boolean recordPurchase(String email, int bookId, String title,
            double price, int quantity) {
        if (quantity != 1) {
            return false;
        }
        return recordPurchases(email,
                List.of(new PurchaseItem(bookId, title, price))) == CheckoutResult.SUCCESS;
    }

    public boolean hasPurchasedBook(String email, int bookId) {
        if (email == null || email.isBlank() || bookId <= 0) {
            return false;
        }
        try (Connection conn = getConnection()) {
            return isOwned(conn, email.trim(), bookId);
        } catch (SQLException e) {
            System.err.println("Could not check e-book ownership: " + e.getMessage());
            return false;
        }
    }

    private boolean isOwned(Connection conn, String email, int bookId)
            throws SQLException {
        String sql = "SELECT 1 FROM ebook_entitlements WHERE email = ? AND bookId = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, email);
            statement.setInt(2, bookId);
            try (ResultSet results = statement.executeQuery()) {
                return results.next();
            }
        }
    }

    private long insertPurchase(Connection conn, String email, PurchaseItem item)
            throws SQLException {
        String sql = "INSERT INTO ebook_purchases " +
                "(email, bookId, title, price, paymentMethod, quantity, totalPrice) " +
                "VALUES (?, ?, ?, ?, ?, 1, ?)";
        try (PreparedStatement statement = conn.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, email);
            statement.setInt(2, item.bookId);
            statement.setString(3, item.title);
            statement.setDouble(4, item.price);
            statement.setString(5, item.paymentMethod);
            statement.setDouble(6, item.price);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        try (Statement statement = conn.createStatement();
             ResultSet results = statement.executeQuery("SELECT last_insert_rowid()")) {
            if (results.next()) {
                return results.getLong(1);
            }
        }
        throw new SQLException("Could not retrieve the new purchase ID.");
    }

    private void insertEntitlement(Connection conn, String email, int bookId,
            long purchaseId) throws SQLException {
        String sql = "INSERT INTO ebook_entitlements (email, bookId, purchaseId) VALUES (?, ?, ?)";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, email);
            statement.setInt(2, bookId);
            statement.setLong(3, purchaseId);
            statement.executeUpdate();
        }
    }

    public List<Map<String, Object>> getPurchasesByEmail(String email) {
        List<Map<String, Object>> purchases = new ArrayList<>();
        if (email == null || email.isBlank()) {
            return purchases;
        }
        String sql = "SELECT p.* FROM ebook_purchases p " +
                "JOIN ebook_entitlements e ON e.purchaseId = p.id " +
                "WHERE e.email = ? COLLATE NOCASE ORDER BY p.purchaseDate DESC";
        try (Connection conn = getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, email.trim());
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    Map<String, Object> purchase = new HashMap<>();
                    purchase.put("id", results.getInt("id"));
                    purchase.put("email", results.getString("email"));
                    purchase.put("bookId", results.getInt("bookId"));
                    purchase.put("title", results.getString("title"));
                    purchase.put("price", results.getDouble("price"));
                    purchase.put("quantity", results.getInt("quantity"));
                    purchase.put("totalPrice", results.getDouble("totalPrice"));
                    purchase.put("purchaseDate", results.getTimestamp("purchaseDate"));
                    purchases.add(purchase);
                }
            }
        } catch (SQLException e) {
            System.err.println("Could not load the e-book library: " + e.getMessage());
        }
        return purchases;
    }

    public double getTotalSpentByEmail(String email) {
        String sql = "SELECT COALESCE(SUM(p.totalPrice), 0) AS total " +
                "FROM ebook_purchases p JOIN ebook_entitlements e ON e.purchaseId = p.id " +
                "WHERE e.email = ? COLLATE NOCASE";
        try (Connection conn = getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, email == null ? "" : email.trim());
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? results.getDouble("total") : 0.0;
            }
        } catch (SQLException e) {
            System.err.println("Could not calculate e-book purchases: " + e.getMessage());
            return 0.0;
        }
    }
}
