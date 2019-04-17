//In this version full block option can be selected for finding the best patch as of the 2nd iteration.
//Last version before making sure that the patch is compatible with the neighboring patch at the destination.
package multi_thread;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;

import puzzle.Global;
import puzzle.Orientation;
import puzzle.Pixel;


public class Extrapolation extends LoopWorker {
	
	//For single part demo
	private static int partNum;
	private static int patchCounter = 0;
	private static int[][]patchMatchData; //[][0-startX, 1-startY, 2-level, 3-part, 4-startX, 5-startY
	private static boolean runningDemo = false;
	
	public static int patchSize = 5;
	public static int numIterations = 1;
	public static double SOURCE_TARGET_PATCH_RATIO = 0.99;
	public static boolean noCorners = false;
	
	private static int burnExtent;
	private static int numLevels = 4;
	private static double resizeFactor = 2;
	private static double resizeFactorDatabase = 2;
	private static int iteration = 0;
	private static int xParts;
	private static int yParts;
	private static int partsNum;
	private static boolean overlap = true;
	private static boolean firstIteration = true;
	private static boolean useFullBlock = true;


	
	private static final int NUM_PATCHES_TO_SAVE = 5;
	private static final double PATCHES_TO_SAVE_FOCAL = 1.1; //For compatibility with destination
	private static final int PATCH_FINDER_SAMPLE_STRIDE = 4;
	private static final int MAX_TREE_SIZE = 500;
	private static final int MIN_TREE_REFRESH_COUNT = 1000;
	private static final double TREE_FOCAL = 1.1; //for the sample strides
	private static final double INF = 999999999;
	private static final double EPS = 0.001;
	
	private static boolean debugBool = false;
	private static double LastFirstPatchRatioSum = 0;
	private static Object sync1 = new Object();
	private static Object sync2 = new Object();
	private  static int numTrees = 0;
	private static int numPatchesPlaced = 0;
	private static double treeSizeSum = 0;
	
	
	private static Pixel[][][]parts;
	private static Pixel[][][][]imagePyramid;
	private static Pixel[][][][]database;
	private static Pixel[][][][]extrapolations;
	
	public Extrapolation(int _name){
		name = _name;
	}
	
	public static void createAndRunWorkers(){
		init(partsNum);
		if (runningDemo){
			numThreads = 1;
		}
		Extrapolation[] threads = new Extrapolation[numThreads];
		for (int t=0; t<numThreads; t++){
			threads[t] = new Extrapolation(t);
			threads[t].start();
		}
		for (int t=0; t<numThreads; t++){
			try {threads[t].join();} catch (InterruptedException e) {}
		}
	}
	

	public static Pixel[][][][] extrapolateParts(){
		int levelOffset = numLevels - numIterations;
		imagePyramid = createImagePyramid(parts,resizeFactor);
		extrapolations = new Pixel[4][partsNum][][];
		for (iteration=0; iteration<numIterations; iteration++){
			//database = getFullPyramidLayers(imagePyramid,extrapolations,iteration+levelOffset);
			createAndRunWorkers();
			if(useFullBlock){
				firstIteration = false;
			}
			System.out.println("average number of checked sample regions = "+treeSizeSum/numTrees);
			System.out.println("last/first key ratio = "+LastFirstPatchRatioSum/numPatchesPlaced);
			LastFirstPatchRatioSum = 0;
			numTrees=0;
			numPatchesPlaced=0;
			treeSizeSum=0;
		}
		return extrapolations;
	}
	
	public void doWork(int part) {
		int levelOffset = numLevels - numIterations;
		Pixel[][]extendedPart = Pixel.copy(imagePyramid[iteration+levelOffset][part]);
		System.out.println("Thread"+name+" , iteration"+iteration+" , part"+part);
		for (Orientation or:Orientation.values()){
			if (extrapolations[or.ordinal()][part] !=null){
				extrapolations[or.ordinal()][part] = resize(extrapolations[or.ordinal()][part],resizeFactor);
			}
			extrapolations[or.ordinal()][part] = extrapolatePart(extendedPart,extrapolations[or.ordinal()][part],database,or);			
			extendedPart = combine(extendedPart,extrapolations[or.ordinal()][part],or);
		}
	}
	
