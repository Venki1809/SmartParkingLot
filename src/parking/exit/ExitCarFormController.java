package parking.exit;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import database.databaseConnector;;

public class ExitCarFormController {

    @FXML
    private TextField vehicleNumberField;

    private Stage dialogStage;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    @FXML
    private void handleCancel() {
    	 Stage stage = (Stage) vehicleNumberField.getScene().getWindow();
         stage.close();
    }

    @FXML
    private void handleSubmit() {
        String vehicleNumber = vehicleNumberField.getText();

        if (vehicleNumber.isEmpty()) {
            showAlert("Error", "Please enter a vehicle number.");
            return;
        }

        try (Connection connection = databaseConnector.getConnection()) {
            // Query to retrieve parking details
            String query = "SELECT * FROM ParkingLot WHERE vehicle_number = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, vehicleNumber);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        // Retrieve data from result set
                        int id = resultSet.getInt("id");
                        String vehicleType = resultSet.getString("vehicle_type");
                        int spotId = resultSet.getInt("spot_id");
                        String emailId = resultSet.getString("email_id");
                        LocalDateTime entryTime = resultSet.getTimestamp("entry_time").toLocalDateTime();

                        // Calculate fare (for example, based on entry time)
                        LocalDateTime exitTime = LocalDateTime.now();
                        long minutesParked = Duration.between(entryTime, exitTime).toMinutes();
                        double fare = calculateFare(minutesParked,vehicleType);

                        // Display parking details in a new window
                        showParkingDetails(id, vehicleNumber, vehicleType, spotId, emailId, entryTime, exitTime, fare);

                        // Optionally, you can update the database to mark spot as available and clear parking record
                        markSpotAsAvailable(connection, spotId);
                        clearParkingRecord(connection, id);

                    } else {
                        showAlert("Error", "Vehicle not found in parking records.");
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Error retrieving data from the database.");
        }
    }

    private double calculateFare(long minutesParked, String vehicleType) throws SQLException {
        double fareRate = 0;
        Connection connection = databaseConnector.getConnection();
        
        // Determine the correct column based on vehicle type
        String column = vehicleType.equals("2") ? "fare_2_wheelers" : "fare_4wheelers";
        System.out.println(column + " " + vehicleType);
        
        // Construct the query dynamically
        String query = "SELECT " + column + " FROM Admin";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        ResultSet resultSet = preparedStatement.executeQuery();
        
        // Fetch the fare rate from the result set
        if (resultSet.next()) {
            fareRate = resultSet.getDouble(column);
            System.out.println("Fare rate: " + fareRate);
        } else {
            throw new SQLException("No fare rate found for vehicle type: " + vehicleType);
        }
        
        resultSet.close();
        preparedStatement.close();
        connection.close();
        
        // Example calculation: fare rate per hour
        double res = minutesParked * fareRate / 60.0;
        
        return Math.ceil(res);
    }


    private void markSpotAsAvailable(Connection connection, int spotId) throws SQLException {
        // Example: Update Spot table to mark spot as available
        String updateQuery = "UPDATE Spot SET is_occupied = false WHERE id = ?";
        try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
            updateStatement.setInt(1, spotId);
            updateStatement.executeUpdate();
        }
    }

    private void clearParkingRecord(Connection connection, int id) throws SQLException {
        // Example: Delete parking record from ParkingLot table
        String deleteQuery = "DELETE FROM ParkingLot WHERE id = ?";
        try (PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery)) {
            deleteStatement.setInt(1, id);
            deleteStatement.executeUpdate();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showParkingDetails(int id, String vehicleNumber, String vehicleType, int spotId, String emailId,
                                     LocalDateTime entryTime, LocalDateTime exitTime, double fare) {
        try {
            // Load the FXML file for the parking details window
        	
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/parking/exit/ParkingDetails.fxml"));
            Parent root = loader.load();

            // Get the controller instance from the loader
            ParkingDetailsController controller = loader.getController();

            // Pass the data to the controller
            controller.initializeData(id, vehicleNumber, vehicleType, spotId, emailId, entryTime, exitTime, fare);

            // Create a new stage for the parking details
            Stage detailsStage = new Stage();
            detailsStage.setTitle("Parking Details");
            detailsStage.setScene(new Scene(root));
            detailsStage.show();
            Stage stage = (Stage) vehicleNumberField.getScene().getWindow();
            stage.close();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load Parking Details.");
        }
    }
}
