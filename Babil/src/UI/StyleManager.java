package UI;

import java.awt.*;

/**
 * StyleManager - Système de design minimaliste pour Babil Bookstore
 * Couleurs: 8C715C (boutons), 3C3C3C (foncé), F4F4F4 (clair), Noir/Blanc (texte)
 */
public class StyleManager {
    
    // ============ COULEURS ============
    public static final Color COLOR_BUTTON = new Color(0x8C715C);        // Marron taupe
    public static final Color COLOR_DARK = new Color(0x3C3C3C);          // Gris foncé
    public static final Color COLOR_LIGHT = new Color(0xF4F4F4);         // Gris très clair
    public static final Color COLOR_WHITE = Color.WHITE;                 // Blanc
    public static final Color COLOR_BLACK = Color.BLACK;                 // Noir
    public static final Color COLOR_BORDER_LIGHT = new Color(0xE0E0E0);  // Gris très clair
    public static final Color COLOR_BORDER_DARK = new Color(0x2A2A2A);   // Gris très foncé
    
    // Status colors
    public static final Color COLOR_SUCCESS = new Color(0x4CAF50);       // Vert
    public static final Color COLOR_ERROR = new Color(0xF44336);         // Rouge
    public static final Color COLOR_INFO = new Color(0x2196F3);          // Bleu
    
    // ============ CONSTANTES STYLE ============
    public static final int BORDER_RADIUS = 8;              // px
    public static final int BORDER_WIDTH = 1;               // px
    public static final int PADDING_SMALL = 8;              // px
    public static final int PADDING_MEDIUM = 12;            // px
    public static final int PADDING_LARGE = 16;             // px
    public static final int PADDING_XLARGE = 24;            // px
    
    // ============ DIMENSIONS ============
    public static final int HEADER_HEIGHT = 60;
    public static final int BUTTON_HEIGHT = 40;
    public static final int BUTTON_WIDTH = 140;
    public static final int CARD_WIDTH = 250;
    public static final int CARD_HEIGHT = 380;
    
    // ============ FONTS ============
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    
    // ============ HELPER METHODS ============
    
    /**
     * Retourne une bordure minimaliste (1px, couleur variable)
     */
    public static javax.swing.border.Border createBorder(Color color) {
        return javax.swing.BorderFactory.createLineBorder(color, BORDER_WIDTH);
    }
    
    /**
     * Crée un style bouton minimaliste
     */
    public static void styleButton(javax.swing.JButton button) {
        // Use Theme primary button colors for consistency
        button.setBackground(Theme.BUTTON);
        button.setForeground(COLOR_WHITE);
        button.setFont(FONT_HEADING);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        
        // Ajouter un effet hover simple
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(Theme.BUTTON_HOVER);  // Plus foncé
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(Theme.BUTTON);
            }
        });
    }
    
    /**
     * Alias pour styleButton() - Applique le style bouton minimaliste
     */
    public static void applyButtonStyle(javax.swing.JButton button) {
        styleButton(button);
    }
    
    /**
     * Crée un style panel minimaliste avec bordure fine
     */
    public static void stylePanel(javax.swing.JPanel panel, Color bgColor) {
        panel.setBackground(bgColor);
        panel.setBorder(createBorder(COLOR_BORDER_LIGHT));
    }
    
    /**
     * Crée un style card minimaliste
     */
    public static void styleCard(javax.swing.JPanel card, Color bgColor) {
        card.setBackground(bgColor);
        card.setBorder(createBorder(COLOR_BORDER_LIGHT));
        card.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
    }
    
    /**
     * Crée un input field minimaliste
     */
    public static void styleTextField(javax.swing.JTextField field) {
        field.setFont(FONT_REGULAR);
        field.setBorder(createBorder(COLOR_BORDER_LIGHT));
        field.setMargin(new Insets(PADDING_SMALL, PADDING_SMALL, PADDING_SMALL, PADDING_SMALL));
    }
    
    /**
     * Crée un label titre
     */
    public static javax.swing.JLabel createTitleLabel(String text) {
        javax.swing.JLabel label = new javax.swing.JLabel(text);
        label.setFont(FONT_TITLE);
        label.setForeground(COLOR_DARK);
        return label;
    }
    
    /**
     * Crée un label normal
     */
    public static javax.swing.JLabel createLabel(String text) {
        javax.swing.JLabel label = new javax.swing.JLabel(text);
        label.setFont(FONT_REGULAR);
        label.setForeground(COLOR_DARK);
        return label;
    }
}
