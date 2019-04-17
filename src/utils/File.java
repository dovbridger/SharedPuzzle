package utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class File {
	private FileWriter m_fw;
	private BufferedWriter m_bw;
	
	public File(String file_name) throws IOException{
		m_fw = new FileWriter(file_name);
		m_bw = new BufferedWriter(m_fw);
	}
	
	public void close(){
		try {

			if (m_bw != null)
				m_bw.close();

			if (m_fw != null)
				m_fw.close();

		} catch (IOException ex) {

			ex.printStackTrace();

		}
	}
	
	public void write(String content){
		try {
			m_bw.write(content);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
