package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class runApp extends Application{

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		try {
			Font.loadFont(getClass().getResource("NanumGothic.ttf").toString(), 10);
			Font.loadFont(getClass().getResource("NanumGothicBold.ttf").toString(), 10);
			
			GridPane root = (GridPane) FXMLLoader.load(getClass().getResource("OSFrame.fxml"));
			Scene scene = new Scene(root, 950, 880);
			
			primaryStage.setScene(scene);
			primaryStage.setResizable(false);
			primaryStage.initStyle(StageStyle.UNDECORATED);
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
