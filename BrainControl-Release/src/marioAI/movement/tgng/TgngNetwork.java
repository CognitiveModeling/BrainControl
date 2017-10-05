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
package marioAI.movement.tgng;

import java.io.Serializable;
import java.util.ArrayList;

import marioAI.brain.simulation.SimulatedLevelScene;
import marioAI.brain.simulation.sprites.SimulatedSprite;
import marioAI.movement.tgng.Tgng.TgngType;
import marioWorld.engine.Logging;


/**
 * network class for the TGNG algorithm, manages nodes and all network-relevant
 * parameters
 * 
 * @author benjamin
 *
 */
public class TgngNetwork implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * a list of all existing nodes in the network
	 */
	private ArrayList<TgngNode> nodes = new ArrayList<TgngNode>();
	
	/**
	 * the generalized type of the enemy
	 */
	private TgngType type;
	
	/**
	 * the last visited node, null if network is not active
	 */
	public transient TgngNode activeNode;

	/**
	 * the last used motor command
	 */
	private transient MovementCoding lastMotorCommand = MovementCoding.UP;
	
	/**
	 * the planner is used to propagate rewards according to the emotional
	 * status
	 */
	//private transient CuriosityPlanner planner;
	
	// TgngAnalyator for evaluation
	private TgngAnalysator Analysator = new TgngAnalysator();

	/**
	 * creates an empty TGNG network
	 */
	public TgngNetwork(/*CuriosityPlanner planner, */ TgngType type) {
		Logging.addLogger("TgngNode");
		Logging.addLogger("SensoryInformation");
		//this.planner = planner;
		this.type = type;
	}

	/**
	 * returns the closest node to the given one, null if network is empty
	 * 
	 * @param simulatedNode
	 * @return
	 */
	public TgngNode getClosestNode(TgngNode simulatedNode) {
		double distance = Double.MAX_VALUE;
		TgngNode closestNode = null;
		for (TgngNode node : this.nodes) {
			double newDistance = node.distance(simulatedNode);
			if (newDistance < distance) {
				distance = newDistance;
				closestNode = node;
			}
		}
		return closestNode;
	}

	
	/**
	 * executes one timestep in the TGNG network
	 * 
	 * @param environment
	 */
	public void tick(SimulatedLevelScene environment) {
		TgngNode simulatedNode = new TgngNode(environment, this.type /*, this.planner*/);
		tick(simulatedNode);
	}

	/**
	 * executes one timestep in the TGNG network, the given enemy will be
	 * tracked. used if the enemy was killed in the last timestep
	 * 
	 * @param environment
	 * @param enemy
	 */
	public void tick(SimulatedLevelScene environment, SimulatedSprite enemy) {
		TgngNode simulatedNode = new TgngNode(environment, this.type, enemy /*, this.planner*/);
		tick(simulatedNode);
	}

	/**
	 * 
	 * executes one timestep in the TGNG network, including propagation of
	 * rewards and agents movement in the network. Also sets lastMotorCommand to
	 * the best option
	 * 
	 * @param simulatedNode
	 */
	private void tick(TgngNode simulatedNode) {
		if (this.activeNode != null) {
			// network was active before, activeNode is the last position in the
			// network
			lastMotorCommand = this.getActionIfActiveNodeExists(simulatedNode);
		} else {
			// network wasn't active before, a new activeNode needs to be found
			lastMotorCommand = this
					.getActionIfActiveNodeNotExists(simulatedNode);
		}

		for (TgngNode node : this.nodes) {
			node.propagate();
		}
		for (TgngNode node : this.nodes) {
			for (TgngEdge edge : node.getOutgoing()) {
				edge.propagate();
			}
		}
	}

	/**
	 * deactivates the network
	 */
	public void deactivate() {
		this.activeNode = null;
		this.lastMotorCommand = MovementCoding.NOTMOVING;
	}

	/**
	 * used if network was inactive before, activeNode has to be set
	 * 
	 * @param simulatedNode
	 * @return
	 */
	private MovementCoding getActionIfActiveNodeNotExists(TgngNode simulatedNode) {
		TgngNode closestNode = this.getClosestNode(simulatedNode);
		if (simulatedNode.distance(closestNode) < Tgng.THETA) {
			// agent is in a state that corresponds to a node in the network
			return this.getActionIfActiveNodeIsSetToExistingNode(simulatedNode,
					closestNode);
		} else {
			// agent is in a state that isn't represented by a node so far
			return this.getActionIfActiveNodeIsSetToNewNode(simulatedNode);
		}
	}

	/**
	 * used if the search starts at a new node
	 * 
	 * @param simulatedNode
	 * @return
	 */
	private MovementCoding getActionIfActiveNodeIsSetToNewNode(
			TgngNode simulatedNode) {
		this.nodes.add(simulatedNode);
		this.activeNode = simulatedNode;
		return this.activeNode.chooseMotorcommand();
	}

	/**
	 * used if the search starts at an existing node
	 * 
	 * @param simulatedNode
	 * @param closestNode
	 * @return
	 */
	private MovementCoding getActionIfActiveNodeIsSetToExistingNode(
			TgngNode simulatedNode, TgngNode closestNode) {
		closestNode.updateNode(simulatedNode);
		this.activeNode = closestNode;
		return this.activeNode.chooseMotorcommand();
	}

	/**
	 * used if the network is already active
	 * 
	 * @param simulatedNode
	 * @return
	 */
	private MovementCoding getActionIfActiveNodeExists(TgngNode simulatedNode) {
		if (simulatedNode.distance(this.activeNode) < Tgng.THETA) {
			// the last movement didn't lead to a major difference so it will be
			// executed again
			return this.lastMotorCommand;
		} else {
			// traversion detected
			return this.getActionIfActiveNodeChange(simulatedNode);
		}
	}

	/**
	 * used if a traversion is necessary
	 * 
	 * @param simulatedNode
	 * @return
	 */
	private MovementCoding getActionIfActiveNodeChange(TgngNode simulatedNode) {
		TgngNode closestNode = this.getClosestNode(simulatedNode);
		if (simulatedNode.distance(closestNode) < Tgng.THETA) {
			// traversion to an existing node
			closestNode.updateNode(simulatedNode);
			return getActionIfNodeChangedToExistingNode(closestNode);
		} else {
			// traversion to a new node
			return getActionIfNodeChangedToNewNode(simulatedNode);
		}
	}

	/**
	 * used if the traversion leads to an existing node
	 * 
	 * @param closestNode
	 * @return
	 */
	private MovementCoding getActionIfNodeChangedToExistingNode(
			TgngNode closestNode) {
		this.activeNode.traverse(this.lastMotorCommand, closestNode);
		this.activeNode = closestNode;
		return this.activeNode.chooseMotorcommand();
	}

	/**
	 * used if the traversion leads to a new node
	 * 
	 * @param simulatedNode
	 * @return
	 */
	private MovementCoding getActionIfNodeChangedToNewNode(
			TgngNode simulatedNode) {
		this.nodes.add(simulatedNode);
		this.activeNode.traverse(this.lastMotorCommand, simulatedNode);
		this.activeNode = simulatedNode;
		return this.activeNode.chooseMotorcommand();
	}

	/**
	 * returns the action based on the last calculated motor command
	 * 
	 * @return
	 */
	public boolean[] getAction() {
		return this.lastMotorCommand.getMovement();
	}
	
	/**
	 * sets the planner of this network recursive to all nodes and edges
	 * @param planner
	 */
//	protected void setPlannerRecursive(CuriosityPlanner planner){
//		this.planner = planner;
//		for(TgngNode node : this.nodes){
//			node.setPlanner(planner);
//			for(TgngEdge edge : node.getOutgoing()){
//				edge.setPlanner(planner);
//			}
//		}
//	}

	public ArrayList<TgngNode> getNodes() {
		return nodes;
	}
	
	// entropy divided by number of Nodes for evaluation
	public float entropyEvaluate() {
		float res = 0;
		for (TgngNode node : nodes) {
			res += node.entropy();
		}
		res = res / ((float) nodes.size());
		return res;
	}
	
	// get function for the Analysator
	public TgngAnalysator getAnalysator() {
		return this.Analysator;
	}

}
