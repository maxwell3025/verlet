package main;

import java.awt.Dimension;

import javax.swing.JFrame;

public class MainFrame extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MainFrame(int width, int height) {
		MainPanel3 panel = new MainPanel3();
		panel.setPreferredSize(new Dimension(width,height));
		add(panel);
		panel.Init();
		//addKeyListener(panel);
		setResizable(false);
		pack();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(3);
		setVisible(true);
	}

}
