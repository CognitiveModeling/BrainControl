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

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.TreeMap;

import marioAI.aStar.AStarNodeVisualizer;
import marioAI.brain.Brain;
import marioAI.brain.Effect;
import marioAI.brain.simulation.LevelSceneAdapter;
import marioAI.brain.simulation.SimulatedLevelScene;
import marioAI.collision.ConditionDirectionPair;
import marioAI.goals.Goal;
import marioAI.goals.PlanningEvent;
import marioAI.goals.PlanningEventType;
import marioAI.movement.heuristics.WorldNodeHeuristic;
import marioAI.run.PlayHook;
import marioWorld.engine.PlayerWorldObject;
import marioWorld.engine.coordinates.GlobalContinuous;
import marioAI.agents.CAEAgent;

/**
 * Provides action plan given a goal and the current world state using
 * getAction.
 * 
 * @author Jonas E.
 * 
 */

public class AStarSimulator extends Observable implements Observer {

	// to create the start of search for AStarSearchRunnable
	private LevelSceneAdapter levelSceneAdapter;

	//private LinkedList<WorldGoalTester> allReachedGoals = new LinkedList<WorldGoalTester>();

	private final TreeMap<ConditionDirectionPair, TreeMap<Effect, Double>> knowledge;
	

	/**
	 * List of goals that are not finished in astar planning
	 */
	private final GoalDeque aStarComputationQueue = new GoalDeque();

	/**
	 * path that is computed by the last AStar search is stored here to compute
	 * the next start position for the next search
	 */
	//List<WorldNode> currentWorldNodePathInActionExecution;		//TODO: what!?
	//private SimulatedLevelScene sceneAtEndOfSearch;		//TODO: what!?	
	
	/**
	 * ActionPlan which is currently used to move Mario
	 */
	private List<PlayerActions> currentlyExecutedActionPlan = new ArrayList<PlayerActions>();

	/**
	 * Goal Mario is currently moving to
	 */
	public GoalSearchWrapper currentGoalInActionExecution;
	
	/**
	 * Data structure that stores the goals that are already computed by their
	 * AStarSearchRunnable is needed to access the action-plans that were
	 * computed.
	 */
	private GoalDeque finishedAStarComputationGoals = new GoalDeque();

	/**
	 * Flag to avoid side-effects while initiallizing the recomputation of all
	 * goals
	 */
	private boolean recompute = false;
	
	/**
	 * flag to see if the current goal list contains goals for more than one agent
	 */
	private boolean interactiveGoal = false;
	//private Brain brain;

	//private WorldGoalTester lastGoalAdded;
	
	private double strategyCosts = 0.0;

	/**
	 * Constructor initializes the attributes
	 * 
	 * @param levelSceneAdapter
	 *            , to access the Player position in case a new search is
	 *            started
	 * @param goalTester
	 *            , first goal to be reached
	 * @param brain
	 *            , brain of player which is considered in planning
	 */
	public AStarSimulator(LevelSceneAdapter levelSceneAdapter, Brain brain) 
	{
		this.levelSceneAdapter = levelSceneAdapter;
		//this.brain = brain;
		this.knowledge = brain.getKnowledge();
		
		// set new goal and start search
		//setGoal(levelSceneAdapter, goal, false);

		currentGoalInActionExecution = null;
	}

	/**
	 * returns the next scheduled action for Mario
	 * 
	 * @param levelSceneAdapter
	 * @return
	 */
	public PlayerActions getNextAction(LevelSceneAdapter levelSceneAdapter) 
	{
		levelSceneAdapter.actionPlanLock.lock();
		try 
		{
			if (!this.recompute) 
			{
				if(currentlyExecutedActionPlan.size() == 0)
				{
					nextActionPlan();
				}
				
				if(currentlyExecutedActionPlan.size() == 0)
				{
					return new PlayerActions();
				}
				
				return this.currentlyExecutedActionPlan.remove(0);
			} 
			else 
			{
				return new PlayerActions();
			}
		} 
		finally 
		{
			levelSceneAdapter.actionPlanLock.unlock();
		}
	}

