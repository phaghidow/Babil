package UI;

import java.awt.*;
import javax.swing.*;

/**
 * HomePage - Page d'accueil Babil Bookstore minimaliste
 */
public class HomePage extends JPanel {
    private final Runnable onExplore;
    private final Runnable onCart;
    // Colors now come from Theme
    
    private JPanel contentPanel;
    private boolean isDark = false;
    
    public HomePage(Runnable onExplore, Runnable onCart) {
        this.onExplore = onExplore;
        this.onCart = onCart;
        initUI();
    }
    
    private void initUI() {
        setLayout(new BorderLayout(0, 0));
        
        // Contenu centré
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(72, 16, 72, 16));
        
        // Logo/Titre
        JLabel logoLabel = new JLabel("🔖");
        logoLabel.setFont(new Font("Arial", Font.BOLD, 60));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(logoLabel);
        
        contentPanel.add(Box.createVerticalStrut(20));
        
        // Titre principal
        JLabel titleLabel = new JLabel("BABIL");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(titleLabel);
        
        contentPanel.add(Box.createVerticalStrut(10));
        
        // Sous-titre
        JLabel subtitleLabel = new JLabel("Discover Your Next Favorite Book");
        subtitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(subtitleLabel);
        
        contentPanel.add(Box.createVerticalStrut(30));
        
        // Description
        JLabel descLabel = new JLabel(
            "<html><div style='text-align: center; width: 450px;'>" +
            "Welcome to Babil Bookstore, your gateway to infinite worlds and stories.<br>" +
            "<br>" +
            "Browse thousands of books, explore new genres, and find your next favorite read.<br>" +
            "Shop with ease and build your personal library today." +
            "</div></html>"
        );
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(descLabel);
        
        contentPanel.add(Box.createVerticalStrut(50));
        
        // Boutons
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 0));
        
        JButton exploreBtn = createButton("🔍 Explore Books");
        exploreBtn.addActionListener(e -> onExplore.run());
        buttonsPanel.add(exploreBtn);
        
        JButton cartBtn = createButton("🛒 Your Cart");
        cartBtn.addActionListener(e -> onCart.run());
        buttonsPanel.add(cartBtn);
        
        JButton themeBtn = createButton("🌙 Toggle Theme");
        themeBtn.addActionListener(e -> {
            // Toggle global theme and refresh window
            Theme.toggleDarkMode();
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w != null) {
                w.revalidate();
                w.repaint();
            }
        });
        buttonsPanel.add(themeBtn);
        
        contentPanel.add(buttonsPanel);
        contentPanel.add(Box.createVerticalStrut(40));
        
        // Footer
        JLabel footerLabel = new JLabel("© 2025 Babil Bookstore");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(footerLabel);
        
        // Centrer le contenu
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.add(Box.createVerticalGlue());
        wrapper.add(contentPanel);
        wrapper.add(Box.createVerticalGlue());
        
        add(wrapper, BorderLayout.CENTER);
        updateTheme();
    }
    
    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        StyleManager.applyButtonStyle(btn);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(150, 42));
        return btn;
    }
    
    private void toggleTheme() {
        isDark = !isDark;
        updateTheme();
        repaint();
    }
    
    private void updateTheme() {
        // Apply Theme colors globally for this component
        setBackground(Theme.getBackground());
        contentPanel.setBackground(Theme.getBackground());
        updateComponentsForTheme(contentPanel);
    }
    
    private void updateComponentsForTheme(JComponent parent) {
        for (Component comp : parent.getComponents()) {
            if (comp instanceof JLabel) {
                ((JLabel) comp).setForeground(Theme.getTextColor());
            } else if (comp instanceof JPanel) {
                ((JPanel) comp).setBackground(Theme.getBackground());
                updateComponentsForTheme((JPanel) comp);
            }
        }
    }
}
