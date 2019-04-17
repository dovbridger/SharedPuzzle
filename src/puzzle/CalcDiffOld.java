package puzzle;


public class CalcDiffOld implements Runnable{
	PuzzleParts puzzleParts;
	Orientation or;
	int ii,jj,partSize,partsNum;
	float diffMatrix[][][];
	int itNum;

	public CalcDiffOld(float[][][] diffMatrix2,int _partSize,int _partsNum,PuzzleParts _puzzleParts,Orientation _or, int _i, int _j,int _itNum) {
		puzzleParts=_puzzleParts;
		or=_or;
		ii=_i;
		jj=_j;
		partSize=_partSize;
		partsNum=_partsNum;
		diffMatrix=diffMatrix2;
		itNum=_itNum;
	}


	@Override
	public void run() {
		for(int i=ii;i<ii+itNum+1&&i<partsNum;i++){
			PuzzlePart p1=puzzleParts.getLABPart(i);
			LABVector p1_l1 = null;
			LABVector p1_l2 = null;
			LABVector p2_l1 = null;
			LABVector p2_l2 = null;
			switch(or){
			case DOWN:
				p1_l1 = p1.getRow(partSize+1);
				p1_l2 = p1.getRow(partSize);
				break;
			case RIGHT:			
				p1_l1 = p1.getCol(partSize+1);
				p1_l2 = p1.getCol(partSize);
				break;
			case LEFT:
				p1_l1 = p1.getCol(0);
				p1_l2 = p1.getCol(1);
				break;
			case UP:
				p1_l1 = p1.getRow(0);
				p1_l2 = p1.getRow(1);
				break;
			default:
				break;
			}
			//		int start=updown*partsNum/2,finish=(1+updown)*partsNum/2;
			//		if(op==1){
			//			start=0;finish=partsNum;
			//		}
			for(int j=jj;j<jj+itNum+1&&j<partsNum;j++){

				int op=or.ordinal()+1;
				double diffB; //Dov - The difference between the average of the borders
				if((!puzzleParts.isDark(i)) && (!puzzleParts.isDark(j))){
					
					double Ri=puzzleParts.avgBorder[0][or.ordinal()][i];
					double Gi=puzzleParts.avgBorder[1][or.ordinal()][i];
					double Bi=puzzleParts.avgBorder[2][or.ordinal()][i];
					double Rj=puzzleParts.avgBorder[0][op][j];
					double Gj=puzzleParts.avgBorder[1][op][j];
					double Bj=puzzleParts.avgBorder[2][op][j];
					 diffB=(Ri-Rj)*(Ri-Rj)+(Gi-Gj)*(Gi-Gj)+(Bi-Bj)*(Bi-Bj);
				}
				else
					diffB=1000;
				if(diffB<1250){ //Dov - Used to be 250, now that there are burnt pixels I changed it in order to be extra careful and let pieces with non-similar borders have a chance
					float diff21=0, diff12=0;
					PuzzlePart p2=puzzleParts.getLABPart(j);
					
					switch(or){
					case DOWN:
						p2_l1 = p2.getRow(0);
						p2_l2 = p2.getRow(1);
						break;
					case RIGHT:			
						p2_l1 = p2.getCol(0);
						p2_l2 = p2.getCol(1);
						break;
					case LEFT:
						p2_l1 = p2.getCol(partSize+1);
						p2_l2 = p2.getCol(partSize);
						break;
					case UP:
						p2_l1 = p2.getRow(partSize+1);
						p2_l2 = p2.getRow(partSize);
						break;
					default:
						break;
					}
					
//					if (i==10){
//						System.out.println(i);
//					}
					
					if (Global.useImportance){
						diff21=(float) p1_l1.sub(p2_l2).abs().sumWithImportance();
						diff12=(float) p2_l1.sub(p1_l2).abs().sumWithImportance();
					}else{
						diff21=(float) p1_l1.sub(p2_l2).abs().sum();
						diff12=(float) p2_l1.sub(p1_l2).abs().sum();
					}

					diffMatrix[or.ordinal()][i][j]=diff21;
					diffMatrix[op][j][i]=diff12;

					if(i==j){
						diffMatrix[or.ordinal()][i][j]=Global.MAX_FLOAT;
						diffMatrix[op][j][i]=Global.MAX_FLOAT;
					}
				}
				else{
					diffMatrix[or.ordinal()][i][j]=Global.MAX_FLOAT;
					diffMatrix[op][j][i]=Global.MAX_FLOAT;
				}
			}
		}
	}
}
