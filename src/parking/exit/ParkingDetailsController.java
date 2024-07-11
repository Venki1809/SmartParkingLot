package parking.exit;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;


import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import database.databaseConnector;

public class ParkingDetailsController {

    @FXML
    private TextField idLabel;

    @FXML
    private TextField vehicleNumberLabel;

    @FXML
    private TextField vehicleTypeLabel;

    @FXML
    private TextField spotIdLabel;

    @FXML
    private TextField emailIdLabel;

    @FXML
    private TextField entryTimeLabel;

    @FXML
    private TextField exitTimeLabel;

    @FXML
    private TextField fareLabel;

    @FXML
    private ToggleGroup paymentTypeGroup;
    
    @FXML
    private Button submit;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void initializeData(int id, String vehicleNumber, String vehicleType, int spotId, String emailId,
                               LocalDateTime entryTime, LocalDateTime exitTime, double fare) {
        // Set data to text fields
        idLabel.setText(String.valueOf(id));
        vehicleNumberLabel.setText(vehicleNumber);
        vehicleTypeLabel.setText(vehicleType);
        spotIdLabel.setText(String.valueOf(spotId));
        emailIdLabel.setText(emailId);
        entryTimeLabel.setText(entryTime.format(formatter));
        exitTimeLabel.setText(exitTime.format(formatter));
        fareLabel.setText(String.format("%.2f", fare));
    }

    @FXML
    private void handleSubmit() {
        RadioButton selectedRadioButton = (RadioButton) paymentTypeGroup.getSelectedToggle();
        String paymentType = selectedRadioButton.getText();
        submit.setDisable(true);
        

        // Store details in the history table
        storeInHistoryTable(paymentType);
    }
    @FXML
    private void handleHome() {
    	Stage stage = (Stage) idLabel.getScene().getWindow();
        stage.close();
    }

    private void storeInHistoryTable(String paymentType) {
       

        String insertQuery = "INSERT INTO History (id, vehicle_number, vehicle_type, spot_id, entry_time, exit_time, payment_type) VALUES (?, ?, ?, ?, ?, ?,?)";

        try (Connection connection = databaseConnector.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {

            preparedStatement.setInt(1, Integer.parseInt(idLabel.getText()));
            preparedStatement.setString(2, vehicleNumberLabel.getText());
            preparedStatement.setString(3, vehicleTypeLabel.getText());
            preparedStatement.setInt(4, Integer.parseInt(spotIdLabel.getText()));
            preparedStatement.setString(5, entryTimeLabel.getText());
            preparedStatement.setString(6, exitTimeLabel.getText());
            preparedStatement.setString(7, paymentType);
            preparedStatement.executeUpdate();
            System.out.println("Details successfully stored in the history table.");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error storing details in the history table.");
        }
    }
}
