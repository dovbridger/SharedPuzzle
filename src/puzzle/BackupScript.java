package puzzle;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeMap;

import parameters.*;

//Manages the progress of a testing script, saving the state of the script after each
//puzzle that is solved, and allowing to reload the script in case it fails from the
//latest state it had reached
public class BackupScript implements Serializable {

	private static final long serialVersionUID = 1L;
	//Scores for the specific puzzle image that is being worked on now
	public TreeMap<Float,ArrayList<String[]>> scores;
	//Scores from all of the solves of all of the puzzle images
	public TreeMap<Float,ArrayList<String[]>> allScores;
	//The array of the "Parameter" objects that were specified for the testing script
	public Parameter[] parameters;
	public int recoverImageNum;
	public short version = 0;
	
	public BackupScript(Parameter[] _parameters){
		parameters = _parameters;
		scores = new TreeMap<Float,ArrayList<String[]>>();
		allScores = new TreeMap<Float,ArrayList<String[]>>();
	}
	
	public void recoverIndexes(){
		for (int i = 0; i < parameters.length; i++){
			parameters[i].recoverIndex = parameters[i].index;
			parameters[i].index = 0;
		}
	}
	
	public static BackupScript loadObject(String fileName){
		try {
			BackupScript loadedObject =  (BackupScript)CopyObject.read(fileName+".txt");
			return loadedObject;
		} catch (ClassNotFoundException e) {e.printStackTrace();
		} catch (IOException e) {e.printStackTrace();}
		return null;
	}
	
	public void saveObject(String fileName){
		try {
			CopyObject.write(this, fileName + version +".txt");
			version = (short)((version + 1) % 2);
			return;
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error - Couldn't save object at " +fileName);
		}
	}

}
