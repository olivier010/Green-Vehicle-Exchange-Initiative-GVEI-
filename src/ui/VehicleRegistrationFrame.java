package ui;

import db.DBConnection;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class VehicleRegistrationFrame extends Frame implements ActionListener {

    Label lblTitle = new Label("Vehicle Registration", Label.CENTER);
    Label lblPlate = new Label("Plate Number:");
    Label lblType = new Label("Vehicle Type:");
    Label lblFuel = new Label("Fuel Type:");
    Label lblYear = new Label("Manufacture Year:");
    Label lblMileage = new Label("Mileage:");

    TextField txtPlate = new TextField(25);
    Choice choiceType = new Choice();
    Choice choiceFuel = new Choice();
    TextField txtYear = new TextField(25);
    TextField txtMileage = new TextField(25);

    Button btnRegister = new Button("Register");
    Button btnBack = new Button("Back");

    int userId;

    public VehicleRegistrationFrame(int userId) {
        this.userId = userId;

        setTitle("Vehicle Registration - GVEI");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(20, 20));

        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        add(lblTitle, BorderLayout.NORTH);

        Panel centerPanel = new Panel(new GridLayout(5, 2, 10, 10));
        centerPanel.add(lblPlate); centerPanel.add(txtPlate);
        centerPanel.add(lblType); choiceType.add("Car"); choiceType.add("Bus"); choiceType.add("Motorcycle"); centerPanel.add(choiceType);
        centerPanel.add(lblFuel); choiceFuel.add("Petrol"); choiceFuel.add("Diesel"); choiceFuel.add("Electric"); centerPanel.add(choiceFuel);
        centerPanel.add(lblYear); centerPanel.add(txtYear);
        centerPanel.add(lblMileage); centerPanel.add(txtMileage);
        add(centerPanel, BorderLayout.CENTER);

        Panel bottomPanel = new Panel(new FlowLayout());
        bottomPanel.add(btnRegister); bottomPanel.add(btnBack);
        add(bottomPanel, BorderLayout.SOUTH);

        btnRegister.addActionListener(this);
        btnBack.addActionListener(this);

        setVisible(true);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
                new CitizenDashboard(userId);
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnRegister) registerVehicle();
        else if (e.getSource() == btnBack) {
            dispose();
            new CitizenDashboard(userId);
        }
    }

    private void registerVehicle() {
        String plate = txtPlate.getText();
        String type = choiceType.getSelectedItem();
        String fuel = choiceFuel.getSelectedItem();
        int year = Integer.parseInt(txtYear.getText());
        int mileage = Integer.parseInt(txtMileage.getText());

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(
                     "INSERT INTO vehicles(owner_id, plate_no, vehicle_type, fuel_type, year, mileage) VALUES(?,?,?,?,?,?)")) {
            pst.setInt(1, userId);
            pst.setString(2, plate);
            pst.setString(3, type);
            pst.setString(4, fuel);
            pst.setInt(5, year);
            pst.setInt(6, mileage);
            pst.executeUpdate();
            showMessage("Vehicle registered successfully!");
        } catch (Exception ex) {
            ex.printStackTrace();
            showMessage("Error: " + ex.getMessage());
        }
    }

    private void showMessage(String msg) {
        Dialog d = new Dialog(this, "Message", true);
        d.setLayout(new FlowLayout());
        d.add(new Label(msg));
        Button ok = new Button("OK");
        ok.addActionListener(ae -> d.dispose());
        d.add(ok);
        d.setSize(300, 150);
        d.setVisible(true);
    }
}
