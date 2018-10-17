package puzzle;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;

public class MatlabHoleFiller extends MatlabProxyFactory {
	private MatlabProxy proxy;
	private float[][][]diffMat;
	private int originalPartSize;
	private int beamSize;
	
	public MatlabHoleFiller(){
		super();
		originalPartSize = Global.ORIGINAL_PART_SIZE;
		//Number of optional neighbors to select for the hole filling test
		beamSize = Global.BEAM_SIZE;
	}
	
	//Set up the connection to Matlab
	public void setProxy(){
		//Only do it if the connection was not yet established and the modifyConf option is set to true
		if (proxy != null || !Global.modifyConf){return;}
		try{
			proxy = this.getProxy();
		}catch (MatlabConnectionException e) {e.printStackTrace();}
	}
	
	public void disconnect(){
		if (proxy == null) {return;};
		proxy.disconnect();
		System.out.println("Matlab proxy disconnected");
	}

	//Loads the java puzzle objects into the Matlab workspace
	public void loadPuzzle(){
		if (proxy == null) {return;};
		try {
			proxy.eval("global opt; global puzzleInfo;");
			proxy.feval("load_opt_for_java", Global.puzzleName,Global.INPUT_FOLDER,originalPartSize,Global.burnExtent);
		} catch (MatlabInvocationException e) {e.printStackTrace();}
	}
	
	//Wrapper method for the Matlab function "findTrueNeighbor"
	//"part_and_confidence" is a tuple containing the best neighbor for "part" (-1 if cannot be determined) and its score
	public float[] checkPartForTrueNeighbor(int part, Orientation or, int oldRank){
		Object[] returnVals = null;
		float[] part_and_confidence = new float[2];
		float[]diffVals = diffMat[or.ordinal()][part];
		try {
			returnVals = proxy.returningFeval("findTrueNeighbor",2,part+1,or.ordinal()+1,beamSize,oldRank,diffVals,Global.MIN_MATLAB_VARIANCE);
		} catch (MatlabInvocationException e) {
			e.printStackTrace();
		}
		part_and_confidence[0] = (float)((double[])(returnVals[0]))[0];
		part_and_confidence[1] = (float)((double[])(returnVals[1]))[0];
		return part_and_confidence;
	}
	//Update the matlab's version of the diffMatrix to the java version.
	//Changes occur after parts are placed and impossible neighbors get a diff score of infinity
	public void updateDiffMat(float[][][] _diffMat){
		diffMat = _diffMat;
	}
}