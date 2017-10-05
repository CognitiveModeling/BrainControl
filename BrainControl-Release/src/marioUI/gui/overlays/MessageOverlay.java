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
package marioUI.gui.overlays;

import java.awt.Graphics;
import java.awt.Graphics2D;


import marioAI.overlay.ToggleableOverlay;

import marioWorld.engine.LevelScene;

public class MessageOverlay extends ToggleableOverlay {

	private static final long BUBBLE_MAX_SHOW_TIME_MS = 2000; // show speech bubble for max. 2s
	private static MessageBox messageBox;
	
	public MessageOverlay(String name) {
		super(name);
		this.setActive(true);
	}
	
	/**
	 * Called from another thread, must be synchronized 
	 * 
	 * @param text spoken text
	 * @param posx x position of pointy end of bubble
	 * @param posy y position of pointy end of bubble
	 */
	public static synchronized void setBoxText(String text, boolean expireAfterTimeout) {
		
		if (messageBox == null) {
			messageBox = new MessageBox(text,expireAfterTimeout);
		}

	}
	
	/**
	 * Called from another thread, must be synchronized 
	 */
	@Override
	public synchronized void drawOverlay(Graphics g, LevelScene levelScene, int playerIndex) {
		// check expiration
		
		
		if ((messageBox != null) && messageBox.isExpired(BUBBLE_MAX_SHOW_TIME_MS)) {
			messageBox = null;
		}
		
		
		
			if (messageBox != null){
				messageBox.draw((Graphics2D) g, levelScene, playerIndex);
		}
	}

	public static void clearBubbleText() {
		messageBox = null;
	}
}
