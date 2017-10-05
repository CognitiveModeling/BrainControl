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
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import javax.swing.JOptionPane;

import mario.main.Settings;
import marioAI.aStar.AStar;
import marioAI.aStar.AStarNodeVisualizer;
import marioAI.aStar.actionEncoded.AStarActionEncoded;
import marioAI.aStar.actionEncoded.Action;
import marioAI.aStar.actionEncoded.State;
import marioAI.aStar.stateEncoded.AStarStateEncoded;
import marioAI.brain.Effect;
import marioAI.brain.simulation.SimulatedLevelScene;
import marioAI.collision.CollisionAtPosition;
import marioAI.collision.ConditionDirectionPair;
import marioAI.goals.PlanningEvent;
import marioAI.goals.PlanningEventType;
import marioAI.movement.heuristics.FineGrainPotentialField;
import marioAI.movement.heuristics.WorldNodeHeuristic;
import marioWorld.engine.Logging;
import marioWorld.engine.coordinates.CoordinatesUtil;
import marioWorld.engine.coordinates.GlobalCoarse;
import marioWorld.engine.coordinates.GlobalContinuous;

/**
 * 
 * @author Stephan, based on Jonas
 * 
 */
public class AStarSearchRunnable extends Observable implements Runnable {

	public static final boolean USE_DIRECTIONAL_APPROACH = false;

	// Time and iteration limit after which the search will stop. Negative
	// values indicate that there is no limit.
	private final long aStarTimeWindow = -10; // if timeWindow<0, this
	// disables any time limit
	// for
	// the A*. Else, a good
	// value
	// might be 5000;
	private final int aStarIterationWindow = 50000;
	/**
	 * constants passed on construction. Do not modify, not even elements in
	 * List
	 */
	private final Deque<WorldGoalTester> goalDeque;
	private final Deque<WorldGoalTester> goalDequeCopy;

	// protected final LevelSceneAdapter levelSceneAdapter;
	private final SimulatedLevelScene simulatedLevelSceneAtStartOfSearch;
	public final GlobalContinuous playerPositionAtStartOfSearch;

	/** Lists filled during search */
	private ArrayList<PlayerActions> actionPlan = new ArrayList<PlayerActions>();
	//private List<WorldNode> path = new ArrayList<WorldNode>();
	private final ArrayList<AStarNodeVisualizer> expandedNodes = new ArrayList<AStarNodeVisualizer>();

	// Whether the last search did not reach the (intermediate) goal but used
	// the best known node so far.
	private boolean goalIsTemporary;

	// Temporary goal when search failed.
	private WorldNode bestNodeAfterSearch;

	// Data needed for overlays
	private WorldNodeHeuristic heuristic;
	
	//currently running astar algorithm
	private AStar<PlayerActions, WorldNode> currentAStar = null;

	/** Multithreading */
	/** The thread which does the search contained in run() */
	private final Thread thread;
	/** this is used to control synchronization between threads */
	private final Lock lock;

	private boolean isSearching;
	//private final TreeMap<ConditionDirectionPair, TreeMap<Effect, Double>> knowledgeCopyAtStartOfSearch;

