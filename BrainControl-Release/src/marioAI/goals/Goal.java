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
package marioAI.goals;

import java.util.ArrayList;
import java.util.TreeMap;

import marioAI.agents.CAEAgent;
import marioAI.brain.Effect;
import marioAI.brain.Effect.ActionEffect;
import marioAI.brain.simulation.SimulatedLevelScene;
import marioAI.brain.simulation.sprites.SimulatedSprite;
import marioAI.collision.CollisionAtPosition;
import marioAI.collision.Condition;
import marioAI.collision.Condition.PlayerHealthCondition;
import marioAI.collision.ConditionDirectionPair;
import marioAI.collision.ConditionDirectionPair.CollisionDirection;
import marioAI.movement.EffectChaining;
import marioAI.movement.WorldGoalTester;
import marioWorld.engine.PlayerWorldObject;
import marioWorld.engine.coordinates.GlobalCoarse;

/**
 * Describes a goal for Mario. A goal consists of a position, the target object
 * (optional), and the desired direction (also optional). Use the supplied
 * getters to find out which of the attributes are actually used in a specific
 * instance of Goal.
 * 
 * @author sebastian, modified by Stephan
 */
public class Goal implements GoalObject{

	/**
	 * Who interacts with whom in which condition, from which relative direction
	 * and at which position.
	 */
	
	//TODO: this makes goals ambigue. a goal should exclusively be an interaction with the environment. effects are translated by the planners or effect chaining itself
	private Effect effect;
	
	public final CollisionAtPosition collisionAtPosition;	
	//saves the effect, that this goal will have, for the negotiation with another agent
	private TreeMap<Effect, Double> negotiationEffect;
	
	private EffectChaining effectChaining;
//	// Constructurs without direction:
//	/** Constructor for sprite without direction. */
//	public Goal(SimulatedSprite sprite) {
//		this(sprite, null, PlayerHealthCondition.IRRELEVANT);
//		if (sprite == null) {
//			throw new IllegalArgumentException(
//					"Goal: Sprite constructor used without a sprite!");
//		}
//	}

	
// 	not used anymore
//	/** Constructor for a position without collision. */	
//	public Goal(GlobalCoarse position) {
//		this(position, null, null, null,
//				PlayerHealthCondition.IRRELEVANT);
//	}

	

//	/**
//	 * Constructor for global position with direction, but no target (neither
//	 * static nor sprite)
//	 */
//	public Goal(GlobalCoarse position, CollisionDirection dir) {
//		this(position, null, dir, null, 
//				PlayerHealthCondition.IRRELEVANT);
//		
//	}
//
//	/** Constructor for global position and Mario world object. */
//	public Goal(GlobalCoarse position, PlayerWorldObject targetObj) {
//		this(position, targetObj, null, null, 
//				PlayerHealthCondition.IRRELEVANT);
//	}
		
	// Construstors with direction:
	/** Constructor for sprite with direction. */
//	public Goal(SimulatedSprite sprite, CollisionDirection dir,
//			PlayerHealthCondition playerHealthCondition) {
//		this(null, sprite.type, dir, sprite, playerHealthCondition);
//	}

//	/** Constructor for global position and Mario world object with direction. */	
//	public Goal(GlobalCoarse position, PlayerWorldObject targetObj,
//			CollisionDirection dir, PlayerHealthCondition playerHealthCondition) {
//		this(position, targetObj, dir, null, playerHealthCondition);
//	}
//	
	/**
	 * Given a final goal, this method creates a list containing intermediate
	 * 
	 * TODO: this function should be static and use some effect. goals should not include Effects!
	 * 
	 * (pre-)goals
	 */
	public ArrayList<GoalDistanceWrapper> translateEffectGoal(SimulatedLevelScene startOfSearch) 
	{
		ArrayList<Goal> goalDeque = new ArrayList<Goal>();

//		if (!hasEffect())
//		{
//			//goalDeque.add(this.createWorldGoalTester(startOfSearch));
//			goalDeque.add(this);
//			return goalDeque;
//		}
//		else
		{
			//--->
			long startTime = System.nanoTime();
	
			effectChaining = new EffectChaining(startOfSearch, this);
			ArrayList<GoalDistanceWrapper> effectChainingGoals = effectChaining.getGoals();
			effectChaining = null;
			
			
			long endTime = System.nanoTime();
			long duration = (endTime-startTime)/1000000;
			System.err.println("\n \n \n \n EffectChaining took " + duration + " ms \n \n \n \n");
			
			
			
			System.out.println("TRANSLATEFFECTGOAL HAT BERECHNET: " + effectChainingGoals);
			return effectChainingGoals;
			//--->
		}
	}
	
	public void stop() {
		if(effectChaining != null){
			effectChaining.stop();
		}
	}
	
	/**
	 * Main constructor.
	 * 
	 * TODO: this should be the ONLY constructor of this class. Maybe except for the effect constructor.
	 * 
	 * @param goalPosition
	 * @param actor
	 * @param target
	 * @param dir
	 * @param targetSprite
	 * @param playerHealthCondition
	 */
	public Goal
	(
			GlobalCoarse goalPosition, 
			PlayerWorldObject actor,
			SimulatedSprite actorSprite,
			PlayerWorldObject target,			
			SimulatedSprite targetSprite,			
			CollisionDirection dir,
			PlayerHealthCondition playerHealthCondition
	) 
	{
		this.collisionAtPosition = new CollisionAtPosition
				(
						new ConditionDirectionPair
						(
								new Condition
								(
										actor,
										target, 
										playerHealthCondition
								),						
								dir
						), 
						goalPosition,
						actorSprite,
						targetSprite
				);
	
	//			
	//			this.isIntermediate = isIntermediate;
	//			this.isPreGoal = isPreGoal;
		
	}
	

