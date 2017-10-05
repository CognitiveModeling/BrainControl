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
import marioAI.collision.Condition;
import marioAI.collision.Condition.PlayerHealthCondition;
import marioAI.collision.ConditionDirectionPair.CollisionDirection;
import marioWorld.engine.PlayerWorldObject;

/**
 * VoiceCommand that stores all information a knowledge request contains.
 * 
 * @author chantal
 *
 */
public class KnowledgeCommand extends VoiceCommand {

	private PlayerWorldObject actor;
	/*
	 * direction
	 */
	private CollisionDirection collisionDirection;

	/*
	 * target object and healthConditioned
	 */
	private Condition condition;

	/*
	 * Effect
	 */
	private Effect effect;

	/**
	 * constructor. In case not all needed information for concrete query of
	 * knowledge is given, the VoiceInterpreter sets those information to
	 * default.
	 * 
	 * @param healthCondition
	 * @param object
	 * @param direction
	 */
	public KnowledgeCommand(PlayerHealthCondition healthCondition, PlayerWorldObject object,
			CollisionDirection direction) {
		super(CommandType.KNOWLEDGE);
		this.collisionDirection = direction;
		this.condition = new Condition(PlayerWorldObject.PLAYER, object, healthCondition);
	}

	public KnowledgeCommand(PlayerHealthCondition healthCondition, PlayerWorldObject actor, PlayerWorldObject object,
			CollisionDirection direction) {
		super(CommandType.KNOWLEDGE);
		this.collisionDirection = direction;
		this.actor = actor;
		// change Player into current actor, because needed for Knowledge
		this.condition = new Condition(actor, object, healthCondition);
		
	}

	public KnowledgeCommand(Effect effect) {
		super(CommandType.KNOWLEDGE);
		this.effect = effect;
	}

	/**
	 * get method
	 * 
	 * @return
	 */
	public CollisionDirection getCollisionDirection() {
		return this.collisionDirection;
	}

	/**
	 * get method
	 * 
	 * @return
	 */
	public Condition getCondition() {
		return this.condition;
	}

	/**
	 * getter
	 */
	public Effect getEffect() {
		return effect;
	}
}
