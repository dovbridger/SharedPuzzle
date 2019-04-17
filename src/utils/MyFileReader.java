package utils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class MyFileReader {
	private FileReader m_file_reader;
	
	public MyFileReader(String file_name) throws FileNotFoundException{
		m_file_reader = new FileReader(file_name);
	}
	
	
	public String read() throws IOException{
		String file_content = "";
		int i;
		while ((i = m_file_reader.read()) != -1){
			file_content = file_content + (char)i;
		}
		return file_content;
	}
	
	public void close(){
		try {
			m_file_reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
