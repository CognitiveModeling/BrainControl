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

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import marioAI.agents.CAEAgent;
import marioAI.brain.simulation.LevelSceneAdapter;


/**
 * this class visualizes the <code>GoalTimeModel<code> for a time 
 * and costs (in expanded search nodes) the player needs to get to a goal
 * 
 * @author Jonas Einig
 *
 */
public class TimeTab extends JPanel {
	private static final long serialVersionUID = -1352922638291463254L;
	public String name = "Time View";
	private JTable table;
	private JScrollPane scrollPane;
	
	public TimeTab(LevelSceneAdapter lsa, CAEAgent agent, PlayerPanel view){
		table = new JTable(new GoalTimeModel(lsa, agent));
		this.table.getTableHeader().setVisible(true);
		this.table.getTableHeader().setReorderingAllowed(false);
		this.table.getModel().addTableModelListener(view);
		this.table.setVisible(true);
		this.scrollPane = new JScrollPane(table);
		this.add(this.scrollPane);
		this.scrollPane.setVisible(true);
		this.setVisible(true);
	}
	
}
