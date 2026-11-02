package UI;

import Models.BookDAO;
import Models.PurchaseDAO;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.Icon;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

public class BookstorePage extends JPanel {
    private static final String CATALOG = "catalog";
    private static final String DETAILS = "details";
    private static final String CART = "cart";
    private static final String COMPLETE = "complete";
    private static final String LIBRARY = "library";
    private static final String ALL_EBOOKS = "All e-books";
    private static final String NEW_RELEASES = "New releases";
    private static final Color[] COVER_COLORS = {
        new Color(0x566B57), new Color(0x33495C), new Color(0x84684E),
        new Color(0x4B536C), new Color(0x8A5E58), new Color(0x687B80)
    };
    private static final BufferedImage HERO_IMAGE = loadHeroImage();
    private static final Icon RATING_ICON = new Icon() {
        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int[] xPoints = {7, 9, 14, 10, 11, 7, 3, 4, 0, 5};
            int[] yPoints = {0, 5, 5, 8, 14, 11, 14, 8, 5, 5};
            for (int i = 0; i < xPoints.length; i++) {
                xPoints[i] += x;
                yPoints[i] += y;
            }
            g.setColor(new Color(0xB17A36));
            g.fillPolygon(xPoints, yPoints, xPoints.length);
            g.dispose();
        }

        @Override
        public int getIconWidth() {
            return 15;
        }

