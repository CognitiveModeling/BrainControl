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
 * 
 * Draws the points on the map where a jump is possible. Mainly used to check if the function that finds the points works correct.
 * 
 * @author Marcel
 * 
 */

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;

import marioWorld.engine.LevelScene;
import marioWorld.engine.coordinates.GlobalContinuous;

public class JumpPointsOverlay extends ToggleableOverlay {
	
	
	/**
	 * ArrayList filled with points where jumping is possible.
	 */
	ArrayList<GlobalContinuous> jumpPoints = new ArrayList<>();

	public JumpPointsOverlay(ArrayList<GlobalContinuous> jumpPoints) {
		super("Jumppoints");
		this.jumpPoints = jumpPoints;

	}

	/**
	 * Draws the points at the position to which they belong.
	 * 
	 */
	public void drawOverlay(Graphics g, LevelScene levelScene, int playerIndex) 
	{
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(Color.RED);
		g2.setStroke(new BasicStroke(1));

		// coordinates of the camera in continuous world coordinates
		float xCam = levelScene.cameras.get(playerIndex)[0];
		float yCam = levelScene.cameras.get(playerIndex)[1];

		for (int i = 1; i < jumpPoints.size(); i++) {

			g2.drawRect((int) (jumpPoints.get(i).x - xCam),
					(int) (jumpPoints.get(i).y - yCam), 1, 1);

		}
	}

}
