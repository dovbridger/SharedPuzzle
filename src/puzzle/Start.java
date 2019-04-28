package puzzle;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import multi_thread.Extrapolation;
import parameters.*;

public class Start{ //Bad name - This class arranges the changing parameters and runs testing scripts for 
						//each different combination of parameters
	public static GeneratePuzzle gp;
	public static MatlabHoleFiller mhf;
	public static BackupScript backup = Global.backupScript; //Back up the state of the testing script in case it crashes
	
	public static float getSolveScore(BufferedImage inputImg ,String imgPath,int partSize, int sizeOverlap) throws Exception{
		Global.sizeOverlap = sizeOverlap;
		Global.diffBlockSize = (int)backup.parameters[Global.ParameterNames.diffBlockSizeVals.ordinal()].getValue();
//		Global.colorDiffRatio = Global.parameters[Global.ParameterNames.colorDiffRatioVals.ordinal()].getDoubleValue();
		Global.diffNorm = (int)backup.parameters[Global.ParameterNames.diffNormVals.ordinal()].getValue();
		Global.lFactor = (double)backup.parameters[Global.ParameterNames.lFactorVals.ordinal()].getValue();
//		Global.SALIENCY_FACTOR = Global.parameters[Global.ParameterNames.saliencyFactorVals.ordinal()].getDoubleValue();
		Global.modifyConf = (int)(backup.parameters[Global.ParameterNames.modifyConfVals.ordinal()].getValue()) != 0;
		Global.useImportance = (int)(backup.parameters[Global.ParameterNames.useImportanceVals.ordinal()].getValue()) != 0 && Global.patchSize>0;
		Global.diff_matrix_correction_method = (String)backup.parameters[Global.ParameterNames.diff_matrix_correction_methods.ordinal()].getValue();
		//Generate the puzzle (written mostly by Genadi)
		gp=new GeneratePuzzle(inputImg,imgPath+Global.FILE_TYPE,imgPath+"_shuffled.png", partSize,false,0);
		System.out.println("PuzzleID: " + Global.puzzleID);
		if (!Global.no_matlab){
			mhf.setProxy();
		}
		try {
			//Slove the puzzle
			new PuzzleSolver(gp.getGenIm(),partSize,imgPath,true,false,gp.getBorderNum(),mhf);
		} catch (Exception e) {
			Global.neighborResult = -1;
			Global.directResult = -1;
			throw e;
		}
		System.out.println("------------------------------------------------");
		if (Global.NEIGHBOR_SCORE_METRIC){
			return Global.neighborResult;
		}else{
			return Global.directResult;
		}
	}
	
	public static void testingScript() throws Exception{
		//Maximum number of solve results to be stored in the score file
		final int MAX_NUM_RESULTS = 300;
		String[]images = Global.puzzleNamesRaw;
		if (!Global.no_matlab){
			mhf = new MatlabHoleFiller();
		}
		//Loop over each puzzle and solve it according to the parameters
		for (int imagesIndex = backup.recoverImageNum; imagesIndex < images.length; imagesIndex++){ 		
			backup.recoverImageNum = imagesIndex;
			Global.puzzleName = images[imagesIndex] + Global.PUZZLE_NAME_SUFFIX;
			Global.createDirectory(Global.SCORES);
			String resultsFileName = Global.puzzleName +"_test_results.txt";
			//Run the solving tests
			runTestsRecursively(0,imagesIndex);
			printScores(backup.scores, MAX_NUM_RESULTS,Global.pathToOutput+resultsFileName,null);
			backup.scores = new TreeMap<Float,ArrayList<String[]>>();
		}
		printScoreStats(Global.SCORES+"stats_"+Global.scoresFileName);
		printScores(backup.allScores, MAX_NUM_RESULTS, Global.SCORES+Global.scoresFileName, images);
		if (!Global.no_matlab){
			mhf.disconnect();
		}
	}
	
