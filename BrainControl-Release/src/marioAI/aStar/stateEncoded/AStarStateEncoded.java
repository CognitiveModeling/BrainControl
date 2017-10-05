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
package marioAI.aStar.stateEncoded;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;

import marioAI.aStar.AStar;
import marioAI.aStar.AStarNodeVisualizer;
import marioAI.aStar.GoalTester;
import marioAI.aStar.actionEncoded.State;
import marioAI.movement.PlayerActions;
import marioAI.movement.WorldNode;
import marioWorld.engine.Logging;

/**
 * Parameterize with the search-Nodes you're using. Use the A* Algorithm to find the shortest path from an initial node to a goal node.
 * 
 */
public class AStarStateEncoded<N extends ISearchNode<N> & State<?>> implements AStar<PlayerActions, N> {

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

	private boolean abort=false;
	
	/**
	 * This is the result of the search. If the goal was reached, this is the node that did. If the search was unsuccessful this is the node with the lowest
	 * cost and used as an temporary goal.
	 */
	private N bestNodeAfterSearch;

	/**
	 * Set whether the overlay should display only the nodes which were expanded (meaning they were the cheapest ones once) or additionally all generated nodes
	 * (meaning every generated node, no matter how bad).
	 */
	private static final boolean DISPLAY_EXPANDED_NODES = false;
	private static final boolean DISPLAY_GENERATED_NODES = !DISPLAY_EXPANDED_NODES;

	/**
	 * Return the shortest Path from start to a goal-node. This version redirects calls which do not provide a list of expanded nodes
	 * 
	 * @param initialNode
	 * @param goalNode
	 * @return
	 */
	public List<N> shortestPath(N initialNode, GoalTester goalNode) {
		return shortestPath(initialNode, goalNode, null);
	}
	
	public void stop()
	{
		abort=true;
	}

	/**
	 * Return the shortest Path from start to a goal-node. This version accepts a list which it fills with the expandedNodes (used for displaying expansion
	 * order)
	 * 
	 * @param initialNode
	 * @param goalNode
	 * @param plotGuiNodes
	 *            empty list to be filled
	 * @return
	 */
	public List<N> shortestPath(N initialNode, GoalTester goalTester, List<AStarNodeVisualizer> plotGuiNodes) {
		N nodeInGoal = this.search(initialNode, goalTester, plotGuiNodes);
		return AStarStateEncoded.<N> path(nodeInGoal);
	}

