package oldPuzzle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;

public class AddCandidates implements Runnable{
	int minX,maxX,minY,maxY,i,partsNum,partsLeft;
	Integer finalPlacement[][];
	HashMap<Integer, Integer[]> chosenParts;
	ArrayList<TreeMap<Double, Integer[]>> nextToPlace;
	float confMatrix[][][];
	int bestBuddies[][];
	int bestBuddies2[][];
	boolean flag,useSize,missPart;
	int budddiesNumber[];
	PuzzleParts puzzleParts;

	public AddCandidates(int _minX,int _maxX,int _minY,int _maxY,int _i,int _partsNum,int _partsLeft,Integer _finalPlacement[][],
			HashMap<Integer, Integer[]> _chosenParts,ArrayList<TreeMap<Double, Integer[]>> _nextToPlace,float _confMatrix[][][],
			int _bestBuddies[][],boolean _flag,boolean _useSize,int _budddiesNumber[],PuzzleParts _puzzleParts,boolean _missPart,int _bestBuddies2[][]) {
		minX=_minX;maxX=_maxX;minY=_minY;maxY=_maxY;
		i=_i;
		partsNum=_partsNum;partsLeft=_partsLeft;
		finalPlacement=_finalPlacement;
		chosenParts=_chosenParts;
		nextToPlace=_nextToPlace;
		confMatrix=_confMatrix;
		bestBuddies=_bestBuddies;
		flag=_flag;
		useSize=_useSize;
		budddiesNumber=_budddiesNumber;
		puzzleParts=_puzzleParts;
		missPart=_missPart;
		bestBuddies2=_bestBuddies2;
	}

	@Override
	public void run() {
		for(int j=minX;j<maxX+1;j++){
			if(finalPlacement[i][j]!=null){
				if(finalPlacement[i-1][j]==null || finalPlacement[i+1][j]==null || finalPlacement[i][j-1]==null || finalPlacement[i][j+1]==null){
					if(flag)
						addBuddies(finalPlacement[i][j]-1);
					else
						addBuddies2(finalPlacement[i][j]-1);
				}
			}
		}
	}

