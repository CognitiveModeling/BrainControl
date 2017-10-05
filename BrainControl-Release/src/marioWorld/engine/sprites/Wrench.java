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

public class Wrench extends Sprite {
	private int width = 4;
	private final int height = 12;// = 24;

	private LevelScene world;
	public int facing;

	public boolean avoidCliffs = false;
	private int life;

	public Wrench(LevelScene world, int x, int y) {
		kind = KIND_WRENCH;
		
		renderer = new SpriteRenderer(this, Art.items, 0, 0);		

		this.x = x;
		this.y = y;
		this.world = world;
		
//		sheet.xPicO = 8;
//		sheet.yPicO = 15;
//		wPic = hPic = 16;
				
		// height = 12;
		facing = 1;
		life = 0;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void collideCheck(Sprite other) 
	{
		if(other instanceof Player) {
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
	}

	@Override
	public void move() {
		if (life < 9) {
			layer = 0;
			y--;
			life++;
			return;
		}
	}

	@Override
	public Wrench cloneWithNewLevelScene(LevelScene ls) {
		Wrench ret = (Wrench) super.clone();
		ret.avoidCliffs = avoidCliffs;
		ret.facing = facing;
		// ret.height = height;
		ret.life = life;
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
}
