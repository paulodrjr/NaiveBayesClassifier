
public class MatchCounter {
	private int matchCount;
	private int[] errorCount;
	
	public MatchCounter(int classCount){
		matchCount = 0;
		errorCount = new int[classCount];
	}
	
	public int getMatchCount() {
		return matchCount;
	}	
	
	public int incMatchCount(){
		matchCount++;
		return matchCount;
	}
	
	public int decMatchCount(){
		matchCount--;
		return matchCount;
	}

	public int getErrorCount(int classIndex) {
		return errorCount[classIndex];
	}	
	
	public int incErrorCount(int classIndex){
		errorCount[classIndex]++;
		return errorCount[classIndex];
	}
	
	public int decErrorCount(int classIndex){
		errorCount[classIndex]--;
		return errorCount[classIndex];
	}
	
	public int getTotalErrorCount(){
		int r = 0;
		for (int i = 0; i < errorCount.length; i++) {
			r += errorCount[i];			
		}
		return r;
	}
}
