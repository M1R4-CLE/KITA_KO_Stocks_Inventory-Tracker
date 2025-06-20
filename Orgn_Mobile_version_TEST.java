import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

    // Removed unused static inner class productThresholds to resolve naming conflict.
public class Orgn_Mobile_version_TEST extends JFrame {
    // --- Fields ---
    private JComboBox<String> nameComboBox;
    private final DefaultTableModel tableModel = new DefaultTableModel(
        new String[] { "No.", "Product Name", "Category", "Stocks", "Price" }, 0
    );
    private final JTable productTable;
    private final JTextField nameField, qtyField, priceField;
    private final JTextPane alertArea;
    private final JButton addButton, editButton, deleteButton, redoButton, clearButton, saveButton, editAlertButton, showStocksButton, settingsBtn;
    private final JLabel warnInfoLabel, warnAmountLabel;
    private final java.util.List<String[]> historyList = new ArrayList<>();
    private DefaultTableModel restockSuggestionTableModel;
    private JTable restockSuggestionTable;
    private final Set<String> productNames = new HashSet<>();
    private final Map<String, Integer> productThresholds = new HashMap<>();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");
    private final Stack<Object[]> deletedStack = new Stack<>();
    private int warnAtQuantity = 5;
    private static final String HISTORY_FILE = "history.txt";
    private static final String STOCK_FILE = "stocks.txt";
    private final int closeBtnWidth = 90, closeBtnHeight = 31;
    private ImageIcon closeIcon;
    private final java.util.List<String> allCategoriesList = new ArrayList<>();
    // Define your theme colors globally
    private JTextField categoryEditor; // Made into a field
    @SuppressWarnings("FieldMayBeFinal")
    private CategoryDocumentListener categoryDocListener; // Ensured it's a field

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Orgn_Mobile_version_TEST app = new Orgn_Mobile_version_TEST();
            app.setVisible(true);
        });
    }
    private JComboBox<String> categoryComboBox;

    @SuppressWarnings("unused")
    public Orgn_Mobile_version_TEST() {
        this.alertArea = new JTextPane();
        alertArea.setEditable(true);
        alertArea.setFont(alertArea.getFont().deriveFont(16f));
        // Load close icon (use a placeholder if not found)
        try {
            closeIcon = new ImageIcon(
                new javax.swing.ImageIcon("D:/KITAKO PORJECT/KITA_KO ICONS/Close Botton crop.png")
                    .getImage().getScaledInstance(closeBtnWidth, closeBtnHeight, java.awt.Image.SCALE_SMOOTH));
        } catch (Exception e) {
            closeIcon = new ImageIcon();
        }

        setTitle("KITA_KO Restocker Dashboard (Mobile)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // setSize(400, 750); // Commented out as we are making it full screen
        // setMinimumSize(new Dimension(320, 600)); // Commented out as it might conflict with full screen
        // setMaximumSize(new Dimension(480, 900)); // Commented out as it would prevent full screen on larger displays
        
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Make the JFrame full screen
        setLocationRelativeTo(null); // This will center the window before it's maximized, or have no effect if already maximized.

        // --- Main Panel ---
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout()); // Change layout to BorderLayout
        mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // --- Product Entry Form ---
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        formPanel.setBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1), // Explicit black line border
                "Product Entry",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                null, // Default font
                Color.BLACK // Title color
        ));
        formPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 200)); // Allow flexible width, reduced height
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center horizontally in its container

        // --- Input Field Panels (Name, Category, Qty, Price) ---
        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setPreferredSize(new Dimension(60, 28));
        nameComboBox = new JComboBox<>();
        nameComboBox.setEditable(true);
        nameComboBox.setMaximumSize(new Dimension(120, 28)); // Made even narrower
        nameField = (JTextField) nameComboBox.getEditor().getEditorComponent();
        formPanel.add(nameLabel);
        formPanel.add(Box.createRigidArea(new Dimension(4, 0)));
        formPanel.add(nameComboBox);

        // Category
        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setPreferredSize(new Dimension(60, 28));
        String[] initialCategories = {
            "Snacks", "Canned Goods", "Breakfast Foods", "Frozen Foods", "Rice and Cooking Needs",
            "Fruits and Vegetables", "Toiletries", "Detergents and Soap", "First Aid",
            "Other Household Needs", "School Supplies"
        };
        // Initialize with a DefaultComboBoxModel for easier manipulation
        categoryComboBox = new JComboBox<>(new DefaultComboBoxModel<>(initialCategories));
        categoryComboBox.setEditable(true); 

        formPanel.add(categoryLabel); // Use the created categoryLabel
        formPanel.add(categoryComboBox);

        // Populate allCategoriesList from the initial items
        Collections.addAll(allCategoriesList, initialCategories);

        // Setup editor for filtering
        categoryEditor = (JTextField) categoryComboBox.getEditor().getEditorComponent(); // Initialize the field

        // Qty
        JLabel qtyLabel = new JLabel("Qty:");
        qtyLabel.setPreferredSize(new Dimension(60, 28));
        qtyField = new JTextField(5);
        qtyField.setMaximumSize(new Dimension(60, 28)); // Made even narrower
        formPanel.add(qtyLabel);
        formPanel.add(Box.createRigidArea(new Dimension(4, 0)));
        formPanel.add(qtyField);

        // Price
        JLabel priceLabel = new JLabel("Price:");
        priceLabel.setPreferredSize(new Dimension(60, 28));
        priceField = new JTextField(7);
        priceField.setMaximumSize(new Dimension(80, 28)); // Made even narrower
        formPanel.add(priceLabel);
        formPanel.add(Box.createRigidArea(new Dimension(4, 0)));
        formPanel.add(priceField);

        addButton = new JButton("Add Product");
        addButton.setAlignmentX(CENTER_ALIGNMENT);
        addButton.setMaximumSize(new Dimension(140, 32));
        formPanel.add(addButton);
        // --- Product Table ---
        productTable = new JTable(tableModel);
        productTable.setFont(productTable.getFont().deriveFont(java.awt.Font.PLAIN, 13f));
        productTable.setRowHeight(24);
        productTable.getTableHeader().setFont(productTable.getTableHeader().getFont().deriveFont(13f));
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Set preferred widths for each column to fit content and new width
        productTable.getColumnModel().getColumn(0).setPreferredWidth(40);    // "No." (narrower)
        productTable.getColumnModel().getColumn(1).setPreferredWidth(260);   // "Product Name" (wider)
        productTable.getColumnModel().getColumn(2).setPreferredWidth(120);   // "Category"
        productTable.getColumnModel().getColumn(3).setPreferredWidth(60);    // "Stocks"
        productTable.getColumnModel().getColumn(4).setPreferredWidth(60);    // "Price"

        // Center align all columns
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        for (int i = 0; i < productTable.getColumnCount(); i++) {
            productTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        productTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                try {
                    int qty = Integer.parseInt(table.getValueAt(row, 3).toString()); // Correct column for "Stocks"
                    if (qty <= warnAtQuantity) {
                        c.setBackground(java.awt.Color.PINK);
                        c.setForeground(java.awt.Color.RED);
                    } else {
                        c.setBackground(isSelected ? table.getSelectionBackground() : java.awt.Color.WHITE);
                        c.setForeground(isSelected ? table.getSelectionForeground() : java.awt.Color.BLACK);
                    }
                } catch (NumberFormatException ex) {
                    c.setBackground(isSelected ? table.getSelectionBackground() : java.awt.Color.WHITE);
                    c.setForeground(isSelected ? table.getSelectionForeground() : java.awt.Color.BLACK);
                }
                return c;
            }
        });

        JScrollPane tableScroll = new JScrollPane(productTable);
        tableScroll.setPreferredSize(new Dimension(480, 300)); // Wider
        tableScroll.setMaximumSize(new Dimension(480, Short.MAX_VALUE));
        tableScroll.setMinimumSize(new Dimension(480, 150));
        tableScroll.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        // Use custom icon for Edit button
        ImageIcon editIcon;
        try {
            editIcon = new ImageIcon(
                new javax.swing.ImageIcon("D:/KITAKO PORJECT/KITA_KO ICONS/Edit Black.png")
                    .getImage().getScaledInstance(28, 28, java.awt.Image.SCALE_SMOOTH)
            );
        } catch (Exception e) {
            editIcon = new ImageIcon();
        }
        editButton = new JButton(editIcon);
        editButton.setToolTipText("Edit");

        // Use custom icon for Delete button
        ImageIcon deleteIcon;
        try {
            deleteIcon = new ImageIcon(
                new javax.swing.ImageIcon("D:/KITAKO PORJECT/KITA_KO ICONS/Delete Black.png")
                    .getImage().getScaledInstance(28, 28, java.awt.Image.SCALE_SMOOTH)
            );
        } catch (Exception e) {
            deleteIcon = new ImageIcon();
        }
        deleteButton = new JButton(deleteIcon);
        deleteButton.setToolTipText("Delete");

        // Use custom icon for Redo button
        ImageIcon redoIcon;
        try {
            redoIcon = new ImageIcon(
                new javax.swing.ImageIcon("D:/KITAKO PORJECT/KITA_KO ICONS/Undo_Black-removebg-preview.png")
                    .getImage().getScaledInstance(26, 26, java.awt.Image.SCALE_SMOOTH)
            );
        } catch (Exception e) {
            redoIcon = new ImageIcon();
        }
        redoButton = new JButton(redoIcon);
        redoButton.setToolTipText("Redo");

        // Use custom icon for Save button
        ImageIcon saveIcon;
        try {
            saveIcon = new ImageIcon(
                new javax.swing.ImageIcon("D:/KITAKO PORJECT/KITA_KO ICONS/Save_Black-removebg-preview.png")
                    .getImage().getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH)
            );
        } catch (Exception e) {
            saveIcon = new ImageIcon();
        }
        saveButton = new JButton(saveIcon);
        saveButton.setToolTipText("Save");

        // Use custom icon for Clear button
        ImageIcon clearIcon;
        try {
            clearIcon = new ImageIcon(
                new javax.swing.ImageIcon("D:/KITAKO PORJECT/KITA_KO ICONS/delete-list.png")
                    .getImage().getScaledInstance(22, 22, java.awt.Image.SCALE_SMOOTH)
            );
        } catch (Exception e) {
            clearIcon = new ImageIcon();
        }
        clearButton = new JButton(clearIcon);
        clearButton.setToolTipText("Clear");

        editButton.setMaximumSize(new Dimension(80, 28));
        deleteButton.setMaximumSize(new Dimension(80, 28));
        redoButton.setMaximumSize(new Dimension(80, 28));
        clearButton.setMaximumSize(new Dimension(80, 28));
        saveButton.setMaximumSize(new Dimension(80, 28));
        buttonPanel.add(editButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(8, 0)));
        buttonPanel.add(deleteButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(8, 0)));
        buttonPanel.add(redoButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(8, 0)));
        buttonPanel.add(clearButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(8, 0)));
        buttonPanel.add(saveButton);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // --- Alerts Panel ---
        JPanel alertPanel = new JPanel(new BorderLayout());
        alertPanel.setBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1), // Explicit black line border
                "Alerts & More",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                null, // Default font
                Color.BLACK // Title color
        ));
        // alertPanel.setMaximumSize(new Dimension(380, 240)); // Remove height constraint
        alertPanel.setPreferredSize(new Dimension(350, 240)); // Set preferred width (increased), height will be flexible
        alertPanel.setAlignmentX(Component.CENTER_ALIGNMENT); // Consistent alignment, though less critical in BorderLayout.WEST

        warnAmountLabel = new JLabel("Warn at quantity: " + warnAtQuantity);
        warnAmountLabel.setFont(warnAmountLabel.getFont().deriveFont(16f));
        warnAmountLabel.setHorizontalAlignment(JLabel.LEFT);

        // Configure the alertPane
        alertArea.setEditable(false);
        alertArea.setFont(alertArea.getFont().deriveFont(16f));
        JScrollPane alertScrollPane = new JScrollPane(alertArea);
        alertScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // --- Restock Suggestion Table ---
        restockSuggestionTableModel = new DefaultTableModel(new String[]{"Product to Restock", "Current Stock"}, 0);
        restockSuggestionTable = new JTable(restockSuggestionTableModel);
        restockSuggestionTable.setFont(restockSuggestionTable.getFont().deriveFont(13f));
        restockSuggestionTable.setRowHeight(22);
        restockSuggestionTable.getTableHeader().setFont(restockSuggestionTable.getTableHeader().getFont().deriveFont(13f));
        
        // Custom renderer for green background
        restockSuggestionTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(new Color(220, 255, 220)); // Light green
                c.setForeground(new Color(0, 100, 0));    // Dark green text
                if (isSelected) {
                    c.setBackground(table.getSelectionBackground());
                    c.setForeground(table.getSelectionForeground());
                }
                setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                return c;
            }
        });
        JScrollPane suggestionTableScrollPane = new JScrollPane(restockSuggestionTable);
        suggestionTableScrollPane.setPreferredSize(new Dimension(0, 100)); // Give it some initial height

        // Panel to hold alertArea and the entire suggestions section (table + button)
        JPanel centerAlertContentPanel = new JPanel(new BorderLayout());
        centerAlertContentPanel.add(alertScrollPane, BorderLayout.CENTER);

        // New panel for the suggestions table and its "View All" button
        JPanel suggestionsAreaPanel = new JPanel(new BorderLayout());
        suggestionsAreaPanel.add(suggestionTableScrollPane, BorderLayout.CENTER); // Table in the middle

        JButton viewSuggestionsButton = new JButton("View All Suggestions");
        viewSuggestionsButton.setFont(viewSuggestionsButton.getFont().deriveFont(12f)); // Keep button font standard
        viewSuggestionsButton.setToolTipText("Open a new window with all restock suggestions");
        viewSuggestionsButton.addActionListener(e -> openRestockSuggestionsDetailFrame());

        JPanel viewSuggestionsButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // Center the button
        viewSuggestionsButtonPanel.add(viewSuggestionsButton);
        suggestionsAreaPanel.add(viewSuggestionsButtonPanel, BorderLayout.SOUTH); // Button at the bottom of suggestions area

        centerAlertContentPanel.add(suggestionsAreaPanel, BorderLayout.SOUTH); // Add this combined panel to the south
        alertPanel.add(centerAlertContentPanel, BorderLayout.CENTER);

        editAlertButton = new JButton("Edit");
        JPanel alertButtonPanel = new JPanel();
        alertButtonPanel.setLayout(new BoxLayout(alertButtonPanel, BoxLayout.X_AXIS));
        alertButtonPanel.add(editAlertButton);
        alertButtonPanel.add(Box.createHorizontalStrut(20));
        alertButtonPanel.add(warnAmountLabel);
        alertPanel.add(alertButtonPanel, BorderLayout.NORTH);

        warnInfoLabel = new JLabel("");
        warnInfoLabel.setFont(warnInfoLabel.getFont().deriveFont(14f));
        warnInfoLabel.setForeground(java.awt.Color.GRAY);
        JPanel warnInfoPanel = new JPanel();
        warnInfoPanel.setLayout(new BoxLayout(warnInfoPanel, BoxLayout.X_AXIS));
        warnInfoPanel.add(Box.createHorizontalStrut(10));
        warnInfoPanel.add(warnInfoLabel);
        warnInfoPanel.add(Box.createHorizontalGlue());
        alertPanel.add(warnInfoPanel, BorderLayout.SOUTH);

        // --- Product Management Panel (Product Entry Form + Main Stock List Table) ---
        JPanel productManagementPanel = new JPanel(new BorderLayout());
        // Add Product Entry form to the top of the product management area
        productManagementPanel.add(formPanel, BorderLayout.NORTH);

        // Panel to hold the Product Table and its action buttons
        JPanel productTableSectionPanel = new JPanel();
        productTableSectionPanel.setLayout(new BoxLayout(productTableSectionPanel, BoxLayout.Y_AXIS));
        productTableSectionPanel.setBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1, true), // Black outline, 2px, rounded
                "Stock List",
                0, // Default title position
                0, // Default title justification
                null, // Default font
                Color.BLACK // Title color
            )
        );
        // Set preferred and maximum size to match the table's scroll pane
        productTableSectionPanel.setPreferredSize(new Dimension(600, 380)); // Wider panel
        productTableSectionPanel.setMaximumSize(new Dimension(600, Integer.MAX_VALUE)); // Wider panel

        // Add components to productTableSectionPanel
        productTableSectionPanel.add(tableScroll); // The table
        productTableSectionPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Spacer
        productTableSectionPanel.add(buttonPanel); // Table action buttons (Edit, Delete, etc.)
        // productTableSectionPanel.setBorder(BorderFactory.createLineBorder(Color.RED)); // Optional for debugging

        // Add Product Table section to the center of the product management area
        productManagementPanel.add(productTableSectionPanel, BorderLayout.CENTER);
        // productManagementPanel.setBorder(BorderFactory.createLineBorder(Color.GREEN)); // Optional for debugging

        // --- Panel for the "list of stock that I input" (Far Right) ---
        JPanel inputStockListPanel = new JPanel(new BorderLayout());
        inputStockListPanel.setBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1), // Explicit black line border
                "View Stocks List",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                null, // Default font
                Color.BLACK // Title color
        ));

        JTable inputtedStockTable = new JTable(tableModel); // Shares the same data model
        inputtedStockTable.setFont(productTable.getFont().deriveFont(java.awt.Font.PLAIN, 13f));
        inputtedStockTable.setRowHeight(productTable.getRowHeight());
        inputtedStockTable.getTableHeader().setFont(productTable.getTableHeader().getFont().deriveFont(13f));
        if (inputtedStockTable.getColumnCount() > 0) { // Ensure columns exist before setting width
            inputtedStockTable.getColumnModel().getColumn(0).setPreferredWidth(40);    // "No." (narrower)
            inputtedStockTable.getColumnModel().getColumn(1).setPreferredWidth(260);   // "Product Name" (wider)
            inputtedStockTable.getColumnModel().getColumn(2).setPreferredWidth(120);   // "Category"
            inputtedStockTable.getColumnModel().getColumn(3).setPreferredWidth(60);    // "Stocks"
            inputtedStockTable.getColumnModel().getColumn(4).setPreferredWidth(60);    // "Price"
        }
        // Center align all columns for the new table
        // Custom renderer to center text and ensure white background when not selected
        javax.swing.table.DefaultTableCellRenderer inputtedStockRenderer = new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE); // Explicitly set to WHITE when not selected
                c.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground()); // Use table's foreground (or Color.BLACK)
                setHorizontalAlignment(javax.swing.SwingConstants.CENTER); // Center the text
                return c;
            }
        };
        for (int i = 0; i < inputtedStockTable.getColumnCount(); i++) {
            inputtedStockTable.getColumnModel().getColumn(i).setCellRenderer(inputtedStockRenderer);
        }
        JScrollPane inputtedStockScroll = new JScrollPane(inputtedStockTable);
        inputtedStockScroll.setPreferredSize(new Dimension(400, 0)); // Further increased preferred width
        inputStockListPanel.add(inputtedStockScroll, BorderLayout.CENTER);

        // --- Full Credits Section (Image + Names) ---
        JPanel fullCreditsPanel = new JPanel();
        fullCreditsPanel.setLayout(new BoxLayout(fullCreditsPanel, BoxLayout.Y_AXIS)); // Stack image and text vertically
        fullCreditsPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5)); // Add some padding

        // Credits Image
        JLabel creditsImageLabel = new JLabel();
        creditsImageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        String creditsImagePath = "D:\\KITAKO PORJECT\\Team logo\\MWorks.png"; // Using the provided path
        try {
            ImageIcon originalIcon = new ImageIcon(creditsImagePath);
            if (originalIcon.getImageLoadStatus() == java.awt.MediaTracker.ERRORED || originalIcon.getIconWidth() == -1) {
                throw new Exception("Image load error or invalid image file for: " + creditsImagePath);
            }
            // Scale the image to a reasonable size, e.g., 150px width, maintaining aspect ratio
            int desiredWidth = 150;
            int originalWidth = originalIcon.getIconWidth();
            int originalHeight = originalIcon.getIconHeight();
            int scaledHeight = (originalHeight * desiredWidth) / originalWidth;
            Image scaledImage = originalIcon.getImage().getScaledInstance(desiredWidth, scaledHeight, Image.SCALE_SMOOTH);
            creditsImageLabel.setIcon(new ImageIcon(scaledImage));
            creditsImageLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Add black outline
        } catch (Exception e) {
            System.err.println("Failed to load embedded credits image: " + e.getMessage());
            creditsImageLabel.setText("MWorks Logo Not Found");
            creditsImageLabel.setPreferredSize(new Dimension(150, 50)); // Fallback size
            creditsImageLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Also add border for fallback
        }
        fullCreditsPanel.add(creditsImageLabel);
        fullCreditsPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Spacer

        // Team Member 1
        JLabel member1Label = new JLabel("<html><div style='text-align: center;'><b>Daryll Dave R. Masapa</b><br>Programmer</div></html>");
        member1Label.setAlignmentX(Component.CENTER_ALIGNMENT);
        member1Label.setHorizontalAlignment(SwingConstants.CENTER);
        fullCreditsPanel.add(member1Label);
        fullCreditsPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Spacer

        // Team Member 2
        JLabel member2Label = new JLabel("<html><div style='text-align: center;'><b>Rose Wyne Takahashi</b><br>Icons, Themes, Wireframe Designer</div></html>");
        member2Label.setAlignmentX(Component.CENTER_ALIGNMENT);
        member2Label.setHorizontalAlignment(SwingConstants.CENTER);
        fullCreditsPanel.add(member2Label);

        // The dateTimeLabel and its timer will be set up after fullCreditsPanel
        // and added to a separate container below it.

        // --- Date and Time Label (Live Update) ---
        JLabel dateTimeLabel = new JLabel();
        dateTimeLabel.setFont(dateTimeLabel.getFont().deriveFont(Font.ITALIC, 13f)); // Increased font size
        dateTimeLabel.setForeground(Color.GRAY);
        // Alignment will be handled by its wrapper panel

        // Timer to update the date and time every second
        javax.swing.Timer timer = new javax.swing.Timer(1000, e -> {
            dateTimeLabel.setText("As of: " + LocalDateTime.now().format(dtf));
        });
        timer.start(); // Start the timer

        // --- Container for the South section of inputStockListPanel ---
        // This panel will hold the fullCreditsPanel (with image and names)
        // and the dateTimeLabel below it.
        JPanel southSectionContainer = new JPanel(new BorderLayout());
        southSectionContainer.add(fullCreditsPanel, BorderLayout.CENTER); // Credits content

        // Panel to hold and center the dateTimeLabel, and provide spacing
        JPanel dateTimeWrapperPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5)); // hgap 0, vgap 5
        // Add top padding to dateTimeWrapperPanel to create space above the date/time
        // This, combined with fullCreditsPanel's bottom padding (5px) and FlowLayout's vgap (5px),
        // creates a similar 15px separation as the original spacer.
        dateTimeWrapperPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0)); // 5px top padding
        dateTimeWrapperPanel.add(dateTimeLabel);

        southSectionContainer.add(dateTimeWrapperPanel, BorderLayout.SOUTH); // DateTime below credits

        inputStockListPanel.add(southSectionContainer, BorderLayout.SOUTH); // Add combined south content

        // Add sections to mainPanel
        mainPanel.add(alertPanel, BorderLayout.WEST);
        mainPanel.add(productManagementPanel, BorderLayout.CENTER);
        mainPanel.add(inputStockListPanel, BorderLayout.EAST);
        // settingsPanel (defined below) will be added to BorderLayout.SOUTH

        // --- Settings and Show Stocks ---
        int settingsBtnWidth = 31, settingsBtnHeight = 31;
        ImageIcon settingsIcon;
        try {
            settingsIcon = new ImageIcon(
                new javax.swing.ImageIcon("D:/KITAKO PORJECT/KITA_KO ICONS/Settings-removebg-preview.png")
                        .getImage().getScaledInstance(settingsBtnWidth, settingsBtnHeight, java.awt.Image.SCALE_SMOOTH)
            );
        } catch (Exception e) {
            settingsIcon = new ImageIcon();
        }
        settingsBtn = new JButton(settingsIcon);
        settingsBtn.setToolTipText("Settings");
        settingsBtn.setPreferredSize(new Dimension(settingsBtnWidth, settingsBtnHeight));
        settingsBtn.setMaximumSize(new Dimension(settingsBtnWidth, settingsBtnHeight));
        settingsBtn.setMinimumSize(new Dimension(settingsBtnWidth, settingsBtnHeight));
        settingsBtn.setAlignmentX(CENTER_ALIGNMENT);
        settingsBtn.setBorderPainted(false);
        settingsBtn.setFocusPainted(false);
        settingsBtn.setContentAreaFilled(false);
        settingsBtn.addActionListener(_ -> openSettingsFrame());

        // --- Show Stocks Button as Icon ---
        int listBtnWidth = 31, listBtnHeight = 31;
        ImageIcon listIcon;
        try {
            listIcon = new ImageIcon(
                new javax.swing.ImageIcon("D:/KITAKO PORJECT/KITA_KO ICONS/bullet-list.png")
                        .getImage().getScaledInstance(listBtnWidth, listBtnHeight, java.awt.Image.SCALE_SMOOTH)
            );
        } catch (Exception e) {
            listIcon = new ImageIcon();
        }
        showStocksButton = new JButton(listIcon);
        showStocksButton.setToolTipText("Show Stocks");
        showStocksButton.setPreferredSize(new Dimension(listBtnWidth, listBtnHeight));
        showStocksButton.setMaximumSize(new Dimension(listBtnWidth, listBtnHeight));
        showStocksButton.setMinimumSize(new Dimension(listBtnWidth, listBtnHeight));
        showStocksButton.setAlignmentX(CENTER_ALIGNMENT);
        showStocksButton.setBorderPainted(false);
        showStocksButton.setFocusPainted(false);
        showStocksButton.setContentAreaFilled(false);
        showStocksButton.addActionListener(_ -> showStocks());

        JButton closeButton = new JButton(closeIcon);
        closeButton.setToolTipText("Close");
        closeButton.setPreferredSize(new Dimension(closeBtnWidth, closeBtnHeight));
        closeButton.setMaximumSize(new Dimension(closeBtnWidth, closeBtnHeight));
        closeButton.setMinimumSize(new Dimension(closeBtnWidth, closeBtnHeight));
        closeButton.setAlignmentX(CENTER_ALIGNMENT);
        closeButton.setBorderPainted(false);
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.addActionListener(_ -> {
            int result = JOptionPane.showConfirmDialog(
                    Orgn_Mobile_version_TEST.this,
                    "Are you sure you want to close the app?",
                    "Confirm Close",
                    JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                saveHistoryToFile();
                saveStockListToFile();
                dispose();
            }
        });

        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.X_AXIS));
        settingsPanel.add(Box.createHorizontalGlue());
        settingsPanel.add(closeButton);
        settingsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        settingsPanel.add(showStocksButton);
        settingsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        settingsPanel.add(settingsBtn);
        settingsPanel.add(Box.createHorizontalGlue());
        mainPanel.add(settingsPanel, BorderLayout.SOUTH); // Add settingsPanel to mainPanel's SOUTH

        // Example: If you want to show the category selection dialog, call this method where needed
        // openCategorySelectionFrame(allCategories);

        setContentPane(mainPanel);

        // --- Button Actions ---
        addButton.addActionListener(e -> addProduct());
        editButton.addActionListener(e -> editProduct());
        deleteButton.addActionListener(e -> deleteProduct());
        redoButton.addActionListener(e -> redoProduct());
        clearButton.addActionListener(e -> clearProducts());
        saveButton.addActionListener(e -> saveProductsToHistory());

        // --- Enhanced Keyboard Navigation ---

        // 1. Name Field (from nameComboBox editor)
        // On Enter, move to categoryComboBox
        nameField.addActionListener(e -> categoryComboBox.requestFocusInWindow());

        // 2. Category ComboBox Editor (for typing and filtering)
        categoryEditor.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                showCategoryPopupWithCorrectItems();
                // Select all text for easy replacement when focus is gained by tabbing/first click
                SwingUtilities.invokeLater(categoryEditor::selectAll);
            }
        });

        categoryDocListener = new CategoryDocumentListener(categoryComboBox, categoryEditor, allCategoriesList);
        categoryEditor.getDocument().addDocumentListener(categoryDocListener);

        // Add KeyListener to categoryEditor for Backspace behavior
        categoryEditor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    final String currentText = categoryEditor.getText();
                    // If the current text is a complete category name from our list
                    if (!currentText.isEmpty() && allCategoriesList.contains(currentText)) {
                        // Clear the editor. The DocumentListener will then repopulate
                        // the dropdown with all items because the text becomes empty.
                        categoryEditor.setText("");
                        // Consume the event to prevent the default Backspace action
                        // (which would just delete the last character).
                        e.consume();
                    }
                    // If not a full category name (e.g., user is typing),
                    // let the default Backspace behavior and DocumentListener handle it.
                }
            }
        });

        // Add MouseListener to categoryEditor to show popup on click if hidden
        categoryEditor.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // If the popup is not visible and the editor is clicked, show it.
                // This handles the case where the editor already has focus.
                if (!categoryComboBox.isPopupVisible()) {
                    showCategoryPopupWithCorrectItems();
                }
            }
        });

        // KeyListener on the JComboBox itself for Enter key (to confirm selection and move)
        // Up/Down arrows are handled by default JComboBox behavior when popup is visible.
        categoryComboBox.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    // If popup is visible, Enter selects the item.
                    // If user typed text and pressed Enter, it might also select.
                    // Then, move focus.
                    qtyField.requestFocusInWindow();
                    e.consume(); 
                }
            }
        });

        // 3. Qty Field: On Enter, move to priceField (existing behavior)
        qtyField.addActionListener(e -> priceField.requestFocusInWindow());
        // 4. Price Field: On Enter, click addButton (existing behavior)
        priceField.addActionListener(e -> addButton.doClick());

        // Edit Alert Button
        editAlertButton.addActionListener(e -> openEditWarnFrame());

        // Load data from files
        loadHistoryFromFile();
        // loadStockListFromFile(); // <--- Comment out or remove this line

        // Save on close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveHistoryToFile();
                saveStockListToFile();
            }
        });
        updateRestockSuggestions(); // Initial call
    }

    // Helper method to populate and show the category popup
    private void showCategoryPopupWithCorrectItems() {
        SwingUtilities.invokeLater(() -> {
            String currentText = this.categoryEditor.getText();
            DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) this.categoryComboBox.getModel();

            // Temporarily prevent DocumentListener from re-filtering during this manual setup
            if (this.categoryDocListener != null) {
                this.categoryDocListener.setFilteringActive(true);
            }

            model.removeAllElements();
            if (currentText.isEmpty()) {
                for (String cat : this.allCategoriesList) {
                    model.addElement(cat);
                }
            } else {
                for (String cat : this.allCategoriesList) {
                    if (cat.toLowerCase().contains(currentText.toLowerCase())) {
                        model.addElement(cat);
                    }
                }
                this.categoryComboBox.getEditor().setItem(currentText); // Restore editor text
            }

            if (this.categoryDocListener != null) {
                this.categoryDocListener.setFilteringActive(false);
            }

            if (model.getSize() > 0) {
                this.categoryComboBox.showPopup();
            }
        });
    }

    // Method to show the category selection frame
    @SuppressWarnings("unused")
    private void openCategorySelectionFrame(String[] allCategories) {
        JFrame categoryFrame = new JFrame("Select Category");
        categoryFrame.setSize(300, 400);
        categoryFrame.setLocationRelativeTo(this);

        JList<String> categoryList = new JList<>(allCategories);
        categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(categoryList);

        JButton saveCategoryButton = new JButton("Save");
        saveCategoryButton.addActionListener(ev -> {
            String selected = categoryList.getSelectedValue();
            if (selected != null) {
                categoryComboBox.setSelectedItem(selected);
                categoryFrame.dispose();
            } else {
                JOptionPane.showMessageDialog(categoryFrame, "Please select a category to save.");
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(saveCategoryButton, BorderLayout.SOUTH);

        categoryFrame.setContentPane(panel);
        categoryFrame.setVisible(true);
    }

    // --- Product Actions ---
    private void addProduct() {
        String productName = nameField.getText().trim();
        String qty = qtyField.getText().trim();
        String price = priceField.getText().trim();

        String category = "";
        if (categoryComboBox.getSelectedItem() != null) {
            category = categoryComboBox.getSelectedItem().toString();
        }

        // Check for duplicate product name in the table
        boolean duplicate = false;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String existingName = tableModel.getValueAt(i, 1).toString(); // Use column 1 for Product Name
            if (existingName.equalsIgnoreCase(productName)) {
                duplicate = true;
                break;
            }
        }

        if (duplicate) {
            JOptionPane.showMessageDialog(
                this,
                "You already have that product.\nCheck your stocks list.",
                "Duplicate Product",
                JOptionPane.WARNING_MESSAGE
            );
            // Do not add the product
            nameField.setText("");
            if (categoryComboBox.getItemCount() > 0) {
                categoryComboBox.setSelectedIndex(0); // Reset to the first item or use -1 for no selection
            }
            qtyField.setText("");
            priceField.setText("");
            nameField.requestFocusInWindow();
            return;
        }

        if (!productName.isEmpty() && !category.isEmpty() && !qty.isEmpty() && !price.isEmpty()) {
            if (!productNames.contains(productName)) {
                productNames.add(productName);
                nameComboBox.addItem(productName);
            }
            int rowNum = tableModel.getRowCount() + 1;
            tableModel.addRow(new Object[] { rowNum, productName, category, qty, price });
            String dateTime = LocalDateTime.now().format(dtf);
            historyList.add(new String[] { productName, category, qty, price, dateTime });
            checkLowStock(productName, qty);
            updateStockListNumbers(); // Add this line
            updateRestockSuggestions();
        }
        nameField.setText("");
        if (categoryComboBox.getItemCount() > 0) {
            categoryComboBox.setSelectedIndex(0); // Reset to the first item or use -1 for no selection
        }
        qtyField.setText("");
        priceField.setText("");
        nameField.requestFocusInWindow();
    }

    private void editProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String number = tableModel.getValueAt(selectedRow, 0).toString();
        String name = tableModel.getValueAt(selectedRow, 1).toString();
        String category = tableModel.getValueAt(selectedRow, 2).toString();
        String qty = tableModel.getValueAt(selectedRow, 3).toString();
        String price = tableModel.getValueAt(selectedRow, 4).toString();
        openEditFrame(selectedRow, number, name, category, qty, price);
        // updateRestockSuggestions will be called after successful edit if stock changes
    }

    private void deleteProduct() {
        int rowToDelete = productTable.getSelectedRow();
        if (rowToDelete >= 0) {
            Object[] deletedRow = {
                    tableModel.getValueAt(rowToDelete, 0),
                    tableModel.getValueAt(rowToDelete, 1),
                    tableModel.getValueAt(rowToDelete, 2),
                    tableModel.getValueAt(rowToDelete, 3),
                    tableModel.getValueAt(rowToDelete, 4)
            };
            deletedStack.push(deletedRow);
            tableModel.removeRow(rowToDelete);
            updateStockListNumbers(); // Add this line
            updateRestockSuggestions();
        }
    }

    private void redoProduct() {
        if (!deletedStack.isEmpty()) {
            Object[] restored = deletedStack.pop();
            tableModel.addRow(restored);
            updateStockListNumbers(); // Add this line
            updateRestockSuggestions();
        }
    }

    private void clearProducts() {
        tableModel.setRowCount(0);
        updateStockListNumbers(); // Add this line (optional, for safety)
        updateRestockSuggestions();
    }

    private void saveProductsToHistory() {
        int added = 0;
        String dateTime = LocalDateTime.now().format(dtf);
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String pname = tableModel.getValueAt(i, 1).toString(); // Product Name
            String category = tableModel.getValueAt(i, 2).toString(); // Category
            String qty = tableModel.getValueAt(i, 3).toString(); // Stocks
            String price = tableModel.getValueAt(i, 4).toString(); // Price
            boolean alreadyInHistory = false;
            for (String[] hist : historyList) {
                if (hist[0].equals(pname) && hist[1].equals(category) && hist[2].equals(qty) && hist[3].equals(price) && hist[4].startsWith(dateTime.substring(0, 10))) {
                    alreadyInHistory = true;
                    break;
                }
            }
            if (!alreadyInHistory) {
                historyList.add(new String[] { pname, category, qty, price, dateTime });
                added++;
            }
        }
        saveHistoryToFile();
        JOptionPane.showMessageDialog(this, "Saved" + (added > 0 ? " (" + added + " new record(s))" : ""));
    }

    private void checkLowStock(String productName, String qty) {
        try {
            int qtyValue = Integer.parseInt(qty);
            int threshold = productThresholds.getOrDefault(productName, warnAtQuantity);
            if (qtyValue <= threshold) {
                String msg = "ALERT: '" + productName + "' is low on stock (" + qty + ")!";
                // The check !alertArea.getText().contains(msg) can be problematic with styled text.
                // A more robust solution might involve tracking active alerts.
                // For now, we'll keep the existing check logic.
                if (!alertArea.getText().contains(msg)) {
                    appendBlinkingAlert(msg); // New method for red, blinking alert
                }
                Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(this, msg, "Low Stock Alert", JOptionPane.WARNING_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            // Ignore if qty is not a number
        }
    }

    // --- Edit Product Frame ---
    @SuppressWarnings("UnnecessaryTemporaryOnConversionFromString")
    private void openEditFrame(int rowIndex, String number, String name, String category, String qty, String price) {
        JFrame editFrame = new JFrame("Edit Product");
        editFrame.setSize(400, 340);
        editFrame.setResizable(false);
        editFrame.setLocationRelativeTo(null); // Center on screen

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel numberLabel = new JLabel("No.:");
        JTextField editNumberField = new JTextField(number, 5);
        JLabel nameLabel = new JLabel("Product Name:");
        JTextField editNameField = new JTextField(name, 15);
        JLabel categoryLabel = new JLabel("Category:");
        JTextField editCategoryField = new JTextField(category, 15);
        JLabel qtyLabel = new JLabel("Stocks:");
        JTextField editQtyField = new JTextField(qty, 5);
        JLabel priceLabel = new JLabel("Price:");
        JTextField editPriceField = new JTextField(price, 7);
        JButton editSaveButton = new JButton("Save");

        gbc.gridx = 0; gbc.gridy = 0; panel.add(numberLabel, gbc);
        gbc.gridx = 1; panel.add(editNumberField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(nameLabel, gbc);
        gbc.gridx = 1; panel.add(editNameField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(categoryLabel, gbc);
        gbc.gridx = 1; panel.add(editCategoryField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; panel.add(qtyLabel, gbc);
        gbc.gridx = 1; panel.add(editQtyField, gbc);
        gbc.gridx = 0; gbc.gridy = 4; panel.add(priceLabel, gbc);
        gbc.gridx = 1; panel.add(editPriceField, gbc);
        gbc.gridx = 1; gbc.gridy = 5; panel.add(editSaveButton, gbc);

        editFrame.add(panel);
        editFrame.setVisible(true);

        editSaveButton.addActionListener(_ -> {
            String newNumber = editNumberField.getText().trim();
            String newName = editNameField.getText().trim();
            String newCategory = editCategoryField.getText().trim();
            String newQty = editQtyField.getText().trim();
            String newPrice = editPriceField.getText().trim();
            // Validate that number and quantity are integers, price is a number
            try {
                Integer.parseInt(newNumber);
                Integer.parseInt(newQty);
                Double.parseDouble(newPrice);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(editFrame, "No. and Stocks must be integers, Price must be a number.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!newNumber.isEmpty() && !newName.isEmpty() && !newCategory.isEmpty() && !newQty.isEmpty() && !newPrice.isEmpty()) {
                tableModel.setValueAt(newNumber, rowIndex, 0);    // No.
                tableModel.setValueAt(newName, rowIndex, 1);      // Product Name
                tableModel.setValueAt(newCategory, rowIndex, 2);  // Category
                tableModel.setValueAt(newQty, rowIndex, 3);       // Stocks
                tableModel.setValueAt(newPrice, rowIndex, 4);     // Price
                if (!productNames.contains(newName)) {
                    productNames.add(newName);
                    nameComboBox.addItem(newName);
                }
                updateStockListNumbers(); // Ensure numbering is correct after edit
                updateRestockSuggestions(); // Update suggestions if stock changed
                editFrame.dispose();
                productTable.clearSelection();
            }
        });
    }

    // --- Edit Warn Quantity Frame ---
    private void openEditWarnFrame() {
        JFrame editWarnFrame = new JFrame("Edit Warn Quantity");
        // editWarnFrame.setSize(400, 220); // Commented out for full screen
        editWarnFrame.setLocationRelativeTo(this);
        editWarnFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        editWarnFrame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Make the JFrame full screen

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel label = new JLabel("Edit Warn of Quantity");
        label.setFont(label.getFont().deriveFont(Font.BOLD, 18f));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField inputField = new JTextField(String.valueOf(warnAtQuantity));
        inputField.setMaximumSize(new Dimension(200, 40));
        inputField.setFont(inputField.getFont().deriveFont(Font.PLAIN, 20f));
        inputField.setHorizontalAlignment(JTextField.CENTER);
        inputField.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton saveBtn = new JButton("Save");
        saveBtn.setFont(saveBtn.getFont().deriveFont(Font.BOLD, 16f));
        saveBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel messageLabel = new JLabel("");
        messageLabel.setFont(messageLabel.getFont().deriveFont(Font.PLAIN, 14f));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(inputField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(saveBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(messageLabel);

        editWarnFrame.add(panel);

        Runnable saveAction = () -> {
            String text = inputField.getText().trim();
            try {
                int newThreshold = Integer.parseInt(text);
                if (newThreshold > 0) {
                    warnAtQuantity = newThreshold;
                    warnAmountLabel.setText("Warn at quantity: " + warnAtQuantity);
                    warnInfoLabel.setText("Warn threshold updated.");

                    // Show alerts for products below threshold
                    StringBuilder alerts = new StringBuilder();
                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        String pname = tableModel.getValueAt(i, 1).toString(); // Product Name
                        String qtyStr = tableModel.getValueAt(i, 3).toString(); // Stocks
                        try {
                            int qty = Integer.parseInt(qtyStr);
                            if (qty <= warnAtQuantity) {
                                String msg = "ALERT: '" + pname + "' is low on stock (" + qty + ")!";
                                if (!alertArea.getText().contains(msg)) {
                                    StyledDocument doc = alertArea.getStyledDocument();
                                    SimpleAttributeSet redAttr = new SimpleAttributeSet();
                                    StyleConstants.setForeground(redAttr, Color.RED);
                                    try {
                                        doc.insertString(doc.getLength(), (alertArea.getText().isEmpty() ? "" : "\n") + msg, redAttr);
                                    } catch (BadLocationException e) {
                                        // Ignore
                                    }
                                }
                                if (alerts.length() > 0) alerts.append("<br>");
                                alerts.append(msg);
                            }
                        } catch (NumberFormatException ignore) {}
                    }

                    if (alerts.length() > 0) {
                        messageLabel.setText("<html><b><span style='color:red;'>" + alerts.toString() + "</span></b></html>");
                    } else {
                        messageLabel.setForeground(new Color(0, 128, 0));
                        messageLabel.setText("You edited the warn product to " + warnAtQuantity);
                    }
                    updateRestockSuggestions(); // Update suggestions based on new threshold
                    editWarnFrame.dispose();
                } else {
                    messageLabel.setForeground(Color.RED);
                    messageLabel.setText("Please enter a positive number.");
                }
            } catch (NumberFormatException ex) {
                messageLabel.setForeground(Color.RED);
                messageLabel.setText("Please enter a valid number.");
            }
        };

        saveBtn.addActionListener(_ -> saveAction.run());
        inputField.addActionListener(_ -> saveAction.run());

        editWarnFrame.setVisible(true);
    }

    // --- Show Stocks ---
    private void showStocks() {
        JFrame stocksFrame = new JFrame("All Stocks");
        // stocksFrame.setSize(400, 750); // Commented out for full screen
        // stocksFrame.setMinimumSize(new Dimension(320, 600)); // Commented out for full screen
        // stocksFrame.setMaximumSize(new Dimension(480, 900)); // Commented out for full screen
        stocksFrame.setLocationRelativeTo(this);
        stocksFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        stocksFrame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Make the JFrame full screen

        DefaultTableModel stocksModel = new DefaultTableModel(
            new String[] { "No.", "Product Name", "Stocks", "Category", "Price" }, 0
        );
        JTable stocksTable = new JTable(stocksModel);

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String number = tableModel.getValueAt(i, 0).toString();      // No.
            String pname = tableModel.getValueAt(i, 1).toString();       // Product Name
            String qty = tableModel.getValueAt(i, 3).toString();         // Stocks
            String category = tableModel.getValueAt(i, 2).toString();    // Category
            String price = tableModel.getValueAt(i, 4).toString();       // Price
            stocksModel.addRow(new String[] { number, pname, qty, category, price });
        }

        // Center align all columns in the Stocks History table
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        for (int i = 0; i < stocksTable.getColumnCount(); i++) {
            stocksTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane stocksScroll = new JScrollPane(stocksTable);
        stocksScroll.setPreferredSize(new Dimension(360, 600)); // Adjusted for taller frame

        // Calculate total target sales
        double totalTargetSales = 0.0;
        for (int i = 0; i < stocksModel.getRowCount(); i++) {
            try {
                int qty = Integer.parseInt(stocksModel.getValueAt(i, 2).toString());    // "Stocks" column
                double price = Double.parseDouble(stocksModel.getValueAt(i, 4).toString()); // "Price" column
                totalTargetSales += qty * price;
            } catch (NumberFormatException ignore) {}
        }

        // Create a label for the total
        JLabel totalLabel = new JLabel("Total Target Sales: " + String.format("%.2f", totalTargetSales));
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD, 18f));
        totalLabel.setForeground(new Color(0, 128, 0));
        totalLabel.setAlignmentX(CENTER_ALIGNMENT);

        // Add custom close button
        JButton closeStocksBtn = new JButton(closeIcon);
        closeStocksBtn.setToolTipText("Close");
        closeStocksBtn.setPreferredSize(new Dimension(closeBtnWidth, closeBtnHeight));
        closeStocksBtn.setMaximumSize(new Dimension(closeBtnWidth, closeBtnHeight));
        closeStocksBtn.setMinimumSize(new Dimension(closeBtnWidth, closeBtnHeight));
        closeStocksBtn.setAlignmentX(CENTER_ALIGNMENT);
        closeStocksBtn.setBorderPainted(false);
        closeStocksBtn.setFocusPainted(false);
        closeStocksBtn.setContentAreaFilled(false);
        closeStocksBtn.addActionListener(_ -> stocksFrame.dispose());

        JPanel closePanel = new JPanel();
        closePanel.setLayout(new BoxLayout(closePanel, BoxLayout.X_AXIS));
        closePanel.add(Box.createHorizontalGlue());
        closePanel.add(closeStocksBtn);
        closePanel.add(Box.createHorizontalGlue());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.add(stocksScroll);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(totalLabel); // <-- Add the total label here
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(closePanel);

        stocksFrame.setContentPane(contentPanel);
        stocksFrame.setVisible(true);
    }

    // --- Settings Frame (Theme & History) ---
    private void openSettingsFrame() {
        JFrame settingsFrame = new JFrame("Settings");
        // settingsFrame.setSize(400, 750); // Commented out for full screen
        // settingsFrame.setMinimumSize(new Dimension(320, 600)); // Commented out for full screen
        // settingsFrame.setMaximumSize(new Dimension(480, 900)); // Commented out for full screen
        settingsFrame.setLocationRelativeTo(this);
        settingsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        settingsFrame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Make the JFrame full screen

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JButton themeButton = new JButton("Theme");
        themeButton.setMaximumSize(new Dimension(200, 40));
        themeButton.setAlignmentX(CENTER_ALIGNMENT);
        JButton historyButton = new JButton("History");
        historyButton.setMaximumSize(new Dimension(200, 40));
        historyButton.setAlignmentX(CENTER_ALIGNMENT);

        panel.add(Box.createVerticalGlue());
        panel.add(themeButton);
        panel.add(Box.createRigidArea(new Dimension(0, 24)));
        panel.add(historyButton);
        panel.add(Box.createVerticalGlue());

        settingsFrame.add(panel);

        themeButton.addActionListener(_ -> openThemeSelectionFrame(settingsFrame));
        historyButton.addActionListener(_ -> openHistoryFrameWithClose(settingsFrame));

        JButton settingsCloseBtn = new JButton(closeIcon);
        settingsCloseBtn.setToolTipText("Close");
        settingsCloseBtn.setPreferredSize(new Dimension(closeBtnWidth, closeBtnHeight));
        settingsCloseBtn.setMaximumSize(new Dimension(closeBtnWidth, closeBtnHeight));
        settingsCloseBtn.setMinimumSize(new Dimension(closeBtnWidth, closeBtnHeight));
        settingsCloseBtn.setAlignmentX(CENTER_ALIGNMENT);
        settingsCloseBtn.setBorderPainted(false);
        settingsCloseBtn.setFocusPainted(false);
        settingsCloseBtn.setContentAreaFilled(false);
        settingsCloseBtn.addActionListener(_ -> settingsFrame.dispose());

        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(settingsCloseBtn);

        settingsFrame.setVisible(true);
    }

    private void openThemeSelectionFrame(JFrame parent) {
        JFrame themeFrame = new JFrame("Theme Selection");
        // themeFrame.setSize(320, 300); // Commented out for full screen
        // themeFrame.setMinimumSize(new Dimension(280, 200)); // Commented out for full screen
        themeFrame.setLocationRelativeTo(parent);
        themeFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        themeFrame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Make the JFrame full screen

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel("Choose a theme:");
        label.setAlignmentX(CENTER_ALIGNMENT);
        label.setFont(label.getFont().deriveFont(16f));
        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(0, 18)));

        JButton darkBtn = new JButton("Dark Theme");
        JButton lightBtn = new JButton("Light Theme");
        JButton customColorBtn = new JButton("Custom Color");
        JButton defaultColorBtn = new JButton("Default Color");
        darkBtn.setAlignmentX(CENTER_ALIGNMENT);
        lightBtn.setAlignmentX(CENTER_ALIGNMENT);
        customColorBtn.setAlignmentX(CENTER_ALIGNMENT);
        defaultColorBtn.setAlignmentX(CENTER_ALIGNMENT);
        darkBtn.setMaximumSize(new Dimension(160, 36));
        lightBtn.setMaximumSize(new Dimension(160, 36));
        customColorBtn.setMaximumSize(new Dimension(160, 36));
        defaultColorBtn.setMaximumSize(new Dimension(160, 36));
        panel.add(darkBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(lightBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(customColorBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(defaultColorBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 24)));

        JButton themeCloseBtn = new JButton("Close");
        themeCloseBtn.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(themeCloseBtn);

        // Action Listeners for Theme Buttons
        darkBtn.addActionListener(_ -> {
            applyTheme(java.awt.Color.DARK_GRAY, java.awt.Color.WHITE); // Dark theme
            themeFrame.dispose();
        });
        lightBtn.addActionListener(_ -> {
            applyTheme(java.awt.Color.WHITE, java.awt.Color.BLACK); // Standard light theme colors
            themeFrame.dispose();
        });
        customColorBtn.addActionListener(_ -> {
            java.awt.Color selectedColor = JColorChooser.showDialog(themeFrame, "Choose Background Color", getBackground());
            if (selectedColor != null) {
                applyTheme(selectedColor, java.awt.Color.BLACK); // Custom color with black text
            }
        });
        defaultColorBtn.addActionListener(_ -> {
            applyTheme(java.awt.Color.WHITE, java.awt.Color.BLACK); // Default theme
            themeFrame.dispose();
        });
        themeCloseBtn.addActionListener(_ -> themeFrame.dispose());

        themeFrame.add(panel);
        themeFrame.setVisible(true);
    }

    private void applyTheme(java.awt.Color bg, java.awt.Color fg) {
        getContentPane().setBackground(bg);
        for (java.awt.Component comp : getContentPane().getComponents()) {
            setComponentTheme(comp, bg, fg);
        }
        repaint();
    }

    private void setComponentTheme(java.awt.Component comp, java.awt.Color bg, java.awt.Color fg) {
        if (comp instanceof JPanel || comp instanceof JScrollPane) {
            comp.setBackground(bg);
        }
        if (comp instanceof JLabel || comp instanceof JButton || comp instanceof JTextField || comp instanceof JComboBox
                || comp instanceof JTable || comp instanceof JTextArea) {
            comp.setForeground(fg);
            comp.setBackground(bg);
        }
        if (comp instanceof JPanel jPanel) {
            for (java.awt.Component child : jPanel.getComponents()) {
                setComponentTheme(child, bg, fg);
            }
        }
        if (comp instanceof JScrollPane jScrollPane) {
            java.awt.Component view = jScrollPane.getViewport().getView();
            if (view != null) {
                setComponentTheme(view, bg, fg);
            }
        }
        if (comp instanceof JTable jTable) {
            jTable.setForeground(fg);
            jTable.setBackground(bg);
            jTable.getTableHeader().setBackground(bg);
            jTable.getTableHeader().setForeground(fg);
        }
        if (comp instanceof JTextArea jTextArea) {
            jTextArea.setForeground(fg);
            jTextArea.setBackground(bg);
        }
    }

    // --- History Frame ---
    private void openHistoryFrameWithClose(JFrame parent) {
        JFrame historyFrame = new JFrame("Stocks History");
        // historyFrame.setSize(400, 750); // Commented out for full screen
        // historyFrame.setMinimumSize(new Dimension(320, 600)); // Commented out for full screen
        // historyFrame.setMaximumSize(new Dimension(480, 900)); // Commented out for full screen
        historyFrame.setLocationRelativeTo(parent);
        historyFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        historyFrame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Make the JFrame full screen

        JLabel selectedDateLabel = new JLabel("No date selected");
        JButton selectDateButton = new JButton("Select Date");
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.X_AXIS));
        filterPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        filterPanel.add(selectDateButton);
        filterPanel.add(Box.createRigidArea(new Dimension(12, 0)));
        filterPanel.add(selectedDateLabel);

        JTextField searchField = new JTextField();
        searchField.setMaximumSize(new Dimension(200, 28));
        searchField.setToolTipText("Search product name...");
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.X_AXIS));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        searchPanel.add(new JLabel("Search: "));
        searchPanel.add(searchField);

        String[] columnNames = { "No.", "Product Name", "Category", "Quantity", "Price", "Date" };
        DefaultTableModel histTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(histTableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(26);
        table.setFont(table.getFont().deriveFont(13f));
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(360, 600));
        scrollPane.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        java.util.List<String[]> filteredRecords = new ArrayList<>();
        Stack<String[]> historyDeletedStack = new Stack<>();

        JLabel totalSalesHistoryLabel = new JLabel("Total Sales (Current View): 0.00");
        JTextArea summaryArea = new JTextArea("Summary/Suggestions will appear here.", 3, 30);

        Runnable reloadTable = () -> {
            String search = searchField.getText().trim().toLowerCase();
            histTableModel.setRowCount(0);
            int num = 1;
            for (String[] row : filteredRecords) {
                // row[0] = Product Name, row[1] = Category
                if (search.isEmpty() ||
                    (row.length > 0 && row[0] != null && row[0].toLowerCase().contains(search)) ||
                    (row.length > 1 && row[1] != null && row[1].toLowerCase().contains(search))) {
                    histTableModel.addRow(new Object[] { num++, row[0], row[1], row[2], row[3], row[4].split(" ")[0] });
                }
            }

            // Calculate total sales for the currently displayed records in histTableModel
            double currentViewTotalSales = 0.0;
            for (int i = 0; i < histTableModel.getRowCount(); i++) {
                try {
                    // Assuming Quantity is at column index 3 and Price at column index 4 in histTableModel
                    int qty = Integer.parseInt(histTableModel.getValueAt(i, 3).toString());
                    double price = Double.parseDouble(histTableModel.getValueAt(i, 4).toString());
                    currentViewTotalSales += qty * price;
                } catch (NumberFormatException | NullPointerException ex) {
                    // Ignore if parsing fails for a row
                }
            }
            totalSalesHistoryLabel.setText("Total Sales (Current View): " + String.format("%.2f", currentViewTotalSales));
            // Placeholder for summary logic
            // summaryArea.setText("Analyzed " + histTableModel.getRowCount() + " records. More suggestions soon!");
        };

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() { // Anonymous inner class
        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) { reloadTable.run(); }
        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) { reloadTable.run(); }
        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) { reloadTable.run(); }
        });

        // --- Select Date Button (filter) ---
        selectDateButton.addActionListener(_ -> {
            JFrame dateFrame = new JFrame("Select Date");
            dateFrame.setSize(340, 160); // Set a smaller size
            dateFrame.setResizable(false);
            dateFrame.setLocationRelativeTo(historyFrame);

            javax.swing.SpinnerDateModel dateModel = new javax.swing.SpinnerDateModel();
            javax.swing.JSpinner dateSpinner = new javax.swing.JSpinner(dateModel);
            dateSpinner.setEditor(new javax.swing.JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
            dateSpinner.setPreferredSize(new Dimension(120, 28)); // Smaller spinner

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            panel.add(Box.createVerticalStrut(10));
            JPanel rowPanel = new JPanel();
            rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
            rowPanel.add(Box.createHorizontalGlue());
            rowPanel.add(new JLabel("Select Date:"));
            rowPanel.add(Box.createRigidArea(new Dimension(10, 0)));
            rowPanel.add(dateSpinner);
            rowPanel.add(Box.createHorizontalGlue());
            panel.add(rowPanel);
            panel.add(Box.createVerticalStrut(10));

            JButton okButton = new JButton("OK");
            JPanel okPanel = new JPanel();
            okPanel.setLayout(new BoxLayout(okPanel, BoxLayout.X_AXIS));
            okPanel.add(Box.createHorizontalGlue());
            okPanel.add(okButton);
            okPanel.add(Box.createHorizontalGlue());
            panel.add(okPanel);

            okButton.addActionListener(_ -> {
                java.util.Date selected = (java.util.Date) dateSpinner.getValue();
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                String selectedDate = sdf.format(selected);
                selectedDateLabel.setText(selectedDate);

                // Filter records for selected date
                filteredRecords.clear();
                for (String[] row : historyList) {
                    String dateOnly = row[4].split(" ")[0]; // Extract only the date part from the history entry
                    if (dateOnly.equals(selectedDate)) {
                        filteredRecords.add(row);
                    }
                }

                if (filteredRecords.isEmpty()) {
                    JOptionPane.showMessageDialog(historyFrame, "No records found for the selected date: " + selectedDate, "No Records", JOptionPane.INFORMATION_MESSAGE);
                }

                reloadTable.run();
                dateFrame.dispose();
            });

            dateFrame.add(panel);
            dateFrame.setVisible(true);
        });

        // --- Edit, Delete, Redo Buttons ---
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");
        JButton redoBtn = new JButton("Redo");

        editBtn.addActionListener(_ -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                String[] record = filteredRecords.get(selectedRow);
                JTextField editNameField = new JTextField(record[0]);
                JTextField editCategoryField = new JTextField(record[1]);
                JTextField editQtyField = new JTextField(record[2]);
                JTextField editPriceField = new JTextField(record[3]);
                JPanel panel = new JPanel(new GridLayout(4, 2));
                panel.add(new JLabel("Product Name:"));
                panel.add(editNameField);
                panel.add(new JLabel("Category:"));
                panel.add(editCategoryField);
                panel.add(new JLabel("Quantity:"));
                panel.add(editQtyField);
                panel.add(new JLabel("Price:"));
                panel.add(editPriceField);
                int result = JOptionPane.showConfirmDialog(historyFrame, panel, "Edit Record", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    record[0] = editNameField.getText().trim();
                    record[1] = editCategoryField.getText().trim();
                    record[2] = editQtyField.getText().trim();
                    record[3] = editPriceField.getText().trim();
                    // Update in historyList as well
                    for (String[] hist : historyList) {
                        if (hist[3].equals(record[3])) {
                            hist[0] = record[0];
                            hist[1] = record[1];
                            hist[2] = record[2];
                            break;
                        }
                    }
                    reloadTable.run();
                    saveHistoryToFile();
                }
            }
        });

        deleteBtn.addActionListener(_ -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                String[] removed = filteredRecords.remove(selectedRow);
                historyDeletedStack.push(removed);
                // Remove from historyList as well
                historyList.removeIf(hist -> hist[3].equals(removed[3]));
                reloadTable.run();
                saveHistoryToFile();
            }
        });

        redoBtn.addActionListener(_ -> {
            if (!historyDeletedStack.isEmpty()) {
                String[] restored = historyDeletedStack.pop();
                filteredRecords.add(restored);
                historyList.add(restored);
                reloadTable.run();
                saveHistoryToFile();
            }
        });

        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.X_AXIS));
        btnPanel.add(editBtn);
        btnPanel.add(Box.createRigidArea(new Dimension(8, 0)));
        btnPanel.add(deleteBtn);
        btnPanel.add(Box.createRigidArea(new Dimension(8, 0)));
        btnPanel.add(redoBtn);

        // --- Total Sales and Summary Area ---
        totalSalesHistoryLabel.setFont(totalSalesHistoryLabel.getFont().deriveFont(Font.BOLD, 16f));
        totalSalesHistoryLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        summaryArea.setEditable(false);
        summaryArea.setLineWrap(true);
        summaryArea.setWrapStyleWord(true);
        summaryArea.setFont(summaryArea.getFont().deriveFont(13f));
        JScrollPane summaryScrollPane = new JScrollPane(summaryArea);

        JButton closeHistoryBtn = new JButton(closeIcon);
        closeHistoryBtn.setToolTipText("Close");
        closeHistoryBtn.setPreferredSize(new Dimension(closeBtnWidth, closeBtnHeight));
        closeHistoryBtn.setMaximumSize(new Dimension(closeBtnWidth, closeBtnHeight));
        closeHistoryBtn.setMinimumSize(new Dimension(closeBtnWidth, closeBtnHeight));
        closeHistoryBtn.setAlignmentX(CENTER_ALIGNMENT);
        closeHistoryBtn.setBorderPainted(false);
        closeHistoryBtn.setFocusPainted(false);
        closeHistoryBtn.setContentAreaFilled(false);
        closeHistoryBtn.addActionListener(_ -> historyFrame.dispose());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(filterPanel);
        mainPanel.add(searchPanel);
        mainPanel.add(scrollPane);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        mainPanel.add(btnPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(totalSalesHistoryLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(summaryScrollPane);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(closeHistoryBtn);
        mainPanel.add(Box.createVerticalGlue());

        historyFrame.add(mainPanel);
        historyFrame.setVisible(true);
    }
    // Helper to find the real index in historyList


    // --- File Handling ---
    private void saveHistoryToFile() {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(HISTORY_FILE))) {
            for (String[] row : historyList) {
                for (int i = 0; i < row.length; i++) {
                    writer.print(row[i].replace("\t", " ").replace("\n", " "));
                    if (i < row.length - 1) writer.print("\t");
                }
                writer.println();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to save history: " + e.getMessage());
        }
    }

    private void loadHistoryFromFile() {
        historyList.clear();
        java.io.File file = new java.io.File(HISTORY_FILE);
        if (!file.exists()) return;
        try (java.util.Scanner scanner = new java.util.Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] row = line.split("\t", -1);
                if (row.length == 5) {
                    historyList.add(row);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load history: " + e.getMessage());
        }
    }

    private void saveStockListToFile() {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(STOCK_FILE))) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String pname = tableModel.getValueAt(i, 1).toString().replace("\t", " "); // Product Name
                String category = tableModel.getValueAt(i, 2).toString().replace("\t", " ");
                String qty = tableModel.getValueAt(i, 3).toString().replace("\t", " ");
                String price = tableModel.getValueAt(i, 4).toString().replace("\t", " ");
                writer.println(pname + "\t" + category + "\t" + qty + "\t" + price);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to save stock list: " + e.getMessage());
        }
    }

    public void setCategoryComboBox(JComboBox<String> categoryComboBox) {
        this.categoryComboBox = categoryComboBox;
    }

    // --- Apply Theme to All Frames ---
    // Call this method when the user selects a theme
    public void applyThemeToAllFrames(Color bgColor, Color fgColor) {

        // Apply to main frame
        applyThemeToFrame(this, bgColor, fgColor);

        // Optionally, keep track of other open JFrames and apply to them as well
        for (Frame frame : Frame.getFrames()) {
            if (frame instanceof JFrame && frame.isVisible()) {
                applyThemeToFrame((JFrame) frame, bgColor, fgColor);
            }
        }
    }

    // Recursively apply theme to all components in a JFrame
    private void applyThemeToFrame(JFrame frame, Color bgColor, Color fgColor) {
        applyThemeToComponent(frame.getContentPane(), bgColor, fgColor);
        frame.getContentPane().setBackground(bgColor);
        frame.getContentPane().setForeground(fgColor);
        frame.repaint();
    }
    private void applyThemeToComponent(Component comp, Color bgColor, Color fgColor) {
        // General background and foreground
        comp.setBackground(bgColor);
        comp.setForeground(fgColor);

        // Specific handling for components that contain other components
        if (comp instanceof JPanel || comp instanceof JScrollPane || comp instanceof JViewport) {
            // For containers, iterate through children
            if (comp instanceof Container container) {
                for (Component child : container.getComponents()) {
                    applyThemeToComponent(child, bgColor, fgColor);
                }
            }
        } else if (comp instanceof JTable table) {
            table.getTableHeader().setBackground(bgColor);
            table.getTableHeader().setForeground(fgColor);
            // Cell renderers might need custom handling if they don't respect table's fg/bg
        }
        // Add more specific component handling if needed
    }

    private void appendBlinkingAlert(String msg) {
        StyledDocument doc = alertArea.getStyledDocument();
        SimpleAttributeSet redAttribute = new SimpleAttributeSet();
        StyleConstants.setForeground(redAttribute, Color.RED);

        SimpleAttributeSet defaultAttribute = new SimpleAttributeSet();
        StyleConstants.setForeground(defaultAttribute, alertArea.getForeground()); // Use current default foreground

        try {
            int insertionPoint = doc.getLength();
            String textToInsert = (insertionPoint > 0 ? "\n" : "") + msg;
            doc.insertString(insertionPoint, textToInsert, redAttribute); // Insert initially in red

            final int messageStartOffset = insertionPoint + (insertionPoint > 0 ? 1 : 0); // Actual start of the message
            final int messageLength = msg.length();

            final int BLINK_DURATION_MS = 3000;
            final int BLINK_INTERVAL_MS = 500; // Blinks twice a second
            final int[] blinkCount = {0};
            final int maxBlinks = BLINK_DURATION_MS / BLINK_INTERVAL_MS;
            final boolean[] isCurrentlyRed = {true};

            javax.swing.Timer blinkTimer = new javax.swing.Timer(BLINK_INTERVAL_MS, null);
            blinkTimer.addActionListener(e -> {
                StyleConstants.setForeground(isCurrentlyRed[0] ? defaultAttribute : redAttribute, isCurrentlyRed[0] ? alertArea.getForeground() : Color.RED);
                doc.setCharacterAttributes(messageStartOffset, messageLength, isCurrentlyRed[0] ? defaultAttribute : redAttribute, false);
                isCurrentlyRed[0] = !isCurrentlyRed[0];
                blinkCount[0]++;
                if (blinkCount[0] >= maxBlinks) {
                    doc.setCharacterAttributes(messageStartOffset, messageLength, redAttribute, false); // Ensure it ends red
                    ((javax.swing.Timer) e.getSource()).stop();
                }
            });
            blinkTimer.setRepeats(true);
            blinkTimer.start();
        } catch (BadLocationException e) {
            System.err.println("Failed to append blinking alert: " + e.getMessage());
            // Fallback to simple text append if styling fails
            alertArea.setText(alertArea.getText() + (alertArea.getText().isEmpty() ? "" : "\n") + msg);
        }
    }

    private void updateStockListNumbers() {
        // Update the "No." column to be sequential (1-based) for all rows
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt(i + 1, i, 0);
        }
    }

    public Map<String, Integer> getProductThresholds() {
        return productThresholds;
    }
    private void updateRestockSuggestions() {
        restockSuggestionTableModel.setRowCount(0); // Clear existing suggestions
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String productName = tableModel.getValueAt(i, 1).toString();
            String qtyStr = tableModel.getValueAt(i, 3).toString();
            try {
                int qtyValue = Integer.parseInt(qtyStr);
                int threshold = productThresholds.getOrDefault(productName, warnAtQuantity);
                if (qtyValue <= threshold) {
                    restockSuggestionTableModel.addRow(new Object[]{productName, qtyValue});
                }
            } catch (NumberFormatException ex) {
                // Ignore if quantity is not a valid number for a row
            }
        }
    }

    // Method to open the detailed restock suggestions frame
    private void openRestockSuggestionsDetailFrame() {
        RestockSuggestionsDetailFrame detailFrame = new RestockSuggestionsDetailFrame(restockSuggestionTableModel, this);
        detailFrame.setVisible(true);
    }
}

