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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import marioAI.run.PlayHook;
import marioWorld.engine.GlobalOptions;

/**
 * this class builds a <code>JPanel<code> for the visualization and the handling 
 * of the voice interaction of the user with the players. This <code>JPanel<code> 
 * can be added as a tab to the main control Gui
 * 
 * @author Jonas Einig, Mihael
 */
public class VoiceControlTab extends JPanel{


	
	private static final long serialVersionUID = -6010318448587665240L;
	
	public VoiceCommandsHistory cmdsHistory;
	private JTable cmdsHistoryTable;
	//private JTable voiceCommandsTable;
	//public JCheckBox activateReservoirs;	//OBSOLETE! now we use voice only to allow separate activation of reservoirs for each player!
	//TODO: remove that
	public JCheckBoxMenuItem activateInsecurity;	//OBSOLETE! (isn't it? does this still work?)
	public JCheckBoxMenuItem activateRecognition;
	private JTextField writtenCommandsTextField;
	private JTextField tagsTextField;
	private JScrollPane historyPane;
	private int historyPosition; //keeps track of the position in the recognized commands history when cycling through the display with up/down key
		

	
	public VoiceControlTab(VoicePanel voicePanel) 
	{
		super(new GridBagLayout());
		
		this.cmdsHistory =  PlayHook.voiceControl.getCommandsHistory();
		
		
		
		this.historyPosition = cmdsHistory.getRecognizedInputCommandsHistory().size();
		
		this.cmdsHistoryTable = new JTable(cmdsHistory);
		

		this.cmdsHistoryTable.getTableHeader().setVisible(true);
		this.cmdsHistoryTable.getTableHeader().setReorderingAllowed(false);
		this.cmdsHistoryTable.getModel().addTableModelListener(voicePanel);
		




		this.writtenCommandsTextField = new JTextField();
		writtenCommandsTextField.addActionListener(voicePanel.controller);

		writtenCommandsTextField.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == 38) {
					previousCommand();
				}
				if(e.getKeyCode() == 40){
					nextCommand();
				}
				
				
			}
		});
		
		this.tagsTextField = new JTextField();
		tagsTextField.addActionListener(voicePanel.controller);	
		
		//this.activateReservoirs = new JCheckBox("Activate Reservoir Goals ", false);
		this.activateInsecurity = new JCheckBoxMenuItem("Activate \"is it true that...\" ", false);
//		this.activateRecognition = new JCheckBox("Activate Voice Recognition", true);
		this.activateRecognition = new JCheckBoxMenuItem("Activate Voice Recognition", true);
	}
	
	public void init(){
		
		JLabel labelRecognized = new JLabel("Recognized Voice Command: ");
		
		historyPane = new JScrollPane(	this.cmdsHistoryTable);
		historyPane.setMinimumSize(new Dimension(350,GlobalOptions.yScreen-60*GlobalOptions.resolution_factor));

		
		
		LayoutHelper.putGrid(this, labelRecognized, 0, 0, 1, 1);
		
		LayoutHelper.putGrid(this, historyPane , 0, 1, 1, 1);

		
		JPanel inputCommands = new JPanel();
		inputCommands.setLayout(new GridBagLayout());
		LayoutHelper.putGrid(inputCommands, new JLabel("Write command:"), 0, 0, 1, 1);
		LayoutHelper.putGrid(inputCommands, this.writtenCommandsTextField, 1, 0, 1, 1,1,1,GridBagConstraints.LAST_LINE_START,GridBagConstraints.BOTH);
		LayoutHelper.putGrid(this,	inputCommands, 0, 2, 1, 1);
		
//		LayoutHelper.putGrid(this, activateRecognition, 0, 3,1, 1);
		
	
		
		
//		LayoutHelper.putGrid(this,	new JLabel("Enter command tags directly:"), 0, 5, 1, 1);
//		LayoutHelper.putGrid(this,	this.tagsTextField, 1, 5, 4, 1);
		

	}

	
	public JScrollPane getHistoryPane() {
		return historyPane;
	}

	/**
	 * called by controller to change text displayed by text field
	 */
	public JTextField getWrittenCommandsTextField() {
		return this.writtenCommandsTextField;
	}
	
	public JTextField getTagsTextField() {
		return this.tagsTextField;
	}
	
	public boolean isActivateRecognitionCheckbox(JCheckBoxMenuItem chkbx) {
		return chkbx == this.activateRecognition;
	}
	
	/**
	 * Following methods are called by controller to determine how to handle an
	 * event
	 */
	public boolean isWrittenCommandsTextField(JTextField field) {
		return field == this.writtenCommandsTextField;
	}
	
	public boolean isTagsTextField(JTextField field) {
		return field == this.tagsTextField;
	}
	
	
	/*public boolean isActivateReservoirCheckbox(JCheckBox checkbox) {
		return checkbox == this.activateReservoirs;
	}*/
	
	public boolean isActivateInsecurityCheckbox(JCheckBoxMenuItem checkbox) {
		return checkbox == this.activateInsecurity;
	}
	
	/*
	 * Cycle thorugh command history with up/down key
	 */
	private void previousCommand(){

		if(historyPosition > 0) {
			historyPosition--;
			this.writtenCommandsTextField.setText(cmdsHistory.getRecognizedInputCommandsHistory().get(historyPosition));
			
		} else {
			this.writtenCommandsTextField.setText("");
			resetHistoryPosition();
		}
	}
	
	private void nextCommand() {
		if(historyPosition < cmdsHistory.getRecognizedInputCommandsHistory().size()-1) {
			historyPosition++;
			this.writtenCommandsTextField.setText(cmdsHistory.getRecognizedInputCommandsHistory().get(historyPosition));
			
		} else {
			this.writtenCommandsTextField.setText("");
			historyPosition = -1;
		}
	}
	public void resetHistoryPosition() {
		this.historyPosition = cmdsHistory.getRecognizedInputCommandsHistory().size();
	}

	public JTable getCmdsHistoryTable() {
		return cmdsHistoryTable;
	}

	public void setCmdsHistoryTable(JTable cmdsHistoryTable) {
		this.cmdsHistoryTable = cmdsHistoryTable;
	}
	

}
