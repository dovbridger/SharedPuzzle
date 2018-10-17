package puzzle;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import multi_thread.ImageNumber;


public class PuzzleParts{
	
	int partSize, h, w, nh, nw,partsNum,missPartsNum,burnPartsNum,darkPartsNum,lowEntPartsNum;
	BufferedImage img;
	BufferedImage puzzlePartsRGB[];
	BufferedImage puzzlePartsNumbered[];
	double[][][] puzzlePartsImportance;
	double[][][] puzzlePartsSaliency;
	double[] puzzlePartsAverageSaliency;
	public PuzzlePart puzzlePartsLAB[];
	Boolean isEmptyV[],isEmptyB[],isEmptyD[];
//	double entropy[];
	public double avgBorder[][][];
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
		puzzlePartsNumbered=new BufferedImage[nh*nw];
		puzzlePartsLAB=new PuzzlePart[nh*nw];
		puzzlePartsImportance = new double[nh*nw][][];
		puzzlePartsSaliency = new double[nh*nw][][];
		isEmptyV=new Boolean[partsNum];
		isEmptyB=new Boolean[partsNum];
		isEmptyD=new Boolean[partsNum];
//		entropy=new double[partsNum];
		avgBorder=new double[3][4][partsNum];
		createPuzzlePartsImportance();
		createPuzzlePartsSaliency();
		createPuzzlePartsRGB();
		createPuzzlePartsLAB();

