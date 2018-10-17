package multi_thread;

public abstract class LoopWorker extends Thread {

	private static int numThreadsDefault = 16;
	private static int chunkSizeDefault = 5;
	
	protected static int numThreads; 
	private static int chunkSize;
	private static Integer currentWork;
	private static int totalWork;

	
	//Thread fields
	protected int name;
	private int workStart;
	private int workEnd;
	
	public LoopWorker(){}
	
	public static void init(int _totalWork, int _numThreads, int _chunkSize){
		totalWork = _totalWork;
		numThreads = _numThreads;
		chunkSize = _chunkSize;
		currentWork = new Integer(0);
	}
	
	public static void init(int _totalWork){
		totalWork = _totalWork;
		numThreads = numThreadsDefault;
		chunkSize = chunkSizeDefault;
		currentWork = new Integer(0);
	}
	
	@Override public void run(){

		boolean moreWork = getWork();	
		while (moreWork){
			for (int work = workStart; work<=workEnd; work++){
				doWork(work);
			}
			moreWork = getWork();
		}
	}
	public void doWork(int work){}
	
	public boolean getWork(){
		synchronized(currentWork){
			if (currentWork.intValue() >= totalWork){
				return false;
			}else{
				workStart = currentWork.intValue();
				workEnd = Math.min(totalWork-1,workStart+chunkSize);
				currentWork = workEnd+1;
				if (currentWork.intValue() + numThreads >= totalWork){
					chunkSize = 1;
				}			
				return true;
			}
		}
	}
}