	/**
	 * extracts the next action plan for mario's further movement
	 */
	private void nextActionPlan() 
	{
		if (!this.recompute) 
		{
			if (!this.finishedAStarComputationGoals.isEmpty())
			{
//				currentGoalInActionExecution = finishedAStarComputationGoals.removeFirst();
//				currentlyExecutedActionPlan = currentGoalInActionExecution.getAStarSearchRunnable().getActionPlan();
								
				//set the action plans of all involved players!
				for(int i=0;i<PlayHook.agents.size();i++) 
				{
					
					boolean planisempty=true;
					for(PlayerActions pa : (List<PlayerActions>) finishedAStarComputationGoals.getFirst().getAStarSearchRunnable().getActionPlan())
					{
						if(!pa.getKeyStates().get(i).isEmpty())
							planisempty=false;
					}
					
					if(!planisempty || interactiveGoal)
					{
						((CAEAgent)PlayHook.agents.get(i)).aStarSimulator.currentGoalInActionExecution = finishedAStarComputationGoals.getFirst();
						((CAEAgent)PlayHook.agents.get(i)).aStarSimulator.currentlyExecutedActionPlan = (List<PlayerActions>) finishedAStarComputationGoals.getFirst().getAStarSearchRunnable().getActionPlan().clone();
					}
				}
				
				finishedAStarComputationGoals.removeFirst();
			} 
			else
			{
				// no planned goals left
				currentlyExecutedActionPlan.clear();
				currentGoalInActionExecution = null;
			}
		}
	}

	/**
	 * sets a Goal (always appends) and starts a new search if no search is
	 * currently running
	 * 
	 * @param levelSceneAdapter
	 *            ,
	 * @param goalTester
	 *            , new goal to set
	 */
	/*public void setGoal(LevelSceneAdapter levelSceneAdapter, Goal goal) 
	{
		this.setGoal(levelSceneAdapter, goal, true);
	}*/
	
	/**
	 * sets a Goal and starts a new search if no search is currently running
	 * 
	 * @param levelSceneAdapter
	 *            ,
	 * @param goalTester
	 *            , new goal to set
	 * @param append
	 *            , if current scheduled goals should be maintained (true) or if
	 *            they should be thrown away (false)
	 */
	public void setGoal(LevelSceneAdapter levelSceneAdapter, ArrayList<Goal> goal, boolean append)
	{
		// if there is no goal --> error massage
		if (goal.size()==0) 
		{
			System.out.println("AStarSimulator.setGoal(): Trying set new goal with goal == null");
		} 
		else 
		{
			if (!append || getAllGoals().size() == 0)
			{
				// throw away current goalList
				// stop last search-Thread
				GoalSearchWrapper first = aStarComputationQueue.peekFirst();
				
				if (first != null && first.getAStarSearchRunnable() != null) 
				{
					first.getAStarSearchRunnable().stop();
				}
				
				// delete all future goals
				aStarComputationQueue.clear();
				// delete all goals in execution queue
				this.finishedAStarComputationGoals.clear();
				
				SimulatedLevelScene startOfSearch;
				
				if(actionsArePending())	//if currently in execution... 
				{						
					//use end state of current action
					WorldNode endState = currentGoalInActionExecution.getAStarSearchRunnable().getBestNodeAfterSearch();					
					startOfSearch = endState.worldState.getNextStaticPosition(endState.actionSequence);
					
					System.out.println("CURRENTLY IN EXECUTION! USING POSITION AFTER EXECUTION: " + endState.getPlayerPos());
				}
				else
				{
					//use current scene
					startOfSearch = levelSceneAdapter.getSimulatedLevelScene();
				}				
				interactiveGoal = checkForInteractiveGoals(goal);
				/*ArrayList<WorldGoalTester> translatedGoals = goal.translateEffectGoal(startOfSearch); 
				
				// add goal into the goalDeque and start search
				for(int i=translatedGoals.size()-1;i>=0;i--)					
					aStarComputationQueue.addFirst(new GoalSearchWrapper(translatedGoals.get(i)));*/
				
				Goal nextGoal = goal.get(0);
				aStarComputationQueue.addFirst(new GoalSearchWrapper(nextGoal.createWorldGoalTester(startOfSearch)));
				goal.remove(0);
				
				for(Goal g : goal)
				{
					aStarComputationQueue.add(new GoalSearchWrapper(g.createWorldGoalTester(levelSceneAdapter.getSimulatedLevelScene())));
				}
				
				//start first goal search
				if(!aStarComputationQueue.isEmpty())
					aStarComputationQueue.peekFirst().startAStarSearch(startOfSearch, knowledge, this);
				else
				{
					System.out.println("THIS SHOULD NEVER HAPPEN!!!");
					//setChanged();
					//this.notifyObservers(new PlanningEvent(PlanningEventType.EFFECT_CHAINING_FAILED,"EFFECT("+nextGoal.getEffect().getType().name().toUpperCase()+")"));
				}				
			} 
			else
			{ 				
				// list is not empty
				// add goal to list but no search is started
				for(Goal g : goal)
					aStarComputationQueue.add(new GoalSearchWrapper(g.createWorldGoalTester(levelSceneAdapter.getSimulatedLevelScene())));
			}
			
			//this.lastGoalAdded = goalTester;
			
			/*this.setChanged();
			this.notifyObservers(PlanningEventType.GOAL_CHANGED);*/
		}

	}
	/**
	 * Checks if the goal list contains goals for different actors
	 * @param goals
	 * @return true if different actors are in goal list
	 * 	false, if only one actor is in goal list
	 */
	private boolean checkForInteractiveGoals(ArrayList<Goal> goals) {
		PlayerWorldObject actor = goals.get(0).getActor();
		for(Goal cur : goals){
			if(!actor.equals(cur.getActor())){
				return true;
			}
			
		}
		return false;
	}

