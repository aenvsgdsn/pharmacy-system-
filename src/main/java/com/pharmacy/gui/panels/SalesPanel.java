package com.pharmacy.gui.panels;

import com.pharmacy.gui.GenerateBillDialog;
import com.pharmacy.gui.ViewSalesDialog;

import javax.swing.*;
import java.awt.*;

public class SalesPanel extends JPanel {
    private final JFrame parent;

    public SalesPanel(JFrame parent) {
        this.parent = parent;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel header = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Sales");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton billBtn = new JButton("Generate bill");
        billBtn.addActionListener(e -> new GenerateBillDialog(parent).setVisible(true));
        JButton salesBtn = new JButton("View sales (owner)");
        salesBtn.addActionListener(e -> new ViewSalesDialog(parent).setVisible(true));

        actions.add(billBtn);
        actions.add(salesBtn);
        add(actions, BorderLayout.CENTER);

        JLabel hint = new JLabel("Tip: After generating a bill, inventory quantity is reduced automatically.");
        hint.setForeground(new Color(90, 90, 90));
        add(hint, BorderLayout.SOUTH);
    }
}

