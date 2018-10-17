package data_type;

public class GeometryPoolEntry extends PoolEntry {

	private static final long serialVersionUID = 2L;
	public double confidenceKey;
	public double distanceFromExpected;
	
	public GeometryPoolEntry(int[] _nextA, int _addedBy, int _stageAdded, int _numNeighbors, int _numBestNeighbors, double _bonus, int _methodType, int[] _scoreComponents,
			 double _neighborFactor, double _nonInclusiveBestNeighbors, double _nonInclusiveNeighbors, double _confidenceKey, double _distanceFromExpected){
		super(_nextA, _addedBy, _stageAdded, _numNeighbors, _numBestNeighbors, _bonus, _methodType, _scoreComponents, _neighborFactor, _nonInclusiveBestNeighbors, _nonInclusiveNeighbors);
		confidenceKey = _confidenceKey;
		distanceFromExpected = _distanceFromExpected;
	}
	
	public GeometryPoolEntry(PoolEntry otherPE, double _confidenceKey, double _distanceFromExpected){
		super(otherPE);
		confidenceKey = _confidenceKey;
		distanceFromExpected = _distanceFromExpected;
	}
	public GeometryPoolEntry(GeometryPoolEntry otherGPE){
		super(otherGPE);
		confidenceKey = otherGPE.confidenceKey;
		distanceFromExpected = otherGPE.distanceFromExpected;
	}
	
	@Override
	public PoolEntry copy(){
		return new GeometryPoolEntry(this);
	}
	@Override
	public boolean equals(Object other){
		boolean isEqual = super.equals(other);
		isEqual = isEqual && ((GeometryPoolEntry)(other)).confidenceKey == confidenceKey;
		return isEqual && ((GeometryPoolEntry)(other)).distanceFromExpected == distanceFromExpected;
	}
	
	@Override
	public double getDistanceFromExpected(){
		return distanceFromExpected;
	}
}
