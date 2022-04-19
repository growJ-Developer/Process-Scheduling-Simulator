package main;

import java.util.*;
import scheduling.*;
import util.*;

/* 스케줄러를 제어합니다(스케줄러 <-> 프레임간 데이터 전달 및 제어 수행) */
public class controlScheduling {
	/* 싱글톤으로 지정합니다 */
	private static controlScheduling singletone = new controlScheduling();
	private static scheduling scheduling;
	private static PriorityQueue<workSection> schedulingList;						// 스케줄링 리스트
	private static int timeQuantum;
	private coreUtil coreSet;
	
	private Thread mThread;
	private boolean isRunning;
	
	public controlScheduling() {
		isRunning = false;
		schedulingList = new PriorityQueue<workSection>();
		timeQuantum = Integer.MAX_VALUE;
	}
	
	/* 스케줄링을 시작합니다 */
	public void start() {
		mThread = new Thread(() -> {
			try {
				init();
				rawStart();
				scheduling.runScheduling();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				
			}
		});
		mThread.start();
	}
	
	/* 스케줄러에 스케줄링 정보 전달 */
	public void init() {
		scheduling.setWorkSection(schedulingList);
		scheduling.setCoreInfo(coreSet);
	}
	
	public void rawStart() throws Exception{
		isRunning = true;
		scheduling.setRunning(true);
		scheduling.runScheduling();
		
		while(isRunning) {
			/* 스케줄링 정보를 받아서 OSFrame에 반영합니다 */			
			//TODO.. Draw Screen
			
			/* 스케줄러 정지 시, 같이 정지 */
			if(!scheduling.isRunning()) isRunning = false;
			if(!isRunning)				scheduling.setRunning(false);
			
			/* 1초마다 실행하도록, Sleep을 가집니다 */
			mThread.sleep(1000);
		}
	}
	
	/* 스케줄링 정보를 초기화합니다 */
	public void clear() {
		
	}
	
	/* 스케줄링 정보를 설정합니다. */
	public void setScheduling(scheduling scheduling, PriorityQueue<workSection> schedulinList, int timeQuantum, coreUtil coreSet) {
		this.scheduling = scheduling;
		this.schedulingList = schedulinList;
		this.timeQuantum = timeQuantum;
		this.coreSet = coreSet;
	}
	
	public void setScheduling(scheduling scheduling, PriorityQueue<workSection> schedulinList, coreUtil coreSet) {
		setScheduling(scheduling, schedulinList, Integer.MAX_VALUE, coreSet);
	}
	
	public static controlScheduling getInstance() {
		return singletone;
	}
}