	public AStarSearchRunnable(SimulatedLevelScene startOfSearch,
			WorldGoalTester goalTester,
			TreeMap<ConditionDirectionPair, TreeMap<Effect, Double>> knowledge,
			ReentrantLock lock, AStarSimulator simulator) 
	{		
		// create a new Thread with the Runnable...
		this.thread = new Thread(this);		
		
		// this.levelSceneAdapter = levelSceneAdapter;
		// this.marioPositionAtStartOfSearch =
		// levelSceneAdapter.getSimulatedLevelScene().getMarioPos();

		this.lock = lock;

		//add the AStarSimulator as an observer to the AStarSearchRunnable 
		this.addObserver(simulator);
		this.simulatedLevelSceneAtStartOfSearch = startOfSearch;
		this.playerPositionAtStartOfSearch = simulatedLevelSceneAtStartOfSearch
				.getPlanningPlayerPos();

		
//		Goals are never declared as intermediate or as a preGoal ..
		
//		if (goalTester.goal.isIntermediate())
//			Logging.log(
//					"AStarSimulator",
//					"AStarSearch called with intermediate goal. Please check if this is really what you want to do.\nGoal is: "
//							+ goalTester.goal.toString()
//							+ "\nPlayer position is: "
//							+ playerPositionAtStartOfSearch.toGlobalCoarse()
//							.toString(), Level.SEVERE);
//		if (goalTester.goal.isPreGoal())
//			throw new IllegalArgumentException("AStarSearch called with "
//					+ "pre-goal. Please call with end-goal.");

		// AStarSearchRunnable.USE_DIRECTIONAL_APPROACH ? new
		// DirectionalWorldGoalTester(goal, simulatedLevelSceneAtStartOfSearch)
		// : new InteractionWorldGoalTester(goal,
		// simulatedLevelSceneAtStartOfSearch);

		//WHY A DEQUE!?!
		this.goalDeque = new LinkedList<WorldGoalTester>();
		this.goalDeque.add(goalTester);		

		//WHY!?
		this.goalDequeCopy = new LinkedList<WorldGoalTester>();
		for (WorldGoalTester goal : goalDeque) 
		{
			goalDequeCopy.add(goal);
		}

		/*((CAEAgent) (PlayHook.agents.get(simulatedLevelSceneAtStartOfSearch.getPlayerIndex()))).changed();
		((CAEAgent) (PlayHook.agents.get(simulatedLevelSceneAtStartOfSearch.getPlayerIndex()))).notifyObservers(PlanningEventType.GOAL_CHANGED);*/

		this.isSearching = true;

		// clone only effects. conditionDirectionPairs should never be
		// modified
//		lock.lock();	//WHAT!!??!
//		try {
//			this.knowledgeCopyAtStartOfSearch = new TreeMap<ConditionDirectionPair, TreeMap<Effect, Double>>();
//			for (Entry<ConditionDirectionPair, TreeMap<Effect, Double>> entry : knowledge
//					.entrySet()) {
//				TreeMap<Effect, Double> newMap = new TreeMap<Effect, Double>();
//				for (Entry<Effect, Double> e : entry.getValue().entrySet()) {
//					newMap.put(e.getKey().clone(), e.getValue());
//				}
//				this.knowledgeCopyAtStartOfSearch.put(entry.getKey(), newMap);
//			}
//		} finally {
//			lock.unlock();
//		}		
	}

	public void search() {
		/** call run() via start() */
		// run(); // no multithreading
		this.thread.start(); // multithreading
	}

