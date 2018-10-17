package oldPuzzle;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeMap;

import javax.imageio.ImageIO;


public class PuzzleSolver{
	PuzzleParts puzzleParts;
	float diffMatrix[][][];
	//	double diffMatrix2[][][];
	float confMatrix[][][];
	int bestBuddies[][];
	int bestBuddies2[][];
	int partSize, partsNum,missPartsNum,burnPartsNum,w,h,startPart,partsLeft,startPart21,startPart22,borderNum;
	BufferedImage img;
	//	ArrayList<HashMap<Integer,Double>> bestBuddiesTree;
	//	ArrayList<HashSet<Integer>> bestBuddiesTreeIn;
	HashMap<Integer, Integer[]> chosenParts;
	ArrayList<TreeMap<Double, Integer[]>> nextToPlace;
	int budddiesNumber[];
	BufferedImage nimg;
	Integer finalPlacement[][];
	String name;
	Date d1,d2,d3;
	Random rnd=new Random();


	boolean useSize,useSize2;
	int minX,maxX,minY,maxY;
	double scale;
	boolean missPart;



	public PuzzleSolver(BufferedImage _img,int _partSize, String _name, boolean _useSize ,boolean _missPart, int _borderNum){
		name=_name;


		useSize=_useSize;
		missPart=_missPart;
		borderNum=_borderNum;

		d1=new Date();
		puzzleParts=new PuzzleParts(_img, _partSize);
		partSize=_partSize;
		partsNum=puzzleParts.getPartsNum();
		missPartsNum=puzzleParts.getMissPartsNum();
		burnPartsNum=puzzleParts.getBurnPartsNum();
		System.out.println("M: "+missPartsNum);
		h=puzzleParts.hetH();
		w=puzzleParts.hetW();
		diffMatrix=new float[4][partsNum][partsNum];
		//		diffMatrix2=new double[partsNum][partsNum][4];
		confMatrix=new float[4][partsNum][partsNum];
		bestBuddies=new int[partsNum][4];
		bestBuddies2=new int[partsNum][4];
		//		bestBuddies2=new int[partsNum][4];
		//		bestBuddiesTree=new ArrayList<HashMap<Integer,Double>>();
		//		bestBuddiesTreeIn=new ArrayList<HashSet<Integer>>();
		partsLeft=partsNum;
		budddiesNumber=new int[partsNum];
		chosenParts=new HashMap<Integer, Integer[]>();
		nextToPlace=new ArrayList<TreeMap<Double, Integer[]>>();
		for(int i=0;i<17;i++){
			//			bestBuddiesTree.add(new HashMap<Integer,Double>());
			//			bestBuddiesTreeIn.add(new HashSet<Integer>());
			nextToPlace.add(new TreeMap<Double, Integer[]>());
		}
		finalPlacement=new Integer[partsNum/2][partsNum/2];
		minX=partsNum/2;maxX=0;minY=partsNum/2;maxY=0;
		scale=672.0/partSize/puzzleParts.nw;
		run();
	}

	public void run() {
		calcDiffMatrix();
		Date d3=new Date();
		//		copyDiffMatrix();
		calcConfMatrix();
//		predictSuccess(); Dov-This method didn't do anything so I commented it out
		findBestStart(true);
		//		printBB();
		solvePuzzle();
		Date d2=new Date();
//		name=((d2.getTime()-d1.getTime())/1000.0)+name;
		createOutputImage(name,true);	
		System.out.println("Time: "+(d2.getTime()-d1.getTime())/1000.0);
		System.out.println("Init Time: "+(d3.getTime()-d1.getTime())/1000.0);
		calcDirectComp2();
		calcNeighborsComp2();
	}

//	private void predictSuccess() {
//		double count=0;
//		for(Orientation or:Orientation.values()){
//			int orN=(or.ordinal()==0)?(0):(1);
//			int op=(orN%2==0)?(orN+1):(orN-1);
//			for(int i=0;i<partsNum;i++){
//
//			}
//		}
//
//	}