	public static Pixel[][] extrapolatePart(Pixel[][]originalPart,Pixel[][]originalExtrapolation, Pixel[][][][]database, Orientation or){	
		int rowStart = 0;//This is where the row and col of the blocks to be extrapolated begin
		int colStart = 0;//For some reason this is always zero, I don't know what I tried to do here
		int height = originalPart.length;
		int width = originalPart[0].length;
		
		int startX = 0; //start of the whole extrapolation strip
		int startY = 0;
		int endX = width; //end of the whole extrapolation strip
		int endY = height;
		
		int blockSizeX = patchSize;
		int blockSizeY = patchSize;
		int rowLimit = 0;					
		int colLimit = 0;
		int startCopyX = 0; //start of location in the extrapolated area to be overridden by this iteration
		int startCopyY = 0;
		
	

		switch(or){
		case UP:
			blockSizeX = 2*patchSize;
			colLimit = width-blockSizeX;	
			endY = patchSize;
			if (originalExtrapolation!=null){
				startCopyY = originalExtrapolation.length-patchSize;
			}
			break;
		case DOWN:
			blockSizeX = 2*patchSize;
			colLimit = width-blockSizeX;
			startY = height-patchSize;
			break;
		case LEFT:
			blockSizeY = 2*patchSize;
			if (runningDemo){
				rowLimit = height-blockSizeY;
			}else{
				rowLimit = height-blockSizeY;//-patchSize; //Really risky change to test not extrapolating corners, used to be height-blockSizeY
				//rowStart = patchSize; //for avoiding corners, didn't used to be here
			}
			endX = patchSize;
			if (originalExtrapolation!=null){
				startCopyX = originalExtrapolation[0].length - patchSize;
			}
			break;
		case RIGHT:
			blockSizeY = 2*patchSize;
			if (runningDemo){
				rowLimit = height-blockSizeY;
			}else{
				rowLimit = height-blockSizeY;//-patchSize; //Really risky change to test not extrapolating corners, used to be height-blockSizeY
				//rowStart = patchSize; //for avoiding corners, didn't used to be here
			}
			startX = width - patchSize;
			break;
		}
		
		if (!firstIteration){
			int extrapolationStartX = 0;
			int extrapolationStartY = 0;
			int extrapolationWidth = width;
			int extrapolationHeight = height;	
			blockSizeX = 2*patchSize;
			blockSizeY = 2*patchSize;
			switch(or){
			case UP:
		//		rowStart = patchSize;
				extrapolationHeight = patchSize;
				extrapolationStartY = originalExtrapolation.length - patchSize;
				break;
			case DOWN:
		//		rowStart = -patchSize;
				extrapolationHeight = patchSize;
				break;
			case LEFT:
				extrapolationWidth = patchSize;
		//		colStart  = patchSize;
				extrapolationStartX = originalExtrapolation[0].length - patchSize;
				break;
			case RIGHT:
				extrapolationWidth = patchSize;
		//		colStart = -patchSize;
				break; 
			}
			originalPart = combine(originalPart,getShiftedBlock(originalExtrapolation,extrapolationWidth,extrapolationHeight,extrapolationStartX,extrapolationStartY),or);
		}
		

		Pixel[][] newExtrapolation = new Pixel[endY-startY][endX-startX];
		Pixel.initWithZeros(newExtrapolation);
		double[][] weightArr = new double[endY-startY][endX-startX];
		int incRow = (overlap?1:blockSizeY);
		int incCol = (overlap?1:blockSizeX);
		Pixel[][]patch = null; //This will hold the best patch until the next iteration
		double[] patchScore = new double[1];
		
		for (int row=rowStart; row<=rowLimit; row=row+incRow){ //Only one of these 2 loops will loop	//rowStart is for avoiding the corners;
			if (!overlap && row>0 && row<rowLimit && row+blockSizeY>rowLimit){
				row = rowLimit-blockSizeY;
			}	
			for (int col=0; col<=colLimit; col=col+incCol){//Only one of these 2 loops will loop	
				if (!overlap && col>0 && col<colLimit && col+blockSizeX>colLimit){
					col = colLimit-blockSizeX;
				}
				
								
				patch = findBestPatch(originalPart,col+startX,row+startY,col+startX+blockSizeX,row+startY+blockSizeY,database,or,patch,patchScore);
				if (overlap){
					//For Demo Only
					if (runningDemo){
						patchMatchData[patchCounter][0] = col+startX;	
						patchMatchData[patchCounter][1] = row+startY-rowStart; //row start offset was put in because of corners
						saveDemoMatch(patchCounter,blockSizeX,blockSizeY);
						//2-5 were assigned already inside "findBestPatch"
						patchCounter++;
					}
					addPatch(newExtrapolation,patch,patchScore,weightArr,row,col);
				}else{
					placePatch(newExtrapolation,patch,patchScore,row,col);
				}
			}
		}
		if (overlap){
			for (int y=0; y<newExtrapolation.length; y++){
				for (int x=0; x<newExtrapolation[0].length; x++){
					if (weightArr[y][x]>0){
						newExtrapolation[y][x].mult(1/weightArr[y][x]);
						newExtrapolation[y][x].importance = newExtrapolation[y][x].importance/(float)weightArr[y][x];
					}
				}
			}
		}
		
		return copyCells(originalExtrapolation,newExtrapolation,startCopyY,startCopyX);
	}
	
	public static void addPatch(Pixel[][]destination,Pixel[][]patch,double[] patchScore,double[][]weightArr, int startRow, int startCol){
		int perpendicularToExtrapolationIterator;//for calculating the distance from the center of the patch and the weight
		boolean horizontalExtrapolation = patch.length>patch[0].length;

		for (int i=0; i<patch.length; i++){
			for (int j=0; j<patch[0].length; j++){
				perpendicularToExtrapolationIterator = (horizontalExtrapolation?i:j);
				double weight = 1/((Math.abs(perpendicularToExtrapolationIterator-patchSize+0.5))+0.5+Global.extrapolationDistannceToWeightFactor);
				Pixel p = new Pixel(patch[i][j]);
				p.mult(weight);
				p.importance = (float)(patchScore[0]*weight); //patchScore is an array of size 1 (used for passing by reference)
				destination[startRow+i][startCol+j].add(p);
				weightArr[startRow+i][startCol+j] += weight;
			}
		}
	}
	
