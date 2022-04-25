package util;

import java.util.Random;

import javafx.scene.paint.Color;

public class workSection implements Comparable<workSection>{
	private int workId;				// work 고유 번호(자동 증가)
	private int arrivalTime;		// 작업 도착 시간 
	private int workCnt;			// 작업의 수
	private int overWorkCnt;		// 남은 작업의 수
	private int waitingTime;		// 대기 시간
	private double ratio;			// 시간 비
	private Color color;			// 프로세스 색상
	
	public workSection(int workId, int arrivalTime, int workCnt, Color color) {
		this.workId = workId;
		this.arrivalTime = arrivalTime;
		this.workCnt = workCnt;
		this.overWorkCnt = workCnt;
		this.color = color;
		this.waitingTime = 0;
		this.ratio = 0.0;
	}
	
	@Override
	public int compareTo(workSection o) {
		if(this.arrivalTime > o.arrivalTime) {
			return 1;
		} else if(this.arrivalTime < o.arrivalTime) {
			return -1;
		} else {
			return 0;
		}
	}
	
	@Override
	public String toString() {
		return "workId : " + workId + " arrivalTime : " + arrivalTime + " workCnt : " + workCnt + " overWornCnt : " + overWorkCnt; 
	}
	
	public int getOverWorkCnt() {
		return overWorkCnt;
	}
	public void setOverWorkCnt(int overWorkCnt) {
		this.overWorkCnt = overWorkCnt;
	}
	public int getArrivalTime() {
		return arrivalTime;
	}
	public void setArrivalTime(int arrivalTime) {
		this.arrivalTime = arrivalTime;
	}
	public int getWorkCnt() {
		return workCnt;
	}
	public void setWorkCnt(int workCnt) {
		this.workCnt = workCnt;
	}
	public int getWorkId() {
		return workId;
	}
	public Color getColor() {
		return color;
	}
	public void setColor(Color color) {
		this.color = color;
	}
	public double getRatio() {
		return ratio;
	}
	public void setRatio(double ratio) {
		this.ratio = ratio;
	}
	public int getWaitingTime() {
		return waitingTime;
	}
	public void setWaitingTime(int waitingTime) {
		this.waitingTime = waitingTime;
	}
}
