package Models;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JsonToSqliteMigration - Utility to migrate books from JSON to SQLite database
 * Reads data/books.json and imports all records into SQLite via BookDAO
 */
public class JsonToSqliteMigration {
    
    /**
     * Migrate books from JSON file to SQLite database
     * @param jsonPath Path to JSON file (e.g., "data/books.json")
     * @param dbPath Path to SQLite database (e.g., "data/books.db")
     * @return Number of books migrated
     */
    public static int migrateBooks(String jsonPath, String dbPath) {
        System.out.println("Starting migration from JSON to SQLite...");
        
        BookDAO dao = new BookDAO(dbPath);
        List<Map<String, String>> books = loadBooksFromJson(jsonPath);
        
        int migratedCount = 0;
        for (Map<String, String> book : books) {
            int bookId = dao.addBook(book);
            if (bookId > 0) {
                migratedCount++;
            }
        }
        
        System.out.println("Migration complete! Migrated " + migratedCount + " out of " + books.size() + " books.");
        return migratedCount;
    }
    
    /**
     * Load books from JSON file using regex parsing (same logic as App.java)
     * @param path Path to JSON file
     * @return List of book maps
     */
    public static List<Map<String, String>> loadBooksFromJson(String path) throws RuntimeException {
        String raw;
        try {
            raw = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON file: " + path, e);
        }
        
        List<Map<String, String>> result = new ArrayList<>();
        
        // Find object blocks
        Pattern objPattern = Pattern.compile("\\{[^}]*\\}", Pattern.DOTALL);
        Matcher m = objPattern.matcher(raw);
        while (m.find()) {
            String obj = m.group();
            Map<String, String> map = new HashMap<>();
            map.put("coverUrl", extractString(obj, "coverUrl"));
            map.put("title", extractString(obj, "title"));
            map.put("author", extractString(obj, "author"));
            map.put("price", extractString(obj, "price"));
            map.put("rating", extractString(obj, "rating"));
            map.put("language", extractString(obj, "language"));
            map.put("summary", extractString(obj, "summary"));
            map.put("pages", extractString(obj, "pages"));
            map.put("publisher", extractString(obj, "publisher"));
            
            String type = extractString(obj, "type");
            if (type == null || type.isEmpty()) type = "Other";
            map.put("type", type);
            
            String added = extractString(obj, "added_to_cart");
            if (added == null || added.isEmpty()) added = "false";
            map.put("added_to_cart", added);
            
            String keywords = extractString(obj, "keywords");
            if (keywords == null) keywords = "";
            map.put("keywords", keywords);
            
            result.add(map);
        }
        return result;
    }
    
    /**
     * Extract string value from JSON object using regex
     * @param obj JSON object string
     * @param key Field name to extract
     * @return Field value or empty string if not found
     */
    private static String extractString(String obj, String key) {
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher m = p.matcher(obj);
        if (m.find()) return m.group(1);
        return "";
    }
}
