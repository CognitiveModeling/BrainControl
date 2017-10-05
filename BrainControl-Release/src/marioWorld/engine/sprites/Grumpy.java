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
import marioWorld.engine.SpriteAnimation;
import marioWorld.engine.SpriteAnimationType;
import marioWorld.engine.SpriteRenderProperties;
import marioWorld.engine.SpriteRenderer;

public class Grumpy extends Sprite {
	private static float GROUND_INERTIA = 0.89f;
	private static float AIR_INERTIA = 0.89f;

	public static final int width = 4;
	public static final int height = 12;

	// private float runTime;
	private boolean onGround = false;

	private LevelScene world;
	public int facing;

	public boolean avoidCliffs = false;
	public int anim;

	public boolean dead = false;
	private int deadTime = 0;
	//public boolean carried;

	public Grumpy(LevelScene world, float x, float y, int type) {
		kind = KIND_GRUMPY;
				
		renderer = new SpriteRenderer(this, Art.enemies, 4, type);  

		this.x = x;
		this.y = y;
		this.world = world;

		ya = -5;
		facing = 0;
				
//		sheet.xPicO = 8;
//		sheet.yPicO = 31;
//		wPic = 16;
	}

	public boolean fireballCollideCheck(Fireball fireball) {
		if (deadTime != 0)
			return false;

		float xD = fireball.x - x;
		float yD = fireball.y - y;

		if (xD > -16 && xD < 16) {
			if (yD > -height && yD < fireball.height) {
				if (facing != 0)
					return true;

				xa = fireball.facing * 2;
				ya = -5;
				if (spriteTemplate != null)
					spriteTemplate.isDead = true;
				deadTime = 100;
				renderer.properties().renderHeight = -renderer.properties().renderHeight;
				renderer.properties().renderFORy = -renderer.properties().renderFORy + 16;
				return true;
			}
		}
		return false;
	}

	@Override
	public void collideCheck(Sprite other)
	{
		if ((this.getCarriedBy() == null) || dead || deadTime > 0)
			return;

		for(Player player : world.getPlayers())
		{
			float xPlayerD = player.x - x;
			float yPlayerD = player.y - y;
			if (xPlayerD > -16 && xPlayerD < 16) {
				if (yPlayerD > -height && yPlayerD < player.height) {
					if (player.ya > 0 && yPlayerD <= 0 && (!player.onGround || !player.wasOnGround)) {
						player.stomp(this);
						if (facing != 0) {
							xa = 0;
							facing = 0;
						} else {
							facing = player.facing;
						}
					} else {
						if (facing != 0) {
							player.getHurt();
						} else {
							player.kick(this);
							facing = player.facing;
						}
					}
				}
			}
		}
	}

	public void move() {
		if (this.getCarriedBy() == null) {
			world.checkGrumpyCollide(this);
			return;
		}

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

		if (facing != 0)
			anim++;

		float sideWaysSpeed = 11f;
		// float sideWaysSpeed = onGround ? 2.5f : 1.2f;

		if (xa > 2) {
			facing = 1;
		}
		if (xa < -2) {
			facing = -1;
		}

		xa = facing * sideWaysSpeed;

		if (facing != 0) {
			world.checkGrumpyCollide(this);
		}

		renderer.properties().flipFrameX = facing == -1;

		// runTime += (Math.abs(xa)) + 5;

		renderer.properties().selectedFrameX = (anim / 2) % 4 + 3;
		

		if (!move(xa, 0)) {
			facing = -facing;
		}
		onGround = false;
		move(0, ya);

		ya *= 0.85f;
		if (onGround) {
			xa *= GROUND_INERTIA;
		} else {
			xa *= AIR_INERTIA;
		}

		if (!onGround) {
			ya += 2;
		}
	}

