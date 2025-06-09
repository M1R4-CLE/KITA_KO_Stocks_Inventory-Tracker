import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class Orgn_Mobile_version extends JFrame {
    // Fields
    private Set<String> productNames = new HashSet<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Orgn_Mobile_version app = new Orgn_Mobile_version();
            app.setVisible(true);
        });
    }
    private final JComboBox<String> nameComboBox;
    private final DefaultTableModel tableModel;
    private final JTable productTable;
    private java.util.List<String[]> historyList = new ArrayList<>();
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");
    private Stack<Object[]> deletedStack = new Stack<>();
    private boolean isDarkTheme = false;
    private final JLabel warnInfoLabel;
    private final JTextArea alertArea;
    private final JPanel alertPanel;
    private final int closeBtnWidth = 90, closeBtnHeight = 31;
    private ImageIcon closeIcon;
    private final JButton showRestocksButton;
    private final JButton settingsBtn;
    private final JTextField nameField;
    private JTextField qtyField, priceField;
    private JButton addButton;
    private Map<String, Integer> productThresholds = new HashMap<>();
    // Default warn quantity
    JButton editAlertButton = new JButton("Edit");
    // ... other code ...
    // ... other code ...    private int warnAtQuantity = 5; // Default value
    private Integer warnAtQuantity;

    public Orgn_Mobile_version() {
        loadHistoryFromFile();

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

        // Main container
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Product Entry Form
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createTitledBorder("Product Entry"));
        formPanel.setMaximumSize(new Dimension(380, 150));
        formPanel.setPreferredSize(new Dimension(380, 150));
        formPanel.setAlignmentX(LEFT_ALIGNMENT);

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

        // Product Table
        String[] columns = { "Product Name", "Stocks", "Price" };
        tableModel = new DefaultTableModel(columns, 0);
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
                        c.setBackground(java.awt.Color.PINK); // or Color.RED for stronger highlight
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

        // Table action buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");
        JButton redoButton = new JButton("Redo");
        JButton clearButton = new JButton("Clear");
        JButton saveButton = new JButton("Save");
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
        alertPanel = new JPanel(new BorderLayout());
        alertPanel.setBorder(BorderFactory.createTitledBorder("Alerts & More"));
        alertPanel.setMaximumSize(new Dimension(380, 240));
        alertPanel.setPreferredSize(new Dimension(380, 240));
        alertPanel.setAlignmentX(LEFT_ALIGNMENT);
        // Use the class field LOW_STOCK_THRESHOLD instead of declaring a local variable

        JLabel warnAmountLabel = new JLabel("Warn at quantity: " + warnAtQuantity);
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
        JButton saveAlertButton = new JButton("Save");
        saveAlertButton.setVisible(false);
        JPanel alertButtonPanel = new JPanel();
        alertButtonPanel.setLayout(new BoxLayout(alertButtonPanel, BoxLayout.X_AXIS));
        alertButtonPanel.add(editAlertButton);
        alertButtonPanel.add(saveAlertButton);
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

        // --- Settings and Restocks ---
        int settingsBtnWidth = 31, settingsBtnHeight = 31;
        ImageIcon settingsIcon = new ImageIcon(
                new javax.swing.ImageIcon("D:/KITAKO PORJECT/KITA_KO ICONS/Settings-removebg-preview.png")
                        .getImage().getScaledInstance(settingsBtnWidth, settingsBtnHeight, java.awt.Image.SCALE_SMOOTH)
        );
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

        showRestocksButton = new JButton("Show Restocks");
        showRestocksButton.setMaximumSize(new Dimension(140, 32));
        showRestocksButton.setAlignmentX(CENTER_ALIGNMENT);
        showRestocksButton.addActionListener(_ -> showRestocks());

        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.X_AXIS));
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
                dispose();
            }
        });

        settingsPanel.add(Box.createHorizontalGlue());
        settingsPanel.add(closeButton);
        settingsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        settingsPanel.add(showRestocksButton);
        settingsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        settingsPanel.add(settingsBtn);
        settingsPanel.add(Box.createHorizontalGlue());
        alertPanel.add(settingsPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        // --- Button Actions ---
        addButton.addActionListener(e -> addProduct());
        editButton.addActionListener(e -> editProduct());
        deleteButton.addActionListener(e -> deleteProduct());
        redoButton.addActionListener(e -> redoProduct());
        clearButton.addActionListener(e -> clearProducts());
        saveButton.addActionListener(e -> saveProductsToHistory());

        // Keyboard navigation
        nameField.addActionListener(e -> qtyField.requestFocusInWindow());
        qtyField.addActionListener(e -> priceField.requestFocusInWindow());
        priceField.addActionListener(e -> addButton.doClick());

        // Add the editAlertButton ActionListener here
        editAlertButton.addActionListener(e -> {
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

            JLabel messageLabel = new JLabel(""); // Message shown below input
            messageLabel.setFont(messageLabel.getFont().deriveFont(Font.PLAIN, 14f));
            messageLabel.setForeground(new Color(0, 128, 0));
            messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            panel.add(label);
            panel.add(Box.createRigidArea(new Dimension(0, 20)));
            panel.add(inputField);
            panel.add(Box.createRigidArea(new Dimension(0, 10)));
            panel.add(saveBtn);
            panel.add(Box.createRigidArea(new Dimension(0, 10)));
            panel.add(messageLabel);

            editWarnFrame.add(panel);

            // This function will be called by both Save button and Enter key
            Runnable saveAction = () -> {
                String text = inputField.getText().trim();
                try {
                    int newThreshold = Integer.parseInt(text);
                    if (newThreshold > 0) {
                        warnAtQuantity = newThreshold;
                        warnAmountLabel.setText("Warn at quantity: " + warnAtQuantity);
                        warnInfoLabel.setText("Warn threshold updated.");

                        // Build alert messages for products below threshold
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

                        // Close the frame after saving
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

            // Save button action
            saveBtn.addActionListener(_ -> saveAction.run());

            // Enter key in input field triggers save
            inputField.addActionListener(_ -> saveAction.run());

            editWarnFrame.setVisible(true);
        });
    }

    // --- Product Actions ---
    private void addProduct() {
        String productName = nameField.getText().trim();
        String qty = qtyField.getText().trim();
        String price = priceField.getText().trim();
        if (!productName.isEmpty() && !qty.isEmpty() && !price.isEmpty()) {
            if (!productNames.contains(productName)) {
                productNames.add(productName);
                nameComboBox.addItem(productName);
            }
            tableModel.addRow(new Object[] { productName, qty, price });
            String dateTime = LocalDateTime.now().format(dtf);
            historyList.add(new String[] { productName, qty, price, dateTime });
            checkLowStock(productName, qty);
        }
        nameField.setText("");
        qtyField.setText("");
        priceField.setText("");
        nameField.requestFocusInWindow();
    }

    private void editProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow >= 0) {
            String name = (String) tableModel.getValueAt(selectedRow, 0);
            String qty = tableModel.getValueAt(selectedRow, 1).toString();
            String price = tableModel.getValueAt(selectedRow, 2).toString();
            openEditFrame(selectedRow, name, qty, price);
        }
    }

    private void deleteProduct() {
        int rowToDelete = productTable.getSelectedRow();
        if (rowToDelete >= 0) {
            Object[] deletedRow = {
                    tableModel.getValueAt(rowToDelete, 0),
                    tableModel.getValueAt(rowToDelete, 1),
                    tableModel.getValueAt(rowToDelete, 2)
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
            String qty = tableModel.getValueAt(i, 1).toString();
            String price = tableModel.getValueAt(i, 2).toString();
            boolean alreadyInHistory = false;
            for (String[] hist : historyList) {
                if (hist[0].equals(pname) && hist[1].equals(qty) && hist[2].equals(price) && hist[3].startsWith(dateTime.substring(0, 10))) {
                    alreadyInHistory = true;
                    break;
                }
            }
            if (!alreadyInHistory) {
                historyList.add(new String[] { pname, qty, price, dateTime });
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
                // Only append if not already present in alertArea
                if (!alertArea.getText().contains(msg)) {
                    alertArea.append((alertArea.getText().isEmpty() ? "" : "\n") + msg);
                }
                java.awt.Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(this, msg, "Low Stock Alert", JOptionPane.WARNING_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            // Ignore if qty is not a number
        }
    }

    // --- History, Settings, Theme, and File Handling ---
    private void openEditFrame(int rowIndex, String name, String qty, String price) {
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
        JLabel qtyLabel = new JLabel("Quantity:");
        JTextField editQtyField = new JTextField(qty, 5);
        JLabel priceLabel = new JLabel("Price:");
        JTextField editPriceField = new JTextField(price, 7);
        JButton saveButton = new JButton("Save");

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(nameLabel, gbc);
        gbc.gridx = 1;
        panel.add(editNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(qtyLabel, gbc);
        gbc.gridx = 1;
        panel.add(editQtyField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(priceLabel, gbc);
        gbc.gridx = 1;
        panel.add(editPriceField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        panel.add(saveButton, gbc);

        editFrame.add(panel);
        editFrame.setVisible(true);

        saveButton.addActionListener(_ -> {
            String newName = editNameField.getText().trim();
            String newQty = editQtyField.getText().trim();
            String newPrice = editPriceField.getText().trim();
            if (!newName.isEmpty() && !newQty.isEmpty() && !newPrice.isEmpty()) {
                tableModel.setValueAt(newName, rowIndex, 0);
                tableModel.setValueAt(newQty, rowIndex, 1);
                tableModel.setValueAt(newPrice, rowIndex, 2);
                if (!productNames.contains(newName)) {
                    productNames.add(newName);
                    nameComboBox.addItem(newName);
                }
                editFrame.dispose();
                productTable.clearSelection();
            }
        });
    }

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

    private void showRestocks() {
        JFrame restocksFrame = new JFrame("Recent Restocks");
        restocksFrame.setSize(400, 750);
        restocksFrame.setMinimumSize(new Dimension(320, 600));
        restocksFrame.setMaximumSize(new Dimension(480, 900));
        restocksFrame.setLocationRelativeTo(this);
        restocksFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        String[] restockColumns = { "Product Name", "Quantity", "Price", "Restock Date" };
        DefaultTableModel restocksModel = new DefaultTableModel(restockColumns, 0);
        JTable restocksTable = new JTable(restocksModel);
        restocksTable.setRowHeight(26);
        restocksTable.setFont(restocksTable.getFont().deriveFont(13f));
        JScrollPane restocksScroll = new JScrollPane(restocksTable);
        restocksScroll.setPreferredSize(new Dimension(360, 600));
        restocksScroll.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate yesterday = today.minusDays(1);
        java.time.format.DateTimeFormatter dateFmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String todayStr = today.format(dateFmt);
        String yesterdayStr = yesterday.format(dateFmt);

        for (String[] row : historyList) {
            String dateOnly = row[3].split(" ")[0];
            if (dateOnly.equals(todayStr) || dateOnly.equals(yesterdayStr)) {
                restocksModel.addRow(new String[] { row[0], row[1], row[2], dateOnly });
            }
        }

        JPanel restocksMainPanel = new JPanel();
        restocksMainPanel.setLayout(new BoxLayout(restocksMainPanel, BoxLayout.Y_AXIS));
        restocksMainPanel.add(restocksScroll);
        restocksMainPanel.add(Box.createVerticalGlue());

        JButton saveRestocksBtn = new JButton("Save");
        saveRestocksBtn.setMaximumSize(new Dimension(140, 32));
        saveRestocksBtn.setAlignmentX(CENTER_ALIGNMENT);
        saveRestocksBtn.addActionListener(_e -> {
            int rows = restocksModel.getRowCount();
            int added = 0;
            for (int i = 0; i < rows; i++) {
                String pname = restocksModel.getValueAt(i, 0).toString();
                String qty = restocksModel.getValueAt(i, 1).toString();
                String price = restocksModel.getValueAt(i, 2).toString();
                String date = restocksModel.getValueAt(i, 3).toString();
                boolean alreadyInHistory = false;
                for (String[] hist : historyList) {
                    if (hist[0].equals(pname) && hist[1].equals(qty) && hist[2].equals(price) && hist[3].startsWith(date)) {
                        alreadyInHistory = true;
                        break;
                    }
                }
                if (!alreadyInHistory) {
                    historyList.add(new String[] { pname, qty, price, date + " 00:00" });
                    added++;
                }
            }
            JOptionPane.showMessageDialog(restocksFrame, "Saved" + (added > 0 ? " (" + added + " new record(s))" : ""));
        });

        JButton restocksCloseBtn = new JButton(closeIcon);
        restocksCloseBtn.setToolTipText("Close");
        restocksCloseBtn.setPreferredSize(new Dimension(closeBtnWidth, closeBtnHeight));
        restocksCloseBtn.setMaximumSize(new Dimension(closeBtnWidth, closeBtnHeight));
        restocksCloseBtn.setMinimumSize(new Dimension(closeBtnWidth, closeBtnHeight));
        restocksCloseBtn.setAlignmentX(CENTER_ALIGNMENT);
        restocksCloseBtn.setBorderPainted(false);
        restocksCloseBtn.setFocusPainted(false);
        restocksCloseBtn.setContentAreaFilled(false);
        restocksCloseBtn.addActionListener(_e -> restocksFrame.dispose());

        JPanel closePanel = new JPanel();
        closePanel.setLayout(new BoxLayout(closePanel, BoxLayout.X_AXIS));
        closePanel.setOpaque(false);
        closePanel.add(Box.createHorizontalGlue());
        closePanel.add(restocksCloseBtn);
        closePanel.add(Box.createHorizontalGlue());
        restocksMainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        restocksMainPanel.add(saveRestocksBtn);
        restocksMainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        restocksMainPanel.add(closePanel);

        restocksFrame.add(restocksMainPanel);
        restocksFrame.setVisible(true);
    }

    private void openHistoryFrameWithClose(JFrame parent) {
        JFrame historyFrame = new JFrame("Transaction History");
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

        String[] columnNames = { "Product Name", "Quantity", "Price", "Date" };
        DefaultTableModel histTableModel = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(histTableModel);
        table.setRowHeight(26);
        table.setFont(table.getFont().deriveFont(13f));
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(360, 600));
        scrollPane.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        for (String[] row : historyList) {
            String dateOnly = row[3].split(" ")[0];
            histTableModel.addRow(new String[] { row[0], row[1], row[2], dateOnly });
        }

        selectDateButton.addActionListener(_ -> {
            JFrame dateFrame = new JFrame("Select Date");
            dateFrame.setSize(400, 750);
            dateFrame.setMinimumSize(new Dimension(320, 600));
            dateFrame.setMaximumSize(new Dimension(480, 900));
            dateFrame.setLayout(new BorderLayout());
            dateFrame.setLocationRelativeTo(historyFrame);

            javax.swing.SpinnerDateModel dateModel = new javax.swing.SpinnerDateModel();
            javax.swing.JSpinner dateSpinner = new javax.swing.JSpinner(dateModel);
            dateSpinner.setEditor(new javax.swing.JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));

            JLabel dateLabel = new JLabel("Select Date:");
            dateLabel.setFont(dateLabel.getFont().deriveFont(15f));
            dateLabel.setAlignmentX(CENTER_ALIGNMENT);

            JPanel centerPanel = new JPanel();
            centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
            centerPanel.add(Box.createVerticalGlue());
            JPanel rowPanel = new JPanel();
            rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
            rowPanel.setOpaque(false);
            rowPanel.add(Box.createHorizontalGlue());
            rowPanel.add(dateLabel);
            rowPanel.add(Box.createHorizontalGlue());
            centerPanel.add(rowPanel);
            centerPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            JPanel spinnerPanel = new JPanel();
            spinnerPanel.setLayout(new BoxLayout(spinnerPanel, BoxLayout.X_AXIS));
            spinnerPanel.setOpaque(false);
            spinnerPanel.add(Box.createHorizontalGlue());
            spinnerPanel.add(dateSpinner);
            spinnerPanel.add(Box.createHorizontalGlue());
            centerPanel.add(spinnerPanel);
            centerPanel.add(Box.createVerticalGlue());
            dateFrame.add(centerPanel, BorderLayout.CENTER);

            JButton okButton = new JButton("OK");
            JButton cancelButton = new JButton("Cancel");
            JPanel btnPanel = new JPanel();
            btnPanel.add(okButton);
            btnPanel.add(cancelButton);
            dateFrame.add(btnPanel, BorderLayout.SOUTH);

            okButton.addActionListener(_ -> {
                java.util.Date selected = (java.util.Date) dateSpinner.getValue();
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                String selectedDate = sdf.format(selected);
                selectedDateLabel.setText(selectedDate);
                histTableModel.setRowCount(0);
                for (String[] row : historyList) {
                    String dateOnly = row[3].split(" ")[0];
                    if (dateOnly.equals(selectedDate)) {
                        histTableModel.addRow(new String[] { row[0], row[1], row[2], dateOnly });
                    }
                }
                dateFrame.dispose();
            });

            cancelButton.addActionListener(_ -> dateFrame.dispose());

            dateFrame.setLocationRelativeTo(historyFrame);
            dateFrame.setVisible(true);
        });

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(filterPanel);
        mainPanel.add(scrollPane);

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

        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(closeHistoryBtn);
        mainPanel.add(Box.createVerticalGlue());

        historyFrame.add(mainPanel);
        historyFrame.setVisible(true);
    }
    
    public JButton getEditAlertButton() {
        return editAlertButton;
    }

    // Add missing file handling methods to avoid errors (implement as needed)
    private void loadHistoryFromFile() {


    }

    private void saveHistoryToFile() {

    }

    public Map<String, Integer> getProductThresholds() {
        return productThresholds;
    }
}
