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

/**
 * Overlay for darker vision.
 * 
 * @author Marcel
 * 
 */

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import marioWorld.engine.LevelScene;

public class NightVisionRenderer implements IGridRectRenderer {

	public NightVisionRenderer() {
		

	}
	

	@Override
	public void prepareDrawing(LevelScene levelScene, int playerIndex) {
		// do nothing
	}

	/**
	 * Draws a rectangle.
	 * 
	 * @param g
	 *            The graphics object used for drawing.
	 * @param levelX
	 *            The x-coordinate of the level position this is to be drawn at
	 * @param levelY
	 *            The y-coordinate of the level position this is to be drawn at
	 * @param rect
	 *            The rectangle to draw on the screen
	 */
	@Override
	public void drawRect(Graphics g, int levelX, int levelY, Rectangle rect) {
		g.setColor(new Color(0f, 0f, 0f, 0.5f));
		g.fillRect(rect.x, rect.y, 	16, 16);
	}
}
