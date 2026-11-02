package Models;

import java.sql.*;
import java.util.*;

/**
 * BookDAO - Data Access Object for SQLite database operations
 * Manages book records with methods for CRUD operations and stock management
 */
public class BookDAO {
    private String dbPath;
    private static final String DB_URL_PREFIX = "jdbc:sqlite:";

    /**
     * Constructor - Initialize DAO with database path
     * @param dbPath Path to SQLite database file (e.g., "data/books.db")
     */
    public BookDAO(String dbPath) {
        this.dbPath = dbPath;
        initializeDatabase();
    }

    /**
     * Initialize database: create connection and ensure books table exists
     */
    private void initializeDatabase() {
        String createTableSQL =
            "CREATE TABLE IF NOT EXISTS books (" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  coverUrl TEXT NOT NULL," +
            "  title TEXT NOT NULL," +
            "  author TEXT NOT NULL," +
            "  price REAL NOT NULL," +
            "  rating REAL DEFAULT 0," +
            "  language TEXT," +
            "  summary TEXT," +
            "  shortDescription TEXT," +
            "  pages INTEGER," +
            "  publisher TEXT," +
            "  type TEXT," +
            "  keywords TEXT," +
            "  stock INTEGER DEFAULT 0," +
            "  added_to_cart INTEGER DEFAULT 0," +
            "  ebookPath TEXT NOT NULL DEFAULT ''" +
            ")";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createTableSQL);
            ensureEbookPathColumn(conn);
            System.out.println("Database initialized successfully at: " + dbPath);
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    private void ensureEbookPathColumn(Connection conn) throws SQLException {
        boolean columnExists = false;
        try (Statement statement = conn.createStatement();
             ResultSet columns = statement.executeQuery("PRAGMA table_info(books)")) {
            while (columns.next()) {
                if ("ebookPath".equalsIgnoreCase(columns.getString("name"))) {
                    columnExists = true;
                    break;
                }
            }
        }
        if (!columnExists) {
            try (Statement statement = conn.createStatement()) {
                statement.executeUpdate(
                        "ALTER TABLE books ADD COLUMN ebookPath TEXT NOT NULL DEFAULT ''");
            }
        }
    }

    /**
     * Get database connection
     * @return SQLite database connection
     * @throws SQLException if connection fails
     */
    private Connection getConnection() throws SQLException {
        try {
            // Try to load the SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            // Fallback: Try direct connection - newer SQLite JDBC auto-registers driver
            System.err.println("Warning: org.sqlite.JDBC not found, attempting direct connection");
        }

        try {
            return DriverManager.getConnection(DB_URL_PREFIX + dbPath);
        } catch (SQLException e) {
            System.err.println("Failed to connect to database at " + dbPath + ": " + e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieve all books from database
     * @return List of maps containing book data
     */
    public List<Map<String, String>> getAllBooks() {
        List<Map<String, String>> books = new ArrayList<>();
        String query = "SELECT * FROM books ORDER BY id";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Map<String, String> book = new HashMap<>();
                book.put("id", String.valueOf(rs.getInt("id")));
                book.put("coverUrl", rs.getString("coverUrl"));
                book.put("title", rs.getString("title"));
                book.put("author", rs.getString("author"));
                book.put("price", String.valueOf(rs.getDouble("price")));
                book.put("rating", String.valueOf(rs.getDouble("rating")));
                book.put("language", rs.getString("language"));
                book.put("summary", rs.getString("summary"));
                book.put("pages", String.valueOf(rs.getInt("pages")));
                book.put("publisher", rs.getString("publisher"));
                book.put("type", rs.getString("type"));
                book.put("keywords", rs.getString("keywords"));
                book.put("stock", String.valueOf(rs.getInt("stock")));
                book.put("added_to_cart", rs.getInt("added_to_cart") == 1 ? "true" : "false");
                book.put("ebookPath", rs.getString("ebookPath"));
                books.add(book);
            }
            System.out.println("Retrieved " + books.size() + " books from database");
        } catch (SQLException e) {
            System.err.println("Error retrieving all books: " + e.getMessage());
        }

        return books;
    }

    /**
     * Retrieve a specific book by ID
     * @param bookId ID of the book to retrieve
     * @return Map containing book data, or empty map if not found
     */
    public Map<String, String> getBookById(int bookId) {
        Map<String, String> book = new HashMap<>();
        String query = "SELECT * FROM books WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, bookId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    book.put("id", String.valueOf(rs.getInt("id")));
                    book.put("coverUrl", rs.getString("coverUrl"));
                    book.put("title", rs.getString("title"));
                    book.put("author", rs.getString("author"));
                    book.put("price", String.valueOf(rs.getDouble("price")));
                    book.put("rating", String.valueOf(rs.getDouble("rating")));
                    book.put("language", rs.getString("language"));
                    book.put("summary", rs.getString("summary"));
                    book.put("pages", String.valueOf(rs.getInt("pages")));
                    book.put("publisher", rs.getString("publisher"));
                    book.put("type", rs.getString("type"));
                    book.put("keywords", rs.getString("keywords"));
                    book.put("stock", String.valueOf(rs.getInt("stock")));
                    book.put("added_to_cart", rs.getInt("added_to_cart") == 1 ? "true" : "false");
                    book.put("ebookPath", rs.getString("ebookPath"));
                    System.out.println("Retrieved book: " + rs.getString("title"));
                } else {
                    System.out.println("Book with ID " + bookId + " not found");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving book by ID: " + e.getMessage());
        }

        return book;
    }

