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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import marioAI.agents.CAEAgent;
import marioAI.overlay.ToggleableOverlay;
import marioWorld.engine.Art;
import marioWorld.engine.GlobalOptions;
import marioWorld.engine.LevelScene;
import marioWorld.engine.SpriteSheet;
import marioWorld.engine.coordinates.GlobalContinuous;
import marioWorld.engine.sprites.Player;

public class WaitingIconOverlay extends ToggleableOverlay{
	
	
	private int time;
	private Player player;
	
	public WaitingIconOverlay(CAEAgent agent) {
		super("waiting icon");
		this.time = agent.getPlayer().birthTime;
		this.player = agent.getPlayer();
	}
	

	/**
	 * Called from another thread, must be synchronized 
	 */
	@Override
	public synchronized void drawOverlay(Graphics g, LevelScene levelScene, int playerIndex) {
    		

		// determine current x,y pos
		int x,y;

		if (player == null) 
		{
			x = GlobalOptions.xScreen/2;
			y = (GlobalOptions.yScreen + 24)/2;
		} 
		else 
		{
			GlobalContinuous pos = player.getPosition();
			
			x = (int) (pos.x - levelScene.cameras.get(playerIndex)[0]) * GlobalOptions.resolution_factor;
			y = (int) (-32+pos.y - levelScene.cameras.get(playerIndex)[1]) * GlobalOptions.resolution_factor;
			

		}

		// Draw the base shape -- the rectangle the image will fit into as well as its outline
//		g.setColor(Color.BLACK);
//		g.fillRect(x,y-32, 32, 32);
		SpriteSheet icon = Art.waiting;
		int frameNumber = time % icon.xImages(); 
		g.drawImage(icon.getImage(frameNumber,0), x, y, null);
		
		
	}



}
