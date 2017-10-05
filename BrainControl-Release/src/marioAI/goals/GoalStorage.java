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

import marioAI.brain.simulation.sprites.SimulatedSprite;
import marioAI.collision.ConditionDirectionPair.CollisionDirection;
import marioWorld.engine.PlayerWorldObject;
import marioWorld.engine.coordinates.GlobalCoarse;

/**
 * This class is a wrapper class around Goal with a different implementation of hashCode and equals. The main problem is that equals and hashCode should be
 * rather universal but some classes need very specific (i.e., non-universal) implementations of those methods. Instead of polluting the original Goal class
 * with those implementation, this class should be used when a specific hashCode implementation which abstracts away details of the underlying SimulatedSprites
 * is needed.
 * 
 * @author sebastian
 * 
 */
public class GoalStorage {
	private Goal goal;

	public GoalStorage(Goal g) {
		super();
		this.goal = g;
	}

	public Goal getGoal() {
		return goal;
	}

	@Override
	public String toString() {
		return goal.toString();
	}

	@Override
	public int hashCode() {
		CollisionDirection direction = goal.getDirection();
		GlobalCoarse position = goal.getPosition();
		SimulatedSprite sprite = goal.getTargetSprite();
		PlayerWorldObject target = goal.getTarget();
//		Boolean intermediate = goal.isIntermediate();
		final int prime = 31;
		int result = 1;
//		result = prime * result + ((!intermediate) ? 0 : intermediate.hashCode());
		result = prime * result + ((sprite == null) ? 0 : sprite.customHashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
		result = prime * result + ((position == null) ? 0 : (position.getX() * 7) + (position.getY() * 11));
		result = prime * result + ((direction == null) ? 0 : direction.hashCode());

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		CollisionDirection direction = goal.getDirection();
		GlobalCoarse position = goal.getPosition();
		SimulatedSprite sprite = goal.getTargetSprite();
		PlayerWorldObject target = goal.getTarget();
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		GoalStorage o = (GoalStorage) obj;
		Goal other = o.getGoal();
		if (direction != other.getDirection()) {
			return false;
		}
		if (position == null) {
			if (other.getPosition() != null)
				return false;
		} else if (position.getX() != other.getPosition().getX() || position.getY() != other.getPosition().getY()) {
			
			return false;
		}
		if (sprite == null) {
			if (other.getTargetSprite() != null)
				return false;
		} else if (!sprite.stemsFromSameEngineSprite(other.getTargetSprite())) {
			return false;
		}
		if (target != other.getTarget()) {
			return false;
		}
		return true;
	}
}
