package oldPuzzle;

//import java.awt.BorderLayout;
//import java.awt.Color;
//import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
//import java.awt.GridLayout;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.io.FilenameFilter;
//import java.io.IOException;
//import java.util.ArrayList;
//
//import javax.imageio.ImageIO;
//import javax.swing.ImageIcon;
//import javax.swing.JButton;
//import javax.swing.JComboBox;
////import javax.swing.JFrame;
import javax.swing.JLabel;
//import javax.swing.JPanel;
//import javax.swing.JTabbedPane;
//import javax.swing.JToggleButton;
//import javax.swing.event.ChangeEvent;
//import javax.swing.event.ChangeListener;

public class StartGUI2 implements Runnable{

	Container contentPane;
	JLabel jlab;



//	JPanel openPanel;
//	JPanel previewPanel;
//	JPanel twoColPanel;
//	JPanel runPanel;
//	JPanel inputPanel;
//	JLabel startLabel;
//	JLabel inputLabel;
//	JLabel preImLab;
//	JComboBox<String> picList;
//	JComboBox<String> elimOpList;
//	GridLayout openLayout;
//	FlowLayout twoColLayout;
//	String[] elimOp = {"<html><i><font size='3';color='green''>Missing Part Method: None</font><i></html>",
//			"<html><font size='3';color='red''>Missing Part Method: Uniform</font></html>",
//			"<html><font size='3';color='red''>Missing Part Method: Vertical Strips</font></html>",
//			"<html><font size='3';color='red''>Missing Part Method: Horizontal Strips</font></html>",
//	"<html><font size='3';color='red''>Missing Part Method: Corners</font></html>"};
//	JButton startB;
//	JButton shuffleB;
//	JTabbedPane tabbedPane;
//	JToggleButton anim;
//	JToggleButton visible;
//	JToggleButton useSize;
//	GeneratePuzzle gp;
//	int type;


	public StartGUI2() {

	}
	
	public StartGUI2(Container _contentPane) {
		contentPane=_contentPane;
	}
	
