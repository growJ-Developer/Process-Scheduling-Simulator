package scheduling;

import java.util.*;

import javafx.event.Event;
import main.OSFrameController;
import util.*;

public class DynamicRRScheduling extends scheduling{
	/* SRTN Scheduling을 수행합니다 */
	public DynamicRRScheduling() {
		super();
	}
	
	@Override
	public void init() {
		super.init();
		readyQueue = new PriorityQueue<workSection>((o1, o2) -> {
			if(o1.getWorkIndex() > o2.getWorkIndex()) 			return 1;
			else if(o1.getWorkIndex() < o2.getWorkIndex()) 		return -1;
			else if(o1.getWorkId() > o2.getWorkId())			return 1;
			else if(o1.getWorkId() < o2.getWorkId())			return -1;
			else return 0;
		});
	}
	
	/* 스케줄링 조건에 따라 작업을 설정합니다 */
	@Override
	public void setWorkCondition() {
		nowQuantum++;
		/* ReadyQueue에 항목들이 있다면, 남은 시간들을 비교합니다 */
		if (nowQuantum % timeQuantum == 0 || nowWork == null) {
			nowWork = getBestWork();
			/* 3번째 아이디어 현재하는 일을 실시간 반영해서 남은일이랑 timeQuantum비교해서 증감 대신에 한계선은 존재함 */
			int leftover = nowWork.getOverWorkCnt();
			if (leftover >= timeQuantum && timeQuantum < 10)
				timeQuantum++;
			else if (leftover < timeQuantum && timeQuantum > 2)
				timeQuantum--;
			nowQuantum = 0;
		}
	}
	
	/* 최적의 작업을 찾습니다 */
	@Override
	public workSection getBestWork() {
		if (nowWork != null) {
			nowWork.updateWorkIndex();
			readyQueue.add(nowWork); // 현재 작업 반영
			return readyQueue.poll();
		} else {
			return readyQueue.poll();
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
}