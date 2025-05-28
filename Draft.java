import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class Draft extends JFrame {
    // Fields
    private Set<String> productNames = new HashSet<>();
    private JComboBox<String> nameComboBox;
    private DefaultTableModel tableModel;
    private JTable productTable;
    private List<String[]> historyList = new ArrayList<>();
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");
    private Stack<Object[]> deletedStack = new Stack<>();
    private boolean isDarkTheme = false;

    // Set your low stock threshold here
    private final int LOW_STOCK_THRESHOLD = 5;

    // Constructor
    @SuppressWarnings("unused")
    public Draft() {
        setTitle("KITA_KO Restocker Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 550);
        setLocationRelativeTo(null);

        // Main container
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // Dashboard Panel (Left)
        JPanel dashboardPanel = new JPanel();
        dashboardPanel.setLayout(new BoxLayout(dashboardPanel, BoxLayout.Y_AXIS));
        dashboardPanel.setBorder(BorderFactory.createTitledBorder("Dashboard"));
        dashboardPanel.setPreferredSize(new Dimension(200, 0));

        // Total Products
        JPanel totalPanel = new JPanel();
        JLabel totalLabel = new JLabel("Total Products:");
        JTextField totalField = new JTextField("120", 5);
        totalField.setEditable(false);
        totalPanel.add(totalLabel);
        totalPanel.add(totalField);
        dashboardPanel.add(totalPanel);
        dashboardPanel.add(Box.createVerticalStrut(10));

        // Low Stock
        JPanel lowStockPanel = new JPanel();
        JLabel lowStockLabel = new JLabel("Low Stock:");
        JTextField lowStockField = new JTextField("5", 5);
        lowStockField.setEditable(false);
        lowStockPanel.add(lowStockLabel);
        lowStockPanel.add(lowStockField);
        dashboardPanel.add(lowStockPanel);
        dashboardPanel.add(Box.createVerticalStrut(10));

        // Restock Alerts
        JPanel restockPanel = new JPanel();
        JLabel restockLabel = new JLabel("Restock Alerts:");
        JTextField restockField = new JTextField("2", 5);
        restockField.setEditable(false);
        restockPanel.add(restockLabel);
        restockPanel.add(restockField);
        dashboardPanel.add(restockPanel);

        // Edit and Save buttons (only one visible at a time)
        JButton editDashboardButton = new JButton("Edit");
        JButton saveDashboardButton = new JButton("Save");
        saveDashboardButton.setVisible(false); // Hide save by default

        dashboardPanel.add(Box.createVerticalStrut(20));
        dashboardPanel.add(editDashboardButton);
        dashboardPanel.add(saveDashboardButton);

        // Edit button action: make fields editable, show Save, hide Edit
        editDashboardButton.addActionListener(e -> {
            totalField.setEditable(true);
            lowStockField.setEditable(true);
            restockField.setEditable(true);
            editDashboardButton.setVisible(false);
            saveDashboardButton.setVisible(true);
            totalField.requestFocusInWindow();
        });

        // Save button action: make fields non-editable, show Edit, hide Save
        saveDashboardButton.addActionListener(e -> {
            totalField.setEditable(false);
            lowStockField.setEditable(false);
            restockField.setEditable(false);
            saveDashboardButton.setVisible(false);
            editDashboardButton.setVisible(true);
        });

        // Product Entry Form (Center)
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Product Entry"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel nameLabel = new JLabel("Product Name:");
        nameComboBox = new JComboBox<>();
        nameComboBox.setEditable(true);
        JTextField nameField = (JTextField) nameComboBox.getEditor().getEditorComponent();

        JLabel qtyLabel = new JLabel("Quantity:");
        JTextField qtyField = new JTextField(5);
        JLabel priceLabel = new JLabel("Price:");
        JTextField priceField = new JTextField(7);
        JButton addButton = new JButton("Add Product");

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(nameLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(nameComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(qtyLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(qtyField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(priceLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(priceField, gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        formPanel.add(addButton, gbc);

        // Product Table (Right)
        String[] columns = {"Product Name", "Quantity", "Price"};
        tableModel = new DefaultTableModel(columns, 0);
        productTable = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(productTable);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < productTable.getColumnCount(); i++) {
            productTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Edit, Delete, Redo Buttons
        JButton editButton = new JButton("Edit Selected Product");
        JButton deleteButton = new JButton("Delete Selected Product");
        JButton redoButton = new JButton("Redo Delete");

        // Edit Button Action
        editButton.addActionListener(e -> {
            int selectedRow = productTable.getSelectedRow();
            if (selectedRow >= 0) {
                String name = (String) tableModel.getValueAt(selectedRow, 0);
                String qty = tableModel.getValueAt(selectedRow, 1).toString();
                String price = tableModel.getValueAt(selectedRow, 2).toString();
                openEditFrame(selectedRow, name, qty, price);
            }
        });

        // Delete Button Action
        deleteButton.addActionListener(e -> {
            int selectedRow = productTable.getSelectedRow();
            if (selectedRow >= 0) {
                Object[] deletedRow = {
                    tableModel.getValueAt(selectedRow, 0),
                    tableModel.getValueAt(selectedRow, 1),
                    tableModel.getValueAt(selectedRow, 2)
                };
                deletedStack.push(deletedRow);
                String name = (String) deletedRow[0];
                String qty = deletedRow[1].toString();
                String price = deletedRow[2].toString();
                historyList.removeIf(entry ->
                    entry[0].equals(name) && entry[1].equals(qty) && entry[2].equals(price)
                );
                tableModel.removeRow(selectedRow);
            }
        });

        // Redo Button Action
        redoButton.addActionListener(_ -> {
            if (!deletedStack.isEmpty()) {
                Object[] restored = deletedStack.pop();
                tableModel.addRow(restored);
                String dateTime = LocalDateTime.now().format(dtf);
                historyList.add(new String[]{
                    restored[0].toString(),
                    restored[1].toString(),
                    restored[2].toString(),
                    dateTime
                });
            }
        });

        // Table panel (right)
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Product List"));
        tablePanel.add(tableScroll, BorderLayout.CENTER);

        // Panel for Edit, Delete, Redo buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(redoButton);
        tablePanel.add(buttonPanel, BorderLayout.SOUTH);
        tablePanel.setPreferredSize(new Dimension(350, 0));

        // --- Alert Panel (Bottom Right) ---
        JPanel alertPanel = new JPanel(new BorderLayout());
        alertPanel.setBorder(BorderFactory.createTitledBorder("Alerts & More"));
        alertPanel.setPreferredSize(new Dimension(400, 120));

        JTextArea alertArea = new JTextArea(3, 30);
        alertArea.setEditable(false);
        alertArea.setText("Product 'Milk' is low on stock!\nProduct 'Eggs' needs restocking.");
        alertPanel.add(new JScrollPane(alertArea), BorderLayout.CENTER);

        // Lower right panel for History and Settings
        JPanel lowerRightPanel = new JPanel();
        lowerRightPanel.setLayout(new BoxLayout(lowerRightPanel, BoxLayout.X_AXIS));
        JButton historyButton = new JButton("History");
        JButton settingsButton = new JButton("Settings");
        lowerRightPanel.add(Box.createHorizontalGlue());
        lowerRightPanel.add(historyButton);
        lowerRightPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        lowerRightPanel.add(settingsButton);
        lowerRightPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        alertPanel.add(lowerRightPanel, BorderLayout.SOUTH);

        // History button action
        historyButton.addActionListener(e -> openHistoryFrame());

        // Add panels to mainPanel
        mainPanel.add(dashboardPanel, BorderLayout.WEST);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(tablePanel, BorderLayout.EAST);
        mainPanel.add(alertPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        // Add product name to suggestions and table when "Add Product" is clicked
        addButton.addActionListener((ActionEvent e) -> {
            String productName = nameField.getText().trim();
            String qty = qtyField.getText().trim();
            String price = priceField.getText().trim();
            if (!productName.isEmpty() && !qty.isEmpty() && !price.isEmpty()) {
                if (!productNames.contains(productName)) {
                    productNames.add(productName);
                    nameComboBox.addItem(productName);
                }
                tableModel.addRow(new Object[]{productName, qty, price});
                String dateTime = LocalDateTime.now().format(dtf);
                historyList.add(new String[]{productName, qty, price, dateTime});

                // --- Visual & Audio Alert for Low Stock ---
                try {
                    int qtyValue = Integer.parseInt(qty);
                    if (qtyValue <= LOW_STOCK_THRESHOLD) {
                        // Visual alert in alertArea
                        alertArea.append("\nALERT: '" + productName + "' is low on stock (" + qty + ")!");
                        // Audio alert
                        java.awt.Toolkit.getDefaultToolkit().beep();
                        // Optional: Show dialog
                        JOptionPane.showMessageDialog(this,
                            "Stock for '" + productName + "' is low (" + qty + ")!",
                            "Low Stock Alert",
                            JOptionPane.WARNING_MESSAGE
                        );
                    }
                } catch (NumberFormatException ex) {
                    // Ignore if qty is not a number
                }
            }
            nameField.setText("");
            qtyField.setText("");
            priceField.setText("");
            nameField.requestFocusInWindow();
        });

        // Settings button action
        settingsButton.addActionListener(_ -> openSettingsFrame());

        nameField.addActionListener(e -> {
            qtyField.requestFocusInWindow();
        });

        qtyField.addActionListener(e -> {
            priceField.requestFocusInWindow();
        });

        priceField.addActionListener(e -> {
            addButton.doClick();
        });

        // Save button actions: focus returns to the same field
        // totalSave.addActionListener(e -> totalField.requestFocusInWindow());
        // lowStockSave.addActionListener(e -> lowStockField.requestFocusInWindow());
        // restockSave.addActionListener(e -> restockField.requestFocusInWindow());
    }

    // --- Methods ---

    // Edit Product Frame
    private void openEditFrame(int rowIndex, String name, String qty, String price) {
        JFrame editFrame = new JFrame("Edit Product");
        editFrame.setSize(350, 200);
        editFrame.setLocationRelativeTo(this);
        editFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel nameLabel = new JLabel("Product Name:");
        JTextField nameField = new JTextField(name, 15);
        JLabel qtyLabel = new JLabel("Quantity:");
        JTextField qtyField = new JTextField(qty, 5);
        JLabel priceLabel = new JLabel("Price:");
        JTextField priceField = new JTextField(price, 7);
        JButton saveButton = new JButton("Save");

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(nameLabel, gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(qtyLabel, gbc);
        gbc.gridx = 1;
        panel.add(qtyField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(priceLabel, gbc);
        gbc.gridx = 1;
        panel.add(priceField, gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        panel.add(saveButton, gbc);

        editFrame.add(panel);
        editFrame.setVisible(true);

        saveButton.addActionListener(_ -> {
            String newName = nameField.getText().trim();
            String newQty = qtyField.getText().trim();
            String newPrice = priceField.getText().trim();
            if (!newName.isEmpty() && !newQty.isEmpty() && !newPrice.isEmpty()) {
                tableModel.setValueAt(newName, rowIndex, 0);
                tableModel.setValueAt(newQty, rowIndex, 1);
                tableModel.setValueAt(newPrice, rowIndex, 2);
                if (!productNames.contains(newName)) {
                    productNames.add(newName);
                    nameComboBox.addItem(newName);
                }
                editFrame.dispose();
                productTable.clearSelection(); // <-- This removes the highlight after saving
            }
        });
    }

    // History Frame
    private void openHistoryFrame() {
        JFrame historyFrame = new JFrame("Product History");
        historyFrame.setSize(700, 300);
        historyFrame.setLocationRelativeTo(this);
        historyFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        String[] columns = {"Product Name", "Quantity", "Price", "Date Added"};
        DefaultTableModel historyModel = new DefaultTableModel(columns, 0);
        for (String[] entry : historyList) {
            historyModel.addRow(entry);
        }
        JTable historyTable = new JTable(historyModel);

        // Center text in all columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < historyTable.getColumnCount(); i++) {
            historyTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(historyTable);

        historyFrame.add(scrollPane);
        historyFrame.setVisible(true);
    }

    // Settings Frame
    private void openSettingsFrame() {
        JFrame settingsFrame = new JFrame("Settings");
        settingsFrame.setSize(300, 150);
        settingsFrame.setLocationRelativeTo(this);
        settingsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        JButton themeButton = new JButton("Theme");

        panel.add(themeButton);
        settingsFrame.add(panel);

        themeButton.addActionListener(_ -> {
            Object[] options = {"Dark Theme", "Light Theme"};
            int choice = javax.swing.JOptionPane.showOptionDialog(
                settingsFrame,
                "Choose a theme:",
                "Theme Selection",
                javax.swing.JOptionPane.DEFAULT_OPTION,
                javax.swing.JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
            );
            if (choice == 0) {
                isDarkTheme = true;
                applyTheme();
            } else if (choice == 1) {
                isDarkTheme = false;
                applyTheme();
            }
        });

        settingsFrame.setVisible(true);
    }

    // Theme Application
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

    // Recursively set theme for all components
    private void setComponentTheme(java.awt.Component comp, java.awt.Color bg, java.awt.Color fg) {
        if (comp instanceof JPanel || comp instanceof JScrollPane) {
            comp.setBackground(bg);
        }
        if (comp instanceof JLabel || comp instanceof JButton || comp instanceof JTextField || comp instanceof JComboBox || comp instanceof JTable || comp instanceof JTextArea) {
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
            if (view != null) setComponentTheme(view, bg, fg);
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

    // Recursively set titled border color
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

    // Main method
    public static void main(String[] args) {
        // You can write your code here
        int i = 0;
        System.out.println("Value of i: " + i);
        SwingUtilities.invokeLater(() -> {
            new Draft().setVisible(true);
        });
    }
}