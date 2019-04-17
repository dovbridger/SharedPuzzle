package utils;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.json.*;
import java.nio.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import flexjson.JSONDeserializer;

public class Utils {
	private static final String START_MAGIC = "$";
	private static final String DIM_1_MAGIC = "&";
	private static final String DIM_2_MAGIC = "#";
	private static final String COMMA_MAGIC = ",";
	
	public static void save_float_matrix3d_to_file(float[][][]matrix, String file_name){
		File f = null;
		try {
			f = new File(file_name);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		f.write(START_MAGIC);
		for (int i = 0; i < matrix.length; i++){
			for (int j = 0; j < matrix[0].length; j++){
				for (int k = 0; k < matrix[0][0].length; k++){
					if (k != 0){
						f.write(COMMA_MAGIC);
					}
					f.write(Float.toString(matrix[i][j][k]));
				}
				f.write(DIM_2_MAGIC);
			}
			f.write(DIM_1_MAGIC);
		}
		f.close();
	}
	
	public static float[][][] load_float_matrix3d_from_json(String file_name){
		String file_string = "";
		try {
			file_string = new String(Files.readAllBytes(Paths.get(file_name)));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayList<ArrayList<ArrayList>> matrix = (ArrayList<ArrayList<ArrayList>>)(new JSONDeserializer().deserialize(file_string));
		float[][][] result = new float[matrix.size()][matrix.get(0).size()][matrix.get(0).get(0).size()];
		for (int i = 0; i < result.length; i++){
			for (int j= 0; j < result[0].length; j++){
				for (int k = 0; k < result[0][0].length; k++){
					result[i][j][k] = (float)(((Double) (matrix.get(i).get(j).get(k))).doubleValue());
				}
			}
		}
		return result;
	}
	
	public static void dump_float_matrix3d_to_json(float[][][]matrix3d, String file_name){
		String json_string = new flexjson.JSONSerializer().serialize(matrix3d);
		File f;
		try {
			f = new File(file_name);
			f.write(json_string);
			f.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static String[] read_strings_from_file(String file_name){
		try {
			String file_content = new String(Files.readAllBytes(Paths.get(file_name)));
			return file_content.split(";");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static float[][][] read_float_matrix3d_from_file(String file_name){
		String file_string = "";
		try {
			MyFileReader f = new MyFileReader(file_name);
			file_string = f.read();
			f.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		if (file_string.indexOf(START_MAGIC) != 0){
			System.out.println("read_float_matrix3d_from_file called with wrong file type");
			return null;
		}
		file_string = file_string.substring(START_MAGIC.length(), file_string.length() - 1);
		String[] first_dim_parse = file_string.split(DIM_1_MAGIC);
		String[][] second_dim_parse = new String[first_dim_parse.length][];
		for (int i = 0; i < first_dim_parse.length; i++){
			second_dim_parse[i] = first_dim_parse[i].split(DIM_2_MAGIC);
		}
		int third_dim_size = second_dim_parse[0][0].split(COMMA_MAGIC).length;
		float[][][] result = new float[second_dim_parse.length][second_dim_parse[0].length][third_dim_size];
		for (int i = 0; i < second_dim_parse.length; i++){
			for (int j= 0; j < second_dim_parse[0].length; j++){
				String[] third_dim = second_dim_parse[i][j].split(COMMA_MAGIC);
				for (int k = 0; k < third_dim_size; k++){
					result[i][j][k] = Float.parseFloat(third_dim[k]);
				}
				
			}
		}
		return result;
	}
	public static void test_equal(float[][][] m1, float[][][] m2){
		boolean equal = true;
		for (int i = 0; i < m1.length; i++){
			for (int j= 0; j < m1[0].length; j++){
				for (int k = 0; k < m1[0][0].length; k++){
					if (m1[i][j][k] != m2[i][j][k]) {
						System.out.println("Wrong value at index " + i + "," + j + "," + k);
						equal = false;
					}
				}
				
			}
		}
		System.out.println("Result: " + equal);
	}
	
	public static void main(String[]args) throws IOException{
		//float [][][]a = {{{1.5f,2.5f},{3.5f,4.5f},{5.5f,6.5f}}, {{7.5f,8.5f},{9.5f,10.5f},{11.5f,12.5f}}, {{13.5f,14.5f},{15.5f,16.5f},{17.5f,18.5f}}, {{19.5f,20.5f},{21.5f,22.5f},{23.5f,24.5f}}};
		String file_name_r = "C:\\SHARE\\vgg_19_12-11b_r.json";
		String file_name_w = "C:\\SHARE\\vgg_19_12-11b_w.json";
	//	dump_float_matrix3d_to_json(a, file_name);
		float[][][] a = load_float_matrix3d_from_json(file_name_r);
		//dump_float_matrix3d_to_json(a, file_name_w);
		float[][][] b = load_float_matrix3d_from_json("C:\\SHARE\\py.json");
		test_equal(a,b);
	}
	

}
