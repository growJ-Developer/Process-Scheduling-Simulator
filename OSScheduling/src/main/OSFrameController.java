package main;

import java.net.*;
import java.util.*;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import scheduling.FCFSScheduling;
import scheduling.HRRNScheduling;
import scheduling.RRScheduling;
import scheduling.SPNScheduling;
import scheduling.SRTNScheduling;
import scheduling.scheduling;
import util.*;

public class OSFrameController implements Initializable{
	/* 싱글톤 패턴으로 지정합니다 */
	private static OSFrameController singletone = new OSFrameController();
	private static PriorityQueue<workSection> schedulingList = new PriorityQueue<workSection>();		// 스케줄링 리스트
	private static scheduling scheduling = null;														// 스케줄링 타입
	private static coreUtil coreSet = new coreUtil(0, 0);
	private int timeQuantum = 0;
	private double xOffset = 0;
	private double yOffset = 0;
	private Stage stage = null;
	private static int MAX_RUN_TIME = 30;
	
	@FXML private GridPane parentPane;
	@FXML private HBox toolBar;									
	@FXML private Button minButton;								// 최소화 버튼 
	@FXML private Button closeButton;							// 닫기 버튼 
	
	@FXML private SplitMenuButton schedulingTypeSelect;			// 스케줄링 타입
	@FXML private TextField arrivalTimeInput;					// arrivalTime
	@FXML private TextField burstTimeInput;						// burstTime 
	@FXML private TextField timeQuantumInput;					// timeQuantum 
	@FXML private SplitMenuButton pCoreSelect;					// pCore 선택기 
	@FXML private SplitMenuButton eCoreSelect;					// eCore 선택기 
	@FXML private Button startBtn;								// 시작버튼
	@FXML private Button stopBtn;								// 정지버튼
	@FXML private Button toUpBtn;								// 스케줄링 순번 변경(위)
	@FXML private Button toDownBtn;								// 스케줄링 순번 변경(아래)
	@FXML private Button toLeftBtn;								// 스케줄링 추가 
	@FXML private Button toRightBtn;							// 스케줄링 삭제 
	
	// Visualizer
	private static ObservableList<workSection> processStatusList = FXCollections.observableArrayList();
	@FXML private ListView<workSection> processStatus;				
	
	// 스케줄링 리스트
	private static ObservableList<schedulingTableModel> schedulingTableList = FXCollections.observableArrayList();
	@FXML private TableView<schedulingTableModel> listTable;
	@FXML private TableColumn<schedulingTableModel, Number> processNoColumn;
	@FXML private TableColumn<schedulingTableModel, Number> arrivalTimeColumn;
	@FXML private TableColumn<schedulingTableModel, Number> burstTimeColumn;
	@FXML private TableColumn<schedulingTableModel, Number> waitingTimeColumn;
	@FXML private TableColumn<schedulingTableModel, Number> turnaroundTimeColumn;
	@FXML private TableColumn<schedulingTableModel, Number> normalizedTimeColumn;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		stageDragableMoveWindow();												// 창이동 Action
		initializeListTable();													// Scheduling List Action
		initializeProcessStatus();												// Process Status(Visualizer) Action
		initializeTextField();													// Set Numeric Field
		handleSchedulingSelectAction();											// Scheduling Type Action
		handleCoreSelectAction();												// Core Selection Action
		timeQuantumInput.setDisable(true);
		closeButton.setOnAction(event -> handleCloseButtonAction(event));		// 닫기버튼 Action
		minButton.setOnAction(event -> handleMinButtonAction(event));
		