// Helper class for JComboBox filtering DocumentListener
class CategoryDocumentListener implements javax.swing.event.DocumentListener {
    private final JComboBox<String> comboBox;
    private final JTextField editor;
    private final java.util.List<String> allItems;
    private boolean isFilteringActive = false;

    public CategoryDocumentListener(JComboBox<String> comboBox, JTextField editor, java.util.List<String> allItems) {
        this.comboBox = comboBox;
        this.editor = editor;
        this.allItems = allItems;
    }

    public void setFilteringActive(boolean active) {
        this.isFilteringActive = active;
    }

    private void filter() {
        if (isFilteringActive) {
            return;
        }
        isFilteringActive = true;

        SwingUtilities.invokeLater(() -> {
            String text = editor.getText();
            DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) comboBox.getModel();
            model.removeAllElements();

            if (text.isEmpty()) {
                for (String item : allItems) {
                    model.addElement(item);
                }
            } else {
                for (String item : allItems) {
                    if (item.toLowerCase().contains(text.toLowerCase())) {
                        model.addElement(item);
                    }
                }
            }
            comboBox.getEditor().setItem(text); // Restore editor text
            if (model.getSize() > 0 && editor.hasFocus()) {
                comboBox.showPopup();
            }
            isFilteringActive = false;
        });
    }
    @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
    @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
    @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
}

