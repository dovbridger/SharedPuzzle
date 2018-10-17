package oldPuzzle;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class PuzzlePart {
	int partSize, h, w;
	BufferedImage img;
	double imgArray[][][];
	ArrayList<LABVector> imgCols;
	ArrayList<LABVector>  imgRows;
	
	public PuzzlePart(BufferedImage _img) {
		img=_img;
		h=img.getHeight();
		w=img.getWidth();
		partSize=w;
		imgArray=new double[w][h][3];
		for(int i=0;i<w;i++){
			for(int j=0;j<h;j++){
				double lab[]=new double[3];
				int RGB=img.getRGB(j, i);
				Color c = new Color(RGB);
				int B=c.getBlue();
				int G=c.getGreen();
				int R=c.getRed();
				ConvColor.rgb2lab(R, G, B, lab);
				//ConvColor.rgb2lab(255, 255, 255, lab);
				imgArray[i][j][0]=lab[0];
				imgArray[i][j][1]=lab[1];
				imgArray[i][j][2]=lab[2];
			}
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
	
	public double[] getPixel(int x, int y){
		double pix[]=new double[3];
		pix[0]=imgArray[x][y][0];
		pix[1]=imgArray[x][y][1];
		pix[2]=imgArray[x][y][2];
		return pix;
	}
	
	public LABVector getColI(int x){
		double pixels[][]=new double[h][3];
		for(int i=0;i<h;i++){
			pixels[i][0]=imgArray[i][x][0];
			pixels[i][1]=imgArray[i][x][1];
			pixels[i][2]=imgArray[i][x][2];
		}
		return new LABVector(pixels);
	}
	
	public LABVector getCol(int x){
		return imgCols.get(x);
	}
	
	
	public LABVector getRowI(int y){
		double pixels[][]=new double[w][3];
		for(int i=0;i<h;i++){
			pixels[i][0]=imgArray[y][i][0];
			pixels[i][1]=imgArray[y][i][1];
			pixels[i][2]=imgArray[y][i][2];
		}
		return new LABVector(pixels);
	}
	
	public LABVector getRow(int y){
		return imgRows.get(y);
	}

}
