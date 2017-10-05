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
package marioAI.collision;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import marioWorld.engine.PlayerWorldObject;

/**
 * Who interacts with whom in which condition and from which relative direction.
 * 
 * @author Ylva, modified.
 */
public final class ConditionDirectionPair implements Comparable<ConditionDirectionPair>, Serializable {

	private static final long serialVersionUID = -1892039864620357923L;

	/** Who interacts with whom in which condition */
	public Condition condition;

	/** the relative direction of the interaction. */
	public CollisionDirection collisionDirection;
	
	public int interactionCount;

	public ConditionDirectionPair(Condition condition, CollisionDirection action) {
		super();
		this.condition = condition;
		this.collisionDirection = action;
		interactionCount=0;	//only used for knowledge entries. not im comparator!
	}

	/**
	 * Returns a new ConditionDirectionPair with the same values
	 * 
	 * @return ConditionDirectionPair
	 */
	@Override
	public ConditionDirectionPair clone() {
		return new ConditionDirectionPair(condition, collisionDirection);
	}

	// /** Returns the first element of the ConditionDirectionPair, a Condition
	// *
	// * @return Condition */
	// public Condition getCondition() {
	// return condition;
	// }
	//
	// /** Returns the second element of the ConditionDirectionPair, a direction
	// *
	// * @return Action */
	// public CollisionDirection getCollisionDirection() {
	// return collisionDirection;
	// }

	public boolean equalConditionAs(ConditionDirectionPair other) {
		return (this.condition.compareTo(other.condition) == 0);
	}

	@Override
	public int compareTo(ConditionDirectionPair other) {
		int compareCondition = this.condition.compareTo(other.condition);
		if (compareCondition != 0)
			return compareCondition;
		return collisionDirection.compareTo(other.collisionDirection);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((collisionDirection == null) ? 0 : collisionDirection.hashCode());
		result = prime * result + ((condition == null) ? 0 : condition.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		return equals(obj, false, false, false, false);
	}

	public boolean equals(Object obj, boolean disregardThisPlayerHealthIfUndefined, boolean disregardOtherPlayerHealthIfUndefined,
			boolean disregardThisDirectionIfUndefined, boolean disregardOtherDirectionIfUndefined) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConditionDirectionPair other = (ConditionDirectionPair) obj;
		
		if (collisionDirection == null) 
		{
			if (other.collisionDirection != null)
				return false;
		} 
		else if (!collisionDirection.equals(other.collisionDirection, disregardThisDirectionIfUndefined, disregardOtherDirectionIfUndefined))
		{
			//System.out.println("unequal collisions");
			return false;
		}
			
		if (condition == null) 
		{
			if (other.condition != null)
				return false;
		} 
		else if (!condition.equals(other.condition, disregardThisPlayerHealthIfUndefined, disregardOtherPlayerHealthIfUndefined))
		{
			//System.out.println("unequal conditions");
			return false;
		}
		
		return true;
	}

	public String toString() {
		return this.condition.toString() + ", direction: from " + this.collisionDirection.name();
	}

	public ConditionDirectionPair changeActor(PlayerWorldObject newActor){
		return new ConditionDirectionPair(new Condition(newActor, this.condition.getTarget(), this.condition.playerHealthCondition), 
											this.collisionDirection);
	}
	
	/**
	 * Describes the direction of a collision.
	 * 
	 * @author sebastian, modified
	 */
	public static enum CollisionDirection 
	{
		ABOVE, BELOW, LEFT, RIGHT, IRRELEVANT;
		
		private static final List<CollisionDirection> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
		private static final int SIZE = VALUES.size();
		private static final Random RANDOM = new Random();

		public static CollisionDirection randomDirection()
		{
			return VALUES.get(RANDOM.nextInt(SIZE-1));
		}	
		
		public boolean equals(CollisionDirection other, boolean disregardThisIfUndefined, boolean disregardOtherIfUndefined) 
		{
			return (this.equals(other) || (disregardThisIfUndefined && this == IRRELEVANT) || (disregardOtherIfUndefined && other == IRRELEVANT));
		}
	}
}
