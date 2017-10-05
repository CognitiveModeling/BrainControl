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
/**
 * This class provides a time-observing function to get the time & number of expanded nodes needed to reach a goal.
 * 
 * 
 * @author Jonas E. edited by Marcel
 */

package marioUI.gui;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import javax.swing.table.AbstractTableModel;

import marioAI.agents.CAEAgent;
import marioAI.brain.simulation.LevelSceneAdapter;
import marioAI.goals.Goal;
import marioAI.goals.PlanningEvent;
import marioAI.goals.PlanningEventType;
import marioWorld.engine.coordinates.GlobalContinuous;

public class GoalTimeModel extends AbstractTableModel implements Observer{

	private static final long serialVersionUID = 3081898647301827431L;

	/**
	 * HashMap to store the table-entries
	 */
	HashMap<Integer, GoalTimeContainer> timeTable = new HashMap<Integer, GoalTimeContainer >();
	
	/**
	 * this attribute makes it possible to extract the time (in frames) since MARIO has been started 
	 */
	private LevelSceneAdapter levelSceneAdapter;
	
	/**
	 * the agent provides access to the goal
	 */
	private CAEAgent agent; 
	
	/**
	 * String[] with the titles of the columns in the table
	 */
	private String[] headerString = {"Goal Number","Goal Position (x, y)", "Time needed in frames", "Total time in frames", "Expanded Nodes", "Time needed in msec", "Total time in msec"};

	/**
	 * Variable to store the system-time when game is started
	 */
	private static Long startTime;

	/**
	 * Constructor
	 * @param adapter
	 * @param agent
	 */
	public GoalTimeModel(LevelSceneAdapter adapter, CAEAgent agent){
		this.levelSceneAdapter = adapter;
		this.agent = agent;
		startTime = System.currentTimeMillis();
		//add this to agent to observe changes in agent
		this.agent.addObserver(this);
	}

	/**
	 * returns the absolute number of columns in the table
	 */
	@Override
	public int getColumnCount() {
		return headerString.length;
	}

	/**
	 * @return returns the name of the column with index 'column' 
	 */
	@Override
	public String getColumnName(int column) {
		return headerString[column];
	}

	/**
	 * returns the absolute number of rows in the table
	 */
	@Override
	public int getRowCount() {
		return timeTable.size();
	}

	
	/**
	 * @return returns the String-Value of a special cell (identified through rowIndex and columnIndex) 
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		// initialize return String
		String ret = ""; 
		switch(columnIndex){
		case 0:
			ret += rowIndex + 1;
			break;
		case 1:
			ret += pointToString(timeTable.get(rowIndex).goal.getPosition().toGlobalContinuous()) ;
			break;
		case 2:
			ret += (timeTable.get(rowIndex).frameTime - ((rowIndex > 0)?timeTable.get(rowIndex-1).frameTime:0));
			break;
		case 3: 
			ret += timeTable.get(rowIndex).frameTime;
			break;
		case 4:
			ret += timeTable.get(rowIndex).expandedNodes;
			break;
		case 5:
			ret += (long)(timeTable.get(rowIndex).msecTime - ((rowIndex > 0)?timeTable.get(rowIndex-1).msecTime:0));
			break;
		case 6: 
			ret += timeTable.get(rowIndex).msecTime;
			break;
		default:
			System.out.println("Should not happen");
		}
		return ret;
	}
	
	/**
	 * Takes a GlobalContinuous-object and creates an output String for the coordinates
	 */
	private static String pointToString(GlobalContinuous point) {
		int x = (int) point.getX();
		int y = (int) point.getY();
		return Integer.toString(x) + "," + Integer.toString(y);
	}

	/**
	 * called by agent 
	 * New entry in the table is created
	 */
	@Override
	public void update(Observable o, Object arg) 
	{
		PlanningEventType currentType = ((PlanningEvent) arg).type;

		if(currentType.equals(PlanningEventType.AGENT_REACHED_GOAL))
		{
			timeTable.put(timeTable.size(), new GoalTimeContainer(this.agent.getCurrentGoal(), levelSceneAdapter.getElapsedTimeInFrames(), agent.getAStarSimulator().getExpandedNodesOfTheSearchPlayerIsMovingTo().size(), System.currentTimeMillis()- startTime));
			this.fireTableDataChanged();
		}
	}

	/**
	 * Class to combine a goal and the time (in frames and msec) since the start of the game until the 
	 * arrival at the goal and the amount of expanded Nodes from the A*-Algorithm  into one Object.
	 * 
	 * @author Jonas E.
	 *
	 */
	private class GoalTimeContainer{
		Goal goal;
		Integer frameTime;
		Long msecTime;
		Integer expandedNodes;
		GoalTimeContainer(Goal g, Integer frameTime, Integer nodes, Long msec){
			this.goal = g;
			this.frameTime = frameTime;
			this.msecTime = msec;
			this.expandedNodes = nodes;
		}
	}
	
	
	public static double getElapsedTime(){
		return System.currentTimeMillis() - startTime;
	}

}
