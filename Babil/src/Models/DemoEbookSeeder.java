package Models;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class DemoEbookSeeder {
    private static final String DEMO_MARKER = "babil-showcase-demo";

    private static final List<DemoBook> SHOWCASE_BOOKS = List.of(
            new DemoBook(
                    "The Last Lantern",
                    "Bābil Studio",
                    "A rain-soaked bookshop, one light left burning, and a stranger "
                            + "who returns a book fifty years late.",
                    "Literary Fiction",
                    320,
                    4.9,
                    List.of(
                            "At the end of Mariner Street, the old bookshop kept one "
                                    + "window lit long after the city had gone dark. "
                                    + "Mara said it was for late readers. Her father "
                                    + "had said it was so the shop could remember "
                                    + "where it was.",
                            "On the first storm of autumn, a stranger arrived with "
                                    + "a blue book wrapped in newspaper. He set it "
                                    + "on the counter as carefully as if it were "
                                    + "still warm. Inside the cover, Mara found a "
                                    + "name in her father's handwriting and a date "
                                    + "from before she was born.",
                            "The stranger asked for no receipt. He only wanted to "
                                    + "know if the lamp still worked. Mara looked "
                                    + "up at the amber window, where the rain made "
                                    + "a hundred small stars, and understood why "
                                    + "her father had never turned it off.")),
            new DemoBook(
                    "Small Steps, Clear Focus",
                    "Nadia Wells",
                    "A practical pocket guide to calm routines, meaningful focus, "
                            + "and making room for work that matters.",
                    "Personal Growth",
                    250,
                    4.8,
                    List.of(
                            "A clear day does not begin with a perfect plan. It "
                                    + "begins with one honest choice about what "
                                    + "deserves your attention. Write that choice "
                                    + "down before the messages and meetings arrive.",
                            "Try a small rhythm: choose one task, give it twenty "
                                    + "quiet minutes, then take a real pause. A "
                                    + "short, repeatable practice is more useful "
                                    + "than a complicated routine that only works "
                                    + "on your best days.",
                            "At the end of the day, notice what moved forward "
                                    + "rather than counting everything left undone. "
                                    + "Focus grows when attention is treated as a "
                                    + "resource, not a measure of personal worth.")),
            new DemoBook(
                    "A Map of Quiet Places",
                    "Elias Hart",
                    "A reflective travel essay about finding small pockets of "
                            + "stillness in a city that never seems to sleep.",
                    "Travel & Essays",
                    290,
                    4.7,
                    List.of(
                            "The city has its own weather indoors: warm air in "
                                    + "the station, cool silence in the museum, "
                                    + "a brief summer beneath the trees beside "
                                    + "the river. I started keeping a map of "
                                    + "these small climates.",
                            "There is a reading room above the old market where "
                                    + "the afternoon arrives slowly. Nobody asks "
                                    + "you to buy anything. From the window, the "
                                    + "crowd below becomes a tide with its own "
                                    + "gentle and unknowable logic.",
                            "Perhaps a quiet place is not somewhere the world "
                                    + "disappears. It is somewhere the world can "
                                    + "be seen without needing an immediate "
                                    + "answer. I left the reading room with no "
                                    + "new route, only a little more space to "
                                    + "notice the one I was already walking.")));

    private DemoEbookSeeder() {
    }

    public static void seedShowcaseIfNeeded(BookDAO bookDAO) throws IOException {
        List<Map<String, String>> existingBooks = bookDAO.getAllBooks();
        for (Map<String, String> book : existingBooks) {
            String path = book.getOrDefault("ebookPath", "");
            String lowerPath = path.toLowerCase();
            if ((lowerPath.endsWith(".epub") || lowerPath.endsWith(".pdf"))
                    && Files.isRegularFile(Path.of(path))) {
                return;
            }
        }

        Path ebookDirectory = Path.of("data", "ebooks").toAbsolutePath().normalize();
        Files.createDirectories(ebookDirectory);
        for (DemoBook demoBook : SHOWCASE_BOOKS) {
            Path ebookPath = ebookDirectory.resolve(demoBook.fileName);
            if (!Files.isRegularFile(ebookPath)) {
                writeEpub(ebookPath, demoBook);
            }
        }

        for (DemoBook demoBook : SHOWCASE_BOOKS) {
            boolean alreadySeeded = existingBooks.stream().anyMatch(book ->
                    book.getOrDefault("keywords", "").contains(DEMO_MARKER)
                    && demoBook.title.equals(book.get("title")));
            if (alreadySeeded) {
                continue;
            }
            Map<String, String> book = new HashMap<>();
            book.put("coverUrl", "");
            book.put("title", demoBook.title);
            book.put("author", demoBook.author);
            book.put("price", String.valueOf(demoBook.price));
            book.put("rating", String.valueOf(demoBook.rating));
            book.put("language", "English");
            book.put("summary", demoBook.summary);
            book.put("pages", String.valueOf(demoBook.pages));
            book.put("publisher", "Bābil Editions · Original demo");
            book.put("type", demoBook.category);
            book.put("keywords", DEMO_MARKER + ", original, e-book");
            book.put("stock", "0");
            book.put("ebookPath", ebookDirectory.resolve(demoBook.fileName).toString());
            if (bookDAO.addBook(book) < 0) {
                throw new IOException("Could not add the showcase e-book: " + demoBook.title);
            }
        }
    }

    private static void writeEpub(Path destination, DemoBook book) throws IOException {
        String title = escapeXml(book.title);
        String author = escapeXml(book.author);
        String identifier = "urn:babil:showcase:" + book.fileName;
        StringBuilder chapter = new StringBuilder(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<html xmlns=\"http://www.w3.org/1999/xhtml\" "
                + "xml:lang=\"en\" lang=\"en\"><head><title>" + title
                + "</title></head><body><h1>" + title
                + "</h1><p class=\"byline\">By " + author
                + "</p><p class=\"note\">An original Bābil portfolio demonstration edition.</p>");
        for (String paragraph : book.paragraphs) {
            chapter.append("<p>").append(escapeXml(paragraph)).append("</p>");
        }
        chapter.append("</body></html>");

        String packageDocument =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<package xmlns=\"http://www.idpf.org/2007/opf\" version=\"3.0\" "
                + "unique-identifier=\"book-id\"><metadata "
                + "xmlns:dc=\"http://purl.org/dc/elements/1.1/\">"
                + "<dc:identifier id=\"book-id\">" + identifier + "</dc:identifier>"
                + "<dc:title>" + title + "</dc:title>"
                + "<dc:creator>" + author + "</dc:creator>"
                + "<dc:language>en</dc:language>"
                + "<meta property=\"dcterms:modified\">2026-01-01T00:00:00Z</meta>"
                + "</metadata><manifest>"
                + "<item id=\"nav\" href=\"nav.xhtml\" media-type=\"application/xhtml+xml\" "
                + "properties=\"nav\"/>"
                + "<item id=\"chapter\" href=\"chapter.xhtml\" "
                + "media-type=\"application/xhtml+xml\"/>"
                + "</manifest><spine><itemref idref=\"chapter\"/></spine></package>";

        String navigation =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<html xmlns=\"http://www.w3.org/1999/xhtml\" "
                + "xmlns:epub=\"http://www.idpf.org/2007/ops\"><head><title>Contents</title>"
                + "</head><body><nav epub:type=\"toc\" id=\"toc\"><h1>Contents</h1>"
                + "<ol><li><a href=\"chapter.xhtml\">" + title + "</a></li>"
                + "</ol></nav></body></html>";

        try (ZipOutputStream epub = new ZipOutputStream(Files.newOutputStream(destination))) {
            byte[] mimetype = "application/epub+zip".getBytes(StandardCharsets.US_ASCII);
            CRC32 crc = new CRC32();
            crc.update(mimetype);
            ZipEntry mimeEntry = new ZipEntry("mimetype");
            mimeEntry.setMethod(ZipEntry.STORED);
            mimeEntry.setSize(mimetype.length);
            mimeEntry.setCompressedSize(mimetype.length);
            mimeEntry.setCrc(crc.getValue());
            epub.putNextEntry(mimeEntry);
            epub.write(mimetype);
            epub.closeEntry();
            writeEntry(epub, "META-INF/container.xml",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<container version=\"1.0\" "
                    + "xmlns=\"urn:oasis:names:tc:opendocument:xmlns:container\">"
                    + "<rootfiles><rootfile full-path=\"OEBPS/content.opf\" "
                    + "media-type=\"application/oebps-package+xml\"/>"
                    + "</rootfiles></container>");
            writeEntry(epub, "OEBPS/content.opf", packageDocument);
            writeEntry(epub, "OEBPS/nav.xhtml", navigation);
            writeEntry(epub, "OEBPS/chapter.xhtml", chapter.toString());
        }
    }

    private static void writeEntry(ZipOutputStream epub, String name, String content)
            throws IOException {
        epub.putNextEntry(new ZipEntry(name));
        epub.write(content.getBytes(StandardCharsets.UTF_8));
        epub.closeEntry();
    }

    private static String escapeXml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private static final class DemoBook {
        private final String title;
        private final String author;
        private final String summary;
        private final String category;
        private final int price;
        private final double rating;
        private final List<String> paragraphs;
        private final String fileName;
        private final int pages = 18;

        private DemoBook(String title, String author, String summary,
                String category, int price, double rating, List<String> paragraphs) {
            this.title = title;
            this.author = author;
            this.summary = summary;
            this.category = category;
            this.price = price;
            this.rating = rating;
            this.paragraphs = paragraphs;
            this.fileName = title.toLowerCase()
                    .replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "") + ".epub";
        }
    }
}
