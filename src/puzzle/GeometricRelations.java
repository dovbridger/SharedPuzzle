package puzzle;

import java.io.IOException;
import java.io.Serializable;

//Manages geometric relations between pieces that were calculated with SIFT in Matlab
public class GeometricRelations implements Serializable {

	private static final long serialVersionUID = 1L;
	//The array holding the relations: 0-from part, 1-to part, 2-source image, 3-coordinate(x/y)
	public double[][][][]data;
	// The value representing that there is no information available
	public double nullValue;
	public int numSources;
	public int mostPopularPart;
	public int[]numOfRelations;
	
	public GeometricRelations(double[][][][]_data, int[]_numOfRelations, int _mostPopularPart, double _nullValue, int _numSources, String puzzleName){
		data = _data;
		nullValue = _nullValue;
		numSources = _numSources;
		mostPopularPart = _mostPopularPart;
		numOfRelations = _numOfRelations;
		Global.puzzleName = puzzleName;
		Global.prepareDirectories();
		try {
			CopyObject.write(this, Global.pathToPuzzleDataGR + Global.geometricRelationsFile);
		} catch (IOException e) {e.printStackTrace();}
	}
	
	public static GeometricRelations loadObject(String fileName ) throws ClassNotFoundException, IOException{
		GeometricRelations loadedObject =  (GeometricRelations)CopyObject.read(fileName);
		return loadedObject;
	}
	
	//All the geometric relations data comes from Matlab in the unit of Pixels.
	//This method normalizes the geometric distances to the part size of the current puzzle
	public void normalizeToPartSize(){
		for (int partFrom = 0; partFrom<data.length; partFrom++){
			for (int partTo = partFrom+1; partTo<data[0].length; partTo++){
				for (int sourceNum = 0; sourceNum<numSources; sourceNum++){
					if (data[partFrom][partTo][sourceNum][0] != nullValue){
						for (int coordinate = 0; coordinate<2; coordinate++){
							data[partFrom][partTo][sourceNum][coordinate] = data[partFrom][partTo][sourceNum][coordinate] /Global.ORIGINAL_PART_SIZE;
							data[partTo][partFrom][sourceNum][coordinate] = data[partTo][partFrom][sourceNum][coordinate] /Global.ORIGINAL_PART_SIZE;
						}
						double diffX = data[partFrom][partTo][sourceNum][0]+data[partTo][partFrom][sourceNum][0];
						double diffY = data[partFrom][partTo][sourceNum][1]+data[partTo][partFrom][sourceNum][1];
						data[partFrom][partTo][sourceNum][2] = Math.sqrt(diffX*diffX + diffY*diffY);
						data[partTo][partFrom][sourceNum][2] = data[partFrom][partTo][sourceNum][2];
					}
				}
			}
		}
	}
	
	//Loads the geometric relations data from the file
	//If it doens't exit the method takes care of turning off the solving options
	//that use geometric relaitons
	public static GeometricRelations loadGeometricRelations(){
		GeometricRelations geometricRelations = null;
		Global.useGeometryInPool = false;
		if (Global.useGeometricRelations) {
			try {
				geometricRelations = loadObject(Global.pathToPuzzleDataGR + Global.geometricRelationsFile);
				geometricRelations.normalizeToPartSize();
				Global.useGeometryInPool = Global.geometryKeyConstant > 0;
			} catch (IOException | ClassNotFoundException e) {
				System.out.println("No Geometric Relations file by the name of "
						+ Global.pathToPreparedInput + Global.preparedName
						+ Global.geometricRelationsFile);
				System.out.println("useGeometricRelations set to false");
				System.out.println("useGeometryInPool set to false");
				Global.useGeometricRelations = false;
				e.printStackTrace();
			}
		}
		
		if (!Global.useGeometricRelations && Global.FIRST_PIECE_CHOICE == Global.FirstPieceChoice.geometry){
			System.out.println("useGeometricRelations is false, setting FIRST_PIECE_CHOICE to normal");
			Global.FIRST_PIECE_CHOICE = Global.FirstPieceChoice.normal;
		}
		return geometricRelations;
	}
}
