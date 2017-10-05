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
 * This class creates objects to represent the nodes of the graph of the TGNG
 * 
 * @author Marc
 *
 */
public class TgngNode implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * abstract implementation of the relevant informations of the reaction
	 * field around the player
	 */
	private SensoryInformation input;
	/**
	 * list of all outgoing edges
	 */
	private ArrayList<TgngEdge> outgoing = new ArrayList<TgngEdge>();
	/**
	 * list of all incoming edges
	 */
	private ArrayList<TgngEdge> incoming = new ArrayList<TgngEdge>();
	/**
	 * array which counts the frequency of occurrence of the diffrent
	 * Motorcodings
	 */
	private int[] counterNode = new int[14];
	/**
	 * the reward the Node holds
	 */
	private double reward;
	/**
	 * the reward the Node had a propagation step before
	 */
	private double oldReward;
	/**
	 * the best edge of all outgoing edges
	 */
	//private TgngEdge bestEdge;
	private MovementCoding bestMotorCommand;
	/**
	 * the exploration rate based on the degree of exploration of the node is
	 * set on [0.05,1]
	 */
	private double explorationRate;
	
	/**
	 * the generalized type of the enemy
	 */
	private TgngType type;
	
	/**
	 * the planner is used to propagate rewards according to the emotional
	 * status
	 */
	//private transient CuriosityPlanner planner;

	/**
	 * creates a TgngNode by extracting information from the
	 * simulatedLevelScene
	 * 
	 * @param environment
	 */
	public TgngNode(SimulatedLevelScene environment, TgngType type /*, CuriosityPlanner planner*/) {
		this.type = type;
		//this.planner = planner;
		this.input = new SensoryInformation(environment);
	}

	/**
	 * creates a TgngNode by extracting information from the simulatedLevelScene
	 * and the already known out- and incoming edges
	 * 
	 * @param environment
	 * @param outgoing
	 * @param incoming
	 */
	public TgngNode(SimulatedLevelScene environment, TgngType type,
			ArrayList<TgngEdge> outgoing, ArrayList<TgngEdge> incoming /*, CuriosityPlanner planner*/) {
		this.type = type;
		//this.planner = planner;
		this.input = new SensoryInformation(environment);
		this.incoming = incoming;
		this.outgoing = outgoing;
	}

	/**
	 * creates a TgngNode using the information from the SimulatedLevelScene
	 * except the enemy-dependent parameters, these will be taken from the given
	 * enemy
	 * 
	 * @param environment
	 * @param enemy
	 */
	public TgngNode(SimulatedLevelScene environment, TgngType type, SimulatedSprite enemy /*, CuriosityPlanner planner*/) {
		this.type = type;
		//this.planner = planner;
		this.input = new SensoryInformation(environment, enemy);
	}

	public int[] getCounterNode() {
		return counterNode;
	}

	/**
	 * calculates the weighted distance between two nodes
	 * 
	 * @param otherNode
	 * @return
	 */
	public double distance(TgngNode otherNode) {
		double weightedDistance = 0;

		double[] ownVector = this.input.toVector();

		if (otherNode == null) {
			return 2 * Tgng.THETA;
		}
		double[] otherVector = otherNode.input.toVector();

		// sum the squared weighted differences between the two vectors
		for (int i = 0; i < ownVector.length; i++) {
			weightedDistance += Math.pow(Tgng.WEIGHTS[i]
					* (ownVector[i] - otherVector[i]), 2);
		}
		// return the square root of the summed differences
		return Math.sqrt(weightedDistance);
	}

	/**
	 * updates the parameters of a node in the direction of another node
	 * 
	 * @param otherNode
	 */
	public void updateNode(TgngNode otherNode) {
		if (this.distance(otherNode) > Tgng.THETA) {
			Logging.log("TgngNode",
					"updateNode called although distance was >theta.");
			return;
		} else if (otherNode == null) {
			Logging.logWarning("TgngNode", "UpdateNode argument was null");
			return;
		}

		this.input = SensoryInformation.getMean(this.input, otherNode.input);
	}

	/**
	 * calculates and sets the explorationRate based on the degree of
	 * exploration of the node
	 */
	public void calculateExplorationRate() {
		double sum = 0;
		// sums up the values of the counterNode array by weakening them with
		// 1/(log x)
		for (int i = 0; i < this.counterNode.length; i++) {
			sum += this.weakeningFunction(this.counterNode[i]);
		}
		this.explorationRate = Math.max(sum / this.counterNode.length, 0.05);

	}

	/**
	 * weakens a value n with the weakening function 1/(log n)
	 * 
	 * @param n
	 * @return
	 */
	public double weakeningFunction(int n) {
		if (n < 2) {
			return 1.0;
		} else {
			return 1.0 / Math.log(n);
		}
	}

	/**
	 * choose the action which will be performed in the next tick with the
	 * probability of the exporationRate the best action will be choosen with
	 * the probability of 1 - explorationRate a random action will be choosen
	 * 
	 * @return
	 */
	public MovementCoding chooseMotorcommand() {
		this.calculateExplorationRate();
		if (Math.random() > this.explorationRate) {
			return this.chooseBestMotorcommand();
		} else {
			return this.chooseRandomMotorcommand();
		}

	}

	/**
	 * 
	 * @return the action of the best Edge
	 */
	public MovementCoding chooseBestMotorcommand() 
	{
		return this.bestMotorCommand;
		
		//return this.bestEdge.getMovement();
	}

	/**
	 * 
	 * @return a random action
	 */
	public MovementCoding chooseRandomMotorcommand() {
		return MovementCoding.getRandomMovementCoding(this.type);
	}

	/**
	 * Propagates the rewards through the network. searches in outgoing for the
	 * edge with the highest reward an sets it as the best edge
	 */
	public void propagate() {
		this.oldReward = this.reward;
		if (this.outgoing.isEmpty()) 
		{
			this.reward = 0;
		} 
		else 
		{
			this.reward = Double.NEGATIVE_INFINITY;
			
			double[] motorCommandReward = new double[MovementCoding.values().length];
			for (TgngEdge edge : this.outgoing) 
			{	
				motorCommandReward[edge.getMovement().ordinal()]+=edge.getReward();
				
				/*if (edge.getReward() > this.reward) 
				{
					this.reward = edge.getReward();
					this.bestEdge = edge;
				}*/							
			}
			
			for (int i=0;i<MovementCoding.values().length;i++)
			{
				if(motorCommandReward[i] > this.reward)
				{
					this.reward = motorCommandReward[i];
					this.bestMotorCommand = MovementCoding.values()[i];
				}
			}			
		}
	}

	/**
	 * updates counter for motorcommand, checks if any edge exists/ is equal to
	 * simulatedEdge, if so traverse edge
	 * 
	 * @param lastMotorCommand
	 * @param successor
	 */
	public void traverse(MovementCoding lastMotorCommand, TgngNode successor) {
		counterNode[lastMotorCommand.ordinal()]++;
		TgngEdge simulatedEdge = new TgngEdge(0, successor, this,
				lastMotorCommand, /*this.planner,*/ this.type);

		for (TgngEdge edge : this.outgoing) {
			if (edge.equals(simulatedEdge)) {
				edge.traverse();
				return;
			}
		}
		this.outgoing.add(simulatedEdge);
		successor.incoming.add(simulatedEdge);
		simulatedEdge.traverse();
	}

	public SensoryInformation getInput() {
		return input;
	}

	public double getReward() {
		return reward;
	}

	public double getOldReward() {
		return oldReward;
	}

	public ArrayList<TgngEdge> getOutgoing() {
		return outgoing;
	}
	
//	public void setPlanner(CuriosityPlanner planner) {
//		this.planner = planner;
//	}
	
	/**
	 * calculates entropy the greater it is, the lower is the uniqueness of the movement choosen by this node
	 * @return res
	 */
	public float entropy() {
		float res = 0;
		for (TgngEdge edge : outgoing)
			{
			res += -1 * (Math.log(edge.getProbability()))/(Math.log(2)) * edge.getProbability();
			}
		return res;
	}
}
