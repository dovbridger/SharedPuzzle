package puzzle;

import java.util.HashMap;

public class EliminateComp  implements Runnable{
	
	int i,partsNum;
	double diffMatrix[][][];
	HashMap<Integer, Integer[]> chosenParts;
	
	public EliminateComp(double _diffMatrix[][][],HashMap<Integer, Integer[]> _chosenParts,int _partsNum,int _i) {
		 diffMatrix=_diffMatrix;
		 partsNum=_partsNum;
		 i=_i;
		 chosenParts=_chosenParts;
	}
	
	@Override
	public void run() {
		for(int j=0;j<partsNum;j++){
			if(chosenParts.containsKey(i) && chosenParts.containsKey(j)){
				diffMatrix[i][j][0]=Double.MAX_VALUE;
				diffMatrix[i][j][1]=Double.MAX_VALUE;
				diffMatrix[i][j][2]=Double.MAX_VALUE;
				diffMatrix[i][j][3]=Double.MAX_VALUE;
			}
		}
	}

}