// New JFrame class for displaying detailed restock suggestions
class RestockSuggestionsDetailFrame extends JFrame {
    public RestockSuggestionsDetailFrame(DefaultTableModel model, Component parentComponent) {
        setTitle("Restock Suggestions");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(350, 500); // Slim, not too wide, decent height for scrollable content
        setLocationRelativeTo(parentComponent); // Open relative to the main app window

        JTable detailedSuggestionsTable = new JTable(model);
        detailedSuggestionsTable.setFont(detailedSuggestionsTable.getFont().deriveFont(15f)); // Bigger text
        detailedSuggestionsTable.setRowHeight(25); // Adjust row height for bigger font
        
        // Apply similar green styling for consistency, or choose a different one
        detailedSuggestionsTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(new Color(230, 255, 230)); // Light green background
                c.setForeground(new Color(0, 100, 0));    // Dark green text
                if (isSelected) {
                    c.setBackground(table.getSelectionBackground());
                    c.setForeground(table.getSelectionForeground());
                }
                setHorizontalAlignment(javax.swing.SwingConstants.CENTER); // Center text in cells
                return c;
            }
        });
        
        // Ensure column widths are reasonable for the new frame
        if (detailedSuggestionsTable.getColumnCount() > 0) { // Model has "Product to Restock", "Current Stock"
            detailedSuggestionsTable.getColumnModel().getColumn(0).setPreferredWidth(200); // Product Name
            detailedSuggestionsTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Current Stock
        }

        JScrollPane scrollPane = new JScrollPane(detailedSuggestionsTable);
        add(scrollPane, BorderLayout.CENTER); // Add the scrollable table to the frame
    }
}
