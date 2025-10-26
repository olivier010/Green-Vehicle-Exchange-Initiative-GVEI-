package ui;

import db.DBConnection;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Timer;
import java.util.TimerTask;

// Canvas to draw a simple bar chart
class StatsChartCanvas extends Canvas {

    private int pending, approved, rejected;

    public StatsChartCanvas(int pending, int approved, int rejected) {
        this.pending = pending;
        this.approved = approved;
        this.rejected = rejected;
        setSize(400, 300);
    }

    @Override
    public void paint(Graphics g) {
        int max = Math.max(pending, Math.max(approved, rejected));
        if (max == 0) max = 1; // avoid division by zero
        int width = 80;
        int spacing = 50;
        int baseY = 250;

        // Pending - Red
        g.setColor(Color.RED);
        int pendingHeight = (int) ((pending / (double) max) * 200);
        g.fillRect(spacing, baseY - pendingHeight, width, pendingHeight);
        g.drawString("Pending", spacing + 10, baseY + 20);
        g.drawString(String.valueOf(pending), spacing + 25, baseY - pendingHeight - 5);

        // Approved - Green
        g.setColor(Color.GREEN);
        int approvedHeight = (int) ((approved / (double) max) * 200);
        g.fillRect(spacing*2 + width, baseY - approvedHeight, width, approvedHeight);
        g.drawString("Approved", spacing*2 + width + 10, baseY + 20);
        g.drawString(String.valueOf(approved), spacing*2 + width + 25, baseY - approvedHeight - 5);

        // Rejected - Gray
        g.setColor(Color.GRAY);
        int rejectedHeight = (int) ((rejected / (double) max) * 200);
        g.fillRect(spacing*3 + width*2, baseY - rejectedHeight, width, rejectedHeight);
        g.drawString("Rejected", spacing*3 + width*2 + 10, baseY + 20);
        g.drawString(String.valueOf(rejected), spacing*3 + width*2 + 25, baseY - rejectedHeight - 5);
    }
}

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
        setLocationRelativeTo(null); // center screen
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
        }, 0, 5000);

        setVisible(true);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                timer.cancel();
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
            timer.cancel();
            new LoginFrame();
            dispose();
        }
    }

    private void showStatistics() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rsPending = stmt.executeQuery("SELECT COUNT(*) AS pending FROM exchange_offers WHERE status='applied'");
            rsPending.next();
            int pending = rsPending.getInt("pending");

            ResultSet rsApproved = stmt.executeQuery("SELECT COUNT(*) AS approved FROM exchange_offers WHERE status='approved'");
            rsApproved.next();
            int approved = rsApproved.getInt("approved");

            ResultSet rsRejected = stmt.executeQuery("SELECT COUNT(*) AS rejected FROM exchange_offers WHERE status='rejected'");
            rsRejected.next();
            int rejected = rsRejected.getInt("rejected");

            // Display chart dialog
            Dialog chartDialog = new Dialog(this, "Exchange Offer Statistics", true);
            chartDialog.setSize(500, 400);
            chartDialog.setLayout(new BorderLayout());

            StatsChartCanvas chart = new StatsChartCanvas(pending, approved, rejected);
            chartDialog.add(chart, BorderLayout.CENTER);

            Button btnClose = new Button("Close");
            btnClose.addActionListener(ae -> chartDialog.dispose());
            Panel panel = new Panel();
            panel.add(btnClose);
            chartDialog.add(panel, BorderLayout.SOUTH);

            chartDialog.setVisible(true);

        } catch (SQLException ex) {
            ex.printStackTrace();
            showMessage("Error fetching statistics: " + ex.getMessage());
        }
    }

    private void showMessage(String msg) {
        Dialog d = new Dialog(this, "Message", true);
        d.setLayout(new FlowLayout());
        d.add(new Label(msg));
        Button ok = new Button("OK");
        ok.addActionListener(ae -> d.dispose());
        d.add(ok);
        d.setSize(300, 200);
        d.setVisible(true);
    }
}
