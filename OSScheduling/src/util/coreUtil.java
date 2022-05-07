package util;

public class coreUtil {
	/* 코어에 대한 정보를 반환합니다 */
	private int pCoreCnt = 0;
	private int eCoreCnt = 0;
	
	private int usePCoreCnt = 0;
	private int useECoreCnt = 0;
	private int overWorkCnt = 0;
	
	public coreUtil(int pCoreCnt, int eCoreCnt) {
		this.pCoreCnt = pCoreCnt;
		this.eCoreCnt = eCoreCnt;
	}
	
	/* 현재 코어 설정에서의 단위장 작업량을 반환합니다 */
	public int getWorkByCore() {
		return (pCoreCnt * workThread.P_CORE_PERFORMANCE + eCoreCnt * workThread.E_CORE_PERFORMANCE);
	}
	
	/* 현재 코어 설정으로 작업할 수 있는 작업량을 설정합니다 */
	public void getUseCount(workSection nowWork) {
		overWorkCnt = nowWork.getOverWorkCnt();
		
		/* P 코어의 개수를 측정합니다 */
		for(int index = 1; index <= pCoreCnt; index++) {
			if(overWorkCnt >= 0) {
				overWorkCnt -= workThread.P_CORE_PERFORMANCE;
				usePCoreCnt = index;
			} else {
				break;
			}
		}
		
		/* E 코어의 개수를 측정합니다 */
		for(int index = 1; index <= eCoreCnt; index++) {
			if((overWorkCnt - workThread.E_CORE_PERFORMANCE) >= 0) {
				overWorkCnt -= workThread.E_CORE_PERFORMANCE;
				useECoreCnt = index;
			} else {
				break;
			}
		}
	}
	
	/* 대기 전력을 계산하여 반환합니다 */
	public double getSleepPower() {
		return (pCoreCnt - usePCoreCnt) * 0.1 + (eCoreCnt - useECoreCnt) * 0.1;
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
	
	public int getUseECoreCnt() {
		return useECoreCnt;
	}
	public int getUsePCoreCnt() {
		return usePCoreCnt;
	}
}
