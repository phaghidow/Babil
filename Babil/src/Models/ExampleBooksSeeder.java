package Models;

import java.util.List;
import java.util.Map;

/**
 * ExampleBooksSeeder - Utility to seed example books into the SQLite database.
 */
public class ExampleBooksSeeder {

    public static void seedExampleBooks(String dbPath) {
        BookDAO dao = new BookDAO(dbPath);
        try {
            // Clear existing books
            dao.clearAllBooks();

            // Load example books from JSON
            List<Map<String, String>> exampleBooks = JsonToSqliteMigration.loadBooksFromJson("data/example_books.json");

            int count = 0;
            for (Map<String, String> book : exampleBooks) {
                int id = dao.addBook(book);
                if (id > 0) {
                    count++;
                }
            }
            System.out.println("Seeded " + count + " example books into SQLite database.");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to seed example books: " + e.getMessage());
        }
    }

    // Run seeding standalone if needed
    public static void main(String[] args) {
        String dbPath = "data/books.db";
        seedExampleBooks(dbPath);
    }
}
