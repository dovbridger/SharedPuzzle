package oldPuzzle;


public class CalcDiff implements Runnable{
	PuzzleParts puzzleParts;
	Orientation or;
	int ii,jj,partSize,partsNum;
	float diffMatrix[][][];
	int itNum;

	public CalcDiff(float[][][] diffMatrix2,int _partSize,int _partsNum,PuzzleParts _puzzleParts,Orientation _or, int _i, int _j,int _itNum) {
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
				p1_l1 = p1.getRow(partSize-1);
				p1_l2 = p1.getRow(partSize-2);
				break;
			case RIGHT:			
				p1_l1 = p1.getCol(partSize-1);
				p1_l2 = p1.getCol(partSize-2);
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
				double diffB;
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
				if(diffB<250){ //Dov - There is a chance that these to peices are neighbors and we need to fully calculate their difference

					//			if(puzzleParts.isEmpty(j)){
					//				diffMatrix[i][j][or.ordinal()]=Double.MAX_VALUE;
					//				diffMatrix[j][i][op]=Double.MAX_VALUE;
					//				continue;
					//			}
					float /*diff11=0, diff12=0,*/diff21=0, diff22=0;//,diff1=0,diff2=0;
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
					diff21=(float) p1_l1.sub(p2_l2).abs().sum();//3.0/10.0.pow(1)
					diff22=(float) p2_l1.sub(p1_l2).abs().sum();//3.0/10.0
//					diff21=(float) p1_l1.sub(p2_l2).abs().pow(3.0/10.0).sum();//3.0/10.0.pow(1)
//					diff22=(float) p2_l1.sub(p1_l2).abs().pow(3.0/10.0).sum();//3.0/10.0
					//				diff21=(float) p1_l1.sub(p1_l2).add(p1_l1).sub(p2_l1).abs().pow(3.0/10.0).sum();//3.0/10.0.pow(1)
					//				diff22=(float) p2_l1.sub(p2_l2).add(p2_l1).sub(p1_l1).abs().pow(3.0/10.0).sum();//3.0/10.0
					//			diff11=p1_l1.sub(p2_l1).abs().sum();//3.0/10.0
					//			diff12=p2_l1.sub(p1_l1).abs().pow(3.0/10.0).sum();//3.0/10.0
					//			diff1=diff11;//Math.pow(diff11, 5.0/48.0);//+Math.pow(diff12, 5.0/48.0);
					//			System.out.println("Comp: D="+diff1+" G="+(Math.abs(p1_l1.avgGrad()-p2_l1.avgGrad())));
					//			diff2=Math.pow(diff21, 5.0/48.0)+Math.pow(diff22, 5.0/48.0);
					//			diffMatrix[i][j][or.ordinal()]=diff1/2+diff2/2;//Math.min(diff1,diff2);
					//			diffMatrix[j][i][op]=diff1/2+diff2/2;//Math.min(diff1,diff2);
					//			diffMatrix[i][j][or.ordinal()]=diff1+diff21;//Math.pow(diff21, 5.0/48.0);//Math.min(diff1,diff2);
					//			diffMatrix[j][i][op]=diff1+diff22;//Math.pow(diff22, 5.0/48.0);//Math.min(diff1,diff2);
					//			diffMatrix[0][i][j][or.ordinal()]=diff1;
					//			diffMatrix[0][j][i][op]=diff1;
					//				diffMatrix[or.ordinal()][i][j]=(float) Math.pow(diff21,5.0/48.0);//Math.pow(diff21,0.1);
					//				diffMatrix[op][j][i]=(float) Math.pow(diff22,5.0/48.0);//Math.pow(diff22,0.1);
					diffMatrix[or.ordinal()][i][j]=diff21;
					diffMatrix[op][j][i]=diff22;
//					diffMatrix[or.ordinal()][i][j]=(float) Math.pow((diff21+diff22),3.0/24.0);
//					diffMatrix[op][j][i]=diffMatrix[or.ordinal()][i][j];
					//				diff21=MGC.calc(p1_l1, p1_l2, p2_l1, p2_l2);
					//				diff22=MGC.calc(p2_l1, p2_l2, p1_l1, p1_l2);
					//				diffMatrix[or.ordinal()][i][j]=diff21+diff22;
					//				diffMatrix[op][j][i]=diff21+diff22;
					//			System.out.println("d1:"+diff1+" || d2:"+diff2/2);
					if(i==j){
						diffMatrix[or.ordinal()][i][j]=Float.MAX_VALUE;
						diffMatrix[op][j][i]=Float.MAX_VALUE;
						//				diffMatrix[1][i][j][or.ordinal()]=Double.MAX_VALUE;
						//				diffMatrix[1][j][i][op]=Double.MAX_VALUE;
					}
				}
				else{
					diffMatrix[or.ordinal()][i][j]=Float.MAX_VALUE;
					diffMatrix[op][j][i]=Float.MAX_VALUE;
				}
			}
		}
	}
}
