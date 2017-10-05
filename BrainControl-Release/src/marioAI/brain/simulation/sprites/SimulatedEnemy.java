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
package marioAI.brain.simulation.sprites;

import java.util.List;

import marioAI.brain.simulation.SimulatedLevelScene;
import marioAI.collision.CollisionAtPosition;
import marioWorld.engine.PlayerWorldObject;
import marioWorld.engine.sprites.Enemy;
import marioWorld.engine.sprites.Sprite;

public class SimulatedEnemy extends SimulatedSprite 
{
	private int deadTime = 0;
	
	public static PlayerWorldObject[][] map;

	@Override
	public int getDeadTime() 
	{
		return deadTime;
	}
	
	protected SimulatedEnemy(Sprite sprite) 
	{
		super(sprite);
		this.deadTime=sprite.getDeadTime();
	}

	@Override
	protected PlayerWorldObject inferType(Sprite sprite) {
		PlayerWorldObject ret = PlayerWorldObject.getElement(sprite);
		/**
		 * The engine encodes winged sprites weirdly: winged sprites who lose
		 * their wings remain winged sprites but have a boolean flag winged set
		 * to false. Catch this here.
		 */
		if (ret.isWinged()) {
			if (!((Enemy) sprite).winged) {
				ret = ret.loseWings();
			}
		}
		return ret;
	}

	/**
	 * @param levelSceneAdapter
	 * @param keys
	 *            not required for non-Marios
	 * @return implementation not required for non-Marios. This method always
	 *         returns false.
	 */
	@Override
	public List<CollisionAtPosition> move(PlayerWorldObject[][] map, boolean[] keys, SimulatedLevelScene scene) 
	{
		// if (deadTime > 0) {
		// deadTime--;
		//
		//
		// if (flyDeath) {
		// x += xa;
		// y += ya;
		// ya *= 0.95;
		// ya += 1;
		// }
		// return;
		// }

		
		if(!this.isAlive()){
			return null;
		}


		float sideWaysSpeed = 1.75f;
		if (type == PlayerWorldObject.GRUMPY_MOVING
				|| type == PlayerWorldObject.GRUMPY_STILL) {
			sideWaysSpeed = 11;
		}

		if (xa > 2) {
			facing = 1;
		}
		if (xa < -2) {
			facing = -1;
		}

		xa = facing * sideWaysSpeed;

		mayJump = (onGround);

		/** horizontal movement */
		move(xa, 0, map);
		onGround = false;

		/** vertical movement */
		move(0, ya, map);

		ya *= winged ? 0.95f : 0.85f;
		if (onGround) {
			xa *= GROUND_INERTIA;
		} else {
			xa *= AIR_INERTIA;
		}

		if (!onGround) {
			if (winged) {
				ya += 0.6f;
			} else {
				ya += 2;
			}
		} else if (winged) {
			ya = -10;
		}
		return null;
	}

	// @Override
	// public boolean grumpyCollideCheck(Grumpy grumpy) {
	// if (deadTime != 0)
	// return false;
	//
	// float xD = grumpy.x - x;
	// float yD = grumpy.y - y;
	//
	// if (xD > -16 && xD < 16) {
	// if (yD > -height && yD < Grumpy.height) {
	// xa = grumpy.facing * 2;
	// ya = -5;
	// flyDeath = true;
	// if (spriteTemplate != null)
	// spriteTemplate.isDead = true;
	// deadTime = 100;
	// winged = false;
	// hPic = -hPic;
	// yPicO = -yPicO + 16;
	// // System.out.println("grumpyCollideCheck");
	// ++LevelScene.killedCreaturesTotal;
	// ++LevelScene.killedCreaturesByGrumpy;
	// return true;
	// }
	// }
	// return false;
	// }

	// @Override
	// public boolean fireballCollideCheck(Fireball fireball) {
	// if (deadTime != 0)
	// return false;
	//
	// float xD = fireball.x - x;
	// float yD = fireball.y - y;
	//
	// if (xD > -16 && xD < 16) {
	// if (yD > -height && yD < fireball.height) {
	// if (noFireballDeath)
	// return true;
	//
	// xa = fireball.facing * 2;
	// ya = -5;
	// flyDeath = true;
	// if (spriteTemplate != null)
	// spriteTemplate.isDead = true;
	// deadTime = 100;
	// winged = false;
	// hPic = -hPic;
	// yPicO = -yPicO + 16;
	// // System.out.println("fireballCollideCheck");
	// ++LevelScene.killedCreaturesTotal;
	// ++LevelScene.killedCreaturesByFireBall;
	// return true;
	// }
	// }
	// return false;
	// }

	// @Override
	// public void bumpCheck(int xTile, int yTile) {
	// if (deadTime != 0)
	// return;
	//
	// if (x + width > xTile * 16 && x - width < xTile * 16 + 16
	// && yTile == (int) ((y - 1) / 16)) {
	// xa = -world.mario.facing * 2;
	// ya = -5;
	// flyDeath = true;
	// if (spriteTemplate != null)
	// spriteTemplate.isDead = true;
	// deadTime = 100;
	// winged = false;
	// hPic = -hPic;
	// yPicO = -yPicO + 16;
	// System.out.println("bumpCheck");
	// }
	// }
}
