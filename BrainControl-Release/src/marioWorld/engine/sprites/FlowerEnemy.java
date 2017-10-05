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

import marioWorld.engine.LevelScene;
import marioWorld.engine.coordinates.GlobalCoarse;
import marioWorld.engine.coordinates.GlobalContinuous;

public class FlowerEnemy extends Enemy {
	private int tick;
	private int yStart;
	private int jumpTime = 0;
	private LevelScene world;

	public FlowerEnemy(LevelScene world, GlobalContinuous position, GlobalCoarse mapPosition) {
		super(world, position, mapPosition, 1, ENEMY_SHALLY, false);
		kind = KIND_ENEMY_FLOWER;
		noFireballDeath = false;
		this.world = world;
		
		renderer.properties().selectedFrameX = 0;
		renderer.properties().selectedFrameY = 6;
		
//		sheet.yPicO = 24;
		
		this.height = 12;
		this.width = 2;

		yStart = (int) position.y;
		ya = -8;

		this.y -= 1;

		this.layer = 0;

		for (int i = 0; i < 4; i++) {
			move();
		}
	}

	@Override
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

		tick++;

		if (y >= yStart) {
			y = yStart;

			for(Player player : world.getPlayers())
			{
				int xd = (int) (Math.abs(player.x - x));
				jumpTime++;
				if (jumpTime > 40 && xd > 24) {
					ya = -8;
				} else {
					ya = 0;
				}
			}
		} else {
			jumpTime = 0;
		}

		y += ya;
		ya *= 0.9;
		ya += 0.1f;

		renderer.properties().selectedFrameX = ((tick / 2) & 1) * 2 + ((tick / 6) & 1);
	}

	@Override
	public FlowerEnemy cloneWithNewLevelScene(LevelScene ls) {
		FlowerEnemy ret = (FlowerEnemy) super.clone();
		ret.tick = tick;
		ret.yStart = yStart;
		ret.jumpTime = jumpTime;
		ret.world = ls;
		return ret;
	}

	/*
	 * public void render(Graphics og, float alpha) { if (!visible) return;
	 * 
	 * int xPixel = (int)(xOld+(x-xOld)*alpha)-xPicO; int yPixel = (int)(yOld+(y-yOld)*alpha)-yPicO;
	 * 
	 * int a = ((tick/3)&1)*2; // a += ((tick/8)&1); og.drawImage(sheet[a*2+0][6], xPixel-8, yPixel+8, 16, 32, null); og.drawImage(sheet[a*2+1][6], xPixel+8,
	 * yPixel+8, 16, 32, null); }
	 */

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
