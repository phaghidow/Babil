import java.awt.*;
import java.util.*;
import javax.swing.*;
import UI.*;
import Models.*;

public class App {
    private static JFrame mainFrame;
    private static CardLayout cardLayout;
    private static JPanel cardPanel;
    private static Map<Integer, Integer> cartItems = new HashMap<>();
    
    public static void main(String[] args) throws Exception {
        final String dbPath = "data/books.db";
        final BookDAO bookDAO = new BookDAO(dbPath);
        final PurchaseDAO purchaseDAO = new PurchaseDAO(dbPath);
        
        java.util.List<java.util.Map<String, String>> booksFromDb = bookDAO.getAllBooks();
        
        if (booksFromDb.isEmpty()) {
            System.out.println("Migrating books...");
            JsonToSqliteMigration.migrateBooks("data/books.json", dbPath);
            booksFromDb = bookDAO.getAllBooks();
        }
        
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
        cardPanel.setBackground(new Color(0xF4F4F4));
        
        HomePage homePage = new HomePage(() -> showPage("books"), () -> showPage("cart"));
        BooksPage booksPage = new BooksPage(bookDAO, cartItems, () -> showPage("home"));
        CartPage cartPage = new CartPage(bookDAO, purchaseDAO, cartItems, () -> showPage("home"));
        
        cardPanel.add(homePage, "home");
        cardPanel.add(booksPage, "books");
        cardPanel.add(cartPage, "cart");
        
        mainFrame.add(cardPanel, BorderLayout.CENTER);
        mainFrame.setVisible(true);
    }
    
    private static void showPage(String pageName) {
        cardLayout.show(cardPanel, pageName);
    }
}
