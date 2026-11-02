package UI;

import Models.FavoritesManager;
import Models.BookDAO;
import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * BooksPage - Responsive grid of books with professional layout
 * Uses HeaderBar for navigation and ImageLoader for book covers
 * Cards: Image + Title + Author + Price + Rating + Description
 * Buttons: Add To Cart + Show More
 */
public class BooksPage extends JPanel {
    private BookDAO bookDAO;
    private java.util.Map<Integer, Integer> cartItems;
    private Runnable onBack;
    private Runnable showCart;
    private JLabel cartBadge;
    private JPanel gridContent;
    private HeaderBar headerBar;
    // Colors pulled from global Theme
    private static final Color BG = Theme.getBackground();
    private static final Color CARD_BG = Theme.getCardBackground();
    private static final Color PRIMARY = Theme.BUTTON;
    private static final Color TEXT_DARK = Theme.getTextColor();
    private static final Color TEXT_GRAY = new Color(0x888888);
    private static final Color SEPARATOR = Theme.getBorderColor();
    
    // Constructor with callback for showing cart
    public BooksPage(BookDAO bookDAO, Map<Integer, Integer> cartItems, Runnable onBack) {
        this(bookDAO, cartItems, onBack, null);
    }
    
    public BooksPage(BookDAO bookDAO, Map<Integer, Integer> cartItems, Runnable onBack, Runnable showCart) {
        this.bookDAO = bookDAO;
        this.cartItems = cartItems;
        this.onBack = onBack;
        this.showCart = showCart;
        initUI();
    }
    