	/**
	 * Extract the sequence of actions that leads to the handed path
	 * 
	 * @param path
	 * @return
	 */
	public static List<PlayerActions> extractActionPlan(List<WorldNode> path) 
	{
		List<PlayerActions> actionPlan = new ArrayList<PlayerActions>();
		// loop through path, first node excluded
		for (int i = 1; i < path.size(); i++) {
			WorldNode next = (WorldNode) path.get(i);
			actionPlan.addAll(next.getActionSequence());
		}
		return actionPlan;
	}

	/**
	 * Returns the initial mario position of the goal which is the next to be
	 * computed
	 * 
	 * @return
	 */
	public GlobalContinuous getPlayerPositionAtStartOfSearch() {
		return this.aStarComputationQueue.getFirstAStarSearchRunnable().playerPositionAtStartOfSearch;
	}

	/**
	 * Returns the heuristic of the goal which is the next to be computed
	 * 
	 * @return
	 */
	public WorldNodeHeuristic getHeuristic() {
		return this.aStarComputationQueue.getFirstAStarSearchRunnable().getHeuristic();
	}

	/**
	 * Returns the directional heuristic of the goal which is the next to be
	 * computed
	 * 
	 * @return
	 */
	/*public DirectionalHeuristic getDirectionalHeuristic() {
		return this.aStarComputationQueue.getFirstAStarSearchRunnable()
				.getCurrentGoalTester().getDirectionalHeuristic();
	}*/

