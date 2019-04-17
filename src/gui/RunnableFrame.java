package gui;

import java.awt.EventQueue;

import javax.swing.JFrame;

public class RunnableFrame extends JFrame implements Runnable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void run() {
		try {
			this.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void launch(){
		EventQueue.invokeLater(this);
	}

}
