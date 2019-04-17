package puzzle;

import java.io.File;
import java.io.IOException;
import java.lang.String;

import multi_thread.Extrapolation;
import parameters.DoubleParameter;
import parameters.IntArrayParameter;
import parameters.IntParameter;
import parameters.Parameter;
import parameters.StringParameter;
import utils.Utils;

public class Global {
	

	public static String[] puzzleNamesRaw = {
											"1"//,"2","3","4","5","6","7","8","9","10","11","12","13","14","15", "16", "17", "18", "19", "20"
											};
	//public static String[] puzzleNamesRaw = Utils.read_strings_from_file("C:\\SHARE\\met_test_names.txt");
	public final static String PUZZLE_NAME_SUFFIX = "met";
	public static final int ORIGINAL_PART_SIZE = 128;
	//Swap the diff matrix before starting to solve the puzzle with a pre-made matrix from the file TEXTUAL_DIFF_MATRIX_FILE_MODIFIED and correction method
	// Possible values: 'none', <string corresponding to a model from 'puzzle_gan' project>
	public final static String[] diff_matrix_correction_methods = {"none"};//, "CalcProbabilityModel_g44_d40_b2x_0", "CalcProbabilityModel_g44_d50_b2x_0", "CalcProbabilityModel_g44_d60_b2x_0"};
	public static boolean LOAD_BACKUP_SCRIPT = false;
	public static final String SCRIPT_NAME = "backup_script1";
	// 0-patchSize(0 means don't extrapolate), 1-numIterations, 2-burnExtent, 3-sizeOverlap (0 means use old calcDiff version), 4-patchratio
	public final static int[][]extrapolationVals = {{0,1,0,0,100}}; 
	//Instead of comparing pixel to pixel, the average of pixel blocks of this size will be compared
	public final static int[]diffBlockSizeVals = {1}; 
	//N.I.U weights for using color vs gradients in CalcDiff. relevant only for the case with overlap.
//	public final static double[]colorDiffRatioVals = {1};
	//Which norm to use when comparing pixels/pixel block averages. Original value was 2.
	public final static int[]diffNormVals = {2};	
	// L weight when comparing pixels in LAB. (A and B have weight 1).
	public final static double[]lFactorVals = {1}; 
	//N.I.U Weights for the saliency channel to be combined with the LAB comparison.
//	public final static double[] saliencyFactorVals = {0}; 
	//Use Matlab Hole Filling to modify confidence, 0-don't use, 1-use
	public final static int[] modifyConfVals = {0};		
	// 0-don't use, 1-use
	public final static int[] useImportanceVals = {0};	
	// The threshold for vetoing a placing of puzzle part.
	// If the expected geometric location is further than this value - don't place.
	public final static double[] geometricRelationsVals = {0}; 
	//Constants to control how the geometry key (for the pool) will be calculated from the inverse of the distance from expected location
	//Must be greater than 0 or it is ignored
	public final static double[] geometryKeyConstantVals = {0}; 
	public static enum ParameterNames {extrapolationVals,diffBlockSizeVals,diffNormVals,lFactorVals,
										modifyConfVals, useImportanceVals, diff_matrix_correction_methods,size;}
	public static enum FirstPieceChoice {normal,geometry,saliency;}