	public static void placePatch(Pixel[][]destination,Pixel[][]patch,double[] patchScore, int startRow, int startCol){

		for (int i=0; i<patch.length; i++){
			for (int j=0; j<patch[0].length; j++){
				destination[startRow+i][startCol+j]=patch[i][j];
				destination[startRow+i][startCol+j].importance = (float)patchScore[0]; //patchScore is an array of size 1 (used for passing by reference)
			}
		}
	}
	
	
	public static Pixel[][] copyCells(Pixel[][]original,Pixel[][]extra, int startRow, int startCol){//works

		if (original==null){
			return extra;
		}

		else{
			for (int i=0; i<extra.length; i++){
				if (startRow+i>=original.length){
					break;
				}
				for (int j=0; j<extra[0].length; j++){
					if (startCol+j>=original[0].length){
						break;
					}
					original[startRow+i][startCol+j] = new Pixel(extra[i][j]);
				}
			}
		}
		return original;
	}
	
	public static Pixel[][] resize(Pixel[][]original, double n){
		if (original == null){
			return null;
		}
		BufferedImage imgOriginal = arrayToImage(original,false);
		BufferedImage imgResized = Scalr.resize(imgOriginal,(int)(n*original[0].length),(int)(n*original.length));
		Pixel[][] resized = imageToArray(imgResized);
		return resized;
	}
	
	public static Pixel[][] magnify(Pixel[][]original){
		return resize(original,2);
	}
	public static Pixel[][]shrink(Pixel[][]original){
		return resize(original,0.5);
	}
	
	
	public static Pixel[][] findBestPatch(Pixel[][]originBlock,int originStartX, int originStartY, int originEndX, int originEndY, Pixel[][][][]database,Orientation or,Pixel[][]previousPatch,double[] patchScore){

		int stride = PATCH_FINDER_SAMPLE_STRIDE;
		TreeMap<Double,int[]> optionalPatches = new TreeMap<Double,int[]>();
		int patchHeight = originEndY - originStartY;
		int patchWidth = originEndX - originStartX;
		double currentScore;
		double bestScore = INF;
		int blockStartRow = 0;
		int blockStartCol = 0;
		int rowOffset = 0;
		int colOffset = 0;
		if (firstIteration){
			switch(or){
			case UP:
				blockStartRow = patchHeight;
				rowOffset = -patchHeight;
				break;
			case DOWN:
				rowOffset = patchHeight;
				break;
			case LEFT:
				blockStartCol = patchWidth;
				colOffset = -patchWidth;
				break;
			case RIGHT:
				colOffset = patchWidth;
			}
		}

		for (int level=0; level<numLevels; level++){
			for (int part=0; part<partsNum; part++){
				int count = 0;
				int rowLimit = database[level][part].length - Math.max(rowOffset,0) - patchHeight;
				int colLimit = database[level][part][0].length - Math.max(colOffset,0) - patchWidth;
				for (int blockY=blockStartRow; blockY<=rowLimit; blockY+=stride){
					for (int blockX=blockStartCol; blockX<=colLimit; blockX+=stride){
						count++;
						currentScore = blockDiff(originBlock,originStartX,originStartY,originEndX,originEndY,database[level][part],blockX,blockY);
						currentScore = Math.pow(currentScore/(3*patchHeight*patchWidth), 1.f/Global.diffNorm);//Normalize
						if (optionalPatches.size()==0 || currentScore<TREE_FOCAL*(optionalPatches.firstKey()+EPS)){
							int[] coords = {level,part,blockX,blockY};
							optionalPatches.put(new Double(currentScore), coords);
						}
						if (count>=MIN_TREE_REFRESH_COUNT && optionalPatches.size()>=MAX_TREE_SIZE){
							optionalPatches = new TreeMap<Double,int[]>(optionalPatches.headMap(TREE_FOCAL*(optionalPatches.firstKey()+EPS)));
							count=0;
						}
					}
				}
			}
		}
		synchronized(sync1){
			treeSizeSum+=optionalPatches.size();
			numTrees++;
		}
		Map.Entry<Double, int[]> first = optionalPatches.firstEntry();
		TreeMap<Double,int[]> topPatches = new TreeMap<Double,int[]>();
		bestScore = first.getKey();
		int[]bestPatchCoords = copyArray(first.getValue());
		Iterator<int[]> iter = optionalPatches.values().iterator();
		int[]coords = null;
		while (iter.hasNext()){
			coords = iter.next();
			int level = coords[0];
			int part = coords[1];
			int rowLimit = database[level][part].length - Math.max(rowOffset,0) - patchHeight;
			int colLimit = database[level][part][0].length - Math.max(colOffset,0) - patchWidth;
			int newRowLimit = Math.min(rowLimit,coords[3]+stride/2);
			int newColLimit = Math.min(colLimit, coords[2]+stride/2);
			int newBlockStartRow = Math.max(blockStartRow, coords[3]-stride/2);
			int newBlockStartCol = Math.max(blockStartCol, coords[2]-stride/2);
			bestScore = bestRegionalScore(originBlock,originStartX,originStartY,originEndX,originEndY,
											bestScore,database,level,part,patchHeight,patchWidth,
											newBlockStartRow,newBlockStartCol,newRowLimit,newColLimit,bestPatchCoords,topPatches);
		}
	
		if (!firstIteration){ //Set the parameters right for getting the correct patch location and dimensions
			switch(or){
			case UP:
				patchHeight = patchHeight/2;
				break;
			case DOWN:
				patchHeight = patchHeight/2;
				rowOffset = patchHeight;
				break;
			case LEFT:
				patchWidth = patchWidth/2;
				break;
			case RIGHT:
				patchWidth = patchWidth/2;
				colOffset = patchWidth;
			}
		}
		 topPatches = new TreeMap<Double,int[]>(topPatches.headMap(PATCHES_TO_SAVE_FOCAL*(topPatches.firstKey()+EPS)));
		if (topPatches.firstKey()>0){
			synchronized(sync2){
				numPatchesPlaced++;
				LastFirstPatchRatioSum += topPatches.lastKey()/topPatches.firstKey();
			}
		}
		if (overlap){
			bestPatchCoords = getCompatibleWithPreviousPatchWithOverlap(topPatches,previousPatch,or,patchWidth,patchHeight,colOffset,rowOffset,database);
		}else{
			bestPatchCoords = getCompatibleWithPreviousPatch(topPatches,previousPatch,or,patchWidth,patchHeight,colOffset,rowOffset,database);
		}
		
		Pixel[][]bestPatch = getShiftedBlock(database[bestPatchCoords[0]][bestPatchCoords[1]],patchWidth,patchHeight,bestPatchCoords[2]+colOffset,bestPatchCoords[3]+rowOffset);
		
		//For Demo Only
		if (runningDemo){
			patchMatchData[patchCounter][2] = bestPatchCoords[0];
			patchMatchData[patchCounter][3] = bestPatchCoords[1];
			patchMatchData[patchCounter][4] = bestPatchCoords[2];
			patchMatchData[patchCounter][5] = bestPatchCoords[3];		
		}
		
		patchScore[0] = topPatches.firstKey();
		return bestPatch;
	}
	
