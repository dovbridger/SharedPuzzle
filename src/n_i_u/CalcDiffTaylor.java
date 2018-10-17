package n_i_u;
import puzzle.PuzzleParts;
import puzzle.LABVector;
import puzzle.Orientation;
import puzzle.PuzzlePart;


public class CalcDiffTaylor implements Runnable{
	PuzzleParts puzzleParts;
	Orientation or;
	int ii,jj,partSize,partsNum;
	float diffMatrix[][][];
	int itNum;
	int numVecs;

	public CalcDiffTaylor(float[][][] diffMatrix2,int _partSize,int _partsNum,PuzzleParts _puzzleParts,Orientation _or, int _i, int _j,int _itNum) {
		puzzleParts=_puzzleParts;
		or=_or;
		ii=_i;
		jj=_j;
		partSize=_partSize;
		partsNum=_partsNum;
		diffMatrix=diffMatrix2;
		itNum=_itNum;
		numVecs = 3; //Dov - how far from the edge do we want to look
	}


	@Override
	public void run() {
		for(int i=ii;i<ii+itNum+1&&i<partsNum;i++){
			PuzzlePart p1=puzzleParts.getLABPart(i);
			LABVector[] p1Edge = getEdge(p1,or);

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
				if(diffB<1250){ //Dov - There is a chance that these to pieces are neighbors and we need to fully calculate their difference

					float diff21=0, diff12=0;
					PuzzlePart p2=puzzleParts.getLABPart(j);
					LABVector[] p2Edge = getEdge(p2,opposite());
//					if (i==201 && j==200 && or.ordinal() == 2){
//						BufferedImage p1Image = puzzleParts.getRGBPart(i);
//						BufferedImage p2Image = puzzleParts.getRGBPart(j);
//						try {
//							ImageIO.write(p1Image, "png",new File("201.png"));
//							ImageIO.write(p2Image, "png", new File("200.png"));
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
//						System.out.println("p1 0");
//						p1Edge[0].print();
//						System.out.println("p1 1");
//						p1Edge[1].print();
//						System.out.println("p1 2");
//						p1Edge[2].print();
//						System.out.println("p2 0");
//						p2Edge[0].print();
//						System.out.println("p2 1");
//						p2Edge[1].print();
//						System.out.println("p2 2");
//						p2Edge[2].print();
//					}
					diff12 = getDiff(p1Edge,p2Edge);
					diff21 = getDiff(p2Edge,p1Edge);

				
					diffMatrix[or.ordinal()][i][j]=diff21;
					diffMatrix[op][j][i]=diff12;
					if(i==j){
						diffMatrix[or.ordinal()][i][j]=Float.MAX_VALUE;
						diffMatrix[op][j][i]=Float.MAX_VALUE;
					}
				}
				else{
					diffMatrix[or.ordinal()][i][j]=Float.MAX_VALUE;
					diffMatrix[op][j][i]=Float.MAX_VALUE;
				}
			}
		}
	}
	
	public static float getDiff(LABVector[] lEdges, LABVector[] rEdges){
		boolean pr = false; //print result
		double diff = 0;
		double svRatio = 0.6; //side vertical
		double[] distanceRatio = {0.6,0.2,0.2,0}; //distance = {close_close,far_close,close_far,far_far}
		LABVector[][] approximated = LABVector.approximateVector(lEdges);
		LABVector[][] subAbs = new LABVector[4][3];
		double[][] confidentSums = new double[4][3];
		double[][] confidence = new double[4][3];
		double totalRealPixels = 0;
		for (int i=0; i<4; i++){
			for (int j=0; j<3; j++){
				subAbs[i][j] = rEdges[i/2].sub(approximated[i][j]).abs();
				confidence[i][j] = subAbs[i][j].countReal();
				totalRealPixels = totalRealPixels + confidence[i][j];
			}
		}
		
		for (int i=0; i<4; i++){
			for (int j=0; j<3; j++){
				confidence[i][j] = 12*confidence[i][j]/totalRealPixels;
				confidentSums[i][j] = confidence[i][j] * subAbs[i][j].sum();
			}
			diff = diff + distanceRatio[i]*(svRatio*confidentSums[i][0]+ (1-svRatio)*0.5*(confidentSums[i][1] + confidentSums[i][2]));
		}
		if (pr){
			LABVector.print(approximated);
			LABVector.print(subAbs);
		}
		
		return (float)diff;
		
	}
	
	public LABVector[]getEdge(PuzzlePart p,Orientation o){
		LABVector[] result = new LABVector[numVecs];
		switch(o){
		case DOWN:
			for (int i=0; i<numVecs; i++){
				result[i] = p.getRow(partSize-i); //2 extra parts exist int getRow because of the original calculation of the expected edge of the adjacent piece (hence we start at partSize)
			}
			break;
		case RIGHT:
			for (int i=0; i<numVecs; i++){
				result[i] = p.getCol(partSize-i);
			}
			break;
		case LEFT:
			for (int i=0; i<numVecs; i++){
				result[i] = p.getCol(i+1);
			}
			break;
		case UP:
			for (int i=0; i<numVecs; i++){
				result[i] = p.getRow(i+1);
			}
			break;
		}
		return result;
	}
	
	public Orientation opposite(){
		Orientation opp = Orientation.UP;
		switch(or){
		case UP:
			opp = Orientation.DOWN;
			break;
		case DOWN:
			opp = Orientation.UP;
			break;
		case LEFT:
			opp = Orientation.RIGHT;
			break;
		case RIGHT:
			opp = Orientation.LEFT;
			break;
		}
		return opp;
	}
}
