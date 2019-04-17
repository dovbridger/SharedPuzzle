package oldPuzzle;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import javax.imageio.ImageIO;


public class GeneratePuzzle {

	String inputFile, outputFile;
	BufferedImage img,nimg;
	int size, h, w, nh, nw,borderNum;
	boolean sh;
	int type;
	Random rnd=new Random();


	public GeneratePuzzle(String _inputFile, String _outputFile, int _size,boolean _sh,int _type) throws IOException {
		inputFile=_inputFile;
		outputFile=_outputFile;
		size=_size; //Dov-Piecce size
		img=ImageIO.read(new File(_inputFile));
		h=img.getHeight();
		w=img.getWidth();
		nh=h/size;
		nw=w/size;
		sh=_sh;//Dov - shuffle or not, only for display (algorithm doesn't use the order of input anyway)
		type=_type;	//Dov - 0 = no missing pieces
		createOutput();
		img=null;
	}

	private void createOutput() throws IOException {
		nimg=new BufferedImage(w, h,BufferedImage.TYPE_INT_RGB);
		int findBorder[][]=new int[nh][nw];
		int count=0;
		borderNum=0;
		ArrayList<Integer> rnadIdx = new ArrayList<Integer>();
		for(int i=0;i<nh*nw;i++){
			rnadIdx.add(i);
		}
		if(sh)
			Collections.shuffle(rnadIdx);
		//		System.out.println(type);
		int start = 0;
		if(type==2)
			start=18;
		if(type==3)
			start=2;
		for(int i=0;i<nh*nw;i++){
			findBorder[i/nw][i%nw]=0;
			int ii=Math.abs(i/nw-nh/2);
			int jj=Math.abs(i%nw-nw/2);
			double r=Math.pow(ii, 2)+Math.pow(jj, 2);
//			System.out.println(ii+","+jj);
			if(type==0 || /*(type==1 && i%20!=5)*/(type==1 && rnd.nextFloat()<0.9) ||
					(type==2 && (i/nw%5!=4 || i%nw<start/6 || i%nw>start/6+5)) ||
					(type==3 && (i%nw%5!=4 || i/nw<start || i/nw>start+5)) ||
					(type==4 && 
					(((i/nw>4 && i/nw<nh-5) || (i%nw>4 && i%nw<nw-5)) || ((i/nw==4 || i/nw==nh-5) && (i%nw==4 || i%nw==nw-5))
							|| ((i/nw==3 || i/nw==nh-4) && (i%nw==4 || i%nw==nw-5))|| ((i/nw==4 || i/nw==nh-5) && (i%nw==3 || i%nw==nw-4)))) ||
							(type==5 && r<nh*nh/4) ||
							(type==6 && r>nh*nh/8) ||
							(type==7 && r>nh*nh/15 && r<nh*nh/4) ||
							(type==8 && (i/nw!=nh/2 && (i/nw%3>0 || i%nw!=nw/2)|| i%nw!=nw/2 && (i%nw%3>0 || i/nw!=nh/2)))
					){
				nimg.setRGB(rnadIdx.get(i)%nw*size, rnadIdx.get(i)/nw*size, size, size, img.getRGB(i%nw*size, i/nw*size, size, size,  null, 0, size), 0, size);
				findBorder[i/nw][i%nw]=1;
				count++;
			}
			else {
				if(type==2)
					start++;
			}
			if(type==3 && i%nw%5==4)
				start++;
			if(type==3 && i%nw==nw-1)
				start=2;
		}
		for(int i=0;i<nh-1;i++)
			for(int j=0;j<nw;j++){
				if(findBorder[i][j]==1 && findBorder[i+1][j]==1)
					borderNum++;
			}
		for(int i=0;i<nh;i++)
			for(int j=0;j<nw-1;j++){
				if(findBorder[i][j]==1 && findBorder[i][j+1]==1)
					borderNum++;
			}
		System.out.println("Parts Num: "+count);
		File outputfile = new File(outputFile);
		ImageIO.write(nimg, "png", outputfile);
	}

	public BufferedImage getOrignalIm(){
		return img;
	}

	public BufferedImage getGenIm(){
		return nimg;
	}

	public int getBorderNum(){
		return borderNum;
	}
}
