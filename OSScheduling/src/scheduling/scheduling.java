package scheduling;

import java.util.*;
import javafx.event.Event;
import main.OSFrameController;
import util.*;

public class scheduling{
	boolean isRunning;								// 스케줄링 진행 여부
	int timeQuantum;								// TimeQuantum
	int nowQuantum;									// TimeQuantum 진행 시간
	int nowTime;									// 현재 시간
	double useEnergy = 0;							// 전력 소모량
	Thread mThread;									// 전용 쓰레드
	PriorityQueue<workSection> workList;			// 작업 리스트
	PriorityQueue<workSection> readyQueue;			// Ready Queue 
	Queue<workSection> endList;						// 작업이 끝난 리스트
	coreUtil coreSet;								// 코어 설정 정보
	workSection nowWork;
	
	/* 멀티 쓰레딩을 위한 PCore, ECore 쓰레드 리스트 */
	private ArrayList<workThread> pThread;			// 성능 코어 쓰레드
	private ArrayList<workThread> eThread;			// 효율 코어 쓰레드
	
	/* SRTN Scheduling을 수행합니다 */
	public scheduling() {
		init();
	}
	
	public void init() {
		isRunning = false;
		nowWork = null;
		workList = new PriorityQueue<workSection>();
		readyQueue = new PriorityQueue<workSection>();
		endList = new LinkedList<workSection>();
		nowTime = -1;
		timeQuantum = 2;
		nowQuantum = 0;
		useEnergy = 0.0;
	}
	
	public void runScheduling() {
		mThread = new Thread(() -> {
			try {
				/* 스케쥴링 작업을 수행합니다. */
				isRunning = true;
				rawRunScheduling();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				clear();
			}
		});
		mThread.start();
	}
	
	public void stopScheduling() {
		isRunning = false;
	}
	
	public void rawRunScheduling() throws Exception{
		while(isRunning) {
			if(nowTime >= OSFrameController.MAX_RUN_TIME) 	isRunning = false;
			else 											nowTime++;
			
			/* 1초 간격으로 실행합니다 */
			mThread.sleep(1000);
			
			/* ReadyQueue를 설정합니다 */
			setReadyQueue();
			
			/* 스케줄링 조건에 따라 작업을 설정합니다 */
			setWorkCondition();
			
			/* UI를 설정합니다 (반드시, 여기서 호출해야 함) */
			setUIComponent();
			
			/* 작업을 진행한뒤, 잔여시간을 체크하여, endList로 옮깁니다 */
			workAction();
			
			checkDoneProcess();
		}
	}
	
	/* 스케줄링 조건에 따라 작업을 설정합니다 */
	public void setWorkCondition() {
		/* ReadyQueue에 항목들이 있다면, 남은 시간들을 비교합니다 */
		if(nowWork == null) 	nowWork = getBestWork();
	}
	
	/* 도착한 작업들을 Queue에 넣습니다 */
	public void setReadyQueue() {
		ArrayList<workSection> tmpQueue = new ArrayList<>();
		/* Waiting Time을 반영합니다 */
		for(int index = 0; index < readyQueue.size(); index++) {
			workSection work = readyQueue.poll();
			work.setWaitingTime(work.getWaitingTime() + 1);
			
			/* Response Ratio를 계산합니다 */
			work.setRatio((work.getWaitingTime() + work.getWorkCnt()) / work.getWorkCnt());
			
			tmpQueue.add(work);
		}
		
		/* 반영한 내용을 ReadyQueue에 반영합니다 */
		for(workSection work : tmpQueue) {
			readyQueue.add(work);
		}
		
		/* workList를 이용하여 Ready Queue를 구성합니다 */
		for(int index = 0; index < workList.size(); index++) {
			workSection work = workList.poll();
			work.updateWorkIndex();
			if(work.getArrivalTime() <= nowTime) {
				readyQueue.add(work);
			} else {
				workList.add(work);
			}
		}
	}
	
	/* 최적의 작업을 찾습니다 */
	public workSection getBestWork() {
		if (nowWork == null)	return readyQueue.poll();
		else					return null;
	}
		
