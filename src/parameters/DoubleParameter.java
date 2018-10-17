package parameters;

import puzzle.Global;

public class DoubleParameter extends Parameter {
	protected double[] values;
	public DoubleParameter(Global.ParameterNames name, double[] _values){
		super(name,_values.length);
		values = _values;
		
	}
	
	@Override
	public Double getValue(){
		return values[index];
	}
	
	@Override
	public String getStringValue(){
		return ""+(int)(100*values[index]);
	}
}