		startBtn.setOnAction(event -> handleStartBtnAction(event));				// 시작버튼 Action
		stopBtn.setOnAction(event -> handleStopBtnAction(event)); 				// 정지버튼 Action
		stopBtn.setVisible(false);
		toLeftBtn.setOnAction(event -> handleToLeftBtnAction(event));			// 추가버튼 Action
	}
	
	public static OSFrameController getInstance() {
		return singletone;
	}
	
	
	public void initializeProcessStatus() {
		processStatus.setOrientation(Orientation.HORIZONTAL);
		processStatus.setItems(processStatusList);
		processStatus.setCellFactory(new Callback<ListView<workSection>, ListCell<workSection>>() {
			@Override
			public ListCell<workSection> call(ListView<workSection> param) {
				return new StatusCell();
			}
		});
	}
	
	/* 프로세스 Visualizer를 설정합니다 */
	public void setProcessStatus(workSection work) {
		Platform.runLater(new Runnable() {	
			@Override
			public void run() {			
				processStatusList.add(work);
			}
		});
		
	}
	
	/* 시작 버튼에 대한 Action을 지정합니다 */
	private void handleStartBtnAction(ActionEvent event) {
		if(checkStartCondition()) {
			setSchedulingClass();													// 스케줄링 기법 정보 획득
			setCoreSet();										
			
			stopBtn.setVisible(true);
			startBtn.setVisible(false);
			
			/* 제어요소 비활성화 */
			arrivalTimeInput.setDisable(true);
			burstTimeInput.setDisable(true);
			timeQuantumInput.setDisable(true);
			pCoreSelect.setDisable(true);
			eCoreSelect.setDisable(true);
			
			/* Visualize 초기화 */
			processStatusList.clear();
			processStatus.setItems(processStatusList);
			
			scheduling.setScheduling(schedulingList, timeQuantum, coreSet);
			scheduling.runScheduling();
		}
		
	}
	
	/* 정상 입력 여부를 체크합니다 */
	private boolean checkStartCondition() {
		/* 코어 확인 */
		int pCoreCnt = Integer.parseInt(pCoreSelect.getText());
		int eCoreCnt = Integer.parseInt(eCoreSelect.getText());
		if(pCoreCnt + eCoreCnt <= 0) {
			alertUtil alert = new alertUtil(AlertType.WARNING, "Core Warnning", "Please specify a core for running Scheduling.");
			alert.showAlert();
			return false;
		}
		
		/* 스케줄링 확인 */
		if(listTable.getItems().size() <= 0) {
			alertUtil alert = new alertUtil(AlertType.WARNING, "Scheduling Warnning", "Scheduling to simulate does not exist.Please add a scheduling entry.");
			alert.showAlert();
			return false;
		}
	
		String timeQuantum = timeQuantumInput.getText();
		/* TimeQuantum 확인 */
		if(!timeQuantumInput.isDisable() && timeQuantum.length() <= 0) {
			alertUtil alert = new alertUtil(AlertType.WARNING, "Add Scheduling Warning", "Please wirte the timeQuantum.");
			alert.showAlert();
			return false;
		} else if(!timeQuantumInput.isDisable() && Integer.parseInt(timeQuantum) <= 0) {
			alertUtil alert = new alertUtil(AlertType.WARNING, "Add Scheduling Warning", "The timeQuantum must be at least zero.");
			alert.showAlert();
			return false;
		}
		return true;
		
	}
	
	/* 정지버튼에 대한 ACtion을 지정합니다. */
	private void handleStopBtnAction(ActionEvent event) {
		scheduling.stopScheduling();
		stopBtn.setVisible(false);
		startBtn.setVisible(true);
		
		
		
		/* 제어요소 비활성화 */
		arrivalTimeInput.setDisable(false);
		burstTimeInput.setDisable(false);
		setTimeQuantumDisable(schedulingTypeSelect.getText());
		pCoreSelect.setDisable(false);
		eCoreSelect.setDisable(false);
	}
	
	/* 코어정보를 확인하여 반영합니다. */
	private void setCoreSet() {
		int pCoreCnt = Integer.parseInt(pCoreSelect.getText());
		int eCoreCnt = Integer.parseInt(eCoreSelect.getText());
		
		coreSet = new coreUtil(pCoreCnt, eCoreCnt);
	}
	
	private void handleCoreSelectAction() {
		ObservableList<MenuItem> menuList = pCoreSelect.getItems();
		
		Iterator<MenuItem> it = menuList.iterator();
		while(it.hasNext()) {
			MenuItem menu = it.next();
			menu.setOnAction(event -> {
				pCoreSelect.setText(menu.getText());
			});
		}
		
		menuList = eCoreSelect.getItems();
		
		it = menuList.iterator();
		while(it.hasNext()) {
			MenuItem menu = it.next();
			menu.setOnAction(event -> {
				eCoreSelect.setText(menu.getText());
			});
		}
	}
	
	/* 스케줄링 정보에 따라서 scheduling Class를 변경합니다 */
	private void setSchedulingClass() {
		/* Scheduling Type 정보를 가져옵니다 */
		String schedulingType = schedulingTypeSelect.getText();
		
		switch (schedulingType) {
		case "FCFS":
			scheduling = new FCFSScheduling();
			break;
		case "Round-Robin":
			scheduling = new RRScheduling();
			break;
		case "SPN":
			scheduling = new SPNScheduling();
			break;
		case "SRTN":
			scheduling = new SRTNScheduling();
			break;
		case "HRRN":
			scheduling = new HRRNScheduling();
			break;
		}
	}
	
	
	/* Scheduling 추가 버튼에 대한 Action을 지정합니다 */
	private void handleToLeftBtnAction(ActionEvent event) {
		if(checkAddScheduleCondition()) {
			int arrivalTime = Integer.parseInt(arrivalTimeInput.getText());
			int burstTime = Integer.parseInt(burstTimeInput.getText());
			
			schedulingTableModel model = new schedulingTableModel(arrivalTime, burstTime);
			workSection work = new workSection(model.getProcessNo().get(), arrivalTime, burstTime, model.getColor());
			schedulingList.add(work);
			listTable.getItems().add(model);
		}
	}
	
	/* 스케줄링 추가 조건을 확인합니다 */
	private boolean checkAddScheduleCondition() {
		String arrivalTime = arrivalTimeInput.getText();
		String burstTime = burstTimeInput.getText();
		
		if(arrivalTime.length() <= 0) {
			alertUtil alert = new alertUtil(AlertType.WARNING, "Add Scheduling Warning", "Please wirte the arrivalTime.");
			alert.showAlert();
			return false;
		} else if(burstTime.length() <= 0) {
			alertUtil alert = new alertUtil(AlertType.WARNING, "Add Scheduling Warning", "Please wirte the burstTime.");
			alert.showAlert();
			return false;
		} else if(Integer.parseInt(burstTime) <= 0) {
			alertUtil alert = new alertUtil(AlertType.WARNING, "Add Scheduling Warning", "The BurstTime must be at least zero.");
			alert.showAlert();
			return false;
		} 
		
		/* 스케줄링 토탈 시간을 점검합니다 */
		int totalBurstTime = 0;
		for(schedulingTableModel model : schedulingTableList) {
			totalBurstTime += model.getBurstTime().get();
		}
		
		if(totalBurstTime > MAX_RUN_TIME) {
			alertUtil alert = new alertUtil(AlertType.WARNING, "Add Scheduling Warning", "The total execution time must be less than " + MAX_RUN_TIME +" seconds.");
			alert.showAlert();
			return false;
		}
		
		return true;
	}
	
	/* 스케줄링 종류 선택에 대한 Action을 지정합니다 */
	private void handleSchedulingSelectAction() {
		ObservableList<MenuItem> menuList = schedulingTypeSelect.getItems();
		
		Iterator<MenuItem> it = menuList.iterator();
		while(it.hasNext()) {
			MenuItem menu = it.next();
			menu.setOnAction(event -> {
				schedulingTypeSelect.setText(menu.getText());
				setTimeQuantumDisable(menu.getText());
			});
		}
	}
	
	/* TimeQuantum의 활성화 여부를 설정합니다 */
	private void setTimeQuantumDisable(String text) {
		switch (text) {
		case "Round-Robin":
			timeQuantumInput.setDisable(false);
			break;
		default:
			timeQuantumInput.setDisable(true);
			break;
		}
	}
	
	/* 스케줄링 리스트 테이블에 대한 초기화를 진행합니다 */
	@FXML
	private void initializeListTable() {
		listTable.setItems(schedulingTableList);
		processNoColumn.setCellValueFactory(cellData -> cellData.getValue().getProcessNo());
		processNoColumn.setCellFactory(new Callback<TableColumn<schedulingTableModel,Number>, TableCell<schedulingTableModel,Number>>() {
			@Override
			public TableCell<schedulingTableModel, Number> call(TableColumn<schedulingTableModel, Number> param) {
				return new ProcessCell();
			}
		});
		arrivalTimeColumn.setCellValueFactory(cellData -> cellData.getValue().getArrivalTime());
		burstTimeColumn.setCellValueFactory(cellData -> cellData.getValue().getBurstTime());
		waitingTimeColumn.setCellValueFactory(cellData -> cellData.getValue().getWaitingTime());
		turnaroundTimeColumn.setCellValueFactory(cellData -> cellData.getValue().getTurnaroundTime());
		normalizedTimeColumn.setCellValueFactory(cellData -> cellData.getValue().getNormalizedTime());
	}
	
	/* InputField에 대한 숫자 입력으로 제한합니다 */
	@FXML
	private void initializeTextField() {
		arrivalTimeInput.textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(ObservableValue<? extends String> observable, String oldValue, 
		        String newValue) {
		        if (!newValue.matches("\\d*")) {
		            arrivalTimeInput.setText(newValue.replaceAll("[^\\d]", ""));
		        }
		    }
		});
		burstTimeInput.textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(ObservableValue<? extends String> observable, String oldValue, 
		        String newValue) {
		        if (!newValue.matches("\\d*")) {
		            arrivalTimeInput.setText(newValue.replaceAll("[^\\d]", ""));
		        }
		    }
		});
		timeQuantumInput.textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(ObservableValue<? extends String> observable, String oldValue, 
		        String newValue) {
		        if (!newValue.matches("\\d*")) {
		            arrivalTimeInput.setText(newValue.replaceAll("[^\\d]", ""));
		        }
		    }
		});
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
	
	/* Process Status에 대한 설정 */
	static class StatusCell extends ListCell<workSection>{
		@Override
		protected void updateItem(workSection item, boolean empty) {
			super.updateItem(item, empty);
			if(item != null) {
				setText("P" + item.getWorkId());
				setTextFill(Color.WHITE);
				setBackground(getBackground().fill(item.getColor()));
				setStyle("-fx-border-radius:5em;");
			}
			
		};
	}
	
	/* ProcessCell 배경 지정 */
	static class ProcessCell extends TableCell<schedulingTableModel, Number>{
		@Override
		protected void updateItem(Number item, boolean empty) {
			super.updateItem(item, empty);
			if(item != null) {
				TableRow<schedulingTableModel> tableRow = getTableRow();
				if(tableRow != null) {
					setText("P" + tableRow.getItem().getProcessNo().get());
					setTextFill(Color.WHITE);
					Color color = tableRow.getItem().getColor();
					setStyle("-fx-background-color:" + toRGBCode(color) + " !important;");
				}
			}
			
			
		};
	}
	
	public static String toRGBCode(Color color) {
		return String.format("#%02X%02X%02X", (int) (color.getRed() * 255), (int) (color.getGreen() * 255),
				(int) (color.getBlue() * 255));
	}
}
