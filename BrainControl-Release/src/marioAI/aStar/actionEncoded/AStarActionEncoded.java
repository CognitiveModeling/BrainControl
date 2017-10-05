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
package marioAI.aStar.actionEncoded;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import mario.main.Settings;
import marioAI.aStar.AStar;
import marioAI.aStar.AStarNodeVisualizer;
import marioAI.aStar.GoalTester;
import marioAI.movement.PlayerAction;
import marioAI.movement.PlayerActions;
import marioAI.movement.WorldGoalTester;

/**
 * Parameterize with the search-Nodes you're using. Use the A* Algorithm to find the shortest path from an initial node to a goal node.
 * Nodes are encoded by sequences of actions that lead to the corresponding state. The set of possible actions is supposed to be independent
 * of the state. If an action is not possible in a given state, the heuristics can indicate this by returning infinite cost.
 * 
 */
public class AStarActionEncoded<ACTION extends Action, STATE extends State<ACTION>> implements AStar<ACTION, STATE> 
{

	/**
	 * The maximum number of completed nodes. After that number the algorithm returns null and the the node in open or closed set with best cost is returned as
	 * a temporary goal. If negative, the search will run until the goal node is found or the search space is exhausted.
	 */
	private int maxSteps = 50000;

	/**
	 * The maximum amount of time in milliseconds the AStar main loop takes. If this limit is reached: see maxSteps. If negative, there is no time constraint
	 */
	private long maxTime = -1;

	/**
	 * Keep track of number of current search step in order to be able to abort if maxSteps is reached.
	 */
	private int currentSearchStep;


	/**
	 * May be called from another thread to stop ongoing search
	 */
	private AtomicBoolean abort = new AtomicBoolean(false);


	/**
	 * The list of possible actions (identical in each step)
	 */
	private final ArrayList<PlayerAction> possibleActions;

	
	/**
	 * Store best node if search fails
	 */
	private SearchNode bestNodeAfterSearch = null; 
	private double bestCostToGoal;
	
	private STATE initialState = null;


	public AStarActionEncoded(ArrayList<PlayerAction> actions) 
	{
		this.possibleActions = actions;
	}

	/**
	 * number of search steps performed 
	 * 
	 * @return number of search steps
	 */
	public int numSearchSteps() {
		return this.currentSearchStep;
	}

	/**
	 * Return the shortest Path from start to a goal-node. This version redirects calls which do not provide a list of expanded nodes
	 * 
	 * @param simulator provides IState objects for action sequences 
	 * @return the cheapest action sequence, or null
	 */
	public LinkedList<ACTION> findActionSequenceToGoal(STATE initialState, GoalTester goalTester) {
		return findActionSequenceToGoal(initialState, goalTester, null);
	}

	/**
	 * Can be called from another thread to stop the ongoing search.
	 */
	public void stop()
	{
		abort.set(true);
	}

	/**
	 * Return the shortest Path from start to a goal-node. This version accepts a list that is filled with viszualizers for search states (used for displaying expansion
	 * order)
	 * 
	 * @param simulator provides IState objects for action sequences 
	 * @param visualizers a list into which visualizers for states are added diring expansion
	 * @param goalTester 
	 * @return the cheapest action sequence, or null
	 */
	public LinkedList<ACTION> findActionSequenceToGoal(STATE initialState, GoalTester goalTester, List<AStarNodeVisualizer> visualizers) {
		return getActionSequence(search(initialState, visualizers, goalTester));
	}


	/**
	 * Return the best action sequence (even if it does not lead to a goal). This can only be called after the search has failed.
	 * 
	 * @param simulator provides IState objects for action sequences 
	 * @param visualizers a list into which visualizers for states are added diring expansion
	 * @return the cheapest action sequence, or null
	 */
	public LinkedList<ACTION> bestActionSequenceFound() {
		return getActionSequence(bestNodeAfterSearch);
	}


