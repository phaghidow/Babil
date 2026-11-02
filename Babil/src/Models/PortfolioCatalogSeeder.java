package Models;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PortfolioCatalogSeeder {
    private static final int TARGET_BOOK_COUNT = 25;

    private PortfolioCatalogSeeder() {
    }

    public static int seedIfNeeded(BookDAO bookDAO) throws IOException {
        List<Map<String, String>> existingBooks = bookDAO.getAllBooks();
        if (existingBooks.size() >= TARGET_BOOK_COUNT) {
            return 0;
        }

        Set<String> existingTitles = new HashSet<>();
        for (Map<String, String> book : existingBooks) {
            existingTitles.add(book.getOrDefault("title", "").trim().toLowerCase());
        }

        List<Map<String, String>> portfolioBooks =
                JsonToSqliteMigration.loadBooksFromJson("data/portfolio_books.json");
        int added = 0;
        for (Map<String, String> book : portfolioBooks) {
            if (existingBooks.size() + added >= TARGET_BOOK_COUNT) {
                break;
            }
            String title = book.getOrDefault("title", "").trim();
            if (title.isEmpty() || !existingTitles.add(title.toLowerCase())) {
                continue;
            }

            book.put("stock", "0");
            book.put("ebookPath", "");
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
