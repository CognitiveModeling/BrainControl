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
package marioAI.movement;

/**
 * Structure to provide the function of delayed starting of the search to a Goal.
 * There are two possibilities (constructors) to use this class:
 * 		- start the search immediately 
 * 		- start the search later
 * @author Jonas E.
 */

import java.util.TreeMap;

import marioAI.brain.Effect;
import marioAI.brain.simulation.SimulatedLevelScene;
import marioAI.collision.ConditionDirectionPair;
import marioAI.goals.Goal;

public class GoalSearchWrapper {

	/**
	 * contains the goal
	 */
	public final WorldGoalTester worldGoalTester;
	/**
	 * SimulatedLevelScene at the start of the search
	 */
	private SimulatedLevelScene startOfSearch;
	/**
	 * executes the search to the goal 
	 */
	private AStarSearchRunnable aStarSearchRunnable;
	

	/**
	 * call when the start of the search should start immediately 
	 * and the startOfSearch is already known
	 * @param w
	 * @param s
	 * @param r
	 */
	public GoalSearchWrapper(WorldGoalTester w, SimulatedLevelScene s, TreeMap<ConditionDirectionPair, TreeMap<Effect, Double>> knowledge, AStarSimulator simulator){
		this.worldGoalTester = w;
		try {
			this.startOfSearch = (SimulatedLevelScene) s.clone();
		} catch (CloneNotSupportedException e) {
			this.startOfSearch =  s;
		}
		this.aStarSearchRunnable = new AStarSearchRunnable(startOfSearch, worldGoalTester, knowledge,startOfSearch.lock, simulator);
		this.aStarSearchRunnable.addObserver(simulator);

		if(this.worldGoalTester == null){
			throw new IllegalArgumentException("called with null goalTester");
		}
		this.aStarSearchRunnable.search();
	}
	
	/**
	 * call this constructor when the search should not be started yet and/or the start of the search is not yet known
	 * @param goalTester
	 */
	public GoalSearchWrapper(WorldGoalTester goalTester){
		this.worldGoalTester = goalTester;
	}
	
	/**
	 * call this method if a search should be started. 
	 * @param startOfSearch
	 * @param knowledge
	 * @param simulator
	 */
	public void startAStarSearch(SimulatedLevelScene startOfSearch ,TreeMap<ConditionDirectionPair, TreeMap<Effect, Double>> knowledge, AStarSimulator simulator) {
		try 
		{
			this.startOfSearch = (SimulatedLevelScene) startOfSearch.clone();
		} 
		catch (CloneNotSupportedException e) 
		{
			this.startOfSearch =  startOfSearch;
		}
		if(this.worldGoalTester == null)
		{
			throw new IllegalArgumentException("goalTester which was previously set is null");
		}
		//set the start of this search
		this.worldGoalTester.setScene(startOfSearch);
		
		//create AStarSearchRunnable
		this.aStarSearchRunnable = new AStarSearchRunnable(startOfSearch, worldGoalTester, knowledge, startOfSearch.lock, simulator);
		
		//start the acutal search
		this.aStarSearchRunnable.search();
	}

	/**
	 * Checks if the AStarSearchRunnable of this has started a search. 
	 * If it is null the method returns false.
	 * @return
	 */
	public boolean isSearching(){
		if(this.aStarSearchRunnable == null) return false;
		return this.aStarSearchRunnable.isSearching();
	}
	
	/**
	 * Gets the SimulatedLevelScene at the start of the search  
	 * @return
	 */
	public SimulatedLevelScene getStartOfSearch() {
		return startOfSearch;
	}

	/**
	 * Returns the AStarSearchRunnable that is used
	 * @return
	 */
	public AStarSearchRunnable getAStarSearchRunnable() {
		return aStarSearchRunnable;
	}

	/**
	 * Returns the Gaol of this GoalSearchWrapper
	 * @return
	 */
	public Goal getGoal() {
		return this.worldGoalTester.getGoal();
	}

}
