package UI;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.regex.Pattern;
import Models.BookDAO;
import Models.PurchaseDAO;

/**
 * CartPage - Panier avec gestion des quantités et achat
 * Permet modifier qty (+/-), supprimer, entrer email et acheter
 */
public class CartPage extends JPanel {
    private BookDAO bookDAO;
    private PurchaseDAO purchaseDAO;
    private Map<Integer, Integer> cartItems;
    private Runnable onBack;
    
    // Couleurs
    private static final Color COLOR_LIGHT = new Color(0xF4F4F4);
    private static final Color COLOR_BUTTON = new Color(0x8C715C);
    private static final Color COLOR_DARK = new Color(0x3C3C3C);
    private static final Color COLOR_WHITE = Color.WHITE;
    private static final Color COLOR_SUCCESS = new Color(0x4CAF50);
    private static final Color COLOR_ERROR = new Color(0xF44336);
    
    private JPanel itemsPanel;
    private JLabel totalLabel;
    private JTextField emailField;
    
    public CartPage(BookDAO bookDAO, PurchaseDAO purchaseDAO, Map<Integer, Integer> cartItems, Runnable onBack) {
        this.bookDAO = bookDAO;
        this.purchaseDAO = purchaseDAO;
        this.cartItems = cartItems;
        this.onBack = onBack;
        // If caller didn't pass any cart items, load persisted cart flags from DB
        if (this.cartItems == null) this.cartItems = new HashMap<>();
        if (this.cartItems.isEmpty()) {
            loadCartFromDB();
        }
        initUI();
    }

    /**
     * Populate `cartItems` map from books persisted with added_to_cart flag.
     */
    private void loadCartFromDB() {
        try {
            java.util.List<Map<String, String>> books = bookDAO.getAllBooks();
            for (Map<String, String> book : books) {
                String idStr = book.getOrDefault("id", "0");
                String added = book.getOrDefault("added_to_cart", "0");
                try {
                    int id = Integer.parseInt(idStr);
                    if ("1".equals(added) || "true".equalsIgnoreCase(added)) {
                        // default quantity 1 for persisted cart items
                        cartItems.put(id, cartItems.getOrDefault(id, 0) + 1);
                    }
                } catch (NumberFormatException ignored) { }
            }
        } catch (Exception ex) {
            System.err.println("Failed to load cart from DB: " + ex.getMessage());
        }
    }
    
    private void initUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(COLOR_LIGHT);
        
        // ===== HEADER =====
        JPanel header = createHeader();
        add(header, BorderLayout.NORTH);
        
        // ===== CONTENU PRINCIPAL =====
        JPanel mainContent = new JPanel(new BorderLayout(12, 12));
        mainContent.setBackground(COLOR_LIGHT);
        mainContent.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        
        // Liste des articles
        itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBackground(COLOR_WHITE);
        itemsPanel.setBorder(BorderFactory.createLineBorder(new Color(0xE0E0E0), 1));
        
        if (cartItems.isEmpty()) {
            JLabel emptyLabel = new JLabel("Your cart is empty. Continue shopping!");
            emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            emptyLabel.setForeground(new Color(0x999999));
            emptyLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            itemsPanel.add(emptyLabel);
        } else {
            for (Map.Entry<Integer, Integer> entry : cartItems.entrySet()) {
                JPanel itemRow = createCartItemRow(entry.getKey(), entry.getValue());
                itemsPanel.add(itemRow);
            }
        }
        
