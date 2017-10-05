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
package marioWorld.engine;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyListener;
import java.awt.image.VolatileImage;

import javax.swing.JComponent;

/*
 * @author: Fabian Schrodt
 * */
public class GameWorldComponent extends JComponent
{
	private static final long serialVersionUID = 1L;
	
	//public GraphicsConfiguration graphicsConfiguration;
	
	public KeyListener prevHumanKeyBoardAgent;	
	
	@Override
	public void paint(Graphics g)
	{
	}

	@Override
	public void update(Graphics g)
	{
	}
	
	/*public OverlayWindow getOverlayWindow()
	{
		return overlayWindow;
	}*/
		
	//CHECK: unused function??
	/*protected void drawImage(final VolatileImage image)
	{
		getGraphics().drawImage(image, 0, 0, GlobalOptions.xScreen, GlobalOptions.yScreen, null);
	}*/	
	
	public void doVisualization(Graphics og, boolean[] action, long tick,
			int currentTrial, int totalNumberOfTrials)
	{
		//String agentName = "1stAgent: " + agents.get(0).getName();
		// LevelScene.drawStringDropShadow(og, agentName, 0, 7, 5);

		//String selectedActions = "Selected Actions: ";
		// LevelScene.drawStringDropShadow(og, selectedActions, 0, 8, 6);

		/** This draws text to indicate which key is pressed */
//		String actions = "";
//		if (action != null)
//		{
//			for (int i = 0; i < Environment.numberOfButtons; ++i)
//				actions += (action[i]) ? Scene.keysStr[i] : "      ";
//		} else
//			actions = "NULL";
//		drawString(og, actions, 6, 78, 1);

		/** This draws 'Click to play' in the beginning */
		if (!this.hasFocus() && tick / 4 % 2 == 0)
		{
			// String msgClick = "CLICK TO PLAY";
			// og.setColor(Color.YELLOW);
			// og.drawString(msgClick, 320 + 1, 20 + 1);
			// int posx = 160 - msgClick.length() * 4;
			// drawString(og, msgClick, posx, 110, 1);
			// drawString(og, msgClick, posx, 110, 7);
		}
		og.setColor(Color.DARK_GRAY);

		/** This draws current fps */
		// LevelScene.drawStringDropShadow(og, "FPS: ", 33, 2, 7);

//		String fps;
//		if (GlobalOptions.FPS > 99)
//		{
//			fps = "\\infty";
//		} else
//		{
//			fps = GlobalOptions.FPS.toString();
//		}
//      LevelScene.drawStringDropShadow(og, fps, 33, 3, 7);

		/** This draws current trial */
//		String trials = "";
//		if (totalNumberOfTrials != -2)
//		{
//			trials = currentTrial + "(";
//			if (totalNumberOfTrials == -1)
//			{
//				trials += "\\infty";
//			} else
//			{
//				trials += totalNumberOfTrials;
//			}
//			trials += ")";
//		}
//
//		LevelScene.drawStringDropShadow(og, "Trial:", 33, 4, 7);
//		LevelScene.drawStringDropShadow(og, trials, 33, 5, 7);
	}	
	
	public GameWorldComponent()
	{
		setFocusable(true);
		setEnabled(true);

		Dimension size = new Dimension(GlobalOptions.xScreen, GlobalOptions.yScreen);

		setPreferredSize(size);
		setMinimumSize(size);
		setMaximumSize(size);
		
		//overlayWindow = new OverlayWindow(overlays);
		
		setFocusable(true);
	}
}
