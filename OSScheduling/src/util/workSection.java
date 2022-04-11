package util;

public class workSection {
	private static int workId = 0;	// work 고유 번호(자동 증가)
	private int workCnt;			// 작업의 수
	private int overWorkCnt;		// 남은 작업의 수
	
	public workSection(int workCnt) {
		this.workId++;
		this.workCnt = workCnt;
		this.overWorkCnt = workCnt;
	}
	
	public int getOverWorkCnt() {
		return overWorkCnt;
	}
	public void setOverWorkCnt(int overWorkCnt) {
		this.overWorkCnt = overWorkCnt;
	}
	public int getWorkCnt() {
		return workCnt;
	}
	public void setWorkCnt(int workCnt) {
		this.workCnt = workCnt;
	}
	public static int getWorkId() {
		return workId;
	}
}