	public static double bestRegionalScore(Pixel[][]originBlock,int originStartX, int originStartY, int originEndX, int originEndY,
			double bestScore,Pixel[][][][]database, int level, int part, int patchHeight, int patchWidth,
			int blockStartRow,int blockStartCol, int rowLimit, int colLimit, int[]bestPatchCoords,TreeMap<Double,int[]>topPatches){
		Pixel[][]currentPart = database[level][part];
		double currentScore;
		for (int blockY=blockStartRow; blockY<=rowLimit; blockY++){
			for (int blockX=blockStartCol; blockX<=colLimit; blockX++){
				currentScore = blockDiff(originBlock,originStartX,originStartY,originEndX,originEndY,currentPart,blockX,blockY);
				currentScore = Math.pow(currentScore/(3*patchHeight*patchWidth), 1.f/Global.diffNorm);//Normalize
				updatePatchTree(topPatches,currentScore,level,part,blockX,blockY);
				if (currentScore<bestScore){
					bestScore = currentScore;
					bestPatchCoords[0] = level;
					bestPatchCoords[1] = part;
					bestPatchCoords[2] = blockX;
					bestPatchCoords[3] = blockY;
				}
			}
		}
		return bestScore;
	}
	
	//this method for overlap=false
	public static int[] getCompatibleWithPreviousPatch(TreeMap<Double,int[]>topPatches,Pixel[][]previousPatch,Orientation or,
															int width, int height, int colOffset, int rowOffset, Pixel[][][][]database){
		if (previousPatch == null){
			return topPatches.firstEntry().getValue();
		}
		int startX1 = 0;//previous patch
		int startY1 = 0;
		int startX2 = 0;//new patch
		int startY2 = 0;
		int shiftX = 0; //used to get the far column/row according the the orientation
		int shiftY = 0;
		if (or == Orientation.UP || or == Orientation.DOWN){
			startX1 = width - 1;
			width = 1;
			shiftX = 1;
		}else{
			startY1 = height-1;
			height = 1;
			shiftY = 1;
		}
		double bestScore = INF;
		int[] bestCoords = null;
	    for (Map.Entry<Double, int[]>E:topPatches.entrySet()) {
	    	int[]coords = E.getValue();
	    	double sourceCompatability = E.getKey();
	    	Pixel[][]currentPart = database[coords[0]][coords[1]];//coords: 0-level, 1-part, 2-xCoord, 3-yCoord
	    	double targetCompatability = 0;
	    	for (int y=0; y<height; y++){
	    		for (int x=0; x<width; x++){
	    			int xInd1 = x+startX1;
	    			int xInd2 = coords[2]+colOffset+x+startX2;
	    			int yInd1 = y+startY1;
	    			int yInd2 = coords[3]+rowOffset+y+startY2;			
	    			targetCompatability += Pixel.linearDisimilarity(previousPatch[yInd1-shiftY][xInd1-shiftX], previousPatch[yInd1][xInd1], currentPart[yInd2][xInd2]);
	    			targetCompatability += Pixel.linearDisimilarity(currentPart[yInd2+shiftY][xInd2+shiftX],currentPart[yInd2][xInd2],previousPatch[yInd1][xInd1]);
		    	}
	    	}
	    	targetCompatability = targetCompatability/(3*height*width*2);//Normalize
	    	double currentScore = SOURCE_TARGET_PATCH_RATIO*sourceCompatability + (1-SOURCE_TARGET_PATCH_RATIO)*targetCompatability;

	    	if (currentScore < bestScore){
	    		bestScore = currentScore;
	    		bestCoords = coords;
	    	}
	    }
		return bestCoords;
	}
	
