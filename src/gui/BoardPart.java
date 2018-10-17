package gui;

import javax.swing.JButton;

public class BoardPart extends JButton {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int content;
	int previousContent;
	
	public BoardPart(){
		content = -1;
		previousContent = -1;
	}
	public void setContent(int _content, boolean savePrevious){
		if (savePrevious){
			previousContent = content;
		}
		content = _content;
	}
	public int getContent(boolean current){
		if (current){
			return content;
		}else{
			return previousContent;
		}
	}
}

