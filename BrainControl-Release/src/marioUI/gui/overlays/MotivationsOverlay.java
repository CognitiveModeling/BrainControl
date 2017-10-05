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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.GrayFilter;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JProgressBar;

import marioAI.agents.CAEAgent;
import marioAI.overlay.ToggleableOverlay;
import marioAI.reservoirMotivation.AbstractReservoir;
import marioWorld.engine.Art;
import marioWorld.engine.GlobalOptions;
import marioWorld.engine.LevelScene;
import marioWorld.engine.coordinates.GlobalCoarse;
import marioWorld.utils.ResourceStream;

public class MotivationsOverlay extends ToggleableOverlay{
	ArrayList<JProgressBar> reservoirBars;
	LinkedList<AbstractReservoir> reservoirList;
	CAEAgent agent;
	int imageMaximumSize;
	String goalReservoir;
	
	public MotivationsOverlay(ArrayList<JProgressBar> resBars, LinkedList<AbstractReservoir> resList,CAEAgent agent) {
		super("motivations overlay");
		this.setActive(true);
		reservoirBars = resBars;
		reservoirList = resList;
		this.agent =agent;
		// TODO Auto-generated constructor stub
	}
	
	
	/**
	 * Called from another thread, must be synchronized 
	 */
	@Override
	public synchronized void drawOverlay(Graphics g, LevelScene levelScene, int playerIndex) {
		
		if(reservoirBars == null) {
			return;
		}
		if(agent != null && agent.getPlayerIndex() != GlobalOptions.playerGameWorldToDisplay) {
			return;
		}
		

		Image[] motivations= calculateImages();
		
		int imageSize = imageMaximumSize;
		int padding = 3;
		
		
		int boxWidth = (imageSize * motivations.length ) + (padding *  motivations.length );
		int boxHeight = (imageSize)+padding*2;
		
		int yTopleftCorner = GlobalOptions.yScreen -(boxHeight+1);
		int xTopleftCorner = 0;		
		
	
		
		Graphics2D g2d =(Graphics2D) g;

		
		
		
		g2d.setColor(new Color(255, 255, 255, 200));
		
		g2d.fill3DRect(xTopleftCorner,yTopleftCorner , boxWidth, boxHeight, true);
		g2d.setColor(Color.GRAY);
		g2d.draw3DRect(xTopleftCorner+1,yTopleftCorner+1 , boxWidth-1, boxHeight-1, true);
		

		g2d.setColor(null);
		for(int i = 0; i<motivations.length; i++) {
			
			int xOffset = (xTopleftCorner+ i*imageSize) +(  (boxWidth - (motivations.length*imageSize)) / motivations.length );
			int yOffset = (yTopleftCorner + ((boxHeight-motivations[i].getHeight(null))/2));
			g2d.drawImage(motivations[i], xOffset, yOffset, null);
		}
		
		
	
	}
	@Override
	public MotivationsOverlay updateOverlayInformation(ArrayList<JProgressBar> resBars, LinkedList<AbstractReservoir> resList,CAEAgent agent) {
		
		this.setActive(true);
		reservoirBars = resBars;
		reservoirList = resList;
		this.agent =agent;
		
		
		return this;
	}
	
	


	private Image[] calculateImages(){
		int size = reservoirBars.size();
		Image[] images = new Image[size];
		Image[] grayImages = new Image[size];
 		
		String[] reservoirNames = new String[size];
		double[] resValues = new double[size];
		int[] imageSizes = new int[size];
		
		images[0] = Art.energy;
		grayImages[0] = Art.grayEnergy;
		
		images[1] = Art.progress;
		grayImages[1] = Art.grayProgress;
				
		images[2] = Art.curiosity;
		grayImages[2] = Art.grayCuriosity;
				
		images[3] = Art.health;
		grayImages[3] = Art.grayHealth;
				
		imageMaximumSize = images[0].getWidth(null);

		for(int i =0; i<size;i++) {
			reservoirNames[i] = this.reservoirList.get(i).getName();
			
			if(goalReservoir == null || goalReservoir != reservoirNames[i]){
				images[i] = grayImages[i];
			}
			
			
			resValues[i] = this.reservoirBars.get(i).getValue();
			imageSizes[i] = (int)(images[i].getWidth(null) * (((resValues[i]/100) * 0.5) +0.5));
			images[i] = Art.createResizedCopy(images[i], imageSizes[i], imageSizes[i], true);
//			if(resValues[i] == 0) {
//				images[i] = GrayFilter.createDisabledImage(images[i]);
//			}
		}

		return images;
	}
	
	
	
	
	public String getGoalReservoir() {
		return goalReservoir;
	}


	public void setGoalReservoir(String goalReservoir) {
		this.goalReservoir = goalReservoir;
	}

}
