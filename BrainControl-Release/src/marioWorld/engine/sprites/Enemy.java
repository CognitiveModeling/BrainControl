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

import java.lang.reflect.Field;
import java.util.HashMap;

import marioWorld.engine.Art;
import marioWorld.engine.LevelScene;
import marioWorld.engine.SpriteAnimation;
import marioWorld.engine.SpriteAnimationType;
import marioWorld.engine.SpriteRenderer;
import marioWorld.engine.coordinates.GlobalCoarse;
import marioWorld.engine.coordinates.GlobalContinuous;

public class Enemy extends Sprite {
	/*
	public static final int ENEMY_RED_KOOPA = 0;
	public static final int ENEMY_GREEN_KOOPA = 1;
	*/
	public static final int ENEMY_RED_VIRUS = 0;
	//public static final int ENEMY_GREEN_VIRUS = 1;
	//public static final int ENEMY_LUKO = 2;
	public static final int ENEMY_GREEN_VIRUS = 1;
	public static final int ENEMY_GRUMPY = 2;
	public static final int ENEMY_SHALLY = 3;
	public static final int ENEMY_FLOWER = 4;

	private static float GROUND_INERTIA = 0.89f;
	private static float AIR_INERTIA = 0.89f;

	private float runTime;
	private boolean onGround = false;
	// private boolean mayJump = false;
	// private int jumpTime = 0;
	// private float xJumpSpeed;
	// private float yJumpSpeed;

	int width = 4;
	int height = 24;

	private LevelScene world;
	public int facing;
	public int deadTime = 0;
	public boolean flyDeath = false;

	public boolean avoidCliffs = true;
	public int type;

	public boolean winged = true;
	public int wingTime = 0;

	public boolean noFireballDeath;

	public Enemy(LevelScene world, GlobalContinuous position, GlobalCoarse mapPosition, int dir, int type, boolean winged) {
		
		//System.out.println("NEW ENEMY!");
		
		byte k = KIND_UNDEF;
		switch (type) {
		//case ENEMY_RED_KOOPA:
		case ENEMY_RED_VIRUS:
			k = (byte) (4 + ((winged) ? 1 : 0));
			break;
		// case ENEMY_GREEN_KOOPA:
		//case ENEMY_GREEN_VIRUS:
		case ENEMY_GRUMPY:
			k = (byte) (6 + ((winged) ? 1 : 0));
			break;
		//case ENEMY_LUKO:
		case ENEMY_GREEN_VIRUS:
			k = (byte) (2 + ((winged) ? 1 : 0));
			break;
		case ENEMY_FLOWER:
			k = (byte) (11);
			break;
		case ENEMY_SHALLY:
			k = (byte) (9 + ((winged) ? 1 : 0));
		}
				
		renderer = new SpriteRenderer(this, Art.enemies, 0, type);
		
		kind = k;
		this.type = type;
		this.winged = winged;

		this.x = position.x;
		this.y = position.y;
		this.mapX = mapPosition.x;
		this.mapY = mapPosition.y;

		this.world = world;

		//avoidCliffs = type == Enemy.ENEMY_RED_KOOPA;
		avoidCliffs = type == Enemy.ENEMY_RED_VIRUS;

		noFireballDeath = type == Enemy.ENEMY_SHALLY;

		if (renderer.properties().selectedFrameY > 1)
			height = 12;
		
		facing = dir;		
		if (facing == 0)
			facing = 1;		
		
//		sheet.xPicO = 8;
//		sheet.yPicO = 31;		
//		sheet.wPic = 16;
	}

	@Override
	public void collideCheck(Sprite other) {
		if (deadTime != 0) {
			return;
		}

		for(Player player : world.getPlayers())
		{
			float xPlayerD = player.x - x;
			float yPlayerD = player.y - y;
			if (xPlayerD > -width * 2 - 4 && xPlayerD < width * 2 + 4) {
				if (yPlayerD > -height && yPlayerD < player.height) {
					if (type != Enemy.ENEMY_SHALLY && player.ya > 0 && yPlayerD <= 0 && (!player.onGround || !player.wasOnGround)) {
						player.stomp(this);
						if (winged) {
							winged = false;
							ya = 0;
						} else {
							renderer.properties().renderFORy = 31 - (32 - 8);
							renderer.properties().renderHeight = 8;
							if (spriteTemplate != null)
								spriteTemplate.isDead = true;
							deadTime = 10;
							winged = false;
	
							//if (type == Enemy.ENEMY_RED_KOOPA) {
							if (type == Enemy.ENEMY_RED_VIRUS) {
								spriteContext.addSprite(new Grumpy(world, x, y, 0));
							//} else if (type == Enemy.ENEMY_GREEN_KOOPA) {
							} //else if (type == Enemy.ENEMY_GREEN_VIRUS) {
							else if (type == Enemy.ENEMY_GRUMPY) {
								spriteContext.addSprite(new Grumpy(world, x, y, 1));
							}
							// System.out.println("collideCheck and stomp");
							++LevelScene.killedCreaturesTotal;
							++LevelScene.killedCreaturesByStomp;
						}
					} else {
						player.getHurt();
					}
				}
			}
		}
	}

