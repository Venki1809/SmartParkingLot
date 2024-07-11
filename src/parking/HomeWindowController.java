package parking;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;
import java.io.IOException;

public class HomeWindowController {

	 @FXML
	    private void handleEnterVehicleAction(ActionEvent event) {
	        try {
	            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/parking/entry/EnterVehicleForm.fxml"));
	            Parent root = fxmlLoader.load();
	            Stage stage = new Stage();
	            stage.setTitle("Enter Vehicle");
	            stage.setScene(new Scene(root));
	            stage.show();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
    @FXML
    private void handleExitVehicleAction(ActionEvent event) {
    	try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/parking/exit/ExitCarForm.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Exit Vehicle");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAdminControlAction(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/parking/admin/AdminPanel.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Admin Panel");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

