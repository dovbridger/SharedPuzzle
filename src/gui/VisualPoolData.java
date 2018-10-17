package gui;

import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.Font;

import javax.swing.SwingConstants;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;


public class VisualPoolData extends RunnableFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	public JLabel[]lblBuddies = new JLabel[4];
	public JLabel lblDFE;
	public JLabel lblCenter;
	private JTable tableMain;
	private JTable tableScore;
	private JTable tableAux;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		VisualPoolData vpd = new VisualPoolData("");
		vpd.launch();
	}

	/**
	 * Create the frame.
	 */
	public VisualPoolData(String title) {
		setTitle("Pool Part Data "+title);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
  	    setAlwaysOnTop(true);
		setBounds(100, 100, 511, 252);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		lblCenter = new JLabel("00");
		lblCenter.setHorizontalAlignment(SwingConstants.CENTER);
		lblCenter.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblCenter.setBounds(388, 78, 50, 34);
		contentPane.add(lblCenter);
		createBuddyLabels(lblCenter);
		
		JLabel lblBestNeighbors = new JLabel("Best Neighbors");
		lblBestNeighbors.setHorizontalAlignment(SwingConstants.CENTER);
		lblBestNeighbors.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblBestNeighbors.setBounds(333, 11, 141, 34);
		contentPane.add(lblBestNeighbors);
		
		JLabel lblScoreComponents = new JLabel("Score Components");
		lblScoreComponents.setHorizontalAlignment(SwingConstants.CENTER);
		lblScoreComponents.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblScoreComponents.setBounds(10, 11, 179, 34);
		contentPane.add(lblScoreComponents);
		
		tableMain = new JTable();
		tableMain.setModel(new DefaultTableModel(
			new Object[][] {
				{null, "Neighbor", "BN Count", "ConfTo", "confFrom"},
				{"Up", null, null, null, null},
				{"Down", null, null, null, null},
				{"Left", null, null, null, null},
				{"Right", null, null, null, null},
				{"Total", "", null, null, null},
			},
			new String[] {
				"New column", "Neighbor", "BN Count", "ConfTo", "ConfFrom"
			}
		) 
		);
		tableMain.getColumnModel().getColumn(0).setPreferredWidth(34);
		tableMain.getColumnModel().getColumn(0).setMinWidth(25);
		tableMain.getColumnModel().getColumn(1).setPreferredWidth(65);
		tableMain.getColumnModel().getColumn(2).setPreferredWidth(65);
		tableMain.getColumnModel().getColumn(3).setPreferredWidth(57);
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		tableMain.setDefaultRenderer(String.class,centerRenderer);
		tableMain.setToolTipText("");
		tableMain.setEnabled(false);
		tableMain.setBounds(10, 44, 313, 96);
		contentPane.add(tableMain);
		
		tableScore = new JTable();
		tableScore.setModel(new DefaultTableModel(
			new String[][] {
				{"Bonus", "N-Factor", "BN-Factor", "Total Score"},
				{null, null, null, ""},
				{null, null, null, null},
			},
			new String[] {
				"New column", "New column", "New column", "New column"
			}
		));
		tableScore.getColumnModel().getColumn(0).setPreferredWidth(45);
		tableScore.setBounds(10, 151, 244, 48);
		contentPane.add(tableScore);
		
		tableAux = new JTable();
		tableAux.setModel(new DefaultTableModel(
			new Object[][] {
				{"Added By", null},
				{"Stage", null},
				{"Type", null},
			},
			new String[] {
				"New column", "New column"
			}
		));
		tableAux.setBounds(264, 151, 126, 48);
		contentPane.add(tableAux);
		
		lblDFE = new JLabel("N/A");
		lblDFE.setBounds(400, 168, 50, 19);
		contentPane.add(lblDFE);
		
		JLabel label = new JLabel("DFE:");
		label.setBounds(400, 151, 34, 19);
		contentPane.add(label);
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
		label.setFont(new Font("Tahoma", Font.PLAIN, 18));
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setForeground(Color.BLACK);
		label.setBackground(Color.LIGHT_GRAY);
		contentPane.add(label);
		return label;
	}

	public void setTable(String[][]tableContent, String tableName){
		JTable table = null;
		switch (tableName){
		case "main":
			table = tableMain;
			break;
		case "score":
			table = tableScore;
			break;
		case "aux":
			table = tableAux;
			break;
		}
		int rowStart = table.getRowCount() - tableContent.length;
		int colStart = table.getColumnCount() - tableContent[0].length;
		for (int row = rowStart; row<table.getRowCount(); row++){
			for (int col = colStart; col<table.getColumnCount(); col++){
				table.getModel().setValueAt(tableContent[row-rowStart][col-colStart], row, col);
			}
		}
	}
}