	@Override
	public void run() {
		
//		File dir = new File("./");
//		File files1[]=dir.listFiles(new FilenameFilter() {
//			public boolean accept(File dir, String filename)
//			{ return filename.endsWith(".png");
//			}
//		} );
//		File files2[]=dir.listFiles(new FilenameFilter() {
//			public boolean accept(File dir, String filename)
//			{ return filename.endsWith(".jpg");
//			}
//		} );
//		ArrayList<String> list=new ArrayList<String>();
//		for(File file:files1){
//			list.add(file.getName());
//		}
//		for(File file:files2){
//			list.add(file.getName());
//		}


		

//		tabbedPane = new JTabbedPane();
		
		
		
//		openPanel=new JPanel();
//		runPanel=new JPanel();
//		inputPanel=new JPanel();
//		inputLabel=new JLabel();
//		twoColPanel=new JPanel();
//		previewPanel=new JPanel();
//		twoColLayout=new FlowLayout();
//		twoColPanel.setLayout(twoColLayout);
//		preImLab=new JLabel();
//		
		jlab=new JLabel("hello");
		contentPane.setSize(300, 300);
		contentPane.setLayout(new FlowLayout());
		contentPane.add(jlab);

			
		

//		anim=new JToggleButton("<html><i><font size='4';color='606060';face='Verdana'>Animation</font><i></html>", true);
//		visible=new JToggleButton("<html><i><font size='4';color='606060';face='Verdana'>Visible</font><i></html>", true);
//		useSize=new JToggleButton("<html><i><font size='4';color='606060';face='Verdana'>Set Size</font><i></html>", false);
//		//		anim.setBackground(new Color(255, 0, 0));
//		ImageIcon selIcon=null;
//		try {
//			selIcon=new ImageIcon(PuzzleSolver.getScaledImage(ImageIO.read(new File("./vidg.png")),25,20));
//		} catch (IOException e3) {
//			// TODO Auto-generated catch block
//			e3.printStackTrace();
//		}
//		ImageIcon uselIcon=null;
//		try {
//			uselIcon=new ImageIcon(PuzzleSolver.getScaledImage(ImageIO.read(new File("./vidr.png")),25,20));
//		} catch (IOException e3) {
//			// TODO Auto-generated catch block
//			e3.printStackTrace();
//		}
//		anim.setIcon(uselIcon);
//		anim.setSelectedIcon(selIcon);
//		ImageIcon vislIcon=null;
//		try {
//			vislIcon=new ImageIcon(PuzzleSolver.getScaledImage(ImageIO.read(new File("./screen.png")),25,20));
//		} catch (IOException e3) {
//			// TODO Auto-generated catch block
//			e3.printStackTrace();
//		}
//		ImageIcon vislIconE=null;
//		try {
//			vislIconE=new ImageIcon(PuzzleSolver.getScaledImage(ImageIO.read(new File("./screenE.png")),25,20));
//		} catch (IOException e3) {
//			// TODO Auto-generated catch block
//			e3.printStackTrace();
//		}
//		visible.setIcon(vislIconE);
//		visible.setSelectedIcon(vislIcon);
//		ImageIcon icon = null;
//		inputPanel.add(inputLabel);
//		try {
//			icon = new ImageIcon(PuzzleSolver.getScaledImage(ImageIO.read(new File("./icon.png")),55,56));
//		} catch (IOException e2) {
//			// TODO Auto-generated catch block
//			e2.printStackTrace();
//		}
//		openPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
//		openLayout=new GridLayout(8,1,0,10);
//		twoColPanel.add(openPanel);
//		twoColPanel.add(previewPanel);
//		tabbedPane.addTab("Set",icon,twoColPanel);
//		tabbedPane.addTab("Input",icon,inputPanel);
//		tabbedPane.addTab("Output",icon,runPanel);
//		tabbedPane.setBackgroundAt(0, new Color(255, 255, 255));
//		tabbedPane.setBackgroundAt(1, new Color(255, 255, 255));
//		tabbedPane.setBackgroundAt(2, new Color(255, 255, 255));
//		
//		openPanel.setLayout(openLayout);
//		startLabel=new JLabel("<html><i><u><font size='6';color='blue';face='Comic sans MS'>Please Select a Photo: </font></u><i></html>");
//		final String[] listS=(String[]) list.toArray(new String[list.size()]);
//		picList=new JComboBox<String>(listS);
//		elimOpList=new JComboBox<String>(elimOp);
//		type=elimOpList.getSelectedIndex();
//		openPanel.add(startLabel);
//		openPanel.add(picList);
//		openPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
//		shuffleB=new JButton("<html><b><font size='5';color='#00FFFF';face='Verdana'>- Shuffle -</font><b></html>");
//		startB=new JButton("<html><b><font size='5';color='green';face='Verdana'>- Start -</font><b></html>");
//		openPanel.add(shuffleB);
//		openPanel.add(startB);
//		openPanel.add(anim);
//		openPanel.add(visible);
//		openPanel.add(useSize);
//		openPanel.add(elimOpList);
//		previewPanel.add(preImLab,BorderLayout.CENTER);
		
		
//		try {
//			BufferedImage im=ImageIO.read(new File(listS[picList.getSelectedIndex()]));
//			int h=im.getHeight()*400/im.getWidth();
//			preImLab.setSize(450,300);
//			preImLab.setIcon(new ImageIcon(PuzzleSolver.getScaledImage(im,450,h)));
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		shuffleB.addActionListener(new RunButton(contentPane,tabbedPane, runPanel, inputPanel, inputLabel, picList, anim, visible, useSize, gp, listS, true,elimOpList));
//		startB.addActionListener(new RunButton(contentPane,tabbedPane, runPanel, inputPanel, inputLabel, picList, anim, visible, useSize, gp, listS, false,elimOpList));  
//		picList.addActionListener(new ActionListener() {
//
//			public void actionPerformed(ActionEvent e)
//			{
//				elimOpList.setSelectedIndex(0);
//				try {
//					BufferedImage im=ImageIO.read(new File(listS[picList.getSelectedIndex()]));
//					int h=im.getHeight()*450/im.getWidth();
//					preImLab.setIcon(new ImageIcon(PuzzleSolver.getScaledImage(im,450,h)));
//				} catch (IOException e1) {
//					e1.printStackTrace();
//				}
//			}
//		}); 
//
//		elimOpList.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e)
//			{
//				try {
//					type=elimOpList.getSelectedIndex();
//					if(type>0)
//						useSize.setSelected(true);
//					gp=new GeneratePuzzle(listS[picList.getSelectedIndex()],"elim_tmp.png", 28,false,type);
//					BufferedImage im=gp.nimg;
//					int h=im.getHeight()*450/im.getWidth();
//					preImLab.setIcon(new ImageIcon(PuzzleSolver.getScaledImage(im,450,h)));
//				} catch (IOException e1) {
//					e1.printStackTrace();
//				}
//			}
//		}); 
//		useSize.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e)
//			{
//				if(type>0)
//					useSize.setSelected(true);
//			}
//		}); 
//		visible.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e)
//			{
//				if(!visible.isSelected())
//					anim.setSelected(false);
//			}
//		});
//
//
	}


}
