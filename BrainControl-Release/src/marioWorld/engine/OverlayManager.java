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
package marioWorld.engine;

import java.awt.Graphics;
import java.util.ArrayList;

/**
 * Manages the overlays to be rendered above the current game screen.
 * 
 * @author sebastian
 * 
 */
public class OverlayManager{
	private ArrayList<IScreenOverlay> overlays;

	public OverlayManager() {
		overlays = new ArrayList<IScreenOverlay>();
	}
	

	/**
	 * Adds an overlay to the list of overlays.
	 * 
	 * @param overlay
	 */
	synchronized public void addOverlay(IScreenOverlay overlay) 
	{
		overlays.add(overlay);
	}

	/**
	 * Removes an overlay from the list of overlays.
	 * 
	 * @param overlay
	 */
	synchronized public void removeOverlay(IScreenOverlay overlay) {
		overlays.remove(overlay);
	}

	/**
	 * Renders all overlays to the given Graphics-object.
	 * 
	 * @param g
	 * @param scene
	 */
	public synchronized void render(Graphics g, LevelScene scene, boolean onGame, int playerIndex) 
	{
		
		for (IScreenOverlay o : overlays) 
		{
			if ((o.isOnGame()==onGame) && o.shouldDraw()) 
			{		
				o.drawOverlay(g, scene, playerIndex);
			}
		}
	}
}
