import javax.swing.*;
import java.awt.*;

/**
 * Système de notifications toast (auto-fermeture)
 */
public class Toast {
    
    public enum Type {
        SUCCESS(new Color(0x4CAF50), "✓"),
        ERROR(new Color(0xF44336), "✗"),
        INFO(new Color(0x2196F3), "ℹ"),
        WARNING(new Color(0xFF9800), "⚠");
        
        public final Color color;
        public final String icon;
        
        Type(Color color, String icon) {
            this.color = color;
            this.icon = icon;
        }
    }
    
    /**
     * Afficher une notification toast
     */
    public static void show(JFrame parent, String message, Type type) {
        // Créer une fenêtre de notification
        JWindow toast = new JWindow(parent);
        toast.setBackground(new Color(0, 0, 0, 0)); // Transparent
        
        // Panel principal
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBackground(type.color);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        
        // Label du message
        JLabel msgLabel = new JLabel(type.icon + "  " + message);
        msgLabel.setForeground(Color.WHITE);
        msgLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(msgLabel, BorderLayout.CENTER);
        
        toast.add(panel);
        toast.pack();
        
        // Position: bas-droite
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = screenSize.width - toast.getWidth() - 20;
        int y = screenSize.height - toast.getHeight() - 70;
        toast.setLocation(x, y);
        
        toast.setVisible(true);
        
        // Auto-fermeture après 3 secondes
        Timer timer = new Timer(3000, e -> toast.dispose());
        timer.setRepeats(false);
        timer.start();
    }
    
    public static void success(JFrame parent, String message) {
        show(parent, message, Type.SUCCESS);
    }
    
    public static void error(JFrame parent, String message) {
        show(parent, message, Type.ERROR);
    }
    
    public static void info(JFrame parent, String message) {
        show(parent, message, Type.INFO);
    }
    
    public static void warning(JFrame parent, String message) {
        show(parent, message, Type.WARNING);
    }
}
