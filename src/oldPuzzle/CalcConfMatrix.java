package oldPuzzle;

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
	int bestBuddies2[][];
	PuzzleParts puzzleParts;
	boolean missPart;
	HashMap<Integer, Integer[]> chosenParts;

	public CalcConfMatrix(float[][][] diffMatrix2,float _confMatrix[][][],int _bestBuddies[][],int _partSize,int _partsNum,int _partsLeft,Orientation _or, PuzzleParts _puzzleParts,boolean _missPart, HashMap<Integer, Integer[]> _chosenParts,int _bestBuddies2[][]) {
		or=_or;
		partSize=_partSize;
		partsNum=_partsNum;
		partsLeft=_partsLeft;
		diffMatrix=diffMatrix2;
		confMatrix=_confMatrix;
		bestBuddies=_bestBuddies; 
		puzzleParts=_puzzleParts;
		missPart=_missPart;
		chosenParts=_chosenParts;
		bestBuddies2=_bestBuddies2;
	}

	private Float[] getRow(float[][][] diffMatrix2, int i, Orientation or,int k) {
		Float[] row=new Float[partsNum];
		for(int j=0;j<partsNum;j++){
			row[j]=diffMatrix2[or.ordinal()][i][j];
		}
		return row;
	}

	@Override
	public void run() {
		int op=(or.ordinal()%2==0)?(or.ordinal()+1):(or.ordinal()-1);
		List<Integer> A=new ArrayList<Integer>();
		List<Integer> A2=new ArrayList<Integer>();
//		for(int i=0;i<partsNum;i++){
//			if(chosenParts.containsKey(i))
//				A.add(i);
//		}
		for(int i=0;i<partsNum;i++){
			if(!chosenParts.containsKey(i))
				A.add(i);
		}
		Collections.shuffle(A);
//		A.addAll(A2);
		for(int i=0;i<partsNum;i++){
			bestBuddies[i][or.ordinal()]=-1;
//			bestBuddies2[i][or.ordinal()]=0;
			//			if(puzzleParts.isEmpty(i)){
			//				for(int j=0;j<partsNum;j++){
			//					confMatrix[i][j][or.ordinal()]=Math.exp(-10);
			//				}
			//				continue;

			TreeMap<Float,Integer> tm=new TreeMap<Float,Integer>();
//			Double[] row=getRow(diffMatrix,i,or,0);
			Float[] row2=getRow(diffMatrix,i,or,0);
//			ArrayList<Double> rowList=new ArrayList<Double>(Arrays.asList(row));
			ArrayList<Float> rowList2=new ArrayList<Float>(Arrays.asList(row2));
//			Collections.sort(rowList);
//			Collections.sort(rowList2);
			Float tmp_min=Collections.min(rowList2);
			rowList2.remove(tmp_min);//Dov-Remove the minimum
//			double s2=rowList.get(1);
//			float s2_2=rowList2.get(1);
			float s2_2=tmp_min;//Dov-This is the new minimum (second smallest dissimilarity)
//			if(s2_2>0)
				s2_2=Collections.min(rowList2);
//			else
//				System.out.println("Bla");
			
//			double s20=rowList.get(19);
//			double s1=rowList.get(0);
			//			if(missPart)
			//				s2=rowList.get(partsNum/8);
//						System.out.println("S1: "+s1+" || S2: "+s2);
//			if(s2_2>=0 && s2_2<5000)
//				System.out.println("S2: "+s2_2);
			if (partsLeft<2)
				s2_2=10000;//5

			for(int jj=0;jj<A.size();jj++){
				int j=A.get((jj+i*3)%A.size());
				//					if((!puzzleParts.isBurn(i) && puzzleParts.isBurn(j)) || (puzzleParts.isBurn(i) && !puzzleParts.isBurn(j))){
				//						confMatrix[i][j][or.ordinal()]=0;
				////						System.out.println("D: "+diffMatrix[i][j][or.ordinal()]);
				//					}
				if(s2_2!=0){
					confMatrix[or.ordinal()][i][j]=1-diffMatrix[or.ordinal()][i][j]/s2_2;//Math.exp(-diffMatrix[0][i][j][or.ordinal()]/s2_2);
//					if(puzzleParts.isLowEnt(i) && puzzleParts.isNLowEnt(j) || (puzzleParts.isLowEnt(j) && puzzleParts.isNLowEnt(i)))
//						confMatrix[i][j][or.ordinal()]=0;
				}
				else /*if(diffMatrix[0][i][j][or.ordinal()]==0)*/{
					confMatrix[or.ordinal()][i][j]=(float) 0.001;//Math.exp(-1);
					if(partsLeft<2*puzzleParts.burnPartsNum || partsLeft<2*puzzleParts.darkPartsNum)
						confMatrix[or.ordinal()][i][j]=(float) 0.5;//Math.exp(-0.5);	
				}
				if(diffMatrix[or.ordinal()][i][j]>1000 && confMatrix[or.ordinal()][i][j]>0)
					confMatrix[or.ordinal()][i][j]=confMatrix[or.ordinal()][i][j]/10000;
				if(missPart && (puzzleParts.isEmpty(i) || puzzleParts.isEmpty(j)))
					confMatrix[or.ordinal()][i][j]=0;//Math.exp(-10);
//				if(partsLeft<partsNum/5 && s1>3.9)
//					confMatrix[i][j][or.ordinal()]=Math.exp(-diffMatrix[i][j][or.ordinal()]*(2/s2));
//				if(((s1>0 && s1==s2)  || (chosenParts.containsKey(i) && chosenParts.containsKey(j))) && s2!=5){//diffMatrix[i][j][or.ordinal()]>4.2 || 
//					confMatrix[i][j][or.ordinal()]=0;
//				}
//				if(puzzleParts.isBurn(i) && puzzleParts.isBurn(j))
//					confMatrix[i][j][or.ordinal()]=Math.exp(-1);
//				if(partsLeft>50 && s1>0 && s1/s20 < 0.7){
////					System.out.println("*"+(s1/s20));
//					confMatrix[i][j][or.ordinal()]=confMatrix[i][j][or.ordinal()]*1.1;
//				}
//				System.out.println("S1:"+s1+" || S2:"+s2+" || Comp: " + confMatrix[i][j][or.ordinal()]);
//				if(!(chosenParts.containsKey(i) && chosenParts.containsKey(j)) && confMatrix[or.ordinal()][i][j]>0)
				tm.put(confMatrix[or.ordinal()][i][j], j);
			}

			Iterator<Integer> ordIt=tm.descendingMap().values().iterator();
//			Iterator<Float> ordItK = tm.descendingMap().keySet().iterator();
			//			synchronized (bestBuddies) {
//			if(ordItK.next()>0)
			if(ordIt.hasNext())
				bestBuddies[i][or.ordinal()]=ordIt.next(); //Dov - Sets i's best neighbor to be the one with the highest confidence
//			if(ordIt.hasNext())
//				if(ordItK.next()>0)
//					bestBuddies2[i][or.ordinal()]=ordIt.next();
				
			//				else
			//					System.out.println("*");
			//			}
		}
	}
}