    /**
     * Add a new book to the database
     * @param book Map containing book data (required: coverUrl, title, author, price; optional: other fields)
     * @return ID of newly inserted book, or -1 if failed
     */
    public int addBook(Map<String, String> book) {
        String insertSQL =
            "INSERT INTO books (coverUrl, title, author, price, rating, language, summary, pages, publisher, type, keywords, stock, added_to_cart, ebookPath) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, book.getOrDefault("coverUrl", ""));
            pstmt.setString(2, book.getOrDefault("title", ""));
            pstmt.setString(3, book.getOrDefault("author", ""));
            pstmt.setDouble(4, parseDouble(book.getOrDefault("price", "0")));
            pstmt.setDouble(5, parseDouble(book.getOrDefault("rating", "0")));
            pstmt.setString(6, book.getOrDefault("language", ""));
            pstmt.setString(7, book.getOrDefault("summary", ""));
            pstmt.setInt(8, parseInt(book.getOrDefault("pages", "0")));
            pstmt.setString(9, book.getOrDefault("publisher", ""));
            pstmt.setString(10, book.getOrDefault("type", "Other"));
            pstmt.setString(11, book.getOrDefault("keywords", ""));
            pstmt.setInt(12, parseInt(book.getOrDefault("stock", "0")));
            pstmt.setInt(13, "true".equalsIgnoreCase(book.getOrDefault("added_to_cart", "false")) ? 1 : 0);
            pstmt.setString(14, book.getOrDefault("ebookPath", ""));

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int bookId = generatedKeys.getInt(1);
                        System.out.println("Book added successfully with ID: " + bookId);
                        return bookId;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding book: " + e.getMessage());
        }

