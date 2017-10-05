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
package marioAI.brain;

import java.io.Serializable;

import marioAI.brain.Effect.ActionEffect;
import marioWorld.engine.PlayerWorldObject;

/**
 * Base class for all possible effects. Each effect is assumed to have two basic properties: a type (see ActionEffect enumeration) and a target
 * 
 * @author sebastian
 */
public class Effect implements Serializable, Comparable<Effect> {

	private static final long serialVersionUID = 2545467977627249884L;
	private final ActionEffect type;
	private final PlayerWorldObject effectTarget;

	/**
	 * Constructs a new Effect with the given ActionEffect type and target.
	 * 
	 * @param type
	 * @param target
	 */
	public Effect(ActionEffect type, PlayerWorldObject target) 
	{		
		this.type = type;	
		this.effectTarget = target;
	}

	/**
	 * Returns the type of this action.
	 * 
	 * @return
	 */
	public ActionEffect getType() {
		return type;
	}

	/**
	 * Returns the target of this action.
	 * 
	 * @return
	 */
	public PlayerWorldObject getEffectTarget() {
		return effectTarget;
	}

	/**
	 * Enumeration of all possible effects.
	 * 
	 * @author sebastian
	 * 
	 */
	public enum ActionEffect implements Serializable {
		NOTHING_HAPPENS, //this actually means "rebound" ... which is the block behavior expected per default
		ENERGY_INCREASE, //
		ENERGY_DECREASE, //
		OBJECT_CREATION, //
		OBJECT_DESTRUCTION, //
		OBJECT_HEALTH_DECREASE, //
		OBJECT_HEALTH_INCREASE, //
		OBJECT_SPEED_CHANGE, //
		OBJECT_STABILITY, //
		MOUNT,	//mount another player to get carried by him
		UNMOUNT,//unmount another player
		CARRY,	//actively carry a player
		DROP, //drop currently carried player
		TRANSPORTATION,
		PROGRESS,
		UNDEFINED  
	}
	
	/**
	 * Checks if the two input effect are contradicting each other
	 * @param effect1
	 * @param effect2
	 * @return
	 */
	public static boolean contradictingEffects(Effect effect1, Effect effect2){
		switch(effect1.getType()){
			case MOUNT:
				return effect2.getType().equals(ActionEffect.UNMOUNT);
			case UNMOUNT:
				return effect2.getType().equals(ActionEffect.MOUNT);
			case CARRY:
				return effect2.getType().equals(ActionEffect.DROP);
			case DROP:
				return effect2.getType().equals(ActionEffect.CARRY);

			default:
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
		String effectType = type.name();
		String targetName = effectTarget.name();
		ret.append(effectType);
		if (!targetName.equals("NONE")) {
			ret.append('(');
			ret.append(effectTarget);
			ret.append(')');
		}
		return ret.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((effectTarget == null) ? 0 : effectTarget.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
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
		
		Effect other = (Effect) obj;
		
		if (effectTarget != other.effectTarget)
			return false;
		
		if (type != other.type)
			return false;
		
		return true;
	}

	public Effect changeTarget (PlayerWorldObject newTarget){
		return new Effect(this.type, newTarget);
	}
	
	@Override
	public int compareTo(Effect o) {
		return hashCode() - o.hashCode();
	}

	public Effect clone() {
		return new Effect(this.type, this.effectTarget);
	}

}