        @Override
        public int getIconHeight() {
            return 15;
        }
    };

    private final BookDAO bookDAO;
    private final PurchaseDAO purchaseDAO;
    private final Map<Integer, Integer> cartItems;
    private String screen = CATALOG;
    private String selectedCategory = ALL_EBOOKS;
    private String searchQuery = "";
    private Map<String, String> selectedBook;
    private JTextField searchField;
    private String checkoutEmail = "";
    private String libraryEmail = "";
    private String paymentMethod = "CCP";
    private String formError = "";
    private String libraryError = "";
    private JPanel catalogGrid;
    private JPanel cartPage;
    private JPanel cartContent;
    private JPanel orderSummary;
    private Boolean cartSummaryBelow;

    public BookstorePage(BookDAO bookDAO, PurchaseDAO purchaseDAO,
            Map<Integer, Integer> cartItems) {
        this.bookDAO = bookDAO;
        this.purchaseDAO = purchaseDAO;
        this.cartItems = cartItems;
        setLayout(new BorderLayout());
        rebuild();
    }

    private Color background() {
        return Theme.getBackground();
    }

    private Color surface() {
        return Theme.getCardBackground();
    }

    private Color textColor() {
        return Theme.getTextColor();
    }

    private Color mutedColor() {
        return Theme.isDark() ? new Color(0xB8B2AC) : new Color(0x77716B);
    }

    private Color borderColor() {
        return Theme.getBorderColor();
    }

    private void rebuild() {
        removeAll();
        setBackground(background());

        JPanel chrome = new JPanel(new BorderLayout());
        chrome.setOpaque(false);
        chrome.add(createHeader(), BorderLayout.NORTH);
        add(chrome, BorderLayout.NORTH);

        switch (screen) {
            case DETAILS:
                add(createDetailsPage(), BorderLayout.CENTER);
                break;
            case CART:
                add(createCartPage(), BorderLayout.CENTER);
                break;
            case COMPLETE:
                add(createCompletePage(), BorderLayout.CENTER);
                break;
            case LIBRARY:
                add(createLibraryPage(), BorderLayout.CENTER);
                break;
            default:
                add(createCatalogPage(), BorderLayout.CENTER);
                break;
        }
        revalidate();
        repaint();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new java.awt.GridBagLayout());
        header.setBackground(surface());
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, borderColor()),
                new EmptyBorder(8, 20, 8, 20)));

        JButton brand = new JButton("Bābil");
        brand.setFont(new Font("Georgia", Font.PLAIN, 22));
        brand.setForeground(Theme.BUTTON);
        brand.setBorderPainted(false);
        brand.setContentAreaFilled(false);
        brand.setFocusPainted(false);
        brand.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        brand.addActionListener(e -> {
            screen = CATALOG;
            selectedCategory = ALL_EBOOKS;
            rebuild();
        });

        JPanel search = new JPanel(new BorderLayout(9, 0));
        search.setBackground(background());
        search.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor()),
                new EmptyBorder(2, 10, 2, 5)));
        search.setPreferredSize(new Dimension(240, 38));
        search.setMaximumSize(new Dimension(245, 38));
        searchField = new JTextField(searchQuery.isBlank()
                ? "Search e-books, authors, topics..." : searchQuery, 18);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        boolean[] searchHint = {searchQuery.isBlank()};
        searchField.setForeground(searchHint[0] ? mutedColor() : textColor());
        searchField.setBackground(background());
        searchField.setBorder(null);
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchHint[0]) {
                    searchField.setText("");
                    searchField.setForeground(textColor());
                    searchHint[0] = false;
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isBlank()) {
                    searchHint[0] = true;
                    searchField.setText("Search e-books, authors, topics...");
                    searchField.setForeground(mutedColor());
                }
            }
        });
        searchField.addActionListener(e -> {
            searchQuery = searchHint[0] ? "" : searchField.getText().trim();
            screen = CATALOG;
            rebuild();
        });
        JLabel searchLabel = new JLabel("Find");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        searchLabel.setForeground(mutedColor());
        search.add(searchLabel, BorderLayout.WEST);
        search.add(searchField, BorderLayout.CENTER);
        JButton searchButton = new JButton("Go");
        styleButton(searchButton, true);
        searchButton.setToolTipText("Search books");
        searchButton.setPreferredSize(new Dimension(36, 30));
        searchButton.addActionListener(e -> {
            searchQuery = searchHint[0] ? "" : searchField.getText().trim();
            screen = CATALOG;
            rebuild();
        });
        search.add(searchButton, BorderLayout.EAST);

        JPanel actions = new JPanel(new java.awt.FlowLayout(
                java.awt.FlowLayout.RIGHT, 7, 0));
        actions.setOpaque(false);
        JButton library = new JButton("My library");
        library.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        library.setForeground(textColor());
        library.setBackground(surface());
        library.setBorderPainted(false);
        library.setFocusPainted(false);
        library.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        library.addActionListener(e -> {
            libraryEmail = checkoutEmail.isBlank() ? libraryEmail : checkoutEmail;
            libraryError = "";
            screen = LIBRARY;
            rebuild();
        });
        JButton cart = new JButton("Bag  " + cartCount());
        cart.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cart.setForeground(textColor());
        cart.setBackground(surface());
        cart.setBorderPainted(false);
        cart.setFocusPainted(false);
        cart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cart.addActionListener(e -> {
            formError = "";
            screen = CART;
            rebuild();
        });
        JButton theme = new JButton(Theme.isDark() ? "☀" : "☾");
        theme.setToolTipText("Toggle theme");
        theme.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));
        theme.setForeground(textColor());
        theme.setBackground(surface());
        theme.setBorderPainted(false);
        theme.setFocusPainted(false);
        theme.addActionListener(e -> {
            Theme.toggleDarkMode();
            rebuild();
        });
        actions.add(library);
        actions.add(cart);
        actions.add(theme);

        JPanel categoryChips = screen.equals(CATALOG) ? createCategoryChips() : null;
        boolean[] compactLayout = {false};
        boolean[] headerLaidOut = {false};
        Runnable layoutHeader = () -> {
            boolean compact = header.getWidth() < 900;
            if (headerLaidOut[0] && compactLayout[0] == compact) {
                if (compact && categoryChips != null) {
                    int categoryWidth = Math.max(130,
                            Math.min(620, (int) ((header.getWidth() - 40) * 0.58)));
                    if (categoryChips.getPreferredSize().width != categoryWidth) {
                        categoryChips.setPreferredSize(new Dimension(categoryWidth, 38));
                        header.revalidate();
                    }
                }
                return;
            }
            compactLayout[0] = compact;
            headerLaidOut[0] = true;
            header.removeAll();
            java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
            c.gridy = 0;
            c.fill = java.awt.GridBagConstraints.HORIZONTAL;
            c.anchor = java.awt.GridBagConstraints.CENTER;
            if (compact) {
                if (categoryChips != null) {
                    int categoryWidth = Math.max(130,
                            Math.min(620, (int) ((header.getWidth() - 40) * 0.58)));
                    categoryChips.setPreferredSize(new Dimension(categoryWidth, 38));
                }
                JPanel topRow = new JPanel(new BorderLayout());
                topRow.setOpaque(false);
                topRow.add(brand, BorderLayout.WEST);
                topRow.add(actions, BorderLayout.EAST);
                c.gridx = 0;
                c.weightx = 1;
                c.gridwidth = categoryChips == null ? 1 : 2;
                header.add(topRow, c);
                c.gridy = 1;
                c.gridx = 0;
                c.weightx = 1;
                c.gridwidth = 1;
                c.insets = new java.awt.Insets(7, 0, 0, 8);
                if (categoryChips == null) {
                    c.gridwidth = 2;
                    c.insets = new java.awt.Insets(7, 0, 0, 0);
                    header.add(search, c);
                } else {
                    JPanel searchAndCategories = new JPanel(new BorderLayout(8, 0));
                    searchAndCategories.setOpaque(false);
                    searchAndCategories.add(search, BorderLayout.CENTER);
                    searchAndCategories.add(categoryChips, BorderLayout.EAST);
                    c.gridwidth = 2;
                    c.insets = new java.awt.Insets(7, 0, 0, 0);
                    header.add(searchAndCategories, c);
                }
            } else {
                c.gridx = 0;
                c.weightx = 0;
                c.gridwidth = 1;
                c.insets = new java.awt.Insets(0, 0, 0, 12);
                header.add(brand, c);
                c.gridx = 1;
                c.weightx = categoryChips == null ? 1 : 0;
                c.fill = categoryChips == null
                        ? java.awt.GridBagConstraints.HORIZONTAL
                        : java.awt.GridBagConstraints.NONE;
                c.anchor = java.awt.GridBagConstraints.WEST;
                c.insets = new java.awt.Insets(0, 0, 0, 12);
                header.add(search, c);
                c.gridx = 2;
                c.weightx = categoryChips == null ? 0 : 1;
                c.fill = java.awt.GridBagConstraints.HORIZONTAL;
                c.anchor = java.awt.GridBagConstraints.CENTER;
                c.insets = new java.awt.Insets(0, 0, 0, 12);
                if (categoryChips != null) {
                    header.add(categoryChips, c);
                }
                c.gridx = 3;
                c.weightx = 0;
                c.insets = new java.awt.Insets(0, 0, 0, 0);
                header.add(actions, c);
            }
            header.revalidate();
        };
        header.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                layoutHeader.run();
            }
        });
        layoutHeader.run();
        return header;
    }

    private JPanel createCategoryChips() {
        JPanel links = new JPanel();
        links.setOpaque(true);
        links.setBackground(surface());
        links.setLayout(new BoxLayout(links, BoxLayout.X_AXIS));
        for (String category : categoryOptions()) {
            boolean selected = category.equals(selectedCategory);
            JButton chip = new JButton(category);
            chip.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            chip.setForeground(selected ? Color.WHITE : textColor());
            chip.setBackground(selected ? Theme.BUTTON : background());
            chip.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(selected ? Theme.BUTTON : borderColor()),
                    new EmptyBorder(5, 8, 5, 8)));
            chip.setFocusPainted(false);
            chip.setContentAreaFilled(true);
            chip.setOpaque(true);
            chip.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            chip.addActionListener(e -> {
                selectedCategory = category;
                screen = CATALOG;
                rebuild();
            });
            links.add(chip);
            links.add(Box.createHorizontalStrut(6));
        }
        JScrollPane categoryScroll = new JScrollPane(links,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        categoryScroll.setBorder(null);
        categoryScroll.setBackground(surface());
        categoryScroll.getViewport().setBackground(surface());
        categoryScroll.getViewport().setOpaque(true);
        categoryScroll.getHorizontalScrollBar().setUnitIncrement(16);
        categoryScroll.setPreferredSize(new Dimension(580, 38));
        categoryScroll.setMinimumSize(new Dimension(100, 38));
        categoryScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        return wrapCategoryChips(categoryScroll);
    }

    private JPanel wrapCategoryChips(JScrollPane categoryScroll) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(categoryScroll, BorderLayout.CENTER);
        wrapper.setPreferredSize(new Dimension(580, 38));
        wrapper.setMinimumSize(new Dimension(100, 38));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        return wrapper;
    }

    private List<String> categoryOptions() {
        List<String> options = new ArrayList<>();
        options.add(ALL_EBOOKS);
        options.add(NEW_RELEASES);
        for (Map<String, String> book : bookDAO.getAllBooks()) {
            String category = value(book, "type", "").trim();
            if (!category.isEmpty() && !options.contains(category)) {
                options.add(category);
            }
        }
        if (!options.contains(selectedCategory)) {
            options.add(selectedCategory);
        }
        return options;
    }

    private JPanel createCatalogPage() {
        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(16, 24, 20, 24));

        List<Map<String, String>> books = filteredBooks();
        JPanel intro = new JPanel();
        intro.setOpaque(false);
        intro.setLayout(new BoxLayout(intro, BoxLayout.Y_AXIS));
        intro.add(createHeroBanner());
        intro.add(Box.createVerticalStrut(6));
        JPanel catalogActions = new JPanel(new java.awt.FlowLayout(
                java.awt.FlowLayout.RIGHT, 10, 0));
        catalogActions.setOpaque(false);
        JButton addEbook = new JButton("+ Add e-book");
        styleButton(addEbook, false);
        addEbook.setPreferredSize(new Dimension(112, 32));
        addEbook.addActionListener(e -> openAddEbookDialog());
        catalogActions.add(addEbook);
        intro.add(catalogActions);
        content.add(intro, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 4, 16, 16));
        grid.setOpaque(false);
        catalogGrid = grid;
        updateCatalogColumns(content.getWidth() - 56);
        for (Map<String, String> book : books) {
            grid.add(createBookCard(book));
        }
        if (books.isEmpty()) {
            JPanel empty = new JPanel(new BorderLayout(0, 10));
            empty.setOpaque(false);
            empty.add(headingLabel("No books found"), BorderLayout.CENTER);
            empty.add(bodyLabel("Try another search or category."), BorderLayout.SOUTH);
            grid.add(empty);
        }
        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateCatalogColumns(scroll.getViewport().getWidth());
            }
        });
        content.add(scroll, BorderLayout.CENTER);
        return content;
    }

    private JPanel createHeroBanner() {
        JPanel banner = new JPanel() {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.setRenderingHint(RenderingHints.KEY_RENDERING,
                        RenderingHints.VALUE_RENDER_QUALITY);
                g.setColor(new Color(0x3B2C24));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g.clip(new RoundRectangle2D.Float(
                        0, 0, getWidth(), getHeight(), 18, 18));
                if (HERO_IMAGE != null && getWidth() > 0 && getHeight() > 0) {
                    double scale = Math.max(
                            (double) getWidth() / HERO_IMAGE.getWidth(),
                            (double) getHeight() / HERO_IMAGE.getHeight());
                    int imageWidth = (int) Math.ceil(HERO_IMAGE.getWidth() * scale);
                    int imageHeight = (int) Math.ceil(HERO_IMAGE.getHeight() * scale);
                    int imageX = (getWidth() - imageWidth) / 2;
                    int imageY = (int) Math.round(getHeight() / 2.0
                            - imageHeight * 0.46);
                    g.drawImage(HERO_IMAGE, imageX, imageY, imageWidth, imageHeight, null);
                    g.setPaint(new GradientPaint(0, 0, new Color(18, 12, 8, 190),
                            getWidth(), 0, new Color(30, 20, 14, 160)));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
                g.dispose();
            }
        };
        banner.setOpaque(false);
        banner.setLayout(new BorderLayout());
        banner.setPreferredSize(new Dimension(800, 100));
        banner.setMinimumSize(new Dimension(180, 90));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        banner.setBorder(new EmptyBorder(14, 26, 14, 26));
        JPanel copy = new JPanel();
        copy.setOpaque(false);
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Find your next chapter");
        title.setFont(new Font("Georgia", Font.BOLD, 27));
        title.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("Stories and ideas, waiting on the shelf.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(0xF4EDE3));
        copy.add(title);
        copy.add(Box.createVerticalStrut(4));
        copy.add(subtitle);
        banner.add(copy, BorderLayout.WEST);
        return banner;
    }

    private void updateCatalogColumns(int availableWidth) {
        if (catalogGrid == null || availableWidth < 1) {
            return;
        }
        int columns = Math.max(1, Math.min(4, availableWidth / 224));
        GridLayout layout = (GridLayout) catalogGrid.getLayout();
        if (layout.getColumns() != columns) {
            catalogGrid.setLayout(new GridLayout(0, columns, 16, 16));
            catalogGrid.revalidate();
        }
    }

    private List<Map<String, String>> filteredBooks() {
        List<Map<String, String>> results = new ArrayList<>();
        List<Map<String, String>> allBooks = bookDAO.getAllBooks();
        int newestId = 0;
        for (Map<String, String> book : allBooks) {
            newestId = Math.max(newestId, intValue(book, "id"));
        }
        for (Map<String, String> book : allBooks) {
            String type = value(book, "type", "");
            String keywords = value(book, "keywords", "");
            boolean categoryMatches = selectedCategory.equals(ALL_EBOOKS)
                    || (selectedCategory.equals(NEW_RELEASES)
                            && intValue(book, "id") > newestId - 8)
                    || type.equalsIgnoreCase(selectedCategory)
                    || keywords.toLowerCase().contains(selectedCategory.toLowerCase());
            String needle = searchQuery.toLowerCase();
            boolean searchMatches = needle.isEmpty()
                    || value(book, "title", "").toLowerCase().contains(needle)
                    || value(book, "author", "").toLowerCase().contains(needle)
                    || type.toLowerCase().contains(needle);
            if (categoryMatches && searchMatches) {
                results.add(book);
            }
        }
        return results;
    }

    private JPanel createBookCard(Map<String, String> book) {
        JPanel card = surfaceCard();
        card.setLayout(new BorderLayout(0, 9));
        card.setBorder(new EmptyBorder(12, 12, 12, 12));
        card.setPreferredSize(new Dimension(210, 372));
        int id = intValue(book, "id");
        JLabel cover = new JLabel(new javax.swing.ImageIcon(createCover(book, 140, 210)));
        cover.setHorizontalAlignment(SwingConstants.CENTER);
        cover.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cover.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openDetails(book);
            }
        });
        card.add(cover, BorderLayout.NORTH);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        JLabel name = headingLabel("<html><div style='width:180px'>"
                + escapeHtml(value(book, "title", "Untitled")) + "</div></html>");
        name.setFont(new Font("Segoe UI", Font.BOLD, 13));
        name.setAlignmentX(Component.LEFT_ALIGNMENT);
        name.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        name.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openDetails(book);
            }
        });
        JLabel author = bodyLabel(value(book, "author", "Unknown author"));
        author.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        author.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel meta = new JPanel(new BorderLayout());
        meta.setOpaque(false);
        JLabel price = new JLabel(money(book));
        price.setFont(new Font("Segoe UI", Font.BOLD, 13));
        price.setForeground(textColor());
        JLabel rating = new JLabel(value(book, "rating", "0"), RATING_ICON,
                SwingConstants.RIGHT);
        rating.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        rating.setForeground(mutedColor());
        rating.setIconTextGap(4);
        meta.add(price, BorderLayout.WEST);
        meta.add(rating, BorderLayout.EAST);
        meta.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(name);
        info.add(Box.createVerticalStrut(3));
        info.add(author);
        info.add(Box.createVerticalStrut(6));
        info.add(meta);
        card.add(info, BorderLayout.CENTER);

        float[] actionOpacity = {0f};
        JPanel buttons = new JPanel(new GridLayout(1, 2, 8, 0)) {
            @Override
            public void paint(Graphics graphics) {
                if (actionOpacity[0] <= 0f) {
                    return;
                }
                Graphics2D g = (Graphics2D) graphics.create();
                g.setComposite(AlphaComposite.SrcOver.derive(actionOpacity[0]));
                super.paint(g);
                g.dispose();
            }
        };
        buttons.setOpaque(false);
        JButton details = new JButton("Details");
        styleButton(details, false);
        details.addActionListener(e -> openDetails(book));
        boolean owned = !libraryEmail.isBlank() && purchaseDAO.hasPurchasedBook(libraryEmail, id);
        boolean inBag = cartItems.containsKey(id);
        boolean editionReady = isEditionAvailable(book);
        String actionText = owned ? "In my library"
                : !editionReady ? "Edition pending"
                : inBag ? "Added · 1" : "Add to bag";
        JButton add = new JButton(actionText);
        styleButton(add, editionReady && !owned && !inBag);
        add.setEnabled(owned || (editionReady && !inBag));
        if (!editionReady && !owned) {
            add.setToolTipText("Attach a PDF or EPUB edition before publishing this title.");
        }
        add.addActionListener(e -> {
            if (owned) {
                screen = LIBRARY;
                rebuild();
            } else if (editionReady && !inBag) {
                addToCart(id);
            }
        });
        buttons.add(details);
        buttons.add(add);
        buttons.setVisible(false);
        card.add(buttons, BorderLayout.SOUTH);
        installCardHover(card, buttons, actionOpacity);
        return card;
    }

    private void installCardHover(JPanel card, JPanel actions, float[] opacity) {
        boolean[] hovered = {false};
        javax.swing.Timer[] animation = {null};
        MouseAdapter hoverListener = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!hovered[0]) {
                    hovered[0] = true;
                    animateCardActions(card, actions, opacity, hovered, animation, 1f);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                Point point = SwingUtilities.convertPoint(
                        e.getComponent(), e.getPoint(), card);
                if (!card.contains(point) && hovered[0]) {
                    hovered[0] = false;
                    animateCardActions(card, actions, opacity, hovered, animation, 0f);
                }
            }
        };
        addHoverListener(card, hoverListener);
    }

    private void animateCardActions(JPanel card, JPanel actions, float[] opacity,
            boolean[] hovered, javax.swing.Timer[] animation, float target) {
        if (animation[0] != null) {
            animation[0].stop();
        }
        if (target > 0f) {
            actions.setVisible(true);
        }
        float start = opacity[0];
        long startTime = System.nanoTime();
        int durationMillis = 150;
        animation[0] = new javax.swing.Timer(15, e -> {
            float progress = Math.min(1f, (System.nanoTime() - startTime)
                    / (durationMillis * 1_000_000f));
            float eased = 1f - (float) Math.pow(1f - progress, 3);
            opacity[0] = start + (target - start) * eased;
            actions.repaint();
            card.repaint();
            if (progress >= 1f) {
                animation[0].stop();
                opacity[0] = target;
                if (target == 0f && !hovered[0]) {
                    actions.setVisible(false);
                }
            }
        });
        animation[0].start();
    }

    private void addHoverListener(Component component, MouseAdapter listener) {
        component.addMouseListener(listener);
        if (component instanceof java.awt.Container) {
            for (Component child : ((java.awt.Container) component).getComponents()) {
                addHoverListener(child, listener);
            }
        }
    }

    private void openAddEbookDialog() {
        JDialog dialog = new JDialog(
                javax.swing.SwingUtilities.getWindowAncestor(this),
                "Add a digital edition", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(12, 12));
        JPanel form = new JPanel(new java.awt.GridBagLayout());
        form.setBackground(surface());
        form.setBorder(new EmptyBorder(20, 22, 12, 22));
        JTextField title = new JTextField(24);
        JTextField author = new JTextField(24);
        JTextField price = new JTextField(24);
        JTextField category = new JTextField("Technology", 24);
        JTextArea summary = new JTextArea(4, 24);
        summary.setLineWrap(true);
        summary.setWrapStyleWord(true);
        JLabel fileLabel = bodyLabel("No EPUB or PDF selected");
        File[] editionFile = {null};
        JButton chooseFile = new JButton("Choose file");
        styleButton(chooseFile, false);
        chooseFile.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter(
                    "E-book files (*.epub, *.pdf)", "epub", "pdf"));
            if (chooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                editionFile[0] = chooser.getSelectedFile();
                fileLabel.setText(editionFile[0].getName());
            }
        });

        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.WEST;
        c.insets = new java.awt.Insets(5, 4, 5, 10);
        addFormRow(form, c, "Title", title);
        c.gridy++;
        addFormRow(form, c, "Author", author);
        c.gridy++;
        addFormRow(form, c, "Price (DA)", price);
        c.gridy++;
        addFormRow(form, c, "Category", category);
        c.gridy++;
        addFormRow(form, c, "Description", new JScrollPane(summary));
        c.gridy++;
        JPanel filePicker = new JPanel(new java.awt.FlowLayout(
                java.awt.FlowLayout.LEFT, 8, 0));
        filePicker.setOpaque(false);
        filePicker.add(chooseFile);
        filePicker.add(fileLabel);
        addFormRow(form, c, "E-book file", filePicker);

        JLabel error = bodyLabel(" ");
        error.setForeground(Theme.ERROR);
        error.setBorder(new EmptyBorder(0, 22, 0, 22));
        JPanel footer = new JPanel(new java.awt.FlowLayout(
                java.awt.FlowLayout.RIGHT, 8, 10));
        footer.setBackground(surface());
        JButton cancel = new JButton("Cancel");
        styleButton(cancel, false);
        cancel.addActionListener(e -> dialog.dispose());
        JButton save = new JButton("Add to catalog");
        styleButton(save, true);
        save.addActionListener(e -> {
            String bookTitle = title.getText().trim();
            String bookAuthor = author.getText().trim();
            double bookPrice;
            try {
                bookPrice = Double.parseDouble(price.getText().trim());
            } catch (NumberFormatException ex) {
                error.setText("Enter a valid price.");
                return;
            }
            if (bookTitle.isEmpty() || bookAuthor.isEmpty()
                    || !Double.isFinite(bookPrice) || bookPrice < 0) {
                error.setText("Title, author and a non-negative price are required.");
                return;
            }
            if (editionFile[0] == null || !editionFile[0].isFile()) {
                error.setText("Choose a valid EPUB or PDF file.");
                return;
            }
            Map<String, String> book = new HashMap<>();
            book.put("coverUrl", "");
            book.put("title", bookTitle);
            book.put("author", bookAuthor);
            book.put("price", String.valueOf(bookPrice));
            book.put("rating", "0");
            book.put("language", "English");
            book.put("summary", summary.getText().trim());
            book.put("publisher", "Bābil Editions");
            book.put("type", category.getText().trim().isEmpty()
                    ? "Other" : category.getText().trim());
            book.put("ebookPath", editionFile[0].getAbsolutePath());
            if (bookDAO.addBook(book) < 0) {
                error.setText("The book could not be saved. Check the application log.");
                return;
            }
            selectedCategory = ALL_EBOOKS;
            searchQuery = "";
            screen = CATALOG;
            dialog.dispose();
            rebuild();
        });
        footer.add(cancel);
        footer.add(save);
        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(surface());
        south.add(error, BorderLayout.CENTER);
        south.add(footer, BorderLayout.SOUTH);
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(south, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(470, 440));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void addFormRow(JPanel form, java.awt.GridBagConstraints c,
            String label, Component field) {
        JLabel formLabel = bodyLabel(label);
        formLabel.setForeground(textColor());
        form.add(formLabel, c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        c.insets = new java.awt.Insets(5, 4, 5, 4);
        form.add(field, c);
        c.gridx = 0;
        c.weightx = 0;
        c.fill = java.awt.GridBagConstraints.NONE;
        c.insets = new java.awt.Insets(5, 4, 5, 10);
    }

    private JPanel createDetailsPage() {
        JPanel page = new JPanel(new BorderLayout(22, 22));
        page.setOpaque(false);
        page.setBorder(new EmptyBorder(20, 28, 26, 28));
        JButton back = linkButton("←  Back to books");
        back.addActionListener(e -> {
            screen = CATALOG;
            rebuild();
        });
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(back, BorderLayout.WEST);
        page.add(top, BorderLayout.NORTH);

        if (selectedBook == null) {
            page.add(bodyLabel("Book is no longer available."), BorderLayout.CENTER);
            return page;
        }
        JPanel details = surfaceCard();
        details.setLayout(new BorderLayout(32, 0));
        details.setBorder(new EmptyBorder(24, 24, 24, 24));
        JLabel cover = new JLabel(new javax.swing.ImageIcon(createCover(selectedBook, 240, 310)));
        cover.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel copy = new JPanel();
        copy.setOpaque(false);
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
        JLabel category = bodyLabel(value(selectedBook, "type", "BOOK").toUpperCase());
        category.setForeground(Theme.BUTTON);
        copy.add(category);
        copy.add(Box.createVerticalStrut(8));
        boolean digitalFileAvailable = isEditionAvailable(selectedBook);
        boolean demoEdition = value(selectedBook, "keywords", "")
                .contains("babil-showcase-demo");
        copy.add(bodyLabel(digitalFileAvailable
                ? demoEdition ? "Original demo EPUB · instant access after checkout"
                        : "E-book file ready · instant access after checkout"
                : "Digital edition · delivery file pending"));
        copy.add(Box.createVerticalStrut(10));
        JLabel title = headingLabel(value(selectedBook, "title", "Untitled"));
        title.setFont(new Font("Georgia", Font.BOLD, 30));
        copy.add(title);
        copy.add(Box.createVerticalStrut(8));
        copy.add(bodyLabel("by " + value(selectedBook, "author", "Unknown author")));
        copy.add(Box.createVerticalStrut(20));
        copy.add(bodyLabel("Rating " + value(selectedBook, "rating", "0")
                + "     ·     " + value(selectedBook, "pages", "—") + " pages"
                + "     ·     " + value(selectedBook, "language", "English")));
        copy.add(Box.createVerticalStrut(20));
        JLabel descriptionTitle = headingLabel("About this book");
        descriptionTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        copy.add(descriptionTitle);
        copy.add(Box.createVerticalStrut(8));
        String summary = value(selectedBook, "summary", "");
        if (summary.isBlank()) {
            summary = "A thoughtful read by " + value(selectedBook, "author", "its author")
                    + ". Explore the ideas and stories within this book.";
        }
        javax.swing.JTextArea description = new javax.swing.JTextArea(summary);
        description.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        description.setForeground(mutedColor());
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setColumns(36);
        description.setRows(5);
        description.setEditable(false);
        description.setOpaque(false);
        description.setFocusable(false);
        description.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        description.setAlignmentX(Component.LEFT_ALIGNMENT);
        copy.add(description);
        copy.add(Box.createVerticalStrut(18));
        copy.add(bodyLabel("Publisher   " + value(selectedBook, "publisher", "Bābil Books")));
        copy.add(Box.createVerticalStrut(18));
        JLabel price = headingLabel(money(selectedBook));
        price.setForeground(Theme.BUTTON);
        price.setFont(new Font("Segoe UI", Font.BOLD, 24));
        copy.add(price);
        if (!formError.isBlank()) {
            JLabel error = bodyLabel(formError);
            error.setForeground(Theme.ERROR);
            copy.add(Box.createVerticalStrut(8));
            copy.add(error);
        }
        copy.add(Box.createVerticalStrut(14));
        JPanel actions = new JPanel(new java.awt.FlowLayout(
                java.awt.FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        actions.setMaximumSize(new Dimension(300, 40));
        actions.setPreferredSize(new Dimension(300, 40));
        int selectedBookId = intValue(selectedBook, "id");
        boolean owned = !libraryEmail.isBlank()
                && purchaseDAO.hasPurchasedBook(libraryEmail, selectedBookId);
        if (owned) {
            JButton library = new JButton("Open my library");
            styleButton(library, true);
            library.setPreferredSize(new Dimension(150, 36));
            library.addActionListener(e -> {
                screen = LIBRARY;
                rebuild();
            });
            actions.add(library);
        } else if (!digitalFileAvailable) {
            JButton pending = new JButton("Add an EPUB or PDF edition first");
            styleButton(pending, false);
            pending.setEnabled(false);
            pending.setPreferredSize(new Dimension(230, 36));
            actions.add(pending);
        } else {
            JButton add = new JButton(cartItems.containsKey(selectedBookId)
                    ? "Added to bag" : "Add to bag");
            styleButton(add, false);
            add.setEnabled(!cartItems.containsKey(selectedBookId));
            add.setPreferredSize(new Dimension(136, 36));
            add.addActionListener(e -> addToCart(selectedBookId));
            JButton buy = new JButton("Buy now");
            styleButton(buy, true);
            buy.setPreferredSize(new Dimension(136, 36));
            buy.addActionListener(e -> {
                cartItems.put(selectedBookId, 1);
                screen = CART;
                rebuild();
            });
            actions.add(add);
            actions.add(buy);
        }
        copy.add(actions);
        JScrollPane detailsScroll = new JScrollPane(details);
        detailsScroll.setBorder(null);
        detailsScroll.setOpaque(false);
        detailsScroll.getViewport().setOpaque(false);
        page.add(detailsScroll, BorderLayout.CENTER);
        boolean[] detailsLayoutMode = {false};
        boolean[] detailsLaidOut = {false};
        Runnable layoutDetails = () -> {
            boolean vertical = page.getWidth() < 720;
            if (detailsLaidOut[0] && detailsLayoutMode[0] == vertical) {
                return;
            }
            detailsLayoutMode[0] = vertical;
            detailsLaidOut[0] = true;
            details.removeAll();
            if (vertical) {
                details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
                cover.setAlignmentX(Component.CENTER_ALIGNMENT);
                copy.setAlignmentX(Component.LEFT_ALIGNMENT);
                details.add(cover);
                details.add(Box.createVerticalStrut(20));
                details.add(copy);
            } else {
                details.setLayout(new BorderLayout(32, 0));
                details.add(cover, BorderLayout.WEST);
                details.add(copy, BorderLayout.CENTER);
            }
            details.revalidate();
        };
        page.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                layoutDetails.run();
            }
        });
        layoutDetails.run();
        return page;
    }

    private JPanel createCartPage() {
        JPanel page = new JPanel(new BorderLayout(20, 0));
        page.setOpaque(false);
        page.setBorder(new EmptyBorder(20, 28, 26, 28));
        cartPage = page;
        cartContent = new JPanel(new BorderLayout(20, 18));
        cartContent.setOpaque(false);
        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        JButton back = linkButton("←  Continue shopping");
        back.addActionListener(e -> {
            screen = CATALOG;
            rebuild();
        });
        heading.add(back, BorderLayout.WEST);
        JLabel title = headingLabel("Your bag");
        title.setFont(new Font("Georgia", Font.BOLD, 26));
        heading.add(title, BorderLayout.EAST);
        cartContent.add(heading, BorderLayout.NORTH);

        JPanel items = new JPanel();
        items.setOpaque(false);
        items.setLayout(new BoxLayout(items, BoxLayout.Y_AXIS));
        if (cartItems.isEmpty()) {
            JPanel empty = surfaceCard();
            empty.setLayout(new BoxLayout(empty, BoxLayout.Y_AXIS));
            empty.setBorder(new EmptyBorder(30, 28, 30, 28));
            empty.add(headingLabel("Your bag is waiting"));
            empty.add(Box.createVerticalStrut(8));
            empty.add(bodyLabel("Add a book to get started."));
            items.add(empty);
        } else {
            for (Map.Entry<Integer, Integer> entry : new ArrayList<>(cartItems.entrySet())) {
                Map<String, String> book = bookDAO.getBookById(entry.getKey());
                if (book.isEmpty()) {
                    continue;
                }
                items.add(createCartRow(book, entry.getValue()));
                items.add(Box.createVerticalStrut(10));
            }
        }
        JScrollPane scroll = new JScrollPane(items);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        cartContent.add(scroll, BorderLayout.CENTER);
        orderSummary = createSummaryPanel();
        page.add(cartContent, BorderLayout.CENTER);
        applyCartLayout();
        page.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                applyCartLayout();
            }
        });
        return page;
    }

    private void applyCartLayout() {
        if (cartPage == null || cartContent == null || orderSummary == null) {
            return;
        }
        boolean summaryBelow = cartPage.getWidth() < 760;
        if (cartSummaryBelow != null && cartSummaryBelow == summaryBelow) {
            return;
        }
        cartSummaryBelow = summaryBelow;
        cartPage.remove(cartContent);
        cartPage.remove(orderSummary);
        if (summaryBelow) {
            cartPage.setLayout(new BorderLayout(0, 12));
            orderSummary.setPreferredSize(new Dimension(0,
                    Math.max(350, 310 + cartItems.size() * 24)));
            cartPage.add(cartContent, BorderLayout.CENTER);
            cartPage.add(orderSummary, BorderLayout.SOUTH);
        } else {
            cartPage.setLayout(new BorderLayout(18, 0));
            orderSummary.setPreferredSize(new Dimension(260, 0));
            cartPage.add(cartContent, BorderLayout.CENTER);
            cartPage.add(orderSummary, BorderLayout.EAST);
        }
        cartPage.revalidate();
    }

    private JPanel createCartRow(Map<String, String> book, int quantity) {
        JPanel row = surfaceCard();
        row.setLayout(new BorderLayout(16, 0));
        row.setBorder(new EmptyBorder(12, 14, 12, 14));
        JLabel cover = new JLabel(new javax.swing.ImageIcon(
                createCover(book, 78, 100)));
        row.add(cover, BorderLayout.WEST);
        JPanel labels = new JPanel();
        labels.setOpaque(false);
        labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
        labels.add(headingLabel(value(book, "title", "Untitled")));
        labels.add(Box.createVerticalStrut(5));
        labels.add(bodyLabel(value(book, "author", "")));
        labels.add(Box.createVerticalGlue());
        labels.add(bodyLabel(money(book)));
        row.add(labels, BorderLayout.CENTER);
        JPanel controls = new JPanel();
        controls.setOpaque(false);
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        JLabel license = bodyLabel("Single-user license");
        license.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        license.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton remove = linkButton("Remove");
        remove.addActionListener(e -> {
            removeFromCart(intValue(book, "id"));
        });
        controls.add(license);
        controls.add(Box.createVerticalStrut(8));
        controls.add(remove);
        row.add(controls, BorderLayout.EAST);
        return row;
    }

    private JPanel createSummaryPanel() {
        JPanel summary = surfaceCard();
        summary.setPreferredSize(new Dimension(260, 0));
        summary.setLayout(new BoxLayout(summary, BoxLayout.Y_AXIS));
        summary.setBorder(new EmptyBorder(22, 20, 20, 20));
        JLabel title = headingLabel("Order summary");
        title.setFont(new Font("Georgia", Font.BOLD, 20));
        summary.add(title);
        summary.add(Box.createVerticalStrut(18));
        for (Map.Entry<Integer, Integer> entry : cartItems.entrySet()) {
            Map<String, String> book = bookDAO.getBookById(entry.getKey());
            if (!book.isEmpty()) {
                summary.add(bodyLabel(value(book, "title", "Book") + "  × "
                        + entry.getValue()));
                summary.add(Box.createVerticalStrut(8));
            }
        }
        summary.add(Box.createVerticalStrut(12));
        summary.add(new JSeparator());
        summary.add(Box.createVerticalStrut(12));
        JLabel total = headingLabel("Total     " + money(totalAmount()));
        total.setFont(new Font("Segoe UI", Font.BOLD, 17));
        total.setForeground(Theme.BUTTON);
        summary.add(total);
        summary.add(Box.createVerticalStrut(20));
        summary.add(bodyLabel("Demo checkout · no payment is processed."));
        summary.add(Box.createVerticalStrut(12));
        summary.add(bodyLabel("Payment method"));
        summary.add(Box.createVerticalStrut(7));
        JComboBox<String> payment = new JComboBox<>(new String[] {"CCP", "CIB"});
        payment.setSelectedItem(paymentMethod);
        payment.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        payment.addActionListener(e -> paymentMethod = (String) payment.getSelectedItem());
        summary.add(payment);
        summary.add(Box.createVerticalStrut(14));
        summary.add(bodyLabel("Email for your receipt"));
        summary.add(Box.createVerticalStrut(7));
        JTextField email = new JTextField(checkoutEmail);
        email.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        email.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor()),
                new EmptyBorder(8, 9, 8, 9)));
        email.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        summary.add(email);
        summary.add(Box.createVerticalStrut(12));
        JLabel error = bodyLabel(formError);
        error.setForeground(Theme.ERROR);
        error.setAlignmentX(Component.LEFT_ALIGNMENT);
        summary.add(error);
        summary.add(Box.createVerticalGlue());
        JButton continueButton = new JButton("Continue  →");
        styleButton(continueButton, true);
        continueButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        continueButton.addActionListener(e -> {
            checkoutEmail = email.getText().trim();
            completePurchase();
        });
        summary.add(continueButton);
        return summary;
    }

    private JPanel createCompletePage() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        JPanel card = surfaceCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(42, 56, 42, 56));
        JLabel mark = headingLabel("✓");
        mark.setFont(new Font("Segoe UI", Font.BOLD, 54));
        mark.setForeground(Theme.BUTTON);
        mark.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel title = headingLabel("Thank you for your order");
        title.setFont(new Font("Georgia", Font.BOLD, 28));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel detail = bodyLabel("Your purchase is confirmed. A receipt will be sent to "
                + checkoutEmail + ".");
        detail.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton library = new JButton("Open my e-library");
        styleButton(library, true);
        library.setAlignmentX(Component.CENTER_ALIGNMENT);
        library.addActionListener(e -> {
            libraryEmail = checkoutEmail;
            libraryError = "";
            screen = LIBRARY;
            rebuild();
        });
        JButton back = linkButton("Continue browsing");
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        back.addActionListener(e -> {
            screen = CATALOG;
            searchQuery = "";
            rebuild();
        });
        card.add(mark);
        card.add(Box.createVerticalStrut(12));
        card.add(title);
        card.add(Box.createVerticalStrut(10));
        card.add(detail);
        card.add(Box.createVerticalStrut(20));
        card.add(library);
        card.add(Box.createVerticalStrut(8));
        card.add(back);
        wrapper.add(card, BorderLayout.CENTER);
        wrapper.setBorder(new EmptyBorder(70, 180, 70, 180));
        return wrapper;
    }

    private JPanel createLibraryPage() {
        JPanel page = new JPanel(new BorderLayout(0, 20));
        page.setOpaque(false);
        page.setBorder(new EmptyBorder(26, 34, 30, 34));

        JPanel heading = new JPanel(new BorderLayout(12, 8));
        heading.setOpaque(false);
        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel eyebrow = bodyLabel("YOUR PERSONAL COLLECTION");
        eyebrow.setForeground(Theme.BUTTON);
        eyebrow.setFont(new Font("Segoe UI", Font.BOLD, 10));
        JLabel title = headingLabel("My e-library");
        title.setFont(new Font("Georgia", Font.BOLD, 28));
        titleBlock.add(eyebrow);
        titleBlock.add(Box.createVerticalStrut(5));
        titleBlock.add(title);
        heading.add(titleBlock, BorderLayout.WEST);
        JButton browse = linkButton("←  Browse the bookstore");
        browse.addActionListener(e -> {
            screen = CATALOG;
            rebuild();
        });
        heading.add(browse, BorderLayout.EAST);
        page.add(heading, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setOpaque(false);
        JPanel access = surfaceCard();
        access.setLayout(new BoxLayout(access, BoxLayout.Y_AXIS));
        access.setBorder(new EmptyBorder(16, 18, 16, 18));
        JPanel accessCopy = new JPanel();
        accessCopy.setOpaque(false);
        accessCopy.setLayout(new BoxLayout(accessCopy, BoxLayout.Y_AXIS));
        accessCopy.setAlignmentX(Component.LEFT_ALIGNMENT);
        accessCopy.add(headingLabel("Find your editions"));
        accessCopy.add(Box.createVerticalStrut(4));
        accessCopy.add(bodyLabel("Enter the email used at checkout to access your books."));
        access.add(accessCopy);
        access.add(Box.createVerticalStrut(12));
        JPanel lookup = new JPanel(new BorderLayout(8, 0));
        lookup.setOpaque(false);
        lookup.setAlignmentX(Component.LEFT_ALIGNMENT);
        lookup.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        JTextField email = new JTextField(libraryEmail, 22);
        email.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        email.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor()),
                new EmptyBorder(7, 9, 7, 9)));
        JButton viewLibrary = new JButton("View library");
        styleButton(viewLibrary, true);
        viewLibrary.addActionListener(e -> loadLibrary(email.getText()));
        email.addActionListener(e -> loadLibrary(email.getText()));
        lookup.add(email, BorderLayout.CENTER);
        lookup.add(viewLibrary, BorderLayout.EAST);
        access.add(lookup);
        content.add(access, BorderLayout.NORTH);

        JPanel collection = new JPanel();
        collection.setOpaque(false);
        collection.setLayout(new BoxLayout(collection, BoxLayout.Y_AXIS));
        if (!libraryError.isBlank()) {
            JLabel error = bodyLabel(libraryError);
            error.setForeground(Theme.ERROR);
            collection.add(error);
            collection.add(Box.createVerticalStrut(10));
        }
        if (!libraryEmail.isBlank() && libraryError.isBlank()) {
            List<Map<String, Object>> purchases =
                    purchaseDAO.getPurchasesByEmail(libraryEmail);
            JPanel collectionHeading = new JPanel(new BorderLayout());
            collectionHeading.setOpaque(false);
            JLabel count = headingLabel(purchases.size() + " e-book"
                    + (purchases.size() == 1 ? "" : "s"));
            collectionHeading.add(count, BorderLayout.WEST);
            collectionHeading.add(bodyLabel("Instant access · yours to keep"),
                    BorderLayout.EAST);
            collection.add(collectionHeading);
            collection.add(Box.createVerticalStrut(12));
            if (purchases.isEmpty()) {
                collection.add(createEmptyLibraryCard());
            } else {
                for (Map<String, Object> purchase : purchases) {
                    collection.add(createLibraryCard(purchase));
                    collection.add(Box.createVerticalStrut(12));
                }
            }
        } else if (libraryEmail.isBlank() && libraryError.isBlank()) {
            collection.add(createEmptyLibraryCard());
        }
        JScrollPane collectionScroll = new JScrollPane(collection);
        collectionScroll.setBorder(null);
        collectionScroll.setOpaque(false);
        collectionScroll.getViewport().setOpaque(false);
        collectionScroll.getVerticalScrollBar().setUnitIncrement(16);
        content.add(collectionScroll, BorderLayout.CENTER);
        page.add(content, BorderLayout.CENTER);
        return page;
    }

    private JPanel createEmptyLibraryCard() {
        JPanel empty = surfaceCard();
        empty.setLayout(new BoxLayout(empty, BoxLayout.Y_AXIS));
        empty.setBorder(new EmptyBorder(26, 22, 26, 22));
        JLabel title = headingLabel("Your reading shelf starts here");
        title.setFont(new Font("Georgia", Font.BOLD, 20));
        JLabel detail = bodyLabel("Purchased e-books will appear here for instant access.");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        detail.setAlignmentX(Component.LEFT_ALIGNMENT);
        empty.add(title);
        empty.add(Box.createVerticalStrut(7));
        empty.add(detail);
        return empty;
    }

    private JPanel createLibraryCard(Map<String, Object> purchase) {
        int bookId = ((Number) purchase.get("bookId")).intValue();
        Map<String, String> book = bookDAO.getBookById(bookId);
        if (book.isEmpty()) {
            book = new HashMap<>();
            book.put("id", String.valueOf(bookId));
            book.put("title", String.valueOf(purchase.getOrDefault("title", "E-book")));
            book.put("author", "Bābil Editions");
        }

        JPanel card = surfaceCard();
        card.setLayout(new BorderLayout(16, 0));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));
        card.setPreferredSize(new Dimension(600, 150));
        JLabel cover = new JLabel(new javax.swing.ImageIcon(createCover(book, 82, 108)));
        card.add(cover, BorderLayout.WEST);

        JPanel details = new JPanel();
        details.setOpaque(false);
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        JLabel title = headingLabel(value(book, "title",
                String.valueOf(purchase.getOrDefault("title", "E-book"))));
        title.setFont(new Font("Georgia", Font.BOLD, 19));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel author = bodyLabel(value(book, "author", "Bābil Editions"));
        author.setAlignmentX(Component.LEFT_ALIGNMENT);
        String purchasedAt = String.valueOf(purchase.getOrDefault("purchaseDate", "Owned"));
        if (purchasedAt.length() > 10) {
            purchasedAt = purchasedAt.substring(0, 10);
        }
        JLabel edition = bodyLabel("DIGITAL EDITION  ·  OWNED " + purchasedAt);
        edition.setFont(new Font("Segoe UI", Font.BOLD, 10));
        edition.setForeground(Theme.BUTTON);
        edition.setAlignmentX(Component.LEFT_ALIGNMENT);
        details.add(title);
        details.add(Box.createVerticalStrut(5));
        details.add(author);
        details.add(Box.createVerticalStrut(9));
        details.add(edition);
        card.add(details, BorderLayout.CENTER);

        String ebookPath = value(book, "ebookPath", "");
        File ebookFile = ebookPath.isBlank() ? null : new File(ebookPath);
        JPanel access = new JPanel();
        access.setOpaque(false);
        access.setLayout(new BoxLayout(access, BoxLayout.Y_AXIS));
        JButton open = new JButton(ebookFile != null && ebookFile.isFile()
                ? "Open e-book" : "Edition unavailable");
        styleButton(open, ebookFile != null && ebookFile.isFile());
        open.setEnabled(ebookFile != null && ebookFile.isFile());
        open.setMaximumSize(new Dimension(150, 36));
        if (ebookFile != null && ebookFile.isFile()) {
            open.addActionListener(e -> openEbook(ebookFile));
        }
        JLabel status = bodyLabel(ebookFile != null && ebookFile.isFile()
                ? "EPUB / PDF" : "File not attached");
        status.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        status.setAlignmentX(Component.CENTER_ALIGNMENT);
        access.add(open);
        access.add(Box.createVerticalStrut(6));
        access.add(status);
        card.add(access, BorderLayout.EAST);
        return card;
    }

    private void loadLibrary(String email) {
        String normalizedEmail = email == null ? "" : email.trim();
        if (!normalizedEmail.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            libraryEmail = normalizedEmail;
            libraryError = "Enter the valid email address used at checkout.";
            rebuild();
            return;
        }
        libraryEmail = normalizedEmail;
        checkoutEmail = normalizedEmail;
        libraryError = "";
        screen = LIBRARY;
        rebuild();
    }

    private void openEbook(File ebookFile) {
        if (!ebookFile.isFile()) {
            libraryError = "This e-book file is no longer available.";
            rebuild();
            return;
        }
        if (!Desktop.isDesktopSupported()
                || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            libraryError = "Opening e-book files is not supported on this device.";
            rebuild();
            return;
        }
        try {
            Desktop.getDesktop().open(ebookFile);
        } catch (IOException | UnsupportedOperationException e) {
            System.err.println("Could not open e-book file: " + e.getMessage());
            libraryError = "Could not open this file. Check that a PDF or EPUB reader is installed.";
            rebuild();
        }
    }

    private void completePurchase() {
        if (cartItems.isEmpty()) {
            formError = "Your bag is empty.";
            rebuild();
            return;
        }
        if (!checkoutEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            formError = "Enter a valid email address.";
            rebuild();
            return;
        }
        List<PurchaseDAO.PurchaseItem> items = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : cartItems.entrySet()) {
            Map<String, String> book = bookDAO.getBookById(entry.getKey());
            if (book.isEmpty()) {
                formError = "A book in your bag is no longer available.";
                rebuild();
                return;
            }
            if (!isEditionAvailable(book)) {
                formError = value(book, "title", "An e-book")
                        + " no longer has an available digital file.";
                rebuild();
                return;
            }
            if (purchaseDAO.hasPurchasedBook(checkoutEmail, entry.getKey())) {
                formError = value(book, "title", "This e-book")
                        + " is already in your library for this email.";
                libraryEmail = checkoutEmail;
                rebuild();
                return;
            }
            items.add(new PurchaseDAO.PurchaseItem(entry.getKey(),
                    value(book, "title", "Untitled"), doubleValue(book, "price"),
                    paymentMethod));
        }
        PurchaseDAO.CheckoutResult result =
                purchaseDAO.recordPurchases(checkoutEmail, items);
        if (result != PurchaseDAO.CheckoutResult.SUCCESS) {
            formError = result == PurchaseDAO.CheckoutResult.ALREADY_OWNED
                    ? "This email already owns one of the e-books in your bag."
                    : "We could not complete checkout. Please try again.";
            libraryEmail = checkoutEmail;
            rebuild();
            return;
        }
        cartItems.clear();
        libraryEmail = checkoutEmail;
        formError = "";
        screen = COMPLETE;
        rebuild();
    }

    private void openDetails(Map<String, String> book) {
        selectedBook = book;
        formError = "";
        screen = DETAILS;
        rebuild();
    }

    private void addToCart(int bookId) {
        if (cartItems.containsKey(bookId)) {
            return;
        }
        Map<String, String> book = bookDAO.getBookById(bookId);
        if (book.isEmpty() || !isEditionAvailable(book)) {
            formError = "Attach a PDF or EPUB file before adding this title to the bag.";
            selectedBook = book;
            screen = DETAILS;
            rebuild();
            return;
        }
        formError = "";
        cartItems.put(bookId, 1);
        rebuild();
    }

    private boolean isEditionAvailable(Map<String, String> book) {
        String path = value(book, "ebookPath", "");
        if (path.isBlank()) {
            return false;
        }
        String lowerPath = path.toLowerCase();
        return (lowerPath.endsWith(".pdf") || lowerPath.endsWith(".epub"))
                && new File(path).isFile();
    }

    private void removeFromCart(int id) {
        cartItems.remove(id);
        rebuild();
    }

    private JPanel surfaceCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(surface());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(borderColor());
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        return card;
    }

    private void styleButton(JButton button, boolean primary) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setForeground(primary ? Color.WHITE : Theme.BUTTON);
        button.setBackground(primary ? Theme.BUTTON : surface());
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BUTTON),
                new EmptyBorder(5, 8, 5, 8)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private JButton smallButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(textColor());
        button.setBackground(surface());
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(borderColor()));
        button.setPreferredSize(new Dimension(34, 30));
        return button;
    }

    private JButton linkButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setForeground(Theme.BUTTON);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JLabel headingLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 21));
        label.setForeground(textColor());
        return label;
    }

    private JLabel bodyLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(mutedColor());
        return label;
    }

    private java.awt.image.BufferedImage createCover(Map<String, String> book,
            int width, int height) {
        String coverPath = value(book, "coverUrl", "");
        if (!coverPath.isBlank() && !coverPath.startsWith("http://")
                && !coverPath.startsWith("https://")) {
            File coverFile = new File(coverPath);
            if (coverFile.isFile()) {
                try {
                    BufferedImage coverImage = ImageIO.read(coverFile);
                    if (coverImage != null) {
                        return resizeCover(coverImage, width, height);
                    }
                    System.err.println("Unsupported book cover image: " + coverPath);
                } catch (IOException e) {
                    System.err.println("Could not read book cover " + coverPath + ": "
                            + e.getMessage());
                }
            } else {
                System.err.println("Book cover image not found: " + coverPath);
            }
        }

        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(
                width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        int id = Math.abs(intValue(book, "id"));
        Color base = COVER_COLORS[id % COVER_COLORS.length];
        g.setPaint(new GradientPaint(0, 0, base.brighter(), width, height, base.darker()));
        g.fillRect(0, 0, width, height);
        g.setColor(new Color(255, 255, 255, 28));
        g.fillOval(width / 2, -height / 5, width, width);
        g.fillOval(-width / 2, height / 2, width, width);
        g.setColor(new Color(255, 255, 255, 165));
        g.fillRect(13, 15, 3, height - 30);
        g.setColor(new Color(255, 255, 255, 220));
        g.setFont(new Font("Georgia", Font.BOLD, Math.max(13, width / 11)));
        drawWrapped(g, value(book, "title", "Bābil Books"), 24, height / 3,
                width - 38, Math.max(17, width / 9));
        g.setFont(new Font("Segoe UI", Font.PLAIN, Math.max(10, width / 17)));
        drawWrapped(g, value(book, "author", ""), 24, height - 32,
                width - 38, Math.max(13, width / 14));
        g.dispose();
        return image;
    }

    private BufferedImage resizeCover(BufferedImage source, int width, int height) {
        BufferedImage cover = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = cover.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        double scale = Math.max((double) width / source.getWidth(),
                (double) height / source.getHeight());
        int scaledWidth = (int) Math.ceil(source.getWidth() * scale);
        int scaledHeight = (int) Math.ceil(source.getHeight() * scale);
        int x = (width - scaledWidth) / 2;
        int y = (height - scaledHeight) / 2;
        g.drawImage(source, x, y, scaledWidth, scaledHeight, null);
        g.dispose();
        return cover;
    }

    private static BufferedImage loadHeroImage() {
        File imageFile = new File("Gemini_Generated_Image_7mbak7mbak7mbak7.png");
        if (!imageFile.isFile()) {
            System.err.println("Storefront banner image not found: " + imageFile.getPath());
            return null;
        }
        try {
            return ImageIO.read(imageFile);
        } catch (IOException e) {
            System.err.println("Could not read storefront banner image: " + e.getMessage());
            return null;
        }
    }

    private void drawWrapped(Graphics2D g, String text, int x, int y,
            int maxWidth, int lineHeight) {
        FontMetrics metrics = g.getFontMetrics();
        StringBuilder line = new StringBuilder();
        for (String word : text.split("\\s+")) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            if (metrics.stringWidth(candidate) > maxWidth && line.length() > 0) {
                g.drawString(line.toString(), x, y);
                y += lineHeight;
                line.setLength(0);
                line.append(word);
            } else {
                if (line.length() > 0) {
                    line.append(' ');
                }
                line.append(word);
            }
        }
        if (line.length() > 0) {
            g.drawString(line.toString(), x, y);
        }
    }

    private String money(Map<String, String> book) {
        return String.format("%,.0f DA", doubleValue(book, "price"));
    }

    private String money(double amount) {
        return String.format("%,.0f DA", amount);
    }

    private double totalAmount() {
        double total = 0;
        for (Map.Entry<Integer, Integer> entry : cartItems.entrySet()) {
            total += doubleValue(bookDAO.getBookById(entry.getKey()), "price")
                    * entry.getValue();
        }
        return total;
    }

    private int cartCount() {
        int total = 0;
        for (int quantity : cartItems.values()) {
            total += quantity;
        }
        return total;
    }

    private String value(Map<String, String> book, String key, String fallback) {
        String value = book.get(key);
        return value == null || value.isBlank() ? fallback : value;
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }

    private int intValue(Map<String, String> values, String key) {
        try {
            return Integer.parseInt(values.getOrDefault(key, "0"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double doubleValue(Map<String, String> values, String key) {
        try {
            return Double.parseDouble(values.getOrDefault(key, "0"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

}
