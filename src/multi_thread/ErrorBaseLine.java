package multi_thread;

import puzzle.Global;
import puzzle.PuzzleSolver;

public class ErrorBaseLine extends LoopWorker {
	
	public static PuzzleSolver ps;
	private static double totalErrorSum;
	private static final Object totalErrorSumLock = new Object();
	private static int burnExtent = 0; //The pixels which should not be counted in the error calculation
	private static String puzzleName ="";
	private static double[] errors;
	
	
	private static boolean isUpdated(int _burnExtent, String _puzzleName){
		return(burnExtent == _burnExtent && _puzzleName.equals(puzzleName));
	}
	
	public static void createAndRunWorkers(int _burnExtent, String _puzzleName, PuzzleSolver _ps){
		if (isUpdated(_burnExtent,_puzzleName)){
			return;
		}

		burnExtent = _burnExtent;
		puzzleName = _puzzleName;
		ps = _ps;
		init(ps.partsNum);
		errors = new double[ps.partsNum];
		totalErrorSum = 0;
		ErrorBaseLine[] threads = new ErrorBaseLine[numThreads];
		for (int t=0; t<numThreads; t++){
			threads[t] = new ErrorBaseLine();
			threads[t].start();
		}
		for (int t=0; t<numThreads; t++){
			try {threads[t].join();} catch (InterruptedException e) {}
		}
	}
	
	@Override
	public void doWork(int part1) {
		double errorSum = 0;
		double currentError;
		for (int part2=0; part2<ps.partsNum; part2++){
			if (part1==part2){continue;};
			currentError = getRMSE(ps.puzzleParts.puzzlePartsLAB[part1].imgArray,ps.puzzleParts.puzzlePartsLAB[part2].imgArray,burnExtent);
			errorSum += currentError;
		}
		synchronized(totalErrorSumLock){
			errors[part1] = errorSum;
			totalErrorSum += errorSum;
		}
	}
	
	public static double getPartialCorrectness(int part1, int part2, double errorBaseline){
		double rmse = getRMSE(ps.puzzleParts.puzzlePartsLAB[part1].imgArray,ps.puzzleParts.puzzlePartsLAB[part2].imgArray,Global.sizeOverlap+Global.burnExtent);
		double normalizedError = Math.min(1, rmse/errorBaseline);
		return 1-normalizedError;
	}
	
	public static double getErrorBaseLine(){
		double averageError = totalErrorSum/(ps.partsNum*(ps.partsNum-1));
		return averageError;
	}
	
	public static double getRMSE(double[][][]p1, double[][][]p2,int burnExtent){
		double errorSum = 0;
		double singleError;
		int counter = 0;
		for (int i=0; i<p1.length; i++){
			for (int j=0; j<p1[0].length; j++){
				if (i<burnExtent || j<burnExtent || i>=p1.length-burnExtent || j>=p1[0].length-burnExtent){continue;}
				singleError = 0;
				for (int c=0; c<p1[0][0].length; c++){
					singleError += Math.pow(p1[i][j][c] - p2[i][j][c], 2);
				}
				errorSum += singleError;
				counter++;
			}
		}
		double result = errorSum/counter;
		return Math.sqrt(result);
	}
}
