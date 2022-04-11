package scheduling;

import java.util.*;

import util.coreUtil;
import util.workSection;

public abstract class scheduling {	
	private boolean isRunning = false;										// 스케쥴링 동작 여부
	private Thread mThread;													// 쓰레드
	private ArrayList<workSection> workList;								// 작업 리스트
	private coreUtil coreSet;												// 코어 정보
	private int timeQuantam;									
	
	public abstract void runScheduling();									// Scheduling을 실행합니다.
	
	public abstract void setWorkSection(ArrayList<workSection> workList);	// 작업 설정
	
	public abstract ArrayList<workSection> getWorkSection();				// 모든 작업 반환
	
	public abstract void clear();											// 모든 진행중인 Scheduling 작업을 취소합니다.
	
	public abstract void setRunning(boolean isRunning);						// 스케쥴링 진행 여부 변경
	
	public abstract boolean isRunning();									// 스케쥴링 진행 여부 반환
	
	public abstract workSection getNowWorking();							// 현재 작업중인 작업 단위를 반환합니다.
	
	public abstract scheduling getInstance();								// 현재 Scheduling Instance를 반환합니다.
	
	public abstract void setCoreInfo(coreUtil coreSet);						// 코어 정보 설정
	
	public abstract coreUtil getCoreInfo();									// 설정된 코어 정보 반환	
}

