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

public class Fireball extends Sprite {
	private static float GROUND_INERTIA = 0.89f;
	private static float AIR_INERTIA = 0.89f;

	private float runTime;
	private boolean onGround = false;

	private int width = 4;
	int height = 24;

	private LevelScene world;
	public int facing;

	public boolean avoidCliffs = false;
	public int anim;

	public boolean dead = false;
	private int deadTime = 0;

	public Fireball(LevelScene world, float x, float y, int facing) {
		kind = KIND_FIREBALL;
		
		renderer = new SpriteRenderer(this, Art.particles, 3, 4);

		this.x = x;
		this.y = y;
		this.world = world;

		height = 8;
		this.facing = facing;
		ya = 4;
		
//		wPic = 8;
//		hPic = 8;
//		xPicO = 4;
//		yPicO = 4;
	}

	@Override
	public void move() {
		if (deadTime > 0) {
			for (int i = 0; i < 8; i++) {
				world.addSprite(new Sparkle((int) (x + Math.random() * 8 - 4) + 4, (int) (y + Math.random() * 8 - 4) + 2, (float) Math.random() * 2 - 1
						- facing, (float) Math.random() * 2 - 1, 0, 1, 5));
			}
			spriteContext.removeSprite(this);

			return;
		}

		if (facing != 0)
			anim++;

		float sideWaysSpeed = 8f;
		// float sideWaysSpeed = onGround ? 2.5f : 1.2f;

		if (xa > 2) {
			facing = 1;
		}
		if (xa < -2) {
			facing = -1;
		}

		xa = facing * sideWaysSpeed;

		world.checkFireballCollide(this);

		renderer.properties().flipFrameX = facing == -1;

		runTime += (Math.abs(xa)) + 5;

		renderer.properties().selectedFrameX = (anim) % 4;

		if (!move(xa, 0)) {
			die();
		}

		onGround = false;
		move(0, ya);
		if (onGround)
			ya = -10;

		ya *= 0.95f;
		if (onGround) {
			xa *= GROUND_INERTIA;
		} else {
			xa *= AIR_INERTIA;
		}

		if (!onGround) {
			ya += 1.5;
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

		return blocking;
	}

	public void die() {
		dead = true;

		xa = -facing * 2;
		ya = -5;
		deadTime = 100;
	}

	@Override
	public Fireball cloneWithNewLevelScene(LevelScene ls) {
		Fireball ret = (Fireball) super.clone();
		ret.anim = anim;
		ret.avoidCliffs = avoidCliffs;
		ret.dead = dead;
		ret.deadTime = deadTime;
		ret.facing = facing;
		ret.height = height;
		ret.onGround = onGround;
		ret.runTime = runTime;
		ret.width = width;
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
