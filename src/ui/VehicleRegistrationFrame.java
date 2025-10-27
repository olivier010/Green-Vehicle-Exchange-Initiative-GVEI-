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

        // Center Panel for input fields
        Panel centerPanel = new Panel(new GridLayout(5, 2, 10, 10));
        centerPanel.add(lblPlate); centerPanel.add(txtPlate);

        choiceType.add("Car"); choiceType.add("Bus"); choiceType.add("Motorcycle");
        centerPanel.add(lblType); centerPanel.add(choiceType);

        choiceFuel.add("Petrol"); choiceFuel.add("Diesel"); choiceFuel.add("Electric");
        centerPanel.add(lblFuel); centerPanel.add(choiceFuel);

        centerPanel.add(lblYear); centerPanel.add(txtYear);
        centerPanel.add(lblMileage); centerPanel.add(txtMileage);
        add(centerPanel, BorderLayout.CENTER);

        // Bottom Panel for buttons
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
        try {
            String plate = txtPlate.getText();
            String type = choiceType.getSelectedItem();
            String fuel = choiceFuel.getSelectedItem();
            int year = Integer.parseInt(txtYear.getText());
            int mileage = Integer.parseInt(txtMileage.getText());

            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(false); // start transaction

                // 1️⃣ Insert vehicle
                String vehicleSql = "INSERT INTO vehicles(owner_id, plate_no, vehicle_type, fuel_type, year, mileage) VALUES(?,?,?,?,?,?)";
                int vehicleId;
                try (PreparedStatement pst = conn.prepareStatement(vehicleSql, Statement.RETURN_GENERATED_KEYS)) {
                    pst.setInt(1, userId);
                    pst.setString(2, plate);
                    pst.setString(3, type);
                    pst.setString(4, fuel);
                    pst.setInt(5, year);
                    pst.setInt(6, mileage);
                    pst.executeUpdate();

                    // Get generated vehicle_id
                    ResultSet rs = pst.getGeneratedKeys();
                    if (rs.next()) vehicleId = rs.getInt(1);
                    else throw new SQLException("Failed to get vehicle_id");
                }

                // 2️⃣ Calculate exchange value & subsidy
                double exchangeValue = calculateExchangeValue(year, mileage);
                double subsidyPercent = calculateSubsidy(exchangeValue);

                // 3️⃣ Insert into exchange_offers
                String offerSql = "INSERT INTO exchange_offers(vehicle_id, exchange_value, subsidy_percent, status) VALUES(?,?,?,?)";
                try (PreparedStatement pst2 = conn.prepareStatement(offerSql)) {
                    pst2.setInt(1, vehicleId);
                    pst2.setDouble(2, exchangeValue);
                    pst2.setDouble(3, subsidyPercent);
                    pst2.setString(4, "applied"); // default status
                    pst2.executeUpdate();
                }

                conn.commit(); // commit transaction
                showMessage("Vehicle registered and exchange offer created successfully!");

            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showMessage("Error: " + ex.getMessage());
        }
    }

    // Example calculation methods
    private double calculateExchangeValue(int year, int mileage) {
        int age = 2025 - year; // current year
        double baseValue = 10000; // arbitrary base
        double depreciation = age * 500 + mileage * 0.1;
        double value = baseValue - depreciation;
        return Math.max(value, 1000); // minimum value
    }

    private double calculateSubsidy(double exchangeValue) {
        return exchangeValue * 0.2; // 20% subsidy for simplicity
    }

    private void showMessage(String msg) {
        Dialog d = new Dialog(this, "Message", true);
        d.setLayout(new FlowLayout());
        d.add(new Label(msg));
        Button ok = new Button("OK");
        ok.addActionListener(ae -> d.dispose());
        d.add(ok);
        d.setSize(400, 200);
        d.setVisible(true);
    }
}
