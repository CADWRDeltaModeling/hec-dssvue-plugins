package hecdssvue.cdec.plugin;

/**
 * Keeps track of progress
 * 
 * @author psandhu
 * 
 */
public class Progress {
	/**
	 * updated from 0 -> 100%
	 */
	private int percentProgress;
	private String message;
	private int totalNumberOfTasks;
	private int numberOfTasksCompleted;

	public synchronized void setPercentProgress(int percentP) {
		if (percentP < 0) {
			percentP = 0;
		} else if (percentP > 100) {
			percentP = 100;
		}
		percentProgress = percentP;
	}
	
	public int getPercentProgress(){
		return percentProgress;
	}
	
	public void setTotalNumberOfTasks(int ntasks){
		this.totalNumberOfTasks = ntasks;
	}
	
	public int getTotalNumberOfTasks(){
		return this.totalNumberOfTasks;
	}
	
	public int getNumberOfTasksCompleted(){
		return this.numberOfTasksCompleted;
	}
	
	public void resetProgress(){
		numberOfTasksCompleted = 0;
	}

	public synchronized void setMessage(String string) {
		this.message = string;
	}
	
	public String getMessage(){
		return this.message;
	}

	public synchronized void incrementProgress() {
		numberOfTasksCompleted++;
		percentProgress = (int) Math.round(numberOfTasksCompleted*100.0/totalNumberOfTasks);
	}
}
