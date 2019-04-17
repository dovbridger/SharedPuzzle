package parameters;

import puzzle.Global;

public class IntParameter extends Parameter {
	protected int[] values;
	public IntParameter(Global.ParameterNames name, int[] _values){
		super(name,_values.length);
		values = _values;
	}
	
	@Override
	public Integer getValue(){
		return values[index];
	}
	
	@Override
	public String getStringValue(){
		return ""+values[index];
	}
}
