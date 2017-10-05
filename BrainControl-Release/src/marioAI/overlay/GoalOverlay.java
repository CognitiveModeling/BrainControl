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
import java.awt.Image;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import marioAI.collision.ConditionDirectionPair.CollisionDirection;
import marioAI.goals.Goal;
import marioAI.movement.AStarSimulator;
import marioAI.movement.GoalSearchWrapper;
import marioAI.movement.WorldGoalTester;
import marioWorld.engine.Art;
import marioWorld.engine.GlobalOptions;
import marioWorld.engine.LevelScene;
import marioWorld.engine.coordinates.GlobalContinuous;
import marioWorld.utils.ResourceStream;

public class GoalOverlay extends ToggleableOverlay {

	private static final int halfOfSize = 3 * GlobalOptions.resolution_factor;
	// private static final Color goalColor = Color.RED;
	// private static final Color intermediateGoalColor = Color.GREEN;

	private AStarSimulator simulator;
	private int ownerPlayerIndex;

	private Goal indicate_goal_not_reached = null;

	public GoalOverlay(AStarSimulator simulator) {
		super("goal overlay");
		this.simulator = simulator;
		this.ownerPlayerIndex = simulator.getCurrentSimulatedLevelScene().getPlanningPlayerIndex();
	}

	public GoalOverlay(AStarSimulator simulator, Goal indicate_goal_not_reached) {
		super("goal overlay");
		this.simulator = simulator;
		this.ownerPlayerIndex = simulator.getCurrentSimulatedLevelScene().getPlanningPlayerIndex();

		this.indicate_goal_not_reached = indicate_goal_not_reached;
	}



	@Override
	public void drawOverlay(Graphics g, LevelScene levelScene, int playerIndex) {
		for (GoalSearchWrapper wgt : simulator.getAllGoals()) {
			if (wgt == null) // WHY?
				continue;
			if (wgt.getGoal() == null) // WHY?
				continue;
			if (wgt.getGoal().getPosition() == null) // WHY?
				continue;

			drawGoal(wgt.getGoal(), g, levelScene, playerIndex, 0);
		}

		if (indicate_goal_not_reached != null)
			drawGoal(indicate_goal_not_reached, g, levelScene, playerIndex, 1);
	}


	private void drawGoal(Goal goal, Graphics g, LevelScene levelScene, int playerIndex, int type) {
		int x, y;

		GlobalContinuous goalPos = goal.getPosition().toGlobalContinuous();
		x = (int) (goalPos.x - levelScene.cameras.get(playerIndex)[0]) * GlobalOptions.resolution_factor;
		y = (int) (goalPos.y - levelScene.cameras.get(playerIndex)[1]) * GlobalOptions.resolution_factor;

		if (x < 0 || y < 0)
			return;

		BufferedImage img = (BufferedImage) Art.cursor.getImage(ownerPlayerIndex, type);

		Graphics2D g2 = (Graphics2D) g;

		int cursorLocationX = x - 10 * GlobalOptions.resolution_factor;
		int cursorLocationY = y - 10 * GlobalOptions.resolution_factor;
		double imgWidth = img.getWidth(null) / 2;
		double imgHeight = img.getHeight(null) / 2;

		double rotationAngle;
		AffineTransform at;
		AffineTransformOp op;

		CollisionDirection direction = goal.getDirection();
		if (direction == null) {
			rotationAngle = Math.toRadians(45);
			at = AffineTransform.getRotateInstance(rotationAngle, imgWidth, imgHeight);
			op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
			g2.drawImage(op.filter(img, null), cursorLocationX, cursorLocationY, null);
			return;
		}

		// calculate rotation angle and location of the cursor dependent on the direction
		switch (direction) {
		case ABOVE:
			cursorLocationY -= img.getHeight(null) / 2;
			rotationAngle = Math.toRadians(180);
			break;
		case LEFT:
			cursorLocationX -= img.getWidth(null) / 2;
			rotationAngle = Math.toRadians(90);
			break;
		case BELOW:
			cursorLocationY += img.getHeight(null) / 2;
			rotationAngle = Math.toRadians(0);
			break;
		case RIGHT:
			cursorLocationX += img.getWidth(null) / 2;
			rotationAngle = Math.toRadians(270);
			break;
		case IRRELEVANT:
			cursorLocationX -= img.getWidth(null) / 2;
			cursorLocationY += img.getHeight(null) / 2;
			rotationAngle = Math.toRadians(45);
			break;
		default:
			throw new IllegalArgumentException("In GoalOverlay.drawArrow: Direction was " + direction);
		}
		at = AffineTransform.getRotateInstance(rotationAngle, imgWidth, imgHeight);
		op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		g2.drawImage(op.filter(img, null), cursorLocationX, cursorLocationY, null);

	}

}
