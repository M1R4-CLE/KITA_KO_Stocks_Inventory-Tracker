import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class Orgn_Mobile_version_TEST extends JFrame {
    // --- Fields ---
    private JComboBox<String> nameComboBox;
    private final DefaultTableModel tableModel = new DefaultTableModel(new String[] { "Product Name", "Category", "Stocks", "Price" }, 0);
    private final JTable productTable;
    private final JTextField nameField, qtyField, priceField;
    private final JTextArea alertArea;
    private final JButton addButton, editButton, deleteButton, redoButton, clearButton, saveButton, editAlertButton, showStocksButton, settingsBtn;
    private final JLabel warnInfoLabel, warnAmountLabel;
    private final java.util.List<String[]> historyList = new ArrayList<>();
    private final Set<String> productNames = new HashSet<>();
    private final Map<String, Integer> productThresholds = new HashMap<>();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");
    private final Stack<Object[]> deletedStack = new Stack<>();
    private int warnAtQuantity = 5;
    private boolean isDarkTheme = false;
    private static final String HISTORY_FILE = "history.txt";
    private static final String STOCK_FILE = "stocks.txt";
    private final int closeBtnWidth = 90, closeBtnHeight = 31;
    private ImageIcon closeIcon;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Orgn_Mobile_version_TEST app = new Orgn_Mobile_version_TEST();
            app.setVisible(true);
        });
    }
    private JButton editSaveButton;
    private final JComboBox<String> categoryComboBox;
    private final JTextField categoryField;

    public Orgn_Mobile_version_TEST() {
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
        setSize(400, 750);
        setMinimumSize(new Dimension(320, 600));
        setMaximumSize(new Dimension(480, 900));
        setLocationRelativeTo(null);

        // --- Main Panel ---
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // --- Product Entry Form ---
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createTitledBorder("Product Entry"));
        formPanel.setMaximumSize(new Dimension(380, 150));
        formPanel.setAlignmentX(LEFT_ALIGNMENT);

        // Name
        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.X_AXIS));
        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setPreferredSize(new Dimension(60, 28));
        nameComboBox = new JComboBox<>();
        nameComboBox.setEditable(true);
        nameComboBox.setMaximumSize(new Dimension(180, 28));
        nameField = (JTextField) nameComboBox.getEditor().getEditorComponent();
        namePanel.add(nameLabel);
        namePanel.add(Box.createRigidArea(new Dimension(4, 0)));
        namePanel.add(nameComboBox);
        formPanel.add(namePanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 4)));

        // Category
        JPanel categoryPanel = new JPanel();
        categoryPanel.setLayout(new BoxLayout(categoryPanel, BoxLayout.X_AXIS));
        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setPreferredSize(new Dimension(60, 28));
        categoryComboBox = new JComboBox<>(new String[] {
            "Liquor", "Soaps", "Shampoo", "Toothpaste", "Snacks", "School Supplies", "Candies", 
            "Cigarettes", "Toys", "Dish Washing Liquid", "Condements", "Laundry Soap", "Laundry Conditioner",
            "Detergent", "Canned Goods", "Ice Cream", "Coffee", "Milks", "Noodles", "Incontinence Products", "Ointments", 
            "First Aid", "Medicine", "Vitamins", "Pet Foods", "Pet Supplies", "Beverages", "Frozen Foods", "Fruits", "Vegetables", "Protien Foods"
        });
        categoryComboBox.setEditable(true);
        categoryComboBox.setMaximumSize(new Dimension(180, 28));
        categoryField = (JTextField) categoryComboBox.getEditor().getEditorComponent();
        categoryPanel.add(categoryLabel);
        categoryPanel.add(Box.createRigidArea(new Dimension(4, 0)));
        categoryPanel.add(categoryComboBox);
        formPanel.add(categoryPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 4)));

        // Qty
        JPanel qtyPanel = new JPanel();
        qtyPanel.setLayout(new BoxLayout(qtyPanel, BoxLayout.X_AXIS));
        JLabel qtyLabel = new JLabel("Qty:");
        qtyLabel.setPreferredSize(new Dimension(60, 28));
        qtyField = new JTextField(5);
        qtyField.setMaximumSize(new Dimension(80, 28));
        qtyPanel.add(qtyLabel);
        qtyPanel.add(Box.createRigidArea(new Dimension(4, 0)));
        qtyPanel.add(qtyField);
        formPanel.add(qtyPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 4)));

        // Price
        JPanel pricePanel = new JPanel();
        pricePanel.setLayout(new BoxLayout(pricePanel, BoxLayout.X_AXIS));
        JLabel priceLabel = new JLabel("Price:");
        priceLabel.setPreferredSize(new Dimension(60, 28));
        priceField = new JTextField(7);
        priceField.setMaximumSize(new Dimension(100, 28));
        pricePanel.add(priceLabel);
        pricePanel.add(Box.createRigidArea(new Dimension(4, 0)));
        pricePanel.add(priceField);
        formPanel.add(pricePanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        addButton = new JButton("Add Product");
        addButton.setAlignmentX(CENTER_ALIGNMENT);
        addButton.setMaximumSize(new Dimension(140, 32));
        formPanel.add(addButton);

        mainPanel.add(formPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        // --- Product Table ---

        productTable = new JTable(tableModel);
        productTable.setFont(productTable.getFont().deriveFont(java.awt.Font.PLAIN, 13f));
        productTable.setRowHeight(24);
        productTable.getTableHeader().setFont(productTable.getTableHeader().getFont().deriveFont(13f));
        productTable.getColumnModel().getColumn(0).setPreferredWidth(120);

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
                    int qty = Integer.parseInt(table.getValueAt(row, 1).toString());
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
        tableScroll.setPreferredSize(new Dimension(360, 250));
        tableScroll.setMaximumSize(new Dimension(380, 270));
        tableScroll.setMinimumSize(new Dimension(320, 150));
        tableScroll.setAlignmentX(LEFT_ALIGNMENT);
        mainPanel.add(tableScroll);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 18)));

        // --- Table Action Buttons ---
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
        buttonPanel.setAlignmentX(LEFT_ALIGNMENT);
        mainPanel.add(buttonPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        // --- Alerts Panel ---
        JPanel alertPanel = new JPanel(new BorderLayout());
        alertPanel.setBorder(BorderFactory.createTitledBorder("Alerts & More"));
        alertPanel.setMaximumSize(new Dimension(380, 240));
        alertPanel.setPreferredSize(new Dimension(380, 240));
        alertPanel.setAlignmentX(LEFT_ALIGNMENT);

        warnAmountLabel = new JLabel("Warn at quantity: " + warnAtQuantity);
        warnAmountLabel.setFont(warnAmountLabel.getFont().deriveFont(16f));
        warnAmountLabel.setHorizontalAlignment(JLabel.LEFT);

        alertArea = new JTextArea(10, 45);
        alertArea.setFont(alertArea.getFont().deriveFont(16f));
        alertArea.setEditable(false);
        alertArea.setText("");
        JScrollPane alertScrollPane = new JScrollPane(alertArea);
        alertScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        alertPanel.add(alertScrollPane, BorderLayout.CENTER);

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

        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(alertPanel);

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
                    this,
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
        alertPanel.add(settingsPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        // --- Button Actions ---
        addButton.addActionListener(_ -> addProduct());
        editButton.addActionListener(_ -> editProduct());
        deleteButton.addActionListener(_ -> deleteProduct());
        redoButton.addActionListener(_ -> redoProduct());
        clearButton.addActionListener(_ -> clearProducts());
        saveButton.addActionListener(_ -> saveProductsToHistory());

        // Keyboard navigation
        nameField.addActionListener(_ -> qtyField.requestFocusInWindow());
        qtyField.addActionListener(_ -> priceField.requestFocusInWindow());
        priceField.addActionListener(_ -> addButton.doClick());

        // Edit Alert Button
        editAlertButton.addActionListener(_ -> openEditWarnFrame());

        // Load data from files
        loadHistoryFromFile();
        loadStockListFromFile();

        // Save on close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveHistoryToFile();
                saveStockListToFile();
            }
        });
    }

    // --- Product Actions ---
    private void addProduct() {
        String productName = nameField.getText().trim();
        String category = categoryField.getText().trim();
        String qty = qtyField.getText().trim();
        String price = priceField.getText().trim();

        // Check for duplicate product name in the table
        boolean duplicate = false;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String existingName = tableModel.getValueAt(i, 0).toString();
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
            categoryField.setText("");
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
            tableModel.addRow(new Object[] { productName, category, qty, price });
            String dateTime = LocalDateTime.now().format(dtf);
            historyList.add(new String[] { productName, category, qty, price, dateTime });
            checkLowStock(productName, qty);
        }
        nameField.setText("");
        categoryField.setText("");
        qtyField.setText("");
        priceField.setText("");
        nameField.requestFocusInWindow();
    }

    private void editProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow >= 0) {
            String name = (String) tableModel.getValueAt(selectedRow, 0);
            String category = tableModel.getValueAt(selectedRow, 1).toString();
            String qty = tableModel.getValueAt(selectedRow, 2).toString();
            String price = tableModel.getValueAt(selectedRow, 3).toString();
            openEditFrame(selectedRow, name, category, qty, price);
        }
    }

    private void deleteProduct() {
        int rowToDelete = productTable.getSelectedRow();
        if (rowToDelete >= 0) {
            Object[] deletedRow = {
                    tableModel.getValueAt(rowToDelete, 0),
                    tableModel.getValueAt(rowToDelete, 1),
                    tableModel.getValueAt(rowToDelete, 2),
                    tableModel.getValueAt(rowToDelete, 3)
            };
            deletedStack.push(deletedRow);
            tableModel.removeRow(rowToDelete);
        }
    }

    private void redoProduct() {
        if (!deletedStack.isEmpty()) {
            Object[] restored = deletedStack.pop();
            tableModel.addRow(restored);
        }
    }

    private void clearProducts() {
        tableModel.setRowCount(0);
    }

    private void saveProductsToHistory() {
        int added = 0;
        String dateTime = LocalDateTime.now().format(dtf);
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String pname = tableModel.getValueAt(i, 0).toString();
            String category = tableModel.getValueAt(i, 1).toString();
            String qty = tableModel.getValueAt(i, 2).toString();
            String price = tableModel.getValueAt(i, 3).toString();
            boolean alreadyInHistory = false;
            for (String[] hist : historyList) {
                // Check if this product, category, qty, price, and date already exist in history
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
                if (!alertArea.getText().contains(msg)) {
                    alertArea.append((alertArea.getText().isEmpty() ? "" : "\n") + msg);
                }
                Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(this, msg, "Low Stock Alert", JOptionPane.WARNING_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            // Ignore if qty is not a number
        }
    }

    // --- Edit Product Frame ---
    private void openEditFrame(int rowIndex, String name, String category, String qty, String price) {
        JFrame editFrame = new JFrame("Edit Product");
        editFrame.setSize(350, 200);
        editFrame.setLocationRelativeTo(this);
        editFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel nameLabel = new JLabel("Product Name:");
        JTextField editNameField = new JTextField(name, 15);
        JLabel categoryLabel = new JLabel("Category:");
        JTextField editCategoryField = new JTextField(category, 15);
        JLabel qtyLabel = new JLabel("Quantity:");
        JTextField editQtyField = new JTextField(qty, 5);
        JLabel priceLabel = new JLabel("Price:");
        JTextField editPriceField = new JTextField(price, 7);
        editSaveButton = new JButton("Save");

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(nameLabel, gbc);
        gbc.gridx = 1;
        panel.add(editNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(categoryLabel, gbc);
        gbc.gridx = 1;
        panel.add(editCategoryField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(qtyLabel, gbc);
        gbc.gridx = 1;
        panel.add(editQtyField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(priceLabel, gbc);
        gbc.gridx = 1;
        panel.add(editPriceField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        panel.add(editSaveButton, gbc);

        editFrame.add(panel);
        editFrame.setVisible(true);

        editSaveButton.addActionListener(_ -> {
            String newName = editNameField.getText().trim();
            String newCategory = editCategoryField.getText().trim();
            String newQty = editQtyField.getText().trim();
            String newPrice = editPriceField.getText().trim();
            if (!newName.isEmpty() && !newCategory.isEmpty() && !newQty.isEmpty() && !newPrice.isEmpty()) {
                tableModel.setValueAt(newName, rowIndex, 0);
                tableModel.setValueAt(newCategory, rowIndex, 1);
                tableModel.setValueAt(newQty, rowIndex, 2);
                tableModel.setValueAt(newPrice, rowIndex, 3);
                if (!productNames.contains(newName)) {
                    productNames.add(newName);
                    nameComboBox.addItem(newName);
                }
                editFrame.dispose();
                productTable.clearSelection();
            }
        });
    }

    // --- Edit Warn Quantity Frame ---
    private void openEditWarnFrame() {
        JFrame editWarnFrame = new JFrame("Edit Warn Quantity");
        editWarnFrame.setSize(400, 220);
        editWarnFrame.setLocationRelativeTo(this);
        editWarnFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

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
                        String pname = tableModel.getValueAt(i, 0).toString();
                        String qtyStr = tableModel.getValueAt(i, 1).toString();
                        try {
                            int qty = Integer.parseInt(qtyStr);
                            if (qty <= warnAtQuantity) {
                                String msg = "ALERT: '" + pname + "' is low on stock (" + qty + ")!";
                                if (!alertArea.getText().contains(msg)) {
                                    alertArea.append((alertArea.getText().isEmpty() ? "" : "\n") + msg);
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
        stocksFrame.setSize(400, 750); // Match main frame
        stocksFrame.setMinimumSize(new Dimension(320, 600));
        stocksFrame.setMaximumSize(new Dimension(480, 900));
        stocksFrame.setLocationRelativeTo(this);
        stocksFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        DefaultTableModel stocksModel = new DefaultTableModel(new String[] { "Product Name", "Category", "Quantity", "Price" }, 0);
        JTable stocksTable = new JTable(stocksModel);

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String pname = tableModel.getValueAt(i, 0).toString();
            String category = tableModel.getValueAt(i, 1).toString();
            String qty = tableModel.getValueAt(i, 2).toString();
            String price = tableModel.getValueAt(i, 3).toString();
            stocksModel.addRow(new String[] { pname, category, qty, price });
        }

        // Center align all columns in the Stocks History table
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        for (int i = 0; i < stocksTable.getColumnCount(); i++) {
            stocksTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane stocksScroll = new JScrollPane(stocksTable);
        stocksScroll.setPreferredSize(new Dimension(360, 600)); // Adjusted for taller frame

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
        contentPanel.add(closePanel);

        stocksFrame.setContentPane(contentPanel);
        stocksFrame.setVisible(true);
    }

    // --- Settings Frame (Theme & History) ---
    private void openSettingsFrame() {
        JFrame settingsFrame = new JFrame("Settings");
        settingsFrame.setSize(400, 750);
        settingsFrame.setMinimumSize(new Dimension(320, 600));
        settingsFrame.setMaximumSize(new Dimension(480, 900));
        settingsFrame.setLocationRelativeTo(this);
        settingsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

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
        themeFrame.setSize(320, 220);
        themeFrame.setMinimumSize(new Dimension(280, 180));
        themeFrame.setLocationRelativeTo(parent);
        themeFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

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
        darkBtn.setAlignmentX(CENTER_ALIGNMENT);
        lightBtn.setAlignmentX(CENTER_ALIGNMENT);
        darkBtn.setMaximumSize(new Dimension(160, 36));
        lightBtn.setMaximumSize(new Dimension(160, 36));
        panel.add(darkBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(lightBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 24)));

        JButton themeCloseBtn = new JButton(closeIcon);
        themeCloseBtn.setToolTipText("Close");
        themeCloseBtn.setPreferredSize(new Dimension(closeBtnWidth, closeBtnHeight));
        themeCloseBtn.setMaximumSize(new Dimension(closeBtnWidth, closeBtnHeight));
        themeCloseBtn.setMinimumSize(new Dimension(closeBtnWidth, closeBtnHeight));
        themeCloseBtn.setAlignmentX(CENTER_ALIGNMENT);
        themeCloseBtn.setBorderPainted(false);
        themeCloseBtn.setFocusPainted(false);
        themeCloseBtn.setContentAreaFilled(false);
        panel.add(themeCloseBtn);

        darkBtn.addActionListener(_ -> {
            isDarkTheme = true;
            applyTheme();
            themeFrame.dispose();
        });
        lightBtn.addActionListener(_ -> {
            isDarkTheme = false;
            applyTheme();
            themeFrame.dispose();
        });
        themeCloseBtn.addActionListener(_ -> themeFrame.dispose());

        themeFrame.add(panel);
        themeFrame.setVisible(true);
    }

    private void applyTheme() {
        java.awt.Color bg, fg;
        if (isDarkTheme) {
            bg = java.awt.Color.DARK_GRAY;
            fg = java.awt.Color.WHITE;
        } else {
            bg = java.awt.Color.WHITE;
            fg = java.awt.Color.BLACK;
        }
        setBackground(bg);
        for (java.awt.Component comp : getContentPane().getComponents()) {
            setComponentTheme(comp, bg, fg);
            setTitledBorderColor(comp, fg);
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
                setTitledBorderColor(child, fg);
            }
        }
        if (comp instanceof JScrollPane jScrollPane) {
            java.awt.Component view = jScrollPane.getViewport().getView();
            if (view != null)
                setComponentTheme(view, bg, fg);
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

    private void setTitledBorderColor(java.awt.Component comp, java.awt.Color fg) {
        if (comp instanceof JPanel jPanel && jPanel.getBorder() instanceof javax.swing.border.TitledBorder tb) {
            tb.setTitleColor(fg);
        }
        if (comp instanceof JPanel jPanel) {
            for (java.awt.Component child : jPanel.getComponents()) {
                setTitledBorderColor(child, fg);
            }
        }
    }

    // --- History Frame ---
    private void openHistoryFrameWithClose(JFrame parent) {
        JFrame historyFrame = new JFrame("Stocks History");
        historyFrame.setSize(400, 750);
        historyFrame.setMinimumSize(new Dimension(320, 600));
        historyFrame.setMaximumSize(new Dimension(480, 900));
        historyFrame.setLocationRelativeTo(parent);
        historyFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

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

        Runnable reloadTable = () -> {
            String search = searchField.getText().trim().toLowerCase();
            histTableModel.setRowCount(0);
            int num = 1;
            for (String[] row : filteredRecords) {
                // row[0] = Product Name, row[1] = Category
                if (search.isEmpty() ||
                    row[0].toLowerCase().contains(search) ||
                    row[1].toLowerCase().contains(search)) {
                    histTableModel.addRow(new Object[] { num++, row[0], row[1], row[2], row[3], row[4].split(" ")[0] });
                }
            }
        };

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
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
            dateFrame.setSize(400, 200);
            dateFrame.setLocationRelativeTo(historyFrame);

            javax.swing.SpinnerDateModel dateModel = new javax.swing.SpinnerDateModel();
            javax.swing.JSpinner dateSpinner = new javax.swing.JSpinner(dateModel);
            dateSpinner.setEditor(new javax.swing.JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(Box.createVerticalStrut(20));
            JPanel rowPanel = new JPanel();
            rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
            rowPanel.add(Box.createHorizontalGlue());
            rowPanel.add(new JLabel("Select Date:"));
            rowPanel.add(Box.createRigidArea(new Dimension(10, 0)));
            rowPanel.add(dateSpinner);
            rowPanel.add(Box.createHorizontalGlue());
            panel.add(rowPanel);
            panel.add(Box.createVerticalStrut(20));

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
                    String dateOnly = row[3].split(" ")[0];
                    if (dateOnly.equals(selectedDate)) {
                        filteredRecords.add(new String[] { row[0], row[1], row[2], row[3] });
                    }
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
                String pname = tableModel.getValueAt(i, 0).toString().replace("\t", " ");
                String category = tableModel.getValueAt(i, 1).toString().replace("\t", " ");
                String qty = tableModel.getValueAt(i, 2).toString().replace("\t", " ");
                String price = tableModel.getValueAt(i, 3).toString().replace("\t", " ");
                writer.println(pname + "\t" + category + "\t" + qty + "\t" + price);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to save stocks: " + e.getMessage());
        }
    }

    private void loadStockListFromFile() {
        tableModel.setRowCount(0);
        java.io.File file = new java.io.File(STOCK_FILE);
        if (!file.exists()) return;
        try (java.util.Scanner scanner = new java.util.Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] row = line.split("\t", -1);
                if (row.length == 4) {
                    tableModel.addRow(row);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load stocks: " + e.getMessage());
        }
    }

    public Map<String, Integer> getProductThresholds() {
        return productThresholds;
    }
    // Fade in a JFrame (Java 9+)
    // Example usage of fadeInFrame (uncomment and place inside a method if needed):
    // yourButton.addActionListener(e -> {
    //     JFrame newFrame = new JFrame("Transition Example");
    //     // ... setup frame ...
    //     fadeInFrame(newFrame);
    // });

}