	@Override
	public void update(Observable o, Object arg) 
	{
		if(!(arg instanceof PlanningEvent))
			return;
		
		PlanningEventType typeOfChange = ((PlanningEvent) arg).type;
		switch (typeOfChange) 
		{
		case ASTAR_REACHED_GOAL:
			//TODO: this is partially redundant code, see setGoal
			
			// add costs of the reached goal
			this.strategyCosts += (double) ((PlanningEvent) arg).object;
			
			GoalSearchWrapper lastGoal = aStarComputationQueue.removeFirst();
			// put the object that ended searching into the endedGoals Deque, this will be pulled by nextActionPlan
			finishedAStarComputationGoals.add(lastGoal);

			if(!aStarComputationQueue.isEmpty()) //there are still goals left, start new search
			{									
				// get end state of last search
				WorldNode endState = lastGoal.getAStarSearchRunnable().getBestNodeAfterSearch();					
				
				SimulatedLevelScene startOfNewSearch = endState.worldState;
										
				//TODO: don't know if this is necessary
				aStarComputationQueue.addFirst(new GoalSearchWrapper(aStarComputationQueue.removeFirst().getGoal().createWorldGoalTester(startOfNewSearch)));
				
				if(!aStarComputationQueue.isEmpty())
					aStarComputationQueue.peekFirst().startAStarSearch(startOfNewSearch, knowledge, this);
				else
				{
					System.out.println("THIS SHOULD NEVER HAPPEN!!!");
					//setChanged();
					//this.notifyObservers(PlanningEventType.EFFECT_CHAINING_FAILED);						
				}
			} else {
				this.setChanged();
				this.notifyObservers(new PlanningEvent(PlanningEventType.GOALPLAN_COSTS, strategyCosts));
				strategyCosts = 0.0;
			}
			
			//pass event to CAEAgent (to start execution) and planners etc:
			setChanged();
			this.notifyObservers(arg);		
			
			break;
		case ASTAR_COULD_NOT_REACH_GOAL:
			
			this.strategyCosts = Double.POSITIVE_INFINITY;

			// observers are notified (most important CAEAgent) they will set a
			// new goal if they have a goal available
			// this makes sure, if there are goals that are not added into the
			// goalDegue yet, are added now
			setChanged();
			notifyObservers(arg);
			
			// CHECK: why does this happen?
			if (aStarComputationQueue.isEmpty())
				break;

			// remove unsuccessful search
			GoalSearchWrapper lastGoal2 = aStarComputationQueue.removeFirst();
			// get next element to be started
			GoalSearchWrapper newGoal = aStarComputationQueue.peekFirst();

			if (newGoal != null) // if there is such an element start the
				// search with the initial Mario Position of
				// the unsuccessful search
				newGoal.startAStarSearch(lastGoal2.getStartOfSearch(), knowledge, this);

			break;
		default:
			// pass notifying to observers
			setChanged();
			notifyObservers(arg);
		}
	}

	/**
	 * get the path of the current search
	 * 
	 * @return
	 */
//	public List<WorldNode> getPath() {
//		if (!this.aStarComputationQueue.isEmpty()) {
//			return this.aStarComputationQueue.getFirstAStarSearchRunnable().getPath();
//		} else {
//			return new ArrayList<WorldNode>();
//		}
//	}

	/**
	 * get all expanded nodes of the current search
	 * 
	 * @return
	 */
	public ArrayList<AStarNodeVisualizer> getExpandedNodes() 
	{
		if (this.aStarComputationQueue.getFirstAStarSearchRunnable() == null)
			return null;
		
		if (this.aStarComputationQueue.getFirstAStarSearchRunnable().isSearching()) 
		{
			return this.aStarComputationQueue.getFirstAStarSearchRunnable().getExpandedNodes();
		}
		
		return new ArrayList<AStarNodeVisualizer>();
	}

	/**
	 * get all expanded nodes of the current search
	 * 
	 * @return
	 */
	public List<AStarNodeVisualizer> getLastExpandedNodes(int last) 
	{
		if (this.aStarComputationQueue.getFirstAStarSearchRunnable() == null || aStarComputationQueue.getFirstAStarSearchRunnable().getExpandedNodes()==null)
			return null;
		
		ArrayList<AStarNodeVisualizer> ret = new ArrayList<AStarNodeVisualizer>();
		
		if (!this.aStarComputationQueue.getFirstAStarSearchRunnable().isSearching()) 
		{
			return ret;
		}		
		
		// why clone? ArrayList<AStarNodeVisualizer> listclone = (ArrayList<AStarNodeVisualizer>) aStarComputationQueue.getFirstAStarSearchRunnable().getExpandedNodes().clone();
		ArrayList<AStarNodeVisualizer> list = aStarComputationQueue.getFirstAStarSearchRunnable().getExpandedNodes();
		List<AStarNodeVisualizer> newList = new ArrayList<>();
		
		synchronized (list) {
			 int n=list.size();
			 newList.addAll(list.subList(Math.max(0, n-last), n));
		}
		return newList;
		/*
		 * the previous implementation inverted the order, why???
		 * 
		 int n=listclone.size();
		for(int i=n-1;i>n-last && i>=0; i--)
		{
			ret.add(listclone.get(i));
		}
		
		return ret;
		*/
	}	
	
