package oldPuzzle;

import java.io.IOException;

public class StartGUI{
	GeneratePuzzle gp;


	public StartGUI() {
		run();
	}


	public void run() {


		String inputs[]={"C:\\workspace\\OldPuzzle\\Data Set\\432\\2.png"//"a1.png","a2.png","a4.png","a13.png"
//				"a1.png","a2.png","a3.png","a4.png","a5.png",
//				"a6.png","a7.png","a8.png","a9.png","a10.png",
//				"a11.png","a12.png","a13.png","a14.png","a15.png",
//				"a16.png","a17.png","a18.png","a19.png","a20.png",
//				"a1.png","a2.png","a3.png","a4.png","a5.png",
//				"a6.png","a7.png","a8.png","a9.png","a10.png",
//				"a11.png","a12.png","a13.png","a14.png","a15.png",
//				"a16.png","a17.png","a18.png","a19.png","a20.png",
//				"a1.png","a2.png","a3.png","a4.png","a5.png",
//				"a6.png","a7.png","a8.png","a9.png","a10.png",
//				"a11.png","a12.png","a13.png","a14.png","a15.png",
//				"a16.png","a17.png","a18.png","a19.png","a20.png",
//				"a1.png","a2.png","a3.png","a4.png","a5.png",
//				"a6.png","a7.png","a8.png","a9.png","a10.png",
//				"a11.png","a12.png","a13.png","a14.png","a15.png",
//				"a16.png","a17.png","a18.png","a19.png","a20.png",
//				"b1.jpg","b2.jpg","b3.jpg","b4.jpg","b5.jpg",
//				"b6.jpg","b7.jpg","b8.jpg","b9.jpg","b10.jpg",
//				"b11.jpg","b12.jpg","b13.jpg","b14.jpg","b15.jpg",
//				"b16.jpg","b17.jpg","b18.jpg","b19.jpg","b20.jpg",
//				"b1.jpg","b2.jpg","b3.jpg","b4.jpg","b5.jpg",
//				"b6.jpg","b7.jpg","b8.jpg","b9.jpg","b10.jpg",
//				"b11.jpg","b12.jpg","b13.jpg","b14.jpg","b15.jpg",
//				"b16.jpg","b17.jpg","b18.jpg","b19.jpg","b20.jpg",
//				"b1.jpg","b2.jpg","b3.jpg","b4.jpg","b5.jpg",
//				"b6.jpg","b7.jpg","b8.jpg","b9.jpg","b10.jpg",
//				"b11.jpg","b12.jpg","b13.jpg","b14.jpg","b15.jpg",
//				"b16.jpg","b17.jpg","b18.jpg","b19.jpg","b20.jpg",
//				"b1.jpg","b2.jpg","b3.jpg","b4.jpg","b5.jpg",
//				"b6.jpg","b7.jpg","b8.jpg","b9.jpg","b10.jpg",
//				"b11.jpg","b12.jpg","b13.jpg","b14.jpg","b15.jpg",
//				"b16.jpg","b17.jpg","b18.jpg","b19.jpg","b20.jpg",
//				"c1.jpg","c2.jpg","c3.jpg","c4.jpg","c5.jpg",
//				"c6.jpg","c7.jpg","c8.jpg","c9.jpg","c10.jpg",
//				"c11.jpg","c12.jpg","c13.jpg","c14.jpg","c15.jpg",
//				"c16.jpg","c17.jpg","c18.jpg","c19.jpg","c20.jpg",
//				"c1.jpg","c2.jpg","c3.jpg","c4.jpg","c5.jpg",
//				"c6.jpg","c7.jpg","c8.jpg","c9.jpg","c10.jpg",
//				"c11.jpg","c12.jpg","c13.jpg","c14.jpg","c15.jpg",
//				"c16.jpg","c17.jpg","c18.jpg","c19.jpg","c20.jpg",
//				"c1.jpg","c2.jpg","c3.jpg","c4.jpg","c5.jpg",
//				"c6.jpg","c7.jpg","c8.jpg","c9.jpg","c10.jpg",
//				"c11.jpg","c12.jpg","c13.jpg","c14.jpg","c15.jpg",
//				"c16.jpg","c17.jpg","c18.jpg","c19.jpg","c20.jpg",
//				"c1.jpg","c2.jpg","c3.jpg","c4.jpg","c5.jpg",
//				"c6.jpg","c7.jpg","c8.jpg","c9.jpg","c10.jpg",
//				"c11.jpg","c12.jpg","c13.jpg","c14.jpg","c15.jpg",
//				"c16.jpg","c17.jpg","c18.jpg","c19.jpg","c20.jpg",
//				"c1.jpg","c2.jpg","c3.jpg","c4.jpg","c5.jpg",
//				"c6.jpg","c7.jpg","c8.jpg","c9.jpg","c10.jpg",
//				"c11.jpg","c12.jpg","c13.jpg","c14.jpg","c15.jpg",
//				"c16.jpg","c17.jpg","c18.jpg","c19.jpg","c20.jpg",
//				"c1.jpg","c2.jpg","c3.jpg","c4.jpg","c5.jpg",
//				"c6.jpg","c7.jpg","c8.jpg","c9.jpg","c10.jpg",
//				"c11.jpg","c12.jpg","c13.jpg","c14.jpg","c15.jpg",
//				"c16.jpg","c17.jpg","c18.jpg","c19.jpg","c20.jpg",
//				"d1.jpg","d2.jpg","d3.jpg",
//				"e1.jpg","e2.jpg","e3.jpg",
//				"d1.jpg","d2.jpg","d3.jpg",
//				"e1.jpg","e2.jpg","e3.jpg",
//				"d1.jpg","d2.jpg","d3.jpg",
//				"e1.jpg","e2.jpg","e3.jpg",
//				"d1.jpg","d2.jpg","d3.jpg",
//				"e1.jpg","e2.jpg","e3.jpg",
//				"d1.jpg","d2.jpg","d3.jpg",
//				"e1.jpg","e2.jpg","e3.jpg",
//				"d1.jpg","d2.jpg","d3.jpg",
//				"e1.jpg","e2.jpg","e3.jpg",
//				"d1.jpg","d2.jpg","d3.jpg",
//				"e1.jpg","e2.jpg","e3.jpg",
//				"d1.jpg","d2.jpg","d3.jpg",
//				"e1.jpg","e2.jpg","e3.jpg",
//				"d1.jpg","d2.jpg","d3.jpg",
//				"e1.jpg","e2.jpg","e3.jpg",
//				"d1.jpg","d2.jpg","d3.jpg",
//				"e1.jpg","e2.jpg","e3.jpg",
//				"b9.jpg"
		
//		"g11i.bmp","g12i.bmp",
//		"g13i.bmp","g14i.bmp",
//		"g15i.bmp","g16i.bmp",
//		"g17i.bmp","g18i.bmp",
//		"g19i.bmp",
//				"g20i.bmp",
//		
//		"f04i.bmp",
//		

//		"h07i.bmp","h08i.bmp",
//		"h09i.bmp","h10i.bmp",
//		"h11i.bmp","h12i.bmp",
//		"h13i.bmp","h14i.bmp",
//		"h15i.bmp","h16i.bmp",
//		"h17i.bmp","h18i.bmp",
//		"h19i.bmp","h20i.bmp",
//		"h01i.bmp","h02i.bmp",
//		"h03i.bmp","h04i.bmp",
//		"h05i.bmp","h06i.bmp",
		
//		"f01i.bmp","f02i.bmp",
//		"f03i.bmp",
//				"f04i.bmp",
//		
//				"f06i.bmp",
//		"f07i.bmp",
//				"f08i.bmp",
//		"f09i.bmp","f10i.bmp",
//		"f11i.bmp",
//		"f12i.bmp",
//		"f13i.bmp",
//		
//		"f15i.bmp","f16i.bmp",
//		"f17i.bmp","f18i.bmp",
//		"f19i.bmp",
//		"f20i.bmp",
//		"f14i.bmp",
//		"f05i.bmp",
		
//		"g01i.bmp","g02i.bmp",
//		"g03i.bmp","g04i.bmp",
//		"g05i.bmp","g06i.bmp",
//		"g07i.bmp","g08i.bmp",
//		"g09i.bmp","g10i.bmp",
//		"g11i.bmp","g12i.bmp",
//		"g13i.bmp",
//		"g14i.bmp",
//		"g15i.bmp","g16i.bmp",
//		"g17i.bmp","g18i.bmp",
//		"g19i.bmp","g20i.bmp",

//		
//"C:\\Puzzle\\Java\\CleanPuzzlesF\\count\\b10_res.png",
//"C:\\Puzzle\\Java\\CleanPuzzlesF\\count\\b13b_res.png",
//"C:\\Puzzle\\Java\\CleanPuzzlesF\\count\\b15_res.png",
//"C:\\Puzzle\\Java\\CleanPuzzlesF\\count\\f12i_res.png",
//"b13.jpg"				
//				"h08i.bmp","h08i1.bmp","h08i2.bmp","h08i3.bmp","h08i4.bmp","h08i5.bmp","h08i6.bmp",


//				"g01i.bmp",
//				"g02i.bmp","g03i.bmp","g04i.bmp",
//				"g05i.bmp","g06i.bmp","g07i.bmp","g08i.bmp",
//				"g09i.bmp","g10i.bmp","g11i.bmp","g12i.bmp",
//				"g13i.bmp","g14i.bmp","g15i.bmp","g16i.bmp",
//				"g17i.bmp","g18i.bmp","g19i.bmp","g20i.bmp",
//				"g01i.bmp","g02i.bmp","g03i.bmp","g04i.bmp",
//				"g05i.bmp","g06i.bmp","g07i.bmp","g08i.bmp",
//				"g09i.bmp","g10i.bmp","g11i.bmp","g12i.bmp",
//				"g13i.bmp","g14i.bmp","g15i.bmp","g16i.bmp",
//				"g17i.bmp","g18i.bmp","g19i.bmp","g20i.bmp",
//				"g01i.bmp","g02i.bmp","g03i.bmp","g04i.bmp",
//				"g05i.bmp","g06i.bmp","g07i.bmp","g08i.bmp",
//				"g09i.bmp","g10i.bmp","g11i.bmp","g12i.bmp",
//				"g13i.bmp","g14i.bmp","g15i.bmp","g16i.bmp",
//				"g17i.bmp","g18i.bmp","g19i.bmp","g20i.bmp",
			

//				"f01i.bmp","f02i.bmp","f03i.bmp","f04i.bmp",
//				"f05i.bmp","f06i.bmp","f07i.bmp","f08i.bmp",
//				"f09i.bmp","f10i.bmp","f11i.bmp","f12i.bmp",
//				"f13i.bmp","f14i.bmp","f15i.bmp","f16i.bmp",
//				"f17i.bmp","f18i.bmp","f19i.bmp","f20i.bmp",

				//"f02i.bmp",	
				//"f18i.bmp",
			//	"a3.png"
				//"f13i.bmp",
//				"h18i.bmp",
//				"f07i.bmp","f07i.bmp","f07i.bmp",
//				"panor1.png",//"panor11.png","panor12.png","panor13.png","panor14.png",
//				"panor2.png",//"panor21.png","panor22.png","panor23.png","panor24.png",


		

		};
//		int jjj=0;
		for(String input:inputs){
			String inputSplit[]=input.split("\\.");
			try{
				gp=new GeneratePuzzle(input,inputSplit[0]+"_shuffled"+"."+inputSplit[1], 28,false,0);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			System.out.println("Image: "+input);
//			Thread run=new Thread(new PuzzleSolver(gp.getGenIm(),28,inputSplit[0]+"_res",true,false));
//			run.start();
//			if(jjj<3)
				new PuzzleSolver(gp.getGenIm(),28,inputSplit[0]+"_res",true,false,gp.getBorderNum());
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
		System.exit(0);

	}


	public static void main(String[] args) {
		new StartGUI();
	}

}
