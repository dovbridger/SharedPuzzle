package gui;

import org.imgscalr.Scalr;

import data_type.PoolEntry;
import puzzle.Global;
import puzzle.Orientation;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

import javax.swing.JScrollPane;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.swing.JTextField;

import java.awt.Font;

import javax.swing.UIManager;


public class GuiDebugger extends RunnableFrame{
	
	public static final int POOL_DATA_WIDTH = 28;
	private static final long serialVersionUID = 1L;
	private static final Color BUTTON = UIManager.getColor("Button.background");
	private static final Color DEF_BACKGROUND = UIManager.getColor("Frame.background");
	private static MasterControlPanel MCP;
	private static final int SIGNIFICANT_DIGITS = Global.SIGNIFICANT_DIGITS;
	private boolean currentStageForBuddies = false;
	private VisualPoolData VPD;
	private final int windowSizeX = 1000;
	private final int windowSizeY = 800;
	private final int POOL_SIZE = 10;
	private final int LABEL_SIZE = 30;
	private final int space = 3;
	private int[] lastButton = new int[2];
	private JPanel contentPane;
	private JPanel controlPanel;
	private JPanel boardPanel;
	private JPanel poolPanel;
	private JScrollPane scrollPoolPane;
	private JScrollPane scrollBoardPane;
	private int partSize = 116;
	private int maxPartSize = 80;
	private String title = "Puzzle Board";
	private int shrinkPartSize = 0;
	private int numParts = 576;
	private int spareRoom = 2;
	private int boardPartsX = 32;
	private int boardPartsY = 18;

	private int poolPanelX;
	private int poolPanelY;
	private int controlPanelX;
	private int controlPanelY; 
	private int boardPanelX; 
	private int boardPanelY; 

	private PoolEntry[] poolParts = new PoolEntry[POOL_SIZE];
	private double[] poolKeys = new double[POOL_SIZE];
	
	public BoardPart[][]btnBoardPart;
	public JButton[]btnPool;
	public JLabel[] lblPoolKey;
	public JLabel[] lblPollData;
	public	JLabel lblStage;
	public JLabel[]lblBuddies = new JLabel[4];
	public JLabel lblCenter;
	JButton btnStepFwd;
	JButton btnStepBack;
	JButton btnBestBuddiesMode;
	BtnListener listener;
	
	BufferedImage[] partImages;
	ImageIcon[] partIcons;
	
	private int offsetX;
	private int offsetY;
	private int minX;
	private int maxX;
	private int minY;
	private int maxY;

	public PuzzleStates puzzleStates;
	private boolean savePuzzleStates;
	public JTextField txtJump;
	private JLabel lblPlacingStage;
	private JButton btnPlacingStage;
	
	public static void main(String[] args) {
		String puzzleName = "4a";
		boolean dualMode = false;
		String type1 = null;
		String type2 = "";
		if (args.length > 0){
			puzzleName = args[0];
		}
		Global.prepareDirectories();
		GuiDebugger[]puzzles = new GuiDebugger[2];
		Global.puzzleName = puzzleName;
		Global.prepareDirectories();
		puzzles[0] = launchByFileName(Global.getPuzzleStatesFile(type1),puzzleName+type1);
		if (dualMode){
			puzzles[1] = launchByFileName(Global.getPuzzleStatesFile(type2),puzzleName+type2);
			MCP = MasterControlPanel.launch(puzzles);
		}
	}
	
	public static GuiDebugger launchByFileName(String fileName,String _title){
		PuzzleStates pst = PuzzleStates.load(fileName);
		GuiDebugger guiDebug = new GuiDebugger(pst.partSize, pst.xParts, pst.yParts, null,_title, false);
		guiDebug.puzzleStates = pst;
		guiDebug.partIcons = guiDebug.puzzleStates.partIcons;
		guiDebug.launch();
		guiDebug.VPD.launch();
		return guiDebug;
	}
	
