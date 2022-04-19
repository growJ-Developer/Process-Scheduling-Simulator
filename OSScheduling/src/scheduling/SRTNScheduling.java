package scheduling;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

import util.coreUtil;
import util.workSection;

public class SRTNScheduling extends scheduling{
	private boolean isRunning;
	private Thread mThread;
	private PriorityQueue<workSection> workList;
	private PriorityQueue<workSection> readyQueue;
	private ArrayList<workSection> endList;
	private coreUtil coreSet;
	private workSection nowWork;
	private int nowTime;
	
	/* SRTN Scheduling을 수행합니다 */
	public SRTNScheduling() {
		isRunning = false;
		nowWork = null;
		workList = new PriorityQueue<>();
		readyQueue = new PriorityQueue<workSection>((o1, o2) -> {
			if(o1.getOverWorkCnt() > o2.getOverWorkCnt()) 		return 1;
			else if(o1.getOverWorkCnt() < o2.getOverWorkCnt()) 	return -1;
			else if(o1.getWorkId() > o2.getWorkId())			return 1;
			else if(o1.getWorkId() < o2.getWorkId())			return -1;
			else return 0;
		});
		endList = new ArrayList<>();
		nowTime = 0;
	}
	
	@Override
	public void runScheduling() {
		clear();
		
		mThread = new Thread(() -> {
			System.out.println("SRTNScheduling Start");
			try {
				/* 스케쥴링 작업을 수행합니다. */
				rawRunScheduling();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				clear();
				System.out.println("SRTNScheduling Stop");
			}
		});
		mThread.start();
	}
	
	public void rawRunScheduling() throws Exception{		
		while(isRunning) {
			if(nowTime >= 31) 	isRunning = false;
			else 				nowTime++;
			
			/* ReadyQueue를 설정합니다 */
			setReadyQueue();
			
			/* ReadyQueue에 항목들이 있다면, 남은 시간들을 비교합니다 */
			if(readyQueue.size() != 0) 	nowWork = getBestWork();
			
			if(nowWork != null) {
				System.out.println(nowTime + " / " + nowWork);
			} else {
				System.out.println(nowTime + " / null");
			}
			
			
			/* 작업을 진행한뒤, 잔여시간을 체크하여, endList로 옮깁니다 */
			if(nowWork != null) {
				nowWork.setOverWorkCnt(nowWork.getOverWorkCnt() - coreSet.getWorkByCore());
				if(nowWork.getOverWorkCnt() <= 0) {
					endList.add(nowWork);
					nowWork = null;
				}
			}
			
			/* 1초 간격으로 실행합니다 */
			mThread.sleep(1000);
		}
	}
	
	/* 최적의 작업을 찾습니다 */
	@Override
	public workSection getBestWork() {
		if (nowWork != null)	readyQueue.add(nowWork);		// 현재 작업 반영
		return readyQueue.poll();
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
	
	@Override
	public void setWorkSection(PriorityQueue<workSection> workList) {
		this.workList = workList;
	}
	
	@Override
	public PriorityQueue<workSection> getWorkSection() {
		return this.workList;
	}
	
	@Override
	public workSection getNowWorking() {
		return null;
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
		// TODO Auto-generated method stub	
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
