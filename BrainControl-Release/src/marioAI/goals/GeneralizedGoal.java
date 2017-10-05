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

import marioAI.brain.Effect;
import marioAI.brain.Effect.ActionEffect;
import marioAI.brain.simulation.sprites.SimulatedSprite;
import marioAI.collision.CollisionAtPosition;
import marioAI.collision.Condition;
import marioAI.collision.Condition.PlayerHealthCondition;
import marioAI.collision.ConditionDirectionPair;
import marioAI.collision.ConditionDirectionPair.CollisionDirection;
import marioAI.collision.Interactors;
import marioWorld.engine.PlayerWorldObject;
import marioWorld.engine.coordinates.GlobalCoarse;
import marioWorld.engine.sprites.Sprite;

/**  A GeneralizedGoal contains all the generalizable information
 *  (not depending on coordinates etc.) you can extract from
 *  a goal
 *
 */
public class GeneralizedGoal implements GoalObject {
	private Effect effect;
	private Interactors interactors;
	private PlayerHealthCondition playerHealthCondition;
	private CollisionDirection collisionDirection;
	/**
	 * The main constructor of the GeneralizedGoal class. It constructs
	 * a GeneralizedGoal object from information extracted from a Goal
	 * object.
	 * 
	 * @param goal the Goal object from which the desired information is extracted
	 */
	GeneralizedGoal(Goal goal){
		if(goal.getEffect() != null){
			this.effect = generalizeEffect(goal.getEffect());
		} else {
			this.effect = null;
		}
		this.interactors  = generalizeInteractors(goal.getActor(),goal.getTarget());
		this.playerHealthCondition = generalizeHealthCondition(goal.getHealthCondition());
		this.collisionDirection = generalizeCollisionDirection(goal.collisionAtPosition.getDirection());
	}
	
	// for testing purposes
	public GeneralizedGoal(ActionEffect effectType, PlayerWorldObject effectTarget, PlayerWorldObject actor, PlayerWorldObject target,
			PlayerHealthCondition health, CollisionDirection direction) {
		this.effect = new Effect(effectType, effectTarget);
		this.interactors = new Interactors(actor, target);
		this.playerHealthCondition = health;
		this.collisionDirection = direction;
	}
	
	// for testing purposes
	public GeneralizedGoal(PlayerWorldObject actor, PlayerWorldObject target,
			PlayerHealthCondition health, CollisionDirection direction) {
		this.effect = null;
		this.interactors = new Interactors(actor, target);
		this.playerHealthCondition = health;
		this.collisionDirection = direction;
	}
	
	//getters and setters:
	public PlayerWorldObject getTarget(){
		return this.interactors.getTarget();
	}
	public PlayerWorldObject getActor(){
		return this.interactors.getActor();
	}
	public CollisionDirection getCollisionDirection(){
		return this.collisionDirection;
	}
	public PlayerHealthCondition getPlayerHealthCondition(){
		return this.playerHealthCondition;
	}
	
	//generalizationfunctions for each field
	private Effect generalizeEffect(Effect effect){
			return new Effect(effect.getType(),effect.getEffectTarget());
	}
	private Interactors generalizeInteractors(PlayerWorldObject actor, PlayerWorldObject target){
		return new Interactors(actor,target);
	}
	private PlayerHealthCondition generalizeHealthCondition(PlayerHealthCondition condition){
		return condition;
	}
	private CollisionDirection generalizeCollisionDirection(CollisionDirection collisionDirection){
		return collisionDirection;
	}
	
	
	//@override
	public String toString(){
		
		String res = "";
		if(effect != null){
		res += "<Effect: " + effect.getEffectTarget().name() + "," + effect.getType().name() + ">,";
		} else {
			res += "<No Effect>,";
		}
		res += "<Interactors: " + interactors.toString() + ">,";
		res += "<HealthCondition: " + playerHealthCondition.toString() + ">,";
		res += "<CollisionDirection: " + collisionDirection.toString() + ">";
		return res;
	}

	public Effect getEffect() {
		return effect;
	}

	@Override
	public boolean equals(Object other){
		if(other instanceof GeneralizedGoal){
			GeneralizedGoal otherGeneralizedGoal = (GeneralizedGoal) other;
			boolean condition =
					this.collisionDirection == otherGeneralizedGoal.collisionDirection       &&
					//the following checks whether both effects are equal
					//since effects can be null, a rather complicated expression
					//is used here:
					(this.effect == null
					? (otherGeneralizedGoal.effect == null)
					: this.effect.equals(otherGeneralizedGoal.effect))                       &&
			        this.interactors.equals(otherGeneralizedGoal.interactors)                &&
			        this.playerHealthCondition == otherGeneralizedGoal.playerHealthCondition;
			if(condition){
				return true;
			}
		}
		return false;
	}
	
	@Override
	/**
	 * Returns a collisionAtPosition, where all components, that are not stored in GeneralizedGoals, are dummies
	 */
	public CollisionAtPosition getCollisionAtPosition() {
		GlobalCoarse dummyCoarse = new GlobalCoarse();
		SimulatedSprite dummySprite = new SimulatedSprite(new Sprite());
//		SimulatedSprite dummySprite = (SimulatedSprite) interactors.getActor().findAgent().getPlanner().levelSceneAdapter.getSimulatedLevelScene().getPlanningPlayer(); // this is bullshit, but should work as dummy
		return new CollisionAtPosition(new ConditionDirectionPair(new Condition(interactors.getActor(), interactors.getTarget(), playerHealthCondition), collisionDirection), dummyCoarse , dummySprite, dummySprite);
	}
	
	public static ArrayList<GoalObject> toGoalObjectList(ArrayList<GeneralizedGoal> list){
		ArrayList<GoalObject> goalObjects = new ArrayList<>();
		for (GeneralizedGoal goal : list) {
			goalObjects.add((GoalObject) goal);
		}
		return goalObjects;
	}

}
