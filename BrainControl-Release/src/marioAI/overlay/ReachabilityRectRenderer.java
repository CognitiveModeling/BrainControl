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
package marioAI.overlay;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import marioAI.brain.simulation.ReachabilityMap;
import marioAI.movement.MovementTuning;
import marioWorld.engine.GlobalOptions;
import marioWorld.engine.LevelScene;



/**
 * Draws the ReachabilityMap for each character by colouring the scene in-game in different shades of grey
 *
 */
public class ReachabilityRectRenderer implements IGridRectRenderer {
	
	private ReachabilityMap Rmap;
	
	public ReachabilityRectRenderer(ReachabilityMap Rmap) 
	{
		this.Rmap=Rmap;
	}
	
	public void prepareDrawing(LevelScene levelScene, int playerIndex) {
		// nothing to do
	}
	
	public void drawRect(Graphics g, int levelX, int levelY, Rectangle rect) {

		if(Rmap==null)
			return;

		int grainFactor = MovementTuning.currentGrainFactor;

		
		if (levelY < 0 || levelY >= Rmap.map.length / grainFactor) //igitt
			return;
		if (levelX < 0 || levelX >= Rmap.map[0].length / grainFactor)
			return;
		

		for (int grainX = 0; grainX < grainFactor; grainX++) {
			for (int grainY = 0; grainY < grainFactor; grainY++) {
				if(Rmap.map[levelY][levelX].isReachable()) {
					g.setColor(new Color(0f,0f,0.5f,0));
				} else {
					g.setColor(new Color(0f, 0f, 0f, 0.25f));
				}
				
				
				int cx = (rect.x + grainX * rect.width / grainFactor) * GlobalOptions.resolution_factor;
				int cy = (rect.y + grainY * rect.height / grainFactor) * GlobalOptions.resolution_factor;
				int dx = (rect.width / grainFactor) * GlobalOptions.resolution_factor;
				int dy = (rect.height / grainFactor) * GlobalOptions.resolution_factor;
				
				g.fillRect(cx, cy, dx, dy);
			}
		
		}
	}
		

}