	/* 작업을 실행합니다 */
	public void workAction() {
		if(nowWork != null) {
			try {
				/* 작업의 양을 통해 쓰레드의 수량을 측정합니다 */
				coreSet.getUseCount(nowWork);
				
				System.out.println(coreSet.getUsePCoreCnt());
				System.out.println(coreSet.getUseECoreCnt());
				
				/* PCore 쓰레드 실행 */
				for(int index = 0; index < coreSet.getUsePCoreCnt(); index++) {
					pThread.get(index).setWork(nowWork);
					pThread.get(index).run();
					useEnergy += workThread.P_CORE_POWER;
				}
				
				/* ECore 쓰레드 실행 */
				for(int index = 0; index < coreSet.getUseECoreCnt(); index++) {
					eThread.get(index).setWork(nowWork);
					eThread.get(index).run();
					useEnergy += workThread.E_CORE_POWER;
				}
				
				/* 대기전력 측정 */
				useEnergy += coreSet.getSleepPower();
				OSFrameController.getInstance().setPowerConsumption(useEnergy);
				
				/* 작업이 끝났을 경우, endList로 옮깁니다 */
				if(nowWork.getOverWorkCnt() <= 0) {
					endList.add(nowWork);
					nowWork = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		setListTable();
	}
	
	/* EndList를 이용하여, Process Table을 설정합니다 */
	public void setListTable() {
		for(int index = 0; index < endList.size(); index++) {
			workSection work = endList.poll();
			OSFrameController.getInstance().setDoneProcess(work, nowTime);
		}
	}
	
	/* 현재 진행중인 작업을 설정합니다(Draw) */
	public void setUIComponent() {
		OSFrameController.getInstance().setProcessStatus(nowWork);
		OSFrameController.getInstance().setNowProcessing(nowWork);
		OSFrameController.getInstance().setReadyQueueStatus(readyQueue);
		OSFrameController.getInstance().setGanttChart(nowWork, nowTime);
	}
	
	
	/* 작업을 완료했는지 확인합니다 */
	public void checkDoneProcess() {
		/* ReadyQueue와 workList가 모두 비어있으면, 작업을 종료합니다 */
		if (readyQueue.size() == 0 && workList.size() == 0 && nowWork == null) {
			isRunning = false;
			OSFrameController.staticStopBtn.fire();
		}
	}
	
	/* 스케줄링 정보를 설정합니다. */
	public void setScheduling( PriorityQueue<workSection> schedulinList, int timeQuantum, coreUtil coreSet) {
		this.workList = schedulinList;
		this.timeQuantum = timeQuantum;
		this.coreSet = coreSet;
		
		/* 코어 정보에 따라 Thread를 구성합니다 */
		pThread = new ArrayList<workThread>(coreSet.getpCoreCnt());
		for(int index = 0; index < coreSet.getpCoreCnt(); index++) pThread.add(new workThread(workThread.P_CORE_PERFORMANCE));
		
		eThread = new ArrayList<workThread>(coreSet.geteCoreCnt());
		for(int index = 0; index < coreSet.geteCoreCnt(); index++) eThread.add(new workThread(workThread.E_CORE_PERFORMANCE));
	}
	
	/* 스케줄링 정보 설정(TimeQuantum 미존재) */
	public void setScheduling(PriorityQueue<workSection> schedulinList, coreUtil coreSet) {
		setScheduling(schedulinList, 2, coreSet);
	}
	
	/* Ready Queue 반환 */
	public PriorityQueue<workSection> getReadyQueue() {
		// TODO Auto-generated method stub
		return readyQueue;
	}
	
	/* 작업 설정 */
	public void setWorkSection(PriorityQueue<workSection> workList) {
		this.workList = workList;
	}
	
	/* TimeQuantum 설정 */
	public void setTimeQuantum(int timeQuantum) {
		this.setTimeQuantum(timeQuantum);
	}
	
	/* 전체 작업 반환 */
	public PriorityQueue<workSection> getWorkSection() {
		return this.workList;
	}
	
	/* 현재 작업중인 리스트 반환 */
	public workSection getNowWorking() {
		return nowWork;
	}
	
	/* 작업이 끝난 리스트 반환 */
	public Queue<workSection> getEndList() {
		return endList;
	}
	
	/* 작업 진행 설정 */
	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}
	
	/* 작업 진행 여부 반환 */
	public boolean isRunning() {
		return this.isRunning;
	}
	
	/* 초기화 -> init() 실행 */
	public void clear() {
		init();
	}
	
	/* 코어 정보 설정 */
	public void setCoreInfo(coreUtil coreSet) {
		this.coreSet = coreSet;	
	}
	
	/* 코어 정보 반환 */
	public coreUtil getCoreInfo() {
		return this.coreSet;
	}	
}
