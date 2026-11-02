package Models;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class PortfolioCatalogSeeder {
    static final String CATALOG_MARKER = "babil-portfolio-catalog";
    private static final int TARGET_BOOK_COUNT = 25;

    private PortfolioCatalogSeeder() {
    }

    public static int seedIfNeeded(BookDAO bookDAO) throws IOException {
        List<Map<String, String>> existingBooks = bookDAO.getAllBooks();
        Set<String> existingTitles = new HashSet<>();
        Map<String, Map<String, String>> catalogByTitle = new HashMap<>();
        List<Map<String, String>> portfolioBooks =
                JsonToSqliteMigration.loadBooksFromJson("data/portfolio_books.json");
        for (Map<String, String> book : portfolioBooks) {
            catalogByTitle.put(book.getOrDefault("title", "").trim()
                    .toLowerCase(Locale.ROOT), book);
        }

        for (Map<String, String> book : existingBooks) {
            String titleKey = book.getOrDefault("title", "").trim().toLowerCase(Locale.ROOT);
            existingTitles.add(titleKey);
            if (book.getOrDefault("keywords", "").contains(CATALOG_MARKER)) {
                Map<String, String> catalogBook = catalogByTitle.get(titleKey);
                boolean metadataChanged = catalogBook != null
                        && (!catalogBook.getOrDefault("coverUrl", "")
                                .equals(book.getOrDefault("coverUrl", ""))
                        || !catalogBook.getOrDefault("type", "Other")
                                .equals(book.getOrDefault("type", "Other"))
                        || !(CATALOG_MARKER + ", "
                                + catalogBook.getOrDefault("keywords", ""))
                                .equals(book.getOrDefault("keywords", ""))
                        || !catalogBook.getOrDefault("ebookPath", "")
                                .equals(book.getOrDefault("ebookPath", "")));
                if (metadataChanged
                        && !bookDAO.updatePortfolioBook(
                                Integer.parseInt(book.get("id")), catalogBook, CATALOG_MARKER)) {
                    throw new IOException("Could not update portfolio book: "
                            + catalogBook.get("title"));
                }
            }
        }

        if (existingBooks.size() >= TARGET_BOOK_COUNT) {
            return 0;
        }

        int added = 0;
        for (Map<String, String> book : portfolioBooks) {
            if (existingBooks.size() + added >= TARGET_BOOK_COUNT) {
                break;
            }
            String title = book.getOrDefault("title", "").trim();
            if (title.isEmpty() || !existingTitles.add(title.toLowerCase(Locale.ROOT))) {
                continue;
            }

            book.put("stock", "0");
            if (book.getOrDefault("ebookPath", "").isBlank()) {
                throw new IOException("Missing e-book path for portfolio title: " + title);
            }
            book.put("keywords", CATALOG_MARKER + ", "
                    + book.getOrDefault("keywords", ""));
            if (bookDAO.addBook(book) < 0) {
                throw new IOException("Could not add portfolio book: " + title);
            }
            added++;
        }

        int totalBooks = bookDAO.getAllBooks().size();
        if (totalBooks < TARGET_BOOK_COUNT) {
            throw new IOException("Portfolio catalog contains only " + totalBooks
                    + " books; expected at least " + TARGET_BOOK_COUNT + ".");
        }
        if (added > 0) {
            System.out.println("Added " + added + " public-domain books; catalog now has "
                    + totalBooks + " books.");
        }
        return added;
    }
}
