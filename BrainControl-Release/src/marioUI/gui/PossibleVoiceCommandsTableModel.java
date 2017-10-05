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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

/**
 *  Class storing possible voice commands to be shown in GUI
 *
 * @author Katrin
 *
 */

public class PossibleVoiceCommandsTableModel extends AbstractTableModel {

	private static final long serialVersionUID = -2488928601882254400L;

	/**
	 * Possible Voice Commands
	 * commands[] command
	 * commands[] phonetic representation
	 */
	private final ArrayList<String[]> commands;
	
	/**
	 * Number of columns
	 */
	private final int colCount = 2;
	
	/**
	 * String[] with the titles of the columns in the table
	 */
	private String[] header = {"Possible Command", "Phoneme Representation"};
	

	/**
	 * Constructor
	 * reads possible commands from commandsAndPhonemes.txt
	 */
	public PossibleVoiceCommandsTableModel() {

		ArrayList<String> lines = new ArrayList<String>();
		BufferedReader reader;
		
		// reading possible voice commands and phoneme representation 
		// from commandsAndPhonemes.txt
		try {
			reader = new BufferedReader(new FileReader("commandsAndPhonemes.txt"));
			String line = reader.readLine();

			while (line != null) {
				lines.add(line);
				line = reader.readLine();
			}
			
			reader.close();
			
		} catch (FileNotFoundException f){
			System.out.println(f.getLocalizedMessage());
		}
		catch (Exception e) {
			System.out.println("Possible Voice Commands: error reading file commandsAndPhonemes.txt");
		} 

		ArrayList<String[]> cmds = new ArrayList<String[]>();
		for(String line : lines){
			String[] commandAndPhonemes = line.split("\t");
			if (commandAndPhonemes.length == 2)
				cmds.add(commandAndPhonemes);
		}
		
		this.commands = cmds;
		
	}

	@Override
	public int getColumnCount() {
		return colCount;
	}

	@Override
	public int getRowCount() {
		return commands.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return (commands.get(rowIndex))[columnIndex];
	}

	public String getColumnName(int column){
		return header[column];
	}
}
