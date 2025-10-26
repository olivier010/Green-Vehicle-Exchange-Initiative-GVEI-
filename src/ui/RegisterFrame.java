package ui;

import db.DBConnection;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class RegisterFrame extends Frame implements ActionListener {

    Label lblTitle = new Label("Register New User", Label.CENTER);
    Label lblName = new Label("Name:");
    Label lblEmail = new Label("Email:");
    Label lblPassword = new Label("Password:");
    Label lblRole = new Label("Role:");
    TextField txtName = new TextField(25);
    TextField txtEmail = new TextField(25);
    TextField txtPassword = new TextField(25);
    Choice choiceRole = new Choice();
    Button btnRegister = new Button("Register");
    Button btnBack = new Button("Back");

    public RegisterFrame() {
        setTitle("User Registration - GVEI");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(20, 20));

        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        add(lblTitle, BorderLayout.NORTH);

        Panel centerPanel = new Panel(new GridLayout(4, 2, 10, 10));
        centerPanel.add(lblName); centerPanel.add(txtName);
        centerPanel.add(lblEmail); centerPanel.add(txtEmail);
        centerPanel.add(lblPassword); centerPanel.add(txtPassword);
        centerPanel.add(lblRole);
        choiceRole.add("citizen"); choiceRole.add("admin");
        centerPanel.add(choiceRole);
        add(centerPanel, BorderLayout.CENTER);

        Panel bottomPanel = new Panel(new FlowLayout());
        bottomPanel.add(btnRegister); bottomPanel.add(btnBack);
        add(bottomPanel, BorderLayout.SOUTH);

        txtPassword.setEchoChar('*');

        btnRegister.addActionListener(this);
        btnBack.addActionListener(this);

        setVisible(true);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
                new LoginFrame();
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnRegister) registerUser();
        else if (e.getSource() == btnBack) {
            dispose();
            new LoginFrame();
        }
    }

    private void registerUser() {
        String name = txtName.getText();
        String email = txtEmail.getText();
        String password = txtPassword.getText();
        String role = choiceRole.getSelectedItem();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(
                     "INSERT INTO users(name,email,password,role) VALUES(?,?,?,?)")) {
            pst.setString(1, name);
            pst.setString(2, email);
            pst.setString(3, password);
            pst.setString(4, role);
            pst.executeUpdate();
            showMessage("User registered successfully!");
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
