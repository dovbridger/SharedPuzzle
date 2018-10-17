package puzzle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

public class CalcConfMatrix implements Runnable{

	Orientation or;
	int i,partSize,partsNum,partsLeft;
	float diffMatrix[][][];
	float confMatrix[][][];
	int bestBuddies[][];
	PuzzleParts puzzleParts;
	boolean missPart;
	HashMap<Integer, Integer[]> chosenParts;

	public CalcConfMatrix(
			float[][][] _diffMatrix,float _confMatrix[][][],int _bestBuddies[][],int _partSize, int _partsNum,
			int _partsLeft,Orientation _or, PuzzleParts _puzzleParts,boolean _missPart, HashMap<Integer, Integer[]> _chosenParts) {
		or=_or;
		partSize=_partSize;
		partsNum=_partsNum;
		partsLeft=_partsLeft;
		diffMatrix=_diffMatrix;
		confMatrix=_confMatrix;
		bestBuddies=_bestBuddies; 
		puzzleParts=_puzzleParts;
		missPart=_missPart;
		chosenParts=_chosenParts;
	}

	private Float[] getRow(float[][][] _diffMatrix, int i, Orientation or,int k) {
		Float[] row=new Float[partsNum];
		for(int j=0;j<partsNum;j++){
			row[j]=_diffMatrix[or.ordinal()][i][j];
		}
		return row;
	}

	@Override
	public void run() {
		List<Integer> A=new ArrayList<Integer>();

		for(int i=0;i<partsNum;i++){
			if(!chosenParts.containsKey(i))
				A.add(i);
		}
		Collections.shuffle(A);

		for(int i=0;i<partsNum;i++){
			TreeMap<Float,Integer> tm=new TreeMap<Float,Integer>();
			Float[] row2=getRow(diffMatrix,i,or,0);
			ArrayList<Float> rowList2=new ArrayList<Float>(Arrays.asList(row2));

			Float tmp_min=Collections.min(rowList2);
			rowList2.remove(tmp_min);//Dov-Remove the minimum

			float s2_2=tmp_min;//Dov-This is the new minimum (second smallest dissimilarity)
			s2_2=Collections.min(rowList2);

			if (partsLeft<2)
				s2_2=10000;//5
			//Dov's modification to the confidence scores.
			//Instead of being between -inf to 1, they will be between 0 and inf.
			//This was done to allow integrating the conf score with geometric location error via
			//multiplication into a final placing score
			if (Global.positiveConf){
				for(int jj=0;jj<A.size();jj++){
					int j=A.get((jj+i*3)%A.size());
					
					if (diffMatrix[or.ordinal()][i][j] == Float.MAX_VALUE){
						confMatrix[or.ordinal()][i][j] = 0;
					}else{
						//To save time, don't change the confidence a part who's conf was already modified and still has the same best neighbor
						//Otherwise, the conf value will probably drop under "MOD_CONF_BASELINE" and the conf modification algorithm will run again
						//for no good reason 
						if (Global.modifyConf && confMatrix[or.ordinal()][i][j] > Global.MOD_CONF_BASELINE && bestBuddies[i][or.ordinal()] == j){
							tm.put(confMatrix[or.ordinal()][i][j], j);
							continue;
						}
						if(diffMatrix[or.ordinal()][i][j]!=0){
							confMatrix[or.ordinal()][i][j]= s2_2/diffMatrix[or.ordinal()][i][j];//float)Math.exp(-diffMatrix[or.ordinal()][i][j]/s2_2);
						}else{	
							if (s2_2 == 0){//Also second best has 0 difference
								confMatrix[or.ordinal()][i][j]=(float) 1.001;
								if(partsLeft<2*puzzleParts.burnPartsNum || partsLeft<2*puzzleParts.darkPartsNum){
									confMatrix[or.ordinal()][i][j]=(float) 1.5;
								}
							}else{
								confMatrix[or.ordinal()][i][j] = Global.MAX_CONF;
							}
						}
						if(diffMatrix[or.ordinal()][i][j]>1000 && confMatrix[or.ordinal()][i][j]>1){ //If the best neighbor has a big dissimilarity
							confMatrix[or.ordinal()][i][j]=(confMatrix[or.ordinal()][i][j]-1)/10000 +1; //Bring the confidence very close to 1 but preserve monotonicity 
						}
						if(missPart && (puzzleParts.isEmpty(i) || puzzleParts.isEmpty(j))){
							confMatrix[or.ordinal()][i][j]=1;
						}
						
						confMatrix[or.ordinal()][i][j] = Math.min(confMatrix[or.ordinal()][i][j], Global.MAX_CONF);
						tm.put(confMatrix[or.ordinal()][i][j], j);
					}
				}
			//The previous method for calculating confidence
			}else{
				for(int jj=0;jj<A.size();jj++){
					int j=A.get((jj+i*3)%A.size());
					if(s2_2!=0){
						confMatrix[or.ordinal()][i][j]= 1 - diffMatrix[or.ordinal()][i][j]/s2_2;//float)Math.exp(-diffMatrix[or.ordinal()][i][j]/s2_2);
					}else{
						confMatrix[or.ordinal()][i][j]=(float) 0.001;
						if(partsLeft<2*puzzleParts.burnPartsNum || partsLeft<2*puzzleParts.darkPartsNum)
							confMatrix[or.ordinal()][i][j]=(float) 0.5;
					}
					if(diffMatrix[or.ordinal()][i][j]>1000 && confMatrix[or.ordinal()][i][j]>0)
						confMatrix[or.ordinal()][i][j]=confMatrix[or.ordinal()][i][j]/10000;
					if(missPart && (puzzleParts.isEmpty(i) || puzzleParts.isEmpty(j)))
						confMatrix[or.ordinal()][i][j]=0;
	
					tm.put(confMatrix[or.ordinal()][i][j], j);
				}
			}
			
			Iterator<Integer> ordIt=tm.descendingMap().values().iterator();
			bestBuddies[i][or.ordinal()]=-1;
			if(ordIt.hasNext())
				bestBuddies[i][or.ordinal()]=ordIt.next(); //Dov - Sets i's best neighbor to be the one with the highest confidence

		}
	}
}
