package parameters;

import java.io.Serializable;

import puzzle.Global;

//Parameters are objects that enable us to set different values for variables that
//affect the puzzle solving algorithm. An object as such enables us to pick a set of values for it's
//puzzle parameter and then we run tests with all of the possible combinations of values for all the parameters
public class Parameter implements Serializable {

	private static final long serialVersionUID = 1L;
	public Global.ParameterNames name;
	public int numValues;
	public int[] numTestElements;
	public float[] scoreSums;
	public int index;
	public int recoverIndex;

	public Parameter(Global.ParameterNames _name, int _numValues){
		numValues = _numValues;
		name = _name;
		numTestElements = new int[numValues];
		scoreSums = new float[numValues];
		index = 0;		
		recoverIndex = 0;
	}
	
	public void updateSum(float score){
		scoreSums[index] = scoreSums[index] + score;
		numTestElements[index] = numTestElements[index]+1;
	}
	
	public float getAverageScore(int _index){
		float result = scoreSums[_index]/numTestElements[_index];
		return result;
	}
	
	public Object getValue() {return null;}
	public String getStringValue(){return null;}
}