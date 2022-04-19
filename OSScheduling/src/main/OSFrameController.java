package main;

import java.net.URL;
import java.util.Observable;
import java.util.ResourceBundle;

import javafx.beans.property.IntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import util.schedulingTableModel;

public class OSFrameController implements Initializable{
	/* 싱글톤 패턴으로 지정합니다 */
	private static OSFrameController singletone = new OSFrameController();
	
	@FXML GridPane parentPane;
	@FXML HBox toolBar;									
	@FXML Button minButton;								// 최소화 버튼 
	@FXML Button closeButton;							// 닫기 버튼 
	
	@FXML SplitMenuButton schedulingTypeSelect;			// 스케줄링 타입
	@FXML TextField arrivalTimeInput;					// arrivalTime
	@FXML TextField burstTimeInput;						// burstTime 
	@FXML TextField timeQuantumInput;					// timeQuantum 
	@FXML SplitMenuButton pCoreSelect;					// pCore 선택기 
	@FXML SplitMenuButton eCoreSelect;					// eCore 선택기 
	@FXML Button startBtn;								// 시작버튼
	@FXML Button toUpBtn;								// 스케줄링 순번 변경(위)
	@FXML Button toDownBtn;								// 스케줄링 순번 변경(아래)
	@FXML Button toLeftBtn;								// 스케줄링 추가 
	@FXML Button toRightBtn;							// 스케줄링 삭제 
	
	// 스케줄링 리스트
	private static ObservableList<schedulingTableModel> schedulingList = FXCollections.observableArrayList();
	@FXML TableView<schedulingTableModel> listTable;
	@FXML TableColumn<schedulingTableModel, Number> processNoColumn;
	@FXML TableColumn<schedulingTableModel, Number> arrivalTimeColumn;
	@FXML TableColumn<schedulingTableModel, Number> burstTimeColumn;
	@FXML TableColumn<schedulingTableModel, Number> waitingTimeColumn;
	@FXML TableColumn<schedulingTableModel, Number> turnaroundTimeColumn;
	@FXML TableColumn<schedulingTableModel, Number> normalizedTimeColumn;
	
	private double xOffset = 0;
	private double yOffset = 0;
	
	private Stage stage = null;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		stageDragableMoveWindow();												// 창이동 Action
		initializeListTable();
		closeButton.setOnAction(event -> handleCloseButtonAction(event));		// 닫기버튼 Action
		minButton.setOnAction(event -> handleMinButtonAction(event));
		toLeftBtn.setOnAction(event -> handleToLeftBtnAction(event));
		
	}
	
	public static OSFrameController getInstance() {
		return singletone;
	}
	
	/* Scheduling 추가 버튼에 대한 Action을 지정합니다 */
	private void handleToLeftBtnAction(ActionEvent event) {
		int arrivalTime = Integer.parseInt(arrivalTimeInput.getText());
		int burstTime = Integer.parseInt(burstTimeInput.getText());
		
		schedulingTableModel model = new schedulingTableModel(arrivalTime, burstTime);
		schedulingList.add(model);
		
	}
	
	/* 스케줄링 리스트 테이블에 대한 초기화를 진행합니다 */
	@FXML
	private void initializeListTable() {
		listTable.setItems(schedulingList);
		processNoColumn.setCellValueFactory(cellData -> cellData.getValue().getProcessNo());
		arrivalTimeColumn.setCellValueFactory(cellData -> cellData.getValue().getArrivalTime());
		burstTimeColumn.setCellValueFactory(cellData -> cellData.getValue().getBurstTime());
		waitingTimeColumn.setCellValueFactory(cellData -> cellData.getValue().getWaitingTime());
		turnaroundTimeColumn.setCellValueFactory(cellData -> cellData.getValue().getTurnaroundTime());
		normalizedTimeColumn.setCellValueFactory(cellData -> cellData.getValue().getNormalizedTime());
	}

	/* 창 드래그에 대한 Action을 지정합니다 */
	@FXML
	private void stageDragableMoveWindow() {
		toolBar.setOnMousePressed((event) -> {
			xOffset = event.getSceneX();
			yOffset = event.getSceneY();
		});
		toolBar.setOnMouseDragged((event) -> {
			stage = (Stage) parentPane.getScene().getWindow();
			stage.setX(event.getScreenX() - xOffset);
			stage.setY(event.getScreenY() - yOffset);
			stage.setOpacity(0.8f); // 창 투명화
		});
		toolBar.setOnDragDone((event) -> {
			stage = (Stage) parentPane.getScene().getWindow();
			stage.setOpacity(1.0f);
		});
		toolBar.setOnMouseReleased((event) -> {
			stage = (Stage) parentPane.getScene().getWindow();
			stage.setOpacity(1.0f);
		});
	}
	
	/* 최소화 버튼에 대한 Action을 지정합니다 */
	@FXML
	private void handleMinButtonAction(ActionEvent event) {
		stage = (Stage) minButton.getScene().getWindow();
		stage.setIconified(true);
	}
	
	/* 닫기 버튼에 대한 Action을 지정합니다 */
	@FXML
	private void handleCloseButtonAction(ActionEvent event) {
		stage = (Stage) closeButton.getScene().getWindow();
		stage.close();
		System.exit(0);
	}
}
