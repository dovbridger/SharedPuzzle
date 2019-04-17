package data_type;

import java.io.Serializable;

public class PoolEntry implements Serializable {

	private static final long serialVersionUID = 1L;
	public int[] nextA;	//The part that is being added to the pool and its assigned locaiton on the board
	public int addedBy;
	public int stageAdded;
	public int numNeighbors;
	public int numBestNeighbors;
	public double bonus;
	public int methodType;
	public int[] scoreComponents;
	public int neighborFactor;
	public int nonInclusiveBestNeighbors;
	public int nonInclusiveNeighbors;
	
	public PoolEntry(){
		nextA = new int[3];
		scoreComponents = new int[16];
	}
	
	public PoolEntry(int[] _nextA, int _addedBy, int _stageAdded, int _numNeighbors, int _numBestNeighbors, double _bonus, int _methodType, int[] _scoreComponents,
					 double _neighborFactor, double _nonInclusiveBestNeighbors, double _nonInclusiveNeighbors){
		nextA = _nextA;
		addedBy = _addedBy;
		stageAdded = _stageAdded;
		numNeighbors = _numNeighbors;
		numBestNeighbors = _numBestNeighbors;
		bonus = _bonus;
		methodType = _methodType;
		scoreComponents = _scoreComponents;
		neighborFactor = (int)_neighborFactor;
		nonInclusiveBestNeighbors = (int)_nonInclusiveBestNeighbors;
		nonInclusiveNeighbors = (int) _nonInclusiveNeighbors;
	}
	
	public PoolEntry(PoolEntry otherPE){
		nextA = copyArray(otherPE.nextA);
		addedBy = otherPE.addedBy;
		stageAdded = otherPE.stageAdded;
		numNeighbors = otherPE.numNeighbors;
		numBestNeighbors = otherPE.numBestNeighbors;
		bonus = otherPE.bonus;
		methodType = otherPE.methodType;
		scoreComponents = copyArray(otherPE.scoreComponents);
		neighborFactor = otherPE.neighborFactor;
		nonInclusiveBestNeighbors = otherPE.nonInclusiveBestNeighbors;
		nonInclusiveNeighbors = otherPE.nonInclusiveNeighbors;
	}
	
	public PoolEntry copy(){
		return new PoolEntry(this);
	}
	
	@Override
	public boolean equals(Object other){
		int[]otherNextA = ((PoolEntry)(other)).nextA;
		return nextA[0] == otherNextA[0] && nextA[1] == otherNextA[1] && nextA[2] == otherNextA[2];
	}
	
	public int[] copyArray(int[] arr){
		if (arr == null){
			return null;
		}
		int[] result = new int[arr.length];
		for (int i = 0; i < arr.length; i++){
			result[i] = arr[i];
		}
		return result;
	}
	
	public double getDistanceFromExpected(){
		return -1;
	}
	public boolean isPoolEntry(){
		return this.getClass() == PoolEntry.class;
	}
	
	public static void main(String[]args){
		PoolEntry pe = new PoolEntry();
		PoolEntry gpe = new GeometryPoolEntry(pe,0,0);
		System.out.println("pe="+pe.isPoolEntry()+"  gpe="+gpe.isPoolEntry());
	}
}
