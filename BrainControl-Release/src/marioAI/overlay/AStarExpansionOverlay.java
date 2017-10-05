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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import marioAI.aStar.AStarNodeVisualizer;
import marioAI.movement.WorldNode;
import marioWorld.engine.GlobalOptions;
import marioWorld.engine.LevelScene;
import marioWorld.engine.coordinates.CoordinatesUtil;

/**
 * 
 * AStarExpansionOverlay draws the expansion of A* and its visited nodes. It
 * gives a color gradient from yellow to black.
 * 
 * @author Katja & Eduard
 * 
 */
public class AStarExpansionOverlay extends ToggleableOverlay {

	/**
	 * Determine whether the dots' colors should represent the order in which
	 * they were expanded or their cost.
	 */
	private static final boolean COLOR_REPRESENTS_ORDER = false;
	private static final boolean COLOR_REPRESENTS_COST = !COLOR_REPRESENTS_ORDER;

	/**
	 * inner class wrapping a position where a dot is drawn and the normalized
	 * color
	 */
	private static class DrawDot {
		private final float alpha = 0.7f;
		/** x position where the dot will be drawn in continuous coordinates */
		private final float x;
		/** y position where the dot will be drawn in continuous coordinates */
		private final float y;
		/** normalized color (i.e. cost) */
		private final Color color;

		/**
		 * constructor for the case where color represents order
		 * 
		 * @param worldNode
		 *            node to draw
		 * @param normalizedPosition
		 *            node's relative position in the list (ranging from 0 to 1)
		 */
		private DrawDot(AStarNodeVisualizer worldNode, float normalizedPosition) {
			this.x = worldNode.getPlayerPos().x;
			// + 2 offset to draw below the red world path line
			this.y = worldNode.getPlayerPos().y + 2;
			// use 1-normalizedPosition invert the colors, nodes last expanded
			// will be darker
			this.color = new Color(1 - normalizedPosition,
					1 - normalizedPosition, 0.f, alpha);
		}

		/**
		 * constructor for the case when color represents cost
		 * 
		 * @param worldNode
		 *            the node which will be drawn
		 * @param max
		 *            precalculated maximum of costs for all Nodes. Used to
		 *            scale the color.
		 */
		private DrawDot(AStarNodeVisualizer worldNode, float min, float max) {
			this.x = worldNode.getPlayerPos().x;
			// + 2 offset to draw below the red world path line
			this.y = worldNode.getPlayerPos().y + 2;
			float color = getUnnormalizedColor(worldNode);
			if (Float.isInfinite(color)) {
				color = max;
			}
			color = 1 - (color - min) / (max - min); // 0 (i.e. black) for max
			// values, 1 (i.e.
			// yellow) for min
			// values
			this.color = new Color(color, color, 0.f, alpha);
		}

		private DrawDot(AStarNodeVisualizer worldNode, Color color) {
			this.x = worldNode.getPlayerPos().x;
			this.y = worldNode.getPlayerPos().y;
			this.color = color;
		}

		/**
		 * encode the way from start node to the specified node (optionally with
		 * the heuristic added) in a not yet normalized color.
		 * 
		 * @param worldNode
		 *            the worldNode which is encoded in color
		 * @return unnormalized color
		 */
		private static float getUnnormalizedColor(AStarNodeVisualizer worldNode) {
			double unnormalizedColor = 0;
			unnormalizedColor = worldNode.cost();
			return (float) unnormalizedColor;
		}
	}

	protected final List<DrawDot> drawDots;

	public AStarExpansionOverlay(List<AStarNodeVisualizer> visitedNodes,
			ArrayList<AStarNodeVisualizer> excludedNodes) {
		super("A star search overlay");
		drawDots = new ArrayList<DrawDot>();
		
		if (COLOR_REPRESENTS_ORDER) { // draw colors according to order of
			// expansion
			int index = 0;
			float listSize = visitedNodes.size();

			for (AStarNodeVisualizer node : visitedNodes) {
				float relativePosition = index / listSize;
				drawDots.add(new DrawDot(node, relativePosition));
				index++;
			}

		} else if (COLOR_REPRESENTS_COST) { // draw colors according to their
											// cost
			float max = Float.MIN_VALUE;
			float min = Float.MAX_VALUE;
			/** find maximum and minimum costs in all nodes */
			for (AStarNodeVisualizer node : visitedNodes) {
				float f = DrawDot.getUnnormalizedColor(node);
				if (max < f && !Float.isInfinite(f)) {
					max = f;
				}
				if (min > f) {
					min = f;
				}
			}

			// Fills array of visited nodes
			for (AStarNodeVisualizer node : visitedNodes) {
				drawDots.add(new DrawDot(node, min, max));
			}

			for (AStarNodeVisualizer n : excludedNodes) {
				drawDots.add(new DrawDot(n, new Color(255, 0, 0)));
			}
		}
	}

	@Override
	public void drawOverlay(Graphics g, LevelScene levelScene, int playerIndex) 
	{
		Graphics2D g2 = (Graphics2D) g;

		// Stroke width
		g2.setStroke(new BasicStroke(2));

		float xCam = GlobalOptions.resolution_factor * levelScene.cameras.get(playerIndex)[0];
		float yCam = GlobalOptions.resolution_factor * levelScene.cameras.get(playerIndex)[1] + (CoordinatesUtil.UNITS_PER_TILE >> 1);

		for (int i = 1; i < drawDots.size(); i++) 
		{
			DrawDot dot = this.drawDots.get(i);
			g2.setColor(dot.color);
			g2.fillRect
			(
				(int) (GlobalOptions.resolution_factor * dot.x - xCam), 
				(int) (GlobalOptions.resolution_factor * dot.y - yCam),
				(int) (GlobalOptions.resolution_factor), 
				(int) (GlobalOptions.resolution_factor)
			);
		}

	}
}
