package scheduling;

import java.util.*;

import javafx.event.Event;
import main.OSFrameController;
import util.*;

public class HRRNScheduling extends scheduling{
	/* HRRN Scheduling을 수행합니다 */
	public HRRNScheduling() {
		super();
	}
	
	@Override
	public void init() {
		super.init();
		readyQueue = new PriorityQueue<workSection>((o1, o2) -> {
			if(o1.getRatio() > o2.getRatio())					return -1;
			else if(o1.getRatio() < o2.getRatio())				return 1;
			else if(o1.getArrivalTime() < o2.getArrivalTime())	return 1;
			else if(o1.getArrivalTime() > o2.getArrivalTime())	return -1;
			else return 0;
		});
	}
	
	/* 스케줄링 조건에 따라 작업을 설정합니다 */
	@Override
	public void setWorkCondition() {
		/* ReadyQueue에 항목들이 있다면, 남은 시간들을 비교합니다 */
		if(nowWork == null) 	nowWork = getBestWork();
	}
	
	/* 최적의 작업을 찾습니다 */
	@Override
	public workSection getBestWork() {
		if (nowWork == null)	return readyQueue.poll();
		else					return null;
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
}
