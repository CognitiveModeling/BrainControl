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

import java.util.HashSet;

import marioWorld.engine.coordinates.CoordinatesUtil;
import marioWorld.engine.coordinates.GlobalCoarse;
import marioWorld.engine.coordinates.GlobalContinuous;
import marioWorld.engine.level.SpriteTemplate;
import marioWorld.engine.sprites.Enemy;
import marioWorld.engine.sprites.FlowerEnemy;
import marioWorld.engine.sprites.Sprite;

public class SimulatedSpawn {
	
	private final int spriteTemplateKind;
	private final boolean isWinged;
	private boolean wasVisibleInLastTimeStep = false;
	private boolean isDead = false;
	/**circumvent costly List.contains() call. Only this class should modify add/remove anyway.*/
	private boolean isInList = false;
	public final GlobalCoarse position;
	private SimulatedSprite simulatedSprite;
	
	public SimulatedSpawn(SpriteTemplate spriteTemplate, GlobalCoarse position) {
		this.spriteTemplateKind = spriteTemplate.getType();
		this.isWinged = spriteTemplate.isWinged();
		this.position = position;
	}
	
	/**
	 * if the sprite is outside of camera view, it is removed
	 * @param sprites the list of sprites from which the respective sprite is removed
	 * @param xCam camera view
	 * @param yCam camera view
	 */
	public void removeSprite(HashSet<SimulatedSprite> sprites, float xCam, float yCam) {
		//cf. LevelScene.tick()
		float xd = simulatedSprite.x - xCam;
		float yd = simulatedSprite.y - yCam;
		if (xd < -64 || xd > 320 + 64 || yd < -64 || yd > 240 + 64) {
			sprites.remove(simulatedSprite);
			this.isInList = false;
		}
	}
	
	private boolean isInsideCameraView(float xCam, float yCam) {
		if(position.x > (int) xCam / CoordinatesUtil.UNITS_PER_TILE -1 && position.x < (int) (xCam + 360) / CoordinatesUtil.UNITS_PER_TILE +1){
			if(position.y > (int) yCam / CoordinatesUtil.UNITS_PER_TILE -1 && position.x < (int) (yCam + 240) / CoordinatesUtil.UNITS_PER_TILE +1){
				return true;
			}
		}
		return false;
	}
	
	public void spawnSprite(HashSet<SimulatedSprite> sprites, GlobalContinuous playerPosition, float xCam, float yCam) {
		if(isInsideCameraView(xCam, yCam)) {
			//cf. LevelScene.tick()
			if(!isDead && !wasVisibleInLastTimeStep && (simulatedSprite==null || !isInList)) {
				this.simulatedSprite = new SimulatedSprite(helpSpawn(playerPosition));
				sprites.add(this.simulatedSprite);
				this.isInList = true;
			}
			wasVisibleInLastTimeStep = true;
		}
	}
		
	public void setDead(boolean isDead) {
		this.isDead = isDead;
	}
	
	private Sprite helpSpawn(GlobalContinuous playerPosition) {
		//cf. SpriteTemplate.spawn
		Sprite ret;
		if(spriteTemplateKind == 4) { //PiranhaPlant
			ret = new FlowerEnemy(null, new GlobalContinuous(position.x * 16 + 15, position.y * 16 + 24), position);
		}else {
			int dir = getDirection(this.position, playerPosition);
			ret = new Enemy(null, new GlobalContinuous(position.x * 16 + 8, position.y * 16 + 15), position, dir, spriteTemplateKind, isWinged);
		}
		return ret;
	}
	
	public static int getDirection(GlobalCoarse spritePosition, GlobalContinuous playerPosition) {
		//cf. LevelScene.tick()
		int dir = 0;
		if (spritePosition.x * CoordinatesUtil.UNITS_PER_TILE + 8 > playerPosition.x + CoordinatesUtil.UNITS_PER_TILE)
			dir = -1;
		if (spritePosition.x * CoordinatesUtil.UNITS_PER_TILE + 8 < playerPosition.x - CoordinatesUtil.UNITS_PER_TILE)
			dir = 1;
		return dir;

	}
}