	public static void runTestsRecursively(int activeParameter,int imagesIndex) throws Exception{
		//If this is the last parameter (the leaf of the recursion)
		if (activeParameter == Global.ParameterNames.size.ordinal()){
			//Take a snapshot and save the scores to the backup file
			backup.saveObject("backup_script");
			//Load expand the "ExtrapolationVals" parameter to each of its components
			int[]currentExtrapolationVals =(int[]) backup.parameters[Global.ParameterNames.extrapolationVals.ordinal()].getValue();
			Global.patchSize = currentExtrapolationVals[0];
			Global.numIterations = currentExtrapolationVals[1];
			Global.burnExtent = currentExtrapolationVals[2];
			Global.sizeOverlap = currentExtrapolationVals[3];
			Global.patchRatio = currentExtrapolationVals[4];
			//Calculate how many pixels the part will be extended by after the extrapolation ("extrapolation extent")
			int extrapolationExtent = (int)(Global.patchSize*Math.pow(2, Global.numIterations-1))-Global.burnExtent;
			// If this test's parameters require an overlap that is not provided by "extrapolation extent" skip the test
			// ignore this condition if patchsize is 0 because that means that there is no extrapolation anyway
			if (extrapolationExtent<Global.sizeOverlap && Global.patchSize>0){
				return;
			}
			//Adjust "partSize" to be the size of the part subtracting the burnt pixels and adding the extrapolated ones
			int partSize = Global.ORIGINAL_PART_SIZE + 2*Global.sizeOverlap;
			if (Global.patchSize == 0){ //This is for a case where there is only burning and no extrapolation
				partSize = partSize - 2*Global.burnExtent;				
			}
			//Create all the directories needed for accessing the data of this specific puzzle
			Global.prepareDirectories();
			
			BufferedImage preparedImg = null;
			if (Global.siftMode){
				//Search for a sift completed image
				try {
					preparedImg = ImageIO.read(new File(Global.pathToPreparedInput+Global.preparedName+Global.SIFT_MODE_SUFFIX+".png"));
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("No Sift Image available, turning off siftMode");
					Global.siftMode = false;
					preparedImg = getOrCreatePreparedImage(Global.preparedName ,Global.patchSize,Global.numIterations,Global.sizeOverlap,Global.patchRatio,Global.burnExtent);
				}
			}else{
				//Search for an image with the current extrapolation parameters
				//If it doesn't exist - it will be created using "Extrapolation" module
				preparedImg = getOrCreatePreparedImage(Global.preparedName ,Global.patchSize,Global.numIterations,Global.sizeOverlap,Global.patchRatio,Global.burnExtent);
			}
			float score = getSolveScore(preparedImg,Global.preparedName ,partSize, Global.sizeOverlap);
			if (score != -1){
				String[] newEntry = new String[backup.parameters.length+1];
				newEntry[0] = ""+imagesIndex;
				for (int i=1; i<newEntry.length;i++){
					newEntry[i]=backup.parameters[i-1].getStringValue();
				}
				//Add the score along with the list of parameters ("new Entry") to the current puzzle image's tree
				//and to the tree of all the scores
				putWithDuplicates(backup.scores,score,newEntry);
				putWithDuplicates(backup.allScores,score,newEntry);
				for (int i=0; i<backup.parameters.length;i++){
					backup.parameters[i].updateSum(score);
				}
			}
		}
		else{
			//The next for loop is backbone of the recursion mechanism here.
			//Each time the father parameter in the recursion tree iterates to the next value,
			//the child parameter starts its loop all over again. If the testing script is running in the backup mode
			//it means that each parameter starts at some intermediate iteration ("recoverIndex").
			//We want the "for" loop to start at that index only on the first time that it runs and that when the
			//father parameter changes value, the child paramter will start over from index 0.
			int start = backup.parameters[activeParameter].recoverIndex;	//Could be nonzero only for the first time (The recovery point)
			for (int i = start; i<backup.parameters[activeParameter].numValues; i++){
				backup.parameters[activeParameter].recoverIndex = 0;	//After recovery, start from 0 every time
				backup.parameters[activeParameter].index = i;
				runTestsRecursively(activeParameter+1,imagesIndex);
			}
		}
	}
	//This methods adds a (key,value) = (score, parameters) to the TreeMap holding the scores
	//while not overriding entries that have identical scores (keys). it adds them to that same key
	public static void putWithDuplicates(TreeMap<Float,ArrayList<String[]>> treeMap, float key, String[]newEntry){
		ArrayList<String[]> oldEntries = treeMap.get(key);
		if (oldEntries != null){
			oldEntries.add(newEntry);
		}else{
			ArrayList<String[]>newEntriesList = new ArrayList<String[]>();
			newEntriesList.add(newEntry);
			treeMap.put(new Float(key), newEntriesList);
		}
	}
	
	public static BufferedImage getOrCreatePreparedImage(String prepared,int patchSize, int numIterations, int sizeOverlap, double patchRatio, int burnExtent){

		Extrapolation.numIterations = numIterations;
		Extrapolation.patchSize = patchSize;
		Extrapolation.SOURCE_TARGET_PATCH_RATIO = patchRatio/100.f;
		BufferedImage preparedImg = null;
		try {

			preparedImg = ImageIO.read(new File(Global.pathToPreparedInput+prepared+Global.FILE_TYPE));
		} catch (IOException e) {
			try {
				Extrapolation.prepareImageForSolving(Global.puzzleName,prepared,burnExtent,100,Global.ORIGINAL_PART_SIZE,sizeOverlap,true,Global.savePartImages);
				preparedImg = ImageIO.read(new File(Global.pathToPreparedInput+prepared+Global.FILE_TYPE));
			} catch (IOException e1) {}
		}
		return preparedImg;
	}
	
	public static void printScores(TreeMap<Float,ArrayList<String[]>> solveScores, int MAX_NUM_RESULTS, String RESULTS_FILE_NAME, String[]images) throws FileNotFoundException, UnsupportedEncodingException{
		PrintWriter writer = new PrintWriter(RESULTS_FILE_NAME,"UTF-8");
		for (int i=0; i<MAX_NUM_RESULTS && solveScores.size() > 0; i++){
			Float score = solveScores.lastKey();
			ArrayList<String[]> valuesList = solveScores.get(score);
			while (!valuesList.isEmpty()){
				String[]values = valuesList.remove(valuesList.size()-1);
				if (images != null){
					writer.write("name: "+images[Integer.valueOf(values[0]).intValue()]+Global.PUZZLE_NAME_SUFFIX+" - ");
				}
				for (int valIndex=1; valIndex<values.length; valIndex++){
					if (valIndex>1){
						writer.write(", ");
					}
					writer.print(backup.parameters[valIndex-1].name+"=");
					writer.print(values[valIndex]);
				}
				writer.println(" : score = "+ (int)(100*score));
				writer.println();
			}
			solveScores.remove(score);
		}
		writer.close();
	}

	public static void printScoreStats(String RESULTS_FILE_NAME) throws FileNotFoundException, UnsupportedEncodingException{
		PrintWriter writer = new PrintWriter(RESULTS_FILE_NAME,"UTF-8");
		for (Global.ParameterNames paramName:Global.ParameterNames.values()){
			if (paramName != Global.ParameterNames.size){
				writer.println(paramName+":");
				Parameter param = backup.parameters[paramName.ordinal()];
				for (int i=0; i<param.numValues; i++){
					param.index = i;
					writer.println(param.getStringValue()+": " + param.getAverageScore(i));
				}
				writer.println();
			}
		}
		writer.close();
	}

}
