package ui;

import db.DBConnection;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ExchangeOffersFrame extends Frame implements ActionListener {

    Label lblTitle = new Label("Exchange Offers", Label.CENTER);
    TextArea taOffers = new TextArea(20, 60);
    Button btnApprove = new Button("Approve");
    Button btnReject = new Button("Reject");
    Button btnBack = new Button("Back");

    int userId = -1; // -1 for admin view
    boolean isAdmin;

    public ExchangeOffersFrame() { // Admin constructor
        this.isAdmin = true;
        setupFrame();
        loadOffers();
    }

    public ExchangeOffersFrame(int userId) { // Citizen constructor
        this.userId = userId;
        this.isAdmin = false;
        setupFrame();
        loadOffers();
    }

    private void setupFrame() {
        setTitle("Exchange Offers - GVEI");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(20, 20));

        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        add(lblTitle, BorderLayout.NORTH);

        taOffers.setEditable(false);
        add(taOffers, BorderLayout.CENTER);

        Panel bottomPanel = new Panel(new FlowLayout());
        if (isAdmin) {
            bottomPanel.add(btnApprove);
            bottomPanel.add(btnReject);
        }
        bottomPanel.add(btnBack);
        add(bottomPanel, BorderLayout.SOUTH);

        btnApprove.addActionListener(this);
        btnReject.addActionListener(this);
        btnBack.addActionListener(this);

        setVisible(true);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
                if (isAdmin) new AdminDashboard();
                else new CitizenDashboard(userId);
            }
        });
    }

    private void loadOffers() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            String query = "SELECT eo.offer_id, u.name, v.plate_no, v.vehicle_type, v.fuel_type, eo.exchange_value, eo.subsidy_percent, eo.status " +
                    "FROM exchange_offers eo " +
                    "JOIN vehicles v ON eo.vehicle_id=v.vehicle_id " +
                    "JOIN users u ON v.owner_id=u.user_id";

            if (!isAdmin) query += " WHERE u.user_id=" + userId;

            ResultSet rs = stmt.executeQuery(query);

            taOffers.setText("");
            while (rs.next()) {
                taOffers.append(
                        "Offer ID: " + rs.getInt("offer_id") +
                                ", Owner: " + rs.getString("name") +
                                ", Plate: " + rs.getString("plate_no") +
                                ", Type: " + rs.getString("vehicle_type") +
                                ", Fuel: " + rs.getString("fuel_type") +
                                ", Value: $" + rs.getDouble("exchange_value") +
                                ", Subsidy: " + rs.getDouble("subsidy_percent") + "%" +
                                ", Status: " + rs.getString("status") + "\n"
                );
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            showMessage("Error loading offers: " + ex.getMessage());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnBack) {
            dispose();
            if (isAdmin) new AdminDashboard();
            else new CitizenDashboard(userId);
        } else if (isAdmin && (e.getSource() == btnApprove || e.getSource() == btnReject)) {
            String input = taOffers.getSelectedText();
            if (input == null || input.isEmpty()) {
                showMessage("Select the offer ID from text area first!");
                return;
            }
            int offerId = extractOfferId(input);
            if (offerId == -1) return;

            String newStatus = (e.getSource() == btnApprove) ? "approved" : "rejected";
            updateOfferStatus(offerId, newStatus);
            loadOffers();
        }
    }

    private int extractOfferId(String selectedText) {
        try {
            String[] parts = selectedText.split(",");
            for (String part : parts) {
                if (part.trim().startsWith("Offer ID:")) {
                    return Integer.parseInt(part.trim().substring(9).trim());
                }
            }
        } catch (Exception ex) {
            showMessage("Invalid selection! Make sure to select full line starting with 'Offer ID:'");
        }
        return -1;
    }

    private void updateOfferStatus(int offerId, String status) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement("UPDATE exchange_offers SET status=? WHERE offer_id=?")) {
            pst.setString(1, status);
            pst.setInt(2, offerId);
            pst.executeUpdate();
            showMessage("Offer " + offerId + " marked as " + status);
        } catch (SQLException ex) {
            ex.printStackTrace();
            showMessage("Error updating offer: " + ex.getMessage());
        }
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