	public GuiDebugger(int _partSize, int xParts, int yParts, BufferedImage[] _partImages,String _title, boolean _savePuzzleStates) {
		savePuzzleStates = _savePuzzleStates;
		if (_partSize !=0){		//Don't use default values, use the ones that were given
			partSize = _partSize;
			numParts = xParts*yParts;
			if(partSize > maxPartSize){
				partImages = new BufferedImage[numParts];
				for (int i = 0; i < partImages.length; i++){
					partImages[i] = Scalr.resize(_partImages[i], maxPartSize, maxPartSize);
				}
			}else{
				partImages = _partImages;
			}
			boardPartsX = xParts + spareRoom;
			boardPartsY = yParts + spareRoom;
		}
		offsetX = boardPartsX/2;
		offsetY = boardPartsY/2;
		minX = offsetX;
		maxX = offsetX;
		minY = offsetY;
		maxY = offsetY;
		
		partSize = Math.min(partSize, maxPartSize);
		poolPanelX = 2*partSize;
		poolPanelY = POOL_SIZE*(partSize+space) + LABEL_SIZE;
		controlPanelX = 110;
		controlPanelY = 800;
		boardPanelX = boardPartsX * partSize;
		boardPanelY = boardPartsY * partSize + LABEL_SIZE;

		if (savePuzzleStates){
			puzzleStates = new PuzzleStates(partSize, xParts, yParts, POOL_SIZE);
		}
		if (_title!=null){
			title = _title;
		}
		VPD = new VisualPoolData(title);
		listener = new BtnListener();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(0, 0, windowSizeX, windowSizeY);
		
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());
        contentPane.setComponentOrientation(java.awt.ComponentOrientation.LEFT_TO_RIGHT);
        setContentPane(contentPane);
        
		poolPanel = new JPanel();
		poolPanel.setPreferredSize(new Dimension(poolPanelX,poolPanelY));
		poolPanel.setLayout(null);
		poolPanel.setName("Pool");

		JLabel lblCandidatePool = new JLabel("Candidate Pool");
		lblCandidatePool.setBounds(10, 11, 130, 14);
		lblCandidatePool.setHorizontalAlignment(SwingConstants.CENTER);
		poolPanel.add(lblCandidatePool);
		btnPool = new JButton[POOL_SIZE];
		lblPoolKey = new JLabel[POOL_SIZE];
		for (int i=0; i<POOL_SIZE; i++){
			btnPool[i] = new JButton("");
			btnPool[i].setBounds(14, 30+i*(partSize+space), partSize, partSize);
			btnPool[i].setName(""+i);
			btnPool[i].addActionListener(listener);
			poolPanel.add(btnPool[i]);
			lblPoolKey[i] = new JLabel("");
			lblPoolKey[i].setBounds(100, 30+i*(partSize+space), partSize, partSize);
			poolPanel.add(lblPoolKey[i]);
		}

