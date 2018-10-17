package puzzle;


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
			int edgeSizeX = Global.sizeOverlap;
			int edgeSizeY = Global.sizeOverlap;
			
			if (or.ordinal()<=1){
				edgeSizeX = partSize;
			}else{
				edgeSizeY=partSize;
			}
			
			Pixel[][]p1Extrapolated = null;
			Pixel[][]p2Extrapolated = null;
			Pixel[][]p1Real = null;
			Pixel[][]p2Real = null;
			
			LABVector[]p1ExtrapolatedV = null;
			LABVector[]p2ExtrapolatedV = null;
			LABVector[]p1RealV = null;
			LABVector[]p2RealV = null;
			PuzzlePart p1=puzzleParts.getLABPart(i);
			if (Global.pixellBlockCalcDiff){   
				p1Extrapolated = new Pixel[edgeSizeY][edgeSizeX];
				p1Real = new Pixel[edgeSizeY][edgeSizeX];
				getEdges(p1,partSize,p1Real, p1Extrapolated, or);
			}else{
				p1ExtrapolatedV = new LABVector[Global.sizeOverlap];
				p1RealV = new LABVector[Global.sizeOverlap];
				getEdges(p1,partSize,p1RealV,p1ExtrapolatedV,or);
			}
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
				if(true){//diffB<1250){ //Dov - Used to be 250, anyway - now that there is overlap different borders should not be considered a problem
					float diff12=0, diff21=0;
					PuzzlePart p2=puzzleParts.getLABPart(j);
					if (Global.pixellBlockCalcDiff){ 
						p2Extrapolated = new Pixel[edgeSizeY][edgeSizeX];
						p2Real = new Pixel[edgeSizeY][edgeSizeX];
						getEdges(p2,partSize,p2Real, p2Extrapolated, or.opposite());
						
						diff12=(float) getDiff(p1Real,p2Extrapolated,Global.diffBlockSize,Global.diffBlockSize);
						diff21=(float) getDiff(p2Real,p1Extrapolated,Global.diffBlockSize,Global.diffBlockSize);
					}else{
						p2ExtrapolatedV = new LABVector[Global.sizeOverlap];
						p2RealV = new LABVector[Global.sizeOverlap];
						getEdges(p2,partSize,p2RealV,p2ExtrapolatedV,or.opposite());
					
						diff12=(float) getDiff(p1RealV,p2ExtrapolatedV);
						diff21=(float) getDiff(p2RealV,p1ExtrapolatedV);
					}

					diffMatrix[or.ordinal()][i][j]=diff12;
					diffMatrix[op][j][i]=diff21;

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
	//This code is for using LABVectors:
	public static double getDiff(LABVector[]real, LABVector[]extrapolated){
		double result = 0;
		for (int k=0; k<real.length; k++){
			result += 0.01*(real[k].sub(extrapolated[k]).pow(2).sum());
		}
		return result;
	}
	
	

	
	//This code is for using pixel blocks:
	public static double getDiff(Pixel[][]real, Pixel[][]extrapolated, int blockSizeX, int blockSizeY){
		int numBlocksY = real.length/blockSizeY;
		int numBlocksX = real[0].length/blockSizeX;
		double result = 0;
		double importanceFactorSum = 0;
		for (int blockY=0; blockY<numBlocksY; blockY++){
			for (int blockX=0; blockX<numBlocksX; blockX++){
				int endY = (blockY+1)*blockSizeY;
				if (blockY == numBlocksY - 1){//last block
					endY = real.length;
				}
				int endX = (blockX+1)*blockSizeX;
				if (blockX == numBlocksX - 1){//last block
					endX = real[0].length;
				}
				int startY = blockY*blockSizeY;
				int startX = blockX*blockSizeX;
				Pixel[][]currentBlockReal = new Pixel[endY-startY][endX-startX];
				Pixel[][]currentBlockExtrapolated = new Pixel[endY-startY][endX-startX];
				for (int y=startY; y<endY; y++){
					for (int x=startX; x<endX; x++){
						currentBlockReal[y-startY][x-startX] = real[y][x];
						currentBlockExtrapolated[y-startY][x-startX] = extrapolated[y][x];
					}
				}
				int blockSize = (endY-startY)*(endX-startX);

				double lastBlockFactor = blockSize/((double)(blockSizeX*blockSizeY));
				double[] colorDiffAndImportance = getColorDiff(currentBlockReal,currentBlockExtrapolated);
				double colorDiff = colorDiffAndImportance[0];
				double importanceFactor = colorDiffAndImportance[1];
				double gradDiff = getGradDiff(currentBlockReal,currentBlockExtrapolated);
				double currentBlockDiff = Global.colorDiffRatio * colorDiff + (1-Global.colorDiffRatio)*gradDiff;
				importanceFactorSum = importanceFactorSum + importanceFactor;
				result = result + importanceFactor*lastBlockFactor*currentBlockDiff;
			}
		}
		return result/importanceFactorSum;
	}
	
	public static double getGradDiff(Pixel[][]currentBlockReal, Pixel[][]currentBlockExtrapolated){
		if (Global.diffBlockSize == 1){
			return 0;
		}
		double xGradReal = 0;
		double yGradReal = 0;
		double xGradExtrapolated = 0;
		double yGradExtrapolated = 0;
		for (int y=1; y<currentBlockReal.length; y++){
			for (int x=0; x<currentBlockReal[0].length; x++){
				yGradReal+= Pixel.getSingleGrad(currentBlockReal[y][x], currentBlockReal[y-1][x]);
				yGradExtrapolated += Pixel.getSingleGrad(currentBlockExtrapolated[y][x], currentBlockExtrapolated[y-1][x]);
			}
		}
		for (int y=0; y<currentBlockReal.length; y++){
			for (int x=1; x<currentBlockReal[0].length; x++){
				xGradReal+= Pixel.getSingleGrad(currentBlockReal[y][x], currentBlockReal[y][x-1]);
				xGradExtrapolated += Pixel.getSingleGrad(currentBlockExtrapolated[y][x], currentBlockExtrapolated[y][x-1]);
			}
		}
		double result = Math.abs(xGradReal - xGradExtrapolated) + Math.abs(yGradReal - yGradExtrapolated);
		int size = (currentBlockReal.length-1)*(currentBlockReal[0].length-1);
		return result/size;
	}
	
	public static double[] getColorDiff(Pixel[][]currentBlockReal, Pixel[][]currentBlockExtrapolated){
		
		Pixel avePixelReal = new Pixel(currentBlockReal);
		Pixel avePixelExtrapolated = new Pixel(currentBlockExtrapolated);
		double[] result = {avePixelReal.squareDiff(avePixelExtrapolated),avePixelReal.importance+avePixelExtrapolated.importance};
		return result;
	}
	//This code is for using LABVectors:
	public static void getEdges(PuzzlePart p, int partSize, LABVector[]real, LABVector[]extrapolated, Orientation or){
		boolean isBegining = false;
		boolean isRow = false;
		switch(or){
		case DOWN:
			isBegining = false;
			isRow = true;
			break;
		case RIGHT:			
			isBegining = false;
			isRow = false;
			break;
		case LEFT:
			isBegining = true;
			isRow = false;
			break;
		case UP:
			isBegining = true;
			isRow = true;
			break;
		}
		
		int sign = (isBegining?1:-1);
		for (int k=1; k<=Global.sizeOverlap; k++){
			int index = (isBegining?k:partSize-k+1);
			if (isRow){
				extrapolated[k-1] = p.getRow(index);
				real[k-1] = p.getRow(index+sign*Global.sizeOverlap);
			}
			else{
				extrapolated[k-1] = p.getCol(index);
				real[k-1] = p.getCol(index+sign*Global.sizeOverlap);
			}
		}
	}
	
	//This code is for using pixel blocks:
	public static void getEdges(PuzzlePart p, int partSize, Pixel[][]real, Pixel[][]extrapolated, Orientation or){
		int startXReal = 0;
		int startYReal = 0;
		int startXExtrapolated = 0;
		int startYExtrapolated = 0;

		switch(or){
		case DOWN:
			startYReal = partSize - 2*Global.sizeOverlap;
			startYExtrapolated = partSize - Global.sizeOverlap;
			break;
		case RIGHT:			
			startXReal = partSize - 2*Global.sizeOverlap;
			startXExtrapolated = partSize - Global.sizeOverlap;
			break;
		case LEFT:
			startXReal = Global.sizeOverlap;
			break;
		case UP:
			startYReal = Global.sizeOverlap;
			break;
		}
		
		p.getBlock(real,startXReal,startYReal);
		p.getBlock(extrapolated,startXExtrapolated,startYExtrapolated);
		
	}
}