	//this method for overlap=true
	public static int[] getCompatibleWithPreviousPatchWithOverlap(TreeMap<Double,int[]>topPatches,Pixel[][]previousPatch,Orientation or,
			int width, int height, int colOffset, int rowOffset, Pixel[][][][]database){
		if (previousPatch == null){
			return topPatches.firstEntry().getValue();
		}
		int startX = 0;//previous patch
		int startY = 0;
		if (or == Orientation.UP || or == Orientation.DOWN){
			startX = 1;
			width = width-1;
		}else{
			startY = 1;
			height = height-1;
		}
		double bestScore = INF;
		int[] bestCoords = null;
	    for (Map.Entry<Double, int[]>E:topPatches.entrySet()) {
	    	int[]coords = E.getValue();
	    	double sourceCompatability = E.getKey();
	    	Pixel[][]currentPart = database[coords[0]][coords[1]];//coords: 0-level, 1-part, 2-xCoord, 3-yCoord
	    	double targetCompatability = 0;
	    	for (int y=0; y<height; y++){
	    		for (int x=0; x<width; x++){
	    			int xInd1 = x+startX;
	    			int xInd2 = coords[2]+colOffset+x;
	    			int yInd1 = y+startY;
	    			int yInd2 = coords[3]+rowOffset+y;			
	    			targetCompatability += previousPatch[yInd1][xInd1].squareDiff(currentPart[yInd2][xInd2]);
		    	}
	    	}
	    	targetCompatability = Math.pow(targetCompatability/(3*height*width), 1.f/Global.diffNorm);//Normalize
	    	double currentScore = SOURCE_TARGET_PATCH_RATIO*sourceCompatability + (1-SOURCE_TARGET_PATCH_RATIO)*targetCompatability;
	    	if (currentScore < bestScore){
//		    	System.out.println("Source compatibility = " +sourceCompatability+ " , "+ "Target Compatibility = "+targetCompatability+" , "+ "Total = "+currentScore);
	    		bestScore = currentScore;
	    		bestCoords = coords;
	    	}
	    }

		return bestCoords;
	}
	
	
	
	public static void updatePatchTree(TreeMap<Double,int[]>topPatches, double score, int level, int part, int blockX, int blockY){
		if (topPatches.size() < NUM_PATCHES_TO_SAVE){
			int[]coords = {level,part,blockX,blockY};
			topPatches.put(new Double(score),coords);
			return;
		}else{
			double worstScore = topPatches.lastKey();
			if (worstScore > score){
				int[]coords = {level,part,blockX,blockY};
				topPatches.remove(worstScore);
				topPatches.put(new Double(score),coords);
			}
		}
	}
	
	public static double blockDiff(Pixel[][]block1,int start1X, int start1Y, int end1X, int end1Y,Pixel[][]block2,int start2X, int start2Y){
		double result = 0;
		int width = end1X - start1X;
		int height = end1Y - start1Y;
		for (int y=0; y<height; y++){
			for (int x=0; x<width; x++){
				result += block1[y+start1Y][x+start1X].squareDiff(block2[y+start2Y][x+start2X]);
			}
		}
		return result;
	}
	
	

	public static Pixel[][][][] createImagePyramid(Pixel[][][]parts,double n){//Works
		Pixel[][][][] imagePyramid = new Pixel[numLevels][partsNum][][];
		for (int part=0; part<partsNum; part++){
			imagePyramid[numLevels-1][part] = parts[part];
			for (int level=numLevels-2; level>=0; level--){
			//	imagePyramid[level][part] = shrink(imagePyramid[level+1][part]);	
				imagePyramid[level][part] = resize(imagePyramid[level+1][part],1/n);
			}
		}
		return imagePyramid;
	}
	public static Pixel[][][][] getFullPyramidLayers(Pixel[][][][] imagePyramid,Pixel[][][][]extrapolations, int level){
		if (extrapolations[0][0]==null){
			return imagePyramid;
		}
		Pixel[][][][] fullPyramidLayers = new Pixel[numLevels][partsNum][][];
		for (int part=0; part<partsNum; part++){
			for (int l=0; l<numLevels; l++){
				fullPyramidLayers[l][part] = imagePyramid[l][part];
			}
			for (Orientation or:Orientation.values()){
				Pixel[][] resizedExtrapolated = (extrapolations[or.ordinal()][part]);
				fullPyramidLayers[level-1][part] = combine(fullPyramidLayers[level-1][part],resizedExtrapolated,or);//attatch the extrapolation the previous level because it was created in the last iteration
//				for (int i=level+1; i<numLevels; i++){
//					resizedExtrapolated = magnify(resizedExtrapolated);
//					fullPyramidLayers[i][part] = combine(fullPyramidLayers[i][part],resizedExtrapolated,or);
//				}
//				resizedExtrapolated = extrapolations[or.ordinal()][part];
				
				//This part uses shrinked extrapolations for the smaller layers of the pyramid. It has bugs and isn't very beneficial so it is out for now
//				for (int i=level-1; i>=0; i--){
//					resizedExtrapolated = shrink(resizedExtrapolated);
//					fullPyramidLayers[i][part] = combine(fullPyramidLayers[i][part],resizedExtrapolated,or);
//				}
			}
		}
		return fullPyramidLayers;
	}
	
	public static Pixel[][]buildFromParts(Pixel[][][]parts, int xParts, int yParts){
		int pHeight = parts[0].length;
		int pWidth = parts[0][0].length;
		Pixel[][] result = new Pixel[yParts*pHeight][xParts*pWidth];
		for (int partY=0; partY<yParts; partY++){
			for (int partX=0; partX<xParts; partX++){
				for (int y=0; y<pHeight; y++){
					for (int x=0; x<pWidth; x++){
						result[partY*pHeight+y][partX*pWidth+x] = parts[partY*xParts + partX][y][x];
					}
				}
			}
		}
		return result;
	}
	
