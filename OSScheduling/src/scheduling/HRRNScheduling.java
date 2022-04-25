package scheduling;

import java.util.*;

import javafx.event.Event;
import main.OSFrameController;
import util.*;

public class HRRNScheduling extends scheduling{
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
	
	
	/* SRTN Scheduling을 수행합니다 */
	public HRRNScheduling() {
		init();
	}
	
	@Override
	public void init() {
		isRunning = false;
		nowWork = null;
		workList = new PriorityQueue<>();
		readyQueue = new PriorityQueue<workSection>((o1, o2) -> {
			if(o1.getRatio() > o2.getRatio())					return -1;
			else if(o1.getRatio() < o2.getRatio())				return 1;
			else if(o1.getArrivalTime() < o2.getArrivalTime())	return 1;
			else if(o1.getArrivalTime() > o2.getArrivalTime())	return -1;
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
			
			/* 현재 작업중인 프로세스가 없다면, 작업을 가져옵니다. */
			if(nowWork == null) 	nowWork = getBestWork();
			
			/* UI를 설정합니다 (반드시, 여기서 호출해야 함) */
			setUIComponent();
			
			/* 작업을 진행한뒤, 잔여시간을 체크하여, endList로 옮깁니다 */
			if(nowWork != null) {
				nowWork.setOverWorkCnt(nowWork.getOverWorkCnt() - coreSet.getWorkByCore());
				if(nowWork.getOverWorkCnt() <= 0) {
					endList.add(nowWork);
					nowWork = null;
				}
			}
			
			checkDoneProcess();
			
			/* 1초 간격으로 실행합니다 */
			mThread.sleep(1000);
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
		}
	}
	
	/* 도착한 작업들을 Queue에 넣습니다 */
	@Override
	public void setReadyQueue() {
		ArrayList<workSection> tmpQueue = new ArrayList<>();
		/* Waiting Time을 반영합니다 */
		for(int index = 0; index < readyQueue.size(); index++) {
			workSection work = readyQueue.poll();
			work.setWaitingTime(work.getWaitingTime() + 1);
			
			/* HRRN의 경우, Response Ratio를 계산합니다 */
			work.setRatio((work.getWaitingTime() + work.getWorkCnt()) / work.getWorkCnt());
			
			tmpQueue.add(work);
		}
		/* 반영한 내용을 ReadyQueue에 반영합니다 */
		for(workSection work : tmpQueue) {
			readyQueue.add(work);
		}
		
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
	}
	
	@Override
	public void setScheduling(PriorityQueue<workSection> schedulinList, coreUtil coreSet) {
		setScheduling(schedulinList, Integer.MAX_VALUE, coreSet);
	}
	
	@Override
	public PriorityQueue<workSection> getReadyQueue() {
		// TODO Auto-generated method stub
		return null;
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