	public static BackupScript backupScript;
	//Final String values
	public static final String PROJECT_PATH = "C://SHARE//checkouts//puzzle_gan_data//java_artifacts//Puzzle Resources//";
	public static final String geometricRelationsFile = "_gm.txt";
	// Where the text puzzle score files will be stored
	public static final String SCORES = PROJECT_PATH + "Scores//"; 
	// Where the data needed for the GuiDebugger and puzzleGui will be stored
	public static final String PUZZLE_DATA = PROJECT_PATH + "Data//"; 
	public static final String scoresFileName = "all_scores_" + PUZZLE_NAME_SUFFIX+".txt";
	// The file that holds the diff, conf rank data etc.. at the begining.
	public static final String INIT_DATA_FILE = "_init_data.txt"; 
	// The file that holds the diff matrix in textual form that can be parsed in python
	public static final String TEXTUAL_DIFF_MATRIX_FILE = "_diff_matrix.txt"; 
	public static final String TEXTUAL_DIFF_MATRIX_FILE_MODIFIED = "_diff_matrix_modified_"; 
	// The file that holds the diff, conf rank data etc.. at the begining but after modified by hole filling in matlab
	public static final String INIT_MOD_DATA_FILE = "_init_mod_data.txt";
	//The suffix for the name of the puzzle data file that will be used by the GuiDebugger	
	public static final String PUZZLE_STATES_FILE = "_states_";	
	public static final String FILE_TYPE = ".jpg";
	// The folder name for the importance maps. Allows each puzzle to have different importance options to choose from
	public static final String IMPORTANCE_TYPE = "_importance_"; 
	public static final String INPUT_FOLDER = PROJECT_PATH+ "INPUT//";
	public static final String OUTPUT_FOLDER = PROJECT_PATH+ "OUTPUT//";
	public static final String IMPORTANCE_FOLDER = PROJECT_PATH+ "Importance//";
	public static final String SALIENCY_FOLDER = PROJECT_PATH+ "Saliency//";
	// Extrapolated parts will be stored here
	public static final String INDIVIDUAL_PARTS_FOLDER = PROJECT_PATH+ "Individual Parts//"; 
	public static final String INTERMEDIATE_OUTPUT_FOLDER = PROJECT_PATH+ "Intermediate Output//";
	public static final String NUMBERS_PATH = INTERMEDIATE_OUTPUT_FOLDER+ "Numbers//";
	public static final String PUZZLE_STATES_FOLDER = PUZZLE_DATA + "states//";
	public static final String GEOMETRIC_RELATIONS_FOLDER = PUZZLE_DATA + "GR//";
	public static final String INIT_DATA_FOLDER = PUZZLE_DATA + "Init//";

	
	//Old stuff - N.I.U
	public static final String readBurntFile=PROJECT_PATH+"";	//Load a burnt pixel configuration from file of this name (N.I.U)
	public static final String writeBurntFile=PROJECT_PATH+ "";	//Save a burnt pixel configuration to a file of this name (N.I.U)
	public static final String readOrderFile=PROJECT_PATH+ "";	//Load the order of parts from a file of this name (N.I.U)
	public static final String writeOrderFile=PROJECT_PATH+ "";	//Save the order of parts (relevant is there is a shuffle going on) to a file of this name (N.I.U)
	public static final String fill = PROJECT_PATH+ "";		//Fill individually burnt pixels from a file of this name (N.I.U)
	public static final String SIFT_MODE_SUFFIX = "_sift";
	
	//Final Boolean values
	public static boolean solve = true; //Solve the puzzle or only do all the preparation work (extrapolation,diff and conf, etc...)
	public static boolean no_matlab = true; //Will be set to true when running java code from matlab and deactivate a possibility of running matlab from java.
											//It is a bug fix and should not be touched here (should be false here)
	public static boolean siftMode = false; //Look for a puzzle image that was siftcompleted. Not declared as final just so that it can be set to false if no sift file is found
	public static boolean useGeometricRelations = false; //Not declared as final just so that it can be set to false if no gr file is found
	public static boolean number = false; //Create a numbered image of the input, Not declared as final...
	public static final boolean saveResultImage = false;
	public static final boolean savePartImages = false; //Save the parts from the extrapolation (and not just the combined image)
	public static final boolean prepare = true; //true = Look for the "prepared" file name and if it is not found create it with extrapolation
												//if false, make sure the puzzle image is in a folder with the puzzle name
	public static final boolean createExtrapolationImportance = false; //Create importance maps during the extrapolation process (based on the quality of patch matching
	public static FirstPieceChoice FIRST_PIECE_CHOICE = FirstPieceChoice.normal;	//	Not declared as final just so that it can be set to normal if no saliency/geometry is found
	public static final boolean pixellBlockCalcDiff = true; //If the overlap method is used, should we use pixel Block or LABVectors. No reason to change this to false
	public static final boolean farBuddies = false;//Mark bestBuddies that are not neighbors on the input image
	public static final boolean burnBorders = false; 
	public static final boolean beVerbose = false;	//Print which piece is being placed at each stage
	public static final boolean shuffle = false;	// Create a shuffled version of the input image
	public static final boolean reverseShuffle = false;	//Create a reverse shuffling action according to a previously saved order file ("readOrderFile") (N.I.U)
	public static final boolean fillBurnt = false;		//Fill burnt pixels according to file "fill" (N.I.U)
	public static final boolean burnByColor = false;	// If the original image has green pixels in it, those pixels will be considered burnt (N.I.U)
	public static final boolean positiveConf = false; //Use the new Confidence 
	
//	public static final boolean calcErrorBaseLine = true;	//
	public static final boolean diffBlockSliding = false;	// If the diffBlockSize is more than one, should the blocks be unique or sliding?
	public static final boolean GUI_DEBUG = true;			// Should the GUI_Debugger run in parallel to the solving algorithm and save the data for future debugging?
	public static final boolean DYNAMIC_THRESHOLD_AT_FIRST_PIECE_SELECTION = true;	//If no first piece meets Genady's criteria, use a dynamic thresholding reduce system instead of just starting with 0.
	public static final boolean MAX_CONF_IN_POOL = false;	//When adding a candidate to the pool, should its score be affected by the maximum conf he has with one of the neighbors?(rather than average?)

