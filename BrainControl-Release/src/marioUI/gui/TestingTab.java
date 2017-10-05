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

import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import marioAI.agents.CAEAgent;
import marioAI.run.PlayHook;

/**
 * this class builds a <code>JPanel<code> for the visualization and the handling 
 * of the voice interaction of the user with the players. This <code>JPanel<code> 
 * can be added as a tab to the main control GUI
 * 
 * @author Mihael
 */
public class TestingTab extends JPanel{
	
	private static final long serialVersionUID = 42L;
	
	public VoiceCommandsHistory cmdsHistory;
	private JTable cmdsHistoryTable;
	private JTextField inputField;
	
	public TestingTab(CAEAgent agent, PlayerPanel view) {
		super(new GridBagLayout());
		this.cmdsHistory =  PlayHook.voiceControl.getCommandsHistory();
		
		this.cmdsHistoryTable = new JTable(cmdsHistory);
		this.cmdsHistoryTable.getTableHeader().setVisible(true);
		this.cmdsHistoryTable.getTableHeader().setReorderingAllowed(false);
		this.cmdsHistoryTable.getModel().addTableModelListener(view);
	}
	
	public void init(){
		
		JLabel labelRecognized = new JLabel("Recognized Voice Command: ");
		LayoutHelper.putGrid(this, labelRecognized, 0, 0, 1, 1);
		LayoutHelper.putGrid(this, new JScrollPane(	this.cmdsHistoryTable), 0, 1, 5, 1, 0.1, 0.3);
		LayoutHelper.putGrid(this, new JLabel("Write command:"), 0, 4, 1, 1);
	}

	
	/**
	 * called by controller to change text displayed by text field
	 */
	public JTextField getWrittenCommandsTextField() {
		return this.inputField;
	}
	public boolean isWrittenCommandsTextField(JTextField field) {
		return field == this.inputField;
	}

}