		findEmpty();
		findBurn();
		findDark();
		findB();
//		findEntropyA();
//		System.out.print("BLA");
		
	}

	private void findB() { //Dov-Finds all 4 average borders for each piece
		for(int i=0;i<partsNum;i++){
			isEmptyB[i]=true;
			for(Orientation or:Orientation.values()){
				int realPixels = 0;
				double L = 0;
				double A = 0;
				double B = 0;
				int x = 0;
				int y = 0;
				for (int k=0; k<partSize; k++){
					switch (or){
					case DOWN:
						y = partSize-1;
						x = k;
						break;
					case LEFT:
						y = k;
						x = 0;		
						break;
					case RIGHT:
						y = k;
						x = partSize-1;
						break;
					case UP:
						y = 0;
						x = k;
						break;	
					}
					if (!puzzlePartsLAB[i].burntPixels[y][x]){
						realPixels++;
						L+=puzzlePartsLAB[i].imgArray[y][x][0];
						A+=puzzlePartsLAB[i].imgArray[y][x][1];
						B+=puzzlePartsLAB[i].imgArray[y][x][2];
					}
				}
				avgBorder[0][or.ordinal()][i]=L/((double)realPixels);
				avgBorder[1][or.ordinal()][i]=A/((double)realPixels);
				avgBorder[2][or.ordinal()][i]=B/((double)realPixels);
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
		
		for (int i = 0; i < nh * nw; i++){
			if (Global.intermediateOutput[1]>=0 || Global.number || Global.GUI_DEBUG){
				String numberFileName = Global.NUMBERS_PATH+partSize+"//"+i+".png";
				try {
					puzzlePartsNumbered[i] = ImageIO.read(new File(numberFileName));
					ImageNumber.createNumberedImage(puzzlePartsRGB[i], puzzlePartsNumbered[i]);
				}catch (IOException e) {
					System.out.println("No number file: " + numberFileName +" , setting number to false and canceling intermediate output");
					Global.number = false;
					Global.intermediateOutput[0] = -1;
					Global.intermediateOutput[1] = -1;
	//				e.printStackTrace();
					puzzlePartsNumbered = puzzlePartsRGB; //The numbered parts will just be the regular parts
					return;
				}
			}
		}
	}
	
	private void createPuzzlePartsImportance() {
		for(int i=0;i<nh*nw;i++){
			BufferedImage tempImg = null;
			if (Global.useImportance){//Check if there is a need to calculate importance
				String name = Global.pathToImportance+Global.puzzleName+i+Global.FILE_TYPE;
				try {
					tempImg = ImageIO.read(new File(name));
				} catch (IOException e) {
					System.out.println("No Importance file by the name: "+name);
					e.printStackTrace();
				}
			}
			puzzlePartsImportance[i] = new double[partSize][partSize];
			for(int x=0;x<partSize;x++){
				for(int y=0;y<partSize;y++){
					if (Global.useImportance){
						int RGB = tempImg.getRGB(x,y);
						Color c = new Color(RGB);
						double impoartanceVal = (double)c.getRed()/255;
						puzzlePartsImportance[i][y][x] = impoartanceVal;
					}else{
						puzzlePartsImportance[i][y][x] = 1;
					}
				}
			}
		}			
	}
	
	private void createPuzzlePartsSaliency() {
		puzzlePartsAverageSaliency = new double[partsNum];
		for(int i=0;i<nh*nw;i++){
			BufferedImage tempImg = null;
			if (Global.NEXT_TO_PLACE_SALIENCY_FACTOR>0||Global.SALIENCY_FACTOR>0||Global.FIRST_PIECE_CHOICE == Global.FirstPieceChoice.saliency){ //Check if there is a need to calculate saliency
				String name = Global.pathToSaliency+Global.puzzleName+i+Global.FILE_TYPE;
				try {
					tempImg = ImageIO.read(new File(name));
				} catch (IOException e) {
					System.out.println("No Saliency file by the name: "+name);
					System.out.println("NEXT_TO_PLACE_SALIENCY_FACTOR and SALIENCY_FACTOR are being set to 0");
					Global.NEXT_TO_PLACE_SALIENCY_FACTOR = 0;
					Global.SALIENCY_FACTOR = 0;
					if (Global.FIRST_PIECE_CHOICE == Global.FirstPieceChoice.saliency){
						Global.FIRST_PIECE_CHOICE = Global.FirstPieceChoice.normal;
						System.out.println("FIRST_PIECE_CHOICE is being set to normal");
					}
					return;
				}
			}
			double saliencySum = 0;
			puzzlePartsSaliency[i] = new double[partSize][partSize];
			for(int x=0;x<partSize;x++){
				for(int y=0;y<partSize;y++){
					if (Global.NEXT_TO_PLACE_SALIENCY_FACTOR>0||Global.SALIENCY_FACTOR>0||Global.FIRST_PIECE_CHOICE == Global.FirstPieceChoice.saliency){ //Can probably get rid of this condition because it will always be true if we have reached this part of the method
						int RGB = tempImg.getRGB(x,y);
						Color c = new Color(RGB);
						double saliencyVal = (double)c.getRed()/255;
						puzzlePartsSaliency[i][y][x] = saliencyVal;
						saliencySum = saliencySum+Math.pow(saliencyVal, Global.aveSalPower);
					}else{
						puzzlePartsSaliency[i][y][x] = 0;
					}
				}
			}
			puzzlePartsAverageSaliency[i] = saliencySum/(partSize*partSize);
		}			
	}


	private void createPuzzlePartsLAB(){
		boolean[][][]burntPixels = null;
		if (Global.readBurntFile.equals(Global.PROJECT_PATH)){
			burntPixels = new boolean[partsNum][][];
		}else{
			SaveworthyPuzzleData t = null;
			try {
				t = (SaveworthyPuzzleData)CopyObject.read(Global.readBurntFile);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			burntPixels = t.burntPixels;
		}
		for(int i=0;i<partsNum;i++){
			puzzlePartsLAB[i]=new PuzzlePart(puzzlePartsRGB[i],burntPixels[i],puzzlePartsImportance[i]);
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
	public void colorFrame(int partNum, Color c, int offest, int side){	//Dov- An added method for coloring the frame of a part
		BufferedImage partImage = puzzlePartsRGB[partNum];
		for (int i=offest; i<partImage.getHeight()-offest; i++ ){
			if (side != 2){
				partImage.setRGB(offest, i, c.getRGB());
			}
			if (side != 3){
				partImage.setRGB(partImage.getWidth()-offest-1,i,c.getRGB());
			}
		}
		for (int j=offest; j<partImage.getWidth()-offest; j++ ){
			if (side!=0){
				partImage.setRGB(j, offest, c.getRGB());
			}
			if (side!=1){
				partImage.setRGB(j,partImage.getHeight()-offest-1,c.getRGB());
			}
		}
	}
	public void saveBurntFile(){
		if (Global.writeBurntFile.equals(Global.PROJECT_PATH)){
			return;
		}
		boolean[][][] bPixels = new boolean[partsNum][][];
		for (int i=0; i<partsNum; i++){
			bPixels[i] = puzzlePartsLAB[i].burntPixels;
		}
		SaveworthyPuzzleData t = new SaveworthyPuzzleData(bPixels);
		try {
			CopyObject.write(t, Global.writeBurntFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void burnBorders(){ //Dov- Method to shade borders for display
		Color green = new Color(0,255,0);
		for (int i=0; i<partsNum; i++){
			for (int y=0; y<puzzlePartsLAB[i].h; y++){
				for (int x=0; x<puzzlePartsLAB[i].h; x++){
					if (puzzlePartsLAB[i].burntPixels[y][x]){
						puzzlePartsRGB[i].setRGB(x, y, green.getRGB());
					}
				}
			}
		}
	}
}