    private void initUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG);
        
        // ===== HEADER BAR =====
        headerBar = new HeaderBar();
        headerBar.addSearchActionListener(e -> {
            String query = headerBar.getSearchText();
            if (!query.isEmpty()) {
                filterBooks(query);
            }
        });
        add(headerBar, BorderLayout.NORTH);
        
        // ===== TOP NAVIGATION BAR =====
        JPanel topBar = createTopBar();
        add(topBar, BorderLayout.PAGE_START);
        
        // ===== RESPONSIVE GRID CONTENT =====
        gridContent = new JPanel();
        updateGridLayout();
        gridContent.setBackground(BG);
        gridContent.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Load all books
        loadBooks();
        
        JScrollPane scrollPane = new JScrollPane(gridContent);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, SEPARATOR));
        topBar.setPreferredSize(new Dimension(0, 55));
        
        // Left: Back button
        JButton backBtn = new JButton("← Back");
        StyleManager.applyButtonStyle(backBtn);
        backBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        backBtn.setPreferredSize(new Dimension(100, 40));
        backBtn.addActionListener(e -> onBack.run());
        
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.add(backBtn);
        
        JLabel titleLabel = new JLabel("📚 Browse Books");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_DARK);
        leftPanel.add(titleLabel);
        
        topBar.add(leftPanel, BorderLayout.WEST);
        
        // Right: Cart badge
        cartBadge = new JLabel("0");
        cartBadge.setFont(new Font("SansSerif", Font.BOLD, 11));
        cartBadge.setForeground(Color.WHITE);
        cartBadge.setBackground(new Color(0xD32F2F));
        cartBadge.setOpaque(true);
        cartBadge.setHorizontalAlignment(JLabel.CENTER);
        cartBadge.setVerticalAlignment(JLabel.CENTER);
        cartBadge.setPreferredSize(new Dimension(24, 24));
        cartBadge.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        
        JButton cartBtn = new JButton("🛒 Cart");
        StyleManager.applyButtonStyle(cartBtn);
        // keep a lighter appearance for the cart button
        cartBtn.setBackground(new Color(0xF5F5F5));
        cartBtn.setForeground(TEXT_DARK);
        cartBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        cartBtn.setPreferredSize(new Dimension(90, 34));
        if (showCart != null) {
            cartBtn.addActionListener(e -> showCart.run());
        }
        
        JPanel cartPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        cartPanel.setBackground(Color.WHITE);
        cartPanel.add(cartBtn);
        cartPanel.add(cartBadge);
        
        topBar.add(cartPanel, BorderLayout.EAST);
        
        // Add Book button for user to add new books (coverUrl can be web URL)
        JButton addBookBtn = new JButton("+ Add Book");
        StyleManager.applyButtonStyle(addBookBtn);
        addBookBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        addBookBtn.setPreferredSize(new Dimension(110, 34));
        addBookBtn.addActionListener(e -> openAddBookDialog());
        cartPanel.add(addBookBtn);

        return topBar;
    }

    private void openAddBookDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New Book", true);
        dialog.setSize(480, 420);
        dialog.setLocationRelativeTo(this);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JTextField titleField = new JTextField();
        JTextField authorField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField stockField = new JTextField();
        JTextField coverField = new JTextField();

        content.add(new JLabel("Title:"));
        content.add(titleField);
        content.add(Box.createVerticalStrut(8));
        content.add(new JLabel("Author:"));
        content.add(authorField);
        content.add(Box.createVerticalStrut(8));
        content.add(new JLabel("Price (DA):"));
        content.add(priceField);
        content.add(Box.createVerticalStrut(8));
        content.add(new JLabel("Stock:"));
        content.add(stockField);
        content.add(Box.createVerticalStrut(8));
        content.add(new JLabel("Cover URL or local path:"));
        content.add(coverField);
        content.add(Box.createVerticalStrut(12));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelBtn = new JButton("Cancel");
        StyleManager.applyButtonStyle(cancelBtn);
        cancelBtn.addActionListener(e -> dialog.dispose());
        JButton saveBtn = new JButton("Save");
        StyleManager.applyButtonStyle(saveBtn);
        saveBtn.addActionListener(e -> {
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String price = priceField.getText().trim();
            String stock = stockField.getText().trim();
            String cover = coverField.getText().trim();

            if (title.isEmpty() || author.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Title and Author are required.", "Missing Data", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Map<String, String> book = new HashMap<>();
            book.put("title", title);
            book.put("author", author);
            book.put("price", price.isEmpty() ? "0" : price);
            book.put("stock", stock.isEmpty() ? "0" : stock);
            book.put("coverUrl", cover);

            int newId = bookDAO.addBook(book);
            if (newId > 0) {
                JOptionPane.showMessageDialog(dialog, "Book added with ID: " + newId, "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                // Reload books grid
                loadBooks();
                gridContent.revalidate();
                gridContent.repaint();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to add book.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnRow.add(cancelBtn);
        btnRow.add(saveBtn);
        content.add(btnRow);

        dialog.add(new JScrollPane(content));
        dialog.setVisible(true);
    }
    
    private void updateGridLayout() {
        gridContent.removeAll();
        // single column vertical list for clearer reading
        gridContent.setLayout(new GridLayout(0, 1, 15, 15));
    }
    
    private void loadBooks() {
        java.util.List<Map<String, String>> books = bookDAO.getAllBooks();
        for (Map<String, String> book : books) {
            JPanel card = createBookCard(book);
            gridContent.add(card);
        }
        updateCartBadge();
    }
    
    private void filterBooks(String query) {
        gridContent.removeAll();
        java.util.List<Map<String, String>> books = bookDAO.getAllBooks();
        
        String lowerQuery = query.toLowerCase();
        for (Map<String, String> book : books) {
            String title = book.getOrDefault("title", "").toLowerCase();
            String author = book.getOrDefault("author", "").toLowerCase();
            
            if (title.contains(lowerQuery) || author.contains(lowerQuery)) {
                JPanel card = createBookCard(book);
                gridContent.add(card);
            }
        }
        
        gridContent.revalidate();
        gridContent.repaint();
    }
    
    private JPanel createBookCard(Map<String, String> book) {
        int bookId = Integer.parseInt(book.getOrDefault("id", "0"));
        String imagePath = book.getOrDefault("coverUrl", "");
        
        // Card container with drop shadow effect
        JPanel cardOuter = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Drop shadow
                g2.setColor(new Color(0, 0, 0, 15));
                g2.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 8, 8);
                
                // Card background
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 8, 8);
                
                // Border
                g2.setColor(SEPARATOR);
                g2.setStroke(new BasicStroke(0.5f));
                g2.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 8, 8);
            }
        };
        
        cardOuter.setLayout(new BorderLayout());
        cardOuter.setOpaque(false);
        cardOuter.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        cardOuter.setPreferredSize(new Dimension(200, 350));
        
        // Inner card with padding
        JPanel cardInner = new JPanel();
        cardInner.setLayout(new BoxLayout(cardInner, BoxLayout.Y_AXIS));
        cardInner.setBackground(CARD_BG);
        cardInner.setOpaque(false);
        
        // Image
        ImageIcon icon = ImageLoader.getBookImage(imagePath, 140, 180);
        JLabel imgLabel = new JLabel(icon);
        // subtle image border to match theme
        imgLabel.setBorder(BorderFactory.createLineBorder(Theme.getBorderColor(), 1));
        imgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardInner.add(imgLabel);
        
        cardInner.add(Box.createVerticalStrut(10));
        
        // Title
        String title = book.getOrDefault("title", "Unknown");
        JLabel titleLabel = new JLabel(
            "<html><div width='150'><b>" + title + "</b></div></html>"
        );
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        titleLabel.setForeground(TEXT_DARK);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardInner.add(titleLabel);
        
        // Author
        String author = book.getOrDefault("author", "Unknown");
        JLabel authorLabel = new JLabel(author);
        authorLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        authorLabel.setForeground(TEXT_GRAY);
        authorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardInner.add(authorLabel);
        
        cardInner.add(Box.createVerticalStrut(6));
        
        // Price and Rating
        String price = book.getOrDefault("price", "0");
        String rating = book.getOrDefault("rating", "0");
        JLabel priceRatingLabel = new JLabel("DA " + price + " ⭐ " + rating);
        priceRatingLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        priceRatingLabel.setForeground(PRIMARY);
        priceRatingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardInner.add(priceRatingLabel);
        
        cardInner.add(Box.createVerticalGlue());
        
        // Buttons
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 3, 8, 0));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        
        JButton addCartBtn = createSmallButton("Add Cart", bookId, title);
        JButton showMoreBtn = createSmallButton("Details", 0, book.getOrDefault("summary", "No details"));
        showMoreBtn.addActionListener(e -> showBookDetails(book));

        // Favorite button
        JButton favBtn = new JButton(FavoritesManager.isFavorite(bookId) ? "❤️" : "🤍");
        StyleManager.applyButtonStyle(favBtn);
        favBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        favBtn.setPreferredSize(new Dimension(56, 30));
        favBtn.addActionListener(e -> {
            FavoritesManager.toggleFavorite(bookId);
            favBtn.setText(FavoritesManager.isFavorite(bookId) ? "❤️" : "🤍");
        });
        
        buttonsPanel.add(addCartBtn);
        buttonsPanel.add(showMoreBtn);
        buttonsPanel.add(favBtn);
        cardInner.add(buttonsPanel);
        
        cardOuter.add(cardInner, BorderLayout.CENTER);
        
        return cardOuter;
    }
    
    private JButton createSmallButton(String text, int bookId, String data) {
        JButton btn = new JButton(text);
        StyleManager.applyButtonStyle(btn);
        btn.setFont(new Font("SansSerif", Font.BOLD, 10));
        btn.setPreferredSize(new Dimension(80, 30));
        
        if ("Add Cart".equals(text) && bookId > 0) {
            btn.addActionListener(e -> {
                int qty = cartItems.getOrDefault(bookId, 0) + 1;
                cartItems.put(bookId, qty);
                // Persist cart status to DB
                try {
                    bookDAO.updateCartStatus(bookId, true);
                } catch (Exception ex) {
                    System.err.println("Failed to update cart status: " + ex.getMessage());
                }
                updateCartBadge();
            });
        }
        
        return btn;
    }
    
    // Hover behaviour now provided by StyleManager; helper removed.
    
    private void updateCartBadge() {
        int total = cartItems.values().stream().mapToInt(Integer::intValue).sum();
        cartBadge.setText(String.valueOf(total));
        cartBadge.setVisible(total > 0);
    }
    
    private void showBookDetails(Map<String, String> book) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Book Details", true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        
        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBackground(BG);
        content.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        
        // Title
        JLabel titleLabel = new JLabel(book.getOrDefault("title", ""));
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_DARK);
        content.add(titleLabel, BorderLayout.NORTH);
        
        // Details
        String summary = book.getOrDefault("summary", "No description available");
        JTextArea descArea = new JTextArea(summary);
        descArea.setWrapStyleWord(true);
        descArea.setLineWrap(true);
        descArea.setEditable(false);
        descArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        descArea.setBackground(BG);
        descArea.setForeground(TEXT_DARK);
        
        JScrollPane scrollPane = new JScrollPane(descArea);
        content.add(scrollPane, BorderLayout.CENTER);
        
        // Close button
        JButton closeBtn = new JButton("Close");
        StyleManager.applyButtonStyle(closeBtn);
        closeBtn.setBackground(PRIMARY);
        closeBtn.addActionListener(e -> dialog.dispose());
        content.add(closeBtn, BorderLayout.SOUTH);
        
        dialog.add(content);
        dialog.setVisible(true);
    }
    
    public Map<Integer, Integer> getCartItems() {
        return cartItems;
    }
}
