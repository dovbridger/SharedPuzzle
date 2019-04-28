package puzzle;

import gui.GuiDebugger;
import utils.Utils;

import java.awt.Color;
//import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeMap;

import javax.imageio.ImageIO;
//import javax.swing.ImageIcon;
//import javax.swing.JLabel;
//import javax.swing.JPanel;
//import javax.swing.SwingUtilities;












import data_type.GeometryPoolEntry;
import data_type.PoolEntry;
import multi_thread.ErrorBaseLine;
import multi_thread.Extrapolation;

public class PuzzleSolver {
	//Holds information about all the puzzle parts in the puzzle
	public PuzzleParts puzzleParts;
	//A dissimilarity matrix between all pairs of parts in the puzzle at a given orientation: 0 - or, 1 - part1, 2 - part2
	//High numbers = bad compatibility
	float diffMatrix[][][];
	//Object that contains the geometric relations data (Calculated in Matlab using SIFT)
	GeometricRelations geometricRelations;
	//The normalized version of "diffMatrix", contains the confidence in the match
	//High numbers = good compatibility
	float confMatrix[][][];
	//Contains the best neighbor (and not best buddy) of each part at a given orientation: 0 - part, 1 - or
	public int bestBuddies[][];
	//The part size for the purpose of solving the puzzle (after burning and extrapolating manipulations, not the original)
	int partSize;
	//Number of parts in the puzzle
	public int partsNum;
	int missPartsNum;
	int burnPartsNum;
	int w;
	int h;
	//The first piece - chosen in the method "findBestStart"
	int startPart;
	public int partsLeft;
	//The number of borders inside the puzzle (border = part to part adjacencies)
	int borderNum;
	int boardSize;
	int boardCenter;
	BufferedImage img;
	HashMap<Integer, Integer[]> chosenParts;
	TreeMap<Double, PoolEntry> nextToPlace;
	TreeMap<Double, PoolEntry> nextToPlaceGeometry; // after calculating the true key of each entry
	ArrayList<GeometryPoolEntry> nextToPlaceGeometryRaw; //before calculating the true key of each entry
	int budddiesNumber[];
	BufferedImage nimg;
	Integer finalPlacement[][];
	String name;
	Date d1, d2, d3;
	Random rnd = new Random();
	TreeMap<Double, Integer> startPartRelationsTree;
	BufferedImage intermediateOutputImage;
	MatlabHoleFiller mhf;
	double[]rankStats;
	int[][]ranks;
	boolean firstConfCalc;
	GuiDebugger guiDebug;
	boolean mistake = false;

	boolean useSize, useSize2;
	int minX, maxX, minY, maxY;
	double scale;
	boolean missPart;
	Double distanceFromExpectedAll;
	double numDistances;

	public PuzzleSolver(BufferedImage _img, int _partSize, String _name,
			boolean _useSize, boolean _missPart, int _borderNum, MatlabHoleFiller _mhf)
			throws Exception {
		name = _name;
		useSize = _useSize;
		missPart = _missPart;
		borderNum = _borderNum;
		d1 = new Date();
		puzzleParts = new PuzzleParts(_img, _partSize);
//		SaveworthyPuzzleData parts = new SaveworthyPuzzleData(puzzleParts);
//		CopyObject.write(parts, "parts.txt");
		puzzleParts.saveBurntFile();
		partSize = _partSize;
		partsNum = puzzleParts.getPartsNum();
		missPartsNum = puzzleParts.getMissPartsNum();
		burnPartsNum = puzzleParts.getBurnPartsNum();
		System.out.println("M: " + missPartsNum);
		h = puzzleParts.hetH();
		w = puzzleParts.hetW();
		boardSize = partsNum/2 + 10;
		boardCenter = boardSize/2;
		diffMatrix = new float[4][partsNum][partsNum];
		confMatrix = new float[4][partsNum][partsNum];
		intermediateOutputImage = new BufferedImage(w*partSize,h*partSize,BufferedImage.TYPE_INT_RGB);
		
		geometricRelations = GeometricRelations.loadGeometricRelations();
		mhf = _mhf;
		firstConfCalc = true;
		if (Global.useGeometricRelations && geometricRelations.data.length != partsNum){
			System.out.println("Something is wrong with the geometric relations array, length doesn't match partsNum!!!!!");
		}
		
		bestBuddies = new int[partsNum][4];
		partsLeft = partsNum;
		budddiesNumber = new int[partsNum];
		chosenParts = new HashMap<Integer, Integer[]>();
		nextToPlace = new TreeMap<Double, PoolEntry>();
		nextToPlaceGeometry = new TreeMap<Double, PoolEntry>();
		nextToPlaceGeometryRaw = new ArrayList<GeometryPoolEntry>();
		distanceFromExpectedAll = 0.d;

		finalPlacement = new Integer[boardSize][boardSize];
		minX = boardSize;
		maxX = 0;
		minY = boardSize;
		maxY = 0;
		scale = 672.0 / partSize / puzzleParts.nw;
		run();
	}

	public void run() throws Exception {
		calcDiffMatrix();
		// Save the diff matrix to a text file that can be parsed in python
		Utils.dump_float_matrix3d_to_json(diffMatrix, Global.pathToPuzzleDataInit + Global.TEXTUAL_DIFF_MATRIX_FILE);
		//If requested - load a previously modified diffMatrix from a file.
		//This was used when hole filling provided new dissimilarity scores to be used in the solving.
		//Now the best method is instead to use an online (with matlab) modification of confidence scores (in "modifyConfMatrix")
		if (!Global.diff_matrix_correction_method.equals("none")){
			float [][][] modified_diff_matrix = Utils.load_float_matrix3d_from_json(Global.pathToPuzzleDataInit + Global.TEXTUAL_DIFF_MATRIX_FILE_MODIFIED +
					Global.diff_matrix_correction_method + ".txt");
			if (modified_diff_matrix == null){
				System.out.println("Couldn't modify diff matrix, exiting");
				throw new Exception();
			} else {
				diffMatrix = modified_diff_matrix;
				System.out.println("Cnn diff matrix used");
			}
		}
		//Load the puzzle context into the Matlab workspace
		if (!Global.no_matlab){
			mhf.loadPuzzle();
		}
		Date d3 = new Date();
		calcConfMatrix();

		// Save initial puzzle data (diff, conf and best buddies) for a gui representation implemented in "PuzzleGUI"
		SaveworthyPuzzleData puzzleData = new SaveworthyPuzzleData(diffMatrix,confMatrix, bestBuddies);
		rankStats = puzzleData.getCorrectPartsRankStats(puzzleParts.nw, puzzleParts.nh);
		ranks = puzzleData.ranks;
		modifyConfMatrix();
		try {
			//if modifications to diffMatrix or confMatrix where made, save them to a "mod" file
			//and don't overwrite the original
			if (!Global.diff_matrix_correction_method.equals("none")|| Global.modifyConf){
				CopyObject.write(puzzleData,Global.pathToPuzzleDataInit + Global.INIT_MOD_DATA_FILE);
			}else{
				//else - save this initial situation to the original data file
				CopyObject.write(puzzleData, Global.pathToPuzzleDataInit + Global.INIT_DATA_FILE);
			}
		} catch (IOException e) {}
		System.out.println();
		System.out.println("Rank Average = "+rankStats[0]);
		System.out.println("Rank STD = "+rankStats[1]);
		System.out.println("Rank Absolute Differences = "+rankStats[2]);
		System.out.println("Rank at "+ Global.RANK_PERCENTILE+" percentile = "+rankStats[3]);
		System.out.println("Percentage of ranks under "+ Global.RANK_THRESH+" = "+rankStats[4]);
		System.out.println("Rank Max = "+rankStats[5]);
		System.out.println();
		
		if (Global.farBuddies) {
			findFarBuddies();
		}
		if (Global.burnBorders) {
			puzzleParts.burnBorders();
		}
		createOriginalImage(name + "_original");
		
		
		
		if (!Global.solve) {
			return;
		}

		findBestStart(true);
		System.out.println("The first piece is " + startPart);
		solvePuzzle();
		Date d2 = new Date();
		System.out.println("Time: " + (d2.getTime() - d1.getTime()) / 1000.0);
		System.out.println("Init Time: " + (d3.getTime() - d1.getTime())
				/ 1000.0);
		Global.directResult = (int) (100 * calcDirectComp());
		Global.neighborResult = (float) calcNeighborsComp();
		if (Global.saveResultImage){
			createOutputImage(name + " direct_" + Global.directResult + " nieghbor_"
					+ (int) (100 * Global.neighborResult) + "_LF_" + Global.lFactor
					+ "_norm_" + Global.diffNorm + "_DBS_" + Global.diffBlockSize,
					true);
		}
	}

