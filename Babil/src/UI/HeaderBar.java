package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Professional HeaderBar component for Babil Bookstore
 * Contains: Logo, Category menu, Search bar, User icon
 */
public class HeaderBar extends JPanel {
    
    // Colors
    private static final Color HEADER_BG = Color.WHITE;
    private static final Color LOGO_COLOR = new Color(0x8A5A3B);
    private static final Color SEPARATOR_COLOR = new Color(0xD0D0D0);
    private static final Color SEARCH_BG = new Color(0xF5F5F5);
    private static final Color SEARCH_BORDER = new Color(0xE0E0E0);
    
    // Search field
    private JTextField searchField;
    private JButton searchButton;
    
    public HeaderBar() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 20, 10));
        setBackground(HEADER_BG);
        setPreferredSize(new Dimension(0, 70));
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, SEPARATOR_COLOR));
        
        // 1. Logo
        JLabel logoLabel = new JLabel("Babil");
        logoLabel.setFont(new Font("Serif", Font.BOLD, 22));
        logoLabel.setForeground(LOGO_COLOR);
        add(logoLabel);
        
        // 2. Categories Menu
        addCategoriesMenu();
        
        // 3. Search Bar
        addSearchBar();
        
        // 4. User Icon
        addUserIcon();
    }
    
    private void addCategoriesMenu() {
        JPanel categoriesPanel = new JPanel();
        categoriesPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        categoriesPanel.setBackground(HEADER_BG);
        
        String[] categories = {"All Books", "Design", "Programming", "CS"};
        
        for (int i = 0; i < categories.length; i++) {
            JLabel catLabel = new JLabel(categories[i]);
            catLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
            catLabel.setForeground(new Color(0x555555));
            catLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            // Hover effect
            catLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    catLabel.setForeground(LOGO_COLOR);
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    catLabel.setForeground(new Color(0x555555));
                }
            });
            
            categoriesPanel.add(catLabel);
            
            // Add separator between items (not after last)
            if (i < categories.length - 1) {
                JLabel separator = new JLabel("|");
                separator.setForeground(SEPARATOR_COLOR);
                separator.setFont(new Font("SansSerif", Font.PLAIN, 13));
                Dimension sepSize = separator.getPreferredSize();
                separator.setPreferredSize(new Dimension(sepSize.width + 10, sepSize.height));
                separator.setHorizontalAlignment(SwingConstants.CENTER);
                categoriesPanel.add(separator);
            }
        }
        
        add(categoriesPanel);
    }
    
    private void addSearchBar() {
        // Create a panel for search bar with rounded corners effect
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BorderLayout(5, 0));
        searchPanel.setBackground(SEARCH_BG);
        searchPanel.setBorder(BorderFactory.createLineBorder(SEARCH_BORDER, 1));
        
        // Search field
        searchField = new JTextField(15);
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 12));
        searchField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        searchField.setBackground(SEARCH_BG);
        searchField.setForeground(new Color(0x333333));
        
        // Placeholder effect
        searchField.setText("Search books...");
        searchField.setForeground(new Color(0x999999));
        
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("Search books...")) {
                    searchField.setText("");
                    searchField.setForeground(new Color(0x333333));
                }
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("Search books...");
                    searchField.setForeground(new Color(0x999999));
                }
            }
        });
        
        // Search button
        searchButton = new JButton("🔍");
        StyleManager.applyButtonStyle(searchButton);
        searchButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        searchButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        searchButton.setBackground(LOGO_COLOR);
        searchButton.setForeground(Color.WHITE);
        
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        
        // Wrap to apply rounded appearance
        JPanel searchWrapper = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SEARCH_BG);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(SEARCH_BORDER);
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            }
        };
        searchWrapper.setLayout(new BorderLayout());
        searchWrapper.setOpaque(false);
        searchWrapper.add(searchPanel);
        searchWrapper.setPreferredSize(new Dimension(250, 35));
        
        add(searchWrapper);
    }
    
    private void addUserIcon() {
        JButton userButton = new JButton("👤");
        StyleManager.applyButtonStyle(userButton);
        userButton.setFont(new Font("SansSerif", Font.PLAIN, 18));
        userButton.setPreferredSize(new Dimension(40, 40));
        userButton.setBackground(SEARCH_BG);
        userButton.setBorder(BorderFactory.createLineBorder(SEPARATOR_COLOR, 1));
        add(userButton);
    }
    
    public String getSearchText() {
        String text = searchField.getText();
        return text.equals("Search books...") ? "" : text;
    }
    
    public void addSearchActionListener(ActionListener listener) {
        searchButton.addActionListener(listener);
        searchField.addActionListener(listener);
    }
}
