package scheduling;

import java.util.*;

import javafx.event.Event;
import main.OSFrameController;
import util.*;

public class SPNScheduling extends scheduling{
	private boolean isRunning;
	private Thread mThread;
	private int timeQuantum;
	private static scheduling scheduling;
	private PriorityQueue<workSection> workList;
	private PriorityQueue<workSection> readyQueue;
	private Queue<workSection> endList;
	private coreUtil coreSet;
	private workSection nowWork;
	private int nowTime;
	
	private ArrayList<workThread> pThread;			// 성능 코어 쓰레드
	private ArrayList<workThread> eThread;			// 효율 코어 쓰레드
	
	/* SRTN Scheduling을 수행합니다 */
	public SPNScheduling() {
		init();
	}
	
	@Override
	public void init() {
		isRunning = false;
		nowWork = null;
		workList = new PriorityQueue<>();
		readyQueue = new PriorityQueue<workSection>((o1, o2) -> {
			if(o1.getWorkCnt() > o2.getWorkCnt()) 				return 1;
			else if(o1.getWorkCnt() < o2.getWorkCnt()) 			return -1;
			else if(o1.getWorkId() > o2.getWorkId())			return 1;
			else if(o1.getWorkId() < o2.getWorkId())			return -1;
			else return 0;
		});
		endList = new LinkedList<>();
		nowTime = -1;
		timeQuantum = Integer.MAX_VALUE;
	}
	
	@Override
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
	
	@Override
	public void stopScheduling() {
		isRunning = false;
	}
	
	@Override
	public void rawRunScheduling() throws Exception{
		while(isRunning) {
			if(nowTime >= OSFrameController.MAX_RUN_TIME) 	isRunning = false;
			else 											nowTime++;
			
			/* ReadyQueue를 설정합니다 */
			setReadyQueue();
			
			/* ReadyQueue에 항목들이 있다면, 남은 시간들을 비교합니다 */
			if(nowWork == null) 	nowWork = getBestWork();
			
			/* UI를 설정합니다 (반드시, 여기서 호출해야 함) */
			setUIComponent();
			
			/* 작업을 진행한뒤, 잔여시간을 체크하여, endList로 옮깁니다 */
			workAction();
			
			checkDoneProcess();
			
			/* 1초 간격으로 실행합니다 */
			mThread.sleep(1000);
		}
	}
	
	/* 작업을 실행합니다 */
	@Override
	public void workAction() {
		try {
			/* 작업 진행을 쓰레드를 이용하여 처리합니다 */
			for(workThread pWorker : pThread) {
				pWorker.setWork(nowWork);
				pWorker.run();	
			}
			for(workThread eWorker : eThread) {
				eWorker.setWork(nowWork);
				eWorker.run();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(nowWork.getOverWorkCnt() <= 0) {
			endList.add(nowWork);
			nowWork = null;
		}
		
		setListTable();
	}
	
	/* EndList를 이용하여, Process Table을 설정합니다 */
	@Override
	public void setListTable() {
		for(int index = 0; index < endList.size(); index++) {
			workSection work = endList.poll();
			OSFrameController.getInstance().setDoneProcess(work, nowTime);
		}
	}
	
	/* 현재 진행중인 작업을 설정합니다(Draw) */
	@Override
	public void setUIComponent() {
		OSFrameController.getInstance().setProcessStatus(nowWork);
		OSFrameController.getInstance().setNowProcessing(nowWork);
		OSFrameController.getInstance().setReadyQueueStatus(readyQueue);
		OSFrameController.getInstance().setGanttChart(nowWork, nowTime);
	}
	
	/* 최적의 작업을 찾습니다 */
	@Override
	public workSection getBestWork() {
		if (nowWork == null)	return readyQueue.poll();
		else					return null;
	}
	
	/* 작업을 완료했는지 확인합니다 */
	@Override
	public void checkDoneProcess() {
		/* ReadyQueue와 workList가 모두 비어있으면, 작업을 종료합니다 */
		if (readyQueue.size() == 0 && workList.size() == 0 && nowWork == null) {
			isRunning = false;
			OSFrameController.staticStopBtn.fire();
		}
	}
	
	/* 도착한 작업들을 Queue에 넣습니다 */
	@Override
	public void setReadyQueue() {
		for(int index = 0; index < workList.size(); index++) {
			workSection work = workList.poll();
			if(work.getArrivalTime() <= nowTime) {
				readyQueue.add(work);
			} else {
				workList.add(work);
			}
		}
	}
	
	/* 스케줄링 정보를 설정합니다. */
	@Override
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
	
	@Override
	public void setScheduling(PriorityQueue<workSection> schedulinList, coreUtil coreSet) {
		setScheduling(schedulinList, Integer.MAX_VALUE, coreSet);
	}
	
	@Override
	public PriorityQueue<workSection> getReadyQueue() {
		// TODO Auto-generated method stub
		return readyQueue;
	}
	
	@Override
	public void setWorkSection(PriorityQueue<workSection> workList) {
		this.workList = workList;
	}
	
	@Override
	public void setTimeQuantum(int timeQuantum) {
		this.setTimeQuantum(timeQuantum);
	}
	
	@Override
	public PriorityQueue<workSection> getWorkSection() {
		return this.workList;
	}
	
	@Override
	public workSection getNowWorking() {
		return nowWork;
	}
	
	@Override
	public Queue<workSection> getEndList() {
		return endList;
	}
	
	@Override
	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}
	
	@Override
	public boolean isRunning() {
		return this.isRunning;
	}
	
	@Override
	public void clear() {
		init();
	}
	
	@Override
	public void setCoreInfo(coreUtil coreSet) {
		this.coreSet = coreSet;	
	}
	
	@Override
	public coreUtil getCoreInfo() {
		return this.coreSet;
	}
	
}
