import java.awt.*;
import java.util.*;
import javax.swing.*;
import UI.*;
import Models.*;

public class App {
    private static JFrame mainFrame;
    private static CardLayout cardLayout;
    private static JPanel cardPanel;
    private static final Map<Integer, Integer> cartItems = new HashMap<>();
    
    public static void main(String[] args) throws Exception {
        final String dbPath = "data/books.db";
        final BookDAO bookDAO = new BookDAO(dbPath);
        final PurchaseDAO purchaseDAO = new PurchaseDAO(dbPath);
        
        java.util.List<java.util.Map<String, String>> booksFromDb = bookDAO.getAllBooks();
        
        if (booksFromDb.isEmpty()) {
            System.out.println("Migrating books...");
            String catalogPath = java.nio.file.Files.isRegularFile(
                    java.nio.file.Path.of("data/books.json"))
                    ? "data/books.json" : "data/example_books.json";
            JsonToSqliteMigration.migrateBooks(catalogPath, dbPath);
            booksFromDb = bookDAO.getAllBooks();
        }

        DemoEbookSeeder.seedShowcaseIfNeeded(bookDAO);
        PortfolioCatalogSeeder.seedIfNeeded(bookDAO);
        booksFromDb = bookDAO.getAllBooks();
        System.out.println("Loaded " + booksFromDb.size() + " books");
        SwingUtilities.invokeLater(() -> createMainWindow(bookDAO, purchaseDAO));
    }
    
    private static void createMainWindow(BookDAO bookDAO, PurchaseDAO purchaseDAO) {
        mainFrame = new JFrame("Babil Bookstore");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1000, 700);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(Theme.getBackground());
        cardPanel.add(new BookstorePage(bookDAO, purchaseDAO, cartItems), "store");
        
        mainFrame.add(cardPanel, BorderLayout.CENTER);
        cardLayout.show(cardPanel, "store");
        mainFrame.setVisible(true);
    }

    private static void showPage(String pageName) {
        cardLayout.show(cardPanel, pageName);
    }
}
