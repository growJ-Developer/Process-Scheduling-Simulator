package scheduling;

import java.util.ArrayList;

import util.coreUtil;
import util.workSection;

public class SRTNScheduling extends scheduling{
	/* 싱글톤 패턴으로 지정합니다 */
	private static SRTNScheduling singletone = new SRTNScheduling();
	private boolean isRunning = false;
	private Thread mThread;
	private ArrayList<workSection> workList = new ArrayList<>();
	private coreUtil coreSet;
	
	/* SRTN Scheduling을 수행합니다 */
	public SRTNScheduling() {
		//super();
	}
	
	@Override
	public void runScheduling() {
		clear();
		
		mThread = new Thread(() -> {
			try {
				/* 스케쥴링 작업을 수행합니다. */
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				clear();
			}
		});
		
	}
	
	@Override
	public void setWorkSection(ArrayList<workSection> workList) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public ArrayList<workSection> getWorkSection() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public workSection getNowWorking() {
		return null;
	}
	
	@Override
	public void setRunning(boolean isRunning) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public boolean isRunning() {
		return false;
	}
	
	@Override
	public void clear() {
		// TODO Auto-generated method stub	
	}
	
	@Override
	public scheduling getInstance() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setCoreInfo(coreUtil coreSet) {
		if(!isRunning()) {
			/* 실행중이 아닐 때에만, 코어 정보를 변경합니다 */
			this.coreSet = coreSet;
		}
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public coreUtil getCoreInfo() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
