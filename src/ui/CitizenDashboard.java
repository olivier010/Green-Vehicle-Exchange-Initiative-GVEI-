package ui;

import java.awt.*;
import java.awt.event.*;

public class CitizenDashboard extends Frame implements ActionListener {
    int userId;
    Label lblTitle;
    Button btnRegisterVehicle = new Button("Register Vehicle");
    Button btnLogout = new Button("Logout");

    public CitizenDashboard(int userId) {
        this.userId = userId;

        setTitle("Citizen Dashboard - GVEI");
        setSize(800, 600);
        setLayout(new GridLayout(5, 2, 10, 10));

        lblTitle = new Label("Welcome Citizen! (User ID: " + userId + ")", Label.CENTER);
        add(lblTitle);
        add(btnRegisterVehicle);
        add(btnLogout);

        btnRegisterVehicle.addActionListener(this);
        btnLogout.addActionListener(this);

        setVisible(true);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
                System.exit(0);
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnRegisterVehicle) {
            new VehicleRegistrationFrame(userId);
            dispose();
        } else if (e.getSource() == btnLogout) {
            new LoginFrame();
            dispose();
        }
    }
}
