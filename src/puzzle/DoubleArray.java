package puzzle;

public class DoubleArray {
	public double[] array;
	
	public DoubleArray(double[]arr){
		array = arr;
	}
	
	@Override
	public boolean equals(Object other){
		if (((DoubleArray)other).array.length != array.length){
			return false;
		}else{
			for(int i=0; i<array.length; i++){
				if (((DoubleArray)other).array[i]!=array[i]){
					return false;
				}
			}
			return true;
		}
	}
}
