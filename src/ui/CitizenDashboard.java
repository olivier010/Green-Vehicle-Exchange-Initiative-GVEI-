package ui;

import db.DBConnection;
import java.awt.*;
import java.awt.event.*;

public class CitizenDashboard extends Frame implements ActionListener {

    Label lblTitle = new Label("Citizen Dashboard - GVEI", Label.CENTER);
    Button btnRegisterVehicle = new Button("Register Vehicle");
    Button btnViewOffers = new Button("View My Offers");
    Button btnLogout = new Button("Logout");

    int userId;

    public CitizenDashboard(int userId) {
        this.userId = userId;

        setTitle("Citizen Dashboard - GVEI");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(20, 20));

        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        add(lblTitle, BorderLayout.NORTH);

        Panel centerPanel = new Panel(new FlowLayout());
        centerPanel.add(btnRegisterVehicle);
        centerPanel.add(btnViewOffers);
        centerPanel.add(btnLogout);
        add(centerPanel, BorderLayout.CENTER);

        btnRegisterVehicle.addActionListener(this);
        btnViewOffers.addActionListener(this);
        btnLogout.addActionListener(this);

        setVisible(true);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
                new LoginFrame();
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnRegisterVehicle) new VehicleRegistrationFrame(userId);
        else if (e.getSource() == btnViewOffers) new ExchangeOffersFrame(userId);
        else if (e.getSource() == btnLogout) {
            dispose();
            new LoginFrame();
        }
    }
}
