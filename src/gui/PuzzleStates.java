package gui;

import java.io.IOException;
import java.io.Serializable;

import javax.swing.ImageIcon;

import puzzle.CopyObject;
import puzzle.Global;
import data_type.PoolEntry;

//An instance of this class represents a puzzle solving simulation that holds all the
//stages of the puzzle solving and can be viewed in the gui debugger
public class PuzzleStates implements Serializable {
	private static final long serialVersionUID = 1L;
	public ImageIcon[] partIcons;
	public PoolEntry[][] poolParts;
	public double[][] poolKeys;
	public int[][] partsPlaced;
	public int[]placingStages;
	public int[][][]bestBuddies;
	public String fileName;
	public int poolSize;
	public int numParts;
	public int xParts;
	public int yParts;
	public int partSize;
	public int currentStage;
	public int mistakeStage;
	public int significantDigits;
	//The constant neighbor score factors that were given in Genadi's algorithm in the "addBuddies1-3 methods"
	public final double POOL_KEY_NEIGHBOR_FACTOR13 = Global.POOL_KEY_NEIGHBOR_FACTOR13;
	public final double POOL_KEY_NEIGHBOR_FACTOR2 = Global.POOL_KEY_NEIGHBOR_FACTOR2;
	public final double POOL_KEY_BEST_NEIGHBOR_FACTOR = Global.POOL_KEY_BEST_NEIGHBOR_FACTOR;
	
	public PuzzleStates(int _partSize, int _xParts, int _yParts, int _poolSize){
		partSize = _partSize;
		numParts = _xParts * _yParts;
		xParts = _xParts;
		yParts = _yParts;
		poolSize = _poolSize;
		poolParts = new PoolEntry[numParts][poolSize];
		poolKeys = new double[numParts][poolSize];
		partsPlaced = new int[numParts][3];
		bestBuddies = new int[numParts][numParts][4];
		placingStages = new int[numParts];
		currentStage = 0;
		mistakeStage = numParts;
		fileName = Global.getPuzzleStatesFile(null); //null means choose by the default options for the name of the file
		significantDigits = Global.SIGNIFICANT_DIGITS;
	}
	//Load the puzzle from a file
	public static PuzzleStates load(String fileName){
		try {
			PuzzleStates loadedObject =  (PuzzleStates)CopyObject.read(fileName);
			return loadedObject;
		} catch (ClassNotFoundException e) {e.printStackTrace();
		} catch (IOException e) {e.printStackTrace();}
		return null;
	}
	//Save the puzzle to a file
	public void save(){
		currentStage = -1;
		try {
			CopyObject.write(this, this.fileName);
		} catch (IOException e){e.printStackTrace();}
	}
	//Copies the "bestBuddies" array from the puzzle to a specific array for the current stage
	//Basically creates a snapshot of that array for every stage
	public void copyBestBuddies(int[][]_bestBuddies){
		for (int part = 0; part<numParts; part++){
			for (int or=0; or<4; or++){
				bestBuddies[currentStage-1][part][or] = _bestBuddies[part][or];
			}
		}
	}
}
