package ui;

import db.DBConnection;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ExchangeOffersFrame extends Frame implements ActionListener {
    List offerList = new List();
    Button btnApprove = new Button("Approve");
    Button btnReject = new Button("Reject");
    Button btnBack = new Button("Back");

    public ExchangeOffersFrame() {
        setTitle("Exchange Offers - GVEI");
        setSize(800, 600); // bigger frame
        setLayout(new GridLayout(5, 2, 10, 10));

        Panel panelButtons = new Panel();
        panelButtons.add(btnApprove);
        panelButtons.add(btnReject);
        panelButtons.add(btnBack);

        add(offerList, BorderLayout.CENTER);
        add(panelButtons, BorderLayout.SOUTH);

        // Add action listeners
        btnApprove.addActionListener(this);
        btnReject.addActionListener(this);
        btnBack.addActionListener(this);

        loadOffers();

        setVisible(true);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose(); // Close only this frame
            }
        });
    }

    private void loadOffers() {
        offerList.removeAll();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT eo.offer_id, v.plate_no, v.vehicle_type, v.fuel_type, eo.exchange_value, eo.subsidy_percent, eo.status " +
                             "FROM exchange_offers eo JOIN vehicles v ON eo.vehicle_id = v.vehicle_id")) {

            while (rs.next()) {
                int offerId = rs.getInt("offer_id");
                String plate = rs.getString("plate_no");
                String type = rs.getString("vehicle_type");
                String fuel = rs.getString("fuel_type");
                double value = rs.getDouble("exchange_value");
                double subsidy = rs.getDouble("subsidy_percent");
                String status = rs.getString("status");

                offerList.add(offerId + ": " + plate + " | " + type + " | " + fuel +
                        " | Value: $" + value + " | Subsidy: " + subsidy + "% | Status: " + status);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String selected = offerList.getSelectedItem();

        if (e.getSource() == btnBack) {
            dispose(); // just close this frame, AdminDashboard stays open
            return;
        }

        if (selected == null) {
            showMessage("Please select an offer first!");
            return;
        }

        int offerId = Integer.parseInt(selected.split(":")[0]);

        if (e.getSource() == btnApprove) {
            updateOfferStatus(offerId, "approved");
        } else if (e.getSource() == btnReject) {
            updateOfferStatus(offerId, "rejected");
        }
    }

    private void updateOfferStatus(int offerId, String status) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement("UPDATE exchange_offers SET status=? WHERE offer_id=?")) {
            pst.setString(1, status);
            pst.setInt(2, offerId);
            pst.executeUpdate();
            showMessage("Offer " + status + " successfully!");
            loadOffers(); // refresh list
        } catch (Exception ex) {
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
        d.setSize(300, 120);
        d.setVisible(true);
    }
}