	//Final Number Values
	public static double NEXT_TO_PLACE_SALIENCY_FACTOR = 0;		//How much weight to give to the saliency of a piece when adding it to the pool? (N.I.U)
	public static int aveSalPower = 4;		//When calculating the average saliency of a piece, what power should it be raised to (higher means favoring pieces with higher variance)
	public static double extrapolationDistannceToWeightFactor = 0.5;	//When averaging patches in overlap mode in Extrapolation, how does the distance of the patch from the center affect
																		// its weight? Higher values lowers the affect of being close to the center.
	public static final int numAdditionalStartParts = 0;	//How many additional start parts should we attempt to add by geometric relations before the sovling begins.
	public static final int[] intermediateOutput = {-1,-1};		//From which stage to which stage should we save intermediate output images? (N.I.U since the Gui-debugger was made)
	public static double geometricRelationsStartPartThreshold = 0.3;	//In finding additional start parts with geometric relations:
																		//How far can a part's expected location in x or y be from a whole value until we need to disqualify it?
	public static double geometricRelationsStartPartMirrorThreshold =0.9;	//How high can the mirror distance be while choosing a start part by geometric relations?
	public static double geometricRelationsMirrorThreshold = 1;		//How high can the mirror distanced be while considering a vote of a placed piece
																	//Regarding the future position of a piece on the board
	public static double errorBaseLine = 0;		//if 0 - Calculate the Error base line for each puzzle, else use this value
	public static final float MAX_CONF = 20;	//If the "positiveConf" option is used, this value limits how high a conf value get.
	public static final float MOD_CONF_BASELINE = 5;	//If the "" is used, Parts with conf under this value will attempt a modification (with Matlab Hole Filling)
	public static final double MIN_MATLAB_VARIANCE = 500;	//Matlabs hole filling algorithm will only work on parts whose relevant border has at least this variance
	public static final int BEAM_SIZE = 5;	//Matlabs Hole filler will consider only this number of neighbor of highest ranking candidates for hole filling
	
	public static final int RANK_PERCENTILE = 90; // Rank Stats about the puzzle: At which percentile of all ranks do you want to take a measurement
	public static final int RANK_THRESH = 15;	// Rank Stats about the puzzle: Which percentage of ranks is under this threshold?
	public static final double POOL_KEY_NEIGHBOR_FACTOR13 = 0.3;	//Genady rewarded the score of parts that are being added to the pool according to the number of neighbors they have on the board 
																	//Multiplied by this factor (But only if there were known to be no missing pieces). What is the weight of the reward for addbuddies types 1 and 3?
	public static final double POOL_KEY_NEIGHBOR_FACTOR2 = 0.5; //What is the weight of the reward for addbuddies type 2?
	public static final double POOL_KEY_BEST_NEIGHBOR_FACTOR = 0.3; //Same kind of factor but for neighbros that are best neighbors
	public static final int SIGNIFICANT_DIGITS = 3;  //How many digits to show for scores of parts in the pool in the debugger?
	public static final int SIGNIFICANT_DIGITS_FACTOR = (int)Math.pow(10, SIGNIFICANT_DIGITS);	//The actuall factor to be multiplied by and divided by
	public static final int FIRST_PIECE_LOOP_CONSTRAINT_VALUE = 180;	//While choosing the first piece, instead of demanding the loop constraint of buddies like
																		//Genady did, I give it a numerical score and add it to the score from best buddies
	public static final int MIN_GEOMETRIC_RELATIONS_FOR_START_PART = 5;
	// To avoid overflows later on - One power less the the original max float (38)
	public static final float MAX_FLOAT = 3.4028235E37f;
	
	//Default values - not relevant to testingScript();

	public static boolean useImportance = false; 
	public static int numBurnt = 100;
	static double burnFactor = 0.5;
	
