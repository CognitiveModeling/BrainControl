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

import marioAI.brain.Generalizer;
import marioAI.brain.simulation.sprites.SimulatedSprite;
import marioWorld.engine.PlayerWorldObject;

/**
 * Who interacts with whom
 * 
 * @author Stephan
 * 
 */
public class Interactors implements Comparable<Interactors>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2238969698638846540L;

	private final PlayerWorldObject actor;
	private final PlayerWorldObject target;

	public PlayerWorldObject getTarget()
	{
		return target;
	}

	public PlayerWorldObject getActor()
	{
		return actor;
	}

	public Interactors(PlayerWorldObject actor, PlayerWorldObject target) 
	{
		if(target==null)
			System.out.println("WARNING: target is null!!!");
		
		this.actor = actor;
		this.target = target;
	}

	@Override
	public int compareTo(Interactors other) {
		final int compareActors = this.actor.compareTo(other.actor);
		if (compareActors != 0)
			return compareActors;
		final int compareTargets = this.target.compareTo(other.target);
		return compareTargets;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + actor.hashCode();
		if (target != null) result = prime * result + target.hashCode();
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
		
		Interactors other = (Interactors) obj;

		if (Generalizer.generalizePlayerWorldObject(this.target) != Generalizer.generalizePlayerWorldObject(other.target))
			return false;
		
		if (this.actor != other.actor)
			return false;
		
		return true;
	}

	@Override
	public String toString() {
		return "actor: " + actor + ", target: " + target;
	}
	
	
}