	/**
	 * Core A* algorithm using TreeMaps to store open/closed sets.
	 * @param goalTester 
	 * 
	 * @param initialNode
	 *            start of the search
	 * @param goalTester
	 * 
	 * @return goal node from which you can reconstruct the path or null if search was unsuccessful
	 */
	private SearchNode search(STATE initialState, List<AStarNodeVisualizer> visualizers, GoalTester goalTester) {
		this.initialState = initialState;
				
		// Reset counter of search steps.
		this.currentSearchStep = 0;
		this.bestNodeAfterSearch = null;
		// Time the search started.
		long startTime = System.currentTimeMillis();

		// Set of open nodes, sorted by their position
		// Operations: - contains similar position (#successors per iteration)
		// - remove from openSet, add substitute (1-2 times per iteration)
		// - find and remove minimum (once per iteration)
		// - find Minimum (once per call)
		TreeMap<Long, SearchNode> openSet = new TreeMap<Long, SearchNode>();

		// Set of closed nodes, sorted by their position
		// Operations: - contains similar Position (#successors per iteration)
		// - add node (once per iteration)
		TreeMap<Long, SearchNode> closedSet = new TreeMap<Long, SearchNode>();

		// Start node is added to the open set to be able to start the loop.
		SearchNode initialNode = new SearchNode(null, null, 0.0, initialState.getHeuristicCostToGoalNode(), possibleActions.size());
		openSet.put(initialState.getKeyCode(), initialNode);
		if (goalTester.isGoalReached(initialState)) {
			return initialNode;
		}

		// Loop through the open set of viable nodes until there are no nodes
		// left, search limit is reached or goal is found.
		bestCostToGoal = Double.POSITIVE_INFINITY;
		while (!abort.get() && (openSet.size() > 0 && (maxSteps < 0 || this.currentSearchStep < maxSteps) && (maxTime < 0 || (System.currentTimeMillis() - startTime) < maxTime))) 
		{
			// The node being expanded in this iteration is the best (cheapest
			// sum of heuristic and cost from start) node not yet explored.
			Entry<Long, SearchNode> bestEntry = getCheapestNode(openSet);
			SearchNode currentNode = bestEntry.getValue();
			Long key = bestEntry.getKey();
			openSet.remove(key);
			
			// Move node to closed set.
			closedSet.put(key, currentNode);
			
			// expand node if costFromStart is smaller than bestCostToGoal
			if (currentNode.costFromStartNode() <= bestCostToGoal) {
				this.currentSearchStep += 1;

				// now expand the action sequence of this node 
				STATE state = getState(currentNode);
				Map<SearchNode,Long> expandedNodes = expand(currentNode, state, goalTester);
				if (Settings.STOP_ASTAR_SEARCH_WHEN_GOAL_WAS_FOUND) {
					// found a goal: return
					if (bestNodeAfterSearch != null) {
						return bestNodeAfterSearch;
					}
				}
				// update the maps of existing nodes
				if (update(expandedNodes, openSet, closedSet)) {
					// Keep track of successfully expanded nodes for AStarExpansionOverlay.
					if (visualizers != null) {
						synchronized (visualizers) {
							AStarNodeVisualizer vis = state.getStateVisualizer(currentNode.costFromStartNode());
							visualizers.add(vis);							
						}
					}
				}
			}
		}

		// Search failed, temporary goal is best node that was found overall.
		this.bestNodeAfterSearch = bestOfBoth(getCheapestNode(openSet),getCheapestNode(closedSet));
		return null;
	}

