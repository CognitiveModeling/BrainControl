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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import marioWorld.engine.LevelScene;
import marioWorld.engine.Tickable;

public class VoicePanel extends JPanel implements TableModelListener, Tickable {
	
	
	private static final long serialVersionUID = 1L;
	protected Controller controller;
	protected VoiceControlTab voiceTab;
	private boolean tableChanged;

	public VoicePanel()
	{		
		this.controller = new Controller();		
		this.controller.setVoicePanel(this);

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowWeights = new double[] {};
		gridBagLayout.columnWeights = new double[] {};
		this.setLayout(gridBagLayout);

		//create and add the voice control Gui as a tab to main control gui
		this.voiceTab = new VoiceControlTab(this);
		this.voiceTab.init();
		
		LayoutHelper.putGrid(this, this.voiceTab, 0, 0, 1, 1, 1, 1,GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH);

		this.setVisible(true);
	}
	
	/**
	 * this method returns the voice tab of the gui
	 * this is necessary for the access of the controller to the voice tab to get additional event information
	 */
	public VoiceControlTab getVoiceTab()
	{
		return this.voiceTab;
	}
	

	@Override
	public void onTick(LevelScene levelScene) {
		// TODO Auto-generated method stub
	
	}
	@Override
	public void tableChanged(TableModelEvent e) {
		

		JTable tmp = this.voiceTab.getCmdsHistoryTable();
		tmp.changeSelection(tmp.getRowCount() - 1, 0, false, false);
		
		
	}
	
	
	
}
