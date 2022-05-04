package scheduling;

import java.util.*;

import javafx.event.Event;
import main.OSFrameController;
import util.*;

public class SPNScheduling extends scheduling{
	/* SPN Scheduling을 수행합니다 */
	public SPNScheduling() {
		super();
	}
	
	@Override
	public void init() {
		super.init();
		readyQueue = new PriorityQueue<workSection>((o1, o2) -> {
			if(o1.getWorkCnt() > o2.getWorkCnt()) 				return 1;
			else if(o1.getWorkCnt() < o2.getWorkCnt()) 			return -1;
			else if(o1.getWorkId() > o2.getWorkId())			return 1;
			else if(o1.getWorkId() < o2.getWorkId())			return -1;
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
