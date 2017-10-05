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

import marioAI.goals.CuriosityPlanner;
import marioAI.movement.tgng.Tgng.TgngType;
import marioAI.reservoirMotivation.FearReservoir;

/**
 * This class creates objects to represent the edges of the graph of the TGNG
 * 
 * @author Marc
 *
 */
public class TgngEdge implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * the transition probability
	 */
	private float transitionProbability;
	/**
	 * The TgngNode to which the edge gos
	 */
	private TgngNode successor;
	/**
	 * The TgngNode from where the edge comes
	 */
	private TgngNode predecessor;
	/**
	 * The movement which is performed in the transition, represented as a
	 * MovementCoding enum
	 */
	private MovementCoding movement;
	/**
	 * The expected reward of the edge
	 */
	private double reward;
	/**
	 * The number of times how often the edge was traversed
	 */
	private int counter = 0;

	/**
	 * the generalized type of the enemy
	 */
	private TgngType type;

	/**
	 * the planner is used to propagate rewards according to the emotional
	 * status
	 * TODO: is it? Appears not to be used at all!
	 */
	//private transient CuriosityPlanner planner;

	/**
	 * Creates a new TgngEdge
	 * 
	 * @param probability
	 * @param successor
	 * @param predecessor
	 * @param movement
	 */
	public TgngEdge(float probability, TgngNode successor,
			TgngNode predecessor, MovementCoding movement,
			/*CuriosityPlanner planner,*/ TgngType type) {
		this.type = type;
		//this.planner = planner;
		this.transitionProbability = probability;
		this.successor = successor;
		this.predecessor = predecessor;
		this.movement = movement;
	}

	public TgngType getType() {
		return type;
	}

	/**
	 * calculates the probability for current edge
	 */
	public void calculateProbability() {
		this.transitionProbability = this.counter
				/ (predecessor.getCounterNode())[movement.ordinal()];
	}

	/**
	 * sets counter after traversion, calculates probability
	 */
	public void traverse() {
		counter++;
		this.calculateProbability();
	}

	/**
	 * evaluates, wether the TgngEdge equals the TgngEdge other by comparing
	 * successor,predecessor and movement
	 * 
	 * @param other
	 * @return
	 */
	public boolean equals(TgngEdge other) {
		return other.successor.equals(this.successor)
				&& other.predecessor.equals(this.predecessor)
				&& other.movement.equals(this.movement);

	}

	/**
	 * Propagates the rewards through the network. Either a exogenous reward is
	 * detected or the rewards get propagated trough the network by weaken the
	 * old reward of it's successor
	 */
	public void propagate() {
		double exogenousReward = this.reward();
		if (exogenousReward != 0) {
			this.reward = exogenousReward;
		} else {
			this.reward = this.transitionProbability * Tgng.GAMMA
					* this.successor.getOldReward();
		}
	}


	/**
	 * Calculates the exogenous Reward by comparing the change of the own health
	 * condition with the change of the enemy's health condition If both the own
	 * health condition and the enemy's health condition are reduced the reward
	 * stays 0
	 * 
	 * @return the exogenous reward of the edge
	 */
	public double reward() {
		double myDeltaHealth = this.successor.getInput().getHealth()
				- this.predecessor.getInput().getHealth();
		double enemyDeltaHealth = this.successor.getInput().getEnemyHealth()
				- this.predecessor.getInput().getEnemyHealth();
		double reward = 0;


		//FearReservoir reservoir = new FearReservoir();
		//int emotion = reservoir.getEmotionalStatus();

		// TODO: the lines above are equal to 
		// int emotion = 0; ????

		// appears that we never use TGNG with a CuriosityPlanner, so the case planner != null is never used

		/*if (this.planner == null)  { */
		// get negative reward if own health condition was reduced
		if (myDeltaHealth < 0) {
			reward += -1;
		}
		// get positive reward if enemy health condition was reduced
		if (enemyDeltaHealth < 0) {
			reward += 1;
		}
		/*}  else {
			// appears that we never use TGNG with a CuriosityPlanner, so the case planner != null is never used
			// anyway the code below is ineffective, since emotion is always == 0!
			//  
			if (emotion == 0) {
				// I'm fearful, so I only get a negative reward when I got hurt,
				// no positive reward, because I only want to save myself
				if (myDeltaHealth < 0) {
					reward += -1;
				}
			} else if (emotion == 2) {
				// I'm courageous, so I want to hurt my enemy and I don't want
				// to save myself
				if (enemyDeltaHealth < 0) {
					reward += 1;
				}
			} else if (emotion == 3) {
				// normal
				// get negative reward if own health condition was reduced
				if (myDeltaHealth < 0) {
					reward += -1;
				}
				// get positive reward if enemy health condition was reduced
				if (enemyDeltaHealth < 0) {
					reward += 1;
				}
			} else {// something wrong
				reward += 0;
			}
	}*/

		return reward;
	}

	public MovementCoding getMovement() {
		return movement;
	}

	public float getProbability() {
		return transitionProbability;
	}

	public double getReward() {
		return reward;
	}

	public TgngNode getSuccessor() {
		return successor;
	}

	/*public void setPlanner(CuriosityPlanner planner) {
		this.planner = planner;
	}*/

}
