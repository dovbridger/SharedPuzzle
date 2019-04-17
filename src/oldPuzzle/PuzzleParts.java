package oldPuzzle;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class PuzzleParts {

	int partSize, h, w, nh, nw,partsNum,missPartsNum,burnPartsNum,darkPartsNum,lowEntPartsNum;
	BufferedImage img;
	BufferedImage puzzlePartsRGB[];
	PuzzlePart puzzlePartsLAB[];
	Boolean isEmptyV[],isEmptyB[],isEmptyD[];
//	double entropy[];
	double avgBorder[][][];

	public PuzzleParts(BufferedImage _img,int _partSize) {
		partSize=_partSize;
		img=_img;
		h=img.getHeight();
		w=img.getWidth();
		nh=h/partSize;
		nw=w/partSize;
		partsNum=nh*nw;
		missPartsNum=0;
		burnPartsNum=0;
		darkPartsNum=0;
		lowEntPartsNum=0;
		puzzlePartsRGB=new BufferedImage[nh*nw];
		puzzlePartsLAB=new PuzzlePart[nh*nw];
		isEmptyV=new Boolean[partsNum];
		isEmptyB=new Boolean[partsNum];
		isEmptyD=new Boolean[partsNum];
//		entropy=new double[partsNum];
		avgBorder=new double[3][4][partsNum];
		createPuzzlePartsRGB();
		createPuzzlePartsLAB();
		findEmpty();
		findBurn();
		findDark();
		findB();
//		findEntropyA();
//		System.out.print("BLA");
		
	}
	
	private void findB() {
		for(int i=0;i<partsNum;i++){
			isEmptyB[i]=true;
		
			for(Orientation or:Orientation.values()){
				int B = 0;
				int G = 0;
				int R = 0;
				int jj;
				switch (or){
				case DOWN:
					for(int k=0;k<partSize;k++){
//						int RGB=puzzlePartsRGB[i].getRGB(k,partSize-1);
//						Color c = new Color(RGB);
						B+=puzzlePartsLAB[i].imgArray[partSize-1][k][0];
						G+=puzzlePartsLAB[i].imgArray[partSize-1][k][1];
						R+=puzzlePartsLAB[i].imgArray[partSize-1][k][2];
						avgBorder[0][or.ordinal()][i]=R/partSize;
						avgBorder[1][or.ordinal()][i]=G/partSize;
						avgBorder[2][or.ordinal()][i]=B/partSize;
					}
					break;
				case LEFT:
					for(int k=0;k<partSize;k++){
//						int RGB=puzzlePartsRGB[i].getRGB(0,k);
//						Color c = new Color(RGB);
						B+=puzzlePartsLAB[i].imgArray[k][0][0];
						G+=puzzlePartsLAB[i].imgArray[k][0][1];
						R+=puzzlePartsLAB[i].imgArray[k][0][2];
						avgBorder[0][or.ordinal()][i]=R/partSize;
						avgBorder[1][or.ordinal()][i]=G/partSize;
						avgBorder[2][or.ordinal()][i]=B/partSize;
					}
					break;
				case RIGHT:
					for(int k=0;k<partSize;k++){
//						int RGB=puzzlePartsRGB[i].getRGB(partSize-1,k);
//						Color c = new Color(RGB);
						B+=puzzlePartsLAB[i].imgArray[k][partSize-1][0];
						G+=puzzlePartsLAB[i].imgArray[k][partSize-1][1];
						R+=puzzlePartsLAB[i].imgArray[k][partSize-1][2];
						avgBorder[0][or.ordinal()][i]=R/partSize;
						avgBorder[1][or.ordinal()][i]=G/partSize;
						avgBorder[2][or.ordinal()][i]=B/partSize;
					}
					break;
				case UP:
					for(int k=0;k<partSize;k++){
//						int RGB=puzzlePartsRGB[i].getRGB(k,0);
//						Color c = new Color(RGB);
						B+=puzzlePartsLAB[i].imgArray[0][k][0];
						G+=puzzlePartsLAB[i].imgArray[0][k][1];
						R+=puzzlePartsLAB[i].imgArray[0][k][2];
						avgBorder[0][or.ordinal()][i]=R/partSize;
						avgBorder[1][or.ordinal()][i]=G/partSize;
						avgBorder[2][or.ordinal()][i]=B/partSize;
					}
					break;
				default:
					break;
				
				}
	

			}
		}
		
	}

	PuzzlePart[] getPuzzlesparts(){
		return puzzlePartsLAB;
	}
//	
//	private void findEntropyA(){
//		for(int i=0;i<partsNum;i++){
//			entropy[i]=findEntropy(i);
////			System.out.println(entropy[i]);
//			if(entropy[i]<0.0000001/*0.000001*/)
//				lowEntPartsNum++;
//		}
//	}
	
//	private Double findEntropy(int i) {
//		BufferedImage pimg=puzzlePartsRGB[i];
//		int hist[]=find_hist(pimg,16);
//		Double res1=0.0,res2=0.0,res3=0.0;
//		for(int j=0;j<16;j++){
//			double h1=hist[j];
//			double h2=hist[j+16];
//			double h3=hist[j+32];
//			if(h1!=0) res1+=(-Math.log10(h1/partSize/partSize)*(h1/partSize/partSize));
//			if(h2!=0) res2+=(-Math.log10(h2/partSize/partSize)*(h2/partSize/partSize));
//			if(h3!=0) res3+=(-Math.log10(h3/partSize/partSize)*(h3/partSize/partSize));
//		}
//		return (res1+res2+res3)/3;
//	}

//	private int[] find_hist(BufferedImage pimg, int bins) {
//		int hist[]=new int[3*bins];
//		for(int i=0;i<bins;i++){
//			hist[i]=0;
//		}
//
//		for(int i=0;i<partSize;i++){
//			for(int j=0;j<partSize;j++){
//				int RGB=pimg.getRGB(i, j);
//				Color c = new Color(RGB);
//				int B=c.getBlue();
//				hist[B/bins]++;
//				int G=c.getGreen();
//				hist[G/bins+bins]++;
//				int R=c.getRed();
//				hist[R/bins+2*bins]++;
//			}
//		}
//		return hist;
//	}
	
	private void findEmpty() {
		for(int i=0;i<partsNum;i++){
			isEmptyV[i]=true;
			int B = 0;
			int G = 0;
			int R = 0;
			for(int j=0;j<partSize;j++){
				for(int k=0;k<partSize;k++){
					int RGB=puzzlePartsRGB[i].getRGB(j, k);
					Color c = new Color(RGB);
					B=c.getBlue();
					G=c.getGreen();
					R=c.getRed();
					if(R>0 || G>0 || B>0){
						isEmptyV[i]=false;
						break;
					}
				}
				if(R>0 || G>0 || B>0){
					isEmptyV[i]=false;
					break;
				}
			}
			if(isEmptyV[i])
				missPartsNum++;
		}
	}
	
	private void findBurn() {
		for(int i=0;i<partsNum;i++){
			isEmptyB[i]=true;
			int B = 0;
			int G = 0;
			int R = 0;
			for(int j=0;j<partSize;j++){
				for(int k=0;k<partSize;k++){
					int RGB=puzzlePartsRGB[i].getRGB(j, k);
					Color c = new Color(RGB);
					B=c.getBlue();
					G=c.getGreen();
					R=c.getRed();
					if(R<252 || G<252 || B<252){
						isEmptyB[i]=false;
						break;
					}
				}
				if(R<254 || G<254 || B<254){
					isEmptyB[i]=false;
					break;
				}
			}
			if(isEmptyB[i])
				burnPartsNum++;
		}
	}
	
	private void findDark() {

		for(int i=0;i<partsNum;i++){
			isEmptyD[i]=true;
			int B = 0;
			int G = 0;
			int R = 0;
			for(int j=0;j<partSize;j++){
				for(int k=0;k<partSize;k++){
					int RGB=puzzlePartsRGB[i].getRGB(j, k);
					Color c = new Color(RGB);
					B=c.getBlue();
					G=c.getGreen();
					R=c.getRed();
					if(R>1 || G>1 || B>1){
						isEmptyD[i]=false;
						break;
					}
				}
				if(R>1 || G>1 || B>1){
					isEmptyD[i]=false;
					break;
				}
			}
			if(isEmptyD[i])
				darkPartsNum++;
		}
	}

	public boolean isEmpty(int i){
		return isEmptyV[i];
	}
	
	public boolean isDark(int i){
		if(i<0 || i>partsNum-1)
			return false;
		return isEmptyD[i];
	}
	
//	public boolean isLowEnt(int i){
//		return entropy[i]<0.0000001;
//	}	
//	
//	public boolean isNLowEnt(int i){
//		return entropy[i]>0.0000001;
//	}
	

	public boolean isBurn(int i){
		if(i<0 || i>partsNum-1)
			return false;
		return isEmptyB[i];
	}


	private void createPuzzlePartsRGB() {
		for(int i=0;i<nh*nw;i++){
			BufferedImage nimg=new BufferedImage(partSize, partSize,BufferedImage.TYPE_INT_RGB);
			nimg.setRGB(0, 0, partSize, partSize, img.getRGB(i%nw*partSize, i/nw*partSize, partSize, partSize,  null, 0, partSize), 0, partSize);
			puzzlePartsRGB[i]=nimg;
		}

	}

	private void createPuzzlePartsLAB() {
		for(int i=0;i<nh*nw;i++){
			puzzlePartsLAB[i]=new PuzzlePart(puzzlePartsRGB[i]);
		}

	}

	public PuzzlePart getLABPart(int i){
		return puzzlePartsLAB[i];
	}

	public BufferedImage getRGBPart(int i){
		return puzzlePartsRGB[i];
	}

	public int getPartsNum(){
		return partsNum;
	}

	public int getMissPartsNum(){
		return missPartsNum;
	}
	

	public int getBurnPartsNum(){
		return burnPartsNum;
	}
	
	public int getDarkPartsNum(){
		return darkPartsNum;
	}
	
	public int getLowEntPartsNum(){
		return lowEntPartsNum;
	}

	public int hetH(){
		return nh;
	}

	public int hetW(){
		return nw;
	}
}