	private void addBuddies(int nextI) {
		ArrayList<Orientation> ao=new ArrayList<Orientation>();
		ao.add(Orientation.DOWN);ao.add(Orientation.UP);ao.add(Orientation.LEFT);ao.add(Orientation.RIGHT);
		Collections.shuffle(ao);
		for(Orientation or:Orientation.values()){
			int next=bestBuddies[nextI][or.ordinal()];
			//			if(puzzleParts.isBurn(next) && partsLeft>puzzleParts.burnPartsNum+2)
			//				continue;
			if(next==-1)
				continue;
			int op=(or.ordinal()%2==0)?(or.ordinal()+1):(or.ordinal()-1);
			//			int count=-1;
			//			Double val=confMatrix[next][nextI][op];
			//			if(val>confMatrix[next][bestBuddies[next][0]][0])
			//				count++;
			//			if(val>confMatrix[next][bestBuddies[next][1]][1])
			//				count++;
			//			if(val>confMatrix[next][bestBuddies[next][2]][2])
			//				count++;
			//			if(val>confMatrix[next][bestBuddies[next][3]][3])
			//				count++;
			//			if(count>2)
			//				continue;
			Integer nextA[]=new Integer[3];
			nextA[0]=next;
			int dir[]=new int[3];
			if(nextI==bestBuddies[next][op]  && !chosenParts.containsKey(next)){ //&&
				//!nextToPlace.get(budddiesNumber[next]-1).containsKey(bestBuddiesTree.get(budddiesNumber[next]-1).get(next))){
				switch(or){
				case UP:
					nextA[1]=chosenParts.get(nextI)[0];
					nextA[2]=chosenParts.get(nextI)[1]-1;
					dir[0]=3;
					dir[1]=1;
					dir[2]=2;
					break;
				case DOWN:
					nextA[1]=chosenParts.get(nextI)[0];
					nextA[2]=chosenParts.get(nextI)[1]+1;
					dir[0]=2;
					dir[1]=0;
					dir[2]=3;
					break;
				case LEFT:
					nextA[1]=chosenParts.get(nextI)[0]-1;
					nextA[2]=chosenParts.get(nextI)[1];
					dir[0]=0;
					dir[1]=3;
					dir[2]=1;
					break;
				case RIGHT:
					nextA[1]=chosenParts.get(nextI)[0]+1;
					nextA[2]=chosenParts.get(nextI)[1];
					dir[0]=1;
					dir[1]=2;
					dir[2]=0;
					break;
				default:
					break;
				}
				if((useSize && (nextA[1]+partsNum/4-minX+1>puzzleParts.nw || maxX-nextA[1]-partsNum/4+1>puzzleParts.nw ||
						nextA[2]+partsNum/4-minY+1>puzzleParts.nh || maxY-nextA[2]-partsNum/4+1>puzzleParts.nh)))
					continue;
//				if(nextA[1]>partsNum/2-2|| nextA[1]<0 ||
//						nextA[0]>partsNum/2-2|| nextA[0]<0)
//					continue;
				int flagc=0;
				double w1=0.5,w2=0.5;
				//				if(partsLeft<partsNum/16){
				//					w1=0.25;
				//					w2=0.75;
				//				}
				if(finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4] !=null && bestBuddies[finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][1]==nextA[0]) {
					flagc++;
				}

				if(finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4] !=null && bestBuddies[nextA[0]][1]==finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1) {
					flagc++;
				}

				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4] !=null && bestBuddies[finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][3]==nextA[0]) {
					flagc++;
				}

				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4] !=null && bestBuddies[nextA[0]][3]==finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1) {
					flagc++;
				}
				if(finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4] !=null && bestBuddies[finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][1]==nextA[0]) {
					flagc++;
				}
				if(finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4] !=null && bestBuddies[nextA[0]][1]==finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1) {
					flagc++;
				}

				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4] !=null && bestBuddies[finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][3]==nextA[0]) {
					flagc++;
				}

				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4] !=null  && bestBuddies[nextA[0]][3]==finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1) {
					flagc++;
				}			
				double th2=0;
				int flagc2=0;
				if(finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4] !=null){
					th2+=w1*confMatrix[1][finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][nextA[0]]+w2*confMatrix[0][nextA[0]][finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1];
					flagc2++;
				}
				if(finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4] !=null){
					th2+=w1*confMatrix[0][finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1][nextA[0]]+w2*confMatrix[1][nextA[0]][finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1];
					flagc2++;
				}
				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4] !=null){
					th2+=w1*confMatrix[3][finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][nextA[0]]+w2*confMatrix[2][nextA[0]][finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1];
					flagc2++;
				}
				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4] !=null){
					th2+=w1*confMatrix[2][finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1][nextA[0]]+w2*confMatrix[3][nextA[0]][finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1];
					flagc2++;
				}
				//				if(flagc2==1 && 
				//						(bestBuddies[next][dir[0]]!=-1 && bestBuddies[bestBuddies[next][dir[0]]][dir[1]]!=-1
				//						&& bestBuddies[bestBuddies[bestBuddies[next][dir[0]]][dir[1]]][dir[2]]!=nextI)// &&
				//(bestBuddies[next][dir[2]]!=-1 && bestBuddies[bestBuddies[next][dir[2]]][dir[1]]!=-1
				//&& bestBuddies[bestBuddies[bestBuddies[next][dir[2]]][dir[1]]][dir[0]]!=nextI))
				//						) 
				//					continue;
				double add=((flagc-1)>0)?(flagc-1):0;
				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]+partsNum/4]==null){
					synchronized (nextToPlace){	
						if(!missPart)
							nextToPlace.get(12).put(th2/flagc2+flagc2*0.0+add*0.3,nextA);
						else
							nextToPlace.get(12).put(th2/flagc2,nextA);
					}
				}
			}
		}
	}

	private void addBuddies2(int nextI) {
		ArrayList<Orientation> ao=new ArrayList<Orientation>();
		ao.add(Orientation.DOWN);ao.add(Orientation.UP);ao.add(Orientation.LEFT);ao.add(Orientation.RIGHT);
		Collections.shuffle(ao);
		for(Orientation or:Orientation.values()){
			int next=bestBuddies[nextI][or.ordinal()];
			//			if(puzzleParts.isBurn(next) && partsLeft>puzzleParts.burnPartsNum+2)
			//				continue;
			if(next==-1)
				continue;
			Integer nextA[]=new Integer[3];
			nextA[0]=next;
			int op=(or.ordinal()%2==0)?(or.ordinal()+1):(or.ordinal()-1);
			//			Double val=confMatrix[nextI][next][or.ordinal()]/2+confMatrix[next][nextI][op]/2;
			//			Double val=confMatrix[next][nextI][op];
			//			if(val<0.368) continue;
			//			int count=0;
			//			Double val=confMatrix[next][nextI][op];
			//			if(val<confMatrix[next][bestBuddies[next][0]][0])
			//				count++;
			//			if(val<confMatrix[next][bestBuddies[next][1]][1])
			//				count++;
			//			if(val<confMatrix[next][bestBuddies[next][2]][2])
			//				count++;
			//			if(val<confMatrix[next][bestBuddies[next][3]][3])
			//				count++;
			//			if(count>2)
			//				continue;
			if(!chosenParts.containsKey(next)){ //&&
				switch(or){
				case UP:
					nextA[1]=chosenParts.get(nextI)[0];
					nextA[2]=chosenParts.get(nextI)[1]-1;
					break;
				case DOWN:
					nextA[1]=chosenParts.get(nextI)[0];
					nextA[2]=chosenParts.get(nextI)[1]+1;
					break;
				case LEFT:
					nextA[1]=chosenParts.get(nextI)[0]-1;
					nextA[2]=chosenParts.get(nextI)[1];
					break;
				case RIGHT:
					nextA[1]=chosenParts.get(nextI)[0]+1;
					nextA[2]=chosenParts.get(nextI)[1];
					break;
				default:
					break;
				}
				if((useSize && (nextA[1]+partsNum/4-minX+1>puzzleParts.nw || maxX-nextA[1]-partsNum/4+1>puzzleParts.nw ||
						nextA[2]+partsNum/4-minY+1>puzzleParts.nh || maxY-nextA[2]-partsNum/4+1>puzzleParts.nh)))
					continue;
//				if(nextA[1]>partsNum/2-2|| nextA[1]<0 ||
//						nextA[0]>partsNum/2-2|| nextA[0]<0)
//					continue;
				int flagc=0;
				double w1=0.5,w2=0.5;
				//				if(partsLeft<partsNum/16){
				//					w1=0.25;
				//					w2=0.75;
				//				}
				if(finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4] !=null && bestBuddies[finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][1]==nextA[0]) {
					flagc++;
				}

				if(finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4] !=null && bestBuddies[nextA[0]][1]==finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1) {
					flagc++;
				}

				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4] !=null && bestBuddies[finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][3]==nextA[0]) {
					flagc++;
				}

				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4] !=null && bestBuddies[nextA[0]][3]==finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1) {
					flagc++;
				}
				if(finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4] !=null && bestBuddies[finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][1]==nextA[0]) {
					flagc++;
				}

				if(finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4] !=null && bestBuddies[nextA[0]][1]==finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1) {
					flagc++;
				}

				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4] !=null && bestBuddies[finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][3]==nextA[0]) {
					flagc++;
				}

				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4] !=null  && bestBuddies[nextA[0]][3]==finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1) {
					flagc++;
				}			
				double th2=0,maxTh=0;
				int flagc2=0;
				if(finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4] !=null){
					th2+=w1*confMatrix[1][finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][nextA[0]]+w2*confMatrix[0][nextA[0]][finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1];
					//					maxTh=Math.max(maxTh,w1*confMatrix[finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][nextA[0]][1]+w2*confMatrix[nextA[0]][finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][0]);
					flagc2++;
				}
				if(finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4] !=null){
					th2+=w1*confMatrix[0][finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1][nextA[0]]+w2*confMatrix[1][nextA[0]][finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1];
					//					maxTh=Math.max(maxTh,w1*confMatrix[finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1][nextA[0]][0]+w2*confMatrix[nextA[0]][finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1][1]);
					flagc2++;
				}
				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4] !=null){
					th2+=w1*confMatrix[3][finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][nextA[0]]+w2*confMatrix[2][nextA[0]][finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1];
					//					maxTh=Math.max(maxTh,w1*confMatrix[finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][nextA[0]][3]+w2*confMatrix[nextA[0]][finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][2]);
					flagc2++;
				}
				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4] !=null){
					th2+=w1*confMatrix[2][finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1][nextA[0]]+w2*confMatrix[3][nextA[0]][finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1];
					//					maxTh=Math.max(maxTh,w1*confMatrix[finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1][nextA[0]][2]+w2*confMatrix[nextA[0]][finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1][3]);
					flagc2++;
				}
				//				if(val<0.369 && flagc2==1) continue;
				double add=((flagc-1)>0)?(flagc-1):0;
				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]+partsNum/4]==null){
					synchronized (nextToPlace){	
						if(!missPart)
							try{
								nextToPlace.get(12).put(th2/flagc2+flagc2*0.5+add*0.3,nextA);
							}
						catch (Exception e) {
							System.out.println("E"+flagc2);
						}
						else
							nextToPlace.get(12).put(th2/flagc2/*+maxTh/2*/,nextA);
					}
				}
			}
			//			next=bestBuddies2[nextI][or.ordinal()];
			//			if(next==-1)
			//				continue;
			//			nextA=new Integer[3];
			//			nextA[0]=next;
			//			if(!chosenParts.containsKey(next)){ //&&
			//				switch(or){
			//				case UP:
			//					nextA[1]=chosenParts.get(nextI)[0];
			//					nextA[2]=chosenParts.get(nextI)[1]-1;
			//					break;
			//				case DOWN:
			//					nextA[1]=chosenParts.get(nextI)[0];
			//					nextA[2]=chosenParts.get(nextI)[1]+1;
			//					break;
			//				case LEFT:
			//					nextA[1]=chosenParts.get(nextI)[0]-1;
			//					nextA[2]=chosenParts.get(nextI)[1];
			//					break;
			//				case RIGHT:
			//					nextA[1]=chosenParts.get(nextI)[0]+1;
			//					nextA[2]=chosenParts.get(nextI)[1];
			//					break;
			//				default:
			//					break;
			//				}
			//				if((useSize && (nextA[1]+partsNum/4-minX+1>puzzleParts.nw || maxX-nextA[1]-partsNum/4+1>puzzleParts.nw ||
			//						nextA[2]+partsNum/4-minY+1>puzzleParts.nh || maxY-nextA[2]-partsNum/4+1>puzzleParts.nh)))
			//				 continue;
			//				int flagc=0;
			//				double w1=0.5,w2=0.5;
			//				if(partsLeft<partsNum/4){
			//					w1=0.25;
			//					w2=0.75;
			//				}
			//				if(finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4] !=null && bestBuddies[finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][1]==nextA[0]) {
			//					flagc++;
			//				}
			//
			//				if(finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4] !=null && bestBuddies[nextA[0]][1]==finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1) {
			//					flagc++;
			//				}
			//
			//				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4] !=null && bestBuddies[finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][3]==nextA[0]) {
			//					flagc++;
			//				}
			//
			//				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4] !=null && bestBuddies[nextA[0]][3]==finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1) {
			//					flagc++;
			//				}
			//				if(finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4] !=null && bestBuddies[finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][1]==nextA[0]) {
			//					flagc++;
			//				}
			//
			//				if(finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4] !=null && bestBuddies[nextA[0]][1]==finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1) {
			//					flagc++;
			//				}
			//
			//				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4] !=null && bestBuddies[finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][3]==nextA[0]) {
			//					flagc++;
			//				}
			//
			//				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4] !=null  && bestBuddies[nextA[0]][3]==finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1) {
			//					flagc++;
			//				}			
			//				double th2=0,maxTh=0;
			//				int flagc2=0;
			//				if(finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4] !=null){
			//					th2+=w1*confMatrix[finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][nextA[0]][1]+w2*confMatrix[nextA[0]][finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][0];
			//					maxTh=Math.max(maxTh,w1*confMatrix[finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][nextA[0]][1]+w2*confMatrix[nextA[0]][finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][0]);
			//					flagc2++;
			//				}
			//				if(finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4] !=null){
			//					th2+=w1*confMatrix[finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1][nextA[0]][0]+w2*confMatrix[nextA[0]][finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1][1];
			//					maxTh=Math.max(maxTh,w1*confMatrix[finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1][nextA[0]][0]+w2*confMatrix[nextA[0]][finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1][1]);
			//					flagc2++;
			//				}
			//				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4] !=null){
			//					th2+=w1*confMatrix[finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][nextA[0]][3]+w2*confMatrix[nextA[0]][finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][2];
			//					maxTh=Math.max(maxTh,w1*confMatrix[finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][nextA[0]][3]+w2*confMatrix[nextA[0]][finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][2]);
			//					flagc2++;
			//				}
			//				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4] !=null){
			//					th2+=w1*confMatrix[finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1][nextA[0]][2]+w2*confMatrix[nextA[0]][finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1][3];
			//					maxTh=Math.max(maxTh,w1*confMatrix[finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1][nextA[0]][2]+w2*confMatrix[nextA[0]][finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1][3]);
			//					flagc2++;
			//				}
			//				double add=((flagc-1)>0)?(flagc-1):0;
			//				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]+partsNum/4]==null){
			//					if(!missPart)
			//						try{
			//							nextToPlace.get(12).put(th2/flagc2+flagc2*0.00+add*0.3,nextA);
			//						}
			//					catch (Exception e) {
			//						System.out.println("E"+flagc2);
			//					}
			//					else
			//						nextToPlace.get(12).put(th2/flagc2/*+maxTh/2*/,nextA);
			//				}
			//			}
		}
	}

}
