package multi_thread;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class ImageNumber extends Thread {
	
	private int _row;
	private static BufferedImage originalImg;
	private static BufferedImage numberedImg;
	private static int height, width;
	
	public ImageNumber(int row){
		_row = row;
	}
	
	@Override
	public void run(){
		for (int col=0; col<width; col++){
			Color c = new Color(numberedImg.getRGB(col, _row));
			if (c.getRed() == 255 && c.getGreen() == 255 && c.getBlue() == 255){
				numberedImg.setRGB(col, _row, originalImg.getRGB(col, _row));
			}
		}
	}
	public static void createNumberedImage(BufferedImage original, BufferedImage number){ //number will be the output image
		originalImg = original;
		numberedImg = number;
		height = original.getHeight();
		width = original.getWidth();
		ImageNumber[] mergers = new ImageNumber[height];
		for (int row=0; row<height; row++){
			mergers[row] = new ImageNumber(row);
			mergers[row].start();
		}
		for (int row=0; row<height; row++){
			try {
				mergers[row].join();
			} catch (InterruptedException e) {}
		}
	}
}
