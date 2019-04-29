package oldPuzzle;

import java.io.File;
import java.io.IOException;

public class StartGUI{
	GeneratePuzzle gp;
	public static double neighbor_score_sum = 0;
	public static double direct_score_sum = 0;



	public StartGUI() {
		run();
	}


	public void run() {
		int PART_SIZE = 60;
		int burn_extent = 2;
		
		String FILE_EXTENSION = ".png";
		String SUFFIX = "c";
		String INPUT_FOLDER = ("C://SHARE//checkouts//puzzle_gan_data//java_artifacts//Puzzle Resources//OldPuzzleInput//BE" + burn_extent +"//");
		String OUTPUT_FOLDER = INPUT_FOLDER + "Output//";
		createDirectory(OUTPUT_FOLDER);
		String inputs[]={
				"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15", "16", "17", "18", "19", "20"
				};
//		int jjj=0;
		for(String id:inputs){
			String input = INPUT_FOLDER + id + SUFFIX + FILE_EXTENSION;
			try{
				gp=new GeneratePuzzle(input, OUTPUT_FOLDER + id + SUFFIX + "_shuffled"+ FILE_EXTENSION, PART_SIZE, false,0);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			System.out.println("Image: "+input);
//			Thread run=new Thread(new PuzzleSolver(gp.getGenIm(),28,inputSplit[0]+"_res",true,false));
//			run.start();
//			if(jjj<3)
				new PuzzleSolver(gp.getGenIm(),PART_SIZE,OUTPUT_FOLDER + id + SUFFIX +"_res",true,false,gp.getBorderNum());
//			if(jjj>0)
//				new PuzzleSolver(gp.getGenIm(),28,inputSplit[0]+"_res",false,false,gp.getBorderNum());
//			try {
//				run.join();
//			} catch (InterruptedException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
			System.out.println("------------------------------------------------");
//			jjj++;
		}
		System.out.println("Average neighbor score: " + neighbor_score_sum / inputs.length);
		System.out.println("Average direct score: " + direct_score_sum / inputs.length);
		System.exit(0);

	}


	public static void main(String[] args) {
		new StartGUI();
	}
	
	public static void createDirectory(String path){
		File directory = new File(path);
		if (!directory.exists()){
			directory.mkdir();
		}
	}
}