	@Override
	public void move() 
	{
		wingTime++;
		if (deadTime > 0) 
		{
			deadTime--;

			if (deadTime == 0) 
			{
				deadTime = 1;
				for (int i = 0; i < 8; i++) 
				{
					world.addSprite(new Sparkle((int) (x + Math.random() * 16 - 8) + 4, (int) (y - Math.random() * 8) + 4, (float) (Math.random() * 2 - 1),
							(float) Math.random() * -1, 0, 1, 5));
				}
				spriteContext.removeSprite(this);
			}

			if (flyDeath) 
			{
				x += xa;
				y += ya;
				ya *= 0.95;
				ya += 1;
			}
			return;
		}

		float sideWaysSpeed = 1.75f;
		// float sideWaysSpeed = onGround ? 2.5f : 1.2f;

		if (xa > 2) {
			facing = 1;
		}
		if (xa < -2) {
			facing = -1;
		}

		xa = facing * sideWaysSpeed;

		// mayJump = (onGround);

		renderer.properties().flipFrameX = facing == -1;

		runTime += (Math.abs(xa)) + 5;

		int runFrame = ((int) (runTime / 20)) % 2;

		if (!onGround) {
			runFrame = 1;
		}

		if (!move(xa, 0))
			facing = -facing;
		onGround = false;
		move(0, ya);

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

		if (winged)
			runFrame = wingTime / 4 % 2;

		//renderer.properties().selectedFrameX = runFrame;
		SpriteAnimation animation = new SpriteAnimation(renderer, SpriteAnimationType.values()[type], true, 2);
		renderer.setAnimation(animation);
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
				// jumpTime = 0;
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
	public boolean grumpyCollideCheck(Grumpy grumpy) {
		if (deadTime != 0)
			return false;

		float xD = grumpy.x - x;
		float yD = grumpy.y - y;

		if (xD > -16 && xD < 16) {
			if (yD > -height && yD < Grumpy.height) {
				xa = grumpy.facing * 2;
				ya = -5;
				flyDeath = true;
				if (spriteTemplate != null)
					spriteTemplate.isDead = true;
				deadTime = 100;
				winged = false;
				renderer.properties().renderHeight = -renderer.properties().renderHeight;
				renderer.properties().renderFORy = -renderer.properties().renderFORy + 16;
				// System.out.println("grumpyCollideCheck");
				++LevelScene.killedCreaturesTotal;
				++LevelScene.killedCreaturesByGrumpy;
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean fireballCollideCheck(Fireball fireball) {
		if (deadTime != 0)
			return false;

		float xD = fireball.x - x;
		float yD = fireball.y - y;

		if (xD > -16 && xD < 16) {
			if (yD > -height && yD < fireball.height) {
				if (noFireballDeath)
					return true;

				xa = fireball.facing * 2;
				ya = -5;
				flyDeath = true;
				if (spriteTemplate != null)
					spriteTemplate.isDead = true;
				deadTime = 100;
				winged = false;
				renderer.properties().renderHeight = -renderer.properties().renderHeight;
				renderer.properties().renderFORy = -renderer.properties().renderFORy + 16;
				// System.out.println("fireballCollideCheck");
				++LevelScene.killedCreaturesTotal;
				++LevelScene.killedCreaturesByFireBall;
				return true;
			}
		}
		return false;
	}

	@Override
	public void bumpCheck(int xTile, int yTile) {
		if (deadTime != 0)
			return;

		for(Player player : world.getPlayers())
		if (x + width > xTile * 16 && x - width < xTile * 16 + 16 && yTile == (int) ((y - 1) / 16)) {
			xa = -player.facing * 2;
			ya = -5;
			flyDeath = true;
			if (spriteTemplate != null)
				spriteTemplate.isDead = true;
			deadTime = 100;
			winged = false;
			renderer.properties().renderHeight = -renderer.properties().renderHeight;
			renderer.properties().renderFORy = -renderer.properties().renderFORy + 16;
			System.out.println("bumpCheck");
		}
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public HashMap<Field, Object> getAttributeMap() {
		HashMap<Field, Object> map = super.getAttributeMap();
		Field[] fields = getClass().getDeclaredFields();
		try { // loop over all fields
			for (Field field : fields) {
				map.put(field, field.get(this)); // read the value inside the
													// field for the instance
													// given by "this" and put
													// the value in the map
			}
		} catch (IllegalAccessException e) {
			new Throwable(e).printStackTrace();
		}
		return map;
	}

	@Override
	public String toString() {
		String s = (this.deadTime > 0 ? "DEAD | " : "");
		s += (!this.onGround ? "In Air | " : "");
		s += super.toString();
		s += (winged ? " | winged " : "");
		return s;
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