	public static Pixel[][][]breakToParts(Pixel[][]imgArray, int xParts, int yParts){//works
		int height = imgArray.length;
		int width = imgArray[0].length;
		int pHeight= height/yParts ;
		int pWidth = width/xParts;
		Pixel[][][] parts = new Pixel[yParts*xParts][pHeight][pWidth];
		for (int partY=0; partY<yParts; partY++){
			for (int partX=0; partX<xParts; partX++){
				for (int y=0; y<pHeight; y++){
					for (int x=0; x<pWidth; x++){
						 parts[partY*xParts + partX][y][x] = imgArray[partY*pHeight+y][partX*pWidth+x];
					}
				}
			}
		}
		return parts;
	}
	public static Pixel[][]removeFrame(Pixel[][]part, int amount){
		if (amount == 0){
			return part;
		}
		int newHeight = part.length - 2*amount;
		int newWidth = part[0].length - 2*amount;
		Pixel[][]result = new Pixel[newHeight][newWidth];
		for (int y=0; y<newHeight; y++){
			for (int x=0; x<newWidth; x++){
				result[y][x] = part[y+amount][x+amount];
			}
		}
		return result;
	}
	
	public static void removeFrame(Pixel[][][]parts,int amount){
		if (amount == 0){
			return;
		}
		for (int part=0; part<parts.length; part++){
			parts[part] = removeFrame(parts[part],amount);
		}
	}
		
	
	
	public static void print(double[][]arr){//works
		System.out.println();
		for (int i=0; i<arr.length; i++){
			for (int j=0; j<arr[0].length; j++){
				System.out.print(arr[i][j]+" ");
			}
			System.out.println();
		}
		System.out.println();
	}

	public static Pixel[][] combine(Pixel[][] original, Pixel[][]extra, Orientation or){//works

		int combinedHeight = original.length;
		int combinedWidth = original[0].length;
		int firstOriginalRow = 0;
		int firstOriginalCol = 0;
		int firstExtraRow = 0;
		int firstExtraCol = 0;
		switch(or){
		case UP:
			firstOriginalRow = extra.length;
			combinedHeight += extra.length;
			break;
		case DOWN:
			firstExtraRow = original.length;
			combinedHeight += extra.length;
			break;
		case LEFT:
			firstOriginalCol = extra[0].length;
			combinedWidth += extra[0].length;
			break;
		case RIGHT:
			firstExtraCol = original[0].length;
			combinedWidth += extra[0].length;
			break;
		}
		Pixel[][]result = new Pixel[combinedHeight][combinedWidth];
		result = copyCells(result,original,firstOriginalRow,firstOriginalCol);
		result = copyCells(result,extra,firstExtraRow,firstExtraCol);
		return result;
	}
	
//	public static Pixel[][] getBlock(Pixel[][]source, int startX, int startY, int width, int height){//Same as getShiftedBlock, can replace this
//		if (source == null){
//			return null;
//		}
//		Pixel[][]result = new Pixel[height][width];
//		for (int y=0; y<height; y++){
//			for (int x=0; x<width; x++){
//				result[y][x] = source[y+startY][x+startX];
//			}
//		}
//		return result;
//	}
	
	public static Pixel[][] getShiftedBlock(Pixel[][]source, int blockWidth, int blockHeight, int startCol, int startRow){
		if (source == null){
			return null;
		}
		Pixel[][]shiftedBlock = new Pixel[blockHeight][blockWidth];
		for (int y=0; y<blockHeight; y++){
			for (int x=0; x<blockWidth; x++){
				shiftedBlock[y][x] = new Pixel(source[y+startRow][x+startCol]);
			}
		}
		return shiftedBlock;
	}
	public static BufferedImage arrayToImage(Pixel[][]arr, boolean importanceImage){//works
		int[]rgb = new int[3];
		BufferedImage nimg = new BufferedImage(arr[0].length,arr.length,BufferedImage.TYPE_INT_RGB);
		Color c = null;
		for (int y=0; y<arr.length; y++){
			for (int x=0; x<arr[0].length; x++){
				if (importanceImage){
					int gray = (int)arr[y][x].importance;
					c = new Color(gray,gray,gray);
				}else{
					rgb = arr[y][x].getRGB();
					c = new Color(rgb[0],rgb[1],rgb[2]);
				}
				nimg.setRGB(x, y, c.getRGB());
			}
		}
		return nimg;
	}
	public static Pixel[][] imageToArray(BufferedImage img){//works
		int w = img.getWidth();
		int h = img.getHeight();
		Pixel[][] imgArray=new Pixel[h][w];
		for(int i=0;i<h;i++){
			for(int j=0;j<w;j++){
				int RGB=img.getRGB(j, i);
				Color c = new Color(RGB);
				int B=c.getBlue();
				int G=c.getGreen();
				int R=c.getRed();
				imgArray[i][j] = new Pixel(R,G,B,1);
			}
		}
		return imgArray;
	}
	
	public static void save(Pixel[][]imgArray,String name ,String partNum, boolean saveImportance){
		BufferedImage img = arrayToImage(imgArray,saveImportance);
		try {
			ImageIO.write(img, "png", new File(name + partNum+Global.FILE_TYPE));
		} catch (IOException e) {e.printStackTrace();}
	}
	
	public static void save(Pixel[][]block, int startX, int startY, int endX, int endY, String name) throws IOException{
		Pixel[][] imgArray = new Pixel[endY-startY][endX - startX];
		for (int y=0; y<imgArray.length; y++){
			for (int x=0; x<imgArray[0].length; x++){
				imgArray[y][x] = block[y+startY][x+startX];
			}
		}
		BufferedImage img = arrayToImage(imgArray,false);
		ImageIO.write(img, "png", new File(name+Global.FILE_TYPE));
	}
	
