package ui;

import db.DBConnection;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginFrame extends Frame implements ActionListener {
    Label lblTitle = new Label("GVEI Login", Label.CENTER);
    Label lblEmail = new Label("Email:");
    Label lblPassword = new Label("Password:");
    TextField txtEmail = new TextField(25);
    TextField txtPassword = new TextField(25);
    Button btnLogin = new Button("Login");
    Button btnRegister = new Button("Register");

    public LoginFrame() {
        setTitle("Green Vehicle Exchange Initiative");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(20, 20));

        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        add(lblTitle, BorderLayout.NORTH);

        Panel centerPanel = new Panel(new GridLayout(2, 2, 10, 10));
        centerPanel.add(lblEmail);
        centerPanel.add(txtEmail);
        centerPanel.add(lblPassword);
        centerPanel.add(txtPassword);
        add(centerPanel, BorderLayout.CENTER);

        Panel bottomPanel = new Panel(new FlowLayout());
        bottomPanel.add(btnLogin);
        bottomPanel.add(btnRegister);
        add(bottomPanel, BorderLayout.SOUTH);

        txtPassword.setEchoChar('*');

        btnLogin.addActionListener(this);
        btnRegister.addActionListener(this);

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
        if (e.getSource() == btnLogin) loginUser();
        else if (e.getSource() == btnRegister) {
            new RegisterFrame();
            dispose();
        }
    }

    private void loginUser() {
        String email = txtEmail.getText();
        String pass = txtPassword.getText();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(
                     "SELECT * FROM users WHERE email=? AND password=?")) {
            pst.setString(1, email);
            pst.setString(2, pass);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                String role = rs.getString("role");
                int userId = rs.getInt("user_id");
                dispose();
                if (role.equals("citizen")) new CitizenDashboard(userId);
                else new AdminDashboard();
            } else showMessage("Invalid credentials!");
        } catch (Exception ex) {
            ex.printStackTrace();
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
