/*******************************************************************************
 * Copyright (C) 2017 Cognitive Modeling Group, University of Tuebingen
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * You can contact us at:
 * University of Tuebingen
 * Department of Computer Science
 * Cognitive Modeling
 * Sand 14
 * 72076 Tübingen 
 * cm-sekretariat -at- inf.uni-tuebingen.de
 ******************************************************************************/
package marioUI.gui;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

public class MarioError extends JFrame{
	private static final long serialVersionUID = 1L;
	private int screenHight;
	private int screenWidth;
	private int frameWidth = 500;
	private JLabel titleLabel;
	private JTextArea errorText;
	private JScrollPane errorScrollPane;
	private JButton helpButton;

	public MarioError(String errorMessage){
		super("ERROR");
		//applicable for multi monitor workstations
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		screenWidth = gd.getDisplayMode().getWidth();
		screenHight = gd.getDisplayMode().getHeight(); 

		this.setLocation((int)((screenWidth - frameWidth)*0.5), (int)(screenHight*0.25));
		this.setSize(frameWidth , (int)(screenHight*0.5)); 
		this.getContentPane().setLayout(new GridBagLayout());
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		titleLabel = new JLabel();
		titleLabel.setText("Unfortunately an Error occured");
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		titleLabel.setFont(new Font(titleLabel.getFont().getFontName(), Font.BOLD, 32));
		titleLabel.setForeground(Color.RED);
		titleLabel.setVisible(true);
		LayoutHelper.putGrid(this, titleLabel, 0, 0, 2,1, 1.0, 1.0);
		
		
		errorText = new JTextArea();
		errorText.setText(errorMessage);
		errorText.setWrapStyleWord(true);
	    errorText.setLineWrap(true);
	    errorText.setEditable(false);
		errorText.setFont(new Font(errorText.getFont().getFontName(), Font.BOLD, 20));
		errorText.setVisible(true);
		

		errorScrollPane = new JScrollPane(errorText);
		errorText.setCaretPosition(0);
		LayoutHelper.putGrid(this, errorScrollPane, 0, 1, 2,1, 1.0, 1.0);
		
		helpButton = new JButton("HELP");
		helpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (Desktop.isDesktopSupported()) {
				    try {
				        File myFile = new File("D:/Data/Desktop/Mario SoSe15 Einfï¿½hrung/MarioHelp.pdf");
				        Desktop.getDesktop().open(myFile);
				    } catch (IOException ex) {
				        new MarioError("Could not find help file");
				    }
				}
			}
		});
		LayoutHelper.putGrid(this, helpButton, 0, 2, 2,1, 1.0, 0.2);	
		this.setVisible(true);
	}

}