	public static int[]copyArray(int[]arr){
		int[]result = new int[arr.length];
		for (int i=0; i<arr.length; i++){
			result[i] = arr[i];
		}
		return result;
	}
	public static BufferedImage burnPixels (String imgName, int partSizeX, int partSizeY,int  burnExtent, double burnProbability) throws IOException{
		BufferedImage img = ImageIO.read(new File(imgName+Global.FILE_TYPE));
		Random r = new Random();
		Color green = new Color(0,255,0);
		for (int y=0; y<img.getHeight();y++){
			for (int x=0; x<img.getWidth(); x++){
				if ( ((x+burnExtent)%partSizeX < 2*burnExtent ) || ((y+burnExtent)%partSizeY < 2*burnExtent )){
					if (r.nextFloat()<burnProbability){
						img.setRGB(x, y, green.getRGB());
					}
				}
			}
		}
//		String burntImageName = imgName+"burnExtent"+burnExtent+"_prob"+burnProbability+Global.FILE_TYPE;
//		ImageIO.write(img,"png",(new File(burntImageName)));
		return img;
	} 
	
	public static void fillBurntPixels(BufferedImage img, BufferedImage fillImg){
		int height = img.getHeight();
		int width = img.getWidth();
		Color c = new Color(0,255,0);
		for (int y=0; y<height; y++){
			for (int x=0; x<width; x++){
				if (img.getRGB(x, y) == c.getRGB()){
					img.setRGB(x, y, fillImg.getRGB(x, y));
				}
			}
		}
	}
	

	public static void initializePartsAndDatabase(String puzzleName, int partSize, int _burnExtent){
		Global.diffNorm = 2; // Maybe change this later
		firstIteration = true;
		burnExtent = _burnExtent;
		BufferedImage img = null;
		try {
			img = ImageIO.read(new File(Global.INPUT_FOLDER +puzzleName+Global.FILE_TYPE));
		}  catch (IOException e) {e.printStackTrace();}
		xParts = img.getWidth()/partSize;
		yParts = img.getHeight()/partSize;
		partsNum = xParts*yParts;
		Pixel[][]imgArray = imageToArray(img);
		parts = breakToParts(imgArray,xParts,yParts);
		removeFrame(parts,burnExtent);
		database = createImagePyramid(parts,resizeFactorDatabase);
	}
	
	public static void prepareImageForSolving(String puzzleName, String preparedName,int _burnExtent, int burnProb,int partSize,int sizeOverlap,boolean singleStep, boolean saveParts){

		int _numIterations = numIterations;
		numIterations = 1;
		initializePartsAndDatabase(puzzleName,partSize,_burnExtent);
		BufferedImage burntImage = null;
		try {
			burntImage = burnPixels(Global.INPUT_FOLDER +puzzleName,partSize,partSize,burnExtent,burnProb/100.f);
		} catch (IOException e) {e.printStackTrace();}
		Pixel[][][]finalParts = Pixel.copy(parts);
		Pixel[][][][]extParts = null;
		
//		if (!singleStep){
//			extParts = extrapolateParts(parts);
//			for (int part=0; part<partsNum; part++){
//				for (int or=0; or<4; or++){
//					finalParts[part] = combine(finalParts[part],extParts[or][part],Orientation.values()[or]);
//				}
//			}
//			removeFrame(finalParts, patchSize-burnExtent);
//			imgArray = buildFromParts(finalParts,xParts,yParts);
////			save(imgArray,input+"_levels"+numLevels+"_iterations"+numIterations+"_overlap "+overlap+"_patchSize"+patchSize+"_BE "+burnExtent+"_UFB_"+useFullBlock+"_PATCH_RAT_"+SOURCE_TARGET_PATCH_RATIO+Global.FILE_TYPE);
//			fillBurntPixels(burntImage,arrayToImage(imgArray,false));
//			imgArray = imageToArray(burntImage);
//			parts = breakToParts(imgArray,xParts,yParts);
//			finalParts = Pixel.copy(parts);
//		}
		
		numIterations = _numIterations;
		treeSizeSum = 0;
		numTrees = 0;
		if (patchSize>0){
			extParts = extrapolateParts();
			int extrapolationExtent = (int)(patchSize*Math.pow(2, numIterations-1))-burnExtent;
			for (int part=0; part<partsNum; part++){
				for (int or=0; or<4; or++){
					normalizeImportance(extParts[or][part],or,extrapolationExtent+burnExtent);
					finalParts[part] = combine(finalParts[part],extParts[or][part],Orientation.values()[or]);
				}
			}
			removeFrame(finalParts,extrapolationExtent - sizeOverlap);
		}
		if (saveParts||Global.createExtrapolationImportance){
			for (int part=0; part<partsNum; part++){
				save(finalParts[part],Global.pathToParts+puzzleName,""+part,false);
				if (Global.createExtrapolationImportance && patchSize>0){
					save(finalParts[part],Global.pathToImportance+puzzleName,""+part,true);
				}
			}
		}
		Pixel[][]imgArray = buildFromParts(finalParts,xParts,yParts);
			save(imgArray,Global.INPUT_FOLDER + puzzleName+"//"+preparedName,"",false);
	}
	
