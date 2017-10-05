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

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.JProgressBar;

import marioAI.agents.CAEAgent;
import marioAI.reservoirMotivation.AbstractReservoir;
import marioUI.gui.overlays.MotivationsOverlay;
import marioUI.gui.overlays.ObjectsOverlay;
import marioWorld.engine.IScreenOverlay;
import marioWorld.engine.LevelScene;

/**
 * Base class for interfaces toggleable from the GUI.
 * 
 * @author sebastian
 * 
 */
public class ToggleableOverlay implements IScreenOverlay {

	protected boolean onGame = true;
	private boolean active = false;
	private final String name;

	public ToggleableOverlay(String name) {
		this.name = name;
		// instantiate to maintain the active value, while not being able to
		// draw
	}

	@Override
	public boolean shouldDraw() {
		return active;
	}

	
	
	public boolean isOnGame() {
		return onGame;
	}

	/**
	 * Returns whether this instance is active (and should thus be rendered).
	 * 
	 * @return
	 */
	public boolean getActive() {
		return active;
	}

	/**
	 * Sets whether to render this instance or not.
	 * 
	 * @param active
	 */
	public void setActive(boolean active) {
		this.active = active;
	}
	


	@Override
	public void drawOverlay(Graphics g, LevelScene levelScene, int playerIndex) {
		// do nothing
	}

	@Override
	public String getName() {
		return name;
	}

	public MotivationsOverlay updateOverlayInformation(ArrayList<JProgressBar> resBars,
			LinkedList<AbstractReservoir> resList, CAEAgent agent) {
		// TODO Auto-generated method stub
		return null;
	}

	public ObjectsOverlay updateOverlayInformation() {
		// TODO Auto-generated method stub
		return null;
	}
}
