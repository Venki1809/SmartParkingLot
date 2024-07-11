package parking.entry;

import database.databaseConnector;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EnterVehicleFormController {

    @FXML
    private TextField vehicleNumberField;

    @FXML
    private RadioButton twoWheelerRadioButton;

    @FXML
    private RadioButton fourWheelerRadioButton;
    
    @FXML
    private ToggleGroup wheelerGroup;

    @FXML
    private TextField emailIdField;

    private Stage dialogStage;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @FXML
    private void handleSubmit() {
        String vehicleNumber = vehicleNumberField.getText();
        
        RadioButton selectedRadioButton = (RadioButton) wheelerGroup.getSelectedToggle();
        String vehicleType = selectedRadioButton.getText();
        int vt=0;
       
        
        
        if (vehicleType.equals("2-Wheeler")) {
            vt = 2;
        } else if (vehicleType.equals("4-Wheeler")) {
        	 vt = 4;
        } else {
            showAlert("Error", "Please select a vehicle type.");
            return;
        }
        
        String emailId = emailIdField.getText();
        
        if (vehicleNumber.isEmpty() || vehicleType == null || emailId.isEmpty()) {
            showAlert("Error", "Please fill in all fields.");
            return;
        }

        try (Connection connection = databaseConnector.getConnection()) {
            // Check for spot availability
            String spotQuery = "SELECT id FROM Spot WHERE spot_type = ? AND is_occupied = 0 LIMIT 1";
            try (PreparedStatement spotStatement = connection.prepareStatement(spotQuery)) {
                spotStatement.setInt(1, vt);
                try (ResultSet spotResultSet = spotStatement.executeQuery()) {
                    if (spotResultSet.next()) {
                        int availableSpotId = spotResultSet.getInt("id");

                        // Insert vehicle details into ParkingLot table
                        String insertQuery = "INSERT INTO ParkingLot (vehicle_number, vehicle_type, spot_id, entry_time, email_id) VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?)";
                        try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                            insertStatement.setString(1, vehicleNumber);
                            insertStatement.setInt(2, vt);
                            insertStatement.setInt(3, availableSpotId);
                            insertStatement.setString(4, emailId);
                            insertStatement.executeUpdate();
                        }

                        // Mark the spot as occupied
                        String updateSpotQuery = "UPDATE Spot SET is_occupied = 1 WHERE id = ?";
                        try (PreparedStatement updateSpotStatement = connection.prepareStatement(updateSpotQuery)) {
                            updateSpotStatement.setInt(1, availableSpotId);
                            updateSpotStatement.executeUpdate();
                        }

                        // Display success details
                        showDetails(vehicleNumber, vehicleType, availableSpotId, emailId);

                    } else {
                        showAlert("No Spots Available", "No available spots for the selected vehicle type.");
                        returnHome();
                        if (dialogStage != null) {
                            dialogStage.close();
                        }
                        
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error connecting to the database.");
        }
        returnHome();
        
    }
    private void returnHome()
    {
    	Stage stage = (Stage) emailIdField.getScene().getWindow();
        stage.close();
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        
    }

    private void showDetails(String vehicleNumber, String vehicleType, int spotId, String emailId) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Vehicle Entered");
        alert.setHeaderText(null);
        alert.setContentText("Vehicle Number: " + vehicleNumber + "\nVehicle Type: " + vehicleType +
                "\nSpot ID: " + spotId + "\nEmail ID: " + emailId);
        alert.showAndWait();
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}
