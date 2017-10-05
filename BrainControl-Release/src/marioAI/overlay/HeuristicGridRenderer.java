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

import marioAI.movement.heuristics.WorldNodeHeuristic;
import marioWorld.engine.GlobalOptions;
import marioWorld.engine.LevelScene;

/**
 * Renders the given heuristic as an overlay to the world.
 * 
 * @author Sebastian
 * 
 */
public class HeuristicGridRenderer implements IGridRectRenderer {

	private WorldNodeHeuristic heuristic;

	/**
	 * Level coordinates where the heuristic expects its (0,0) to be.
	 */
	private int baseX, baseY;

	/**
	 * Alpha value of the rendering.
	 */
	private float alpha = 0.5f;

	/**
	 * Scaled distance map for rendering purposes.
	 */
	protected double[][] scaledDistanceMap;

	/**
	 * Creates a new heuristic renderer. The renderer needs to know where the heuristic is based; that is where the heuristic expects 0,0 to be.
	 * 
	 * @param h
	 *            The heuristic to render.
	 * @param baseX
	 *            x-coordinate of the level-tile where the heuristic expects (0,0) of the map to be.
	 * @param baseY
	 *            y-coordinate of the level-tile where the heuristic expects (0,0) of the map to be.
	 */
	public HeuristicGridRenderer(WorldNodeHeuristic h, int baseX, int baseY) {
		heuristic = h;
		this.baseX = baseX;
		this.baseY = baseY;
		updateScaledDistanceMap();
	}

	/**
	 * Updates the scaled version of the distance map.
	 */
	private void updateScaledDistanceMap() {
		double[][] distanceMap = heuristic.getMap();
		final int numCols = distanceMap.length;
		final int numRows = distanceMap[0].length;
		double maxValue = 0;

		// find maximum in order to scale the field to the interval [0,1]
		for (int col = 0; col < numCols; col++) {
			for (int row = 0; row < numRows; row++) {
				double d = distanceMap[col][row];
				if (maxValue < d && !Double.isInfinite(d) && d != WorldNodeHeuristic.MAX_VALUE) {
					maxValue = d;
				}
			}
		}

		if (this.scaledDistanceMap == null) {
			scaledDistanceMap = new double[numCols][numCols];
		}
		for (int col = 0; col < numCols; col++) {
			for (int row = 0; row < numRows; row++) {
				scaledDistanceMap[col][row] = distanceMap[col][row] / maxValue;
			}
		}
	}

	/**
	 * See the interface's definition for more information.
	 */
	@Override
	public void drawRect(Graphics g, int levelX, int levelY, Rectangle rect) {
		double[][] map = scaledDistanceMap;
		int x = levelX - baseX;
		int y = levelY - baseY;

		if (y < 0 || y >= map.length)
			return;
		if (x < 0 || x >= map[0].length)
			return;

		float distance = (float) map[y][x];
		if (Float.isInfinite(distance)) {
			g.setColor(new Color(0f, 0f, 0f, alpha));
		} else {
			float red = distance > 0.5 ? 1 : distance * 2;
			float green = 1 - (float) Math.abs(distance - 0.5) * 2;
			float blue = distance < 0.5 ? 1 : 1 - (distance - 0.5f) * 2;
			g.setColor(new Color(red, green, blue, alpha));
		}
		g.fillRect(rect.x * GlobalOptions.resolution_factor, rect.y * GlobalOptions.resolution_factor, rect.width, rect.height);
	}

	public float getAlpha() {
		return alpha;
	}

	public void setAlpha(float alpha) {
		this.alpha = alpha < 0f ? 0 : (alpha > 1f ? 1f : alpha);
	}

	@Override
	public void prepareDrawing(LevelScene levelScene, int playerIndex) {
		// nothing to do here

	}
}
