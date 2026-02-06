package com.pharmacy.gui.panels;

import com.pharmacy.dao.ProductDAO;
import com.pharmacy.gui.AddProductDialog;
import com.pharmacy.gui.EditProductDialog;
import com.pharmacy.model.Product;
import com.pharmacy.util.DateUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ProductsPanel extends JPanel {
    private static final String ALL = "All";

    private final JFrame parent;
    private final ProductDAO productDAO;

    private JTextField searchField;
    private JComboBox<String> companyCombo;
    private JComboBox<String> distributorCombo;
    private JCheckBox expiringOnlyCheck;
    private JCheckBox lowStockOnlyCheck;

    private JTable table;
    private DefaultTableModel tableModel;

    private List<Product> allProducts = new ArrayList<>();
    private List<Product> filteredProducts = new ArrayList<>();

    public ProductsPanel(JFrame parent) {
        this.parent = parent;
        this.productDAO = new ProductDAO();
        initUI();
        refreshData();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(createFilterBar(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
    }

    private JComponent createFilterBar() {
        JPanel bar = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        int x = 0;

        gbc.gridx = x++;
        gbc.gridy = 0;
        bar.add(new JLabel("Search (name):"), gbc);

        gbc.gridx = x++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        searchField = new JTextField(24);
        searchField.putClientProperty("JTextField.placeholderText", "Type product name…");
        bar.add(searchField, gbc);

        gbc.gridx = x++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        bar.add(new JLabel("Company:"), gbc);

        gbc.gridx = x++;
        companyCombo = new JComboBox<>(new String[]{ALL});
        companyCombo.setPrototypeDisplayValue("Select a very long company name");
        bar.add(companyCombo, gbc);

        gbc.gridx = x++;
        bar.add(new JLabel("Distributor:"), gbc);

        gbc.gridx = x++;
        distributorCombo = new JComboBox<>(new String[]{ALL});
        distributorCombo.setPrototypeDisplayValue("Select a very long distributor name");
        bar.add(distributorCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        expiringOnlyCheck = new JCheckBox("Expired / expiring soon only");
        bar.add(expiringOnlyCheck, gbc);

        gbc.gridx = 2;
        gbc.gridwidth = 2;
        lowStockOnlyCheck = new JCheckBox("Low stock (<5) only");
        bar.add(lowStockOnlyCheck, gbc);

        gbc.gridx = 4;
        gbc.gridwidth = 1;
        JButton addBtn = new JButton("Add");
        addBtn.addActionListener(e -> {
            new AddProductDialog(parent).setVisible(true);
            refreshData();
        });
        bar.add(addBtn, gbc);

        gbc.gridx = 5;
        JButton editBtn = new JButton("Edit");
        editBtn.addActionListener(e -> editSelectedProduct());
        bar.add(editBtn, gbc);

        gbc.gridx = 6;
        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            companyCombo.setSelectedItem(ALL);
            distributorCombo.setSelectedItem(ALL);
            expiringOnlyCheck.setSelected(false);
            lowStockOnlyCheck.setSelected(false);
            applyFilters();
        });
        bar.add(clearBtn, gbc);

        gbc.gridx = 7;
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshData());
        bar.add(refreshBtn, gbc);

        // Real-time updates
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilters(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });
        companyCombo.addItemListener(e -> applyFilters());
        distributorCombo.addItemListener(e -> applyFilters());
        expiringOnlyCheck.addActionListener(e -> applyFilters());
        lowStockOnlyCheck.addActionListener(e -> applyFilters());

        return bar;
    }

    private JComponent createTablePanel() {
        String[] cols = {
                "Serial", "Name", "Salt", "Company", "Distributor", "Batch",
                "Qty", "Price (Rs)", "MFG", "EXP", "Expiry", "Stock"
        };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(26);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.setDefaultRenderer(Object.class, new ProductRowRenderer());

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    editSelectedProduct();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        return scroll;
    }

    public void refreshData() {
        // Keep current selections if possible
        String selectedCompany = companyCombo != null && companyCombo.getSelectedItem() != null
                ? companyCombo.getSelectedItem().toString()
                : ALL;
        String selectedDistributor = distributorCombo != null && distributorCombo.getSelectedItem() != null
                ? distributorCombo.getSelectedItem().toString()
                : ALL;

        allProducts = productDAO.getAllProducts();

        // Rebuild dropdowns (All + distinct values)
        List<String> companies = productDAO.getDistinctCompanies();
        List<String> distributors = productDAO.getDistinctDistributors();

        rebuildCombo(companyCombo, companies, selectedCompany);
        rebuildCombo(distributorCombo, distributors, selectedDistributor);

        applyFilters();
    }

    private void rebuildCombo(JComboBox<String> combo, List<String> values, String selected) {
        if (combo == null) return;
        combo.removeAllItems();
        combo.addItem(ALL);
        for (String v : values) combo.addItem(v);

        if (selected != null) {
            ComboBoxModel<String> model = combo.getModel();
            for (int i = 0; i < model.getSize(); i++) {
                if (selected.equals(model.getElementAt(i))) {
                    combo.setSelectedItem(selected);
                    return;
                }
            }
        }
        combo.setSelectedItem(ALL);
    }

    private void applyFilters() {
        String search = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String company = companyCombo.getSelectedItem() == null ? ALL : companyCombo.getSelectedItem().toString();
        String distributor = distributorCombo.getSelectedItem() == null ? ALL : distributorCombo.getSelectedItem().toString();
        boolean expiringOnly = expiringOnlyCheck.isSelected();
        boolean lowStockOnly = lowStockOnlyCheck.isSelected();

        filteredProducts = new ArrayList<>();
        for (Product p : allProducts) {
            if (!search.isEmpty()) {
                String name = p.getName() == null ? "" : p.getName().toLowerCase();
                if (!name.contains(search)) continue;
            }
            if (!ALL.equals(company)) {
                if (p.getCompany() == null || !p.getCompany().equals(company)) continue;
            }
            if (!ALL.equals(distributor)) {
                if (p.getDistributor() == null || !p.getDistributor().equals(distributor)) continue;
            }
            if (expiringOnly) {
                if (!(DateUtil.isExpired(p.getExpDate()) || DateUtil.isNearExpiry(p.getExpDate()))) continue;
            }
            if (lowStockOnly) {
                if (!(p.getQuantity() < 5)) continue;
            }
            filteredProducts.add(p);
        }

        reloadTable();
    }

    private void reloadTable() {
        tableModel.setRowCount(0);
        for (Product p : filteredProducts) {
            String expiryStatus = DateUtil.isExpired(p.getExpDate())
                    ? "Expired"
                    : (DateUtil.isNearExpiry(p.getExpDate()) ? "Expiring soon" : "Valid");
            String stockStatus = p.getQuantity() <= 0
                    ? "Out of stock"
                    : (p.getQuantity() < 5 ? "Low stock" : "OK");

            tableModel.addRow(new Object[]{
                    p.getSerial(),
                    p.getName(),
                    p.getSalt(),
                    p.getCompany(),
                    p.getDistributor(),
                    p.getBatch(),
                    p.getQuantity(),
                    String.format("%.2f", p.getPrice()),
                    DateUtil.formatDate(p.getMfgDate()),
                    DateUtil.formatDate(p.getExpDate()),
                    expiryStatus,
                    stockStatus
            });
        }
    }

    private void editSelectedProduct() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a product row to edit.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= filteredProducts.size()) return;

        Product p = filteredProducts.get(modelRow);
        new EditProductDialog(parent, p).setVisible(true);
        refreshData();
    }

    private class ProductRowRenderer extends DefaultTableCellRenderer {
        private final Color purple = new Color(128, 0, 128);
        private final Color lowStockOrange = new Color(255, 153, 51);
        private final Color outOfStockGray = new Color(90, 90, 90);

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            int modelRow = table.convertRowIndexToModel(row);
            Product p = (modelRow >= 0 && modelRow < filteredProducts.size()) ? filteredProducts.get(modelRow) : null;

            if (isSelected) {
                c.setBackground(table.getSelectionBackground());
                c.setForeground(table.getSelectionForeground());
                return c;
            }

            if (p != null) {
                if (DateUtil.isExpired(p.getExpDate())) {
                    c.setBackground(Color.RED);
                    c.setForeground(Color.WHITE);
                } else if (DateUtil.isNearExpiry(p.getExpDate())) {
                    c.setBackground(purple);
                    c.setForeground(Color.WHITE);
                } else if (p.getQuantity() <= 0) {
                    c.setBackground(outOfStockGray);
                    c.setForeground(Color.WHITE);
                } else if (p.getQuantity() < 5) {
                    c.setBackground(lowStockOrange);
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(table.getBackground());
                    c.setForeground(table.getForeground());
                }
            } else {
                c.setBackground(table.getBackground());
                c.setForeground(table.getForeground());
            }

            return c;
        }
    }
}

