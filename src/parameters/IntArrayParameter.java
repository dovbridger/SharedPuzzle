package parameters;

import puzzle.Global;

public class IntArrayParameter extends Parameter {
	protected int[][] values;
	
	public IntArrayParameter(Global.ParameterNames name, int[][] _values){
		super(name,_values.length);
		values = _values;
	}
	
	@Override
	public int[] getValue(){
		return values[index];
	}
	
	@Override
	public String getStringValue(){
		String result = "(";
		for (int i=0; i<values[index].length; i++){
			if (i!=0){
				result = result+",";
			}
			result = result + values[index][i];
		}
		return result+")";
	}
}
