package puzzle;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;
import parameters.Parameter;

//A general class that holds information about the puzzle that can be saved to a file
//Not nice code - usually a saved instance will only use part of the fields
public class SaveworthyPuzzleData implements Serializable {
	
	private static final long serialVersionUID = 1L;
	public float conf[][][];
	public float diff[][][];
	public	int buddies[][];
	public int []sortedRanks;
	public int[][]ranks;
	boolean[][][]burntPixels;
	ArrayList<Integer>partsOrder;			
	double[][][][]partsLAB;
	
	//Different constructor for the different types of data combinations that we will want to save
	public SaveworthyPuzzleData(){}
	
	public SaveworthyPuzzleData(float diffMat[][][], float confMat[][][], int BestBuddies[][]){
		conf = confMat;
		buddies = BestBuddies;
		diff = diffMat;
	}
	
	public SaveworthyPuzzleData(boolean[][][]bPixels){
		burntPixels = bPixels;
	}
	
	public SaveworthyPuzzleData(ArrayList<Integer> order){
		partsOrder = order;
	}
	
	public static SaveworthyPuzzleData loadObject(String fileName ){
		try {
			SaveworthyPuzzleData loadedObject =  (SaveworthyPuzzleData)CopyObject.read(fileName);
			return loadedObject;
		} catch (ClassNotFoundException e) {e.printStackTrace();
		} catch (IOException e) {e.printStackTrace();}
		return null;
	}
	
	public void saveObject(String fileName){
		try {
			CopyObject.write(this, fileName);
			return;
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error - Couldn't save object at " +fileName);
		}
	}
	
	public void copy(SaveworthyPuzzleData s){
		conf = s.conf;
		diff = s.diff;
		buddies = s.buddies;
		sortedRanks = s.sortedRanks;
		burntPixels = s.burntPixels;
		partsOrder = s.partsOrder;
		partsLAB = s.partsLAB;
	}
	
	public SaveworthyPuzzleData(PuzzleParts puzzleParts){
		partsLAB = new double[puzzleParts.partsNum][][][];
		for (int part=0; part<puzzleParts.partsNum; part++){
			partsLAB[part] = puzzleParts.getLABPart(part).imgArray;
		}
	}
	
	//The following methods should't really be in this class.
	//They are here because they calculate some of the data that is
	//saved by instances of this class
	
	//Collects information about the rank for each edge of a piece
	//Rank means the position where the correct neighbor for that 
	//piece's edge is ranked in the diff/conf matrix
	public double[] getCorrectPartsRankStats(int xParts, int yParts){
		double[] stats = new double[6]; //0-average, 1-std, 2- absolute differences, 3-percentile, 4- percent Under threshold, 5-max
		int numRanks = 2*((xParts*(yParts-1))+(yParts*(xParts-1)));
		int[]partRanks = new int[numRanks];
		ranks = new int[xParts*yParts][4];
		int currentIndex = 0;
		int sumRanks = 0;
		int maxRank = 0;
		
		for (Orientation or:Orientation.values()){
			for (int part=0; part<xParts*yParts; part++){
				int currectNeighbor = getCorrectNeighbor(part,or,xParts,yParts);
				if (currectNeighbor == -1){
					ranks[part][or.ordinal()] = xParts*yParts;
					continue;
					}
				int[]partNumbers = initArrayOfIndexes(xParts*yParts);
		    	 for (int i=0; i<xParts*yParts-1; i++){
		    		 for (int j=i+1; j<xParts*yParts; j++){
		    			 if (diff[or.ordinal()][part][partNumbers[j]]<diff[or.ordinal()][part][partNumbers[i]]){
		    				int temp = partNumbers[i];
		    				partNumbers[i] = partNumbers[j];
		    				partNumbers[j] = temp;
		    			 }
		    		 }
		    		 if (currectNeighbor == partNumbers[i]){
		    			 partRanks[currentIndex] = i;
		    			 ranks[part][or.ordinal()] = i;
		    			 break;
		    		 }
		    	 }
		    	 sumRanks += partRanks[currentIndex];
		    	 
		    	 if (partRanks[currentIndex] > maxRank){
		    		 maxRank = partRanks[currentIndex];
		    	 }
		    	 currentIndex++;
			}
		}
		stats[0] = (double)sumRanks/(double)numRanks;
		int percentileIndex = (int)(Global.RANK_PERCENTILE*numRanks/100.d) - 1;
		Arrays.sort(partRanks);
		stats[3] = partRanks[percentileIndex];
		stats[5] = maxRank;
		boolean foundPercentage = false;
		//Calculate the std and absolute differences
		double sumSquareError = 0;
		double sumError = 0;
		stats[4] = 100;
		for (int i=0; i<numRanks; i++){
			if (!foundPercentage && partRanks[i] > Global.RANK_THRESH){
				stats[4] = 100*i/numRanks;
				foundPercentage = true;
			}
			double currentAbsError = Math.abs(stats[0] - (double)partRanks[i]);
			double currentSquareError = Math.pow(currentAbsError,2);
			sumError += currentAbsError;
			sumSquareError += currentSquareError;
		}
		double var = sumSquareError/numRanks;
		stats[1] = Math.sqrt(var);
		stats[2] = sumError/numRanks;
		sortedRanks = partRanks;
		return stats;
	}
	
	//Returns the correct neighbor for a piece "part" at orientation "or" given the puzzle dimensions
    public static int getCorrectNeighbor(int part, Orientation or, int xParts, int yParts){
   	 int diff = 0;
   	 switch(or){
		case UP:
			if (part<xParts){return -1;}
			diff = -xParts;
			break;
		case DOWN:
			if (part>=xParts*(yParts-1)){return -1;}
			diff = xParts;
			break;
		case LEFT:
			if (part%xParts == 0){return -1;}
			diff = -1;
			break;
		case RIGHT:
			if (part%xParts == xParts-1){return -1;}
			diff = 1;
			break;
		}
   	 return part+diff; 
    }
    
    //In python this method is equivalent to "return range(size)"
    public static int[] initArrayOfIndexes(int size){
    	int[]result = new int[size];
    	for (int i=0; i<size; i++){
    		result[i] = i;
    	}
    	return result;
    }
}