	//TODO: creating goals without targets avoids detecting when a goal was reached by CAEAgent!!
//	public Goal(GlobalCoarse staticPosition, CollisionDirection direction) 
//	{
//		this(staticPosition, null, direction, null, false, false, PlayerHealthCondition.IRRELEVANT);
//	}

	//TODO: creating goals without targets avoids detecting when a goal was reached by CAEAgent!!
	public Goal(Effect effect) 
	{
	
		// TODO this is dirty ;)
		// Effect Target as actor and Target??
		
		this(new GlobalCoarse(), null, null, null, null, null, PlayerHealthCondition.IRRELEVANT);
		this.effect = effect;
	}

	public Goal(PlayerWorldObject actor, /*SimulatedSprite actorSprite, */Effect effect) 
	{
	
		// TODO this is dirty ;)
		// Effect Target as actor and Target??
		
		this(new GlobalCoarse(), actor, null, null, null, null, PlayerHealthCondition.IRRELEVANT);
		this.effect = effect;
	}
	
	public boolean exists(SimulatedLevelScene simulatedLevelScene) 
	{
		return collisionAtPosition.exists(simulatedLevelScene);
	}

	public WorldGoalTester createWorldGoalTester(SimulatedLevelScene simulatedLevelScene)
	{
		//TEST:
		return new WorldGoalTester(this,simulatedLevelScene);
	}	
	
	public GlobalCoarse getPosition() 
	{
		return collisionAtPosition.getPosition();
	}

	public CollisionDirection getDirection() 
	{
		return collisionAtPosition.conditionDirectionPair.collisionDirection;
	}

	/*
	 * Actor funtions (who is the actor that interacts with the target?)
	 * */
	public PlayerWorldObject getActor() 
	{
		return collisionAtPosition.conditionDirectionPair.condition.getActor();
	}

	public SimulatedSprite getActorSprite() 
	{
		return collisionAtPosition.getActorSprite();
	}

	public boolean hasActorSprite() 
	{
		return collisionAtPosition.getActorSprite() != null;
	}
	
	
	/*
	 * Target functions (what is the thing interacted with)
	 * */
	public PlayerWorldObject getTarget() 
	{
		return collisionAtPosition.conditionDirectionPair.condition.getTarget();
	}
	
	public SimulatedSprite getTargetSprite() 
	{
		return collisionAtPosition.getTargetSprite();
	}

	public void setTargetSprite(SimulatedSprite set) 
	{
		collisionAtPosition.setTargetSprite(set);
	}	
	
	public boolean hasTargetSprite() 
	{
		return collisionAtPosition.getTargetSprite() != null;
	}

	
	
	@Override
	public String toString() 
	{
		String s = "[Goal: ";
		
		s += "Effect: " + getEffect() + ", ";
		s += getActor() + ", ";
		s += getTarget() + ", ";
		s += getDirection() + ", ";
		s += getPosition() + ", ";
		s += (hasTargetSprite() ? getTargetSprite() : "NO_SPRITE_TARGET");
		s += "] ";

		return s;
	}

	@Override
	public int hashCode() {
		return collisionAtPosition.hashCode();
		// final int prime = 31;
		// int result = 1;
		// result = prime * result + ((direction == null) ? 0 :
		// direction.hashCode());
		// result = prime * result + ((position == null) ? 0 :
		// position.hashCode());
		// result = prime * result + ((sprite == null) ? 0 : sprite.hashCode());
		// result = prime * result + ((target == null) ? 0 : target.hashCode());
		// return result;
	}

	public PlayerHealthCondition getHealthCondition() {
		return this.collisionAtPosition.conditionDirectionPair.condition.playerHealthCondition;
	}

	public void setHealthCondition(PlayerHealthCondition health) {
		this.collisionAtPosition.conditionDirectionPair.condition.playerHealthCondition = health;
	}

	@Override
	public boolean equals(Object obj) 
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return this.collisionAtPosition.equals(((Goal) obj).collisionAtPosition);
		// Goal other = (Goal) obj;
		// if (direction != other.direction)
		// return false;
		// if (position == null) {
		// if (other.position != null)
		// return false;
		// } else if (!position.equals(other.position))
		// return false;
		// if (sprite == null) {
		// if (other.sprite != null)
		// return false;
		// } else if (!sprite.equals(other.sprite))
		// return false;
		// if (target != other.target)
		// return false;
		// return true;
	}

	//TODO: has to be updated because now EffectChaining gives Goals an Effect
	public boolean hasEffect() 
	{
		return this.effect != null;
	}

	public Effect getEffect() 
	{
		return this.effect;
	}

	public void setEffect(Effect effect) {
		this.effect = effect;
	}

	@Override
	public CollisionAtPosition getCollisionAtPosition() {
		// TODO Auto-generated method stub
		return collisionAtPosition;
	}

	public boolean isEffectChainingGoal() 
	{
		return this.effect != null;
	}

	public void setNegotiationEffect(TreeMap<Effect, Double> simulatedEffectList) {
		this.negotiationEffect = simulatedEffectList;
	}
	public TreeMap<Effect, Double> getNegotiationEffect() 
	{
		return this.negotiationEffect;
	}

	

}
