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
package marioUI.gui.overlays;

import java.awt.Color;
import java.awt.Graphics;

import marioAI.agents.CAEAgent;
import marioAI.overlay.ToggleableOverlay;
import marioWorld.engine.GlobalOptions;
import marioWorld.engine.LevelScene;
import marioWorld.engine.PlayerWorldObject;
import marioWorld.engine.coordinates.GlobalCoarse;
import marioWorld.engine.coordinates.GlobalContinuous;
import marioWorld.engine.sprites.Sprite;

public class ObjectsOverlay extends ToggleableOverlay {
	
	private CAEAgent agent;

	public ObjectsOverlay(CAEAgent agent) {
		super("objects overlay");
		this.agent = agent;
	}
	
	@Override
	public ObjectsOverlay updateOverlayInformation() {
		return this;
	}
	
	
	/**
	 * Draws the points at the position to which they belong.
	 */
	public void drawOverlay(Graphics g, LevelScene levelScene, int playerIndex) 
	{
		PlayerWorldObject[][] map = agent.getBrain().getLevelSceneAdapter().getSimulatedLevelScene().getStaticGlobalMap();
		for(int i= 0; i<map[0].length; i++) {
			for(int j = 0; j <map.length;j++) {
				if(map[j][i].isRelevantToLearning()) {
					GlobalContinuous pos = (new GlobalCoarse(i,j)).toGlobalContinuous();
					
					int x = (int) (pos.x - levelScene.cameras.get(playerIndex)[0]) * GlobalOptions.resolution_factor;
					int y = (int) (pos.y - levelScene.cameras.get(playerIndex)[1]) * GlobalOptions.resolution_factor;
					drawNameLabel(g, x, y, map[j][i]);		
				}
			}
		}
		
		for(Sprite sprite : levelScene.getSprites()) {
			
//			GlobalContinuous pos = (new GlobalCoarse(sprite.mapX,sprite.mapY)).toGlobalContinuous();
			
			int x = (int) (sprite.x - levelScene.cameras.get(playerIndex)[0] ) * GlobalOptions.resolution_factor;
			int y = (int) (sprite.y - levelScene.cameras.get(playerIndex)[1] - 8) * GlobalOptions.resolution_factor;
			drawNameLabel(g, x, y , sprite.getType());
		}
				
		
	}
	
	private void drawNameLabel(Graphics g,int x, int y, PlayerWorldObject pwo) {
		Color ori = g.getColor();
		String str = PlayerWorldObject.getObjectVocalName(pwo);
		
		if(str != "") {
			g.setColor(Color.BLACK);
			g.drawString(str, ShiftWest(x, 1), ShiftNorth(y, 1));
			g.drawString(str, ShiftWest(x, 1), ShiftSouth(y, 1));
			g.drawString(str, ShiftEast(x, 1), ShiftNorth(y, 1));
			g.drawString(str, ShiftEast(x, 1), ShiftSouth(y, 1));
			g.setColor(Color.white);
			g.drawString(str, x, y);
		}
		
		
		g.setColor(ori);
		
		
	}
	
	int ShiftNorth(int p, int distance) {
		   return (p - distance);
		   }
		int ShiftSouth(int p, int distance) {
		   return (p + distance);
		   }
		int ShiftEast(int p, int distance) {
		   return (p + distance);
		   }
		int ShiftWest(int p, int distance) {
		   return (p - distance);
		   }

}
