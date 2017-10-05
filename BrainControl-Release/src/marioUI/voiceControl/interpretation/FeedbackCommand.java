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
package marioUI.voiceControl.interpretation;

import marioAI.brain.Effect;
import marioAI.brain.Effect.ActionEffect;
import marioAI.collision.Condition;
import marioAI.collision.Condition.PlayerHealthCondition;
import marioAI.collision.ConditionDirectionPair.CollisionDirection;
import marioWorld.engine.PlayerWorldObject;

/**
 * VoiceCommand that represents a feedback by the voice input. Stores all information to process a feedback.
 * 
 * @author chantal
 *
 */
public class FeedbackCommand extends VoiceCommand{
	
	/*
	 * collision direction of the action to be given information about 
	 */
	private CollisionDirection collisionDirection = null;
	
	/*
	 * the effect to be given information about
	 */
	private Effect effect = null;
	
	/*
	 * Marios condition during the action the user gives information about
	 */
	private Condition condition = null;
	
	private double probability = 1.0;
	
	/**
	 * constructor. In case there is no information given about a needed parameter this enum parameter is set to undefined
	 * 
	 * @param collisionDirection
	 * @param target
	 * @param health
	 * @param actionEffect
	 * @param subject
	 * @param object
	 */
	public FeedbackCommand(CollisionDirection collisionDirection, PlayerWorldObject target, PlayerHealthCondition health, ActionEffect actionEffect, PlayerWorldObject subject, PlayerWorldObject object, double probability) 
	{
		super(CommandType.FEEDBACK);
		this.collisionDirection = collisionDirection;
		this.condition = new Condition(subject, object, health);
		this.effect = new Effect(actionEffect, target);
		this.probability=probability;
	}
	
	/**
	 * get method
	 * 
	 * @return
	 */
	public CollisionDirection getCollisionDirection(){
		return this.collisionDirection;
	}
	
	/**
	 * get method
	 * 
	 * @return
	 */
	public Condition getCondition()
	{
		return this.condition;
	}
	
	/**
	 * get method
	 * 
	 * @return
	 */
	public Effect getEffect(){
		return this.effect;
	}
	
	public double getProbability()
	{
		return probability;
	}
}
