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

import marioAI.brain.simulation.SimulatedLevelScene;
import marioAI.brain.simulation.sprites.SimulatedSprite;
import marioAI.collision.Condition.PlayerHealthCondition;
import marioAI.collision.ConditionDirectionPair.CollisionDirection;
import marioWorld.engine.PlayerWorldObject;
import marioWorld.engine.PlayerWorldObject.ObjectType;
import marioWorld.engine.coordinates.GlobalCoarse;

/**
 * Who interacts with whom in which condition, from which relative direction and at which position.
 */
public class CollisionAtPosition {

	/**
	 * Who interacts with whom in which condition and from which relative direction.
	 */
	public final ConditionDirectionPair conditionDirectionPair;

	/** Where the collision takes place. May be null. */
	private final GlobalCoarse position;

	/** May be null. */
	private SimulatedSprite targetSprite;
	private SimulatedSprite actorSprite;

	/**
	 * @param collision
	 * @param position
	 *            use, if a collision with a static object occurs
	 * @param targetSprite
	 *            use, if a collision with a moving object occurs
	 */
	public CollisionAtPosition(ConditionDirectionPair collision, GlobalCoarse position, SimulatedSprite actorSprite, SimulatedSprite targetSprite) 
	{
		if(position==null)
		{
			try
			{
				throw new Exception("don't create CollisionAtPositions without positions!");
			} 
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		this.conditionDirectionPair = collision;
		this.position = position;
		
		this.actorSprite = actorSprite;
		this.targetSprite = targetSprite;		
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		GlobalCoarse position = getPosition();
		result = prime * result + ((conditionDirectionPair == null) ? 0 : conditionDirectionPair.hashCode());
		result = prime * result + ((position == null) ? 0 : Float.floatToIntBits(position.x));
		result = prime * result + ((position == null) ? 0 : Float.floatToIntBits(position.y));
		return result;
	}

	/**
	 * if the target (still) exists in the world
	 * 
	 * @param scene
	 *            the world
	 * @return
	 */
	public boolean exists(SimulatedLevelScene scene) 
	{
		if (targetSprite != null)
			return scene.getSimulatedSprites().contains(targetSprite);
		PlayerWorldObject target = conditionDirectionPair.condition.interactors.getTarget();
		if (target != null) 
		{
			PlayerWorldObject mapEntry = scene.getGlobalStaticSimulation()[position.y][position.x];
			
			return mapEntry == target;
		}
		return true; // goals with only a position but no static/moving target
						// always exist.
	}

//	/**
//	 * 
//	 * @param setDirectionToUndefined
//	 * @param setPlayerHealthToUndefined
//	 * @param doDeepCloning
//	 *            quite some overhead. Use false if you don't need deep cloning.
//	 * @return
//	 */
//	public CollisionAtPosition clone(boolean setDirectionToUndefined, boolean setPlayerHealthToUndefined, boolean doDeepCloning) 
//	{
//		// modify and deep clone
//		CollisionDirection newDirection = setDirectionToUndefined ? CollisionDirection.IRRELEVANT : this.conditionDirectionPair.collisionDirection;
//		Condition newCondition = new Condition(this.conditionDirectionPair.condition.getActor(), this.conditionDirectionPair.condition.getTarget(),
//				setPlayerHealthToUndefined ? PlayerHealthCondition.IRRELEVANT : this.conditionDirectionPair.condition.playerHealthCondition);
//		// deep clone remaining if demanded
//		GlobalCoarse newPosition = this.position == null ? null : (doDeepCloning ? this.position.clone() : this.position);
//		SimulatedSprite newTargetSprite = this.targetSprite == null ? null : (doDeepCloning ? this.targetSprite.clone() : this.targetSprite);
//		SimulatedSprite newActorSprite = this.actorSprite == null ? null : (doDeepCloning ? this.actorSprite.clone() : this.actorSprite);
//		return new CollisionAtPosition(new ConditionDirectionPair(newCondition, newDirection), newPosition, newActorSprite, newTargetSprite);
//	}

//	/**
//	 * 
//	 * @return if the collision has a static tile as target. Else, it has a sprite as target
//	 */
//	public boolean hasStaticPosition() {
//		//check if everything is valid
//		if(position==null ^ sprite==null) {
//			return position!=null;
//		}else {
//			throw new IllegalArgumentException("Only position OR sprite should be null");
//		}
//	}
	
//	public GlobalCoarse getPositionWithSprite() {
////		if ((position == null) == (sprite == null))
////			throw new IllegalArgumentException("Either both position and sprite are set or none. Please use exactly one.");
//		if (position != null)
//			return position;
//		return sprite.getOriginalSpritePosition().toGlobalCoarse();
//	}

	public GlobalCoarse getPosition() {
		return position;
	}

	public SimulatedSprite getTargetSprite() 
	{
		return targetSprite;
	}

	public void setTargetSprite(SimulatedSprite set) {
		targetSprite = set;
	}	

	public SimulatedSprite getActorSprite() 
	{
		return actorSprite;
	}	
	
	public boolean equalPositionAs(CollisionAtPosition other) 
	{
		// one interaction
		// is with static
		// objects and one
		// with moving
		// objects
		if ((position == null) != (other.position == null)) 
			return false;
		if(position != null && other.position != null) {
			if(!position.equals(other.position))
				return false;
		}
		
		// unequal
		// static
		// position
		//if (position != null && (position.x != other.position.x || position.y != other.position.y)) 
		//	return false; //TODO: wieder auf return false setzen
		
		//TEST: this is disabled. should not be neccessary.
//		// one interaction is
//		// with static objects
//		// and one with moving
//		// objects
//		if ((sprite == null) != (other.sprite == null)) 
//			return false;
//		
//		// unequal
//		// moving
//		// sprites
//		if (sprite != null && !sprite.stemsFromSameEngineSprite(other.sprite))  
//			return false;
		
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		return equals(obj, false, false, false, false);
	}

	public boolean equals
		(
			Object obj, 
			boolean disregardThisPlayerHealthIfUndefined, 
			boolean disregardOtherPlayerHealthIfUndefined,
			boolean disregardThisDirectionIfUndefined, 
			boolean disregardOtherDirectionIfUndefined
		) 
	{
		if (this == obj)
			return true;
		
		if (obj == null)
			return false;
		
		if (getClass() != obj.getClass())
			return false;
		
		CollisionAtPosition other = (CollisionAtPosition) obj;
		
		if (conditionDirectionPair == null || other.conditionDirectionPair == null) 
		{
			System.out.println("CAP equals: condition null... this should not happen.");
			return false;
		} 
		else if
		(
				!conditionDirectionPair.equals
				(
						other.conditionDirectionPair, 
						disregardThisPlayerHealthIfUndefined, 
						disregardOtherPlayerHealthIfUndefined,
						disregardThisDirectionIfUndefined, 
						disregardOtherDirectionIfUndefined
				)
		)
		{
			//System.out.println("unequal cdps");
			return false;
		}
		
		return equalPositionAs(other);
	}

	@Override
	public String toString() 
	{
		String s = "collision: " + conditionDirectionPair.toString() + " ";
				
		s += "position: " + position.toString();
		
		if(targetSprite!=null)
			s += "sprite: " + targetSprite.toString();
		else
			s += "sprite: none";
			
		return s;
	}
	
	public PlayerHealthCondition getHealth(){
		return this.conditionDirectionPair.condition.playerHealthCondition;
	}
	
	public CollisionDirection getDirection(){
		return this.conditionDirectionPair.collisionDirection;
	}
}