        JScrollPane scrollPane = new JScrollPane(itemsPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().setBackground(COLOR_WHITE);
        mainContent.add(scrollPane, BorderLayout.CENTER);
        
        // ===== TOTAL ET EMAIL =====
        JPanel checkoutPanel = createCheckoutPanel();
        mainContent.add(checkoutPanel, BorderLayout.SOUTH);
        
        add(mainContent, BorderLayout.CENTER);
    }
    
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_WHITE);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE0E0E0)));
        header.setPreferredSize(new Dimension(0, 60));
        
        JButton backBtn = new JButton("← Back");
        StyleManager.applyButtonStyle(backBtn);
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        backBtn.setPreferredSize(new Dimension(100, 40));
        backBtn.addActionListener(e -> onBack.run());
        header.add(backBtn, BorderLayout.WEST);
        
        JLabel titleLabel = new JLabel("Your Shopping Cart");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(COLOR_DARK);
        header.add(titleLabel, BorderLayout.CENTER);
        
        return header;
    }
    
    private JPanel createCartItemRow(int bookId, int quantity) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(COLOR_WHITE);
        row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE0E0E0)));
        row.setPreferredSize(new Dimension(0, 60));
        row.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        
        // Récupérer les infos du livre
        Map<String, String> book = getBookById(bookId);
        String title = book.getOrDefault("title", "Unknown");
        double price = Double.parseDouble(book.getOrDefault("price", "0"));
        
        // Infos livre
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(COLOR_WHITE);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(COLOR_DARK);
        infoPanel.add(titleLabel, BorderLayout.WEST);
        
        JLabel priceLabel = new JLabel(String.format("DA %.2f", price));
        priceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        priceLabel.setForeground(COLOR_BUTTON);
        infoPanel.add(priceLabel, BorderLayout.EAST);
        
        row.add(infoPanel, BorderLayout.CENTER);
        
        // Quantité
        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        qtyPanel.setBackground(COLOR_WHITE);
        
        JButton minusBtn = new JButton("−");
        StyleManager.applyButtonStyle(minusBtn);
        minusBtn.setFont(new Font("Arial", Font.BOLD, 14));
        minusBtn.setPreferredSize(new Dimension(30, 30));
        minusBtn.addActionListener(e -> {
            int current = cartItems.getOrDefault(bookId, 0);
            if (current > 1) {
                cartItems.put(bookId, current - 1);
                try { bookDAO.updateCartStatus(bookId, true); } catch (Exception ex) { }
            } else {
                // Remove if going to zero
                cartItems.remove(bookId);
                try { bookDAO.updateCartStatus(bookId, false); } catch (Exception ex) { }
            }
            refreshUI();
        });
        
        JLabel qtyLabel = new JLabel(String.valueOf(quantity));
        qtyLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        qtyLabel.setPreferredSize(new Dimension(30, 30));
        qtyLabel.setHorizontalAlignment(JLabel.CENTER);
        
        JButton plusBtn = new JButton("+");
        StyleManager.applyButtonStyle(plusBtn);
        plusBtn.setFont(new Font("Arial", Font.BOLD, 14));
        plusBtn.setPreferredSize(new Dimension(30, 30));
        plusBtn.addActionListener(e -> {
            int current = cartItems.getOrDefault(bookId, 0);
            cartItems.put(bookId, current + 1);
            try { bookDAO.updateCartStatus(bookId, true); } catch (Exception ex) { }
            refreshUI();
        });
        
        qtyPanel.add(minusBtn);
        qtyPanel.add(qtyLabel);
        qtyPanel.add(plusBtn);
        row.add(qtyPanel, BorderLayout.EAST);
        
        // Remove button
        JButton removeBtn = new JButton("✕");
        StyleManager.applyButtonStyle(removeBtn);
        removeBtn.setFont(new Font("Arial", Font.BOLD, 14));
        removeBtn.setForeground(COLOR_ERROR);
        removeBtn.setPreferredSize(new Dimension(35, 35));
        removeBtn.addActionListener(e -> {
            cartItems.remove(bookId);
            try { bookDAO.updateCartStatus(bookId, false); } catch (Exception ex) { }
            refreshUI();
        });
        
        JPanel actionPanel = new JPanel();
        actionPanel.setBackground(COLOR_WHITE);
        actionPanel.add(removeBtn);
        row.add(actionPanel, BorderLayout.WEST);
        
        return row;
    }
    
    private JPanel createCheckoutPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(COLOR_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        
        // Total
        JPanel totalPanel = new JPanel(new BorderLayout());
        totalPanel.setBackground(COLOR_WHITE);
        totalPanel.setBorder(BorderFactory.createLineBorder(new Color(0xE0E0E0), 1));
        totalPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        
        JLabel totalTextLabel = new JLabel("Total: ");
        totalTextLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalLabel = new JLabel("DA 0.00");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalLabel.setForeground(COLOR_BUTTON);
        totalPanel.add(totalTextLabel, BorderLayout.WEST);
        totalPanel.add(totalLabel, BorderLayout.EAST);
        
        panel.add(totalPanel, BorderLayout.NORTH);
        
        // Email et bouton Buy
        JPanel checkoutPanel = new JPanel(new BorderLayout(12, 0));
        checkoutPanel.setBackground(COLOR_LIGHT);
        
        JLabel emailLabelText = new JLabel("Email: ");
        emailLabelText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        emailField = new JTextField(20);
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        emailField.setBorder(BorderFactory.createLineBorder(new Color(0xE0E0E0), 1));
        emailField.setMargin(new Insets(6, 6, 6, 6));
        
        JPanel emailPanel = new JPanel(new BorderLayout(5, 0));
        emailPanel.setBackground(COLOR_LIGHT);
        emailPanel.add(emailLabelText, BorderLayout.WEST);
        emailPanel.add(emailField, BorderLayout.CENTER);
        
        checkoutPanel.add(emailPanel, BorderLayout.CENTER);
        
        JButton buyBtn = new JButton("Buy");
        StyleManager.applyButtonStyle(buyBtn);
        buyBtn.setBackground(COLOR_SUCCESS);
        buyBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        buyBtn.setPreferredSize(new Dimension(100, 40));
        buyBtn.addActionListener(e -> handleBuyClick());
        
        checkoutPanel.add(buyBtn, BorderLayout.EAST);
        panel.add(checkoutPanel, BorderLayout.SOUTH);
        
        updateTotal();
        return panel;
    }
    
    private void handleBuyClick() {
        String email = emailField.getText().trim();
        
        // Valider email
        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a valid email address.", 
                "Invalid Email", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Si panier vide
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Your cart is empty.", 
                "Empty Cart", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Validate stock for each requested item before recording purchases
        for (Map.Entry<Integer, Integer> entry : cartItems.entrySet()) {
            int bookId = entry.getKey();
            int quantity = entry.getValue();
            Map<String, String> book = getBookById(bookId);
            int currentStock = 0;
            try {
                currentStock = Integer.parseInt(book.getOrDefault("stock", "0"));
            } catch (NumberFormatException ex) {
                currentStock = 0;
            }
            if (quantity > currentStock) {
                JOptionPane.showMessageDialog(this,
                    String.format("Not enough stock for '%s' (requested %d, available %d).", book.getOrDefault("title", "Unknown"), quantity, currentStock),
                    "Insufficient Stock",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // Enregistrer les achats (all validated)
        for (Map.Entry<Integer, Integer> entry : cartItems.entrySet()) {
            int bookId = entry.getKey();
            int quantity = entry.getValue();
            Map<String, String> book = getBookById(bookId);
            String title = book.getOrDefault("title", "Unknown");
            double price = Double.parseDouble(book.getOrDefault("price", "0"));

            purchaseDAO.recordPurchase(email, bookId, title, price, quantity);
            // Decrease stock and clear added_to_cart flag
            try {
                int currentStock = Integer.parseInt(book.getOrDefault("stock", "0"));
                int newStock = Math.max(0, currentStock - quantity);
                bookDAO.updateStock(bookId, newStock);
                bookDAO.updateCartStatus(bookId, false);
            } catch (Exception ex) {
                System.err.println("Failed to update stock/cart status for book " + bookId + ": " + ex.getMessage());
            }
        }
        
        // Afficher confirmation
        JDialog confirmDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Purchase Confirmation", true);
        confirmDialog.setSize(400, 250);
        confirmDialog.setLocationRelativeTo(this);
        
        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBackground(COLOR_LIGHT);
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel successLabel = new JLabel("✓ Thank you for your purchase!");
        successLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        successLabel.setForeground(COLOR_SUCCESS);
        content.add(successLabel, BorderLayout.NORTH);
        
        JLabel emailLabel = new JLabel("Confirmation email sent to: " + email);
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        content.add(emailLabel, BorderLayout.CENTER);
        
        JButton okBtn = new JButton("OK");
        StyleManager.applyButtonStyle(okBtn);
        okBtn.addActionListener(e -> {
            confirmDialog.dispose();
            cartItems.clear();
            refreshUI();
        });
        content.add(okBtn, BorderLayout.SOUTH);
        
        confirmDialog.add(content);
        confirmDialog.setVisible(true);
    }
    
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return Pattern.matches(emailRegex, email);
    }
    
    private void updateTotal() {
        double total = 0;
        for (Map.Entry<Integer, Integer> entry : cartItems.entrySet()) {
            Map<String, String> book = getBookById(entry.getKey());
            double price = Double.parseDouble(book.getOrDefault("price", "0"));
            total += price * entry.getValue();
        }
        if (totalLabel != null) {
            totalLabel.setText(String.format("DA %.2f", total));
        }
    }
    
    private void refreshUI() {
        removeAll();
        initUI();
        revalidate();
        repaint();
    }
    
    private Map<String, String> getBookById(int bookId) {
        java.util.List<Map<String, String>> books = bookDAO.getAllBooks();
        for (Map<String, String> book : books) {
            if (Integer.parseInt(book.getOrDefault("id", "0")) == bookId) {
                return book;
            }
        }
        return new HashMap<>();
    }
}