	//Initialize the parameters to equal the first choice of parameter string of values. 
	//This is used so that the parameters will be initialized if the class is accessed from somewhere else (Matlab, GuiDebugger maybe) when the puzzle solver insn't running
	public static String puzzleName = puzzleNamesRaw[0] + PUZZLE_NAME_SUFFIX;
	public static int patchSize = extrapolationVals[0][0];
	public static int numIterations = extrapolationVals[0][1];
	public static int burnExtent = extrapolationVals[0][2];
	public static int sizeOverlap = extrapolationVals[0][3];
	public static int patchRatio = extrapolationVals[0][4];
	public static int diffBlockSize = diffBlockSizeVals[0];
	public static double colorDiffRatio = 1;//colorDiffRatioVals[0];
	public static boolean modifyConf = modifyConfVals[0] != 0;	// Each time the confmatrix is calculated, should it be modified by the hole filling algorithm in Matlab?
	public static String diff_matrix_correction_method = diff_matrix_correction_methods[0];
	public static double lFactor = lFactorVals[0];
	public static int diffNorm = diffNormVals[0];
	public static double geometricRelationsThreshold = geometricRelationsVals[0];
	public static double geometryKeyConstant = geometryKeyConstantVals[0];
	
	//Fields to be set during Runtime
	public static String preparedName;
	public static String puzzleID;
	public static String puzzleSiftPreparedName;
	public static String pathToParts;
	public static String pathToImportance;
	public static String pathToIntermediateOutput;
	public static String pathToSaliency;
	public static String pathToPreparedInput;
	public static  String pathToOutput;
	public static String pathToPuzzleDataInit;
	public static String pathToPuzzleDataGR;
	public static String pathToPuzzleDataStates;
	public static double SALIENCY_FACTOR;
	public static boolean useGeometryInPool;
	public static float neighborResult;
	
	public static void main(String[] args) throws Exception {
		//If this is a resume of a previously started run, load the info of the previous run
		//from the backup file named "SCRIPT_NAME"
		if (LOAD_BACKUP_SCRIPT){
			backupScript = BackupScript.loadObject(SCRIPT_NAME);
			backupScript.recoverIndexes();	
		//Else - set the parameters as specified above in this module
		}else{
			Parameter parameters[] = new Parameter[ParameterNames.size.ordinal()];
			parameters[ParameterNames.extrapolationVals.ordinal()] = new IntArrayParameter(ParameterNames.extrapolationVals,extrapolationVals);
			parameters[ParameterNames.diffBlockSizeVals.ordinal()] = new IntParameter(ParameterNames.diffBlockSizeVals,diffBlockSizeVals);
//			parameters[ParameterNames.colorDiffRatioVals.ordinal()] = new DoubleParameter(ParameterNames.colorDiffRatioVals,colorDiffRatioVals);
			parameters[ParameterNames.modifyConfVals.ordinal()] = new IntParameter(ParameterNames.modifyConfVals, modifyConfVals);
			parameters[ParameterNames.diffNormVals.ordinal()] = new IntParameter(ParameterNames.diffNormVals,diffNormVals);
			parameters[ParameterNames.lFactorVals.ordinal()] = new DoubleParameter(ParameterNames.lFactorVals,lFactorVals);
//			parameters[ParameterNames.saliencyFactorVals.ordinal()] = new DoubleParameter(ParameterNames.saliencyFactorVals,saliencyFactorVals);
			parameters[ParameterNames.useImportanceVals.ordinal()] = new IntParameter(ParameterNames.useImportanceVals,useImportanceVals);
			parameters[ParameterNames.diff_matrix_correction_methods.ordinal()] = new StringParameter(ParameterNames.diff_matrix_correction_methods,diff_matrix_correction_methods);
			backupScript = new BackupScript(parameters);
		}

		System.out.println("Testing Script Parameters:");
		for (int i = 0; i < ParameterNames.size.ordinal(); i++){
			System.out.println(backupScript.parameters[i].name + " index = "+backupScript.parameters[i].recoverIndex + " of " + (backupScript.parameters[i].numValues-1));
		}
		System.out.println("Image: " + backupScript.recoverImageNum + " of " + (Global.puzzleNamesRaw.length -1));
		Start.testingScript();
	}
	
	//Create a single directory if it doesn't exist
	public static void createDirectory(String path){
		File directory = new File(path);
		if (!directory.exists()){
			directory.mkdir();
		}
	}
	
