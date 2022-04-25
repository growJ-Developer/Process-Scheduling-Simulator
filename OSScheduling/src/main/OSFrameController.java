package main;

import java.net.*;
import java.util.*;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import scheduling.*;
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
	public static int MAX_RUN_TIME = 30;
	private Thread mThread;
	
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
	@FXML private Button toLeftBtn;								// 스케줄링 추가 
	@FXML private Button toRightBtn;							// 스케줄링 삭제 
	
	// 현재실행 정보
	@FXML private Label nowRunning;
	private static Label staticNowRunning;
	@FXML private ProgressBar progressBar;
	private static ProgressBar staticProgressBar;
	
	// Visualizer
	private static ObservableList<workSection> processStatusList = FXCollections.observableArrayList();
	@FXML private ListView<workSection> processStatus;	
	@FXML private Slider tickSlider;
	
	// 스케줄링 리스트
	private static ObservableList<schedulingTableModel> schedulingTableList = FXCollections.observableArrayList();
	@FXML private TableView<schedulingTableModel> listTable;
	@FXML private TableColumn<schedulingTableModel, Number> processNoColumn;
	@FXML private TableColumn<schedulingTableModel, Number> arrivalTimeColumn;
	@FXML private TableColumn<schedulingTableModel, Number> burstTimeColumn;
	@FXML private TableColumn<schedulingTableModel, Number> waitingTimeColumn;
	@FXML private TableColumn<schedulingTableModel, Number> turnaroundTimeColumn;
	@FXML private TableColumn<schedulingTableModel, Number> normalizedTimeColumn;
	
	// Ready Queue
	private static ObservableList<workSection> readyQueueList = FXCollections.observableArrayList();
	@FXML private ListView<workSection> readyQueue;
	
	// Gantt Chart
	@FXML private GridPane ganttPane;	
	private static GridPane staticGanttPane;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		stageDragableMoveWindow();												// 창이동 Action
		initializeListTable();													// Scheduling List Action
		initializeProcessStatus();												// Process Status(Visualizer) Action
		initalizeReadyQueue();													// Ready Queue Action
		initializeTextField();													// Set Numeric Field
		handleSchedulingSelectAction();											// Scheduling Type Action
		handleCoreSelectAction();												// Core Selection Action
		timeQuantumInput.setDisable(true);
		
		staticNowRunning = nowRunning;
		staticProgressBar = progressBar;
		staticGanttPane = ganttPane;
		
		listTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		
		closeButton.setOnAction(event -> handleCloseButtonAction(event));		// 닫기버튼 Action
		minButton.setOnAction(event -> handleMinButtonAction(event));
		
		startBtn.setOnAction(event -> handleStartBtnAction(event));				// 시작버튼 Action
		stopBtn.setOnAction(event -> handleStopBtnAction(event)); 				// 정지버튼 Action
		stopBtn.setVisible(false);
		toLeftBtn.setOnAction(event -> handleToLeftBtnAction(event));			// 추가버튼 Action
		toRightBtn.setOnAction(event -> handleToRightBtnAction(event)); 		// 삭제버튼 Action
		
		tickSlider.setMin(0);
		tickSlider.setMax(MAX_RUN_TIME + 1);
	}
	
	public static OSFrameController getInstance() {
		return singletone;
	}
	
	private void initializeProcessStatus() {
		processStatus.setOrientation(Orientation.HORIZONTAL);
		processStatus.setItems(processStatusList);
		processStatus.setCellFactory(new Callback<ListView<workSection>, ListCell<workSection>>() {
			@Override
			public ListCell<workSection> call(ListView<workSection> param) {
				return new StatusCell();
			}
		});
	}
	
	private void initalizeReadyQueue() {
		readyQueue.setOrientation(Orientation.HORIZONTAL);
		readyQueue.setItems(readyQueueList);
		readyQueue.setCellFactory(new Callback<ListView<workSection>, ListCell<workSection>>() {
			@Override
			public ListCell<workSection> call(ListView<workSection> param) {
				return new StatusCell();
			}
		});
	}
	
	/* GanttChart의 초기 설정을 진행합니다 */
	private void initializeGanttChart() {
		/* 기존 Column과 Row를 삭제합니다 */
		for(int index = 0; index < ganttPane.getRowConstraints().size(); index++) {
			ganttPane.getRowConstraints().remove(index);
		}
		for(int index = 0; index < ganttPane.getColumnConstraints().size(); index++) {
			ganttPane.getColumnConstraints().remove(index);
		}
		
		for(int index = 0; index < schedulingTableList.size(); index++) {
			/* Row를 설정합니다 */
			RowConstraints row = new RowConstraints();
			row.setPrefHeight(50);
			ganttPane.getRowConstraints().add(row);
		}
	
		/* Process 정보 Column을 설정합니다. */
		ColumnConstraints processColumn = new ColumnConstraints();
		processColumn.setPrefWidth(60);
		ganttPane.getColumnConstraints().add(processColumn);
		
		/* Column을 설정합니다 */
		for(int index = 0; index < MAX_RUN_TIME; index++) {
			ColumnConstraints col = new ColumnConstraints();
			col.setPrefWidth(60);
			ganttPane.getColumnConstraints().add(col);
		}
		
		/* GanttChart의 프로세스 정보를 설정합니다 */
		for(int index = 0; index < schedulingTableList.size(); index++) {
			schedulingTableModel model = schedulingTableList.get(index);
			Label label = new Label("P" + model.getProcessNo().get());
			label.getStyleClass().add("nowRunning");
			label.setPrefHeight(40);
			label.setPrefWidth(40);
			label.setStyle("-fx-background-color:" + toRGBCode(model.getColor()) + " !important;");
			ganttPane.add(label, 0, index);
		}
	}
	
	/* GranttChart를 설정합니다 */
	public void setGanttChart(workSection work, int timeCount) {
		Platform.runLater(new Runnable() {	
			@Override
			public void run() {
				if(work != null) {
					/* GanttChart 내 현재 index를 확인합니다 */
					int index = 0;
					for(schedulingTableModel model : schedulingTableList) {
						if(model.getProcessNo().get() == work.getWorkId()) {
							break;
						} 
						index++;
					}
					
					ProgressBar ganttProgress = new ProgressBar();
					ganttProgress.getStyleClass().add("ganttProgressBar");
					ganttProgress.setStyle("-fx-accent:" + toRGBCode(work.getColor()) + " !important");
					ganttProgress.setProgress(1.0);
					staticGanttPane.add(ganttProgress, timeCount + 1, index);
				}
			}
		});
	}
	
	/* 프로세스 Visualizer를 설정합니다 */
	public void setProcessStatus(workSection work) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {			
				/* 프로세스 진행 막대 설정 */
				processStatusList.add(work);
			}
		});
		
	}
	
	/* 현재 프로세스 정보를 설정합니다 */
	public void setNowProcessing(workSection work) {
		Platform.runLater(new Runnable() {		
			@Override
			public void run() {
				if(work != null) {
					/* 현재 프로세스를 설정 */
					staticNowRunning.setText("P" + work.getWorkId());
					staticNowRunning.setStyle("-fx-border-radius:0.5em;");
					staticNowRunning.setStyle("-fx-background-radius:0.5em;");
					staticNowRunning.setStyle("-fx-background-color:" + toRGBCode(work.getColor()) + " !important");
					
					/* 진행 상태를 설정 */
					double progressPoint = 0.0;
					if(work.getOverWorkCnt() <= 0) progressPoint = 1.0;
					else {
						progressPoint = ((double) work.getWorkCnt() - (double) work.getOverWorkCnt()) / (double) work.getWorkCnt();
					}
					staticProgressBar.setProgress(progressPoint);
				}
			}
		});	
	}
	
	/* ReadyQueue를 설정합니다 */
	public void setReadyQueueStatus(PriorityQueue<workSection> readyQueueOrigin) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				readyQueueList.clear();
				
				ArrayList<workSection> readyList = new ArrayList<>(readyQueueOrigin);
				
				for(int index = 0; index < readyList.size(); index++) {
					workSection work = readyList.get(index);
					readyQueueList.add(work);
				}
			}
		});
	}
	
	
	
	/* 시작 버튼에 대한 Action을 지정합니다 */
	private void handleStartBtnAction(ActionEvent event) {
		if(checkStartCondition()) {
			setSchedulingClass();													// 스케줄링 기법 정보 획득
			setCoreSet();										
			
			/* 스케줄러 Queue를 구성합니다 */
			schedulingList = new PriorityQueue<>();
			for(schedulingTableModel model : schedulingTableList) {
				workSection work = new workSection(model.getProcessNo().get(), model.getArrivalTime().get(), model.getBurstTime().get(), model.getColor());
				schedulingList.add(work);
			}
			
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
			
			/* Gantt Chart 설정 */
			initializeGanttChart();			// 간트 차트를 초기화합니다.
			
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
	
		String timeQuantumStr = timeQuantumInput.getText();
		/* TimeQuantum 확인 */
		if(!timeQuantumInput.isDisable() && timeQuantumStr.length() <= 0) {
			alertUtil alert = new alertUtil(AlertType.WARNING, "Add Scheduling Warning", "Please wirte the timeQuantum.");
			alert.showAlert();
			return false;
		} else if(!timeQuantumInput.isDisable() && Integer.parseInt(timeQuantumStr) <= 0) {
			alertUtil alert = new alertUtil(AlertType.WARNING, "Add Scheduling Warning", "The timeQuantum must be at least zero.");
			alert.showAlert();
			return false;
		} else if(!timeQuantumInput.isDisable()) {
			timeQuantum = Integer.parseInt(timeQuantumStr);
		}
		
		return true;
		
	}
	
	public void stopProcess() {
		
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
	
	/* 코어정보 설정을 제어합니다 */
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
			listTable.getItems().add(model);
		}
	}
	
	/* Scheduling 삭제 버튼에 대한 Action을 지정합니다 */
	private void handleToRightBtnAction(ActionEvent event) {
		ObservableList<schedulingTableModel> modelList = listTable.getSelectionModel().getSelectedItems();
		ArrayList<schedulingTableModel> rows = new ArrayList<>(modelList);
		
		Iterator<schedulingTableModel> it = rows.iterator();
		while(it.hasNext()) {
			schedulingTableModel row = it.next();
			int index = schedulingTableList.indexOf(row);
			
			/* 삭제 뒤에 색상 지우기를 위해 빈 요소 삽입후 삭제합니다 */
			schedulingTableModel blank = new schedulingTableModel();
			blank.setProcessNo(-1);
			blank.setColor(Color.WHITE);
			blank.setEmpty(true);
			schedulingTableList.add(blank);
			
			schedulingTableList.remove(index);
			
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					schedulingTableList.remove(schedulingTableList.size() - 1);
				}
			});
			
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
	private void handleMinButtonAction(ActionEvent event) {
		stage = (Stage) minButton.getScene().getWindow();
		stage.setIconified(true);
	}
	
	/* 닫기 버튼에 대한 Action을 지정합니다 */
	private void handleCloseButtonAction(ActionEvent event) {
		stage = (Stage) closeButton.getScene().getWindow();
		stage.close();
		System.exit(0);
	}
	
	private static String toRGBCode(Color color) {
		return String.format("#%02X%02X%02X", (int) (color.getRed() * 255), (int) (color.getGreen() * 255),
				(int) (color.getBlue() * 255));
	}

	/* Process Status에 대한 설정 */
	static class StatusCell extends ListCell<workSection>{
		@Override
		protected void updateItem(workSection item, boolean empty) {
			super.updateItem(item, empty);
			if(item != null) {
				if(processStatusList.size() > 1 && processStatusList.get(processStatusList.size() - 2).getWorkId() == item.getWorkId()) {
					setText("");
				} else {
					setText("P" + item.getWorkId());
				}
				
				setTextFill(Color.WHITE);
				setBackground(getBackground().fill(item.getColor()));
				setPrefHeight(30);
				setMaxHeight(30);
				setPrefWidth(30);
				setMaxWidth(30);
				setPadding(new Insets(0, 0, 0, 5));
				setStyle("-fx-font-size:8px !important;");
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
					schedulingTableModel model = tableRow.getItem();
					setText("P" + model.getProcessNo().get());
					setTextFill(Color.WHITE);
					Color color = model.getColor();
					setStyle("-fx-background-color:" + toRGBCode(color) + " !important;");
				}
			}
		};
		
		
	}
}