	/**
	 * Core A* algorithm using TreeMaps to store open/closed sets.
	 * 
	 * @param initialNode
	 *            start of the search
	 * @param goalTester
	 * 
	 * @return goal node from which you can reconstruct the path
	 */
	@SuppressWarnings("unused")
	private N search(N initialNode, GoalTester goalTester, List<AStarNodeVisualizer> plotGuiNodes) {
		// Reset counter of search steps.
		this.currentSearchStep = 0;
		// Time the search started.
		long startTime = System.currentTimeMillis();

		// Set of open nodes, sorted by their position
		// Operations: - contains similar position (#successors per iteration)
		// - remove from openSet, add substitute (1-2 times per iteration)
		// - find and remove minimum (once per iteration)
		// - find Minimum (once per call)
		TreeMap<Long, N> openSet = new TreeMap<Long, N>();

		// Set of closed nodes, sorted by their position
		// Operations: - contains similar Position (#successors per iteration)
		// - add node (once per iteration)
		TreeMap<Long, N> closedSet = new TreeMap<Long, N>();

		// Start node is added to the open set to be able to start the loop.
		openSet.put(initialNode.keyCode(), initialNode);

		// Loop through the open set of viable nodes until there are no nodes
		// left, search limit is reached or goal is found.
		
		while (!abort && (openSet.size() > 0 && (maxSteps < 0 || this.currentSearchStep < maxSteps) && (maxTime < 0 || (System.currentTimeMillis() - startTime) < maxTime))) 
		{
			// The node being expanded in this iteration is the best (cheapest
			// sum of heuristic and cost from start) node not yet explored.			
			N currentNode = cheapestNode(openSet);
			
			Logging.log("AStarSimulator", "cheapestNode: "+currentNode.toString(), Level.FINE);
			// Best node is no longer available to be expanded.
			openSet.remove(currentNode.keyCode());
			closedSet.put(currentNode.keyCode(), currentNode);
			this.currentSearchStep += 1;

			// Keep track of expanded nodes for AStarExpansionOverlay.
			if (DISPLAY_EXPANDED_NODES && plotGuiNodes != null) {
				synchronized (plotGuiNodes) {
					plotGuiNodes.add(((WorldNode)currentNode).getStateVisualizer(currentNode.costFromStartNode()));
				}
			}

			// If goal reached return the goal-node (to then recreate its path).
			if (goalTester.isGoalReached(currentNode)) 
			{
				this.bestNodeAfterSearch = currentNode;
				//System.out.println("AStar reached goal after " + currentSearchStep + " steps.");
				return currentNode;
			}

			// Expand the current best node and get its successors.
			List<N> successorNodes = currentNode.getSuccessors();

			// For each successor do:
			// - check if already visited (closedSet), if yes skip it
			// - check if node is in openSet, maybe replace that one if new cost
			// is cheaper
			for (N successorNode : successorNodes) {
				// Nodes with cost infinity are not considered to be in the
				// search space since search over those makes no sense (i.e.
				// when they are out of the observation and heuristic cost is
				// infinite). Though this is not mandatory since those nodes
				// will not be expanded while cheaper ones are available,
				// the search would otherwise pointlessly explore those regions
				// indefinitely when no limit is given.
				if (successorNode.heuristicCostToGoalNode() == Double.POSITIVE_INFINITY) {
					continue;
				}

				// This node has already been expanded earlier (-> at cheaper
				// cost), skip.
				if (closedSet.containsKey(successorNode.keyCode())) {
					continue;
				}

				boolean isInOpenSet = false;

				// Check if node with same key is already in open set.
				N alreadyContainedNode = openSet.get(successorNode.keyCode());
				if (alreadyContainedNode != null) {
					Logging.log("AStarSimulator", "found other node with shorter Path. Other node is: "+alreadyContainedNode+". New node is: "+successorNode.toString(), Level.FINE);
					isInOpenSet = true;
				}

				// Calculate new successor's cost by adding traveled way from
				// parent node (=currentNode).
//				double successorsCost = currentNode.costFromStartNode() + currentNode.costToSuccessor(successorNode);
				
				// Node with same key is already in open set, use the cheaper
				// one.
				if (isInOpenSet)
					if (successorNode.costFromStartNode() >= alreadyContainedNode.costFromStartNode()) {
						Logging.log("AStarSimulator", "found other node with shorter Path. Other node is: "+alreadyContainedNode+". New node would have been: "+successorNode.toString(), Level.FINE);
						continue;
					}
					else
						openSet.remove(alreadyContainedNode.keyCode());

				// This successor node is neither in closed set nor is there a
				// cheaper one with same key in open set. Update cost and parent
				// of successor node before inserting.
//				successorNode.setCostFromStartNode(successorsCost);
//				successorNode.setParent(currentNode);
				openSet.put(successorNode.keyCode(), successorNode);

				// Keep track of all generated nodes for AStarExpansionOverlay.
				if (plotGuiNodes != null && DISPLAY_GENERATED_NODES) {
					synchronized (plotGuiNodes) {
						plotGuiNodes.add(((WorldNode)currentNode).getStateVisualizer(currentNode.costFromStartNode()));
					}
				}
			}
			
//			System.out.println("parent: "+currentNode.toString());
//			
//			for (N successorNode : successorNodes) {
//				String s = ": successor: "+successorNode.toString();
//				System.out.println(s);
//			}
			
//			int debugTest = AStarSimulator.extractActionPlan(AStar.<WorldNode> path((WorldNode)currentNode)).size();
//			if(debugTest>16) {
//				System.exit(-1);
//			}
		}

		// Search failed, temporary goal is best node that was found overall.
		// this.bestNodeAfterSearch = openSet.size() > 0 ? cheapestNode(openSet)
		// : cheapestNode(closedSet);
		this.bestNodeAfterSearch = cheapestNode(closedSet);

		// N closestOpenSetNode = getNodeClosestToGoal(openSet.values());
		// N closestClosedSetNode = getNodeClosestToGoal(openSet.values());
		// this.bestNodeAfterSearch =
		// (closestClosedSetNode.heuristicCostToGoalNode() <
		// closestClosedSetNode.heuristicCostToGoalNode() ?
		// closestOpenSetNode : closestClosedSetNode);

		return null;
	}

	/**
	 * Retrace path of a node
	 * 
	 * @param node
	 *            node from which the parents are to be found.
	 * @return path to the node in the argument
	 */
	public static <N extends ISearchNode<N>> List<N> path(N node) {
		if (node == null)
			return null;

		List<N> path = new ArrayList<N>();
		path.add(node);
		N currentNode = node;
		while (currentNode.getParent() != null) {
			N parent = currentNode.getParent();
			path.add(0, parent);
			currentNode = parent;
		}
		return path;
	}

	/**
	 * Return node with least summed cost of heuristic and distance from goal
	 * 
	 * @param map
	 * @return
	 */
	private N cheapestNode(TreeMap<Long, N> map) {
		// Find minimum search node according to total cost.
		return Collections.min(map.values(), new Comparator<N>() {
			public int compare(N x, N y) {
				return Double.compare(x.totalCost(), y.totalCost());
			}
		});
	}

	public int numSearchSteps() {
		return this.currentSearchStep;
	}

	public N bestNodeAfterSearch() {
		return this.bestNodeAfterSearch;
	}

	public void setMaxSteps(int maxSteps) {
		this.maxSteps = maxSteps;

	}

	public void setMaxTime(long maxTime) {
		this.maxTime = maxTime;
	}

	@Override
	public List<PlayerActions> findActionSequenceToGoal(N initialState, GoalTester goalTester, List<AStarNodeVisualizer> visualizers) {
		return extractActionSequence(shortestPath(initialState, goalTester, visualizers));
	}

	@Override
	public List<PlayerActions> bestActionSequenceFound() {
		return extractActionSequence(path(bestNodeAfterSearch));
	}

	private List<PlayerActions> extractActionSequence(List<N> path) {
		if (path == null)
			return null;
		List<PlayerActions> actionPlan = new ArrayList<PlayerActions>();
		// loop through path, first node excluded
		for (int i = 1; i < path.size(); i++) {
			WorldNode next = (WorldNode) path.get(i);
			actionPlan.addAll(next.getActionSequence());
		}
		return actionPlan;
	}

	@Override
	public N bestStateAfterSearch() {
		return bestNodeAfterSearch;
	}
	
}
