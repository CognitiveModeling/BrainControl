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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import marioAI.brain.Brain;


/**
 * This class is a visualization panel for the players brain. 
 * 
 * @author Jonas Einig
 *
 */
public class BrainTab extends JPanel{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2337365286535858641L;
	private JTable table;
	private JScrollPane tablePane;
	private JButton clearButton;

	public BrainTab(Brain brain, PlayerPanel view)
	{
		final Brain finalBrain = brain;
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));

		// clear button for brain tab
		clearButton = new JButton("Clear Knowledge");
		clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				finalBrain.clearKnowledge();
			}
		});
		controlPanel.add(clearButton);


		// add clear button, disable emoticons, table header
		// and knowledge table to the brain tab and set layout
		this.add(controlPanel);
		controlPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		controlPanel.setAlignmentX(Component.TOP_ALIGNMENT);

		this.table = new JTable(brain);
		
		this.add(table.getTableHeader());
		table.getTableHeader().setAlignmentX(Component.LEFT_ALIGNMENT);
		table.getTableHeader().setAlignmentX(Component.TOP_ALIGNMENT);
		
		this.tablePane = new JScrollPane(table);
		
		this.add(tablePane);
		table.setAlignmentX(Component.LEFT_ALIGNMENT);
		table.setAlignmentY(Component.BOTTOM_ALIGNMENT);

		this.table.getTableHeader().setVisible(true);
		this.table.getTableHeader().setReorderingAllowed(false);
		this.tablePane.setVisible(true);
		
		// TODO is this good practice?
		new BrainTableRenderer(brain).setTableRenderer(this.table);

		// Register this class as listener of the table model
		table.getModel().addTableModelListener(view);
	}
	
	public JTable getTable(){
		return this.table;
	}
}
