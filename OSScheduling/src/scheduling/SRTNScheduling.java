package scheduling;

import java.util.*;

import javafx.event.Event;
import main.OSFrameController;
import util.*;

public class SRTNScheduling extends scheduling{
	/* SRTN Scheduling을 수행합니다 */
	public SRTNScheduling() {
		super();
	}
	
	@Override
	public void init() {
		super.init();
		readyQueue = new PriorityQueue<workSection>((o1, o2) -> {
			if(o1.getOverWorkCnt() > o2.getOverWorkCnt()) 		return 1;
			else if(o1.getOverWorkCnt() < o2.getOverWorkCnt()) 	return -1;
			else if(o1.getWorkId() > o2.getWorkId())			return 1;
			else if(o1.getWorkId() < o2.getWorkId())			return -1;
			else return 0;
		});
	}
	
	/* 스케줄링 조건에 따라 작업을 설정합니다 */
	@Override
	public void setWorkCondition() {
		/* ReadyQueue에 항목들이 있다면, 남은 시간들을 비교합니다 */
		if(readyQueue.size() != 0) 	nowWork = getBestWork();
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
}
