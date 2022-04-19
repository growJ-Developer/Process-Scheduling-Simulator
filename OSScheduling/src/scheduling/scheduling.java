package scheduling;

import java.util.*;

import util.coreUtil;
import util.workSection;

public abstract class scheduling {		
	public abstract void runScheduling();										// Scheduling을 실행합니다.
	
	public abstract void setReadyQueue();										// ReadyQueue를 설정합니다.
	
	public abstract workSection getBestWork();									// 최선의 작업을 찾습니다.
	
	public abstract PriorityQueue<workSection> getReadyQueue();					// ReadyQueue를 반환합니다. 
	
	public abstract void setWorkSection(PriorityQueue<workSection> workList);	// 작업 설정
	
	public abstract PriorityQueue<workSection> getWorkSection();				// 모든 작업 반환
	
	public abstract void clear();												// 모든 진행중인 Scheduling 작업을 취소합니다.
	
	public abstract void setRunning(boolean isRunning);							// 스케쥴링 진행 여부 변경
	
	public abstract boolean isRunning();										// 스케쥴링 진행 여부 반환
	
	public abstract workSection getNowWorking();								// 현재 작업중인 작업 단위를 반환합니다.
	
	public abstract void setCoreInfo(coreUtil coreSet);							// 코어 정보 설정
	
	public abstract coreUtil getCoreInfo();										// 설정된 코어 정보 반환	
}

