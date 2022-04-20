package util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class alertUtil{
	private static Alert alert;
	
	public alertUtil(AlertType alertTyle, String headerText, String contentText) {
		alert = new Alert(alertTyle);
		alert.setHeaderText(headerText);
		alert.setContentText(contentText);
	}
	
	public void showAlert() {
		alert.showAndWait();
	}
}
