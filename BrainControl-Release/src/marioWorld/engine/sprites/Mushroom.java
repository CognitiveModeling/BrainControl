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

public class Mushroom extends Sprite {
	private static float GROUND_INERTIA = 0.89f;
	private static float AIR_INERTIA = 0.89f;

	private float runTime;
	private boolean onGround = false;
	private boolean mayJump = false;
	private int jumpTime = 0;
	private float xJumpSpeed;
	private float yJumpSpeed;

	private static final int width = 4;
	private static final int height = 12;

	private LevelScene world;
	public int facing;

	public boolean avoidCliffs = false;
	private int life;

	public Mushroom(LevelScene world, int x, int y) {
		kind = KIND_WRENCH;
		
		renderer = new SpriteRenderer(this, Art.items, 0, 0);

		this.x = x;
		this.y = y;
		this.world = world;
		
//		sheet.xPicO = 8;
//		sheet.yPicO = 15;
//		wPic = hPic = 16;

		facing = 1;
		life = 0;
	}

	@Override
	public void collideCheck(Sprite other)
	{
		for(Player player : world.getPlayers())
		{
			float xPlayerD = player.x - x;
			float yPlayerD = player.y - y;
			if (xPlayerD > -16 && xPlayerD < 16) {
				if (yPlayerD > -height && yPlayerD < player.height) {
					player.getWrench();
					spriteContext.removeSprite(this);
				}
			}
		}
	}

	@Override
	public void move() {
		if (life < 9) {
			layer = 0;
			y--;
			life++;
			return;
		}
		float sideWaysSpeed = 1.75f;
		layer = 1;
		// float sideWaysSpeed = onGround ? 2.5f : 1.2f;

		if (xa > 2) {
			facing = 1;
		}
		if (xa < -2) {
			facing = -1;
		}

		xa = facing * sideWaysSpeed;

		mayJump = (onGround);

		renderer.properties().flipFrameX = facing == -1;

		runTime += (Math.abs(xa)) + 5;

		if (!move(xa, 0))
			facing = -facing;
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
				jumpTime = 0;
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

	@Override
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

	@Override
	public Mushroom cloneWithNewLevelScene(LevelScene ls) {
		Mushroom ret = (Mushroom) super.clone();
		ret.runTime = runTime;
		ret.onGround = onGround;
		ret.mayJump = mayJump;
		ret.jumpTime = jumpTime;
		ret.xJumpSpeed = xJumpSpeed;
		ret.yJumpSpeed = yJumpSpeed;
		ret.world = ls;
		ret.facing = facing;
		ret.avoidCliffs = avoidCliffs;
		ret.life = life;
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
}