	/**
	 * get expanded nodes of the current search (since the last call of this method) and clear the list
	 * 
	 * @return
	 */
	public List<AStarNodeVisualizer> getExpandedNodesAndClear() 
	{
		if (this.aStarComputationQueue.getFirstAStarSearchRunnable() == null || aStarComputationQueue.getFirstAStarSearchRunnable().getExpandedNodes()==null)
			return null;
		
		ArrayList<AStarNodeVisualizer> ret = new ArrayList<AStarNodeVisualizer>();		
		if (!this.aStarComputationQueue.getFirstAStarSearchRunnable().isSearching()) 
		{
			return ret;
		}		
		
		ArrayList<AStarNodeVisualizer> list = aStarComputationQueue.getFirstAStarSearchRunnable().getExpandedNodes();
		
		synchronized (list) {
			 ret.addAll(list);
			 list.clear();
		}
		return ret;
	}	

	
	/**
	 * checks if actionPlan of the current search is empty
	 * 
	 * @return
	 */
	public boolean actionsArePending() 
	{
		return aStarComputationQueue.size() > 0 ||  finishedAStarComputationGoals.size() > 0 || currentGoalInActionExecution!=null;
	}

	/**
	 * removes all actions of the AStarSearchRunnable of the current search
	 */
	public void clearActionPlan() 
	{
		this.aStarComputationQueue.getFirstAStarSearchRunnable().getActionPlan().clear();
	}

	/**
	 * Returns the Goal Mario is currently moving to
	 * 
	 * @return
	 */
	public Goal getCurrentGoalPlayerIsMovingTo() 
	{
		if (this.currentGoalInActionExecution == null) 
		{
			return null;
		}
		return this.currentGoalInActionExecution.getGoal();
	}

	public ArrayList<AStarNodeVisualizer> getExpandedNodesOfTheSearchPlayerIsMovingTo() 
	{
		if (currentGoalInActionExecution != null) {
			return this.currentGoalInActionExecution.getAStarSearchRunnable().getExpandedNodes();
		}
		return new ArrayList<AStarNodeVisualizer>();
	}

	public AStarSearchRunnable getCurrentAStarSearchRunnable() 
	{
		if (this.aStarComputationQueue.getFirstAStarSearchRunnable() == null)
			return null;
		if (this.aStarComputationQueue.getFirstAStarSearchRunnable().isSearching()) 
		{
			return this.aStarComputationQueue.getFirstAStarSearchRunnable();
		}
		return null;
	}

	/**
	 * Returns ALL Goals that are stored in AStarSimulator. This includes the
	 * goal to which Mario is currently moving
	 * 
	 * @return a GoalDeque with all Goals
	 */
	public GoalDeque getAllGoals() 
	{
		GoalDeque ret = new GoalDeque();
		ret.addAll(this.finishedAStarComputationGoals);
		ret.add(this.currentGoalInActionExecution);
		ret.addAll(this.aStarComputationQueue);
		return ret;
	}
	
	/**
	 * 
	 */
	public Deque<WorldGoalTester> getAllQuedGoalsWithIntermediateGoals() 
	{		
		Deque<WorldGoalTester> ret = new LinkedList<WorldGoalTester>();

		if (this.currentGoalInActionExecution != null) 
		{
			ret.addAll(this.currentGoalInActionExecution.getAStarSearchRunnable().getGoalDeque());
		}
		for (GoalSearchWrapper goal : this.finishedAStarComputationGoals) 
		{
			ret.addAll(goal.getAStarSearchRunnable().getGoalDeque());
		}
		for (GoalSearchWrapper goal : this.aStarComputationQueue) 
		{
			ret.add(goal.worldGoalTester);
		}
		return ret;
	}

