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

import marioAI.movement.WorldNode;
import marioWorld.engine.LevelScene;
import marioWorld.engine.coordinates.CoordinatesUtil;
import marioWorld.engine.coordinates.GlobalContinuous;

/**
 * Overlay that paints a path of WorldNodes.
 * 
 * @author Niels
 * 
 */
public class WorldPathOverlay extends ToggleableOverlay {

	protected final List<GlobalContinuous> drawPath;
	

	public WorldPathOverlay(List<WorldNode> path) {
		super("desired path overlay");
		drawPath = new ArrayList<GlobalContinuous>();

		if (path.size() < 2) {
			return;
		}

		for (int i = 0; i < path.size(); i++) {
			// actual node positions in the world are the simulated positions of
			// mario
			GlobalContinuous worldPos = path.get(i).getPlayerPos();
			drawPath.add(worldPos);
		}
	}

	@Override
	public void drawOverlay(Graphics g, LevelScene levelScene, int playerIndex) 
	{
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(Color.RED);
		g2.setStroke(new BasicStroke(1));

		// coordinates of the camera in continuous world coordinates
		float xCam = levelScene.cameras.get(playerIndex)[0];
		// adding half a tile to the y coordinate, to move drawn path from
		// mario's feet to his belly
		float yCam = levelScene.cameras.get(playerIndex)[1] + (CoordinatesUtil.UNITS_PER_TILE >> 1);

		GlobalContinuous start;
		if (!drawPath.isEmpty()) {
			start = drawPath.get(0);

			for (int i = 1; i < drawPath.size(); i++) {
				GlobalContinuous end = drawPath.get(i);
				g2.drawLine((int) (start.x - xCam), (int) (start.y - yCam), (int) (end.x - xCam), (int) (end.y - yCam));
				start = end;
			}
		}

	}
}
