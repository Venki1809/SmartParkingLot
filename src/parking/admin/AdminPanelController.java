package parking.admin;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import database.databaseConnector;

import java.util.Optional;

public class AdminPanelController {

    @FXML
    private TabPane adminTabPane;

    @FXML
    private TextField numberOf2WheelerSpotsField;

    @FXML
    private TextField numberOf4WheelerSpotsField;

    @FXML
    private TextField parkingFareField;

    @FXML
    private TextField parkingFareField1;

    @FXML
    private TextField currentPasswordField;

    @FXML
    private TextField newPasswordField;

    @FXML
    private TextField confirmNewPasswordField;

    @FXML
    private void handleSaveSettings() {
        String numberOf2WheelerSpots = numberOf2WheelerSpotsField.getText();
        String numberOf4WheelerSpots = numberOf4WheelerSpotsField.getText();
        String parkingFare2Wheeler = parkingFareField.getText();
        String parkingFare4Wheeler = parkingFareField1.getText();

        Alert confirmationAlert = new Alert(AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirmation");
        confirmationAlert.setHeaderText("Are you sure you want to save these settings?");
        confirmationAlert.setContentText("2-Wheeler Spots: " + numberOf2WheelerSpots + "\n" +
                "4-Wheeler Spots: " + numberOf4WheelerSpots + "\n" +
                "2-Wheeler Fare: " + parkingFare2Wheeler + "\n" +
                "4-Wheeler Fare: " + parkingFare4Wheeler);

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            saveSettings(numberOf2WheelerSpots, numberOf4WheelerSpots, parkingFare2Wheeler, parkingFare4Wheeler);
            showAlert(AlertType.INFORMATION, "Success", "Settings saved successfully!");
        }
    }
    
