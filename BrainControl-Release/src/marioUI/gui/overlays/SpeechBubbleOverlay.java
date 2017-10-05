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
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import marioAI.overlay.ToggleableOverlay;
import marioUI.gui.SpeechBubble;
import marioWorld.engine.LevelScene;
import marioWorld.engine.sprites.Player;

public class SpeechBubbleOverlay extends ToggleableOverlay {

	private static final long BUBBLE_MAX_SHOW_TIME_MS = 2000; // show speech bubble for max. 2s
	private static Map<Integer,SpeechBubble> speechBubbles = new TreeMap<Integer,SpeechBubble>();
	
	public SpeechBubbleOverlay(String name) {
		super(name);
	}
	
	/**
	 * Called from another thread, must be synchronized 
	 * 
	 * @param playerId id of speaking player
	 * @param text spoken text
	 * @param posx x position of pointy end of bubble
	 * @param posy y position of pointy end of bubble
	 */
	public static synchronized void setBubbleText(int playerId, String text, Player player, int relPosx, int relPosy, boolean expireAfterTimeout) {
		SpeechBubble bubble = speechBubbles.get(playerId);
		if (bubble == null) {
			bubble = new SpeechBubble(text,player,playerId,expireAfterTimeout);
			speechBubbles.put(playerId, bubble);
		}
		bubble.setRelativePosition(relPosx, relPosy);
	}
	
	/**
	 * Called from another thread, must be synchronized 
	 */
	@Override
	public synchronized void drawOverlay(Graphics g, LevelScene levelScene, int playerIndex) {
		// check expiration
		for (int i = 0; i < speechBubbles.size(); i++) {
			SpeechBubble bubble = speechBubbles.get(i);
			if ((bubble != null) && bubble.isExpired(BUBBLE_MAX_SHOW_TIME_MS)) {
				speechBubbles.put(i, null);
			}
		}
		for (Entry<Integer, SpeechBubble> e : speechBubbles.entrySet()) {
			SpeechBubble bubble = e.getValue();
			if (bubble != null)
				bubble.draw((Graphics2D) g, levelScene, playerIndex);
		}
	}

	public static void clearBubbleText(int playerIndex) {
		speechBubbles.put(playerIndex, null);
	}

}
