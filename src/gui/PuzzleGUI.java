package gui;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import java.awt.Event;
import java.awt.Font;
import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JLabel;

import java.awt.Toolkit;
import java.io.IOException;

import javax.swing.ImageIcon;

import puzzle.Global;
import puzzle.Orientation;
import puzzle.SaveworthyPuzzleData;

//Old class, not very useful now
//Shows a graphic representation of the dissimilarity and confidence scores between all peices of the puzzle
public class PuzzleGUI {
	private static String partsPath;
	private static String importancePath;
	private static final String SUFFIX = ".png";
	private static int frameX = 300;
	private static int frameY = 1;
	private static int frameWidth = 4000;
	private static int frameHeight = 3000;
	private static int xParts;
	private static int yParts;
	private static int partsNum;
	
	private JTextField txtRIGHT;
	private JTextField txtUP;
	private JTextField txtLEFT;
	private JTextField txtDOWN;
	private JButton[] btnComparison;
	private JButton[] btnImportance;
	private JButton btnCenter;
	public int selectedImage;
	private JFrame frame;
	private JFrame frame2;
	private JLayeredPane layeredPane;
	private JPanel confPanel; 
	private float confMatrix[][][];
	private float diffMatrix[][][];
	private float compatibilityMatrix[][][];
	private float confMatrixNormalized[][][];
	private float diffMatrixNormalized[][][];
	private float compatibilityMatrixNormalized[][][];
	private int buddiesMatrix[][];
	private int partSize = 90;//Global.ORIGINAL_PART_SIZE+2*Global.sizeOverlap;
	int borderThickness = 0;
	private int gapBetweenParts = 0;
	private int labelSize = 35;
	private int textBoxSize = 30;
	private int space = 10; //Standard Spacing from edges of containers
	private int bigSpace = 50; //Standard spacing between buttons and labels
	private int confTextLength = 1500;
	private Color textColor = new Color(200,150,100);
	private Color backgroundColor = new Color(200, 200, 255);
	private float labelOffset = (1-(float)labelSize/(borderThickness+gapBetweenParts+partSize))/2;
	private Orientation or;
	private JLabel lblOrientation;
	private JLabel lblBuddy;
	private JLabel correctPartRank;
	private JButton btnOrientation;
	private JButton btnCompatibility;
	JButton[] btnPart = new JButton[partsNum];
	JLabel[] lblPart = new JLabel[partsNum];
	JLabel[] lblConf = new JLabel[partsNum];
	BtnListener listener = new BtnListener();
	TxtListener txtListener = new TxtListener();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					xParts = 10;
					yParts = 7;
					partsNum = xParts*yParts;
					PuzzleGUI window = new PuzzleGUI(Global.puzzleName);
					window.frame.setVisible(true);
					window.frame2.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public static void launch() { //For Launching from outside the class
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					PuzzleGUI window = new PuzzleGUI(Global.puzzleName);
					window.frame.setVisible(true);
					window.frame2.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public PuzzleGUI(String puzzleName) throws HeadlessException, ClassNotFoundException, IOException {
		Global.puzzleName = puzzleName;
		Global.prepareDirectories();
		SaveworthyPuzzleData puzzle = SaveworthyPuzzleData.loadObject(Global.pathToPuzzleDataInit + Global.INIT_DATA_FILE);
		partsPath = Global.INDIVIDUAL_PARTS_FOLDER+puzzleName+"//"+Global.preparedName+"//"+puzzleName;
		importancePath = Global.IMPORTANCE_FOLDER+ puzzleName + "//" +Global.preparedName +"//"+Global.IMPORTANCE_TYPE+"//"+puzzleName;
		diffMatrix = puzzle.diff;
		confMatrix = puzzle.conf;
		buddiesMatrix = puzzle.buddies;
		diffMatrixNormalized = new float[4][partsNum][partsNum];
		confMatrixNormalized = new float[4][partsNum][partsNum];
		compatibilityMatrixNormalized = new float[4][partsNum][partsNum];
		normalizeCompatibility(diffMatrix,diffMatrixNormalized,true);
		normalizeCompatibility(confMatrix,confMatrixNormalized,false);
		or = Orientation.UP;
		compatibilityMatrix = confMatrix;
		compatibilityMatrixNormalized = confMatrixNormalized;
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame2 = new JFrame();
		layeredPane = new JLayeredPane();
		confPanel = new JPanel();
//		confPanel.setBounds(xParts*(partSize+gapBetweenParts+borderThickness)+frameX, frameY, 2*space + confTextLength, partsNum*textBoxSize+2*space);
		confPanel.setBounds(1, 1, 2*space + confTextLength, partsNum*textBoxSize+2*space);
//		confPanel.setBounds(1, 1, 200,200);
		frame.getContentPane().setBackground(backgroundColor);
		frame2.getContentPane().setBackground(backgroundColor);
		confPanel.setBackground(backgroundColor);
		frame.setBounds(1, 1, frameWidth, frameHeight);
		frame2.setBounds(1, 1, frameWidth, frameHeight);
		layeredPane.setBounds(frameX,frameY,frameWidth,frameHeight);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame2.getContentPane().setLayout(null);
		frame.getContentPane().add(layeredPane);
		frame2.getContentPane().add(confPanel);
		frame2.getContentPane().add(createComparisonPanel());
		frame2.getContentPane().add(createImportancePanel());
		
		
		for (int y=0; y<yParts; y++){
			for (int x=0; x<xParts; x++){
				int partIndex = y*xParts + x;
				btnPart[partIndex] = new JButton();
				lblPart[partIndex] = new JLabel();
				lblConf[partIndex] = new JLabel();
				btnPart[partIndex].setName(""+partIndex);
				lblPart[partIndex].setText(""+partIndex);
				lblPart[partIndex].setHorizontalAlignment(SwingConstants.CENTER);
				btnPart[partIndex].setIcon(new ImageIcon(partsPath+partIndex+SUFFIX));
				btnPart[partIndex].addActionListener(listener);
				lblPart[partIndex].setForeground(textColor);
				btnPart[partIndex].setBackground(Color.BLACK);
				lblPart[partIndex].setBackground(Color.BLACK);
				lblPart[partIndex].setOpaque(true);
				lblPart[partIndex].setFont(new Font("Tahoma", Font.BOLD, 16));
				btnPart[partIndex].setBounds((partSize+gapBetweenParts+borderThickness-1)*x, (partSize+gapBetweenParts+borderThickness-1)*y, partSize+borderThickness, partSize+borderThickness);
				lblPart[partIndex].setBounds((int)((partSize+gapBetweenParts+borderThickness)*(x+labelOffset)), (int)((partSize+gapBetweenParts+borderThickness)*(y+labelOffset)), labelSize, labelSize);
				confPanel.add(lblConf[partIndex]);
				lblConf[partIndex].setBounds(space,space+partIndex*textBoxSize,textBoxSize,confTextLength);
				layeredPane.add(btnPart[partIndex]);
				layeredPane.add(lblPart[partIndex]);
				layeredPane.setLayer(lblPart[partIndex],1);
			}
		}
		
		lblOrientation = new JLabel("UP");
		lblOrientation.setBounds(1, 1, frameX, 50);
		lblBuddy = new JLabel("Buddy : Null");
		correctPartRank = new JLabel("Correct Part  : Null");
		btnOrientation = new JButton("Orientation");
		lblOrientation.setHorizontalAlignment(SwingConstants.CENTER);
		btnOrientation.setBounds(frameX/4,bigSpace,frameX/2,50);
		btnOrientation.addActionListener(listener);
		lblBuddy.setBounds(1,2*bigSpace,frameX,50);
		lblBuddy.setHorizontalAlignment(SwingConstants.CENTER);
		correctPartRank.setBounds(1,3*bigSpace,frameX,50);
		correctPartRank.setHorizontalAlignment(SwingConstants.CENTER);
		btnCompatibility = new JButton("Confidence");
		btnCompatibility.setBounds(frameX/4,3*bigSpace,frameX/2,50);
		btnCompatibility.addActionListener(listener);
		
		frame.getContentPane().add(lblOrientation);
		frame.getContentPane().add(btnOrientation);
		frame.getContentPane().add(btnCompatibility);
		frame.getContentPane().add(lblBuddy);
	}

	private void normalizeCompatibility(float[][][]comp,float[][][]compNormalized, boolean isDiff){
		for (int k=0; k<comp.length; k++){
			for (int i = 0; i<comp[0].length; i++){
				float min = 99999;
				float max = -99999;
				for (int j=0; j<comp[0][0].length; j++){
					if (comp[k][i][j] < -10000 || comp[k][i][j]>10000){
						continue;
					}
					if (comp[k][i][j] < min){
						min = comp[k][i][j];
					}
					if (comp[k][i][j] > max){
						max = comp[k][i][j];
					}
				}
				for (int j=0; j<comp[0][0].length; j++){					
					if (isDiff){
						compNormalized[k][i][j] = Math.max(0,(comp[k][i][j]-min)/((max-min)/255));
						compNormalized[k][i][j] = 255 - Math.min(compNormalized[k][i][j],255);
						comp[k][i][j] = Math.min(comp[k][i][j],99999);
					}else{
						compNormalized[k][i][j] = Math.max(0,(comp[k][i][j]-min)/((1-min)/255));
						compNormalized[k][i][j] = Math.min(compNormalized[k][i][j],255);
						comp[k][i][j] = Math.max(comp[k][i][j],-99999);
					}
				}
			}
		}
	}
	private JPanel createImportancePanel(){
		JPanel panel = new JPanel();
		int panelSize = 3*partSize + 2*space;
//		panel.setBounds(confTextLength+frameX+xParts*(borderThickness+gapBetweenParts+partSize)+2*space+textBoxSize, frameY+panelSize+2*textBoxSize, panelSize,panelSize);
		panel.setBounds(confTextLength+2*space+textBoxSize, frameY+panelSize+2*textBoxSize, panelSize,panelSize);
		panel.setBackground(backgroundColor);
		panel.setLayout(null);
		btnImportance = new JButton[5];
		btnImportance[3] = new JButton("");
		btnImportance[3].setBounds(space+2*partSize, panelSize/2-partSize/2, partSize, partSize);
		panel.add(btnImportance[3]);
		
		btnImportance[4] = new JButton("");
		btnImportance[4].setBounds(space+partSize, panelSize/2-partSize/2, partSize, partSize);
		panel.add(btnImportance[4]);
		
		btnImportance[0] = new JButton("");
		btnImportance[0].setBounds(space+partSize, space, partSize, partSize);
		panel.add(btnImportance[0]);
		
		btnImportance[1] = new JButton("");
		btnImportance[1].setBounds(space+partSize, space+2*partSize, partSize, partSize);
		panel.add(btnImportance[1]);
		
		btnImportance[2] = new JButton("");
		btnImportance[2].setBounds(space, panelSize/2-partSize/2, partSize, partSize);
		panel.add(btnImportance[2]);
		
		return panel;
	}
	private JPanel createComparisonPanel(){
		JPanel panel = new JPanel();
		int panelSize = 3*partSize + 2*textBoxSize + 2*space;
//		panel.setBounds(confTextLength+frameX+xParts*(borderThickness+gapBetweenParts+partSize)+2*space, frameY, panelSize,panelSize);
		panel.setBounds(confTextLength+2*space, 1, panelSize,panelSize);
		panel.setBackground(backgroundColor);
		panel.setLayout(null);
		btnComparison = new JButton[4];
		btnComparison[3] = new JButton("");
		btnComparison[3].setBounds(space+textBoxSize+2*partSize, panelSize/2-partSize/2, partSize, partSize);
		panel.add(btnComparison[3]);
		
		btnCenter = new JButton("");
		btnCenter.setBounds(space+textBoxSize+partSize, panelSize/2-partSize/2, partSize, partSize);
		panel.add(btnCenter);
		
		btnComparison[0] = new JButton("");
		btnComparison[0].setBounds(space+textBoxSize+partSize, space+textBoxSize, partSize, partSize);
		panel.add(btnComparison[0]);
		
		btnComparison[1] = new JButton("");
		btnComparison[1].setBounds(space+textBoxSize+partSize, space+textBoxSize+2*partSize, partSize, partSize);
		panel.add(btnComparison[1]);
		
		btnComparison[2] = new JButton("");
		btnComparison[2].setBounds(space+textBoxSize, panelSize/2-partSize/2, partSize, partSize);
		panel.add(btnComparison[2]);
		
		txtRIGHT = new JTextField();
		txtRIGHT.setForeground(textColor);
		txtRIGHT.setName(""+3);
		txtRIGHT.setHorizontalAlignment(SwingConstants.CENTER);
		txtRIGHT.setText("15");
		txtRIGHT.setBounds(panelSize-space-textBoxSize,panelSize/2-textBoxSize/2, textBoxSize, textBoxSize);
		panel.add(txtRIGHT);
		txtRIGHT.addActionListener(txtListener);
		
		txtUP = new JTextField();
		txtUP.setForeground(textColor);
		txtUP.setName(""+0);
		txtUP.setText("15");
		txtUP.setHorizontalAlignment(SwingConstants.CENTER);
		txtUP.addActionListener(txtListener);
		txtUP.setBounds(panelSize/2-textBoxSize/2,space, textBoxSize, textBoxSize);
		panel.add(txtUP);
		
		txtLEFT = new JTextField();
		txtLEFT.setForeground(textColor);
		txtLEFT.setName(""+2);
		txtLEFT.setText("15");
		txtLEFT.setHorizontalAlignment(SwingConstants.CENTER);
		txtLEFT.addActionListener(txtListener);
		txtLEFT.setBounds(space, panelSize/2-textBoxSize/2, textBoxSize, textBoxSize);
		panel.add(txtLEFT);
		
		
		txtDOWN = new JTextField();
		txtDOWN.setForeground(textColor);
		txtDOWN.setName(""+1);
		txtDOWN.setText("15");
		txtDOWN.setHorizontalAlignment(SwingConstants.CENTER);
		txtDOWN.addActionListener(txtListener);
		txtDOWN.setBounds(panelSize/2-textBoxSize/2,panelSize-space-textBoxSize, textBoxSize, textBoxSize);
		panel.add(txtDOWN);
		return panel;
	}
   private class BtnListener implements ActionListener {
	     @Override
	     public void actionPerformed(ActionEvent e){
	         JButton source = (JButton)e.getSource();
	         if (source == btnOrientation){
	        	 updateOrientation();
	         }else if (source == btnCompatibility){
	        	 updateCompatibility();		 
	         }else{
	        	 selectedImage = Integer.parseInt(source.getName());
	         }

        	 int[]sortedCompIndexes = sortIndexes(compatibilityMatrixNormalized[or.ordinal()][selectedImage]);
        	 int correctNeighbor = getCorrectNeighbor(selectedImage,or,xParts,yParts);
        	 int correctNeighborRank = -1;
        	 for (int i=0; i<partsNum; i++){
        		 int color = (int)compatibilityMatrixNormalized[or.ordinal()][selectedImage][i];
        		 lblPart[i].setBackground(new Color(color,color,color));
        		 float confVal = compatibilityMatrix[or.ordinal()][selectedImage][sortedCompIndexes[i]];
        		 lblConf[i].setText(i+" - Part "+sortedCompIndexes[i]+": "+((float)Math.round(confVal*100))/100+"   ");
        		 if (correctNeighbor != -1 && sortedCompIndexes[i] == correctNeighbor){
        			 correctNeighborRank = i;
        		 }
        	 }
        	 lblBuddy.setText(selectedImage +"'s best match is : " + buddiesMatrix[selectedImage][or.ordinal()]+" , Ranked : "+ correctNeighborRank);
        	 int bestNeighbor = buddiesMatrix[selectedImage][or.ordinal()];
        	 lblPart[bestNeighbor].setBackground(new Color(0,255,0));
        	 Color c;
        	 if (buddiesMatrix[bestNeighbor][or.opposite().ordinal()] == selectedImage){
        		 c = new Color(0,150,0);
        	 }else{
        		 c = new Color(255,0,0);
        	 }
        	 lblPart[selectedImage].setBackground(c);
        	 btnCenter.setIcon(btnPart[selectedImage].getIcon());
        	 btnImportance[4].setIcon(new ImageIcon(importancePath+selectedImage+SUFFIX));
	        txtListener.actionPerformed(new ActionEvent(txtRIGHT,0,""));
	        txtListener.actionPerformed(new ActionEvent(txtLEFT,0,""));
	        txtListener.actionPerformed(new ActionEvent(txtUP,0,""));
	        txtListener.actionPerformed(new ActionEvent(txtDOWN,0,""));
	     }
   }
      
     private class TxtListener implements ActionListener {
	     @Override
	     public void actionPerformed(ActionEvent e) {
	         JTextField source = (JTextField)e.getSource();
	         int sourceID = Integer.parseInt(source.getName());
	         int wantedImageID = Integer.parseInt(source.getText());
	         btnComparison[sourceID].setIcon(btnPart[wantedImageID].getIcon());
	         btnImportance[sourceID].setIcon(new ImageIcon(importancePath+wantedImageID+SUFFIX));
	         int color = (int)Math.max(0,compatibilityMatrixNormalized[sourceID][selectedImage][wantedImageID]);
    		 source.setBackground(new Color(color,color,color));
	     }
     }
     
     private int[] sortIndexes(float[] conf){
    	 int[] sortedIndexes = new int[partsNum];
    	 for (int i=0; i<partsNum-1; i++){
    		 if (i==selectedImage){
    			 sortedIndexes[i] = partsNum-1;
    			 sortedIndexes[partsNum-1] = i;
    		 }else{
        		 sortedIndexes[i] = i; 
    		 }
    	 }
    	 for (int i=0; i<partsNum-1; i++){
    		 for (int j=i+1; j<partsNum-1; j++){
    			 if (conf[sortedIndexes[j]]>conf[sortedIndexes[i]]){
    				int temp = sortedIndexes[i];
    			 	sortedIndexes[i] = sortedIndexes[j];
    			 	sortedIndexes[j] = temp;
    			 }
    		 }
    	 }
    	 return sortedIndexes;
     }
     
     public static int getCorrectNeighbor(int part, Orientation or, int xParts, int yParts){
    	 int diff = 0;
    	 switch(or){
 		case UP:
 			if (part<xParts){return -1;}
			diff = -xParts;
			break;
		case DOWN:
			if (part>=xParts*(yParts-1)-1){return -1;}
			diff = xParts;
			break;
		case LEFT:
			if (part%xParts == 0){return -1;}
			diff = -1;
			break;
		case RIGHT:
			if (part%xParts == xParts-1){return -1;}
			diff = 1;
			break;
		}
    	 return part+diff; 
     }
     
	 private void updateOrientation(){
		switch(or){
		case UP:
			lblOrientation.setText("DOWN");
			or = Orientation.DOWN;
			break;
		case DOWN:
			lblOrientation.setText("LEFT");
			or = Orientation.LEFT;
			break;
		case LEFT:
			lblOrientation.setText("RIGHT");
			or = Orientation.RIGHT;
			break;
		case RIGHT:
			lblOrientation.setText("UP");
			or = Orientation.UP;
			break;
		}
   }
	 
	 private void updateCompatibility(){
		if (btnCompatibility.getText().equals("Confidence")){
			btnCompatibility.setText("Difference");
			compatibilityMatrix = diffMatrix;
			compatibilityMatrixNormalized = diffMatrixNormalized;
		}else if(btnCompatibility.getText().equals("Difference")){
			btnCompatibility.setText("Confidence");
			compatibilityMatrix = confMatrix;
			compatibilityMatrixNormalized = confMatrixNormalized;
		}
   }
}
