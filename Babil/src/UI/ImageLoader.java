package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import javax.imageio.ImageIO;

/**
 * ImageLoader - Helper class for loading and caching book images
 * Provides fallback placeholder if image not found
 */
public class ImageLoader {
    
    private static final int DEFAULT_WIDTH = 180;
    private static final int DEFAULT_HEIGHT = 240;
    private static final Color PLACEHOLDER_COLOR = new Color(0xF0F0F0);
    private static final Color ICON_COLOR = new Color(0xCCCCCC);
    
    /**
     * Load an image from file path or return placeholder
     * @param imagePath Path to the image file
     * @param width Desired width
     * @param height Desired height
     * @return BufferedImage (actual or placeholder)
     */
    public static BufferedImage loadImage(String imagePath, int width, int height) {
        if (imagePath == null || imagePath.isEmpty()) {
            return createPlaceholder(width, height);
        }
        
        try {
            BufferedImage originalImage;
            // Support web URLs (http/https)
            if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
                try {
                    URL url = new URL(imagePath);
                    originalImage = ImageIO.read(url);
                } catch (Exception e) {
                    return createPlaceholder(width, height);
                }
            } else {
                File imageFile = new File(imagePath);
                if (!imageFile.exists()) {
                    return createPlaceholder(width, height);
                }
                originalImage = ImageIO.read(imageFile);
            }
            if (originalImage == null) {
                return createPlaceholder(width, height);
            }
            
            return resizeImage(originalImage, width, height);
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
            return createPlaceholder(width, height);
        }
    }
    
    /**
     * Load image with default dimensions
     */
    public static BufferedImage loadImage(String imagePath) {
        return loadImage(imagePath, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
    
    /**
     * Create a placeholder image with book icon
     */
    private static BufferedImage createPlaceholder(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        
        // Fill background
        g2.setColor(PLACEHOLDER_COLOR);
        g2.fillRect(0, 0, width, height);
        
        // Draw border
        g2.setColor(new Color(0xDDDDDD));
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(1, 1, width - 2, height - 2);
        
        // Draw book emoji/icon
        g2.setFont(new Font("Arial", Font.PLAIN, 60));
        g2.setColor(ICON_COLOR);
        FontMetrics fm = g2.getFontMetrics();
        String icon = "📖";
        int x = (width - fm.stringWidth(icon)) / 2;
        int y = ((height - fm.getAscent()) / 2) + fm.getAscent();
        g2.drawString(icon, x, y);
        
        g2.dispose();
        return image;
    }
    
    /**
     * Resize image to fit dimensions while maintaining aspect ratio
     */
    private static BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        // Calculate aspect ratio
        double originalAspect = (double) originalImage.getWidth() / originalImage.getHeight();
        double targetAspect = (double) width / height;
        
        int newWidth = width;
        int newHeight = height;
        
        if (originalAspect > targetAspect) {
            // Original is wider, fit to height
            newWidth = (int) (height * originalAspect);
        } else {
            // Original is taller, fit to width
            newHeight = (int) (width / originalAspect);
        }
        
        // Create resized image
        Image scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = resized.createGraphics();
        
        // Fill background
        g2.setColor(PLACEHOLDER_COLOR);
        g2.fillRect(0, 0, width, height);
        
        // Draw scaled image centered
        int x = (width - newWidth) / 2;
        int y = (height - newHeight) / 2;
        g2.drawImage(scaledImage, x, y, null);
        
        g2.dispose();
        return resized;
    }
    
    /**
     * Convert BufferedImage to ImageIcon
     */
    public static ImageIcon toImageIcon(BufferedImage image) {
        return new ImageIcon(image);
    }
    
    /**
     * Get a book image as ImageIcon
     */
    public static ImageIcon getBookImage(String imagePath, int width, int height) {
        return toImageIcon(loadImage(imagePath, width, height));
    }
}
