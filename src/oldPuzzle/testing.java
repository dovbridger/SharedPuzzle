package oldPuzzle;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
public class testing {

	public static void colorFrame(BufferedImage img, Color c, int offest){	
		for (int i=offest+1; i<img.getHeight()-offest; i++ ){
			img.setRGB(offest+1, i, c.getRGB());
			img.setRGB(img.getWidth()-offest,i,c.getRGB());
		}
		for (int j=offest+1; j<img.getWidth()-offest; j++ ){
			img.setRGB(j, offest+1, c.getRGB());
			img.setRGB(j,img.getHeight()-offest,c.getRGB());
		}
	}
	public static void main(String args[]) throws IOException{
	
	/*	
		Random r = new Random();
		BufferedImage img = ImageIO.read(new File("testImage.png"));
		System.out.println("H="+img.getHeight()+" W="+img.getWidth());
		for (int k=3; k<250; k=k+20){
			Color c = new Color(r.nextInt(k/3), r.nextInt(k/2),r.nextInt(k));
			colorFrame(img,c,k);colorFrame(img,c,k+1);colorFrame(img,c,k+2);colorFrame(img,c,k+3);
		}
		File out = new File("testImage1.png");
		ImageIO.write(img, "png", out);
*/		
	}
}