	/**
	 * This runs on the search-thread and is called via thread.start()
	 */
	@Override
	public void run() 
	{
		try {
			//System.out.println("AStarSearchRunnable is starting new search. GoalDeque has " + goalDeque.size() + " goal(s). First is: " + goalDeque.peekFirst().goal + ". Tester is: " + this.getCurrentGoalTester());

			WorldNode initialNode = null;
			WorldGoalTester currentGoalTester = this.getCurrentGoalTester();

			initRun();

			// Should not happen anymore
			if (getCurrentGoalTester() == null) 
			{
				System.out.println("goalTester = null");
			}

			// if the goal is not within the level, clear action plan and return
			//if (currentGoalTester != null && !simulatedLevelSceneAtStartOfSearch.isInMap(currentGoalTester.getGoalPos())) 
			if (currentGoalTester != null && !currentGoalTester.getGoalPos().isWithinObservation(simulatedLevelSceneAtStartOfSearch.getPlanningPlayerPos()))
			{
				this.actionPlan.clear();
				return;
			}
			// initialize searchReturn
			AStarSearchReturn searchReturn = new AStarSearchReturn(null, null, false);

			// initialize some variables needed for searching of intermediate AND
			// final goals (not just the first goal in the list.)
			SimulatedLevelScene simulatedLevelSceneAtStartOfSearch = this.simulatedLevelSceneAtStartOfSearch;
			WorldNode lastEndNode = null;
			boolean moreNodesToSearch = true;
			double costs = 0.0;

			//as long as there are goals left that should be planned
			while (moreNodesToSearch && goalDeque.size() > 0 && isSearching) 
			{
				// initial node in the search
				initialNode = initSearch(currentGoalTester,simulatedLevelSceneAtStartOfSearch);

				setChanged();
				notifyObservers(new PlanningEvent(PlanningEventType.ASTAR_START_SEARCH));

				// perform AStar search parallel to main thread.
				System.out.println("\t\t\t\tNEW SEARCH THREAD!!!");
				searchReturn = searchActionPlan(
						simulatedLevelSceneAtStartOfSearch, initialNode,
						currentGoalTester, goalIsTemporary, aStarTimeWindow,
						aStarIterationWindow, this.expandedNodes);

				// the actionPlan and the path for the whole computed search is accumulated
				this.actionPlan.addAll(searchReturn.wasGoalReached ? searchReturn.actionPlan : new ArrayList<PlayerActions>());
				//this.path.addAll(searchReturn.path);

				// last endNode is stored to compute the next initial Mario Position
				// for the next part of the search
				lastEndNode = searchReturn.bestNodeAfterSearch;

				//the problem here is: if mario does not stop after this action is completed, this function will never return! thus, disabled
				//instead, only plan to intermediate goals where it is possible to stand somewhere afterwards
				//simulatedLevelSceneAtStartOfSearch = lastEndNode.worldState.getNextStaticPosition(lastEndNode.actionSequence);
				simulatedLevelSceneAtStartOfSearch = lastEndNode.worldState;
				
				
				// notify ...-> VoicePlanner about costs; purpose: strategy evaluation
				for (PlayerActions playerActions : searchReturn.actionPlan) {
					costs += playerActions.getActionCost();
				}
				System.out.println("SD: Goal cost is: " + costs + "; Computed for goal: " + goalDeque.getFirst().goal);
				
				// remove the element that was searched
				if (goalDeque.size() > 0) { // doesn't remove last goal
					goalDeque.removeFirst();
				}
				currentGoalTester = this.getCurrentGoalTester();
				// if (goalDeque.size() == 1) // end loop
				// moreNodesToSearch = false;
				
				
			}
			
			
			//this.bestNodeAfterSearch = searchReturn.bestNodeAfterSearch;
			this.bestNodeAfterSearch = searchReturn.bestNodeAfterSearch;			
			
			if (isSearching)	//thread was not aborted 
			{				
				setChanged();
				if(searchReturn.wasGoalReached)
				{
					//System.out.println("AStarSearchRunnable is done. PATH FOUND!");
					System.out.println("SD: AStar computed costs are sent");
					notifyObservers(new PlanningEvent(PlanningEventType.ASTAR_REACHED_GOAL, costs));
				}
				else
				{
					//System.out.println("AStarSearchRunnable is done. PATH NOT FOUND!");
					notifyObservers(new PlanningEvent(PlanningEventType.ASTAR_COULD_NOT_REACH_GOAL,goalDequeCopy.peekFirst().getGoal()));
				}
			} 
			//		else
			//			System.out.println("Search for goal abborted. PATH NOT FOUND!");
			
			this.isSearching = false;	
		} 
		catch(Throwable e) 
		{
			String msg = "Unexpected error occurred: "+e.getMessage();
			Throwable cause = e;
			do {
				for (StackTraceElement st : cause.getStackTrace()) {
					msg += "\n" + st.toString();
				}
				cause = cause.getCause();
				if (cause != null) {
					msg += "\nCaused by:";
				}
			} while(cause != null);
			JOptionPane.showMessageDialog(null, msg,"Error occurred",JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			System.exit(0);
		}
	}

	protected WorldGoalTester getGoal() {
		lock.lock();
		try {
			for (WorldGoalTester wgt : goalDeque) {
		
				//Goals are never intermediate
				//if (!wgt.isIntermediate()) {
					return wgt;
			//	}
			}
			return null;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Used for overlays - Thread-Safety is disregarded
	 */

	protected void stop()
	{
		System.out.println("AStar thread forced to stop.");
		
		//this.thread.stop();
		
		isSearching=false;
		
		if(currentAStar!=null)
			currentAStar.stop();
	}

	// TODO Thread-Safety?
	protected ArrayList<PlayerActions> getActionPlan() {
		return actionPlan;
	}

	/**
	 * Used for overlays - Thread-Safety is disregarded
	 */
	public WorldNodeHeuristic getHeuristic() {
		return heuristic;
	}

	// TODO Thread-Safety?
	protected WorldGoalTester getCurrentGoalTester() {
		return goalDeque.peekFirst();
	}

	/**
	 * Used for overlays - Thread-Safety is disregarded
	 */
//	protected List<WorldNode> getPath() {
//		return path;
//	}

	/**
	 * Used for overlays - Thread-Safety is disregarded
	 */
	protected ArrayList<AStarNodeVisualizer> getExpandedNodes() {
		return expandedNodes;
	}

//	// TODO Thread-Safety?
//	protected boolean actionPlanIsEmpty() {
//		return actionPlan.size() < 1;
//	}

	// TODO Thread-Safety?
	public boolean isSearching() 
	{
		return isSearching;
	}

	/**
	 * Returns whether a new AStar search is desired.
	 * 
	 * @return
	 */
//	protected boolean isNewActionPlanSearchWanted(
//			SimulatedLevelScene currentLevelScene) {
//		lock.lock();
//		try {
//			//CHECK: this is not true anymore.
//			// if we have no goals, or mario is still moving, don't plan.
//			// Problem with moving is: after the search would have finished,
//			// mario would have a
//			// different
//			// state as compared to when the search started. So just wait until
//			// friction slows him down (Mario's friction results in
//			// non-asymptotic behavior, so he WILL stand still at some future
//			// time).
//			if (goalDeque.size() == 0 || currentLevelScene.getPlayer().isMoving())
//				return false;
//
//			GlobalContinuous playerPositionInCurrentTimeStep = currentLevelScene
//					.getPlayerPos();
//			GlobalCoarse goalPosition = getCurrentGoalTester().getGoal()
//					.getPosition();
//			GlobalContinuous lastSearchStartPosition = playerPositionAtStartOfSearch;
//			boolean closeEnoughToRecalculate = false;
//			if (lastSearchStartPosition != null) {
//				double distanceFromStart = playerPositionInCurrentTimeStep
//						.distance(lastSearchStartPosition)
//						/ CoordinatesUtil.UNITS_PER_TILE;
//				double distanceToGoal = goalPosition
//						.distance(playerPositionInCurrentTimeStep
//								.toGlobalCoarse());
//
//				closeEnoughToRecalculate = (distanceFromStart / distanceToGoal) > MovementTuning.currentRecalculationCriterion;
//				if (distanceToGoal <= MovementTuning.currentLowerRecalculationBound)
//					closeEnoughToRecalculate = false;
//			}
//
//			//if(getCurrentGoalTester().goalReached(currentLevelScene,currentLevelScene.getCollisionsInLastTick(),this.knowledgeCopyAtStartOfSearch))
//			/*if(getCurrentGoalTester().goalInteractionReached(currentLevelScene))
//			{
//				goalDeque.removeFirst();
//				return goalDeque.size() > 0;
//			}*/	//TEST: disabled
//
//			boolean noMoreActionsPlanned = this.actionPlan.size() < 1 && !isSearching;
//			return noMoreActionsPlanned || closeEnoughToRecalculate;
//		} finally {
//			lock.unlock();
//		}
//	}

	/**
	 * This method gets called at the beginning of run
	 */
	private void initRun() {
		if (this.actionPlan.size() < 1) {
			MovementTuning.resetTuningValues();
		}
		MovementTuning.updateTuningValues();
	}
	
	/**
	 * plans from a levelScene to the goal node
	 * 
	 * @param simulatedLevelScene
	 * @return
	 * @throws Exception 
	 */
	private AStarSearchReturn searchActionPlan(
			SimulatedLevelScene simulatedLevelScene, WorldNode initialNode,
			WorldGoalTester currentGoalTester, boolean goalIsTemporary,
			long aStarTimeWindow, int aStarIterationWindow,
			List<AStarNodeVisualizer> expandedNodes) throws Exception {
		/**
		 * This is to provide the goal tester with information, whether it
		 * points to a blocking tile or not. This is used in
		 * WorldGoalTester.isGoalWithinMarioThresholds() so that mario can still
		 * reach the goal, even if he cannot enter the tile in question.
		 */
		// goalTester.isDirectionallyBlocking =
		// simulatedLevelSceneAtStartOfSearch.isBlockingAgainstDirection(goalTester.getGoalPos(),
		// goalTester.getDirection());

		// // initial node in the search
		// WorldNode initialNode = initSearch(goalTester, simulatedLevelScene);

		//TEST:
		if(currentAStar!=null)
		{
			throw new Exception("this should not happen inside astarsearchrunnable!!"); 
		}
		
		// perform A* and getPath
		if (Settings.USE_ACTION_ENCODED_ASTAR) 
		{
			ArrayList<PlayerAction> actions = new ArrayList<PlayerAction>();
			for (PlayerAction action : WorldNode.PLAYER_ACTIONS) 
			{
				actions.add(action);
			}
			currentAStar = new AStarActionEncoded<PlayerActions,WorldNode>(actions);
		} 
		else 
		{
			 currentAStar = new AStarStateEncoded<WorldNode>();
		}
		
		// set number of steps AStar plans ahead; one step == one iteration
		// WorldNode.advance
		currentAStar.setMaxTime(aStarTimeWindow);
		currentAStar.setMaxSteps(aStarIterationWindow);


		// get shortest path from initial node to intermediate goal
		List<PlayerActions> foundActionPlan = currentAStar.findActionSequenceToGoal(initialNode, currentGoalTester, expandedNodes);

		WorldNode bestNodeAfterSearch = (WorldNode) currentAStar.bestStateAfterSearch();
		boolean modifiedGoalIsTemporary = goalIsTemporary;

		// simulatedBestNode can be either in the intermediate goal or it is the
		// node with the least estimated cost to the goal
		// assume we only use WorldNodes at this point
		boolean wasGoalReached = (foundActionPlan != null);
		if (!wasGoalReached) {
			// no path was found in maxSteps constraint, therefore
			// use the path to the best node
			modifiedGoalIsTemporary = true;
			foundActionPlan = currentAStar.bestActionSequenceFound();
		}

		//Logging.logFine("AStarSimulator", "numSearchSteps: " + aStar.numSearchSteps());

		//List<PlayerAction> actionPlan = AStarSimulator.extractActionPlan(path);
		
		return new AStarSearchReturn(foundActionPlan, /*path,*/ bestNodeAfterSearch,
				/*modifiedGoalIsTemporary,*/ wasGoalReached);

	}

	private void printActionPlan(String caption, List<boolean[]> actionPlan) {
		StringBuffer sb = new StringBuffer(caption);
		for (boolean[] action : actionPlan) {
			sb.append(WorldNode.actionToString(action));
		}
		Logging.logInfo("AStarSimulator", sb.toString() + "\n");
	}

	private WorldNodeHeuristic recalculateHeuristicMap(
			SimulatedLevelScene simulatedLevelScene,
			WorldGoalTester worldGoalTester) 
	{
		WorldNodeHeuristic heuristicAlgorithm = new FineGrainPotentialField(simulatedLevelScene.createLocalStaticSimulation(), worldGoalTester,simulatedLevelScene.getPlanningPlayerPos(), true, true);
		//WorldNodeHeuristic heuristicAlgorithm = new ManhattanDistance(simulatedLevelScene.staticLocalMap, worldGoalTester,simulatedLevelScene.getPlayerPos());
		return heuristicAlgorithm;
	}

	/**
	 * This method gets called at the beginning of the search
	 * 
	 * @param goalTester
	 * @param simulatedLevelScene
	 * @return please specify the initial node from which to start here
	 */
	private WorldNode initSearch(WorldGoalTester goalTester,
			SimulatedLevelScene simulatedLevelScene) 
	{
		// recalculate heuristic
		this.heuristic = this.recalculateHeuristicMap(simulatedLevelScene,goalTester);

		//goalTester.refreshDirectionalHeuristic(simulatedLevelScene.getPlayerPos());

		// initial node in the search
		return new WorldNode(simulatedLevelScene, this.getCurrentGoalTester(),
				/*this.knowledgeCopyAtStartOfSearch, */ new ArrayList<PlayerActions>(),
				this.heuristic, simulatedLevelScene.getPlanningPlayerPos().clone(),
				new ArrayList<CollisionAtPosition>(), null);
	}

	/**
	 * Class responsibility is only encapsulating the three return objects of
	 * searchActionPlan
	 */
	private static class AStarSearchReturn {
		public final List<PlayerActions> actionPlan;
		//public final List<WorldNode> path;
		public final WorldNode bestNodeAfterSearch;
		//public final boolean goalIsTemporary;
		public final boolean wasGoalReached;

		public AStarSearchReturn(List<PlayerActions> actionPlan2,
				/*List<WorldNode> path,*/ WorldNode bestNodeAfterSearch,
				/*boolean goalIsTemporary,*/ boolean wasGoalReached) {
			this.actionPlan = actionPlan2;
			//this.path = path;
			this.bestNodeAfterSearch = bestNodeAfterSearch;
			//this.goalIsTemporary = goalIsTemporary;
			this.wasGoalReached = wasGoalReached;
		}

	}

	public Deque<WorldGoalTester> getGoalDeque() {
		return goalDequeCopy;
	}

	public WorldNode getBestNodeAfterSearch() {
		return bestNodeAfterSearch;
	}
}