	//Create all directories a long a given path
	public static void createDirectory(String[]fullPath){ 
		String currentPath = "";
		for (int i=0; i<fullPath.length; i++){
			currentPath = currentPath + fullPath[i]+"//";
			createDirectory(currentPath);
		}
	}
	//Create the directories for the current puzzle, and set the path variables to the values relevant for the current puzzle that is about to be solved
	public static void prepareDirectories(){
		if (Global.prepare){
			Global.preparedName = getPreparedName(Global.puzzleName, Global.patchSize, Global.numIterations, Global.sizeOverlap, Global.burnExtent, Global.patchRatio);
		}else{
			Global.preparedName  = Global.puzzleName;
		}
		puzzleID = getPuzzleID();
		pathToOutput = Global.OUTPUT_FOLDER+Global.puzzleName +"//";
		pathToParts = Global.INDIVIDUAL_PARTS_FOLDER + Global.puzzleName + "//"+Global.preparedName+"//";
		pathToImportance = Global.IMPORTANCE_FOLDER+ Global.puzzleName + "//" +Global.preparedName +"//"+Global.IMPORTANCE_TYPE+"//";
		pathToIntermediateOutput = Global.INTERMEDIATE_OUTPUT_FOLDER + Global.puzzleName + "//" +Global.preparedName +"//";
		pathToSaliency = Global.SALIENCY_FOLDER + Global.puzzleName + "//" +Global.preparedName +"//";
		pathToPreparedInput = Global.INPUT_FOLDER + "//"+Global.puzzleName+"//";
		pathToPuzzleDataGR = GEOMETRIC_RELATIONS_FOLDER + puzzleName + "_OPS" + ORIGINAL_PART_SIZE;
		pathToPuzzleDataInit = INIT_DATA_FOLDER + puzzleID;
		pathToPuzzleDataStates = PUZZLE_STATES_FOLDER + puzzleID;
		createDirectory(Global.pathToOutput.split("//"));
		createDirectory(Global.pathToParts.split("//"));
		createDirectory(Global.pathToImportance.split("//"));
		createDirectory(Global.pathToIntermediateOutput.split("//"));
		createDirectory(Global.pathToSaliency.split("//"));
		createDirectory(Global.pathToPreparedInput.split("//"));
		createDirectory(Global.GEOMETRIC_RELATIONS_FOLDER.split("//"));
		createDirectory(Global.INIT_DATA_FOLDER.split("//"));
		createDirectory(Global.PUZZLE_STATES_FOLDER.split("//"));
	}
	
	//Get the "preparedName" format according to values of your choice
	public static String getPreparedName(String puzzleName, int patchSize, int numIterations, int sizeOverlap, int burnExtent, int patchRatio){
		return "name="+puzzleName+"-part_size="+ORIGINAL_PART_SIZE+"-"+"burn_extent="+burnExtent;
	}
	
	//Set the "preparedName" according the the current values of the puzzle extrapolation parameters
	public static String setPreparedName(){
		Global.preparedName = getPreparedName(puzzleName, patchSize, numIterations, sizeOverlap, burnExtent, patchRatio);
		return Global.preparedName;
	}
	
	//Get the puzzle ID based on the "preparedName" and the other parameters that determine how the puzzle will be solved
	public static String getPuzzleID(){
		String solveParameters = "_DBS"+diffBlockSize  + "_DN" + diffNorm + "_LF" + lFactor + "_UI" + boolToInt(useImportance) 
		+ "_MC" + boolToInt(modifyConf);
		return preparedName + solveParameters;
	}
	
	public static int boolToInt(boolean bool){
		if (bool){
			return 1;
		}else{
			return 0;
		}
	}
	//If you want to automatically add "true" or "false" to the Puzzle States File to note if the modifyConf option was selected
	//Call this function will null, otherwise you can add a different suffix if you want
	public static String getPuzzleStatesFile(String options){
		if (options == null){
			options = "modConf_" + modifyConf + "_GRVal_" + geometricRelationsThreshold + "_GRKC_" + geometryKeyConstant;
		}
		return pathToPuzzleDataStates+PUZZLE_STATES_FILE+options+".txt";
	}
	public static int doubleToInt(double x, int numDigits){
		int keyDigits;
		if (numDigits > 0){
			keyDigits = (int)Math.pow(10, numDigits);
		}else{
			keyDigits = SIGNIFICANT_DIGITS_FACTOR;
		}
		int result = (int)Math.round(keyDigits*x);
		return result;
	}
	public static double intToDouble(int x, int numDigits){
		int keyDigits;
		if (numDigits > 0){
			keyDigits = (int)Math.pow(10, numDigits);
		}else{
			keyDigits = SIGNIFICANT_DIGITS_FACTOR;
		}
		double result = x/(double)keyDigits;
		return result;
	}
	
	public static double roundDouble(double x, int numDigits){
		return intToDouble(doubleToInt(x, numDigits), numDigits);
	}
}