	public static void normalizeImportance(float[] importanceVector,int extrapolationExtent){
		double max = 0;
		double min = INF;
		double sum = 0;
		double currentScore;

		for (int i=0; i<importanceVector.length; i++){
			if ((i<extrapolationExtent || i>=importanceVector.length-extrapolationExtent)){
				continue;
			}
			currentScore = importanceVector[i];
			if (max < currentScore){
				max = currentScore;
			}
			if (min > currentScore){
				min = currentScore;
			}

			sum = sum + currentScore;
		}
		double mean = sum/(importanceVector.length-2*burnExtent); //Not used for now
		
		for (int i=0; i<importanceVector.length; i++){
			if ((i<extrapolationExtent || i>=importanceVector.length-extrapolationExtent)){
				importanceVector[i]= 0;
			}else{
				currentScore = Math.round(255*(importanceVector[i]-min)/(max-min));
				importanceVector[i] = (float)Math.min(255, 255-currentScore+1); //Flip the scale to make high scores into low importance and make the minimum importance 1 (instead of 0).
			}
		}
	}
	
	public static void normalizeImportance(Pixel[][]block,int or,int extrapolationExtent){//For simplifying importance calculation
		int vectorSize = Math.max(block.length, block[0].length);
		float[] vector = new float[vectorSize];
		if (or ==0 || or == 1){
			extrapolationExtent = 0;
			for (int x=0; x<block[0].length; x++){
				vector[x] = block[0][x].importance;
			}
		}else{
			for (int y=0; y<block.length; y++){
				 vector[y] = block[y][0].importance;
			}
		}
		normalizeImportance(vector,extrapolationExtent);
		if (or ==0 || or == 1){
			for (int y=0; y<block.length; y++){
				for (int x=0; x<block[0].length; x++){
					block[y][x].importance = vector[x];
				}
			}
		}else{
			for (int x=0; x<block[0].length; x++){
				for (int y=0; y<block.length; y++){
					block[y][x].importance = vector[y];
				}
			}
		}		
	}
	public static void makeIndividualParts(String input, String output,int partSize,int sizeOverlap) throws IOException {
		System.out.println("Making individual parts");
		if (partSize!=Global.ORIGINAL_PART_SIZE){
			System.out.println("Warning! partSize doesn't match");
		}
		BufferedImage img = ImageIO.read(new File(Global.INPUT_FOLDER+Global.puzzleName+"\\"+input+Global.FILE_TYPE));
		int yParts = img.getHeight()/(partSize + 2*sizeOverlap);
		int xParts = img.getWidth()/(partSize + 2*sizeOverlap);
		int partsNum = xParts*yParts;
		Pixel[][] imgArray = imageToArray(img);
		Pixel[][][]parts = breakToParts(imgArray,xParts,yParts);
		for (int part=0; part<partsNum; part++){
			save(parts[part],output,""+part,false);
		}
	}
	
//	public static void makeIndividualPartsMatlab(){//To be used only in case the matlab method doesn't work
//	    int sizeOverlap = 0;
//	    int _burnExtent = 7;
//	    int patchSize = 7;
//	    String puzzleName = "6dovSC4";
//	    String preparedName = setPreparedName();
//	    try {
//			makeIndividualParts(preparedName,Global.pathToParts,Global.ORIGINAL_PART_SIZE,sizeOverlap);
//		} catch (IOException e) {e.printStackTrace();}
//	}
	
	public static void singlePartDemo(String puzzleName, int partSize, int _burnExtent, int _partNum, Orientation or ){
		runningDemo = true;
		partNum = _partNum;
		patchSize = _burnExtent;
		initializePartsAndDatabase(puzzleName,partSize,_burnExtent);
		int numPatches = partSize - 2*burnExtent - 2*patchSize + 1;
		patchMatchData = new int[numPatches][6];
		Pixel[][]extrapolation = extrapolatePart(parts[partNum],null,database,or);
		Pixel[][] completedPart = combine(parts[partNum],extrapolation,or);
		save(completedPart,"Matches//completed","",false);
	}
	public static void saveDemoMatch(int matchNum,int patchWidth, int patchHeight){//"or" not used for now
//		Pixel[][]zeros = new Pixel[parts[0].length][10];
//		Pixel.initWithZeros(zeros);
		Random r = new Random();
		Pixel[][]originalPart = Pixel.copy(parts[partNum]);
		Pixel[][]sourcePart = Pixel.copy(database[patchMatchData[matchNum][2]][patchMatchData[matchNum][3]]);
		int[]color = {r.nextInt(256),r.nextInt(256),r.nextInt(256)};
		colorPatchFrame(originalPart,color,patchWidth,patchHeight,patchMatchData[matchNum][0],patchMatchData[matchNum][1]);
		colorPatchFrame(sourcePart,color,patchWidth,patchHeight,patchMatchData[matchNum][4],patchMatchData[matchNum][5]);
		save(originalPart,"Matches//original",""+matchNum,false);
		save(sourcePart,"Matches//source",""+matchNum,false);
		System.out.println("Finished match "+matchNum+" out of "+(patchMatchData.length-1));
	}
	
	public static void colorPatchFrame(Pixel[][] part,int[]color, int patchWidth, int patchHeight, int patchStartX, int patchStartY){
		Pixel p = new Pixel(color[0],color[1],color[2],0);
		for (int i=0; i<patchHeight; i++){
			part[i+patchStartY][0+patchStartX] = p;
			part[i+patchStartY][patchWidth-1+patchStartX] = p;
		}
		for (int j=0; j<patchWidth; j++){
			part[0+patchStartY][j+patchStartX] = p;
			part[patchHeight-1+patchStartY][j+patchStartX] = p;
		}
	}
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		singlePartDemo("1dov",128,7,437,Orientation.DOWN);

	}
}
