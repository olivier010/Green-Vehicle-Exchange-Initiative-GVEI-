package ui;

import db.DBConnection;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Timer;
import java.util.TimerTask;

public class AdminDashboard extends Frame implements ActionListener {

    Label lblTitle = new Label("Admin Dashboard - GVEI", Label.CENTER);
    Label lblTotalOffers = new Label("Total Offers: 0");
    Label lblPending = new Label("Pending Offers: 0");
    Label lblApproved = new Label("Approved Offers: 0");
    Label lblRejected = new Label("Rejected Offers: 0");

    Button btnViewOffers = new Button("View Exchange Offers");
    Button btnStats = new Button("View Statistics");
    Button btnLogout = new Button("Logout");

    Timer timer;

    public AdminDashboard() {
        setTitle("Admin Dashboard - GVEI");
        setSize(800, 600);
        setLayout(new GridLayout(7, 1, 10, 10));

        add(lblTitle);
        add(lblTotalOffers);
        add(lblPending);
        add(lblApproved);
        add(lblRejected);
        add(btnViewOffers);
        add(btnStats);
        add(btnLogout);

        btnViewOffers.addActionListener(this);
        btnStats.addActionListener(this);
        btnLogout.addActionListener(this);

        loadOfferCounts();

        // Auto-refresh every 5 seconds
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                loadOfferCounts();
            }
        }, 0, 5000); // delay 0ms, repeat every 5000ms

        setVisible(true);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                timer.cancel(); // stop the timer
                dispose();
                System.exit(0);
            }
        });
    }

    private void loadOfferCounts() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM exchange_offers");
            if (rs.next()) lblTotalOffers.setText("Total Offers: " + rs.getInt("total"));

            rs = stmt.executeQuery("SELECT COUNT(*) AS pending FROM exchange_offers WHERE status='applied'");
            if (rs.next()) lblPending.setText("Pending Offers: " + rs.getInt("pending"));

            rs = stmt.executeQuery("SELECT COUNT(*) AS approved FROM exchange_offers WHERE status='approved'");
            if (rs.next()) lblApproved.setText("Approved Offers: " + rs.getInt("approved"));

            rs = stmt.executeQuery("SELECT COUNT(*) AS rejected FROM exchange_offers WHERE status='rejected'");
            if (rs.next()) lblRejected.setText("Rejected Offers: " + rs.getInt("rejected"));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnViewOffers) {
            new ExchangeOffersFrame();
        } else if (e.getSource() == btnStats) {
            showStatistics();
        } else if (e.getSource() == btnLogout) {
            timer.cancel(); // stop auto-refresh
            new LoginFrame();
            dispose();
        }
    }

    private void showStatistics() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rs1 = stmt.executeQuery("SELECT COUNT(*) AS total FROM exchange_offers WHERE status='approved'");
            rs1.next();
            int totalExchanged = rs1.getInt("total");

            ResultSet rs2 = stmt.executeQuery("SELECT SUM(exchange_value * subsidy_percent / 100) AS total_subsidy FROM exchange_offers WHERE status='approved'");
            rs2.next();
            double totalSubsidy = rs2.getDouble("total_subsidy");

            int carbonReduction = totalExchanged; // assume 1 ton per vehicle

            String stats = "Total Exchanged Vehicles: " + totalExchanged +
                    "\nTotal Subsidies: $" + totalSubsidy +
                    "\nEstimated Carbon Reduction: " + carbonReduction + " tons";

            showMessage(stats);

        } catch (Exception ex) {
            ex.printStackTrace();
            showMessage("Error fetching statistics: " + ex.getMessage());
        }
    }

    private void showMessage(String msg) {
        Dialog d = new Dialog(this, "Statistics", true);
        d.setLayout(new FlowLayout());
        d.add(new Label(msg));
        Button ok = new Button("OK");
        ok.addActionListener(ae -> d.dispose());
        d.add(ok);
        d.setSize(300, 200);
        d.setVisible(true);
    }
}
