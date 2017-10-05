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
package marioUI.gui;

import java.awt.event.MouseEvent;

import marioWorld.engine.GlobalOptions;
import marioWorld.engine.LevelScene;
import marioWorld.engine.coordinates.GlobalCoarse;
import marioWorld.engine.coordinates.GlobalContinuous;

/**
 * Keeps track of the mouse input from the mouse listener of the differenct screen panels.
 * Is situated in the CAEAgent
 * @author Yves
 *
 */
public class MouseInputController {
	private MouseEvent lastMouseEvent;
	private MouseEvent currentMouseEvent;
	private boolean overlayActive;
	private GlobalContinuous selectedTile = new GlobalContinuous();
	private boolean newInput;
	
	
	
	public boolean isOverlayActive() {
		return overlayActive;
	}



	public void setOverlayActive(boolean overlayActive) {
		this.overlayActive = overlayActive;
	}



	public void processInput(MouseEvent e) {
		currentMouseEvent = e;
		overlayActive = true;
		newInput = true;
		System.out.println(e.getX());
		System.out.println(e.getY());	
		
	}




	public GlobalContinuous getSelectedTilePosition(LevelScene levelScene, int playerIndex) {
		
		if(newInput) {
			GlobalContinuous test = new GlobalContinuous(currentMouseEvent.getX(),currentMouseEvent.getY());

			float x = (int) ((int) (test.x) + ((levelScene.cameras.get(playerIndex)[0] * GlobalOptions.resolution_factor)));
			float y = (int) (test.y /  GlobalOptions.resolution_factor);
			GlobalContinuous res = new GlobalContinuous(x/GlobalOptions.resolution_factor,y);
			GlobalCoarse resCorase = res.toGlobalCoarse();
			if(selectedTile.toGlobalCoarse().equals(resCorase)) {
				overlayActive = false;
				selectedTile = new GlobalContinuous(-1,-1);
			} else {
				this.selectedTile = resCorase.toGlobalContinuous();
			}
			
			
			newInput = false;
		}
		return selectedTile;
		
	}



	public MouseEvent getLastMouseEvent() {
		return lastMouseEvent;
	}



	public void setLastMouseEvent(MouseEvent lastMouseEvent) {
		this.lastMouseEvent = lastMouseEvent;
	}



	public MouseEvent getCurrentMouseEvent() {
		return currentMouseEvent;
	}



	public void setCurrentMouseEvent(MouseEvent currentMouseEvent) {
		this.currentMouseEvent = currentMouseEvent;
	}
	
	
	
}
