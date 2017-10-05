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

import marioAI.movement.heuristics.FineGrainPotentialField;
import marioWorld.engine.LevelScene;
import marioWorld.engine.coordinates.GlobalContinuous;

public class JumpRangeRadius extends ToggleableOverlay{
	
	
	GlobalContinuous playerPos;
	

	public JumpRangeRadius(GlobalContinuous playerPos) {
		super("JumpRange");
		this.playerPos = playerPos;

	}

	/**
	 * Draws the points at the position to which they belong.
	 * 
	 */
	public void drawOverlay(Graphics g, LevelScene levelScene, int playerIndex) 
	{
		
		ArrayList<GlobalContinuous> maxJumpingDistance = new ArrayList<>();
		
		for (int i = -1* (int) FineGrainPotentialField.MAX_JUMP_VALUE; i < FineGrainPotentialField.MAX_JUMP_VALUE; i++) {
			for (int j = -1* (int) FineGrainPotentialField.MAX_JUMP_VALUE; j < FineGrainPotentialField.MAX_JUMP_VALUE; j++) {
				if ((int) Math.sqrt(i*i+j*j) == FineGrainPotentialField.MAX_JUMP_VALUE) {
					maxJumpingDistance.add(new GlobalContinuous(playerPos.getX() + i, playerPos.getY() + j));
				}
			}
		}
		
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(Color.RED);
		g2.setStroke(new BasicStroke(1));

		// coordinates of the camera in continuous world coordinates
		float xCam = levelScene.cameras.get(playerIndex)[0];
		float yCam = levelScene.cameras.get(playerIndex)[1];

		for (int i = 1; i < maxJumpingDistance.size(); i++) {

			g2.drawRect((int) (maxJumpingDistance.get(i).x - xCam),
					(int) (maxJumpingDistance.get(i).y - yCam), 1, 1);

		}
	}

}
