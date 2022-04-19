package main;

import java.net.*;
import java.util.*;
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import scheduling.SRTNScheduling;
import scheduling.scheduling;
import util.*;

public class OSFrameController implements Initializable{
	/* 싱글톤 패턴으로 지정합니다 */
	private static OSFrameController singletone = new OSFrameController();
	private static PriorityQueue<workSection> schedulingList = new PriorityQueue<workSection>();		// 스케줄링 리스트
	private static scheduling scheduling = null;														// 스케줄링 타입
	private static coreUtil coreSet = new coreUtil(0, 0);
	private double xOffset = 0;
	private double yOffset = 0;
	private Stage stage = null;
	
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
	private static ObservableList<schedulingTableModel> schedulingTableList = FXCollections.observableArrayList();
	@FXML TableView<schedulingTableModel> listTable;
	@FXML TableColumn<schedulingTableModel, Number> processNoColumn;
	@FXML TableColumn<schedulingTableModel, Number> arrivalTimeColumn;
	@FXML TableColumn<schedulingTableModel, Number> burstTimeColumn;
	@FXML TableColumn<schedulingTableModel, Number> waitingTimeColumn;
	@FXML TableColumn<schedulingTableModel, Number> turnaroundTimeColumn;
	@FXML TableColumn<schedulingTableModel, Number> normalizedTimeColumn;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		stageDragableMoveWindow();												// 창이동 Action
		initializeListTable();													// Scheduling List Action 
		handleSchedulingSelectAction();											// Scheduling Type Action
		closeButton.setOnAction(event -> handleCloseButtonAction(event));		// 닫기버튼 Action
		minButton.setOnAction(event -> handleMinButtonAction(event));
		
		
		
		startBtn.setOnAction(event -> handleStartBtnAction(event));				// 시작버튼 Action
		
		toLeftBtn.setOnAction(event -> handleToLeftBtnAction(event));			// 추가버튼 Action
		
	}
	
	public static OSFrameController getInstance() {
		return singletone;
	}
	
	/* 시작 버튼에 대한 Action을 지정합니다 */
	private void handleStartBtnAction(ActionEvent event) {
		setSchedulingClass();													// 스케줄링 기법 정보 획득
		setCoreSet();															// 코어정보 설정 
		
		controlScheduling control = controlScheduling.getInstance();
		control.setScheduling(scheduling, schedulingList, coreSet);						// 스케줄링 정보 설정
		control.start();
	}
	
	/* 코어정보를 확인하여 반영합니다. */
	private void setCoreSet() {
		//int pCoreCnt = Integer.parseInt(pCoreSelect.getText());
		//int eCoreCnt = Integer.parseInt(eCoreSelect.getText());
		
		/* 코어 임시 설정 */
		int pCoreCnt = 1;
		int eCoreCnt = 1;
		
		coreSet = new coreUtil(pCoreCnt, eCoreCnt);
	}
	
	/* 스케줄링 정보에 따라서 scheduling Class를 변경합니다 */
	private void setSchedulingClass() {
		/* Scheduling Type 정보를 가져옵니다 */
		String schedulingType = schedulingTypeSelect.getText();
		
		switch (schedulingType) {
		case "FCFS":
			break;
		case "Round-Robin":
			break;
		case "SPN":
			break;
		case "SRTN":
			System.out.println("SRTN");
			scheduling = new SRTNScheduling();
			break;
		case "HRRN":
			break;
		}
	}
	
	
	/* Scheduling 추가 버튼에 대한 Action을 지정합니다 */
	private void handleToLeftBtnAction(ActionEvent event) {
		int arrivalTime = Integer.parseInt(arrivalTimeInput.getText());
		int burstTime = Integer.parseInt(burstTimeInput.getText());
		
		schedulingTableModel model = new schedulingTableModel(arrivalTime, burstTime);
		workSection work = new workSection(model.getProcessNo().get(), arrivalTime, burstTime);
		schedulingList.add(work);
		schedulingTableList.add(model);
		
	}
	
	/* 스케줄링 종류 선택에 대한 Action을 지정합니다 */
	private void handleSchedulingSelectAction() {
		ObservableList<MenuItem> menuList = schedulingTypeSelect.getItems();
		
		Iterator<MenuItem> it = menuList.iterator();
		while(it.hasNext()) {
			MenuItem menu = it.next();
			menu.setOnAction(event -> {
				schedulingTypeSelect.setText(menu.getText());
			});
		}
	}
	
	/* 스케줄링 리스트 테이블에 대한 초기화를 진행합니다 */
	@FXML
	private void initializeListTable() {
		listTable.setItems(schedulingTableList);
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
