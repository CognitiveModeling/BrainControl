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
package marioWorld.engine.sprites;

import marioWorld.engine.Art;
import marioWorld.engine.LevelScene;
import marioWorld.engine.SpriteRenderProperties;
import marioWorld.engine.SpriteRenderer;

public class BulletWilly extends Sprite {
	int height;

	private LevelScene world;
	public int facing;

	public boolean avoidCliffs = false;
	public int anim;

	public boolean dead = false;
	private int deadTime = 0;

	public BulletWilly(LevelScene world, float x, float y, int dir) 
	{
		super.kind = KIND_BULLET_WILLY; // added by SK
		
		super.x = x;
		super.y = y;

		super.ya = -5;
		this.height = 12;
		this.world = world;
		this.facing = dir;		
		
//		sheet.xPicO = 8;
//		sheet.yPicO = 31;
//		sheet.wPic = 16;

		renderer = new SpriteRenderer(this, Art.enemies, 0, 5);
	}

	public void collideCheck() {
		if (dead)
			return;

		for(Player player : world.getPlayers())
		{
			float xPlayerD = player.x - x;
			float yPlayerD = player.y - y;
			if (xPlayerD > -16 && xPlayerD < 16) {
				if (yPlayerD > -height && yPlayerD < player.height) {
					if (player.ya > 0 && yPlayerD <= 0 && (!player.onGround || !player.wasOnGround)) {
						player.stomp(this);
						dead = true;
	
						xa = 0;
						ya = 1;
						deadTime = 100;
					} else {
						player.getHurt();
					}
				}
			}
		}
	}

	public void move() {
		if (deadTime > 0) {
			deadTime--;

			if (deadTime == 0) {
				deadTime = 1;
				for (int i = 0; i < 8; i++) {
					world.addSprite(new Sparkle((int) (x + Math.random() * 16 - 8) + 4, (int) (y - Math.random() * 8) + 4, (float) (Math.random() * 2 - 1),
							(float) Math.random() * -1, 0, 1, 5));
				}
				spriteContext.removeSprite(this);
			}

			x += xa;
			y += ya;
			ya *= 0.95;
			ya += 1;

			return;
		}

		float sideWaysSpeed = 4f;

		xa = facing * sideWaysSpeed;
		renderer.properties().flipFrameX = facing == -1;
		move(xa, 0);
	}

	private boolean move(float xa, float ya) {
		x += xa;
		return true;
	}

	public boolean fireballCollideCheck(Fireball fireball) {
		if (deadTime != 0)
			return false;

		float xD = fireball.x - x;
		float yD = fireball.y - y;

		if (xD > -16 && xD < 16) {
			if (yD > -height && yD < fireball.height) {
				return true;
			}
		}
		return false;
	}

	public boolean grumpyCollideCheck(Grumpy grumpy) {
		if (deadTime != 0)
			return false;

		float xD = grumpy.x - x;
		float yD = grumpy.y - y;

		if (xD > -16 && xD < 16) {
			if (yD > -height && yD < Grumpy.height) {
				dead = true;

				xa = 0;
				ya = 1;
				deadTime = 100;

				return true;
			}
		}
		return false;
	}

	/**
	 * This function clones a BulletBill while setting its world to null.
	 */
	@Override
	public BulletWilly cloneWithNewLevelScene(LevelScene ls) {
		BulletWilly ret = (BulletWilly) super.clone();
		ret.anim = anim;
		ret.height = height;
		ret.dead = dead;
		ret.avoidCliffs = avoidCliffs;
		ret.deadTime = deadTime;
		ret.world = ls;
		return ret;
	}

	@Override
	public boolean isFacingDefined() {
		return true;
	}

	@Override
	public int getFacing() {
		return facing;
	}

	@Override
	public boolean isDeadTimeDefined() {
		return true;
	}

	@Override
	public int getDeadTime() {
		return deadTime;
	}
}
