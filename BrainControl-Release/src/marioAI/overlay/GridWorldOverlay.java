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

import java.awt.Graphics;
import java.awt.Rectangle;

import marioWorld.engine.LevelScene;
import marioWorld.engine.coordinates.CoordinatesUtil;

/**
 * Draws a grid overlay on the screen aligned to the level's tiles. You need to provide an IGridRectRenderer to actually make it do something sensible (other
 * than just rendering a grid).
 * 
 * @author sebastian
 * 
 */
public class GridWorldOverlay extends ToggleableOverlay {

	private IGridRectRenderer renderer;
	private boolean enableCropping;

	private static final int NUM_ROWS = 22;
	private static final int NUM_COLS = 22;

	/**
	 * Constructs a new GridWorldOverlay, taking a renderer for each rect of the grid. If no render is specified, a simple grid will be drawn on screen.
	 * 
	 * @param renderer
	 *            The renderer to use for the rectangles of the grid.
	 * @param enableCropping
	 *            Whether to crop rectangles at the edge of the screen.
	 */
	public GridWorldOverlay(IGridRectRenderer renderer, boolean enableCropping, String name) {
		super(name);
		this.renderer = renderer;
		this.enableCropping = enableCropping;
	}

	public GridWorldOverlay(IGridRectRenderer renderer, String name) {
		this(renderer, false, name);
	}

	@Override
	public void drawOverlay(Graphics g, LevelScene levelScene, int playerIndex) 
	{
		if (renderer != null) {
			renderer.prepareDrawing(levelScene, playerIndex);
		}

		// level coordinates
		int leftScreenCoord = (int) levelScene.cameras.get(playerIndex)[0] / CoordinatesUtil.UNITS_PER_TILE;
		int upperScreenCoord = (int) levelScene.cameras.get(playerIndex)[1] / CoordinatesUtil.UNITS_PER_TILE;
		int offsetX = ((int) levelScene.cameras.get(playerIndex)[0]) % CoordinatesUtil.UNITS_PER_TILE;
		int offsetY = ((int) levelScene.cameras.get(playerIndex)[1]) % CoordinatesUtil.UNITS_PER_TILE;
		for (int col = 0; col < NUM_COLS; col++) {
			for (int row = 0; row < NUM_ROWS; row++) {
				int width = IGridRectRenderer.REGULAR_RECT_WIDTH;
				int height = IGridRectRenderer.REGULAR_RECT_HEIGHT;
				int x = row * IGridRectRenderer.REGULAR_RECT_WIDTH - offsetX;
				int y = col * IGridRectRenderer.REGULAR_RECT_HEIGHT - offsetY;

				if (enableCropping) {
					if (x <= 0) {
						width += x;
						x = 0;
					}
					if (x + width > IGridRectRenderer.REGULAR_RECT_WIDTH) {
						width = IGridRectRenderer.REGULAR_RECT_WIDTH - x;
					}
					if (y <= 0) {
						height += y;
						y = 0;
					}
					if (y + height > IGridRectRenderer.REGULAR_RECT_HEIGHT) {
						height = IGridRectRenderer.REGULAR_RECT_HEIGHT - y;
					}
				}

				if (renderer != null) {
					int levelX = leftScreenCoord + row;
					int levelY = upperScreenCoord + col;
					Rectangle rect = new Rectangle(x, y, width, height);
					renderer.drawRect(g, levelX, levelY, rect);
					//System.out.println("leftscreencord: "+leftScreenCoord+" upperscreencord: "+upperScreenCoord);
					//renderer.drawRect(g, levelX, levelY, rect);						
				} else {
					// if there is no custom renderer specified, just draw a
					// plain grid.
					g.drawRect(x, y, width, height);
				}
			}
		}
	}
}
