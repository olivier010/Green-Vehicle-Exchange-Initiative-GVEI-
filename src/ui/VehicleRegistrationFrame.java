package ui;

import db.DBConnection;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.Year;

public class VehicleRegistrationFrame extends Frame implements ActionListener {

    int userId;

    Label lblTitle = new Label("Vehicle Registration", Label.CENTER);
    Label lblPlate = new Label("Plate Number:");
    Label lblType = new Label("Vehicle Type:");
    Label lblFuel = new Label("Fuel Type:");
    Label lblYear = new Label("Manufacture Year:");
    Label lblMileage = new Label("Estimated Mileage:");

    TextField txtPlate = new TextField(20);
    Choice chType = new Choice();
    Choice chFuel = new Choice();
    TextField txtYear = new TextField(10);
    TextField txtMileage = new TextField(10);

    Button btnSubmit = new Button("Register Vehicle");
    Button btnBack = new Button("Back");

    public VehicleRegistrationFrame(int userId) {
        this.userId = userId;

        setTitle("Register Vehicle - GVEI");
        setSize(800, 600);
        setLayout(new GridLayout(8, 2, 10, 10));

        // Vehicle type choices
        chType.add("Car");
        chType.add("Bus");
        chType.add("Motorcycle");
        chType.add("Truck");

        // Fuel type choices
        chFuel.add("Petrol");
        chFuel.add("Diesel");
        chFuel.add("Electric");
        chFuel.add("Hybrid");

        add(lblTitle); add(new Label(""));
        add(lblPlate); add(txtPlate);
        add(lblType); add(chType);
        add(lblFuel); add(chFuel);
        add(lblYear); add(txtYear);
        add(lblMileage); add(txtMileage);
        add(btnSubmit); add(btnBack);

        btnSubmit.addActionListener(this);
        btnBack.addActionListener(this);

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
        if (e.getSource() == btnSubmit) {
            registerVehicle();
        } else if (e.getSource() == btnBack) {
            new CitizenDashboard(userId);
            dispose();
        }
    }

    private void registerVehicle() {
        try {
            String plate = txtPlate.getText();
            String type = chType.getSelectedItem();
            String fuel = chFuel.getSelectedItem();
            int year = Integer.parseInt(txtYear.getText());
            int mileage = Integer.parseInt(txtMileage.getText());

            int currentYear = Year.now().getValue();
            int age = currentYear - year;

            boolean eligible = (age > 5) && (fuel.equalsIgnoreCase("Petrol") || fuel.equalsIgnoreCase("Diesel"));

            try (Connection conn = DBConnection.getConnection()) {
                // Insert vehicle
                PreparedStatement pst = conn.prepareStatement(
                        "INSERT INTO vehicles(owner_id, plate_no, vehicle_type, fuel_type, year, mileage) VALUES(?, ?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS
                );
                pst.setInt(1, userId);
                pst.setString(2, plate);
                pst.setString(3, type);
                pst.setString(4, fuel);
                pst.setInt(5, year);
                pst.setInt(6, mileage);
                pst.executeUpdate();

                ResultSet rs = pst.getGeneratedKeys();
                int vehicleId = 0;
                if (rs.next()) {
                    vehicleId = rs.getInt(1);
                }

                // If eligible, create exchange offer
                if (eligible) {
                    double exchangeValue = calculateExchangeValue(age, mileage);
                    double subsidy = 20.0; // Example: 20% subsidy

                    PreparedStatement pst2 = conn.prepareStatement(
                            "INSERT INTO exchange_offers(vehicle_id, exchange_value, subsidy_percent, status) VALUES(?, ?, ?, 'applied')"
                    );
                    pst2.setInt(1, vehicleId);
                    pst2.setDouble(2, exchangeValue);
                    pst2.setDouble(3, subsidy);
                    pst2.executeUpdate();

                    showMessage("Vehicle registered and eligible! Exchange value: $" + exchangeValue);
                } else {
                    showMessage("Vehicle registered but NOT eligible for exchange.");
                }

                new CitizenDashboard(userId);
                dispose();

            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showMessage("Error: " + ex.getMessage());
        }
    }

    private double calculateExchangeValue(int age, int mileage) {
        double baseValue = 10000.0;
        double depreciation = (age * 0.05) + (mileage / 100000.0);
        return Math.max(baseValue * (1 - depreciation), 1000.0);
    }

    private void showMessage(String msg) {
        Dialog d = new Dialog(this, "Message", true);
        d.setLayout(new FlowLayout());
        d.add(new Label(msg));
        Button ok = new Button("OK");
        ok.addActionListener(ae -> d.dispose());
        d.add(ok);
        d.setSize(300, 120);
        d.setVisible(true);
    }
}
