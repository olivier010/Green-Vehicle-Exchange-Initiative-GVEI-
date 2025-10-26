Green Vehicle Exchange Initiative (GVEI) Desktop Application
Project Overview

The Green Vehicle Exchange Initiative (GVEI) is a desktop application developed using Java AWT and MySQL to support the Rwandan government’s program of promoting electric vehicles (EVs) by exchanging old fuel-powered vehicles.

The system allows citizens to register their vehicles, check exchange eligibility, and apply for government subsidies. Government officers can approve or reject applications and monitor key statistics such as total subsidies and carbon reduction.

Core Features
1. User Registration & Login

Citizens can create accounts and log in.

Admin (government officers) can login with special credentials.

Secure login with role-based access.

2. Vehicle Registration

Citizens can register their vehicles by providing:

Owner ID

Plate number

Vehicle type (Car, Bus, Motorcycle, etc.)

Fuel type

Manufacture year

Estimated mileage

3. Exchange Eligibility Check

System automatically determines eligibility based on:

Vehicle age > 5 years

Fuel type = Petrol/Diesel

4. Exchange Offer Management

The system calculates the exchange value and subsidy percentage.

Admin can approve or reject applications.

5. Reporting

Admin dashboard displays:

Total exchange offers

Pending, Approved, Rejected offers

Total subsidies

Estimated carbon reduction

Live auto-refresh for updated statistics.

Technical Details
Frontend

Developed with Java AWT:

Components: Frame, Panel, Label, TextField, Button, Choice, List

Layouts: GridLayout, BorderLayout, FlowLayout

Separate forms for:

Login / Registration

Vehicle Registration

Exchange Offers

Admin Dashboard

Backend

Database: MySQL (or Oracle)

JDBC for database connection

Suggested tables:

users(user_id, name, email, password, role)
vehicles(vehicle_id, owner_id, plate_no, vehicle_type, fuel_type, year, mileage)
exchange_offers(offer_id, vehicle_id, exchange_value, subsidy_percent, status)


CRUD operations implemented with PreparedStatements:

INSERT, SELECT, UPDATE, DELETE

Properly closes database connections to avoid leaks.

Project Structure
GVEI/
├─ src/
│  ├─ ui/
│  │  ├─ LoginFrame.java
│  │  ├─ RegisterFrame.java
│  │  ├─ CitizenDashboard.java
│  │  ├─ VehicleRegistrationFrame.java
│  │  ├─ AdminDashboard.java
│  │  └─ ExchangeOffersFrame.java
│  └─ db/
│     └─ DBConnection.java
├─ lib/
│  └─ mysql-connector-java-x.x.x.jar
├─ README.md
└─ gvei_db.sql

Installation & Setup

Install Java JDK (version 21 recommended).

Install MySQL and create a database named gvei_db.

Import the provided SQL file (gvei_db.sql) to create tables and an initial admin user.

Add MySQL Connector JAR to your project classpath.

Configure DBConnection.java with the correct database URL, username, and password.

private static final String URL = "jdbc:mysql://localhost:3306/gvei_db";
private static final String USER = "root";
private static final String PASSWORD = "";


Compile and run the project using your IDE (IntelliJ, Eclipse, etc.) or via command line.

How to Use
Admin

Log in using admin credentials.

View a  live dashboard of exchange offers.

Approve/Reject pending offers.

View statistics (subsidies, carbon reduction).

Citizen

Register a new account.

Log in and register vehicles.

Check eligibility and see exchange offers.

Future Improvements

Add email notifications for citizens when offers are approved/rejected.

Implement data validation and password hashing.

Add report export (CSV, PDF).

Enhance UI with Swing or JavaFX for a modern look.

Author

NSENGIYUMVA YVES Olivier – Advanced diplomas in Information Technology – Developer
