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
 * Renders the given FineGrainPotentialField's jumpValues as an overlay to the world.
 * 
 * @author Marcel
 * 
 */

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import marioAI.movement.MovementTuning;
import marioAI.movement.heuristics.FineGrainPotentialField;
import marioWorld.engine.LevelScene;

public class JumpValueRenderer implements IGridRectRenderer {
	private FineGrainPotentialField heuristic;
	
	/**
	 * Scaled distance map for rendering purposes.
	 */
	protected double[][] scaledDistanceMap;
	
	
	/**
	 * Level coordinates where the heuristic expects its (0,0) to be.
	 */
	int baseX;
	int baseY;
	
	/**
	 * Alpha value of the rendering.
	 */
	private float alpha = 0.5f;
	
	/**
	 * Creates a new heuristic renderer. The renderer needs to know where the heuristic is based; that is where the heuristic expects 0,0 to be.
	 * 
	 * @param heuristic
	 *            The heuristic to render.
	 * @param baseX
	 *            x-coordinate of the level-tile where the heuristic expects (0,0) of the map to be.
	 * @param baseY
	 *            y-coordinate of the level-tile where the heuristic expects (0,0) of the map to be.
	 */
	public JumpValueRenderer(FineGrainPotentialField heuristic, int baseX,
			int baseY) {

		this.heuristic = heuristic;
		this.baseX = baseX;
		this.baseY = baseY;
		updateScaledDistanceMap();
	}

	/**
	 * Updates the scaled version of the distance map.
	 */
	private void updateScaledDistanceMap() {
		double[][] jumpValueMap = heuristic.getJumpValueMap();
		final int numCols = jumpValueMap.length;
		final int numRows = jumpValueMap[0].length;
		double maxValue = FineGrainPotentialField.MAX_JUMP_VALUE;

		if (this.scaledDistanceMap == null) {
			scaledDistanceMap = new double[numCols][numRows];
		}
		for (int col = 0; col < numCols; col++) {
			for (int row = 0; row < numRows; row++) {

				scaledDistanceMap[col][row] = jumpValueMap[col][row] / maxValue;

			}
		}
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
		double[][] map = scaledDistanceMap;
		int grainFactor = MovementTuning.currentGrainFactor;
		int x = levelX - baseX;
		int y = levelY - baseY;

		if (y < 0 || y >= map.length / grainFactor)
			return;
		if (x < 0 || x >= map[0].length / grainFactor)
			return;
		for (int grainX = 0; grainX < grainFactor; grainX++) {
			for (int grainY = 0; grainY < grainFactor; grainY++) {
				float distance = (float) map[y * grainFactor + grainY][x
						* grainFactor + grainX];
				if (Float.isInfinite(distance)) {
					g.setColor(new Color(0f, 0f, 0f, 0.75f));
				} else {
					float red = distance > 0.5 ? 1 : distance * 2;
					float green = 1 - (float) Math.abs(distance - 0.5) * 2;
					float blue = distance < 0.5 ? 1 : 1 - (distance - 0.5f) * 2;
					g.setColor(new Color(red, green, blue, alpha));
				}
				g.fillRect(rect.x + grainX * rect.width / grainFactor, rect.y
						+ grainY * rect.height / grainFactor, rect.width
						/ grainFactor, rect.height / grainFactor);
			}
		}
	}

}
