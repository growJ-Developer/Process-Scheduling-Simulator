package util;

public class coreUtil {
	/* 코어에 대한 정보를 반환합니다 */
	private int pCoreCnt = 0;
	private int eCoreCnt = 0;
	
	public coreUtil(int pCoreCnt, int eCoreCnt) {
		this.pCoreCnt = pCoreCnt;
		this.eCoreCnt = eCoreCnt;
	}
	
	/* 현재 코어 설정에서의 단위장 작업량을 반환합니다 */
	public int getWorkByCore() {
		return (pCoreCnt * 2 + eCoreCnt * 1);
	}
	
	public int geteCoreCnt() {
		return eCoreCnt;
	}
	public void seteCoreCnt(int eCoreCnt) {
		this.eCoreCnt = eCoreCnt;
	}
	public int getpCoreCnt() {
		return pCoreCnt;
	}
	public void setpCoreCnt(int pCoreCnt) {
		this.pCoreCnt = pCoreCnt;
	}
}
