package scheduling;

import java.util.*;

import javafx.event.Event;
import main.OSFrameController;
import util.*;

public class DynamicRRScheduling extends scheduling{
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
	private int nowQuantum;
	
	
	/* SRTN Scheduling을 수행합니다 */
	public DynamicRRScheduling() {
		init();
	}
	
	@Override
	public void init() {
		isRunning = false;
		nowWork = null;
		workList = new PriorityQueue<>();
		readyQueue = new PriorityQueue<workSection>((o1, o2) -> {
			if(o1.getWorkIndex() > o2.getWorkIndex()) 			return 1;
			else if(o1.getWorkIndex() < o2.getWorkIndex()) 		return -1;
			else if(o1.getWorkId() > o2.getWorkId())			return 1;
			else if(o1.getWorkId() < o2.getWorkId())			return -1;
			else return 0;
		});
		endList = new LinkedList<>();
		nowTime = -1;
		timeQuantum = Integer.MAX_VALUE;
		nowQuantum = 0;
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
			
			if(nowWork != null)								nowQuantum++;
			
			/* ReadyQueue를 설정합니다  */
			setReadyQueue();
			
			/* ReadyQueue에 항목들이 있다면, 남은 시간들을 비교합니다 */
			if (nowQuantum % timeQuantum == 0 || nowWork == null) {
				nowWork = getBestWork();
				/* 3번째 아이디어 현재하는 일을 실시간 반영해서 남은일이랑 timeQuantum비교해서 증감 대신에 한계선은 존재함 */
				int leftover = nowWork.getOverWorkCnt();
				if (leftover >= this.timeQuantum && this.timeQuantum < 10)
					this.timeQuantum++;
				else if (leftover < this.timeQuantum && this.timeQuantum > 2)
					this.timeQuantum--;
				nowQuantum = 0;
			}
			
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
		/* 작업을 진행한뒤, 잔여시간을 체크하여, endList로 옮깁니다 */
		if(nowWork != null) {
			nowWork.setOverWorkCnt(nowWork.getOverWorkCnt() - coreSet.getWorkByCore());
			if(nowWork.getOverWorkCnt() <= 0) {
				endList.add(nowWork);
				nowWork = null;
			}
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
		if (nowWork != null) {
			nowWork.updateWorkIndex();
			readyQueue.add(nowWork);		// 현재 작업 반영
			return readyQueue.poll();
		} else {
			
			return readyQueue.poll();
		}
		
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
		for (int index = 0; index < workList.size(); index++) {
			workSection work = workList.poll();
			work.updateWorkIndex();
			if (work.getArrivalTime() <= nowTime) {
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
		this.timeQuantum =2;
		this.coreSet = coreSet;
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