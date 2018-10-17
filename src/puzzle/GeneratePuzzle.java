package puzzle;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import javax.imageio.ImageIO;

import multi_thread.Extrapolation;


public class GeneratePuzzle {

	String inputFile, outputFile;
	BufferedImage img,nimg;
	int size, h, w, nh, nw,borderNum;
	boolean sh;
	int type;
	Random rnd=new Random();

	public GeneratePuzzle(BufferedImage _img,String _inputFile, String _outputFile, int _size,boolean _sh,int _type) throws ClassNotFoundException {
		inputFile=_inputFile;
		outputFile=_outputFile;
		size=_size; //Dov-Piecce size
		img=_img;
		h=img.getHeight();
		w=img.getWidth();
		nh=h/size;
		nw=w/size;
		sh=_sh;//Dov - shuffle or not, only for display (algorithm doesn't use the order of input anyway)
		type=_type;	//Dov - 0 = no missing pieces
		try {
			createOutput();
		} catch (IOException e) {}
		img=null;
	}

	private void createOutput() throws IOException, ClassNotFoundException {
		nimg=new BufferedImage(w, h,BufferedImage.TYPE_INT_RGB);
		int findBorder[][]=new int[nh][nw];
		int count=0;
		borderNum=0;
		ArrayList<Integer> rnadIdx = new ArrayList<Integer>();
		for(int i=0;i<nh*nw;i++){
			rnadIdx.add(i);
		}
		SaveworthyPuzzleData t = null;
		if(sh){
			Collections.shuffle(rnadIdx);
			t = new SaveworthyPuzzleData(rnadIdx);
			CopyObject.write(t, Global.writeOrderFile);
		}
		else{
			//A feature added to reverse a shuffle that had been done in the past
			if (Global.reverseShuffle){
				t = (SaveworthyPuzzleData)CopyObject.read(Global.readOrderFile);
				rnadIdx = doReverseShuffle(t.partsOrder);
			}
			else{
				//Put the parts in a specific order specified in "readOrderFile"
				if (!Global.readOrderFile.equals(Global.PROJECT_PATH)){
					t = (SaveworthyPuzzleData)CopyObject.read(Global.readOrderFile);
					rnadIdx = t.partsOrder;
				}
			}
		}
		//This questionable code creates different types of missing piece patterns specified by (type)
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
		if (sh){
			ImageIO.write(nimg, "png", outputfile);
		}
		if (Global.fillBurnt){
			BufferedImage fillImage = ImageIO.read(new File(Global.fill));
			Extrapolation.fillBurntPixels(nimg,fillImage);
		}
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
	
	public ArrayList<Integer> doReverseShuffle(ArrayList<Integer>shuffled){
		ArrayList<Integer> result = new ArrayList<Integer>(shuffled.size());
		for (int i=0; i<shuffled.size(); i++){
			for (int j=0; j<shuffled.size();j++){
				if (shuffled.get(j) == i){
					result.add(j);
				}
			}
		}
		return result;
	}
	//A deprecated method to burn "numBurnt" random pixels in a row/column
	public static boolean[] ignoreRandPixels(int partSize){//Dov - Works on a single row/column
		int next = 0;
		int count = partSize;
		Random r = new Random();
		boolean result[] = new boolean[partSize];
		ArrayList<Integer> temp = new ArrayList<Integer>();
		for (int i=0; i<partSize; i++){
			temp.add(i);
			result[i] = false;
		}
		for (int i=0; i<Global.numBurnt; i++){
			next = r.nextInt(count);
			result[temp.remove(next)] = true;
			count--;
		}
		return result;
	}
}
