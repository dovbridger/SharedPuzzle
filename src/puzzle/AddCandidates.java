package puzzle;

import gui.GuiDebugger;
import data_type.PoolEntry;

public class AddCandidates implements Runnable{
	int i;
	PuzzleSolver ps;
	boolean flag;

	public AddCandidates(int _i,boolean _flag,PuzzleSolver _ps) {
		i=_i;
		flag=_flag;
		ps = _ps;
	}

	@Override
	public void run() {
		for(int j=ps.minX;j<ps.maxX+1;j++){
			if(ps.finalPlacement[i][j]!=null){
				if(ps.finalPlacement[i-1][j]==null || ps.finalPlacement[i+1][j]==null || ps.finalPlacement[i][j-1]==null || ps.finalPlacement[i][j+1]==null){
					addBuddies(ps.finalPlacement[i][j]-1,ps,(flag?1:2)); //flag true = buddies1, flag false = buddies2
				}
			}
		}
	}
	
	public static void addBuddies(int nextI, PuzzleSolver ps, int type){
		boolean use_size = ps.useSize2;
		double neighborFactor = Global.POOL_KEY_NEIGHBOR_FACTOR13;
		double bestNeighborFactor = Global.POOL_KEY_BEST_NEIGHBOR_FACTOR;
		if (type == 1){use_size = ps.useSize;}
		else if (type == 2){neighborFactor = Global.POOL_KEY_NEIGHBOR_FACTOR2;}

		for(Orientation or:Orientation.values()){
			int next=ps.bestBuddies[nextI][or.ordinal()];
			if(next==-1)
				continue;
			
			double bonus = 0;
			double add2 = 0;
			int op = or.opposite().ordinal();
			
//			Integer nextA[]=new Integer[GuiDebugger.POOL_DATA_WIDTH]; //0-part, 1-x, 2-y, 3-added by, 4 - added at stage, 5-flagc2, 6-flagc, 7-bonus, 8-type, 9-neighborFactor, 10-add, 11-add2, 12-27 - data for scoreComponentes table
			int[] nextA = new int[3];
			nextA[0]=next;
			int[] scoreComponents = new int [16];
			
			int dir[] = new int[3]; // Dov - dir[] is the orientations that come after "or" in clockwise order
			Orientation relativeRighToNext = null;
			if ((nextI!=ps.bestBuddies[next][op] && type!=2)  || ps.chosenParts.containsKey(next)){ 
				continue;
			}
			switch (or) {
			case UP:
				nextA[1] = ps.chosenParts.get(nextI)[0];
				nextA[2] = ps.chosenParts.get(nextI)[1] - 1;
				dir[0] = 3;
				dir[1] = 1;
				dir[2] = 2;
				relativeRighToNext = Orientation.RIGHT;		
			break;
			case DOWN:
				nextA[1] = ps.chosenParts.get(nextI)[0];
				nextA[2] = ps.chosenParts.get(nextI)[1] + 1;
				dir[0] = 2;
				dir[1] = 0;
				dir[2] = 3;
				relativeRighToNext = Orientation.LEFT;
			break;
			case LEFT:
				nextA[1] = ps.chosenParts.get(nextI)[0] - 1;
				nextA[2] = ps.chosenParts.get(nextI)[1];
				dir[0] = 0;
				dir[1] = 3;
				dir[2] = 1;
				relativeRighToNext = Orientation.UP;
			break;
			case RIGHT:
				nextA[1] = ps.chosenParts.get(nextI)[0] + 1;
				nextA[2] = ps.chosenParts.get(nextI)[1];
				dir[0] = 1;
				dir[1] = 2;
				dir[2] = 0;
				relativeRighToNext = Orientation.DOWN;
			break;
			default:
			break;
			}
			int cN = ps.getNeighbor(nextA[1],nextA[2],relativeRighToNext);
			int cN2 = ps.getNeighbor(nextA[1],nextA[2],relativeRighToNext.opposite());

		
			if((use_size && (ps.get_absolute_coordinate(nextA[1])-ps.minX+1>ps.puzzleParts.nw
					|| ps.maxX-ps.get_absolute_coordinate(nextA[1])+1>ps.puzzleParts.nw
					|| ps.get_absolute_coordinate(nextA[2])-ps.minY+1>ps.puzzleParts.nh
					|| ps.maxY-ps.get_absolute_coordinate(nextA[2])+1>ps.puzzleParts.nh)))
				continue;

			int flagc=0; //Best Neighbors I will have, to me + from me
			int flagc2=0; //Neighbors I will have on the board
			double w1=0.5,w2=0.5;
			double th2=0;

			
			for (Orientation o:Orientation.values()){
				int currentOrsBestNeighbors = ps.countBestNeighbors(nextA, o);
				flagc = flagc + currentOrsBestNeighbors;
				int oldPart = ps.getNeighbor(nextA[1],nextA[2], o);
				double confOldToNew=0, confNewToOld=0;
				int saveConfStateIndex = 4*o.ordinal();
				if (oldPart != -1){
					flagc2++;
					confOldToNew = ps.confMatrix[o.opposite().ordinal()][oldPart][nextA[0]];
					confNewToOld = ps.confMatrix[o.ordinal()][nextA[0]][oldPart];
					if (Global.MAX_CONF_IN_POOL){
						th2 = Math.max(th2, w1*confOldToNew + w2*confNewToOld);
					}else{
						th2 += w1*confOldToNew + w2*confNewToOld; 
					}
				}
				scoreComponents[saveConfStateIndex] = oldPart;
				scoreComponents[saveConfStateIndex+1] = currentOrsBestNeighbors;
				scoreComponents[saveConfStateIndex+2] = Global.doubleToInt(confOldToNew, 0);
				scoreComponents[saveConfStateIndex+3] = Global.doubleToInt(confNewToOld, 0);
			}
			if (Global.MAX_CONF_IN_POOL){
				th2 = th2*flagc2; //To compensate for the division later(which was originally placed to average a sum)
			}
			if (type == 3){
				if (flagc2 == 1
						&& (ps.bestBuddies[next][dir[0]] != -1
						&& ps.bestBuddies[ps.bestBuddies[next][dir[0]]][dir[1]] != -1 && ps.bestBuddies[ps.bestBuddies[ps.bestBuddies[next][dir[0]]][dir[1]]][dir[2]] == nextI)){
					bonus += 0.25;
//					System.out.println("bonus1");
				}
				if (cN != -1 && flagc2 == 1 && (ps.bestBuddies[next][dir[0]] != -1 && ps.bestBuddies[ps.bestBuddies[next][dir[0]]][dir[1]] == cN)){
					bonus += 0.25;
//					System.out.println("bonus2");
				}
				if (flagc2 == 1
						&& (ps.bestBuddies[next][dir[2]] != -1
						&& ps.bestBuddies[ps.bestBuddies[next][dir[2]]][dir[1]] != -1 && ps.bestBuddies[ps.bestBuddies[ps.bestBuddies[next][dir[2]]][dir[1]]][dir[0]] == nextI)){
					bonus += 0.25;
//					System.out.println("bonus3");
				}
				if (cN2 != -1 && flagc2 == 1 && (ps.bestBuddies[next][dir[0]] != -1 && ps.bestBuddies[ps.bestBuddies[next][dir[2]]][dir[1]] == cN2)){
					bonus += 0.25;
//					System.out.println("bonus4");
				}
				add2 = Math.max(flagc2-2, 0); //add2 is applicable only for type 2-3, so should stay 0 for type 1;
			}else if (type == 2){
				add2 = flagc2;
			}else{
				add2 = 0;
			}
			
			double add = Math.max(flagc-1, 0);
			int addingStage = ps.partsNum-ps.partsLeft-1;
//			nextA[3] = nextI; nextA[4] = addingStage; nextA[5] = flagc2; nextA[6] =flagc; //additional data collected for debugging
//			nextA[7] = Global.doubleToInt(bonus, 0); nextA[8] = type; nextA[9] = Global.doubleToInt(neighborFactor, 0);
//			nextA[10] = (int)add;
//			nextA[11] = (int)add2;
			PoolEntry poolEntry = new PoolEntry(nextA, nextI, addingStage, flagc2, flagc, bonus, type, scoreComponents, neighborFactor, add, add2);
			if(ps.getPart(nextA[1],nextA[2]) == -1){
				synchronized (ps.nextToPlace){	
					double saliencyBonus = Global.NEXT_TO_PLACE_SALIENCY_FACTOR*ps.puzzleParts.puzzlePartsAverageSaliency[nextA[0]];
					if(!ps.missPart){
						double key = th2/flagc2 + add*bestNeighborFactor + add2*neighborFactor + bonus + saliencyBonus;
						ps.nextToPlace.put(key,poolEntry);
						ps.addToGeometryTree(poolEntry,key);
						
					}else{
						ps.nextToPlace.put(th2/flagc2+bonus+ saliencyBonus,poolEntry);
						System.out.println("CandidateB1Miss  "+(th2/flagc2)+" saliencyBonus "+saliencyBonus);
					}
				}
			}
		}
	}
}
