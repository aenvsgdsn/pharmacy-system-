package com.pharmacy.gui.panels;

import com.pharmacy.dao.ProductDAO;
import com.pharmacy.dao.SaleDAO;
import com.pharmacy.model.Product;
import com.pharmacy.util.DateUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class DashboardPanel extends JPanel {
    private final ProductDAO productDAO;
    private final SaleDAO saleDAO;

    private JLabel totalProductsLabel;
    private JLabel expiringSoonLabel;
    private JLabel todaySalesLabel;
    private JLabel todayRevenueLabel;

    private DefaultTableModel topModel;

    public DashboardPanel() {
        this.productDAO = new ProductDAO();
        this.saleDAO = new SaleDAO();
        initUI();
        refresh();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel header = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Dashboard");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        header.add(title, BorderLayout.WEST);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refresh());
        header.add(refreshBtn, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        JPanel stats = new JPanel(new GridLayout(2, 2, 10, 10));
        stats.add(wrapStat("Total products", totalProductsLabel = new JLabel("-")));
        stats.add(wrapStat("Expiring within 6 months", expiringSoonLabel = new JLabel("-")));
        stats.add(wrapStat("Today's sales (lines)", todaySalesLabel = new JLabel("-")));
        stats.add(wrapStat("Today's revenue", todayRevenueLabel = new JLabel("-")));
        add(stats, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(10, 10));
        JLabel topTitle = new JLabel("Top 5 best-selling products");
        topTitle.setFont(topTitle.getFont().deriveFont(Font.BOLD, 14f));
        bottom.add(topTitle, BorderLayout.NORTH);

        topModel = new DefaultTableModel(new String[]{"Product", "Quantity sold"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable topTable = new JTable(topModel);
        topTable.setRowHeight(26);
        topTable.setAutoCreateRowSorter(true);

        bottom.add(new JScrollPane(topTable), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private JPanel wrapStat(String title, JLabel valueLabel) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        JLabel t = new JLabel(title);
        t.setForeground(new Color(90, 90, 90));
        panel.add(t, BorderLayout.NORTH);

        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 18f));
        panel.add(valueLabel, BorderLayout.CENTER);
        return panel;
    }

    public void refresh() {
        List<Product> products = productDAO.getAllProducts();
        int totalProducts = products.size();
        int expiringSoon = 0;
        for (Product p : products) {
            if (DateUtil.isNearExpiry(p.getExpDate())) expiringSoon++;
        }
        double pct = totalProducts == 0 ? 0.0 : (expiringSoon * 100.0 / totalProducts);

        totalProductsLabel.setText(String.valueOf(totalProducts));
        expiringSoonLabel.setText(expiringSoon + " (" + String.format("%.1f", pct) + "%)");

        var today = saleDAO.getTodaySalesSummary(LocalDate.now());
        todaySalesLabel.setText(String.valueOf(today.getSalesCount()));
        todayRevenueLabel.setText("Rs " + String.format("%.2f", today.getRevenue()));

        topModel.setRowCount(0);
        var top = saleDAO.getTopSellingProducts(5);
        for (SaleDAO.TopSellingProduct t : top) {
            topModel.addRow(new Object[]{t.getProductName(), t.getQuantitySold()});
        }
    }
}