    private void initializeSpotsTable(Connection connection, int numberOfSpots, int spotType) {
        String deleteQuery = "DELETE FROM spot WHERE spot_type = ?";
        String insertQuery = "INSERT INTO spot (id, spot_number, spot_type, is_occupied) VALUES (?, ?, ?, ?)";

        try (PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery);
             PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
            // Delete existing spots of the given type
            deleteStmt.setInt(1, spotType);
            deleteStmt.executeUpdate();

            // Initialize spots
            int startId = (spotType == 2) ? 1 : getStartIdFor4Wheelers(connection);
            for (int i = 0; i < numberOfSpots; i++) {
                int id = startId + i;
                insertStmt.setInt(1, id);
                insertStmt.setInt(2, id);
                insertStmt.setInt(3, spotType);
                insertStmt.setInt(4, 0);
                insertStmt.executeUpdate();
            }
            System.out.println("Initialized " + numberOfSpots + " spot(s) for spot_type " + spotType);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Database Error: Error initializing spots table.");
        }
    }

    private int getStartIdFor4Wheelers(Connection connection) throws SQLException {
        String query = "SELECT MAX(id) FROM spot WHERE spot_type = 2";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1) + 1;
            } else {
                return 1; // If no 2-wheeler spots, start from 1
            }
        }
    }

    private void saveSettings(String numberOf2WheelerSpots, String numberOf4WheelerSpots, String parkingFare2Wheeler, String parkingFare4Wheeler) {
    	if (numberOf2WheelerSpots != null && !numberOf2WheelerSpots.isEmpty()) {
            try (Connection connection = databaseConnector.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement("UPDATE admin SET available_spots_2wheelers = ?")) {
                int n2 = Integer.parseInt(numberOf2WheelerSpots);
                preparedStatement.setInt(1, n2);
                int updatedRows = preparedStatement.executeUpdate();
                System.out.println("Updated " + updatedRows + " row(s) for available_spots_2wheelers: " + n2);
                initializeSpotsTable(connection, n2, 2); // Initialize 2-wheeler spots
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Database Error: Error updating available_spots_2wheelers.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format for available_spots_2wheelers: " + numberOf2WheelerSpots);
            }
        }

        // Update 4-Wheeler Spots if not null
        if (numberOf4WheelerSpots != null && !numberOf4WheelerSpots.isEmpty()) {
            try (Connection connection = databaseConnector.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement("UPDATE admin SET available_spots_4_wheelers = ?")) {
                int n4 = Integer.parseInt(numberOf4WheelerSpots);
                preparedStatement.setInt(1, n4);
                int updatedRows = preparedStatement.executeUpdate();
                System.out.println("Updated " + updatedRows + " row(s) for available_spots_4wheelers: " + n4);
                initializeSpotsTable(connection, n4, 4); // Initialize 4-wheeler spots
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Database Error: Error updating available_spots_4wheelers.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format for available_spots_4wheelers: " + numberOf4WheelerSpots);
            }
        }
    	    
    	    if (parkingFare2Wheeler != null && parkingFare2Wheeler!="") {
    	        try (Connection connection = databaseConnector.getConnection();
    	             PreparedStatement preparedStatement = connection.prepareStatement("UPDATE admin SET fare_2_wheelers = ?")) {
    	            int p2 = Integer.parseInt(parkingFare2Wheeler);
    	            preparedStatement.setInt(1, p2);
    	            int updatedRows = preparedStatement.executeUpdate();
    	            System.out.println("Updated " + updatedRows + " row(s) for parkingFare2Wheeler: " + p2);
    	        } catch (SQLException e) {
    	            e.printStackTrace();
    	            System.out.println("Database Error: Error updating fare_2_wheelers.");
    	        } catch (NumberFormatException e) {
    	            System.out.println("Invalid number format for parking fare 2 wheelers: " + parkingFare2Wheeler);
    	        }
    	    }
    	    
    	    if (parkingFare4Wheeler != null && parkingFare4Wheeler!="") {
    	        try (Connection connection = databaseConnector.getConnection();
    	             PreparedStatement preparedStatement = connection.prepareStatement("UPDATE admin SET fare_4wheelers = ?")) {
    	            int p4 = Integer.parseInt(parkingFare4Wheeler);
    	            preparedStatement.setInt(1, p4);
    	            int updatedRows = preparedStatement.executeUpdate();
    	            System.out.println("Updated " + updatedRows + " row(s) for parkingFare4Wheeler: " + p4);
    	        } catch (SQLException e) {
    	            e.printStackTrace();
    	            System.out.println("Database Error: Error updating fare_4_wheelers.");
    	        } catch (NumberFormatException e) {
    	            System.out.println("Invalid number format forparking fare 4wheelers: " + parkingFare4Wheeler);
    	        }
    	    }
    }

    @FXML
    private void handleChangePassword() {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmNewPassword = confirmNewPasswordField.getText();

        if (newPassword.equals(confirmNewPassword)) {
            try {
                String query = "SELECT password FROM admin WHERE username = ?"; // Update with the correct username if needed
                try (Connection connection = databaseConnector.getConnection();
                     PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setString(1, "admin"); // Replace with actual admin username if needed
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        String actualPasswordHash = resultSet.getString("password");
                        String currentPasswordHash = hashPassword(currentPassword);

                        if (actualPasswordHash.equals(currentPasswordHash)) {
                            String newPasswordHash = hashPassword(newPassword);
                            String updateQuery = "UPDATE admin SET password = ? WHERE username = ?";
                            try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                                updateStatement.setString(1, newPasswordHash);
                                updateStatement.setString(2, "admin"); // Replace with actual admin username if needed
                                updateStatement.executeUpdate();
                                showAlert(AlertType.INFORMATION, "Success", "Password changed successfully!");
                            }
                        } else {
                            showAlert(AlertType.ERROR, "Error", "Current password is incorrect!");
                        }
                    } else {
                        showAlert(AlertType.ERROR, "Error", "Admin user not found!");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(AlertType.ERROR, "Error", "Database error occurred!");
            }
        } else {
            showAlert(AlertType.ERROR, "Error", "New passwords do not match!");
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private void showAlert(AlertType alertType, String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

  
