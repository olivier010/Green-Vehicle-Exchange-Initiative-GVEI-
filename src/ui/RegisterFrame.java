package ui;

import db.DBConnection;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class RegisterFrame extends Frame implements ActionListener {
    Label lblTitle = new Label("Citizen Registration", Label.CENTER);
    Label lblName = new Label("Full Name:");
    Label lblEmail = new Label("Email:");
    Label lblPassword = new Label("Password:");
    TextField txtName = new TextField(25);
    TextField txtEmail = new TextField(25);
    TextField txtPassword = new TextField(25);
    Button btnRegister = new Button("Register");
    Button btnBack = new Button("Back");

    public RegisterFrame() {
        setLayout(new GridLayout(6, 2, 10, 10));
        setTitle("Register - GVEI");
        setSize(800, 600);

        txtPassword.setEchoChar('*');
        add(lblTitle); add(new Label(""));
        add(lblName); add(txtName);
        add(lblEmail); add(txtEmail);
        add(lblPassword); add(txtPassword);
        add(btnRegister); add(btnBack);

        btnRegister.addActionListener(this);
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
        if (e.getSource() == btnRegister) {
            registerUser();
        } else if (e.getSource() == btnBack) {
            new LoginFrame();
            dispose();
        }
    }

    private void registerUser() {
        String name = txtName.getText();
        String email = txtEmail.getText();
        String pass = txtPassword.getText();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(
                     "INSERT INTO users(name, email, password, role) VALUES(?, ?, ?, 'citizen')")) {
            pst.setString(1, name);
            pst.setString(2, email);
            pst.setString(3, pass);
            pst.executeUpdate();

            showMessage("Registration successful!");
            dispose();
            new LoginFrame();
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
        d.setSize(250, 100);
        d.setVisible(true);
    }
}