		scrollPoolPane = new JScrollPane(poolPanel,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
//		scrollPoolPane.setBounds(controlPanelX,0,poolPanelX,windowSizeY);
		scrollPoolPane.setPreferredSize(new Dimension(poolPanelX,windowSizeY));
		scrollPoolPane.setMaximumSize((new Dimension(poolPanelX,poolPanelY)));

		contentPane.add(scrollPoolPane,BorderLayout.LINE_END);
		
		boardPanel = new JPanel();
		boardPanel.setPreferredSize(new Dimension(boardPanelX,boardPanelY));
		boardPanel.setLayout(null);
		boardPanel.setName("Board");
		
		JLabel lblPuzzleBoard = new JLabel(title);
		lblPuzzleBoard.setHorizontalAlignment(SwingConstants.CENTER);
		lblPuzzleBoard.setBounds(277, 5, 724, 14);
		boardPanel.add(lblPuzzleBoard);
		btnBoardPart = new BoardPart[boardPartsX][boardPartsY];
		for (int x=0; x<boardPartsX; x++){
			for (int y=0; y<boardPartsY; y++){
				btnBoardPart[x][y] = new BoardPart();
				btnBoardPart[x][y].setBounds(x*(partSize-shrinkPartSize), 20+y*partSize, partSize-shrinkPartSize, partSize);
				btnBoardPart[x][y].setName(x+","+y);
				btnBoardPart[x][y].addActionListener(listener);
				btnBoardPart[x][y].setIconTextGap(0);
				boardPanel.add(btnBoardPart[x][y]);
			}
		}
		scrollBoardPane = new JScrollPane(boardPanel,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollBoardPane.setMaximumSize(new Dimension(boardPanelX,boardPanelY));
		contentPane.add(scrollBoardPane,BorderLayout.CENTER);
		
		for (int i = 0; i < poolParts.length; i++){
			poolParts[i] = new PoolEntry();
		}
		if (_savePuzzleStates){
			partIcons = new ImageIcon[numParts];
			for (int i=0; i<numParts; i++){
				if (partImages[i] == null){
					String fileName = null;
					try {
						fileName = Global.pathToParts+Global.puzzleName+i+".png";
						partImages[i] = ImageIO.read(new File(fileName));
					} catch (IOException e) {
						e.printStackTrace();
						System.out.println("Couldn't find the file "+ fileName);
					}
				}
				partIcons[i] = new ImageIcon(partImages[i]);
			}
			puzzleStates.partIcons = partIcons;
		}
		controlPanel = new JPanel();
		controlPanel.setBounds(0, 0, 110, 800);
		controlPanel.setPreferredSize(new Dimension(controlPanelX,controlPanelY));
		controlPanel.setLayout(null);
		contentPane.add(controlPanel,BorderLayout.LINE_START);
		
		JLabel lblNewLabel = new JLabel("Controls");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setBounds(10, 11, 89, 14);
		controlPanel.add(lblNewLabel);
		
		JButton btnCentralize = new JButton("Centralize");
		btnCentralize.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				centralizeBoard();
			}
		});
		btnCentralize.setBounds(5, 692, 100, 23);
		controlPanel.add(btnCentralize);
		
		btnStepFwd = new JButton("Forward");
		btnStepFwd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				forward();
			}
		});
		btnStepFwd.setBounds(5, 120, 100, 23);
		controlPanel.add(btnStepFwd);
		
		btnStepBack = new JButton("Backward");
		btnStepBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				backward();
			}
		});
		btnStepBack.setBounds(5, 154, 100, 23);
		controlPanel.add(btnStepBack);
		
		lblStage = new JLabel("Stage 0");
		lblStage.setHorizontalAlignment(SwingConstants.CENTER);
		lblStage.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblStage.setBounds(0, 86, 99, 23);
		controlPanel.add(lblStage);
		
		txtJump = new JTextField();
		txtJump.setHorizontalAlignment(SwingConstants.CENTER);
		txtJump.setText("0");
		txtJump.setBounds(30, 191, 47, 20);
		controlPanel.add(txtJump);
		txtJump.setColumns(10);
		
		JButton btnJump = new JButton("Jump");
		btnJump.setBackground(BUTTON);
		btnJump.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jump();
			}
		});
		btnJump.setBounds(5, 216, 100, 23);
		controlPanel.add(btnJump);
		
		JButton btnMistake = new JButton("Mistake");
		btnMistake.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				jumpToMistake();
			}
		});
		btnMistake.setBounds(5, 246, 100, 23);
		controlPanel.add(btnMistake);
		
		lblCenter = new JLabel("0");
		lblCenter.setHorizontalAlignment(SwingConstants.CENTER);
		lblCenter.setForeground(Color.BLACK);
		lblCenter.setBackground(Color.LIGHT_GRAY);
		lblCenter.setBounds(35, 330, 30, 30);
		controlPanel.add(lblCenter);
		createBuddyLabels(lblCenter);
		
		JButton btnSync = new JButton("Sync");
		btnSync.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (MCP != null){
					MCP.allJump(""+(puzzleStates.currentStage));
				}
			}
		});
		btnSync.setBounds(5, 52, 100, 23);
		controlPanel.add(btnSync);
		
		lblPlacingStage = new JLabel("Placed:");
		lblPlacingStage.setHorizontalAlignment(SwingConstants.LEFT);
		lblPlacingStage.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblPlacingStage.setBounds(1, 432, 72, 14);
		controlPanel.add(lblPlacingStage);
		
		JButton btnPoolData = new JButton("Pool Data");
		btnPoolData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				VPD.setVisible(!VPD.isVisible());
			}
		});
		btnPoolData.setBounds(5, 457, 100, 23);
		controlPanel.add(btnPoolData);
		
		btnPlacingStage = new JButton("0");
		btnPlacingStage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				txtJump.setText(btnPlacingStage.getText());
				jump();
			}
		});
		btnPlacingStage.setBounds(44, 432, 65, 17);
		controlPanel.add(btnPlacingStage);
		
		btnBestBuddiesMode = new JButton("Initial BB");
		btnBestBuddiesMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (currentStageForBuddies){
					btnBestBuddiesMode.setText("Initial BB");
				}else{
					btnBestBuddiesMode.setText("Current BB");
				}
				currentStageForBuddies = !currentStageForBuddies;	
				setBuddieLables(Integer.parseInt(lblCenter.getText()),lblCenter, lblBuddies);
				setBuddieLables(Integer.parseInt(VPD.lblCenter.getText()),VPD.lblCenter, VPD.lblBuddies);
			}
		});
		btnBestBuddiesMode.setBounds(5, 398, 100, 23);
		controlPanel.add(btnBestBuddiesMode);
		
	}
	public void createBuddyLabels(JLabel center){
		int lblCenterX,lblCenterY,lblCenterSizeX,lblCenterSizeY;
		lblCenterX = center.getX();
		lblCenterY = center.getY();
		lblCenterSizeX = center.getWidth();
		lblCenterSizeY = center.getHeight();
		lblBuddies[0] = initLabel("U",lblCenterX, lblCenterY-lblCenterSizeY, lblCenterSizeX, lblCenterSizeY);
		lblBuddies[1] = initLabel("D",lblCenterX, lblCenterY+lblCenterSizeY, lblCenterSizeX, lblCenterSizeY);
		lblBuddies[2] = initLabel("L",lblCenterX-lblCenterSizeX, lblCenterY, lblCenterSizeX, lblCenterSizeY);
		lblBuddies[3] = initLabel("R",lblCenterX+lblCenterSizeX, lblCenterY, lblCenterSizeX, lblCenterSizeY);
	}
	public JLabel initLabel(String text,int x, int y, int sizeX, int sizeY){
		JLabel label = new JLabel(text);
		label.setBounds(x, y, sizeX, sizeY);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setForeground(Color.BLACK);
		label.setBackground(Color.LIGHT_GRAY);
		controlPanel.add(label);
		return label;
	}
	
	public void setBoardPartContent(int partNum,int coordX, int coordY, boolean savePrevious){
		btnBoardPart[coordX][coordY].setContent(partNum,savePrevious);
		if (partNum==-1){
			btnBoardPart[coordX][coordY].setIcon(null);
			btnBoardPart[coordX][coordY].setText("");
		}
		else{
			btnBoardPart[coordX][coordY].setIcon(partIcons[partNum]);//Global.pathToParts+Global.puzzleName+partNum+".png"));
			btnBoardPart[coordX][coordY].setText(""+partNum);
		}
	}
	
	public void step (int partPlaced, int x, int y){
		int coordX = x + offsetX;
		int coordY = y + offsetY;
		minX = Math.min(minX, coordX);
		minY = Math.min(minY, coordY);
		maxX = Math.max(maxX, coordX);
		maxY = Math.max(maxY, coordY);
		setBoardPartContent(partPlaced,coordX,coordY,true);
		if ((minX == 0 && maxX < boardPartsX-1) || (minX>0 && maxX==boardPartsX-1)
			||(minY == 0 && maxY < boardPartsY-1) || (minY>0 && maxY==boardPartsY-1)){
			centralizeBoard();
		}
		if (savePuzzleStates){
			puzzleStates.partsPlaced[puzzleStates.currentStage][0] = partPlaced;
			puzzleStates.partsPlaced[puzzleStates.currentStage][1] = x;
			puzzleStates.partsPlaced[puzzleStates.currentStage][2] = y;
			puzzleStates.placingStages[partPlaced] = puzzleStates.currentStage;
			puzzleStates.currentStage = puzzleStates.currentStage + 1;
		}
	}
	
	public void removePart (int x, int y){
		int coordX = x + offsetX;
		int coordY = y + offsetY;
		setBoardPartContent(-1,coordX,coordY,false);
	}
	
	public void jumpToMistake(){
		txtJump.setText(""+ puzzleStates.mistakeStage);
		jump();
	}
	
	public void jump(){
		int targetStage = Integer.parseInt(txtJump.getText());
		targetStage = Math.min(numParts-1,targetStage);
		targetStage = Math.max(0,targetStage);
		while (targetStage > puzzleStates.currentStage){
			forward();
		}
		while(targetStage < puzzleStates.currentStage){
			backward();
		}
	}
	public void forward(){
		int stage = puzzleStates.currentStage+1;
		if (stage == numParts){return;}
		lblStage.setText("Stage "+stage);
		step(puzzleStates.partsPlaced[stage][0],puzzleStates.partsPlaced[stage][1],puzzleStates.partsPlaced[stage][2]);
		loadCandidatePool(stage);
		puzzleStates.currentStage = stage;
		if (puzzleStates.currentStage == puzzleStates.mistakeStage){
			mistake(false); //undo = false
		}
	}
	public void backward(){
		int stage = puzzleStates.currentStage - 1;
		if (stage == puzzleStates.mistakeStage-1){
			mistake(true);
		}
		if (stage < 0){return;}
		lblStage.setText("Stage "+(stage));
		int[] lastPart = puzzleStates.partsPlaced[stage+1];
		removePart(lastPart[1],lastPart[2]);
		loadCandidatePool(stage);
		puzzleStates.currentStage = stage;
	}
	
	public void centralizeBoard(){
		int shiftX = ((boardPartsX - 1 - maxX) - minX)/2;
		int shiftY = ((boardPartsY - 1 - maxY) - minY)/2;
		for (int x=0; x<boardPartsX; x++){
			for (int y=0; y<boardPartsY; y++){
				setBoardPartContent(-1,x,y,true);
			}
		}
		int startX = Math.max(shiftX, 0);
		int endX = startX + boardPartsX - Math.abs(shiftX);
		int startY = Math.max(shiftY, 0);
		int endY = startY + boardPartsY - Math.abs(shiftY);
		for (int x=startX; x<endX; x++){
			for (int y=startY; y<endY; y++){
				int tmp = btnBoardPart[x-shiftX][y-shiftY].getContent(false);
				setBoardPartContent(tmp,x,y,false);
			}
		}
		minX = minX + shiftX;
		minY = minY + shiftY;
		maxX = maxX + shiftX;
		maxY = maxY + shiftY;
		offsetX = offsetX + shiftX;
		offsetY = offsetY + shiftY;
	}
	public void updateCandidatePool(Iterator<PoolEntry> partsIter, Iterator<Double> keysIter,int[][]_bestBuddies){
		if (puzzleStates.currentStage == 0){return;}
		puzzleStates.copyBestBuddies(_bestBuddies);
		for (int i=0; i<POOL_SIZE; i++){
			if(partsIter.hasNext()){
				PoolEntry currentEntry = partsIter.next();
				poolParts[i] = currentEntry.copy();
				poolKeys[i] = keysIter.next();	
				btnPool[i].setIcon(partIcons[poolParts[i].nextA[0]]);
				double key = Global.roundDouble(poolKeys[i], SIGNIFICANT_DIGITS);
				lblPoolKey[i].setText("" + key);
			}else{
				poolParts[i].nextA[0] = -1;
				btnPool[i].setIcon(null);
				lblPoolKey[i].setText("");
			}
			if (savePuzzleStates){
				puzzleStates.poolParts[puzzleStates.currentStage-1][i] = poolParts[i];
				puzzleStates.poolKeys[puzzleStates.currentStage-1][i] = poolKeys[i];
			}
		}
	}
	
	
	public void loadCandidatePool(int stage){
		poolParts = puzzleStates.poolParts[stage];
		poolKeys = puzzleStates.poolKeys[stage];
		for (int i=0; i<POOL_SIZE; i++){
			if (poolParts[i].nextA[0] == -1){
				btnPool[i].setIcon(null);
				lblPoolKey[i].setText("");
			}else{
				btnPool[i].setIcon(partIcons[poolParts[i].nextA[0]]);
				double key = Global.doubleToInt(poolKeys[i], puzzleStates.significantDigits);
				key = Global.intToDouble((int)key,puzzleStates.significantDigits);
				String text = new String();
				text = (""+key);
				lblPoolKey[i].setText(text);
			}
		}
	}
	public void mistake(boolean undo){
		Color c;
		if (undo){
			c = DEF_BACKGROUND;
			
		}else{
			c = Color.RED;
			if (savePuzzleStates){
				puzzleStates.mistakeStage = puzzleStates.currentStage-1; //CurrentStage will have already been incremeted by this point so we have to use -1
			}
		}
//		controlPanel.setBackground(c);
		poolPanel.setBackground(c);
		boardPanel.setBackground(c);
	}
	public void savePuzzleStates(){
		puzzleStates.save();
	}
	
    public void showProspectivePoolParts(int x, int y){
    	boolean found = false;
    	resetLastButton(x,y);
  	    for (int i=0; i<POOL_SIZE; i++){
  	  	    if (poolParts[i].nextA[1] + offsetX == x && poolParts[i].nextA[2] + offsetY  == y){
  	  		    lblPoolKey[i].setForeground(Color.green);
  	  		    found = true;
  		    }else{
  			    lblPoolKey[i].setForeground(Color.black);
  		    }
  	    }
  	    if (found){
  	    	btnBoardPart[x][y].setBackground(Color.green);
  	    }else{
  	    	btnBoardPart[x][y].setBackground(BUTTON);
  	    }
    }
    public void resetLastButton(int x1, int x2){
    	JButton button;
    	if (lastButton[0] == -1){
    		button = btnPool[lastButton[1]];
    	}else{
    		button = btnBoardPart[lastButton[0]][lastButton[1]];
    	}
    	button.setBackground(BUTTON);
    	lastButton[0] = x1;
    	lastButton[1] = x2;
    }
    
    public void showPlacingLocation(int i){
    	int x = poolParts[i].nextA[1] + offsetX;
    	int y = poolParts[i].nextA[2] + offsetY;
    	resetLastButton(x,y);
    	btnBoardPart[x][y].setBackground(Color.blue);
    }
    
    public void setBuddieLables(int part,JLabel center, JLabel[]buddies){
    	int stage = 0;
    	if (currentStageForBuddies){
    		stage = puzzleStates.currentStage;
    		if (stage == -1){return;}
    	}
    	if (part==-1){return;}
    	center.setText(""+part);
    	for (Orientation or:Orientation.values()){
    		int op = or.opposite().ordinal();
    		int bestNeighbor = puzzleStates.bestBuddies[stage][part][or.ordinal()];
    		buddies[or.ordinal()].setText(""+bestNeighbor);
    		if (bestNeighbor != -1 && puzzleStates.bestBuddies[stage][bestNeighbor][op] == part){
    			buddies[or.ordinal()].setForeground(Color.green);
    		}else{
    			buddies[or.ordinal()].setForeground(Color.black);
    		}
    	}
    }
    public void setPoolData(PoolEntry entry){
//    	lblAddedBy.setText("Added by: "+data[3]+"("+data[4]+")");
//    	lblNeighbors.setText("Neighbors: "+data[5]);
//    	lblBestNeighbors.setText("Best Neighbors: "+data[6]);
//    	lblBonus.setText("Bonus: "+data[7]);
//    	lblMethod.setText("Method: "+data[8]);
    	String[][] tableMain = new String[5][4];
    	int[]bestNeighbors = new int[5];
    	String[]neighbors = new String[5];
    	double[][]conf = new double[5][2];
    	for (int row = 0; row<4; row++){
    		neighbors[row] = (entry.scoreComponents[4*row]==-1?"":""+entry.scoreComponents[4*row]);
    		bestNeighbors[row] = entry.scoreComponents[4*row+1];
    		bestNeighbors[4] = bestNeighbors[4] + bestNeighbors[row];
    		conf[row][0] = Global.intToDouble(entry.scoreComponents[4*row+2], puzzleStates.significantDigits);
    		conf[row][1] = Global.intToDouble(entry.scoreComponents[4*row+3], puzzleStates.significantDigits);
    		conf[4][0]=conf[4][0]+conf[row][0];
    		conf[4][1]=conf[4][1]+conf[row][1];
    	}
    	neighbors[4] = "("+entry.nonInclusiveNeighbors+")";
    	for (int row = 0; row<5; row++){
    		tableMain[row][0] = neighbors[row];
    		tableMain[row][1] = (neighbors[row].equals("")?"":""+bestNeighbors[row]);
    		tableMain[row][2] = (neighbors[row].equals("")?"":""+conf[row][0]);
    		tableMain[row][3] = (neighbors[row].equals("")?"":""+conf[row][1]);
    	}
    	tableMain[4][1]+= "("+entry.nonInclusiveBestNeighbors+")";
    	double bonus = Global.roundDouble(entry.bonus, puzzleStates.significantDigits);
    	double neighborFactor = Global.roundDouble(entry.neighborFactor, puzzleStates.significantDigits);
    	double neighborScore = Global.roundDouble(entry.nonInclusiveBestNeighbors * neighborFactor, puzzleStates.significantDigits);
    	double bestNeighborScore = Global.roundDouble(entry.nonInclusiveBestNeighbors * puzzleStates.POOL_KEY_BEST_NEIGHBOR_FACTOR, puzzleStates.significantDigits);
    	double totalScore = (conf[4][0] + conf[4][1])/(2*entry.numNeighbors) + neighborScore + bestNeighborScore + bonus;
    	totalScore = Global.roundDouble(totalScore, puzzleStates.significantDigits);
    	String[][]tableScore = new String[][]{{"",""+neighborFactor,""+puzzleStates.POOL_KEY_BEST_NEIGHBOR_FACTOR,""},
    									{""+bonus,""+neighborScore,""+bestNeighborScore,""+Global.roundDouble(totalScore, puzzleStates.significantDigits)}};
    	String[][]tableAux = new String[][]{{""+entry.addedBy},{""+entry.stageAdded},{""+entry.methodType}};
    	VPD.setTable(tableMain, "main");								
    	VPD.setTable(tableScore, "score");
    	VPD.setTable(tableAux, "aux");
    	double distanceFromExpected = entry.getDistanceFromExpected();
    	if (distanceFromExpected == -1){
    		VPD.lblDFE.setText("N/A");    	
    	}else{
    		VPD.lblDFE.setText(""+Global.roundDouble(distanceFromExpected, puzzleStates.significantDigits));
    	}
    }
    private class BtnListener implements ActionListener {
	      @Override
	      public void actionPerformed(ActionEvent e){
	          JButton source = (JButton)e.getSource();
	          String[] sourceName = source.getName().split(",");
	          if (sourceName.length == 2){ //board part
	         	 int x = Integer.parseInt(sourceName[0]);
	         	 int y = Integer.parseInt(sourceName[1]);
	         	 boolean getCurrentContent = true;
	         	 int content = btnBoardPart[x][y].getContent(getCurrentContent);
	         	 if (content == -1){
	         		 showProspectivePoolParts(x,y);
	         	 }else{
	         		 setBuddieLables(content,lblCenter,lblBuddies);
	         		 btnPlacingStage.setText(""+puzzleStates.placingStages[content]);
	         	 }
	          }else if(sourceName.length == 1){ //pool part
	        	  int sourceID = Integer.parseInt(sourceName[0]);
	        	  showPlacingLocation(sourceID);
	        	  setBuddieLables(poolParts[sourceID].nextA[0],VPD.lblCenter,VPD.lblBuddies);
	        	  setPoolData(poolParts[sourceID]);
	          }
   	      }  
     }
}
