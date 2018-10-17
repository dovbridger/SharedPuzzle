package puzzle;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class PuzzlePart {
	int partSize, h, w;
	BufferedImage img;
	BufferedImage importanceImg;
	public double imgArray[][][];
	double importance[][];
	ArrayList<LABVector> imgCols;
	ArrayList<LABVector>  imgRows;
	boolean[][] burntPixels;
	
	public PuzzlePart(BufferedImage _img, boolean[][] _burntPixels, double[][] _importance) { //Constructor for RGB, burnt pixels may or may not come from file
		importance = _importance;
		img=_img;
		h=img.getHeight();
		w=img.getWidth();
		partSize=w;
		Color green = new Color(0,255,0);
		burntPixels = new boolean[h][w];
		imgArray=new double[h][w][3];
		for(int x=0;x<w;x++){
			for(int y=0;y<h;y++){
				double lab[]=new double[3];
				int RGB=img.getRGB(x, y);
				Color c = new Color(RGB);
				int B=c.getBlue();
				int G=c.getGreen();
				int R=c.getRed();
				ConvColor.rgb2lab(R, G, B, lab);
				imgArray[y][x][0]=lab[0];
				imgArray[y][x][1]=lab[1];
				imgArray[y][x][2]=lab[2];		
				
				if (Global.readBurntFile.equals(Global.PROJECT_PATH) && Global.burnByColor && RGB == green.getRGB()){
					burntPixels[y][x] = true;
				}
			}
		}
		generalConstructor(_burntPixels);
	}
	
	public PuzzlePart(BufferedImage _img ,boolean[][] _burntPixels,double _imgArray[][][]){	//Constructor for LAB, burnt pixels come from file
		img=_img;
		h=img.getHeight();
		w=img.getWidth();
		partSize=w;
		imgArray = _imgArray;
		generalConstructor(_burntPixels);
	}
	
	public void generalConstructor(boolean[][]_burntPixels){	//Constructor for the things that are comon for RGB and LAB
		if (!Global.readBurntFile.equals(Global.PROJECT_PATH)){
			burntPixels = _burntPixels;
		}	
		imgCols=new ArrayList<LABVector>();
		imgRows=new ArrayList<LABVector>();
		LABVector c0 = getColI(0);
		LABVector c1 = getColI(1);
		imgCols.add(c0.sub(c1).add(c0));
		imgCols.add(c0);
		imgCols.add(c1);
		LABVector r0 = getRowI(0);
		LABVector r1 = getRowI(1);
		imgRows.add(r0.sub(r1).add(r0));
		imgRows.add(r0);
		imgRows.add(r1);
		for(int i=2;i<partSize-2;i++){
			imgCols.add(getColI(i));
			imgRows.add(getRowI(i));
		}
		LABVector c26 = getColI(partSize-2);
		LABVector c27 = getColI(partSize-1);
		imgCols.add(c26);
		imgCols.add(c27);
		imgCols.add(c27.sub(c26).add(c27));
		LABVector r26 = getRowI(partSize-2);
		LABVector r27 = getRowI(partSize-1);
		imgRows.add(r26);
		imgRows.add(r27);
		imgRows.add(r27.sub(r26).add(r27));
		
	}
	
	public boolean[] shadeByCol(int direction){
		int count = 0;
		boolean[] result = new boolean[partSize];
		int x = 0;
		int y = 0;
		for (int i=0; i<partSize; i++){
			switch (direction){
			case 0:
				y = 0;
				x = i;
				break;
			case 1:
				y = partSize-1;
				x = i;
				break;
			case 2:
				y = i;
				x = 0;
				break;
			case 3:
				y = i;
				x = partSize-1;
			}
			int RGB=img.getRGB(x, y);
			Color c = new Color(RGB);
			int B=c.getBlue();
			int G=c.getGreen();
			int R=c.getRed();
			if (R==0 && G==255 && B==0){
				result[i] = true;
				count++;
			}
		}
		System.out.println(count);
		return result;
	}
	public double[] getPixel(int x, int y){ //Old method, not sure what it was for
		double pix[]=new double[3];
		pix[0]=imgArray[y][x][0];
		pix[1]=imgArray[y][x][1];
		pix[2]=imgArray[y][x][2];
		return pix;
	}
	
	public void getBlock(Pixel[][]output,int startX, int startY){
		double l,a,b;
		for (int y=0; y<output.length; y++){
			for (int x=0; x<output[0].length; x++){
				l = imgArray[y+startY][x+startX][0];
				a = imgArray[y+startY][x+startX][1];
				b = imgArray[y+startY][x+startX][2];
				double _importance = importance[y+startY][x+startX];
				output[y][x] = new Pixel(l,a,b,_importance);
			}
		}
	}
	public LABVector getColI(int x){
		int blockSize = Global.diffBlockSize;
		int numBlocks,remainder;
		if (Global.diffBlockSliding){
			numBlocks = h - blockSize + 1;
			remainder = 0;
		}else{
			numBlocks = h/blockSize;
			remainder = h % blockSize;
		}
		Pixel pixels[]=new Pixel[numBlocks];
		Pixel pixelBlock[][] = new Pixel[1][blockSize]; //The reason these arrays are 2D is to be compatible with the "average pixel from block" constructor
		Pixel lastPixelBlock[][] = new Pixel[1][blockSize+remainder];
		int index = -1;
		Pixel[][] block;
		for(int i=0;i<numBlocks;i++){
			if (i==numBlocks-1){ //Last Block
				block = lastPixelBlock;
			}else{
				block = pixelBlock;
			}
			for (int j=0; j<block[0].length; j++){
				index++;
				block[0][j] = new Pixel(imgArray[index][x][0],imgArray[index][x][1],imgArray[index][x][2],importance[index][x]);
			}
			pixels[i]= new Pixel(block);
			pixels[i].burnt = burntPixels[index][x];
			if (Global.diffBlockSliding){
				index = index-blockSize+1;
			}
		}
		return new LABVector(pixels);
	}
	
	public LABVector getCol(int x){
		return imgCols.get(x);
	}
	
	
	public LABVector getRowI(int y){
		int blockSize = Global.diffBlockSize;
		int numBlocks,remainder;
		if (Global.diffBlockSliding){
			numBlocks = w - blockSize + 1;
			remainder = 0;
		}else{
			numBlocks = w/blockSize;
			remainder = w % blockSize;
		}
		Pixel pixels[]=new Pixel[numBlocks];
		Pixel pixelBlock[][] = new Pixel[1][blockSize];//The reason these arrays are 2D is to be compatible with the "average pixel from block" constructor
		Pixel lastPixelBlock[][] = new Pixel[1][blockSize+remainder]; //This is redundant in the "slidind block" clase
		Pixel[][]block;
		int index = -1;
		for(int i=0;i<numBlocks;i++){
			if (i==numBlocks-1){ //Last Block
				block = lastPixelBlock;
			}else{
				block = pixelBlock;
			}
			for (int j=0; j<block[0].length; j++){
				index++;
				block[0][j] = new Pixel(imgArray[y][index][0],imgArray[y][index][1],imgArray[y][index][2],importance[y][index]);
			}
			pixels[i]= new Pixel(block);
			pixels[i].burnt = burntPixels[y][index];
			if (Global.diffBlockSliding){
				index = index-blockSize+1;
			}
		}
		return new LABVector(pixels);
	}
	
	public LABVector getRow(int y){
		return imgRows.get(y);
	}
	public static void main(String[]args){
		double lab[]=new double[3];
		int rgb[] = new int[3];
		ConvColor.rgb2lab(0, 0, 255, lab);
		ConvColor.lab2rgb(lab[0], lab[1], lab[2], rgb);
		System.out.println(lab[0]+" , "+lab[1]+" , "+lab[2]);
		System.out.println(rgb[0]+" , "+rgb[1]+" , "+rgb[2]);
		
		
		double labMax[]=new double[3];
		double labMin[]=new double[3];
		for (int r=0; r<256; r++){
			for (int g=0; g<256; g++){
				for (int b=0; b<256; b++){
					ConvColor.rgb2labOld(r, g, b, lab);
					for (int k=0; k<3; k++){
						if (labMax[k] < lab[k]){
							labMax[k] = lab[k];
						}
						if (labMin[k] > lab[k]){
							labMin[k] = lab[k];
						}
					}
				}
			}
		}
		System.out.println("Max: "+labMax[0]+" , "+labMax[1]+" , "+labMax[2]);
		System.out.println("Min: "+labMin[0]+" , "+labMin[1]+" , "+labMin[2]);
		System.out.println("Range: "+(labMax[0]-labMin[0])+" , "+(labMax[1]-labMin[1])+" , "+(labMax[2]-labMin[2]));

	}
}