	/**
	 * Deletes all Goals including the parameter
	 * 
	 * @param goal
	 */
	public void deleteAllGoalsSince(GoalSearchWrapper goal) {
		this.recompute = true;
		// searched goal is first goal
		if (goal == this.currentGoalInActionExecution) {
			this.currentlyExecutedActionPlan.clear();
			this.currentGoalInActionExecution = null;
			this.finishedAStarComputationGoals.clear();
			this.aStarComputationQueue.peekFirst().getAStarSearchRunnable().stop();
			this.aStarComputationQueue.clear();
			return;
		}

		int count = 0;
		boolean found = false;
		while (count < finishedAStarComputationGoals.size()) {
			if (finishedAStarComputationGoals.get(count) == goal) {
				found = true;
				break;
			}
			count++;
		}
		// searched Goal is element of endedGoals
		if (found) {
			this.aStarComputationQueue.peekFirst().getAStarSearchRunnable().stop();
			int i = this.finishedAStarComputationGoals.size();
			while (i >= count) {
				finishedAStarComputationGoals.remove(i);
				i--;
			}
			aStarComputationQueue.clear();
			return;
		}

		count = 0;
		// searched goal is not computed yet
		while (count < aStarComputationQueue.size()) {
			if (aStarComputationQueue.get(count) == goal) {
				break;
			}
			count++;
		}
		if (count == 0) { // if searched goal is the goal which is planned
							// currently
			// stop actual search
			aStarComputationQueue.peekFirst().getAStarSearchRunnable().stop();
		}
		int i = aStarComputationQueue.size() - 1;
		while (i >= count) {
			aStarComputationQueue.remove(i);
			i--;
		}

		this.recompute = false;
	}

	/**
	 * recomputes all goals including the goal Mario is currently moving to
	 */
	public void recomputeAllGoals() 	//CHECK!
	{
		this.recompute = true;

		this.currentlyExecutedActionPlan.clear();

		if (this.aStarComputationQueue.isSearching())
			this.aStarComputationQueue.getFirstAStarSearchRunnable().stop();

		while (!finishedAStarComputationGoals.isEmpty()) {
			this.aStarComputationQueue.addFirst(finishedAStarComputationGoals.removeLast());
		}
		if (this.currentGoalInActionExecution != null) {
			this.aStarComputationQueue.addFirst(this.currentGoalInActionExecution);
		}
		this.currentGoalInActionExecution = null;

		ArrayList<PlayerActions> actionSequence = new ArrayList<PlayerActions>();

		if (!this.aStarComputationQueue.isEmpty()) {
			this.aStarComputationQueue.peekFirst().startAStarSearch(
					this.levelSceneAdapter.getSimulatedLevelScene()
							.getNextStaticPosition(actionSequence), knowledge,
					this);
		}

		this.currentlyExecutedActionPlan = actionSequence;

		this.recompute = false;

	}

	/**
	 * this method stops every Action in AStar-Computation. It stops the
	 * search-Thread and deletes all stored goals from AStarSimulator.
	 */
	public void stopEverythingInAStarComputation() 
	{		
		this.recompute = true;
		
		if (this.aStarComputationQueue.isSearching()) 
		{
			this.aStarComputationQueue.getFirstAStarSearchRunnable().stop();
		}
		
		this.currentGoalInActionExecution = null;
		
		this.currentlyExecutedActionPlan.clear();		

		this.finishedAStarComputationGoals.clear();
		this.aStarComputationQueue.clear();		
		
		//this.lastGoalAdded=null;
		
		/*this.setChanged();
		notifyObservers(PlanningEventType.GOAL_CHANGED);*/		

		this.recompute = false;
	}

	public void setLevelSceneAdapter(LevelSceneAdapter levelSceneAdapter) 
	{
		this.levelSceneAdapter = levelSceneAdapter;

	}

	public SimulatedLevelScene getCurrentSimulatedLevelScene() 
	{
		/*if (this.sceneAtEndOfSearch != null) {
			return this.sceneAtEndOfSearch;
		} 
		else*/ 
		{

			return levelSceneAdapter.getSimulatedLevelScene();
		}
	}

	/*public LinkedList<WorldGoalTester> getAllReachedGoals() {
		return allReachedGoals;
	}*/

	public boolean isAllGoalsExecuted() 
	{
		return currentlyExecutedActionPlan != null && currentlyExecutedActionPlan.size() == 0;
	}	
	
	public Goal getCurrentGoal() 
	{
		if(currentGoalInActionExecution!=null)
			return currentGoalInActionExecution.getGoal();
		else
			return null;
	}

}
