package ui;

import db.DBConnection;
import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.sql.*;
import java.util.ArrayList;

public class ExchangeOffersFrame extends Frame implements ActionListener {

    Label lblTitle = new Label("Exchange Offers", Label.CENTER);
    TextField txtSearch = new TextField(20);
    Choice choiceFilter = new Choice();
    TextArea taOffers = new TextArea(20, 60);
    Button btnApprove = new Button("Approve");
    Button btnReject = new Button("Reject");
    Button btnBack = new Button("Back");
    Button btnExport = new Button("Export CSV");
    Button btnFilter = new Button("Search");

    int userId = -1; // -1 for admin view
    boolean isAdmin;

    ArrayList<Integer> offerIds = new ArrayList<>(); // track visible offer IDs for filtering

    public ExchangeOffersFrame() { // Admin constructor
        this.isAdmin = true;
        setupFrame();
        loadOffers(null, null);
    }

    public ExchangeOffersFrame(int userId) { // Citizen constructor
        this.userId = userId;
        this.isAdmin = false;
        setupFrame();
        loadOffers(null, null);
    }

    private void setupFrame() {
        setTitle("Exchange Offers - GVEI");
        setSize(900, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        add(lblTitle, BorderLayout.NORTH);

        Panel topPanel = new Panel(new FlowLayout());
        topPanel.add(new Label("Search:"));
        topPanel.add(txtSearch);

        choiceFilter.add("All"); choiceFilter.add("applied"); choiceFilter.add("approved"); choiceFilter.add("rejected");
        topPanel.add(new Label("Status:")); topPanel.add(choiceFilter);

        topPanel.add(btnFilter); topPanel.add(btnExport);

        add(topPanel, BorderLayout.NORTH);

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
        btnFilter.addActionListener(this);
        btnExport.addActionListener(this);

        setVisible(true);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
                if (isAdmin) new AdminDashboard();
                else new CitizenDashboard(userId);
            }
        });
    }

    private void loadOffers(String search, String statusFilter) {
        taOffers.setText("");
        offerIds.clear();

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            String query = "SELECT eo.offer_id, u.name, v.plate_no, v.vehicle_type, v.fuel_type, eo.exchange_value, eo.subsidy_percent, eo.status " +
                    "FROM exchange_offers eo " +
                    "JOIN vehicles v ON eo.vehicle_id=v.vehicle_id " +
                    "JOIN users u ON v.owner_id=u.user_id";

            if (!isAdmin) query += " WHERE u.user_id=" + userId;

            if (search != null && !search.isEmpty()) {
                query += (query.contains("WHERE") ? " AND " : " WHERE ") +
                        "(u.name LIKE '%" + search + "%' OR v.plate_no LIKE '%" + search + "%')";
            }

            if (statusFilter != null && !statusFilter.equals("All")) {
                query += (query.contains("WHERE") ? " AND " : " WHERE ") + "eo.status='" + statusFilter + "'";
            }

            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                int id = rs.getInt("offer_id");
                offerIds.add(id);
                taOffers.append(
                        "Offer ID: " + id +
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
            String input = promptForOfferId();
            if (input == null || input.isEmpty()) return;

            int offerId;
            try {
                offerId = Integer.parseInt(input);
            } catch (NumberFormatException ex) {
                showMessage("Invalid Offer ID!");
                return;
            }

            String newStatus = (e.getSource() == btnApprove) ? "approved" : "rejected";
            updateOfferStatus(offerId, newStatus);
            loadOffers(txtSearch.getText(), choiceFilter.getSelectedItem());
        } else if (e.getSource() == btnFilter) {
            loadOffers(txtSearch.getText(), choiceFilter.getSelectedItem());
        } else if (e.getSource() == btnExport) {
            exportToCSV();
        }
    }

    private String promptForOfferId() {
        Dialog dialog = new Dialog(this, "Enter Offer ID", true);
        dialog.setLayout(new FlowLayout());

        Label lbl = new Label("Offer ID:");
        TextField txt = new TextField(10);
        Button ok = new Button("OK");

        final String[] result = {null};
        ok.addActionListener(ae -> {
            result[0] = txt.getText();
            dialog.dispose();
        });

        dialog.add(lbl);
        dialog.add(txt);
        dialog.add(ok);
        dialog.setSize(300, 150);
        dialog.setVisible(true);

        return result[0];
    }

    private void updateOfferStatus(int offerId, String status) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement("UPDATE exchange_offers SET status=? WHERE offer_id=?")) {
            pst.setString(1, status);
            pst.setInt(2, offerId);
            int updated = pst.executeUpdate();
            if (updated > 0) showMessage("Offer " + offerId + " marked as " + status);
            else showMessage("Offer ID not found!");
        } catch (SQLException ex) {
            ex.printStackTrace();
            showMessage("Error updating offer: " + ex.getMessage());
        }
    }

    private void exportToCSV() {
        try (FileWriter fw = new FileWriter("exchange_offers.csv")) {
            fw.write("OfferID,Owner,Plate,Type,Fuel,Value,Subsidy,Status\n");
            String[] lines = taOffers.getText().split("\n");
            for (String line : lines) {
                String csvLine = line.replaceAll("Offer ID: |, Owner: |, Plate: |, Type: |, Fuel: |, Value: \\$|, Subsidy: |%, Status: ", ",");
                fw.write(csvLine + "\n");
            }
            showMessage("Exported successfully to exchange_offers.csv");
        } catch (Exception ex) {
            ex.printStackTrace();
            showMessage("Error exporting CSV: " + ex.getMessage());
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