	private Map<SearchNode, Long> expand(SearchNode currentNode, STATE state, GoalTester goalTester) {
		Long currentKey = state.getKeyCode();
		Map<SearchNode,Long> newNodes = new HashMap<>();
		int numActions = possibleActions.size();
		ArrayList<ACTION> list = new ArrayList<>(1);
		list.add(null);
		for (int i = 0; i < numActions; i++) 
		{
			//TODO: dirty!!
			int ai = ((WorldGoalTester)goalTester).getActorIndex();
			ACTION action = (ACTION) new PlayerActions(ai,(PlayerAction)(possibleActions.get(i)));
			
			list.set(0, action);
			State<ACTION> newState = state.getSuccessor(list);
			Long newKey = newState.getKeyCode();
			// if it is not the same node, add 
			if (newKey != currentKey)
			{
				double heuristicCost = newState.getHeuristicCostToGoalNode();
				// if heuristic cost is infinite, the action is considered impossible 
				if (heuristicCost != Double.POSITIVE_INFINITY) {
					SearchNode newNode = new SearchNode(currentNode, action, action.getActionCost(), heuristicCost, numActions);
					newNodes.put(newNode, newKey);
					// check if goal state is reached
					if (goalTester.isGoalReached(newState)) {
						this.bestNodeAfterSearch = newNode;
						this.bestCostToGoal = newNode.costFromStartNode();
					}
				}
			}
		}
		return newNodes;
	}

	@SuppressWarnings("unchecked")
	private STATE getState(SearchNode node) {
		return (STATE) initialState.getSuccessor(getActionSequence(node));
	}

	private boolean update(Map<SearchNode, Long> expandedNodes, TreeMap<Long, SearchNode> openSet, TreeMap<Long, SearchNode> closedSet) {
		boolean updated = false;
		for (Entry<SearchNode, Long> e : expandedNodes.entrySet()) {
			SearchNode newNode = e.getKey();
			Long keyCode = e.getValue();
			
			// check if it is a closed node
			SearchNode existingNode = closedSet.get(keyCode);
			if (existingNode != null) {		
				// update successors 
				existingNode.updateRecursively(newNode.costFromStartNode(), newNode.getParent(), newNode.getAction(), possibleActions);
				//updated = true;
			} else {
				// check if node already in open set.
				existingNode = openSet.get(keyCode);
				// If no node with same key is already in open set or new one is cheaper: put it into list (overwriting the existing one)
				if ((existingNode == null) || (newNode.costFromStartNode() < existingNode.costFromStartNode())) {
					openSet.put(keyCode, newNode);
					updated = true;
				}
			}			
		}
		return updated;
	}

	private SearchNode bestOfBoth(Entry<Long, SearchNode> e1, Entry<Long, SearchNode> e2) {
		if ((e1 == null) && (e2 == null))
			return null;
		if (e1 == null) 
			return e2.getValue();
		if (e2 == null) 
			return e1.getValue();
		
		//TODO: does comparing the size of the hashs make any sense?
		if (e1.getKey().compareTo(e2.getKey())<0) 
		{
			return e1.getValue();
		} 
		else 
		{
			return e2.getValue();
		}
	}

	/**
	 * Return node with least summed cost of heuristic and distance from goal, and remove it from the map
	 * 
	 * @param map
	 * @return
	 */
	private Entry<Long, SearchNode> getCheapestNode(TreeMap<Long, SearchNode> map) {
		double min = Double.POSITIVE_INFINITY;
		Entry<Long, SearchNode> best = null; 
		for (Entry<Long, SearchNode> e : map.entrySet()) {
			double cost = e.getValue().estimatedTotalCost();
			if ((cost < min) || (best == null)) {
				min = cost;
				best = e;
			}
		}
		return best;
	}


	/**
	 * Translate ancestor list of a node into an action sequence
	 * @param node
	 * @return
	 */
	private LinkedList<ACTION> getActionSequence(SearchNode node) {
		if (node == null)
			return null;
		LinkedList<ACTION> result = new LinkedList<>();
		// root node has no action assigned
		while (!node.isRoot()) {
			result.addFirst((ACTION) node.getAction());	//CHECK
			node = node.getParent();
		}
		return result;
	}

	@Override
	public void setMaxSteps(int maxSteps) {
		this.maxSteps = maxSteps;

	}

	@Override
	public void setMaxTime(long maxTime) {
		this.maxTime = maxTime;
	}

	@Override
	public STATE bestStateAfterSearch() {
		return getState(bestNodeAfterSearch);
	}

}
