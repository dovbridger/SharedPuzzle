package gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class MasterControlPanel extends RunnableFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtJump;
	private GuiDebugger[]puzzles;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MasterControlPanel frame = new MasterControlPanel(null);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	public static MasterControlPanel launch(GuiDebugger[]puzzles) {
		MasterControlPanel mcp = new MasterControlPanel(puzzles);
		EventQueue.invokeLater(mcp);
		return mcp;
	}
	/**
	 * Create the frame.
	 */
	public MasterControlPanel(GuiDebugger[]_puzzles) {
		puzzles = _puzzles;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 187, 334);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JButton btnFwd = new JButton("All Forward");
		btnFwd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				for (int i=0; i<puzzles.length; i++){
					puzzles[i].forward();
				}
			}
		});
		btnFwd.setBounds(24, 126, 123, 50);
		contentPane.add(btnFwd);
		
		JButton btnBack = new JButton("All Backward");
		btnBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (int i=0; i<puzzles.length; i++){
					puzzles[i].backward();
				}
			}
		});
		btnBack.setBounds(24, 180, 123, 50);
		contentPane.add(btnBack);
		
		JButton btnJump = new JButton("All Jump");
		btnJump.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				allJump(txtJump.getText());
			}
		});
		btnJump.setBounds(24, 71, 123, 50);
		contentPane.add(btnJump);
		
		txtJump = new JTextField();
		txtJump.setHorizontalAlignment(SwingConstants.CENTER);
		txtJump.setText("0");
		txtJump.setBounds(42, 43, 86, 20);
		contentPane.add(txtJump);
		txtJump.setColumns(10);
		
		JButton btnMistake = new JButton("All Mistake");
		btnMistake.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (int i=0; i<puzzles.length; i++){
					puzzles[i].jumpToMistake();
				}
			}
		});
		btnMistake.setBounds(24, 234, 123, 50);
		contentPane.add(btnMistake);
		
		JLabel lblNewLabel = new JLabel("Master Control Panel");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setBounds(24, 11, 123, 21);
		contentPane.add(lblNewLabel);
	}
	public void allJump(String stage){
		for (int i=0; i<puzzles.length; i++){
			puzzles[i].txtJump.setText(stage);
			puzzles[i].jump();
		}
	}
}
