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

public class EndItem extends Sprite {
	//private static float GROUND_INERTIA = 0.89f;
	//private static float AIR_INERTIA = 0.89f;

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

	public EndItem(LevelScene world, float x, float y) 
	{
		kind = KIND_END_ITEM;
		
		switch (world.getOwnLevelName()){
		case "releaseLevel0": renderer = new SpriteRenderer(this, Art.camera, 0, 0); break;
		case "releaseLevel1": renderer = new SpriteRenderer(this, Art.computer, 0, 0); break;
		case "releaseLevel2": renderer = new SpriteRenderer(this, Art.microphone, 0, 0); break;
		case "releaseLevel3": renderer = new SpriteRenderer(this, Art.dreamCatcher, 0, 0); break;
		case "releaseLevel4": renderer = new SpriteRenderer(this, Art.Maria, 0, 0); break;
		default: renderer = new SpriteRenderer(this, Art.camera, 0, 0); break;
		}
			
		//renderer = new SpriteRenderer(this, Art.camera, 0, 0);

		this.x = x;
		this.y = y;
		this.world = world;

		facing = 1;
		life = 0;		
		
//		sheet.xPicO = 23;
//		sheet.yPicO = 40;		
//		yPic = 0;
//		wPic = 16;
//		hPic = 32;
	}

	@Override
	public void collideCheck(Sprite other)
	{
		for(Player player : world.getPlayers())
		{
			float xPlayerD = player.x - x;
			float yPlayerD = player.y - y;
			if (xPlayerD > -8 && xPlayerD < 8) {
				if (yPlayerD > -height && yPlayerD < player.height) {
					player.reachEndItem();
				}
			}
		}
	}

	
	
	@Override
	public void move() {
		//just do nothing
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
	public EndItem cloneWithNewLevelScene(LevelScene ls) {
		EndItem ret = (EndItem) super.clone();
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
