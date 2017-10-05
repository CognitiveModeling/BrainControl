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
import marioWorld.engine.sprites.Player;
import marioWorld.engine.sprites.Sprite;

/**
 * Who interacts with whom in which condition
 * 
 * @author Ylva
 */
public class Condition implements Comparable<Condition>, Serializable {

	private static final long serialVersionUID = -981779315538417205L;

	/** Who interacts with whom */
	public final Interactors interactors;

	/** Mario's condition */
	public PlayerHealthCondition playerHealthCondition;

	public Condition(PlayerWorldObject actor, PlayerWorldObject target, PlayerHealthCondition playerHealthCondition) 
	{
		super();
		this.interactors = new Interactors(actor, Generalizer.generalizePlayerWorldObject(target));																									
		this.playerHealthCondition = playerHealthCondition;
	}

	public Condition(PlayerWorldObject actor, PlayerWorldObject target, int playerHealth, boolean wasInvulnerable, boolean isPlayer) 
	{
		this(actor, target, PlayerHealthCondition.fromInt(playerHealth, wasInvulnerable, !isPlayer));
	}

	public boolean equalAgentStateAs(Condition other) {
		return (this.playerHealthCondition == other.playerHealthCondition && this.getActor() == other.getActor());
	}

	public PlayerWorldObject getActor() {
		return interactors.getActor();
	}

	public PlayerWorldObject getTarget() {
		return interactors.getTarget();
	}

	public int getPlayerHealth() {
		return playerHealthCondition.toInt();
	}

	public PlayerHealthCondition getPlayerHealthCondition() {
		return playerHealthCondition;
	}

	public boolean getWasInvulnerable() {
		return playerHealthCondition == PlayerHealthCondition.INVULNERABLE;
	}

	public boolean isPlayer() {
		return interactors.getActor().isPlayer();
	}

	@Override
	public int compareTo(Condition other) {
		final int compareInteractors = this.interactors.compareTo(other.interactors);
		if (compareInteractors != 0)
			return compareInteractors;
		return this.getPlayerHealthCondition().compareTo(other.getPlayerHealthCondition());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = interactors.hashCode();
		result = prime * result + getPlayerHealth();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		return equals(obj, false, false);
	}

	public boolean equals(Object obj, boolean disregardThisPlayerHealthIfUndefined,
			boolean disregardOtherPlayerHealthIfUndefined) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Condition other = (Condition) obj;
		
		if (!this.interactors.equals(other.interactors))
			return false;
		
		if (playerHealthCondition.equals(other.playerHealthCondition, disregardThisPlayerHealthIfUndefined,
				disregardOtherPlayerHealthIfUndefined))
			return true;
		
		return false;
	}

	@Override
	public String toString() {
		return interactors + ", playerHealth: " + getPlayerHealth();
	}

	/**
	 * UNDEFINED = -1, SMALL = 0, LARGE_NON_BULB = 1, BULB = 2, INVULNERABLE =
	 * 3, as mapped by toInt() and fromInt()
	 */
	public static enum PlayerHealthCondition {
		IRRELEVANT("undefined"), SMALL("small"), LARGE_NON_BULB("large"), BULB("bulb"), INVULNERABLE("invulnerable");

		public final String guiString;

		private PlayerHealthCondition(String guiString) {
			this.guiString = guiString;
		}

		public int toInt() {
			switch(this) {
			case IRRELEVANT:
				return -1;
			case SMALL:
				return 0;
			case LARGE_NON_BULB:
				return Player.healthLevelScaling;
			case BULB:
				return Player.healthLevelScaling*2;
			case INVULNERABLE:
				return Player.healthLevelScaling*2 +1;
			}
			return this.ordinal() - 1;
		}

		public boolean equals(PlayerHealthCondition other, boolean disregardThisIfUndefined,
				boolean disregardOtherIfUndefined) {
			return (this.equals(other) || (disregardThisIfUndefined && this == IRRELEVANT)
					|| (disregardOtherIfUndefined && other == IRRELEVANT));
		}

		public static PlayerHealthCondition fromInt(int playerHealth, boolean invulnerable, boolean undefined) {
			if (undefined) {
				return IRRELEVANT;
			}
			if (invulnerable) {
				return INVULNERABLE;
			}
			if (playerHealth == 0) {
				return SMALL;
			}
			if(playerHealth <= Player.healthLevelScaling) {
				return LARGE_NON_BULB;
			}
			if(playerHealth <=Player.healthLevelScaling *2) {
				return BULB;
			}
			return PlayerHealthCondition.values()[playerHealth + 1];
		}

	}// end of enum MarioHealthCondition

	public static class carryCondition {
		Sprite target_carries;
		Sprite target_carriedBy;
		Sprite actor_carries;
		Sprite actor_carriedBy;

		@Override
		public String toString() {
			String ret = "";

			if (target_carries != null)
				ret += "target_carries ";
			if (target_carriedBy != null)
				ret += "target_carriedBy ";
			if (actor_carries != null)
				ret += "actor_carries ";
			if (actor_carriedBy != null)
				ret += "actor_carriedBy ";

			return ret;
		}
	};
}
