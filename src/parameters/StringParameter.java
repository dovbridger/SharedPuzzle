package parameters;

import puzzle.Global;

public class StringParameter extends Parameter {
	protected String[] values;
	public StringParameter(Global.ParameterNames name, String[] _values){
		super(name,_values.length);
		values = _values;
	}
	
	@Override
	public String getValue(){
		return values[index];
	}
	
	@Override
	public String getStringValue(){
		return values[index];
	}
}