	private boolean move(float xa, float ya) {
		while (xa > 8) {
			if (!move(8, 0))
				return false;
			xa -= 8;
		}
		while (xa < -8) {
			if (!move(-8, 0))
				return false;
			xa += 8;
		}
		while (ya > 8) {
			if (!move(0, 8))
				return false;
			ya -= 8;
		}
		while (ya < -8) {
			if (!move(0, -8))
				return false;
			ya += 8;
		}

		boolean collide = false;
		if (ya > 0) {
			if (isBlocking(x + xa - width, y + ya, xa, 0))
				collide = true;
			else if (isBlocking(x + xa + width, y + ya, xa, 0))
				collide = true;
			else if (isBlocking(x + xa - width, y + ya + 1, xa, ya))
				collide = true;
			else if (isBlocking(x + xa + width, y + ya + 1, xa, ya))
				collide = true;
		}
		if (ya < 0) {
			if (isBlocking(x + xa, y + ya - height, xa, ya))
				collide = true;
			else if (collide || isBlocking(x + xa - width, y + ya - height, xa, ya))
				collide = true;
			else if (collide || isBlocking(x + xa + width, y + ya - height, xa, ya))
				collide = true;
		}
		if (xa > 0) {
			if (isBlocking(x + xa + width, y + ya - height, xa, ya))
				collide = true;
			if (isBlocking(x + xa + width, y + ya - height / 2, xa, ya))
				collide = true;
			if (isBlocking(x + xa + width, y + ya, xa, ya))
				collide = true;

			if (avoidCliffs && onGround && !world.level.isBlocking((int) ((x + xa + width) / 16), (int) ((y) / 16 + 1), xa, 1))
				collide = true;
		}
		if (xa < 0) {
			if (isBlocking(x + xa - width, y + ya - height, xa, ya))
				collide = true;
			if (isBlocking(x + xa - width, y + ya - height / 2, xa, ya))
				collide = true;
			if (isBlocking(x + xa - width, y + ya, xa, ya))
				collide = true;

			if (avoidCliffs && onGround && !world.level.isBlocking((int) ((x + xa - width) / 16), (int) ((y) / 16 + 1), xa, 1))
				collide = true;
		}

		if (collide) {
			if (xa < 0) {
				x = (int) ((x - width) / 16) * 16 + width;
				this.xa = 0;
			}
			if (xa > 0) {
				x = (int) ((x + width) / 16 + 1) * 16 - width - 1;
				this.xa = 0;
			}
			if (ya < 0) {
				y = (int) ((y - height) / 16) * 16 + height;
				this.ya = 0;
			}
			if (ya > 0) {
				y = (int) (y / 16 + 1) * 16 - 1;
				onGround = true;
			}
			return false;
		} else {
			x += xa;
			y += ya;
			return true;
		}
	}

	private boolean isBlocking(float _x, float _y, float xa, float ya) {
		int x = (int) (_x / 16);
		int y = (int) (_y / 16);
		if (x == (int) (this.x / 16) && y == (int) (this.y / 16))
			return false;

		boolean blocking = world.level.isBlocking(x, y, xa, ya);

		for(Player player : world.getPlayers())
		{
			if (blocking && ya == 0 && xa != 0)
			{
				world.bump(x, y, true, player);
			}
		}

		return blocking;
	}

	public void bumpCheck(int xTile, int yTile) 
	{
		for(Player player : world.getPlayers())
		{
			if (x + width > xTile * 16 && x - width < xTile * 16 + 16 && yTile == (int) ((y - 1) / 16)) {
				facing = -player.facing;
				ya = -10;
			}
		}
	}

	public void die() {
		dead = true;

		this.setCarriedBy(null);

		xa = -facing * 2;
		ya = -5;
		deadTime = 100;
	}

	public boolean grumpyCollideCheck(Grumpy grumpy) {
		if (deadTime != 0)
			return false;

		float xD = grumpy.x - x;
		float yD = grumpy.y - y;

		for(Player player : world.getPlayers())
		{
			if (xD > -16 && xD < 16) {
				if (yD > -height && yD < Grumpy.height) {
					if (player.getCarries() == grumpy || player.getCarries() == this) {
						player.setCarries(null);
					}
	
					die();
					grumpy.die();
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void release(Player player) {
		this.setCarriedBy(null);
		facing = player.facing;
		x += facing * 8;
	}

	@Override
	public Grumpy cloneWithNewLevelScene(LevelScene ls) {
		Grumpy ret = (Grumpy) super.clone();
		ret.onGround = onGround;
		ret.world = ls;
		ret.facing = facing;
		ret.avoidCliffs = avoidCliffs;
		ret.anim = anim;
		ret.dead = dead;
		ret.deadTime = deadTime;
		ret.setCarriedBy(this.getCarriedBy());
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
	public boolean isOnGroundDefined() {
		return true;
	}

	@Override
	public boolean isOnGround() {
		return onGround;
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
