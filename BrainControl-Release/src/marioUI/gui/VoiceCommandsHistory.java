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

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import marioWorld.agents.Agent;


/**
 * Class storing recognised voice commands to be shown in GUI
 * 
 * @author Katrin, Mihael
 * 
 */
public class VoiceCommandsHistory extends AbstractTableModel 
{
	private static final long serialVersionUID = -6010318448587665240L;	
	
	/**
	 * String[] with the titles of the columns in the table
	 */
	private String[] header;	

	/**
	 * Stores recognised voice commands
	 */
	private ArrayList<String[]> recognizedCommandsHistory;
	
	private ArrayList<String> recognizedInputCommandsHistory;


	private int colCount;	
	
	public VoiceCommandsHistory(ArrayList <Agent> agents)
	{
		header = new String[agents.size()+1];
		
		header[0]="YOU";
		for(int i=0;i<agents.size();i++)
			header[i+1]=agents.get(i).getName();
		
		recognizedCommandsHistory = new ArrayList<String[]>();
		recognizedInputCommandsHistory = new ArrayList<String>();
		
		colCount=agents.size()+1;
	}

	@Override
	public int getColumnCount() {
		return colCount;
	}

	@Override
	public int getRowCount() {
		return this.recognizedCommandsHistory.size();
	}
	
	public String getColumnName(int column){
		return header[column];
	}

	@Override
	public Object getValueAt(int row, int column) {
//		return this.recognizedCommandsHistory.get(recognizedCommandsHistory.size()-(row +1))[column];

		return this.recognizedCommandsHistory.get(row)[column];
	}
	


	public void addNewEntry(String[] entry){
		this.recognizedCommandsHistory.add(entry);
		this.fireTableDataChanged();
		
		if(entry[0]!= null) {
			this.recognizedInputCommandsHistory.add(entry[0]);
		}
		
		
	}
	

	public ArrayList<String> getRecognizedInputCommandsHistory() {
		return recognizedInputCommandsHistory;
	}
}
