package util;

public class workThread extends Thread{
	public static final int P_CORE_PERFORMANCE = 2;
	public static final int E_CORE_PERFORMANCE = 1;
	private int CORE_PERFORMANCE;
	private workSection work;
	
	/* 작업을 진행할 쓰레드를 생성 */
	public workThread(int CoreType) {
		/* 코어의 타입을 고려하여 작업 효율을 설정합니다 */
		this.CORE_PERFORMANCE = CoreType;
		this.work = null;
	}
	
	/* 일을 처리할 작업을 설정합니다 */
	public void setWork(workSection work) {
		this.work = work;
	}
	
	/* 작업을 진행합니다 */
	public synchronized void run(){
		System.out.println("!");
		if(this.work != null) {
			work.setOverWorkCnt(work.getOverWorkCnt() - CORE_PERFORMANCE);
		}
	}
}
