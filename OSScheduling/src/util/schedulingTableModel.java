package util;

import java.util.Random;

import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;

/* 스케줄링 테이블 모델 클래스 */
public class schedulingTableModel {
	private static int processCount = 1;
	private final IntegerProperty processNo;
	private final IntegerProperty arrivalTime;
	private final IntegerProperty burstTime;
	private final IntegerProperty waitingTime;
	private final IntegerProperty turnaroundTime;
	private final StringProperty normalizedTime;
	private Color color;
	private boolean isEmpty;
	
	/* 초기화 생성자 */
	public schedulingTableModel() {
		this.processNo = new SimpleIntegerProperty();
		this.arrivalTime = new SimpleIntegerProperty();
		this.burstTime = new SimpleIntegerProperty();
		this.waitingTime = new SimpleIntegerProperty();
		this.turnaroundTime = new SimpleIntegerProperty();
		this.normalizedTime = new SimpleStringProperty();
		this.isEmpty = false;
		
		Random rand = new Random();
		float r = rand.nextFloat();
		float g = rand.nextFloat() / 2f;
		float b = rand.nextFloat() / 2f;
		color = new Color(r, g, b, 1.0);
	}
	
	/* 스케줄링 목록 초기화 생성 */
	public schedulingTableModel(int arrivalTime, int burstTime) {
		this();
		this.processNo.set(getProcessCount());
		this.arrivalTime.set(arrivalTime);
		this.burstTime.set(burstTime);
	}

	/* ProcessNo를 생성합니다 */
	public int getProcessCount() {
		return processCount++;
	}
	
	/* 프로세스 순번을 초기화합니다 */
	public void clearProcessCount() {
		this.processCount = 0;
	}

	/* Getter and Setter */
	public void setProcessNo(int processNo) {
		this.processNo.set(processNo);
	}
	
	public IntegerProperty getProcessNo() {
		return processNo;
	}
	
	public void setArrivalTime(int arrivalTime) {
		this.arrivalTime.set(arrivalTime);
	}

	public IntegerProperty getArrivalTime() {
		return arrivalTime;
	}
	
	public void setBurstTime(int burstTime) {
		this.burstTime.set(burstTime);
	}

	public IntegerProperty getBurstTime() {
		return burstTime;
	}

	public void setWaitingTime(int waitingTime) {
		this.waitingTime.set(waitingTime);
	}
	
	public IntegerProperty getWaitingTime() {
		return waitingTime;
	}

	public void setTurnaroundTime(int turnaroundTime) {
		this.turnaroundTime.set(turnaroundTime);
	}
	
	public IntegerProperty getTurnaroundTime() {
		return turnaroundTime;
	}
	
	public void setNormalizedTime(String normalizedTime) {
		this.normalizedTime.set(normalizedTime);
	}

	public StringProperty getNormalizedTime() {
		return normalizedTime;
	}
	
	public void setColor(Color color) {
		this.color = color;
	}
	
	public Color getColor() {
		return color;
	}
	
	public void setEmpty(boolean isEmpty) {
		this.isEmpty = isEmpty;
	}
	
	public boolean getEmpty() {
		return isEmpty;
	}

	
}
