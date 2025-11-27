package UI;

import java.awt.*;

/**
 * Système de thème (clair/sombre) centralisé
 */
public class Theme {
    private static boolean isDarkMode = false;
    
    // Couleurs en mode clair
    public static final Color LIGHT_BG = new Color(0xF4F4F4);
    public static final Color LIGHT_CARD = new Color(0xFFFFFF);
    public static final Color LIGHT_TEXT = new Color(0x3C3C3C);
    public static final Color LIGHT_BORDER = new Color(0xE0E0E0);
    
    // Couleurs en mode sombre
    public static final Color DARK_BG = new Color(0x1A1A1A);
    public static final Color DARK_CARD = new Color(0x2D2D2D);
    public static final Color DARK_TEXT = new Color(0xEEEEEE);
    public static final Color DARK_BORDER = new Color(0x404040);
    
    // Couleurs constantes
    public static final Color BUTTON = new Color(0x8C715C);
    public static final Color BUTTON_HOVER = new Color(0x7A5F4F);
    public static final Color SUCCESS = new Color(0x4CAF50);
    public static final Color ERROR = new Color(0xF44336);
    public static final Color WARNING = new Color(0xFF9800);
    public static final Color INFO = new Color(0x2196F3);
    
    /**
     * Obtenir la couleur de fond selon le thème
     */
    public static Color getBackground() {
        return isDarkMode ? DARK_BG : LIGHT_BG;
    }
    
    /**
     * Obtenir la couleur des cartes selon le thème
     */
    public static Color getCardBackground() {
        return isDarkMode ? DARK_CARD : LIGHT_CARD;
    }
    
    /**
     * Obtenir la couleur du texte selon le thème
     */
    public static Color getTextColor() {
        return isDarkMode ? DARK_TEXT : LIGHT_TEXT;
    }
    
    /**
     * Obtenir la couleur de bordure selon le thème
     */
    public static Color getBorderColor() {
        return isDarkMode ? DARK_BORDER : LIGHT_BORDER;
    }
    
    /**
     * Toggle le mode sombre
     */
    public static void toggleDarkMode() {
        isDarkMode = !isDarkMode;
    }
    
    /**
     * Définir le mode sombre
     */
    public static void setDarkMode(boolean dark) {
        isDarkMode = dark;
    }
    
    /**
     * Obtenir le mode sombre actuel
     */
    public static boolean isDark() {
        return isDarkMode;
    }
}