	private void printBB() {
		// TODO Auto-generated method stub
		File f = new File("BB.txt");
		try {
			BufferedWriter bf = new BufferedWriter(new FileWriter(f));
			for (int k = 0; k < 4; k++) {
				bf.write(k + ":");
				bf.newLine();
				for (int i = 0; i < partsNum; i++) {
					bf.write(i + ":" + bestBuddies[i][k]);
					bf.newLine();
				}
				bf.newLine();
			}
			bf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//Get the Direct comparison score of the solve
	private double calcDirectComp() {
		double result = 0; // Dov
		double sum = 0;
		Integer[] off = chosenParts.get(startPart);
		int xOff = boardCenter - minX - (startPart - 1) % puzzleParts.nw - 1;
		int yOff = boardCenter - minY - (startPart - 1) / puzzleParts.nw;
		System.out.println("Off: " + xOff + " " + yOff);
		for (int j = minX; j < maxX + 1; j++) {
			for (int i = minY; i < maxY + 1; i++) {
				if ((finalPlacement[i][j] != null &&
					finalPlacement[i][j] - 1 == j - minX - xOff + (i - minY - yOff) * puzzleParts.nw)){
					sum++;
				}
			}
		}
		result = sum / (partsNum - missPartsNum);
		System.out.println("Direct Comp: " + result);
		return result;
	}

	//Get the neighbor comparison score of the solve
	private double calcNeighborsComp() {
		double errorBaseLine;
		//If an errorBaseLine already exists - use it
		if (Global.errorBaseLine > 0){
			errorBaseLine = Global.errorBaseLine;
		//Else - calculate (multi-threaded) the average SSD between any two parts in the puzzle
		}else{
			ErrorBaseLine.createAndRunWorkers(Global.burnExtent+Global.sizeOverlap,Global.puzzleName,this);
			errorBaseLine = ErrorBaseLine.getErrorBaseLine();
		}
		double result = 0;
		double sum = 0;
		double partialCorrectnessSum = 0;
		int nh = puzzleParts.nh, nw = puzzleParts.nw;
		for (int i = minY; i < maxY + 1; i++) {
			for (int j = minX; j < maxX + 1; j++) {
				if ((finalPlacement[i][j] != null
						&& finalPlacement[i][j + 1] != null
						&& (finalPlacement[i][j] - 1) % nw < nw - 1)){
					if (finalPlacement[i][j] == finalPlacement[i][j + 1] - 1){
				
						sum++;
					}else{
						//A value between 0 and 1 representing how much a wrongly placed neighbor is similar to the correct neighbor
						partialCorrectnessSum += ErrorBaseLine.getPartialCorrectness(finalPlacement[i][j]-1,finalPlacement[i][j+1]-1,errorBaseLine); //Everything in final placement is +1 from the regular part numbering
					}
				}
					
				if ((finalPlacement[i][j] != null
						&& finalPlacement[i + 1][j] != null
						&& (finalPlacement[i][j] - 1) / nw < nh - 1)){
					if (finalPlacement[i][j]- 1 + nw == finalPlacement[i + 1][j] - 1){
						sum++;
					}else{
						partialCorrectnessSum += ErrorBaseLine.getPartialCorrectness(finalPlacement[i][j]-1,finalPlacement[i + 1][j]-1,errorBaseLine);
					}
				}
			}
		}

		result = sum / borderNum;
		double resultWithPartialCorrectness = (sum+partialCorrectnessSum)/borderNum;
		System.out.println("Neighbors Comp: " + result);
		System.out.println();
		System.out.println("Error base line = " +errorBaseLine);
		System.out.println("Neighbors Comp New: " + resultWithPartialCorrectness);
		return result;
	}

	static public Image getScaledImage(Image srcImg, int w, int h) {
		BufferedImage resizedImg = new BufferedImage(w, h,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = resizedImg.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(srcImg, 0, 0, w, h, null);
		g2.dispose();
		return resizedImg;
	}

	private void createOutputImage(String s, boolean save) {

		
		int borders[] = findBorder();
		nimg = new BufferedImage(partSize * (borders[3] - borders[2] + 1),
				partSize * (borders[1] - borders[0] + 1),
				BufferedImage.TYPE_INT_RGB);
		int offsety = boardCenter - borders[0];
		int offsetx = boardCenter - borders[2];
		Iterator<Integer> partsIt = chosenParts.keySet().iterator();
		while (partsIt.hasNext()) {
			int part = partsIt.next();
			Integer[] place = chosenParts.get(part);
			// System.out.println(place[0]+","+place[1]); //Dov
			nimg.setRGB(
					place[0] * partSize + partSize * offsetx,
					place[1] * partSize + partSize * offsety,
					partSize,
					partSize,
					puzzleParts.getRGBPart(part).getRGB(0, 0, partSize,
							partSize, null, 0, partSize), 0, partSize);
		}
		if (Global.saveResultImage) {
			File outputfile = new File(Global.pathToOutput + s + ".png");
			try {
				ImageIO.write(nimg, "png", outputfile);
				if (Global.sizeOverlap > 0) { // Check if the result image needs to be trimmed (overlaping boarders of parts)
					int xParts = nimg.getWidth() / partSize;
					int yParts = nimg.getHeight() / partSize;
					Pixel[][] imgArray = Extrapolation.imageToArray(nimg);
					Pixel[][][] parts = Extrapolation.breakToParts(imgArray,
							xParts, yParts);
					Extrapolation.removeFrame(parts, Global.sizeOverlap);
					imgArray = Extrapolation.buildFromParts(parts, xParts,
							yParts);
					Extrapolation.save(imgArray, outputfile + "_trimmed.png","",false); //The false means don't save as grayscale (importance)
				}

			} catch (IOException e) {e.printStackTrace();}
		}
	}

	private void intermediateOutput(int count,int part, Integer[]place) {
		
		nimg = new BufferedImage(partSize * (maxX - minX + 1),
				partSize * (maxY-minY + 1),
				BufferedImage.TYPE_INT_RGB);	
		int offsety = boardCenter - minY;
		int offsetx = boardCenter - minX;
		
		Iterator<Integer> partsIt = chosenParts.keySet().iterator();
		while (partsIt.hasNext()) {
			part = partsIt.next();
			place = chosenParts.get(part);
			nimg.setRGB((place[0]+offsetx)*partSize, (place[1]+offsety)*partSize, partSize, partSize,
					puzzleParts.puzzlePartsNumbered[part].getRGB(0, 0,
							partSize, partSize, null, 0, partSize), 0, partSize);
		}

		File outputfile = new File(Global.pathToIntermediateOutput
				+ Global.puzzleName + "_stage" + count + Global.FILE_TYPE);
		try {
			ImageIO.write(nimg, "jpg", outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void createOriginalImage(String s) {// Dov
		nimg = new BufferedImage(partSize * puzzleParts.nw, partSize
				* puzzleParts.nh, BufferedImage.TYPE_INT_RGB);
		for (int part = 0; part < partsNum; part++) {
			int row = part / puzzleParts.nw;
			int col = part % puzzleParts.nw;
			BufferedImage partImg;
			if (Global.number) {
				partImg = puzzleParts.puzzlePartsNumbered[part];
			} else {
				partImg = puzzleParts.getRGBPart(part);
			}
			nimg.setRGB(
					col * partSize,
					row * partSize,
					partSize,
					partSize,
					partImg.getRGB(0, 0, partSize, partSize, null, 0, partSize),
					0, partSize);
		}
		File outputfile = new File(Global.pathToOutput + s + ".png");
		try {
			ImageIO.write(nimg, "png", outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private int[] findBorder() {// Dov-Finds the start and end indexes of the x
								// and y coordinates in the finalPlacement array
		int borders[] = { boardSize, 0, boardSize, 0 };
		// int borders[]={minY,maxY,minX,maxX};
		for (int i = 0; i < boardSize; i++) {
			for (int j = 0; j < boardSize; j++) {
				if (finalPlacement[i][j] != null) {
					borders[1] = Math.max(borders[1], i);
					borders[0] = Math.min(borders[0], i);
					borders[3] = Math.max(borders[3], j);
					borders[2] = Math.min(borders[2], j);
				}
			}
		}
		return borders;
	}

	private void solvePuzzle() {
		if (Global.GUI_DEBUG){
			guiDebug = new GuiDebugger(partSize,puzzleParts.nw,puzzleParts.nh,puzzleParts.puzzlePartsNumbered,Global.puzzleName,true);
			guiDebug.launch();
		}
		// int lastPart = -1;
//		boolean mistakeWasMade = false;
		int lastPartCounter = partsNum;
		int denum = 10;
		if (burnPartsNum > 10)
			denum = 64;
		Integer base[] = { 0, 0 };
//		chosenParts.put(startPart, base);
//		finalPlacement[partsNum / 4][partsNum / 4] = startPart + 1;
//		maxX = Math.max(maxX, partsNum / 4);
//		minX = Math.min(minX, partsNum / 4);
//		maxY = Math.max(maxY, partsNum / 4);
//		minY = Math.min(minY, partsNum / 4);
//		partsLeft--;
//		addBuddies3(startPart);
		placePart(startPart,base,0);
		if (Global.numAdditionalStartParts>0 && Global.FIRST_PIECE_CHOICE == Global.FirstPieceChoice.geometry){
			addMoreStartParts();
		}
		
		boolean forceBestBuddies = true;
		useSize2 = useSize;
		double offset = 0.0;
		int itNum = 0, itF = 0, itF2 = 0, itS = 0;
		int ppartsLeft = partsLeft;
		int times = 1;
		// missPartsNum=0;
		while (partsLeft - missPartsNum > 0) {
			int i;
			for (i = 12; i > 11; i--) {
				Iterator<PoolEntry> best = nextToPlace.descendingMap().values().iterator();
				Iterator<Double> bestK = nextToPlace.descendingMap().keySet().iterator();
				if (!best.hasNext())
					continue;
				
//				double[]keyStats = getKeyStatistics(nextToPlace.get(i).descendingMap().keySet().toArray());
				
				Integer base2[] = new Integer[2];
				int[] nextI;
				Double key = 0.d;
				Double geometryKey = 0.d;
				GeometryPoolEntry GPE = null;
				PoolEntry PE = null;
				
				PE = best.next();
				nextI = PE.nextA;
				key = bestK.next();
				if (Global.useGeometryInPool){
					nextToPlaceGeometry = balanceGeometryTree();	
					Iterator<PoolEntry> bestGeometryIterator = nextToPlaceGeometry.descendingMap().values().iterator();
					Iterator<Double> bestGeometryKeyIterator = nextToPlaceGeometry.descendingMap().keySet().iterator();
					if (!bestGeometryIterator.hasNext()){continue;}
					GPE = ((GeometryPoolEntry)(bestGeometryIterator.next()));
					key = GPE.confidenceKey;
					geometryKey = bestGeometryKeyIterator.next();
					nextI[0]=GPE.nextA[0];
					nextI[1]=GPE.nextA[1];
					nextI[2]=GPE.nextA[2];
				}
				// The number of best buddies the new part will touch
				double flagc = 0; 
				boolean flag = false;
				int nextI_abs_x = get_absolute_coordinate(nextI[1]);
				int nextI_abs_y = get_absolute_coordinate(nextI[2]);
				Integer current_neighbor = null;
				//above
				current_neighbor = finalPlacement[nextI_abs_y - 1][nextI_abs_x];
				if (current_neighbor != null
						&& (bestBuddies[current_neighbor - 1][1] == nextI[0] 
						&& bestBuddies[nextI[0]][0] == current_neighbor - 1)) {
					flagc++;
				}
				//below
				current_neighbor = finalPlacement[nextI_abs_y + 1][nextI_abs_x];
				if (current_neighbor != null
						&& (bestBuddies[current_neighbor - 1][0] == nextI[0]
						&& bestBuddies[nextI[0]][1] == current_neighbor - 1)) {
					flagc++;
				}
				//on the left
				current_neighbor = finalPlacement[nextI_abs_y][nextI_abs_x - 1];
				if (current_neighbor != null
						&& (bestBuddies[current_neighbor - 1][3] == nextI[0]
						&& bestBuddies[nextI[0]][2] == current_neighbor - 1)) {
					flagc++;
				}
				//on the right
				current_neighbor = finalPlacement[nextI_abs_y][nextI_abs_x + 1];
				if (current_neighbor != null
						&& (bestBuddies[current_neighbor - 1][2] == nextI[0]
						&& bestBuddies[nextI[0]][3] == current_neighbor - 1)) {
					flagc++;
				}
				if (flagc == 0)
					flag = true; // Dov - flag: Don't place this piece because
									// there is some problem with it
				if (!forceBestBuddies || partsLeft == 1)
					flag = false;
				if (chosenParts.containsKey(nextI[0]))
					flag = true;
				base2[0] = nextI[1];
				base2[1] = nextI[2];
				if ((useSize2 && (
				get_absolute_coordinate(base2[0]) - minX + 1 > puzzleParts.nw
						|| maxX - get_absolute_coordinate(base2[0]) + 1 > puzzleParts.nw
						|| get_absolute_coordinate(base2[1]) - minY + 1 > puzzleParts.nh
						|| maxY - get_absolute_coordinate(base2[1]) + 1 > puzzleParts.nh))) {
					flag = true;
				}
				double distanceFromExpected = distanceFromExpectedByMirror(nextI[0], nextI[1], nextI[2]);//calcDistanceFromGeometricExpectedLocation(nextI[0], nextI[1], nextI[2]);//
				if (Global.useGeometricRelations) {
					if (lastPartCounter > 0 || forceBestBuddies) {
						if (distanceFromExpected > Global.geometricRelationsThreshold) {
							flag = true;
							// lastPart = nextI[0];
							if (!forceBestBuddies) {
								lastPartCounter--;
							}
						}
					}
				}
				// This if checks if there is a reason not to place this part
				// (nextI[0]).
				if ((finalPlacement[get_absolute_coordinate(base2[1])][get_absolute_coordinate(base2[0])]) != null || flag) {
					nextToPlace.remove(key);// nextToPlace.get(i).pollLastEntry();
					nextToPlaceGeometry.remove(geometryKey);
					removeFromGeometryTree(GPE); //removes from the raw list
					break;
				}
				itF--;
				
				placePart(nextI[0],base2,distanceFromExpected);

				nextToPlace.remove(key);
				nextToPlaceGeometry.remove(geometryKey);
				removeFromGeometryTree(GPE); //removes from the raw list
				if (!forceBestBuddies && partsNum / 2 > partsLeft){
					AddCandidates.addBuddies(nextI[0],this,2); //addBuddies2
				}

				if ((!forceBestBuddies && (itF < 1 || partsLeft > partsNum / 2 - 1))
						|| missPart/* && ((itF<30 && !useSize)) */) {
					forceBestBuddies = true;
				}
				break;
			}
			if (i == 11) { // (i=11 means the pool is empty)
				eliminateComp();
				// itF=1;
				if (ppartsLeft - ppartsLeft / 5 - 1 < partsLeft) {
					// itS++;
					// if((forceBestBuddies)){
					// addCandidates(true);
					// }
					// else{

					addCandidates(false);

					// break;
					// }
					forceBestBuddies = false;
					itF = Math.max(nextToPlace.size() * (partsNum - partsLeft) / partsNum / denum, 1);
					if (partsNum / 2 > partsLeft)
						itF = partsLeft / 2;
					if (missPart)
						itF = 1;
					itS += itF;
				} else
					addCandidates(true);
				// if(itS>3 && partsLeft<partsNum/2 && !missPart)
				// forceBestBuddies=false;
				itNum++;
				ppartsLeft = partsLeft;
				if (itNum > 10000)
					break;
				// break;
			}
			if (partsLeft - missPartsNum - burnPartsNum < 1 && useSize && !useSize2) {
				removeWraped();
				eliminateComp();
				addCandidates(true);
				forceBestBuddies = true;
				useSize2 = true;
			}
		}
		// cleanBad();
		System.out.println("PrL: "+ (double) (itS - puzzleParts.burnPartsNum - puzzleParts.darkPartsNum)/ partsNum);
		System.out.println("itNum: " + itNum + "# itS: " + itS);
		System.out.println("RNum: " + partsLeft);
		if (Global.GUI_DEBUG){
			guiDebug.savePuzzleStates();
		}
	}

	private void removeWraped() {
		int w = puzzleParts.nw, h = puzzleParts.nh;
		int yOff = maxY - minY + 1 - h + 1;
		yOff = Math.max(1, yOff);
		int xOff = maxX - minX + 1 - w + 1;
		xOff = Math.max(1, xOff);
		System.out.println("Tmp: " + xOff + ":" + yOff);
		TreeMap<Integer, Integer[]> op = new TreeMap<Integer, Integer[]>();
		for (int i = minY; i < minY + yOff; i++) {
			for (int j = minX; j < minX + xOff; j++) {
				Integer tmp[] = { i, j };
				op.put(countParts(i, j, h, w), tmp);
			}
		}
		Integer res[] = op.descendingMap().values().iterator().next();
		removeParts(res[1], res[0], h, w);
		minX = res[1];
		maxX = minX + w - 1;
		minY = res[0];
		maxY = minY + h - 1;
		int border[] = findBorder();
		minY = border[0];
		maxY = border[1];
		minX = border[2];
		maxX = border[3];
	}

	private void removeParts(Integer x, Integer y, int h, int w) {
		for (int i = minY; i < maxY + 1; i++) {
			for (int j = minX; j < maxX + 1; j++) {
				if (finalPlacement[i][j] != null
						&& (i < y || i > y + h - 1 || j < x || j > x + w - 1 || puzzleParts
								.isBurn(finalPlacement[i][j] - 1))) {
					chosenParts.remove(finalPlacement[i][j] - 1);
					finalPlacement[i][j] = null;
					partsLeft++;
				}
			}
		}

	}

	private Integer countParts(int i, int j, int h, int w) {
		Integer count = 0;
		for (int ii = 0; ii < h; ii++) {
			for (int jj = 0; jj < w; jj++) {
				if (finalPlacement[ii + i][jj + j] != null)
					count++;
			}
		}
		// System.out.println("C: "+count);
		return count;
	}

	private void cleanBad() {
		// double avgD=avgDiff();
		// for(int i=minY;i<maxY;i++){
		// for(int j=minX;j<maxX;j++){
		// boolean flag=false;
		// if(finalPlacement[i][j]!=null){
		// if(diffMatrix2[finalPlacement[i][j]-1][finalPlacement[i][j+1]-1][Orientation.RIGHT.ordinal()]<avgD*0.9)
		// flag=true;
		// if(diffMatrix2[finalPlacement[i][j]-1][finalPlacement[i+1][j]-1][Orientation.DOWN.ordinal()]<avgD*0.9)
		// flag=true;
		// if(flag){
		// chosenParts.remove(finalPlacement[i][j]-1);
		// finalPlacement[i][j]=null;
		// partsLeft--;
		// }
		// }
		// }
		// }

	}

	// private double avgDiff() {
	// double avgD=0;
	// int counter=0;
	// for(int i=minY;i<maxY;i++){
	// for(int j=minX;j<maxX;j++){
	// if(finalPlacement[i][j]!=null){
	// avgD+=diffMatrix2[finalPlacement[i][j]-1][finalPlacement[i][j+1]-1][Orientation.RIGHT.ordinal()];
	// avgD+=diffMatrix2[finalPlacement[i][j]-1][finalPlacement[i+1][j]-1][Orientation.DOWN.ordinal()];
	// avgD+=diffMatrix2[finalPlacement[i][j+1]-1][finalPlacement[i][j]-1][Orientation.LEFT.ordinal()];
	// avgD+=diffMatrix2[finalPlacement[i+1][j]-1][finalPlacement[i][j]-1][Orientation.UP.ordinal()];
	// counter+=2;
	// }
	// }
	// }
	// avgD/=counter;
	// System.out.println("avgD: "+avgD/2);
	// return avgD;
	// }

	private void addCandidates(boolean flag) { // Dov: flag= if true choose
												// addbuddies else choose
												// addbuddies2 in the
												// addCandidate class
		if (maxX - minX > 5) {
			Thread[] threads = new Thread[maxY - minY + 1];
			for (int i = 0; i < maxY - minY + 1; i++) {
				threads[i] = new Thread(new AddCandidates(i+minY,flag,this));
				threads[i].start();

			}
			for (int i = 0; i < maxY - minY + 1; i++) {
				try {
					threads[i].join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			for (int i = minY; i < maxY + 1; i++) {

				for (int j = minX; j < maxX + 1; j++) {
					if (finalPlacement[i][j] != null) {
						if (finalPlacement[i - 1][j] == null
								|| finalPlacement[i + 1][j] == null
								|| finalPlacement[i][j - 1] == null
								|| finalPlacement[i][j + 1] == null) {
							AddCandidates.addBuddies(finalPlacement[i][j] - 1, this,(flag?1:2));
						}
					}
				}
			}
		}
	}

	private void eliminateComp() {// Dov - Recalculate the diff and conf
									// matrices after the pool got empty
		for (int i = 0; i < partsNum; i++) {
			for (int j = 0; j < partsNum; j++) {
				if (chosenParts.containsKey(i) && chosenParts.containsKey(j)) {
					diffMatrix[0][i][j] = Global.MAX_FLOAT;
					diffMatrix[1][i][j] = Global.MAX_FLOAT;
					diffMatrix[2][i][j] = Global.MAX_FLOAT;
					diffMatrix[3][i][j] = Global.MAX_FLOAT;
					//Dov - added the other direction
					diffMatrix[1][j][i] = Global.MAX_FLOAT;
					diffMatrix[0][j][i] = Global.MAX_FLOAT;
					diffMatrix[3][j][i] = Global.MAX_FLOAT;
					diffMatrix[2][j][i] = Global.MAX_FLOAT;
					
				} else if (chosenParts.containsKey(j)) {
					int nextA[] = new int[2]; // Dov - notice here nextA has
												// only size 2, these are the x
												// and y coordinates
					int nextA_absolute[] = new int[2];
					nextA[0] = chosenParts.get(j)[0];
					nextA[1] = chosenParts.get(j)[1];
					int nextA_abs_x = get_absolute_coordinate(nextA[0]);
					int nextA_abs_y = get_absolute_coordinate(nextA[1]);
					//above
					if (finalPlacement[nextA_abs_y - 1][nextA_abs_x] != null){
						diffMatrix[1][i][j] = Global.MAX_FLOAT;
						diffMatrix[0][j][i] = Global.MAX_FLOAT; //Dov added the other direction
						
					}
					//below
					if (finalPlacement[nextA_abs_y + 1][nextA_abs_x] != null){
						diffMatrix[0][i][j] = Global.MAX_FLOAT;
						diffMatrix[1][j][i] = Global.MAX_FLOAT; //Dov added the other direction
						
					}
					//on the left
					if (finalPlacement[nextA_abs_y][nextA_abs_x - 1] != null){
						diffMatrix[3][i][j] = Global.MAX_FLOAT;
					
						diffMatrix[2][j][i] = Global.MAX_FLOAT; //Dov added the other direction
					}
					//on the right
					if (finalPlacement[nextA_abs_y][nextA_abs_x + 1] != null){
						diffMatrix[2][i][j] = Global.MAX_FLOAT;
						
						diffMatrix[3][j][i] = Global.MAX_FLOAT; //Dov added the other direction
						
					}
				}
			}
		}
		calcConfMatrix();
		// clean();
		// findBestStart(false);
	}

	private void clean() {
		// bestBuddiesTree=new ArrayList<HashMap<Integer,Double>>();
		nextToPlace = new TreeMap<Double, PoolEntry>();
	}

	private void findBestStart(boolean first) {
		
		if (Global.FIRST_PIECE_CHOICE == Global.FirstPieceChoice.saliency){
			double maxSaliency = 0;
			for (int i = 0; i < partsNum; i++) {
				if (puzzleParts.puzzlePartsAverageSaliency[i] > maxSaliency) {
					maxSaliency = puzzleParts.puzzlePartsAverageSaliency[i];
					startPart = i;
				}
			}
			return;
		}
		Random r = new Random();
		TreeMap<Double, Integer> tm = new TreeMap<Double, Integer>();
		TreeMap<Double, Integer> tmRelations = new TreeMap<Double, Integer>();
		TreeMap<Double, Integer> tmCompromise = new TreeMap<Double, Integer>();
		TreeMap<Double, Integer> tmRelationsCompromise = new TreeMap<Double, Integer>();
		TreeMap<Double, Integer> tmRelationsAllParts = new TreeMap<Double, Integer>();
		double count = 0, TP = 0;
		Double[]averageConf = new Double[partsNum];
		int nh = puzzleParts.nh, nw = puzzleParts.nw;
		for (int i = 0; i < partsNum; i++) {
			budddiesNumber[i] = 0;
			Double avgConf = 0.0;
			for (int or = 0; or < 4; or++) {
				int op = (or % 2 == 0) ? (or + 1) : (or - 1);
				if (bestBuddies[i][or] == -1)
					continue;
				avgConf += confMatrix[or][i][bestBuddies[i][or]] / 2
						+ confMatrix[op][bestBuddies[i][or]][i] / 2;
				if (i == bestBuddies[bestBuddies[i][or]][op]) {
					budddiesNumber[i] += 25;
					count++;
					switch (or) {
					case 0:
						if (i - nw == bestBuddies[i][or])
							TP++; // TP = True Positive
						break;
					case 1:
						if (i + nw == bestBuddies[i][or])
							TP++;
						break;
					case 2:
						if (i - 1 == bestBuddies[i][or])
							TP++;
						break;
					case 3:
						if (i + 1 == bestBuddies[i][or])
							TP++;
						break;

					}
				}
				if (bestBuddies[bestBuddies[i][or]][or] == -1)
					continue;
				if (first
						&& bestBuddies[i][or] > -1
						&& bestBuddies[bestBuddies[i][or]][or] > -1
						&& bestBuddies[i][or] == bestBuddies[bestBuddies[bestBuddies[i][or]][or]][op])
					budddiesNumber[i] += 5; // i's best neighbor has a best
											// buddy in the same direction
				if (first
						&& bestBuddies[i][or] > -1
						&& bestBuddies[bestBuddies[i][or]][or] > -1
						&& bestBuddies[bestBuddies[bestBuddies[i][or]][or]][or] > -1
						&& bestBuddies[bestBuddies[i][or]][or] == bestBuddies[bestBuddies[bestBuddies[bestBuddies[i][or]][or]][or]][op])
					budddiesNumber[i]++; // i's best neighbor has a best
											// neighbor who has a best buddy, all
											// in the same dircetion
			}
			avgConf /= 4;
			averageConf[i] = avgConf;
//			boolean loopConstraint = (bestBuddies[bestBuddies[bestBuddies[bestBuddies[i][0]][3]][1]][2] == i
//					&& bestBuddies[bestBuddies[bestBuddies[bestBuddies[i][3]][1]][2]][0] == i
//					&& bestBuddies[bestBuddies[bestBuddies[bestBuddies[i][1]][2]][0]][3] == i 
//					&& bestBuddies[bestBuddies[bestBuddies[bestBuddies[i][2]][0]][3]][1] == i);
//			loopConstraint: Best neighbors in a clockwise circle starting at i and at all 4 orientations
//			if (loopConstraint){
//				budddiesNumber[i] += Global.FIRST_PIECE_LOOP_CONSTRAINT_VALUE;
//			}
			
			if(bestBuddies[bestBuddies[bestBuddies[bestBuddies[i][0]][3]][1]][2] == i){
				budddiesNumber[i] += Global.FIRST_PIECE_LOOP_CONSTRAINT_VALUE/4;
			}
			if(bestBuddies[bestBuddies[bestBuddies[bestBuddies[i][3]][1]][2]][0] == i){
				budddiesNumber[i] += Global.FIRST_PIECE_LOOP_CONSTRAINT_VALUE/4;
			}
			if(bestBuddies[bestBuddies[bestBuddies[bestBuddies[i][1]][2]][0]][3] == i){
				budddiesNumber[i] += Global.FIRST_PIECE_LOOP_CONSTRAINT_VALUE/4;
			}
			if(bestBuddies[bestBuddies[bestBuddies[bestBuddies[i][2]][0]][3]][1] == i){
				budddiesNumber[i] += Global.FIRST_PIECE_LOOP_CONSTRAINT_VALUE/4;
			}
			
//			if (budddiesNumber[i] > 119 && loopConstraint) {
			//		tm.put(4 * avgConf, i);
			//		if (Global.FIRST_PIECE_CHOICE == Global.FirstPieceChoice.geometry){
			//			tmRelations.put(geometricRelations.numOfRelations[i]+r.nextDouble(), i);
			//		}
//				}
			//	if (budddiesNumber[i] > 119 || loopConstraint) {
			//		tmCompromise.put(4 * avgConf, i);
			//		if (Global.FIRST_PIECE_CHOICE == Global.FirstPieceChoice.geometry){
			//			tmRelationsCompromise.put(geometricRelations.numOfRelations[i]+r.nextDouble(),i);
			//		}
			//	}
			
			//Add the part to the tree that contains all parts for forcing the best geometric relations and disregarding the
			//Compatibility scores
			if (Global.FIRST_PIECE_CHOICE == Global.FirstPieceChoice.geometry){
				tmRelationsAllParts.put(geometricRelations.numOfRelations[i]+r.nextDouble(),i);
			}
		}
		int threshold = 119 + Global.FIRST_PIECE_LOOP_CONSTRAINT_VALUE;
		while(threshold>0){
			boolean foundPiece = false;
			for (int i = 0; i < partsNum; i++) {		
				if (budddiesNumber[i] > threshold) {
					foundPiece = true;
					tm.put(4 * averageConf[i], i);
					if (Global.FIRST_PIECE_CHOICE == Global.FirstPieceChoice.geometry){
						tmRelations.put(geometricRelations.numOfRelations[i]+r.nextDouble(), i);
						if (Global.MIN_GEOMETRIC_RELATIONS_FOR_START_PART > geometricRelations.numOfRelations[i]){
							foundPiece = false;
						}
					}
				}
			}
			if (foundPiece || !Global.DYNAMIC_THRESHOLD_AT_FIRST_PIECE_SELECTION){
				System.out.println("First Piece Threshold = "+threshold);
				break;
			}
			threshold--;
		}
		
		System.out.println("Pr: " + (count + 4 * puzzleParts.burnPartsNum + 4 * puzzleParts.darkPartsNum) / ((nh - 1) * (nw) + (nw - 1) * (nh)) / 2);
		System.out.println("BB: " + count / 2 + " TP: " + TP / 2 + " NOB: " + ((nh - 1) * (nw) + (nw - 1) * (nh)));
		if (first) { //Genady's parameter to the function. Probably was used to signify if this was the first time searching for the first piece or if this is a
					//a separate puzzle (from the mixed puzzles feature of his code)
			Iterator<Integer> ordIt = null;
			Iterator<Integer> ordItCompromise = null;
			switch (Global.FIRST_PIECE_CHOICE){
			case geometry:
				ordIt = tmRelations.descendingMap().values().iterator();
				ordItCompromise = tmRelationsCompromise.descendingMap().values().iterator();
				startPartRelationsTree = tmRelations;
				break;
			case normal:
				ordIt=tm.descendingMap().values().iterator();
				ordItCompromise=tmCompromise.descendingMap().values().iterator();
				break;
			default:
				System.out.println("No first piece choice type selected");
				return;
			}

			if (ordIt.hasNext()) {
				System.out.println("Good first part");
				startPart = ordIt.next();
			} else {
				if (ordItCompromise.hasNext()) {
					System.out.println("Compromising first part");
					startPart = ordItCompromise.next();
					if (Global.FIRST_PIECE_CHOICE == Global.FirstPieceChoice.geometry){
						startPartRelationsTree = tmRelationsCompromise;
					}
				}
			}
			//Force more use of geometry:
			if(Global.FIRST_PIECE_CHOICE == Global.FirstPieceChoice.geometry){
				System.out.println("Startpart has "+geometricRelations.numOfRelations[startPart] + " relations");
				if (geometricRelations.numOfRelations[startPart] < Global.MIN_GEOMETRIC_RELATIONS_FOR_START_PART){
					System.out.println("The start part "+startPart+" has only "+geometricRelations.numOfRelations[startPart]+" relations");
					int better_geometry_startPart =  tmRelationsAllParts.get(tmRelationsAllParts.lastKey());
					if (geometricRelations.numOfRelations[better_geometry_startPart] >= Global.MIN_GEOMETRIC_RELATIONS_FOR_START_PART){
						startPart = better_geometry_startPart;
						System.out.println("Changing start part to "+startPart +" which has "+geometricRelations.numOfRelations[startPart]+" relations");
					}
				}
				if (Global.numAdditionalStartParts > startPartRelationsTree.size()){
					System.out.println("The startPart tree has only "+startPartRelationsTree.size()+" elements, adding all parts to the tree");
					startPartRelationsTree = tmRelationsAllParts;
				}
			}
		}
	}

	private Double findEntropy(int i) {
		BufferedImage pimg = puzzleParts.getRGBPart(i);
		int hist[] = find_hist(pimg, 16);
		Double res1 = 0.0, res2 = 0.0, res3 = 0.0;
		for (int j = 0; j < 16; j++) {
			double h1 = hist[j];
			double h2 = hist[j + 16];
			double h3 = hist[j + 32];
			if (h1 != 0)
				res1 += (-Math.log10(h1 / partSize / partSize) * (h1 / partSize / partSize));
			if (h2 != 0)
				res2 += (-Math.log10(h2 / partSize / partSize) * (h2 / partSize / partSize));
			if (h3 != 0)
				res3 += (-Math.log10(h3 / partSize / partSize) * (h3 / partSize / partSize));
		}
		return (res1 + res2 + res3) / 3;
	}

	private int[] find_hist(BufferedImage pimg, int bins) {
		int hist[] = new int[3 * bins];
		for (int i = 0; i < bins; i++) {
			hist[i] = 0;
		}

		for (int i = 0; i < partSize; i++) {
			for (int j = 0; j < partSize; j++) {
				int RGB = pimg.getRGB(i, j);
				Color c = new Color(RGB);
				int B = c.getBlue();
				hist[B / bins]++;
				int G = c.getGreen();
				hist[G / bins + bins]++;
				int R = c.getRed();
				hist[R / bins + 2 * bins]++;
			}
		}
		return hist;
	}

	private void calcConfMatrix() {
		Thread[] threads = new Thread[4];
		for (Orientation or : Orientation.values()) {
			threads[or.ordinal()] = new Thread(
				new CalcConfMatrix(diffMatrix, confMatrix, bestBuddies, partSize, partsNum, partsLeft - missPartsNum,
				or, puzzleParts, missPart, chosenParts));
			threads[or.ordinal()].start();
		}

		for (Orientation or : Orientation.values()) {

			try {
				threads[or.ordinal()].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (!firstConfCalc){
			modifyConfMatrix();
		}
	}
	
	//If modifyConf is True, this method will use the matlab hole filling
	//to modify the confidence values between parts. it is called everytime the confMatrix is re-calculated
	public void modifyConfMatrix(){
		if (!Global.modifyConf || Global.no_matlab){
			return;
		}
		float[]trueNeighborFindings;
		mhf.updateDiffMat(diffMatrix);
		for (int part=0; part<partsNum; part++){
			for (Orientation or : Orientation.values()){
				int initialBestNeighbor = bestBuddies[part][or.ordinal()];
				if (initialBestNeighbor == -1){
					continue;
				}
				int oldRank = ranks[part][or.ordinal()];
				float oldConf = confMatrix[or.ordinal()][part][initialBestNeighbor]; 
				if (confMatrix[or.ordinal()][part][initialBestNeighbor] > Global.MOD_CONF_BASELINE){
//					System.out.println("Didn't modify confidence of part " + part + ", or "+or+ " with initial neighbor "+initialBestNeighbor+ ". oldConf =" +oldConf);
					continue;
				}else if(oldRank >= Global.BEAM_SIZE && firstConfCalc){
//					System.out.println("Didn't modify confidence of part " + part + ", or "+or+ " with initial neighbor "+initialBestNeighbor+ ". oldRank =" +oldRank);
					continue;
				}
				trueNeighborFindings = mhf.checkPartForTrueNeighbor(part, or,oldRank);
				int neighbor = (int)trueNeighborFindings[0];
				if (neighbor == -1){
//					System.out.println("Didn't modify confidence of part " + part + ", or "+or+ " because the caseCheck failed");
				}else{
					float newConf = Global.MOD_CONF_BASELINE - 1 + trueNeighborFindings[1];
					int op = or.opposite().ordinal();
					System.out.println("Modified Confidence of part " + part + " with part " + neighbor + " at or "+or + " (oldRank "+oldRank+") from "+ confMatrix[or.ordinal()][part][neighbor]+" to "+ newConf);
					confMatrix[or.ordinal()][part][neighbor] = newConf;
					confMatrix[op][neighbor][part] = newConf;
					bestBuddies[part][or.ordinal()] = neighbor;
					
					//Force a best buddy from the other side? Should only be used if the mistake probability is very small
					//And this may not be the case with a small beam size and small puzzle pieces
//					bestBuddies[neighbor][op] = part;
				}
			}
		}
		firstConfCalc = false;
	}

	private void calcDiffMatrix() {
		Orientation arr[] = new Orientation[2];
		arr[0] = Orientation.values()[0];
		arr[1] = Orientation.values()[2];
		int thNum = 1;// partsNum/400;//partsNum/(partsNum/500+1);
		int itNum = partsNum / thNum;
		Thread[] threads = new Thread[2 * thNum * thNum + thNum + 1];
		// System.out.println("THNUM:"+thNum+" || Jump:"+itNum);
		// threads = new Thread[2*2*partsNum];
		for (Orientation or : arr) {
			int num = (or.ordinal() == 0) ? (0) : (1);
			for (int i = 0; i < partsNum; i += itNum) {
				for (int j = 0; j < partsNum; j += itNum) {
					if (Global.sizeOverlap > 0) {
						threads[num * thNum * thNum + i / itNum * thNum + j
								/ itNum] = new Thread(new CalcDiff(diffMatrix,
								partSize, partsNum, puzzleParts, or, i, j,
								itNum));
					} else {
						threads[num * thNum * thNum + i / itNum * thNum + j
								/ itNum] = new Thread(new CalcDiffOld(
								diffMatrix, partSize, partsNum, puzzleParts,
								or, i, j, itNum));
					}
					// threads[2*num*partsNum+2*i+1]=new Thread(new
					// CalcDiff(diffMatrix,partSize,partsNum, puzzleParts, or,
					// i,1,0));
					threads[num * thNum * thNum + i / itNum * thNum + j / itNum]
							.start();
					// threads[2*num*partsNum+2*i+1].start();

				}
			}
		}

		for (Orientation or : arr) {
			int num = (or.ordinal() == 0) ? (0) : (1);
			for (int i = 0; i < partsNum; i += itNum) {
				for (int j = 0; j < partsNum; j += itNum) {
					try {
						threads[num * thNum * thNum + i / itNum * thNum + j
								/ itNum].join();
						// threads[2*num*partsNum+2*i+1].join();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void findFarBuddies() {// Dov-Method to find "best buddies" that are
									// not neighbors in the original picture
		int offset = 0;
		Random r = new Random();
		for (int or = 0; or < 4; or++) {
			int op = (or % 2 == 0 ? or + 1 : or - 1);
			for (int i = 0; i < partsNum; i++) {
				for (int j = 0; j < partsNum; j++) {
					if (op == 0) {
						offset = -puzzleParts.nw;
					}
					if (op == 1) {
						offset = puzzleParts.nw;
					}
					if (op == 2) {
						offset = -1;
					}
					if (op == 3) {
						offset = 1;
					}
					if (bestBuddies[i][or] == j && bestBuddies[j][op] == i
							&& (i != j + offset)) {
						// System.out.println(i+","+j+", op= "+
						// op+" offset="+offset);
						Color c = new Color(r.nextInt(256), r.nextInt(256),
								r.nextInt(256));
						puzzleParts.colorFrame(i, c, 1, or);
						puzzleParts.colorFrame(i, c, 2, or);
						puzzleParts.colorFrame(j, c, 1, op);
						puzzleParts.colorFrame(j, c, 2, op);
					}
				}
			}
		}
	}

	public double calcDistanceFromGeometricExpectedLocation(int nextPartName,double nextX, double nextY) {
		if (!Global.useGeometricRelations) {
			return -1;
		}
		double sumAllX = 0;
		double sumAllY = 0;
		double sumAllFactors = 0;
		double reliabilitySquareDistanceSum = 0;
		int numValidDistnaces = 0;
		for (int currentPartName = 0; currentPartName < partsNum; currentPartName++) {
			if (!chosenParts.containsKey(currentPartName)) {
				continue;
			}
			Integer[] currentPart = chosenParts.get(currentPartName);
			double currentX = currentPart[0].intValue();
			double currentY = currentPart[1].intValue();
			double reliabilitySquareDistnace = (currentX - nextX)
					* (currentX - nextX) + (currentY - nextY)
					* (currentY - nextY);
			
			for (int sourceNum = 0; sourceNum < geometricRelations.numSources; sourceNum++) {
				if (geometricRelations.data[currentPartName][nextPartName][sourceNum][0] != geometricRelations.nullValue &&
						geometricRelations.data[currentPartName][nextPartName][sourceNum][2]<=Global.geometricRelationsMirrorThreshold) {
					reliabilitySquareDistanceSum += reliabilitySquareDistnace;
					numValidDistnaces++;
				}
			}
		}
		if (numValidDistnaces == 0) {
			return -1;
		}
		double reliabilitySquareDistanceMean = reliabilitySquareDistanceSum
				/ numValidDistnaces;

		for (int currentPartName = 0; currentPartName < partsNum; currentPartName++) {
			if (!chosenParts.containsKey(currentPartName)) {
				continue;
			}
			Integer[] currentPart = chosenParts.get(currentPartName);
			double currentX = currentPart[0].intValue();
			double currentY = currentPart[1].intValue();
			double sumX = 0;
			double sumY = 0;
			int validSources = 0;
			
			for (int sourceNum = 0; sourceNum < geometricRelations.numSources; sourceNum++) {
				if (geometricRelations.data[currentPartName][nextPartName][sourceNum][0] == geometricRelations.nullValue||
					geometricRelations.data[currentPartName][nextPartName][sourceNum][2]>Global.geometricRelationsMirrorThreshold) {
					continue;
				}
				sumX += currentX
						+ geometricRelations.data[currentPartName][nextPartName][sourceNum][0];
				sumX += currentX
						- geometricRelations.data[nextPartName][currentPartName][sourceNum][0];
				sumY += currentY
						+ geometricRelations.data[currentPartName][nextPartName][sourceNum][1];
				sumY += currentY
						- geometricRelations.data[nextPartName][currentPartName][sourceNum][1];
				validSources++;
			}
			if (validSources == 0) {
				continue;
			}
			double reliabilitySquareDistnace = (currentX - nextX)
					* (currentX - nextX) + (currentY - nextY)
					* (currentY - nextY);
			double factor = Math
					.pow(Math.E,
							-(reliabilitySquareDistnace / reliabilitySquareDistanceMean));
			sumAllFactors += factor * 2 * validSources;
			double currentExpectedX = factor * sumX;
			double currentExpectedY = factor * sumY;
			sumAllX += currentExpectedX;
			sumAllY += currentExpectedY;
		}
		double expectedX = sumAllX / sumAllFactors;
		double expectedY = sumAllY / sumAllFactors;

		double distanceFromExpected = Math.sqrt((expectedX - nextX)
				* (expectedX - nextX) + (expectedY - nextY)
				* (expectedY - nextY));
		return distanceFromExpected;
	}
	
	public double distanceFromExpectedByMirror(int nextPartName,
			double nextX, double nextY) {
		if (!Global.useGeometricRelations) {
			return -1;
		}
		double sumAllX = 0;
		double sumAllY = 0;
		double sumAllFactors = 0;
		double mirrorDifferenceSum = 0;
		int numValidDistnaces = 0;
		for (int currentPartName = 0; currentPartName < partsNum; currentPartName++) {
			if (!chosenParts.containsKey(currentPartName)) {
				continue;
			}			
			for (int sourceNum = 0; sourceNum < geometricRelations.numSources; sourceNum++) {
				double mirrorDifference = geometricRelations.data[currentPartName][nextPartName][sourceNum][2];
				if (geometricRelations.data[currentPartName][nextPartName][sourceNum][0] != geometricRelations.nullValue &&
						geometricRelations.data[currentPartName][nextPartName][sourceNum][2]<=Global.geometricRelationsMirrorThreshold) {
					mirrorDifferenceSum += mirrorDifference;
					numValidDistnaces++;
				}
			}
		}
		if (numValidDistnaces == 0) {
			return -1;
		}
		double mirrorDifferenceMean = mirrorDifferenceSum/ numValidDistnaces;

		for (int currentPartName = 0; currentPartName < partsNum; currentPartName++) {
			if (!chosenParts.containsKey(currentPartName)) {
				continue;
			}
			Integer[] currentPart = chosenParts.get(currentPartName);
			double currentX = currentPart[0].intValue();
			double currentY = currentPart[1].intValue();
			
			for (int sourceNum = 0; sourceNum < geometricRelations.numSources; sourceNum++) {
				if (geometricRelations.data[currentPartName][nextPartName][sourceNum][0] == geometricRelations.nullValue||
					geometricRelations.data[currentPartName][nextPartName][sourceNum][2]>Global.geometricRelationsMirrorThreshold) {
					continue;
				}

				double mirrorDifference = geometricRelations.data[currentPartName][nextPartName][sourceNum][2];
				double factor = Math.pow(Math.E,-(mirrorDifference / mirrorDifferenceMean));
				sumAllFactors += factor;
				double currentExpectedX = currentX+0.5*(geometricRelations.data[currentPartName][nextPartName][sourceNum][0]-geometricRelations.data[nextPartName][currentPartName][sourceNum][0]);
				double currentExpectedY = currentY+0.5*(geometricRelations.data[currentPartName][nextPartName][sourceNum][1]-geometricRelations.data[nextPartName][currentPartName][sourceNum][1]);
				sumAllX += factor * currentExpectedX;
				sumAllY += factor * currentExpectedY;
			}
		}
		double expectedX = sumAllX / sumAllFactors;
		double expectedY = sumAllY / sumAllFactors;

		double distanceFromExpected = Math.sqrt((expectedX - nextX)
				* (expectedX - nextX) + (expectedY - nextY)
				* (expectedY - nextY));
		return distanceFromExpected;
	}
	
	public void addToGeometryTree(PoolEntry entry, double confidenceKey) {
		if (!Global.useGeometryInPool) {
			return;
		}
		synchronized (nextToPlaceGeometryRaw){
			double distanceFromExpected = distanceFromExpectedByMirror(entry.nextA[0], entry.nextA[1], entry.nextA[2]);//calcDistanceFromGeometricExpectedLocation(nextPart[0], nextPart[1], nextPart[2]);//
//			double[] geometryData = new double[5];
//			geometryData[0] = entry.nextA[0];
//			geometryData[1] = entry.nextA[1];
//			geometryData[2] = entry.nextA[2];
//			geometryData[3] = confidenceKey;
//			geometryData[4] = distanceFromExpected;
			GeometryPoolEntry GPE = new GeometryPoolEntry(entry, confidenceKey, distanceFromExpected);
			if (distanceFromExpected != -1) {
				synchronized (distanceFromExpectedAll){
					distanceFromExpectedAll +=distanceFromExpected;
					numDistances++;
				}
			}
			if (!nextToPlaceGeometryRaw.contains(GPE))
				nextToPlaceGeometryRaw.add(GPE); 
		}
	}
	public TreeMap<Double, PoolEntry> balanceGeometryTree(){
		TreeMap<Double,PoolEntry> newGeometryMap = new TreeMap<Double,PoolEntry>();
		Double newKey;
		
//		Iterator<double[]> it = nextToPlaceGeometry.descendingMap().values().iterator();
//		while (it.hasNext()){ //Get entries from the old geometry map
//			entry = it.next();
//			newKey = calcMergedKey(entry);
//			newGeometryMap.put(newKey, entry);
//		}
		for (int i=0; i<nextToPlaceGeometryRaw.size(); i++){ //Get entries from the raw geometry list
			GeometryPoolEntry GPE = nextToPlaceGeometryRaw.get(i);
			synchronized(distanceFromExpectedAll){
				if (numDistances <= 1){ //No geometry available
					newKey = GPE.confidenceKey;
				}else{
					newKey = calcMergedKey(GPE.confidenceKey, GPE.distanceFromExpected);
				}
			}
			newGeometryMap.put(newKey, GPE);
		}
		return newGeometryMap;
	}
	
	public Double calcMergedKey(double confidenceKey, double distanceFromExpected){
		if (distanceFromExpected == -1){ 
			distanceFromExpected = distanceFromExpectedAll.doubleValue()/numDistances;
		}
		double geometryKey = 1/(Global.geometryKeyConstant + distanceFromExpected);
		Double newKey = geometryKey*confidenceKey;
		return newKey;
	}
	
	public void removeFromGeometryTree(GeometryPoolEntry entry){
		if (!Global.useGeometryInPool) {
			return;
		}
		synchronized (nextToPlaceGeometryRaw){
			if (entry != null && nextToPlaceGeometryRaw.remove(entry)){
				double distanceFromExpected = entry.distanceFromExpected;
				if (distanceFromExpected != -1){
					synchronized (distanceFromExpectedAll){
						distanceFromExpectedAll -=distanceFromExpected;
						numDistances--;
					}
				}
			}
		}
	}

	public boolean checkForMistakes(int lastPlacedPart,Integer[] lastPartCoords) {
		boolean mistake = false;
		Integer[] startPartCoords = chosenParts.get(startPart);
		int startPartX = startPart % puzzleParts.nw;
		int startPartY = startPart / puzzleParts.nw;
		int partX = lastPlacedPart % puzzleParts.nw;
		int partY = lastPlacedPart / puzzleParts.nw;
		if (startPartX - partX != startPartCoords[0] - lastPartCoords[0]
				|| startPartY - partY != startPartCoords[1] - lastPartCoords[1]) {
			mistake = true;
		}
		return mistake;
	}
	
	public double[]getKeyStatistics(Object[]arr){//0-number of elemets, 1-average,   2-std
		double[] result = new double[3];
		result[0] = arr.length;
		for(int i=0; i<arr.length; i++){
			result[1] = result[1] + ((Double)arr[i]).doubleValue();
		}
		result[1] = result[1]/result[0];
		for(int i=0; i<arr.length; i++){
			result[2] = result[2] + Math.pow(((Double)arr[i]).doubleValue()-result[1], 2);
		}
		result[2] = Math.sqrt(result[2]/result[0]);
		return result;
	}
	
	public void placePartInGui(int partToBePlaced, Integer[]base2){
		Iterator<Double> keysIterator;
		Iterator<PoolEntry> valuesIterator;
		if (Global.useGeometryInPool){
			keysIterator = nextToPlaceGeometry.descendingKeySet().iterator();
			valuesIterator = nextToPlaceGeometry.descendingMap().values().iterator();
		}else{
			keysIterator = nextToPlace.descendingKeySet().iterator();
			valuesIterator = nextToPlace.descendingMap().values().iterator();
		}
		guiDebug.updateCandidatePool(valuesIterator, keysIterator, bestBuddies);
		guiDebug.step(partToBePlaced, base2[0], base2[1]);
	}
	
	public void placePart(int partToBePlaced, Integer[] partLocation, double distanceFromExpected){
		if (Global.GUI_DEBUG){
			placePartInGui(partToBePlaced, partLocation);
		}
		if (Global.beVerbose){printPoolOfCandidates(10);}
		chosenParts.put(partToBePlaced, partLocation);
		finalPlacement[partLocation[1] + boardCenter][partLocation[0] + boardCenter] = partToBePlaced + 1;
		maxX = Math.max(maxX, partLocation[0] + boardCenter);
		minX = Math.min(minX, partLocation[0] + boardCenter);
		maxY = Math.max(maxY, partLocation[1] + boardCenter);
		minY = Math.min(minY, partLocation[1] + boardCenter);
		partsLeft--;
		int stage = partsNum - partsLeft;
		if (Global.beVerbose) {
			System.out.println("Stage " + stage + ": Placing part " + partToBePlaced
								+ ", Distance from expected = " + distanceFromExpected);
		}
		if (!mistake && checkForMistakes(partToBePlaced,partLocation)) {
			if (Global.beVerbose){
				System.out.println();
				System.out.println("A mistake was made!!!");
				System.out.println();
			}
			mistake = true;
			if (Global.GUI_DEBUG){guiDebug.mistake(false);}
		}
		if (stage >= Global.intermediateOutput[0] && stage <= Global.intermediateOutput[1]) {
			intermediateOutput(stage,partToBePlaced,partLocation);
		}
//		eliminateComp();
		AddCandidates.addBuddies(partToBePlaced, this, 3);
	}
	
	public void addMoreStartParts(){
		Iterator<Integer> it = startPartRelationsTree.descendingMap().values().iterator();
		if (it.hasNext()){
			it.next(); //remove the first part (the startPart);
		}
		double th = Global.geometricRelationsStartPartThreshold;
		for (int i=0; i<Global.numAdditionalStartParts; i++){
			boolean placedPart = false;
			while (!placedPart && it.hasNext()){
				int part = it.next().intValue();
				System.out.println("Trying to add part "+ part);
				int validSources = 0;
				double diffX = 0;
				double diffY = 0;
				int diffXRounded = 0;
				int diffYRounded = 0;
				for (int source=0; source<geometricRelations.numSources; source++){
					if (geometricRelations.data[startPart][part][source][0]!= geometricRelations.nullValue &&
							geometricRelations.data[startPart][part][source][2] < Global.geometricRelationsStartPartMirrorThreshold){
						
						diffX += (geometricRelations.data[startPart][part][source][0] - geometricRelations.data[part][startPart][source][0])/2;
						diffY += (geometricRelations.data[startPart][part][source][1] - geometricRelations.data[part][startPart][source][1])/2;
						validSources++;
					}
				}
				if (validSources > 0){
					diffX = diffX/validSources;
					diffY = diffY/validSources;
					diffXRounded = (int)Math.round(diffX);
					diffYRounded = (int)Math.round(diffY);
					double distanceFromExpected = distanceFromExpectedByMirror(part,diffXRounded,diffYRounded);
					if ((Math.abs(diffXRounded - diffX) < th) &&
							(Math.abs(diffYRounded - diffY) < th) && 
							distanceFromExpected < Global.geometricRelationsThreshold){
						Integer[] base ={diffXRounded,diffYRounded};
						placePart(part,base,distanceFromExpected);
						placedPart = true;
					}else
						System.out.println("Addition failed, part "+part+"doesn't comply with already placed parts");
				}else{
					System.out.println("Addition failed, no valid sources for part "+part);
				}
			}
		}
		System.out.println("Finished adding additional parts");
	}
	
	public void printPoolOfCandidates(int scope){
		System.out.println("Candidate Pool of size "+nextToPlace.size()+":");
		Iterator<Double> it = nextToPlace.descendingKeySet().iterator();
		for (int i=0; i<scope; i++){
			if (!it.hasNext()){
				break;
			}else{
				Double key = it.next();
				int[] part = nextToPlace.get(key).nextA;
				System.out.println(i +": key:"+key+" part:"+part[0]+" Coordinates:"+part[1]+","+part[2]);
			}
		}
	}
	
	public int getPart(int x, int y){
		Integer part = finalPlacement[y+boardCenter][x+boardCenter];
		if (part == null)
			return -1;
		else{
			return part.intValue() - 1;
		}
	}
	public int getPart(int[]coords){
		return getPart(coords[0],coords[1]);
	}
	public int getNeighbor(int x, int y, Orientation or){
		int offsetX = 0, offsetY = 0;
		switch(or){
		case UP:
			offsetY = -1;
			break;
		case DOWN:
			offsetY = 1;
			break;
		case LEFT:
			offsetX = -1;
			break;
		case RIGHT:
			offsetX = 1;
			break;
		default:
			break;
		}
		return getPart(x+offsetX,y+offsetY);
	}
	
	public boolean isBestNeighbor(int part1,int part2, Orientation or){
		if (part1 == -1 || part2 == -1){return false;}
		return bestBuddies[part1][or.ordinal()] == part2;
	}
	public boolean areBestBuddies(int part1, int part2, Orientation or){
		return isBestNeighbor(part1,part2,or) && isBestNeighbor(part2,part1,or.opposite());
	}
	
	public int countBestNeighbors(int[] newPart, Orientation or){
		int part = newPart[0];
		int x = newPart[1];
		int y = newPart[2];
		int result = 0;
		int part2 = getNeighbor(x,y,or);
		if (isBestNeighbor(part, part2, or)){
			result++;
		}
//		part2 = getNeighbor(x,y,or.opposite());
//		if (isBestNeighbor(part2,part,or)){
//			result++;
//		}
		if (isBestNeighbor(part2, part, or.opposite())){
			result++;
		}
		return result;
	}
	
	public int get_absolute_coordinate(int relative_coordinate){
		return relative_coordinate + boardCenter;
	}
	
//	private Iterator<Integer[]> getNormalValuesByGeometryIterator(Iterator<double[]> geometryKeys){
//		ArrayList<Integer[]> resultList = new ArrayList<Integer[]>();
//		while (geometryKeys.hasNext()){
//	Integer[] currentValue = nextToPlace.get(geometryKeys.next()[3]);
//			resultList.add(currentValue);
//		}
//		return resultList.iterator();
//	}
	
//	public static void main(String[] args){
//		String a = "5,7";
//		String b = "5";
//		String[]a1 = a.split(",");
//		String[]b1 = b.split(",");
//		System.out.println(a1.length+" "+a1[1]);
//		System.out.println(b1.length+" "+b1[0]);
//	}
}
