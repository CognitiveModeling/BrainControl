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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;

import marioWorld.engine.LevelScene;
import marioWorld.engine.PlayerWorldObject;
import marioWorld.engine.PlayerWorldObject.ObjectType;
import marioWorld.mario.environments.Environment;

/**
 * Renders an overlay displaying the IDs of the single tiles on screen.
 * 
 * @author Sebastian
 * 
 */
public class IdGridRenderer implements IGridRectRenderer {

	/**
	 * Height of the UI of the game, we probably don't want to render over it.
	 */
	private static final int CUT_OFF_Y = 80;

	/**
	 * The font to use to render the IDs.
	 */
	private static final Font font = new Font("ARIAL", Font.PLAIN, 14);

	/**
	 * The current set of IDs.
	 */
	private byte[][] observation;

	/**
	 * Translation of the observation, that is where its (0,0) is in world coordinates.
	 */
	private int baseX, baseY;

	/**
	 * Opacity of the text.
	 */
	private float alpha = 1;// 0.75f;

	/**
	 * if true, this renderer draws static objects, if false, moving objects
	 */
	private final boolean drawsStaticObjects;

	private final boolean colorUnknownIdsDifferently = true; //for debugging purposes, slightly time consuming
	
	/**
	 * Constructor
	 * 
	 * @param drawsStaticObjects
	 *            if true, this renderer draws static objects, if false, moving objects
	 */
	public IdGridRenderer(boolean drawsStaticObjects) 
	{
		this.drawsStaticObjects = drawsStaticObjects;
	}

	@Override
	public void prepareDrawing(LevelScene levelScene, int playerIndex) 
	{
		observation = drawsStaticObjects ? levelScene.levelSceneObservation(0,levelScene.getPlayers().get(playerIndex)) : levelScene.enemiesObservation(0,levelScene.getPlayers().get(playerIndex));
		baseX = levelScene.getPlayers().get(playerIndex).mapX - Environment.HalfObsWidth;
		baseY = levelScene.getPlayers().get(playerIndex).mapY - Environment.HalfObsHeight;
	}

	@Override
	public void drawRect(Graphics g, int levelX, int levelY, Rectangle rect) {
		if (observation == null) {
			return;
		}
		if (rect.y <= CUT_OFF_Y)
			return;
		int x = levelX - baseX;
		int y = levelY - baseY;
		if (y < 0 || y >= observation.length)
			return;
		if (x < 0 || x >= observation[0].length)
			return;
		byte id = observation[y][x];
		String idString = String.valueOf(id);

		// choose smaller font until the string fits in one cell
		Font font = IdGridRenderer.font;
		int stringWidth;
		while ((stringWidth = g.getFontMetrics(font).stringWidth(idString)) > IGridRectRenderer.REGULAR_RECT_WIDTH) {
			float newSize = font.getSize() - 1f;
			font = font.deriveFont(newSize);
		}
		g.setColor(new Color(0.5f, 1f, 1f, alpha));
		
		if(colorUnknownIdsDifferently) {
			ObjectType objectType = ObjectType.getObjectType(drawsStaticObjects);
			if(PlayerWorldObject.getElement(id, objectType, 0)==PlayerWorldObject.UNDEFINED) {
				g.setColor(Color.RED);
			}
		}
		g.setFont(font);

		// getting the text to render in the middle of the tile
		// requires some adjustments
		// y-adjustment
		int maxAscent = g.getFontMetrics().getMaxAscent();
		int textOffsetY = (IGridRectRenderer.REGULAR_RECT_HEIGHT - maxAscent) / 2;
		// x-adjustment
		int textOffsetX = (IGridRectRenderer.REGULAR_RECT_WIDTH - stringWidth) / 2;

		if (!drawsStaticObjects && !idString.equals("0")) {
			g.setColor(Color.BLUE);
		}
		g.drawString(idString, rect.x + textOffsetX, rect.y - textOffsetY);
	}

	public float getAlpha() {
		return alpha;
	}

	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}
}
