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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import marioAI.agents.CAEAgent;
import marioAI.overlay.ToggleableOverlay;
import marioWorld.engine.GlobalOptions;
import marioWorld.engine.LevelScene;
import marioWorld.engine.coordinates.GlobalCoarse;
import marioWorld.engine.coordinates.GlobalContinuous;

public class TileSelectionOverlay extends ToggleableOverlay{
	
	private CAEAgent agent;
	private GlobalContinuous selectedTile;
	public TileSelectionOverlay(CAEAgent agent) {
		super("tile selection");
		this.agent = agent;
		this.setActive(true);
		if(agent != null) {
			selectedTile = agent.getMouseInput().getSelectedTilePosition(agent.getBrain().levelSceneAdapter.getClonedLevelScene(), agent.getPlayerIndex());
		}
		// TODO Auto-generated constructor stub
	}
	
	
	/**
	 * Called from another thread, must be synchronized 
	 */
	@Override
	public synchronized void drawOverlay(Graphics g, LevelScene levelScene, int playerIndex) {
    		
		if(agent.getMouseInput().isOverlayActive()) {
			
	
			// determine current x,y pos
			int x,y;
			int tileSize = 16*GlobalOptions.resolution_factor;
			Graphics2D g2d = (Graphics2D)g;
			
			
			if(!selectedTile.toGlobalCoarse().equals(new GlobalCoarse(-1,-1))){
				x = (int) (selectedTile.x - levelScene.cameras.get(playerIndex)[0]) * GlobalOptions.resolution_factor;
				y = (int) (selectedTile.y - levelScene.cameras.get(playerIndex)[1]) * GlobalOptions.resolution_factor;
					
				switch(agent.getPlayerIndex()) {
				case 0:
					g2d.setColor(Color.RED);
					break;
				case 1:
					g2d.setColor(Color.BLUE);
					break;
				case 2:
					g2d.setColor(Color.GREEN);
					break;
				}
				
				Stroke tmp = g2d.getStroke();
				Stroke dashed = new BasicStroke(2.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10, new float[]{5}, agent.getPlayerIndex()*1.5f);
				g2d.setStroke(dashed);
				g2d.drawRect(x-tileSize/2,y-tileSize/2, tileSize, tileSize);
	//			SpriteSheet icon = Art.waiting;
	//			int frameNumber = time % icon.xImages(); 
	//			g.drawImage(icon.getImage(frameNumber,0), x, y, null);
				g2d.setStroke(tmp);
			}

		}
	}

}