        return -1;
    }

    /**
     * Update book stock quantity
     * @param bookId ID of the book to update
     * @param newStock New stock quantity
     * @return true if update successful, false otherwise
     */
    public boolean updateStock(int bookId, int newStock) {
        String updateSQL = "UPDATE books SET stock = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {

            pstmt.setInt(1, newStock);
            pstmt.setInt(2, bookId);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Stock updated for book ID " + bookId + " to " + newStock);
                return true;
            } else {
                System.out.println("No book found with ID " + bookId);
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error updating stock: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update "added_to_cart" status for a book
     * @param bookId ID of the book
     * @param addedToCart true if added to cart, false otherwise
     * @return true if update successful, false otherwise
     */
    public boolean updateCartStatus(int bookId, boolean addedToCart) {
        String updateSQL = "UPDATE books SET added_to_cart = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {

            pstmt.setInt(1, addedToCart ? 1 : 0);
            pstmt.setInt(2, bookId);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Cart status updated for book ID " + bookId);
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error updating cart status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Search books by query across multiple fields
     * @param query Search term (searches title, author, keywords, type)
     * @return List of matching books
     */
    public List<Map<String, String>> searchBooks(String query) {
        List<Map<String, String>> results = new ArrayList<>();
        String searchSQL =
            "SELECT * FROM books WHERE " +
            "title LIKE ? OR author LIKE ? OR keywords LIKE ? OR type LIKE ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(searchSQL)) {

            String queryParam = "%" + query + "%";
            pstmt.setString(1, queryParam);
            pstmt.setString(2, queryParam);
            pstmt.setString(3, queryParam);
            pstmt.setString(4, queryParam);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> book = new HashMap<>();
                    book.put("id", String.valueOf(rs.getInt("id")));
                    book.put("coverUrl", rs.getString("coverUrl"));
                    book.put("title", rs.getString("title"));
                    book.put("author", rs.getString("author"));
                    book.put("price", String.valueOf(rs.getDouble("price")));
                    book.put("rating", String.valueOf(rs.getDouble("rating")));
                    book.put("language", rs.getString("language"));
                    book.put("summary", rs.getString("summary"));
                    book.put("pages", String.valueOf(rs.getInt("pages")));
                    book.put("publisher", rs.getString("publisher"));
                    book.put("type", rs.getString("type"));
                    book.put("keywords", rs.getString("keywords"));
                    book.put("stock", String.valueOf(rs.getInt("stock")));
                    book.put("added_to_cart", rs.getInt("added_to_cart") == 1 ? "true" : "false");
                    book.put("ebookPath", rs.getString("ebookPath"));
                    results.add(book);
                }
                System.out.println("Found " + results.size() + " books matching: " + query);
            }
        } catch (SQLException e) {
            System.err.println("Error searching books: " + e.getMessage());
        }

        return results;
    }

    /**
     * Delete a book from the database
     * @param bookId ID of the book to delete
     * @return true if deletion successful, false otherwise
     */
    public boolean deleteBook(int bookId) {
        String deleteSQL = "DELETE FROM books WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {

            pstmt.setInt(1, bookId);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Book ID " + bookId + " deleted successfully");
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error deleting book: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete all books from the database
     */
    public void clearAllBooks() {
        String deleteAllSQL = "DELETE FROM books";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            int rowsDeleted = stmt.executeUpdate(deleteAllSQL);
            System.out.println("Deleted " + rowsDeleted + " books from the database.");
        } catch (SQLException e) {
            System.err.println("Error clearing all books: " + e.getMessage());
        }
    }

    /**
     * Get all books with specific type
     * @param type Book type to filter by
     * @return List of books of that type
     */
    public List<Map<String, String>> getBooksByType(String type) {
        List<Map<String, String>> books = new ArrayList<>();
        String query = "SELECT * FROM books WHERE type = ? ORDER BY id";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, type);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> book = new HashMap<>();
                    book.put("id", String.valueOf(rs.getInt("id")));
                    book.put("coverUrl", rs.getString("coverUrl"));
                    book.put("title", rs.getString("title"));
                    book.put("author", rs.getString("author"));
                    book.put("price", String.valueOf(rs.getDouble("price")));
                    book.put("rating", String.valueOf(rs.getDouble("rating")));
                    book.put("language", rs.getString("language"));
                    book.put("summary", rs.getString("summary"));
                    book.put("pages", String.valueOf(rs.getInt("pages")));
                    book.put("publisher", rs.getString("publisher"));
                    book.put("type", rs.getString("type"));
                    book.put("keywords", rs.getString("keywords"));
                    book.put("stock", String.valueOf(rs.getInt("stock")));
                    book.put("added_to_cart", rs.getInt("added_to_cart") == 1 ? "true" : "false");
                    book.put("ebookPath", rs.getString("ebookPath"));
                    books.add(book);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving books by type: " + e.getMessage());
        }

        return books;
    }

    /**
     * Helper method to safely parse double values
     */
    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Helper method to safely parse integer values
     */
    private int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