	private void printBB() {
		// TODO Auto-generated method stub
		File f=new File("BB.txt");
		try {
			BufferedWriter bf=new BufferedWriter(new FileWriter(f));
			for(int k=0;k<4;k++){
				bf.write(k+":");
				bf.newLine();
				for(int i=0;i<partsNum;i++){
					bf.write(i+":"+bestBuddies[i][k]);
					bf.newLine();
				}
				bf.newLine();
			}
			bf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//	private void copyDiffMatrix() {
	//		for(Orientation or:Orientation.values()){
	//			for(int i=0;i<partsNum;i++){
	//				for(int j=0;j<partsNum;j++){
	//					diffMatrix2[i][j][or.ordinal()]=diffMatrix[0][i][j][or.ordinal()];
	//				}
	//			}
	//		}
	//
	//	}

//	private void calcDirectComp() { Dov-Commented out because it wasn't used
//		double sum=0;
//		for(int j=minX;j<maxX+1;j++){
//			for(int i=minY;i<maxY+1;i++){
//				if((finalPlacement[i][j]!=null && finalPlacement[i][j]-1==j-minX+(i-minY)*puzzleParts.nw) ||
//						(finalPlacement[i][j]!=null && puzzleParts.isBurn(finalPlacement[i][j]-1) && puzzleParts.isBurn(j-minX+(i-minY)*puzzleParts.nw))||
//						(finalPlacement[i][j]!=null && puzzleParts.isDark(finalPlacement[i][j]-1) && puzzleParts.isDark(j-minX+(i-minY)*puzzleParts.nw)))
//					sum++;
//			}
//		}
//		System.out.println("Direct Comp: "+(sum/partsNum));
//	}

	private void calcDirectComp2() {
		double sum=0;
		Integer[] off = chosenParts.get(startPart);
		int xOff=partsNum/4-minX-(startPart-1)%puzzleParts.nw-1;
		int yOff=partsNum/4-minY-(startPart-1)/puzzleParts.nw;
		System.out.println("Off: "+xOff+" "+yOff);
		for(int j=minX;j<maxX+1;j++){
			for(int i=minY;i<maxY+1;i++){
				if((finalPlacement[i][j]!=null && finalPlacement[i][j]-1==j-minX-xOff+(i-minY-yOff)*puzzleParts.nw))//s ||
//						(finalPlacement[i][j]!=null && puzzleParts.isBurn(finalPlacement[i][j]-1) && puzzleParts.isBurn(j-minX-xOff+(i-minY-yOff)*puzzleParts.nw))||
//						(finalPlacement[i][j]!=null && puzzleParts.isDark(finalPlacement[i][j]-1) && puzzleParts.isDark(j-minX-xOff+(i-minY-yOff)*puzzleParts.nw)))
					sum++;
//				else
//					System.out.println("i j: "+(finalPlacement[i][j]-1)+" "+(j-minX-xOff+(i-minY-yOff)*puzzleParts.nw));
			}
		}
		System.out.println("Direct Comp: "+(sum/(partsNum-missPartsNum)));
	}
/*
	private void calcNeighborsComp() { Dov- commented out because it wasn't used
		double sum=0;
		int nh=puzzleParts.nh, nw=puzzleParts.nw;
		for(int i=minY;i<maxY+1;i++){
			for(int j=minX;j<maxX+1;j++){
				if((finalPlacement[i][j]!=null && finalPlacement[i][j+1]!=null && (finalPlacement[i][j]-1)%nw<nw-1 && finalPlacement[i][j]==finalPlacement[i][j+1]-1)||
						((finalPlacement[i+1][j]!=null && finalPlacement[i][j]!=null && puzzleParts.isBurn(finalPlacement[i][j]-1)) || (finalPlacement[i][j]!=null && finalPlacement[i+1][j]!=null && puzzleParts.isBurn(finalPlacement[i+1][j]-1)))||
						(finalPlacement[i+1][j]!=null && finalPlacement[i][j]!=null && puzzleParts.isDark(finalPlacement[i][j]-1)) || (finalPlacement[i][j]!=null && finalPlacement[i+1][j]!=null && puzzleParts.isDark(finalPlacement[i+1][j]-1)))
					sum++;
				//				else
				//					System.out.println((finalPlacement[i][j]-1)+"*1"+finalPlacement[i][j+1]);
				if((finalPlacement[i][j]!=null && finalPlacement[i+1][j]!=null && (finalPlacement[i][j]-1)/nw<nh-1 && finalPlacement[i][j]-1+nw==finalPlacement[i+1][j]-1) ||
						(finalPlacement[i][j+1]!=null && finalPlacement[i][j]!=null && puzzleParts.isBurn(finalPlacement[i][j]-1) || (finalPlacement[i][j]!=null && finalPlacement[i][j+1]!=null && puzzleParts.isBurn(finalPlacement[i][j+1]-1)))||
						(finalPlacement[i][j+1]!=null && finalPlacement[i][j]!=null && puzzleParts.isDark(finalPlacement[i][j]-1))|| (finalPlacement[i][j]!=null && finalPlacement[i][j+1]!=null && puzzleParts.isDark(finalPlacement[i][j+1]-1)))
					sum++;
				//				else
				//					System.out.println((finalPlacement[i][j]-1)+"*2"+finalPlacement[i+1][j]);
			}
		}
		System.out.println("Neighbors Comp: "+(sum/((nh-1)*(nw)+(nw-1)*(nh))));
	}*/

	private void calcNeighborsComp2() {
		double sum=0;
		int nh=puzzleParts.nh, nw=puzzleParts.nw;
		for(int i=minY;i<maxY+1;i++){
			for(int j=minX;j<maxX+1;j++){
				if((finalPlacement[i][j]!=null && finalPlacement[i][j+1]!=null && (finalPlacement[i][j]-1)%nw<nw-1 && finalPlacement[i][j]==finalPlacement[i][j+1]-1))
//						||((finalPlacement[i+1][j]!=null && finalPlacement[i][j]!=null && puzzleParts.isBurn(finalPlacement[i][j]-1)) || (finalPlacement[i][j]!=null && finalPlacement[i+1][j]!=null && puzzleParts.isBurn(finalPlacement[i+1][j]-1))))
					//					||
					//						(finalPlacement[i][j]!=null && puzzleParts.isDark(finalPlacement[i][j]-1)) || (finalPlacement[i+1][j]!=null && puzzleParts.isDark(finalPlacement[i+1][j]-1)))
					sum++;
				//				else
				//					System.out.println((finalPlacement[i][j]-1)+"*1"+finalPlacement[i][j+1]);
				if((finalPlacement[i][j]!=null && finalPlacement[i+1][j]!=null && (finalPlacement[i][j]-1)/nw<nh-1 && finalPlacement[i][j]-1+nw==finalPlacement[i+1][j]-1))
//						||(finalPlacement[i][j+1]!=null && finalPlacement[i][j]!=null && puzzleParts.isBurn(finalPlacement[i][j]-1) || (finalPlacement[i][j]!=null && finalPlacement[i][j+1]!=null && puzzleParts.isBurn(finalPlacement[i][j+1]-1))))
					//					||	(finalPlacement[i][j]!=null && puzzleParts.isDark(finalPlacement[i][j]-1)))
					sum++;
				//				else
				//					System.out.println((finalPlacement[i][j]-1)+"*2"+finalPlacement[i+1][j]);
			}
		}
		//		System.out.println("Neighbors Comp: "+(sum/(-missPartsNum*4+(nh-1)*(nw)+(nw-1)*(nh))));
		System.out.println("Neighbors Comp: "+(sum/borderNum));
	}

	static public Image getScaledImage(Image srcImg, int w, int h){
		BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = resizedImg.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(srcImg, 0, 0, w, h, null);
		g2.dispose();
		return resizedImg;
	}

	private void createOutputImage(String s, boolean save) {
		int borders[]=findBorder();
		nimg=new BufferedImage(partSize*(borders[3]-borders[2]+1), partSize*(borders[1]-borders[0]+1),BufferedImage.TYPE_INT_RGB);
		int offsety=partsNum/4-borders[0];
		int offsetx=partsNum/4-borders[2];
		Iterator<Integer> partsIt = chosenParts.keySet().iterator();
		while(partsIt.hasNext()){
			int part=partsIt.next();
			Integer[] place=chosenParts.get(part);
			nimg.setRGB(place[0]*partSize+partSize*offsetx, place[1]*partSize+partSize*offsety, partSize, partSize, puzzleParts.getRGBPart(part).getRGB(0, 0, partSize, partSize, null, 0, partSize), 0, partSize);
		}
		if(save){
			File outputfile = new File(s+".png");
			try {
				ImageIO.write(nimg, "png", outputfile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private int[] findBorder() {//Dov-Finds the start and end indexes of the x and y coordinates in the finalPlacement array
		int borders[]={partsNum/2, 0, partsNum/2, 0};
		//		int borders[]={minY,maxY,minX,maxX};
		for(int i=0;i<partsNum/2;i++){
			for(int j=0;j<partsNum/2;j++){
				if(finalPlacement[i][j]!=null){
					borders[1]=Math.max(borders[1], i);
					borders[0]=Math.min(borders[0], i);
					borders[3]=Math.max(borders[3], j);
					borders[2]=Math.min(borders[2], j);
				}
			}
		}
		return borders;
	}

	private void solvePuzzle() {
		int denum=10;
		if(burnPartsNum>10)
			denum=64;
		Integer base[]={0,0};
		chosenParts.put(startPart,base);
		finalPlacement[partsNum/4][partsNum/4]=startPart+1;
		maxX=Math.max(maxX, partsNum/4);
		minX=Math.min(minX, partsNum/4);
		maxY=Math.max(maxY, partsNum/4);
		minY=Math.min(minY, partsNum/4);
		partsLeft--;
		//		createOutputImage(Integer.toString(partsNum-partsLeft),true);
		addBuddies3(startPart);
		boolean forceBestBuddies=true;
		useSize2=useSize;
		double offset=0.0;
		int itNum=0,itF=0,itF2=0,itS=0;
		int ppartsLeft = partsLeft;
		int times=1;
//		missPartsNum=0;
		while(partsLeft-missPartsNum-burnPartsNum>0){
			int i;
			for(i=12;i>11;i--){
				//				System.out.println(s"S:"+nextToPlace.get(i).size());
				Iterator<Integer[]> best=nextToPlace.get(i).descendingMap().values().iterator();
												Iterator<Double> bestK=nextToPlace.get(i).descendingMap().keySet().iterator();
				if(!best.hasNext()) continue;
				Integer base2[]=new Integer[2];
				Integer[] nextI=best.next();
												Double key=bestK.next();
//								Double key=bestK.next();
				double flagc=0,flagc2=0;
				boolean flag=false;
				Double th = 0.0,th2 = 0.0;
				if(finalPlacement[nextI[2]-1+partsNum/4][nextI[1]+partsNum/4] !=null && (bestBuddies[finalPlacement[nextI[2]-1+partsNum/4][nextI[1]+partsNum/4]-1][1]==nextI[0] && bestBuddies[nextI[0]][0]==finalPlacement[nextI[2]-1+partsNum/4][nextI[1]+partsNum/4]-1)) {
					flagc++;
					//					th+=confMatrix[finalPlacement[nextI[2]-1+partsNum/4][nextI[1]+partsNum/4]-1][nextI[0]][1]/2+confMatrix[nextI[0]][finalPlacement[nextI[2]-1+partsNum/4][nextI[1]+partsNum/4]-1][0]/2;
				}
				//				if(finalPlacement[nextI[2]-1+partsNum/4][nextI[1]+partsNum/4] !=null){
				//					th2+=confMatrix[finalPlacement[nextI[2]-1+partsNum/4][nextI[1]+partsNum/4]-1][nextI[0]][1]/2+confMatrix[nextI[0]][finalPlacement[nextI[2]-1+partsNum/4][nextI[1]+partsNum/4]-1][0]/2;
				//					flagc2++;
				//				}
				if(finalPlacement[nextI[2]+1+partsNum/4][nextI[1]+partsNum/4] !=null && (bestBuddies[finalPlacement[nextI[2]+1+partsNum/4][nextI[1]+partsNum/4]-1][0]==nextI[0] && bestBuddies[nextI[0]][1]==finalPlacement[nextI[2]+1+partsNum/4][nextI[1]+partsNum/4]-1)) {
					flagc++;
					//					th+=confMatrix[finalPlacement[nextI[2]+1+partsNum/4][nextI[1]+partsNum/4]-1][nextI[0]][0]/2+confMatrix[nextI[0]][finalPlacement[nextI[2]+1+partsNum/4][nextI[1]+partsNum/4]-1][1]/2;
				}
				//				if(finalPlacement[nextI[2]+1+partsNum/4][nextI[1]+partsNum/4] !=null){
				//					th2+=confMatrix[finalPlacement[nextI[2]+1+partsNum/4][nextI[1]+partsNum/4]-1][nextI[0]][0]/2+confMatrix[nextI[0]][finalPlacement[nextI[2]+1+partsNum/4][nextI[1]+partsNum/4]-1][1]/2;
				//					flagc2++;
				//				}
				if(finalPlacement[nextI[2]+partsNum/4][nextI[1]-1+partsNum/4] !=null && (bestBuddies[finalPlacement[nextI[2]+partsNum/4][nextI[1]-1+partsNum/4]-1][3]==nextI[0] && bestBuddies[nextI[0]][2]==finalPlacement[nextI[2]+partsNum/4][nextI[1]-1+partsNum/4]-1)) {
					flagc++;
					//					th+=confMatrix[finalPlacement[nextI[2]+partsNum/4][nextI[1]-1+partsNum/4]-1][nextI[0]][3]/2+confMatrix[nextI[0]][finalPlacement[nextI[2]+partsNum/4][nextI[1]-1+partsNum/4]-1][2]/2;
				}
				//				if(finalPlacement[nextI[2]+partsNum/4][nextI[1]-1+partsNum/4] !=null){
				//					th2+=confMatrix[finalPlacement[nextI[2]+partsNum/4][nextI[1]-1+partsNum/4]-1][nextI[0]][3]/2+confMatrix[nextI[0]][finalPlacement[nextI[2]+partsNum/4][nextI[1]-1+partsNum/4]-1][2]/2;
				//					flagc2++;
				//				}
				if(finalPlacement[nextI[2]+partsNum/4][nextI[1]+1+partsNum/4] !=null && (bestBuddies[finalPlacement[nextI[2]+partsNum/4][nextI[1]+1+partsNum/4]-1][2]==nextI[0] && bestBuddies[nextI[0]][3]==finalPlacement[nextI[2]+partsNum/4][nextI[1]+1+partsNum/4]-1)) {
					flagc++;
					//					th+=confMatrix[finalPlacement[nextI[2]+partsNum/4][nextI[1]+1+partsNum/4]-1][nextI[0]][2]/2+confMatrix[nextI[0]][finalPlacement[nextI[2]+partsNum/4][nextI[1]+1+partsNum/4]-1][3]/2;
				}
				//				if(finalPlacement[nextI[2]+partsNum/4][nextI[1]+1+partsNum/4] !=null){
				//					th2+=confMatrix[finalPlacement[nextI[2]+partsNum/4][nextI[1]+1+partsNum/4]-1][nextI[0]][2]/2+confMatrix[nextI[0]][finalPlacement[nextI[2]+partsNum/4][nextI[1]+1+partsNum/4]-1][3]/2;
				//					flagc2++;
				//				}
				//								if(forceBestBuddies && finalPlacement[nextI[2]-1+partsNum/4][nextI[1]+partsNum/4] !=null && (bestBuddies[finalPlacement[nextI[2]-1+partsNum/4][nextI[1]+partsNum/4]-1][1]!=nextI[0] || bestBuddies[nextI[0]][0]!=finalPlacement[nextI[2]-1+partsNum/4][nextI[1]+partsNum/4]-1)) flag=true;
				//								if(forceBestBuddies && finalPlacement[nextI[2]+1+partsNum/4][nextI[1]+partsNum/4] !=null && (bestBuddies[finalPlacement[nextI[2]+1+partsNum/4][nextI[1]+partsNum/4]-1][0]!=nextI[0] || bestBuddies[nextI[0]][1]!=finalPlacement[nextI[2]+1+partsNum/4][nextI[1]+partsNum/4]-1)) flag=true;
				//								if(forceBestBuddies && finalPlacement[nextI[2]+partsNum/4][nextI[1]-1+partsNum/4] !=null && (bestBuddies[finalPlacement[nextI[2]+partsNum/4][nextI[1]-1+partsNum/4]-1][3]!=nextI[0] || bestBuddies[nextI[0]][2]!=finalPlacement[nextI[2]+partsNum/4][nextI[1]-1+partsNum/4]-1)) flag=true;
				//								if(forceBestBuddies && finalPlacement[nextI[2]+partsNum/4][nextI[1]+1+partsNum/4] !=null && (bestBuddies[finalPlacement[nextI[2]+partsNum/4][nextI[1]+1+partsNum/4]-1][2]!=nextI[0] || bestBuddies[nextI[0]][3]!=finalPlacement[nextI[2]+partsNum/4][nextI[1]+1+partsNum/4]-1)) flag=true;
				//				double offset2=0.0;
				//								System.out.println(th);
				if(flagc==0) flag=true;
//								if(key<0.5 && rnd.nextFloat()<0.2) flag=true;
				//				if(flagc==1 && th/flagc<0.4) flag=true;//0.371
				//				if(flagc==1 && th/flagc<0.11) flag=true;
				//				if(flagc2>2 && flagc<2) flag=true;
				//				if(flagc>1) flag=false;
				//				if(!forceBestBuddies && th2>0.366)  flag=false;
				if(!forceBestBuddies)  flag=false;
				if(chosenParts.containsKey(nextI[0])) 
					flag=true;
				base2[0]=nextI[1];
				base2[1]=nextI[2];
				if((useSize2 && (/*(puzzleParts.isBurn(nextI[0]) && puzzleParts.getBurnPartsNum()<partsLeft-10 )||*/
						/*(puzzleParts.isDark(nextI[0]) && puzzleParts.getDarkPartsNum()<partsLeft-5)||*/
						base2[0]+partsNum/4-minX+1>puzzleParts.nw || maxX-base2[0]-partsNum/4+1>puzzleParts.nw ||
						base2[1]+partsNum/4-minY+1>puzzleParts.nh || maxY-base2[1]-partsNum/4+1>puzzleParts.nh)))
					flag=true;
//				if(key<0.7 && forceBestBuddies) flag=true;
				if((finalPlacement[base2[1]+partsNum/4][base2[0]+partsNum/4])!=null /*|| (burnPartsNum<partsLeft && puzzleParts.isBurn(nextI[0]))*/ || flag ){
					nextToPlace.get(i).pollLastEntry();
					//					System.out.println("*part*: "+nextI[0]+"|| state: "+forceBestBuddies);
					break;
				}
				itF--;
				//				System.out.println("part: "+nextI[0]+"|| state: "+forceBestBuddies+" parts num: "+nextToPlace.get(12).size()+" | "+flagc+"/"+flagc2);
				//				if(partsLeft==partsNum/3 || partsLeft==2*partsNum/3){
				//				if(flagc==0){
				//					base2[0]=base2[0]-40*times;
				//					base2[1]=base2[1]-40*times;
				//					times++;
				//				}
				chosenParts.put(nextI[0],base2);
				finalPlacement[base2[1]+partsNum/4][base2[0]+partsNum/4]=nextI[0]+1;
				maxX=Math.max(maxX, base2[0]+partsNum/4);
				minX=Math.min(minX, base2[0]+partsNum/4);
				maxY=Math.max(maxY, base2[1]+partsNum/4);
				minY=Math.min(minY, base2[1]+partsNum/4);
				partsLeft--;
				//				createOutputImage(Integer.toString(partsNum-partsLeft),true);
				nextToPlace.get(i).pollLastEntry();
				addBuddies3(nextI[0]);
				if(!forceBestBuddies && partsNum/2>partsLeft)
					addBuddies2(nextI[0]);
//					System.out.println("***");
//				}
				
					
				if((!forceBestBuddies && (itF<1 || partsLeft>partsNum/2-1)) || missPart/*&& ((itF<30 && !useSize))*/){
					forceBestBuddies=true;
					//					clean();
				}
				break;
			}
			if(i==11) {
//				System.out.println("Check: "+partsLeft);
				eliminateComp();
				//				itF=1;
				if(ppartsLeft-ppartsLeft/5-1<partsLeft){
					//					itS++;
//					if((forceBestBuddies)){
						//						addCandidates(true);
//					}
//					else{

						addCandidates(false);

						//												break;
//					}
					forceBestBuddies=false;
					itF=Math.max(nextToPlace.get(12).size()*(partsNum-partsLeft)/partsNum/denum,1);
					if(partsNum/2>partsLeft)
						itF=partsLeft/2;
					if(missPart)
						itF=1;
					itS+=itF;
				}
				else
					addCandidates(true);
				//				if(itS>3 && partsLeft<partsNum/2 && !missPart)
				//					forceBestBuddies=false;
				itNum++;
				ppartsLeft=partsLeft;
//				System.out.println(forceBestBuddies+" s:"+nextToPlace.get(12).size());
				//				itF=1;//Math.max(nextToPlace.get(12).size()*(partsNum-partsLeft)/partsNum/denum,1);
				//				if(partsNum>5000 && partsLeft<partsNum/2)
				//					Math.max(nextToPlace.get(12).size()/denum,1);
				//				System.out.println("Can: " + itF);
				if(itNum>10000) break;
				//				break;
			}
			if(partsLeft-missPartsNum-burnPartsNum<1 && useSize && !useSize2){
				removeWraped();
				eliminateComp();
				addCandidates(true);
				forceBestBuddies=true;
				useSize2=true;
			}
		}
		//		cleanBad();
		System.out.println("PrL: "+ (double)(itS-puzzleParts.burnPartsNum	-puzzleParts.darkPartsNum)/partsNum);
		System.out.println("itNum: "+itNum+"# itS: "+itS);
		System.out.println("RNum: "+partsLeft);
	}

	private void removeWraped() {
		int w=puzzleParts.nw,h=puzzleParts.nh;
		int yOff=maxY-minY+1-h+1;
		yOff=Math.max(1, yOff);
		int xOff=maxX-minX+1-w+1;
		xOff=Math.max(1, xOff);
		System.out.println("Tmp: "+ xOff +":"+ yOff);
		TreeMap<Integer, Integer[]> op=new TreeMap<Integer, Integer[]>();
		for(int i=minY;i<minY+yOff;i++){
			for(int j=minX;j<minX+xOff;j++){
				Integer tmp[]={i,j};
				op.put(countParts(i,j,h,w), tmp);
			}
		}
		Integer res[]=op.descendingMap().values().iterator().next();
		removeParts(res[1],res[0],h,w);
		minX=res[1];
		maxX=minX+w-1;
		minY=res[0];
		maxY=minY+h-1;
		int border[]=findBorder();
		minY=border[0];maxY=border[1];
		minX=border[2];maxX=border[3];
	}

	private void removeParts(Integer x, Integer y, int h, int w) {
		for(int i=minY;i<maxY+1;i++){
			for(int j=minX;j<maxX+1;j++){
				if(finalPlacement[i][j]!=null &&(i<y || i>y+h-1 || j<x || j>x+w-1 || puzzleParts.isBurn(finalPlacement[i][j]-1))){
					chosenParts.remove(finalPlacement[i][j]-1);
					finalPlacement[i][j]=null;
					partsLeft++;
				}
			}
		}

	}

	private Integer countParts(int i, int j, int h, int w) {
		Integer count=0;
		for(int ii=0;ii<h;ii++){
			for(int jj=0;jj<w;jj++){
				if(finalPlacement[ii+i][jj+j]!=null)
					count++;
			}
		}
		//		System.out.println("C: "+count);
		return count;
	}

	private void cleanBad() {
		//		double avgD=avgDiff();
		//		for(int i=minY;i<maxY;i++){
		//			for(int j=minX;j<maxX;j++){
		//				boolean flag=false;
		//				if(finalPlacement[i][j]!=null){
		//					if(diffMatrix2[finalPlacement[i][j]-1][finalPlacement[i][j+1]-1][Orientation.RIGHT.ordinal()]<avgD*0.9)
		//						flag=true;
		//					if(diffMatrix2[finalPlacement[i][j]-1][finalPlacement[i+1][j]-1][Orientation.DOWN.ordinal()]<avgD*0.9)
		//						flag=true;
		//					if(flag){
		//						chosenParts.remove(finalPlacement[i][j]-1);
		//						finalPlacement[i][j]=null;
		//						partsLeft--;
		//					}
		//				}
		//			}
		//		}


	}

	//	private double avgDiff() {
	//		double avgD=0;
	//		int counter=0;
	//		for(int i=minY;i<maxY;i++){
	//			for(int j=minX;j<maxX;j++){
	//				if(finalPlacement[i][j]!=null){
	//					avgD+=diffMatrix2[finalPlacement[i][j]-1][finalPlacement[i][j+1]-1][Orientation.RIGHT.ordinal()];
	//					avgD+=diffMatrix2[finalPlacement[i][j]-1][finalPlacement[i+1][j]-1][Orientation.DOWN.ordinal()];
	//					avgD+=diffMatrix2[finalPlacement[i][j+1]-1][finalPlacement[i][j]-1][Orientation.LEFT.ordinal()];
	//					avgD+=diffMatrix2[finalPlacement[i+1][j]-1][finalPlacement[i][j]-1][Orientation.UP.ordinal()];
	//					counter+=2;
	//				}
	//			}
	//		}
	//		avgD/=counter;
	//		System.out.println("avgD: "+avgD/2);
	//		return avgD;
	//	}

	private void addCandidates(boolean flag) {
		if(maxX-minX>5){
			Thread[] threads = new Thread[maxY-minY+1];
			for(int i=0;i<maxY-minY+1;i++){
				threads[i]=new Thread(new AddCandidates(minX, maxX, minY, maxY, i+minY, partsNum, partsLeft, finalPlacement, chosenParts, nextToPlace, confMatrix, bestBuddies, flag, useSize2, budddiesNumber, puzzleParts,missPart,bestBuddies2));
				threads[i].start();

			}
			for(int i=0;i<maxY-minY+1;i++){
				try {
					threads[i].join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else{
			for(int i=minY;i<maxY+1;i++){

				for(int j=minX;j<maxX+1;j++){
					if(finalPlacement[i][j]!=null){
						if(finalPlacement[i-1][j]==null || finalPlacement[i+1][j]==null || finalPlacement[i][j-1]==null || finalPlacement[i][j+1]==null){
							if(flag)
								addBuddies(finalPlacement[i][j]-1);
							else
								addBuddies2(finalPlacement[i][j]-1);
						}
					}
				}
			}
		}
	}

	private void eliminateComp() {
		for(int i=0;i<partsNum;i++){
			for(int j=0;j<partsNum;j++){
				if(chosenParts.containsKey(i) && chosenParts.containsKey(j)){
					diffMatrix[0][i][j]=Float.MAX_VALUE;
					diffMatrix[1][i][j]=Float.MAX_VALUE;
					diffMatrix[2][i][j]=Float.MAX_VALUE;
					diffMatrix[3][i][j]=Float.MAX_VALUE;
					//					diffMatrix[1][i][j][0]=Double.MAX_VALUE;
					//					diffMatrix[1][i][j][1]=Double.MAX_VALUE;
					//					diffMatrix[1][i][j][2]=Double.MAX_VALUE;
					//					diffMatrix[1][i][j][3]=Double.MAX_VALUE;
				}
				else if(chosenParts.containsKey(j)){
					int nextA[]=new int[2];
					nextA[0]=chosenParts.get(j)[0];
					nextA[1]=chosenParts.get(j)[1];
					if(finalPlacement[nextA[1]-1+partsNum/4][nextA[0]+partsNum/4] != null)
						diffMatrix[1][i][j]=Float.MAX_VALUE;
					if(finalPlacement[nextA[1]+1+partsNum/4][nextA[0]+partsNum/4] != null)
						diffMatrix[0][i][j]=Float.MAX_VALUE;
					if(finalPlacement[nextA[1]+partsNum/4][nextA[0]+partsNum/4-1] != null)
						diffMatrix[3][i][j]=Float.MAX_VALUE;
					if(finalPlacement[nextA[1]+partsNum/4][nextA[0]+partsNum/4+1] != null)
						diffMatrix[2][i][j]=Float.MAX_VALUE;
					//					if(finalPlacement[nextA[1]-1+partsNum/4][nextA[0]+partsNum/4] != null &&
					//							finalPlacement[nextA[1]+1+partsNum/4][nextA[0]+partsNum/4] != null &&
					//							finalPlacement[nextA[1]+partsNum/4][nextA[0]+partsNum/4-1] != null &&
					//							finalPlacement[nextA[1]+partsNum/4][nextA[0]+partsNum/4+1] != null){				
					//						diffMatrix[0][i][j][1]=Double.MAX_VALUE;
					//						diffMatrix[0][i][j][0]=Double.MAX_VALUE;
					//						diffMatrix[0][i][j][3]=Double.MAX_VALUE;
					//						diffMatrix[0][i][j][2]=Double.MAX_VALUE;
					//					}

					//						diffMatrix[1][i][j][0]=Double.MAX_VALUE;
					//						diffMatrix[1][i][j][1]=Double.MAX_VALUE;
					//						diffMatrix[1][i][j][2]=Double.MAX_VALUE;
					//						diffMatrix[1][i][j][3]=Double.MAX_VALUE;

				}
				//				else{
				//					diffMatrix[0][i][j][0]=diffMatrix[0][i][j][0];
				//					diffMatrix[0][i][j][1]=diffMatrix[0][i][j][1];
				//					diffMatrix[0][i][j][2]=diffMatrix[0][i][j][2];
				//					diffMatrix[0][i][j][3]=diffMatrix[0][i][j][3];
				//				}
			}
		}
		calcConfMatrix();
		//clean();
		//findBestStart(false);
	}

	private void clean() {
		//		bestBuddiesTree=new ArrayList<HashMap<Integer,Double>>();
		nextToPlace=new ArrayList<TreeMap<Double, Integer[]>>();
		for(int i=0;i<17;i++){
			//			bestBuddiesTree.add(new HashMap<Integer,Double>());
			nextToPlace.add(new TreeMap<Double, Integer[]>());
		}
	}

	private void addBuddies(int nextI) {
		ArrayList<Orientation> ao=new ArrayList<Orientation>();
		ao.add(Orientation.DOWN);ao.add(Orientation.UP);ao.add(Orientation.LEFT);ao.add(Orientation.RIGHT);
		Collections.shuffle(ao);
		for(Orientation or:Orientation.values()){
			int next=bestBuddies[nextI][or.ordinal()];
			//			if(puzzleParts.isBurn(next) && partsLeft>puzzleParts.burnPartsNum+2)
			//				continue;
			if(next==-1)
				continue;
			int op=(or.ordinal()%2==0)?(or.ordinal()+1):(or.ordinal()-1);
			//			int count=-1;
			//			Double val=confMatrix[next][nextI][op];
			//			if(val>confMatrix[next][bestBuddies[next][0]][0])
			//				count++;
			//			if(val>confMatrix[next][bestBuddies[next][1]][1])
			//				count++;
			//			if(val>confMatrix[next][bestBuddies[next][2]][2])
			//				count++;
			//			if(val>confMatrix[next][bestBuddies[next][3]][3])
			//				count++;
			//			if(count>1)
			//				continue;
			Integer nextA[]=new Integer[3];
			nextA[0]=next;
			//			Double val=confMatrix[nextI][next][or.ordinal()]/2+confMatrix[next][nextI][op]/2;
			int dir[]=new int[3];
			if(nextI==bestBuddies[next][op]  && !chosenParts.containsKey(next)){ //&&
				//!nextToPlace.get(budddiesNumber[next]-1).containsKey(bestBuddiesTree.get(budddiesNumber[next]-1).get(next))){
				switch(or){
				case UP:
					nextA[1]=chosenParts.get(nextI)[0];
					nextA[2]=chosenParts.get(nextI)[1]-1;
					dir[0]=3;
					dir[1]=1;
					dir[2]=2;
					break;
				case DOWN:
					nextA[1]=chosenParts.get(nextI)[0];
					nextA[2]=chosenParts.get(nextI)[1]+1;
					dir[0]=2;
					dir[1]=0;
					dir[2]=3;
					break;
				case LEFT:
					nextA[1]=chosenParts.get(nextI)[0]-1;
					nextA[2]=chosenParts.get(nextI)[1];
					dir[0]=0;
					dir[1]=3;
					dir[2]=1;
					break;
				case RIGHT:
					nextA[1]=chosenParts.get(nextI)[0]+1;
					nextA[2]=chosenParts.get(nextI)[1];
					dir[0]=1;
					dir[1]=2;
					dir[2]=0;
					break;
				default:
					break;
				}
				if((useSize2 && (nextA[1]+partsNum/4-minX+1>puzzleParts.nw || maxX-nextA[1]-partsNum/4+1>puzzleParts.nw ||
						nextA[2]+partsNum/4-minY+1>puzzleParts.nh || maxY-nextA[2]-partsNum/4+1>puzzleParts.nh)))
					continue;
				//				if(finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4] !=null && (bestBuddies[finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][1]!=next || bestBuddies[next][0]!=finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1)) break;
				//				if(finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4] !=null && (bestBuddies[finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1][0]!=next || bestBuddies[next][1]!=finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1)) break;
				//				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4] !=null && (bestBuddies[finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][3]!=next || bestBuddies[next][2]!=finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1)) break;
				//				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4] !=null && (bestBuddies[finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1][2]!=next || bestBuddies[next][3]!=finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1)) break;
				int flagc=0;
				double w1=0.5,w2=0.5;
				//				if(partsLeft<partsNum/16){
				//					w1=0.25;
				//					w2=0.75;
				//				}
				//				Double val=0.0;
				//System.out.println(nextA[0]+" "+nextA[1]+" "+nextA[2]+" "+val);
				if(finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4] !=null && bestBuddies[finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][1]==nextA[0]) {
					flagc++;
					//					val+=confMatrix[finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][nextA[0]][1]/2+confMatrix[nextA[0]][finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][0]/2;
				}

				if(finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4] !=null && bestBuddies[nextA[0]][1]==finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1) {
					flagc++;
					//					val+=confMatrix[finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1][nextA[0]][0]/2+confMatrix[nextA[0]][finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1][1]/2;
				}

				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4] !=null && bestBuddies[finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][3]==nextA[0]) {
					flagc++;
					//					val+=confMatrix[finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][nextA[0]][3]/2+confMatrix[nextA[0]][finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][2]/2;
				}

				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4] !=null && bestBuddies[nextA[0]][3]==finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1) {
					flagc++;
					//					val+=confMatrix[finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1][nextA[0]][2]/2+confMatrix[nextA[0]][finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1][3]/2;
				}
				if(finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4] !=null && bestBuddies[finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][1]==nextA[0]) {
					flagc++;
					//					val+=confMatrix[finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][nextA[0]][1]/2+confMatrix[nextA[0]][finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][0]/2;
				}

				if(finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4] !=null && bestBuddies[nextA[0]][1]==finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1) {
					flagc++;
					//					val+=confMatrix[finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1][nextA[0]][0]/2+confMatrix[nextA[0]][finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1][1]/2;
				}

				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4] !=null && bestBuddies[finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][3]==nextA[0]) {
					flagc++;
					//					val+=confMatrix[finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][nextA[0]][3]/2+confMatrix[nextA[0]][finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][2]/2;
				}

				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4] !=null  && bestBuddies[nextA[0]][3]==finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1) {
					flagc++;
					//					val+=confMatrix[finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1][nextA[0]][2]/2+confMatrix[nextA[0]][finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1][3]/2;
				}			
				double th2=0;
				int flagc2=0;
				if(finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4] !=null){
					th2+=w1*confMatrix[1][finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][nextA[0]]+w2*confMatrix[0][nextA[0]][finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1];
					flagc2++;
				}
				if(finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4] !=null){
					th2+=w1*confMatrix[0][finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1][nextA[0]]+w2*confMatrix[1][nextA[0]][finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1];
					flagc2++;
				}
				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4] !=null){
					th2+=w1*confMatrix[3][finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][nextA[0]]+w2*confMatrix[2][nextA[0]][finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1];
					flagc2++;
				}
				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4] !=null){
					th2+=w1*confMatrix[2][finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1][nextA[0]]+w2*confMatrix[3][nextA[0]][finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1];
					flagc2++;
				}
				//				if(flagc2==1 && //partsNum-partsLeft>4 &&
				//						(bestBuddies[next][dir[0]]!=-1 && bestBuddies[bestBuddies[next][dir[0]]][dir[1]]!=-1
				//						&& bestBuddies[bestBuddies[bestBuddies[next][dir[0]]][dir[1]]][dir[2]]!=nextI) //&&
				//						(bestBuddies[next][dir[2]]!=-1 && bestBuddies[bestBuddies[next][dir[2]]][dir[1]]!=-1
				//						&& bestBuddies[bestBuddies[bestBuddies[next][dir[2]]][dir[1]]][dir[0]]!=nextI)						)
				//						)
				//					continue;
				double add=((flagc-1)>0)?(flagc-1):0;
				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]+partsNum/4]==null){
					synchronized (nextToPlace){	
						if(!missPart)
							nextToPlace.get(12).put(th2/flagc2+flagc2*0.000+add*0.3,nextA);///flagc+(flagc-1)*0.08
						else
							nextToPlace.get(12).put(th2/flagc2,nextA);///flagc+(flagc-1)*0.08
					}
				}

				//				nextToPlace.get(budddiesNumber[next]).put(bestBuddiesTree.get(budddiesNumber[next]).get(next),nextA);
			}
		}
	}

	private void addBuddies2(int nextI) {
		ArrayList<Orientation> ao=new ArrayList<Orientation>();
		ao.add(Orientation.DOWN);ao.add(Orientation.UP);ao.add(Orientation.LEFT);ao.add(Orientation.RIGHT);
		Collections.shuffle(ao);
		for(Orientation or:Orientation.values()){
			int next=bestBuddies[nextI][or.ordinal()];
			//			if(puzzleParts.isBurn(next) && partsLeft>puzzleParts.burnPartsNum+2)
			//				continue;
			if(next==-1)
				continue;
			int op=(or.ordinal()%2==0)?(or.ordinal()+1):(or.ordinal()-1);
			//			int next2=bestBuddies[next][op];
			//			Double val=confMatrix[nextI][next][or.ordinal()]/2+confMatrix[next][nextI][op]/2;
			//			Double val=confMatrix[next][nextI][op];
			//			if(val<0.368) continue;
			//			int count=0;
			//			Double val=confMatrix[next][nextI][op];
			//			if(val<confMatrix[next][bestBuddies[next][0]][0])
			//				count++;
			//			if(val<confMatrix[next][bestBuddies[next][1]][1])
			//				count++;
			//			if(val<confMatrix[next][bestBuddies[next][2]][2])
			//				count++;
			//			if(val<confMatrix[next][bestBuddies[next][3]][3])
			//				count++;
			//			if(count>1)
			//				continue;
			Integer nextA[]=new Integer[3];
			nextA[0]=next;
			if(!chosenParts.containsKey(next)){ //&&
				switch(or){
				case UP:
					nextA[1]=chosenParts.get(nextI)[0];
					nextA[2]=chosenParts.get(nextI)[1]-1;
					break;
				case DOWN:
					nextA[1]=chosenParts.get(nextI)[0];
					nextA[2]=chosenParts.get(nextI)[1]+1;
					break;
				case LEFT:
					nextA[1]=chosenParts.get(nextI)[0]-1;
					nextA[2]=chosenParts.get(nextI)[1];
					break;
				case RIGHT:
					nextA[1]=chosenParts.get(nextI)[0]+1;
					nextA[2]=chosenParts.get(nextI)[1];
					break;
				default:
					break;
				}
				if((useSize2 && (nextA[1]+partsNum/4-minX+1>puzzleParts.nw || maxX-nextA[1]-partsNum/4+1>puzzleParts.nw ||
						nextA[2]+partsNum/4-minY+1>puzzleParts.nh || maxY-nextA[2]-partsNum/4+1>puzzleParts.nh)))
					continue;
				int flagc=0;
				double w1=0.5,w2=0.5;
				//				if(partsLeft<partsNum/16){
				//					w1=0.25;
				//					w2=0.75;
				//				}
				//				double val=0;
				//System.out.println(nextA[0]+" "+nextA[1]+" "+nextA[2]+" "+val);
				if(finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4] !=null && bestBuddies[finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][1]==nextA[0]) {
					flagc++;
					//					val+=confMatrix[finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][nextA[0]][1]/2+confMatrix[nextA[0]][finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][0]/2;
				}

				if(finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4] !=null && bestBuddies[nextA[0]][1]==finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1) {
					flagc++;
					//					val+=confMatrix[finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1][nextA[0]][0]/2+confMatrix[nextA[0]][finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1][1]/2;
				}

				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4] !=null && bestBuddies[finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][3]==nextA[0]) {
					flagc++;
					//					val+=confMatrix[finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][nextA[0]][3]/2+confMatrix[nextA[0]][finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][2]/2;
				}

				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4] !=null && bestBuddies[nextA[0]][3]==finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1) {
					flagc++;
					//					val+=confMatrix[finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1][nextA[0]][2]/2+confMatrix[nextA[0]][finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1][3]/2;
				}
				if(finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4] !=null && bestBuddies[finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][1]==nextA[0]) {
					flagc++;
					//					val+=confMatrix[finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][nextA[0]][1]/2+confMatrix[nextA[0]][finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][0]/2;
				}

				if(finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4] !=null && bestBuddies[nextA[0]][1]==finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1) {
					flagc++;
					//					val+=confMatrix[finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1][nextA[0]][0]/2+confMatrix[nextA[0]][finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1][1]/2;
				}

				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4] !=null && bestBuddies[finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][3]==nextA[0]) {
					flagc++;
					//					val+=confMatrix[finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][nextA[0]][3]/2+confMatrix[nextA[0]][finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][2]/2;
				}

				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4] !=null  && bestBuddies[nextA[0]][3]==finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1) {
					flagc++;
					//					val+=confMatrix[finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1][nextA[0]][2]/2+confMatrix[nextA[0]][finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1][3]/2;
				}			
				double th2=0,maxTh=0;
				int flagc2=0;
				if(finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4] !=null){
					th2+=w1*confMatrix[1][finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][nextA[0]]+w2*confMatrix[0][nextA[0]][finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1];
					//					maxTh=Math.max(maxTh,w1*confMatrix[finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][nextA[0]][1]+w2*confMatrix[nextA[0]][finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][0]);
					flagc2++;
				}
				if(finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4] !=null){
					th2+=w1*confMatrix[0][finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1][nextA[0]]+w2*confMatrix[1][nextA[0]][finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1];
					//					maxTh=Math.max(maxTh,w1*confMatrix[finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1][nextA[0]][0]+w2*confMatrix[nextA[0]][finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1][1]);
					flagc2++;
				}
				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4] !=null){
					th2+=w1*confMatrix[3][finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][nextA[0]]+w2*confMatrix[2][nextA[0]][finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1];
					//					maxTh=Math.max(maxTh,w1*confMatrix[finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][nextA[0]][3]+w2*confMatrix[nextA[0]][finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][2]);
					flagc2++;
				}
				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4] !=null){
					th2+=w1*confMatrix[2][finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1][nextA[0]]+w2*confMatrix[3][nextA[0]][finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1];
					//					maxTh=Math.max(maxTh,w1*confMatrix[finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1][nextA[0]][2]+w2*confMatrix[nextA[0]][finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1][3]);
					flagc2++;
				}
				double add=((flagc-1)>0)?(flagc-1):0;
				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]+partsNum/4]==null){
					synchronized (nextToPlace){	
						if(!missPart)
							nextToPlace.get(12).put(th2/flagc2+flagc2*0.5+add*0.3,nextA);
						else
							nextToPlace.get(12).put(th2/flagc2/*+maxTh/2*/,nextA);
					}
				}
			}
			//			next=bestBuddies2[nextI][or.ordinal()];
			//			if(next==-1)
			//				continue;
			//			nextA=new Integer[3];
			//			nextA[0]=next;
			//			if(!chosenParts.containsKey(next)){ //&&
			//				switch(or){
			//				case UP:
			//					nextA[1]=chosenParts.get(nextI)[0];
			//					nextA[2]=chosenParts.get(nextI)[1]-1;
			//					break;
			//				case DOWN:
			//					nextA[1]=chosenParts.get(nextI)[0];
			//					nextA[2]=chosenParts.get(nextI)[1]+1;
			//					break;
			//				case LEFT:
			//					nextA[1]=chosenParts.get(nextI)[0]-1;
			//					nextA[2]=chosenParts.get(nextI)[1];
			//					break;
			//				case RIGHT:
			//					nextA[1]=chosenParts.get(nextI)[0]+1;
			//					nextA[2]=chosenParts.get(nextI)[1];
			//					break;
			//				default:
			//					break;
			//				}
			//				if((useSize && (nextA[1]+partsNum/4-minX+1>puzzleParts.nw || maxX-nextA[1]-partsNum/4+1>puzzleParts.nw ||
			//						nextA[2]+partsNum/4-minY+1>puzzleParts.nh || maxY-nextA[2]-partsNum/4+1>puzzleParts.nh)))
			//					continue;
			//				int flagc=0;
			//				double w1=0.5,w2=0.5;
			//				if(partsLeft<partsNum/4){
			//					w1=0.25;
			//					w2=0.75;
			//				}
			//				if(finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4] !=null && bestBuddies[finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][1]==nextA[0]) {
			//					flagc++;
			//				}
			//
			//				if(finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4] !=null && bestBuddies[nextA[0]][1]==finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1) {
			//					flagc++;
			//				}
			//
			//				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4] !=null && bestBuddies[finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][3]==nextA[0]) {
			//					flagc++;
			//				}
			//
			//				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4] !=null && bestBuddies[nextA[0]][3]==finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1) {
			//					flagc++;
			//				}
			//				if(finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4] !=null && bestBuddies[finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][1]==nextA[0]) {
			//					flagc++;
			//				}
			//
			//				if(finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4] !=null && bestBuddies[nextA[0]][1]==finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1) {
			//					flagc++;
			//				}
			//
			//				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4] !=null && bestBuddies[finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][3]==nextA[0]) {
			//					flagc++;
			//				}
			//
			//				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4] !=null  && bestBuddies[nextA[0]][3]==finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1) {
			//					flagc++;
			//				}			
			//				double th2=0,maxTh=0;
			//				int flagc2=0;
			//				if(finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4] !=null){
			//					th2+=w1*confMatrix[finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][nextA[0]][1]+w2*confMatrix[nextA[0]][finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][0];
			//					maxTh=Math.max(maxTh,w1*confMatrix[finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][nextA[0]][1]+w2*confMatrix[nextA[0]][finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][0]);
			//					flagc2++;
			//				}
			//				if(finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4] !=null){
			//					th2+=w1*confMatrix[finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1][nextA[0]][0]+w2*confMatrix[nextA[0]][finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1][1];
			//					maxTh=Math.max(maxTh,w1*confMatrix[finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1][nextA[0]][0]+w2*confMatrix[nextA[0]][finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1][1]);
			//					flagc2++;
			//				}
			//				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4] !=null){
			//					th2+=w1*confMatrix[finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][nextA[0]][3]+w2*confMatrix[nextA[0]][finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][2];
			//					maxTh=Math.max(maxTh,w1*confMatrix[finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][nextA[0]][3]+w2*confMatrix[nextA[0]][finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][2]);
			//					flagc2++;
			//				}
			//				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4] !=null){
			//					th2+=w1*confMatrix[finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1][nextA[0]][2]+w2*confMatrix[nextA[0]][finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1][3];
			//					maxTh=Math.max(maxTh,w1*confMatrix[finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1][nextA[0]][2]+w2*confMatrix[nextA[0]][finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1][3]);
			//					flagc2++;
			//				}
			//				double add=((flagc-2)>0)?(flagc-2):0;
			//				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]+partsNum/4]==null){
			//					if(!missPart)
			//						try{
			//							nextToPlace.get(12).put(th2/flagc2+flagc2*0.000+add*0.005,nextA);
			//						}
			//					catch (Exception e) {
			//						System.out.println("E"+flagc2);
			//					}
			//					else
			//						nextToPlace.get(12).put(th2/flagc2/*+maxTh/2*/,nextA);
			//				}
			//			}
		}
	}

	private void addBuddies3(int nextI) {
		ArrayList<Orientation> ao=new ArrayList<Orientation>();
		ao.add(Orientation.DOWN);ao.add(Orientation.UP);ao.add(Orientation.LEFT);ao.add(Orientation.RIGHT);
		Collections.shuffle(ao);
		for(Orientation or:Orientation.values()){
			int next=bestBuddies[nextI][or.ordinal()];
			//			if(puzzleParts.isBurn(next) && partsLeft>puzzleParts.burnPartsNum+2)
			//				continue;
			if(next==-1)
				continue;
			int op=(or.ordinal()%2==0)?(or.ordinal()+1):(or.ordinal()-1);
			Integer nextA[]=new Integer[3];
			nextA[0]=next;
			//			Double val=confMatrix[nextI][next][or.ordinal()]/2+confMatrix[next][nextI][op]/2;
			int count=0;
			//			Float val=confMatrix[next][nextI][op];
			//			if(val<confMatrix[next][bestBuddies[next][0]][0])
			//				count++;
			//			if(val<confMatrix[next][bestBuddies[next][1]][1])
			//				count++;
			//			if(val<confMatrix[next][bestBuddies[next][2]][2])
			//				count++;
			//			if(val<confMatrix[next][bestBuddies[next][3]][3])
			//				count++;

			int dir[]=new int[3];
			Integer cN=null,cN2=null;
			if(nextI==bestBuddies[next][op]  && !chosenParts.containsKey(next)){ //&&
				//!nextToPlace.get(budddiesNumber[next]-1).containsKey(bestBuddiesTree.get(budddiesNumber[next]-1).get(next))){
				switch(or){
				case UP:
					nextA[1]=chosenParts.get(nextI)[0];
					nextA[2]=chosenParts.get(nextI)[1]-1;
					dir[0]=3;
					dir[1]=1;
					dir[2]=2;
					cN=finalPlacement[nextA[2]+partsNum/4][nextA[1]+partsNum/4+1];
					cN2=finalPlacement[nextA[2]+partsNum/4][nextA[1]+partsNum/4-1];
					break;
				case DOWN:
					nextA[1]=chosenParts.get(nextI)[0];
					nextA[2]=chosenParts.get(nextI)[1]+1;
					dir[0]=2;
					dir[1]=0;
					dir[2]=3;
					cN=finalPlacement[nextA[2]+partsNum/4][nextA[1]+partsNum/4-1];
					cN2=finalPlacement[nextA[2]+partsNum/4][nextA[1]+partsNum/4+1];
					break;
				case LEFT:
					nextA[1]=chosenParts.get(nextI)[0]-1;
					nextA[2]=chosenParts.get(nextI)[1];
					dir[0]=0;
					dir[1]=3;
					dir[2]=1;
					cN=finalPlacement[nextA[2]+partsNum/4-1][nextA[1]+partsNum/4];
					cN2=finalPlacement[nextA[2]+partsNum/4+1][nextA[1]+partsNum/4];
					break;
				case RIGHT:
					nextA[1]=chosenParts.get(nextI)[0]+1;
					nextA[2]=chosenParts.get(nextI)[1];
					dir[0]=1;
					dir[1]=2;
					dir[2]=0;
					cN=finalPlacement[nextA[2]+partsNum/4+1][nextA[1]+partsNum/4];
					cN2=finalPlacement[nextA[2]+partsNum/4-1][nextA[1]+partsNum/4];
					break;
				default:
					break;
				}
				if((useSize2 && (nextA[1]+partsNum/4-minX+1>puzzleParts.nw || maxX-nextA[1]-partsNum/4+1>puzzleParts.nw ||
						nextA[2]+partsNum/4-minY+1>puzzleParts.nh || maxY-nextA[2]-partsNum/4+1>puzzleParts.nh)))
					continue;
				//				if(nextA[1]>partsNum/2-2|| nextA[1]<0 ||
				//						nextA[0]>partsNum/2-2|| nextA[0]<0)
				//					continue;
				//				if(finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4] !=null && (bestBuddies[finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][1]!=next || bestBuddies[next][0]!=finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1)) break;
				//				if(finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4] !=null && (bestBuddies[finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1][0]!=next || bestBuddies[next][1]!=finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1)) break;
				//				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4] !=null && (bestBuddies[finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][3]!=next || bestBuddies[next][2]!=finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1)) break;
				//				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4] !=null && (bestBuddies[finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1][2]!=next || bestBuddies[next][3]!=finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1)) break;
				int flagc=0;
				double w1=0.5,w2=0.5;
				//				if(partsLeft<partsNum/16){
				//					w1=0.25;
				//					w2=0.75;
				//				}
				//				Double val=0.0;
				//System.out.println(nextA[0]+" "+nextA[1]+" "+nextA[2]+" "+val);
				if(finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4] !=null && bestBuddies[finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][1]==nextA[0]) {
					flagc++;
					//					val+=confMatrix[finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][nextA[0]][1]/2+confMatrix[nextA[0]][finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][0]/2;
				}

				if(finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4] !=null && bestBuddies[nextA[0]][1]==finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1) {
					flagc++;
					//					val+=confMatrix[finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1][nextA[0]][0]/2+confMatrix[nextA[0]][finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1][1]/2;
				}

				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4] !=null && bestBuddies[finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][3]==nextA[0]) {
					flagc++;
					//					val+=confMatrix[finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][nextA[0]][3]/2+confMatrix[nextA[0]][finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][2]/2;
				}

				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4] !=null && bestBuddies[nextA[0]][3]==finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1) {
					flagc++;
					//					val+=confMatrix[finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1][nextA[0]][2]/2+confMatrix[nextA[0]][finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1][3]/2;
				}
				if(finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4] !=null && bestBuddies[finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][1]==nextA[0]) {
					flagc++;
					//					val+=confMatrix[finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][nextA[0]][1]/2+confMatrix[nextA[0]][finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][0]/2;
				}

				if(finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4] !=null && bestBuddies[nextA[0]][1]==finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1) {
					flagc++;
					//					val+=confMatrix[finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1][nextA[0]][0]/2+confMatrix[nextA[0]][finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1][1]/2;
				}

				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4] !=null && bestBuddies[finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][3]==nextA[0]) {
					flagc++;
					//					val+=confMatrix[finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][nextA[0]][3]/2+confMatrix[nextA[0]][finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][2]/2;
				}

				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4] !=null  && bestBuddies[nextA[0]][3]==finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1) {
					flagc++;
					//					val+=confMatrix[finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1][nextA[0]][2]/2+confMatrix[nextA[0]][finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1][3]/2;
				}			
				double th2=0;
				int flagc2=0;
				if(finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4] !=null){
					th2+=w1*confMatrix[1][finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1][nextA[0]]+w2*confMatrix[0][nextA[0]][finalPlacement[nextA[2]-1+partsNum/4][nextA[1]+partsNum/4]-1];
					flagc2++;
				}
				if(finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4] !=null){
					th2+=w1*confMatrix[0][finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1][nextA[0]]+w2*confMatrix[1][nextA[0]][finalPlacement[nextA[2]+1+partsNum/4][nextA[1]+partsNum/4]-1];
					flagc2++;
				}
				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4] !=null){
					th2+=w1*confMatrix[3][finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1][nextA[0]]+w2*confMatrix[2][nextA[0]][finalPlacement[nextA[2]+partsNum/4][nextA[1]-1+partsNum/4]-1];
					flagc2++;
				}
				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4] !=null){
					th2+=w1*confMatrix[2][finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1][nextA[0]]+w2*confMatrix[3][nextA[0]][finalPlacement[nextA[2]+partsNum/4][nextA[1]+1+partsNum/4]-1];
					flagc2++;
				}
				double bonus=0.0;
				//				if(flagc2==1)
				//					bonus=0.5;
				//				if(flagc2==1 &&
				//						(bestBuddies[next][dir[0]]!=-1 && bestBuddies[bestBuddies[next][dir[0]]][dir[1]]!=-1
				//						&& bestBuddies[bestBuddies[bestBuddies[next][dir[0]]][dir[1]]][dir[2]]!=nextI) &&
				//						(bestBuddies[next][dir[2]]!=-1 && bestBuddies[bestBuddies[next][dir[2]]][dir[1]]!=-1
				//						&& bestBuddies[bestBuddies[bestBuddies[next][dir[2]]][dir[1]]][dir[0]]!=nextI))
				//						)
				//					bonus=0;
				//				continue;
				if(flagc2==1 &&
						(bestBuddies[next][dir[0]]!=-1 && bestBuddies[bestBuddies[next][dir[0]]][dir[1]]!=-1
						&& bestBuddies[bestBuddies[bestBuddies[next][dir[0]]][dir[1]]][dir[2]]==nextI))
					bonus+=0.25;
				if(cN != null && flagc2==1 &&
						(bestBuddies[next][dir[0]]!=-1 && 
						bestBuddies[bestBuddies[next][dir[0]]][dir[1]]==cN))
					bonus+=0.25;
				if(flagc2==1 &&
						(bestBuddies[next][dir[2]]!=-1 && bestBuddies[bestBuddies[next][dir[2]]][dir[1]]!=-1
						&& bestBuddies[bestBuddies[bestBuddies[next][dir[2]]][dir[1]]][dir[0]]==nextI))
					bonus+=0.25;
				if(cN2 != null && flagc2==1 &&
						(bestBuddies[next][dir[0]]!=-1 && 
						bestBuddies[bestBuddies[next][dir[2]]][dir[1]]==cN2))
					bonus+=0.25;
				//				if(bonus<0.2 && count>2)
				//					continue;
				//				bonus-=count*0.03;
				//				System.out.println("S1:"+th2/flagc2);
				double add=((flagc-1)>0)?(flagc-1):0;
				double add2=((flagc2-2)>0)?(flagc2-2):0;
				if(finalPlacement[nextA[2]+partsNum/4][nextA[1]+partsNum/4]==null){
					synchronized (nextToPlace){	
						if(!missPart)
							nextToPlace.get(12).put(th2/flagc2+add2*0.3+add*0.3+bonus,nextA);///flagc+(flagc-1)*0.08
						else
							nextToPlace.get(12).put(th2/flagc2+bonus,nextA);///flagc+(flagc-1)*0.08
					}
				}
				//				nextToPlace.get(budddiesNumber[next]).put(bestBuddiesTree.get(budddiesNumber[next]).get(next),nextA);
			}
		}
	}

	private void findBestStart(boolean first) {
		TreeMap<Double,Integer> tm=new TreeMap<Double,Integer>();
		//		TreeMap<Double,Integer> tm2=new TreeMap<Double,Integer>();
		double count=0,TP=0;
		int nh=puzzleParts.nh, nw=puzzleParts.nw;
		for(int i=0;i<partsNum;i++){
			budddiesNumber[i]=0;
			Double avgConf=0.0;
			for(int or=0;or<4;or++){
				int op=(or%2==0)?(or+1):(or-1);
				if(bestBuddies[i][or]==-1)
					continue;
				avgConf+=confMatrix[or][i][bestBuddies[i][or]]/2+confMatrix[op][bestBuddies[i][or]][i]/2;
				if(i==bestBuddies[bestBuddies[i][or]][op]) {
					budddiesNumber[i]+=25;
					count++;
					switch (or){
					case 0:
						if(i-nw==bestBuddies[i][or])
							TP++;
						break;
					case 1:
						if(i+nw==bestBuddies[i][or])
							TP++;
						break;
					case 2:
						if(i-1==bestBuddies[i][or])
							TP++;
						break;
					case 3:
						if(i+1==bestBuddies[i][or])
							TP++;
						break;

					}
				}
				if(bestBuddies[bestBuddies[i][or]][or]==-1)
					continue;
				if(first && bestBuddies[i][or]>-1 && bestBuddies[bestBuddies[i][or]][or]>-1 && bestBuddies[i][or]==bestBuddies[bestBuddies[bestBuddies[i][or]][or]][op]) budddiesNumber[i]+=5;
				if(first && bestBuddies[i][or]>-1 && bestBuddies[bestBuddies[i][or]][or]>-1 && bestBuddies[bestBuddies[bestBuddies[i][or]][or]][or]>-1 && bestBuddies[bestBuddies[i][or]][or]==bestBuddies[bestBuddies[bestBuddies[bestBuddies[i][or]][or]][or]][op]) budddiesNumber[i]++;
			}
			avgConf/=4;
			if(
					bestBuddies[bestBuddies[bestBuddies[bestBuddies[i][0]][3]][1]][2]==i &&
					bestBuddies[bestBuddies[bestBuddies[bestBuddies[i][3]][1]][2]][0]==i &&
					bestBuddies[bestBuddies[bestBuddies[bestBuddies[i][1]][2]][0]][3]==i &&
					bestBuddies[bestBuddies[bestBuddies[bestBuddies[i][2]][0]][3]][1]==i 
					){
				if(budddiesNumber[i]>119) {

					//				Double e=0.2*findEntropy(i)+0.2*findEntropy(bestBuddies[i][0])+0.2*findEntropy(bestBuddies[i][1])+
					//						0.2*findEntropy(bestBuddies[i][2])+0.2*findEntropy(bestBuddies[i][3]);
					tm.put(4*avgConf,i);//e+
				}
				//				count++;
			}
			//if(budddiesNumber[i]-1>0){
			//bestBuddiesTree.get(budddiesNumber[i]).put(i,avgConf);	
			//}
		}
		System.out.println("Pr: "+(count+4*puzzleParts.burnPartsNum+4*puzzleParts.darkPartsNum)/((nh-1)*(nw)+(nw-1)*(nh))/2);
		System.out.println("BB: "+count/2+" TP: "+TP/2+" NOB: "+((nh-1)*(nw)+(nw-1)*(nh)));
		if(first){
			Iterator<Integer> ordIt=tm.descendingMap().values().iterator();
			if(ordIt.hasNext())
				startPart=ordIt.next();
//			startPart=1;
		}
	}

	private Double findEntropy(int i) {
		BufferedImage pimg=puzzleParts.getRGBPart(i);
		int hist[]=find_hist(pimg,16);
		Double res1=0.0,res2=0.0,res3=0.0;
		for(int j=0;j<16;j++){
			double h1=hist[j];
			double h2=hist[j+16];
			double h3=hist[j+32];
			if(h1!=0) res1+=(-Math.log10(h1/partSize/partSize)*(h1/partSize/partSize));
			if(h2!=0) res2+=(-Math.log10(h2/partSize/partSize)*(h2/partSize/partSize));
			if(h3!=0) res3+=(-Math.log10(h3/partSize/partSize)*(h3/partSize/partSize));
		}
		return (res1+res2+res3)/3;
	}

	private int[] find_hist(BufferedImage pimg, int bins) {
		int hist[]=new int[3*bins];
		for(int i=0;i<bins;i++){
			hist[i]=0;
		}

		for(int i=0;i<partSize;i++){
			for(int j=0;j<partSize;j++){
				int RGB=pimg.getRGB(i, j);
				Color c = new Color(RGB);
				int B=c.getBlue();
				hist[B/bins]++;
				int G=c.getGreen();
				hist[G/bins+bins]++;
				int R=c.getRed();
				hist[R/bins+2*bins]++;
			}
		}
		return hist;
	}

	private void calcConfMatrix() {
		Thread[] threads = new Thread[4];
		for(Orientation or:Orientation.values()){
			threads[or.ordinal()]=new Thread(new CalcConfMatrix(diffMatrix, confMatrix, bestBuddies, partSize, partsNum, partsLeft-missPartsNum, or,puzzleParts,missPart,chosenParts,bestBuddies2));
			threads[or.ordinal()].start();

		}

		for(Orientation or:Orientation.values()){

			try {
				threads[or.ordinal()].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}


	private void calcDiffMatrix() {
		Orientation arr[]=new Orientation[2];
		arr[0]=Orientation.values()[0];
		arr[1]=Orientation.values()[2];
		int thNum=4;//partsNum/400;//partsNum/(partsNum/500+1);
		int itNum=partsNum/thNum;
		Thread[] threads=new Thread[2*thNum*thNum+thNum+1];
		//		System.out.println("THNUM:"+thNum+" || Jump:"+itNum);
		//		threads = new Thread[2*2*partsNum];
		for(Orientation or:arr){
			int num=(or.ordinal()==0)?(0):(1);
			for(int i=0;i<partsNum;i+=itNum){
				for(int j=0;j<partsNum;j+=itNum){
					threads[num*thNum*thNum+i/itNum*thNum+j/itNum]=new Thread(new CalcDiff(diffMatrix,partSize,partsNum, puzzleParts, or, i,j,itNum));
					//				threads[2*num*partsNum+2*i+1]=new Thread(new CalcDiff(diffMatrix,partSize,partsNum, puzzleParts, or, i,1,0));
					threads[num*thNum*thNum+i/itNum*thNum+j/itNum].start();
					//				threads[2*num*partsNum+2*i+1].start();

				}
			}
		}

		for(Orientation or:arr){
			int num=(or.ordinal()==0)?(0):(1);
			for(int i=0;i<partsNum;i+=itNum){
				for(int j=0;j<partsNum;j+=itNum){
					try {
						threads[num*thNum*thNum+i/itNum*thNum+j/itNum].join();
						//					threads[2*num*partsNum+2*i+1].join();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}




	public static void main(String[] args) throws IOException {
		//		Date d1=new Date();
		//		PuzzleSolver.startGUI();
		//		SwingUtilities.invokeLater(new StartGUI());
		//		run.start();
		String in = "a";
		//				GeneratePuzzle gp=new GeneratePuzzle(in+".png",in+"_mix.png",28,true);
		//		Date d2=new Date();
		//				PuzzleSolver ps=new PuzzleSolver(gp.getGenIm(),28,in+"_res");

		//		Date d3=new Date();

		//		System.out.println(ps.chosenParts.keySet());
		//		System.out.println(ps.chosenParts.size());
		//		System.out.println(ps.startPart);
		//		System.out.println("gen time:"+(d2.getTime()-d1.getTime())/1000.0);
		//		System.out.println("solve time:"+(d3.getTime()-d2.getTime())/1000.0);
	}
}
