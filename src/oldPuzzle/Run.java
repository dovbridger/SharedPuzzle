package oldPuzzle;


import java.awt.Container;

import javax.swing.JApplet;
import javax.swing.SwingUtilities;

public class Run extends JApplet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public void init(){
		Container a = getContentPane();
		try{ SwingUtilities.invokeAndWait(new StartGUI2(a));
		} 
		catch(Exception e){
			System.out.println(e);
			}
	}	
}
