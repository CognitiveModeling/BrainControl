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

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.LinkedList;

import javax.swing.JPanel;

import marioAI.goals.ReservoirMultiplePlanner;
import marioAI.reservoirMotivation.AbstractReservoir;
import marioWorld.engine.LevelScene;
import marioWorld.engine.Tickable;


/**
 * this class is the visual representation of the players reservoirs  
 * @author Jonas Einig, Marcel 
 *
 */
public class ReservoirGuiTab extends JPanel implements Tickable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** at the moment you can use the reservoirIllustration with ReservoirDoublePlanner
	 * and ReservoirMultiplePlanner, but when ReservoirDoublePlanner isn't used anymore by
	 * the VoicePlanner you could delete the constructor for the reservoirDoublePlanner*/
	private ReservoirMultiplePlanner reservoirDoublePlanner = null;
		
	private final int repaintInterval = 1; //interval after that the graphic is repainted
	//list of all graphs
	private LinkedList<GraphPanel> panelList = new LinkedList<GraphPanel>();
	
//	/**
//	 * constructor for ReservoirMultiplePlanner
//	 * draws GraphPanels for every reservoir and shows all in one frame
//	 * @param reservoirMultiplePlanner
//	 */
//	public ReservoirGuiTab(ReservoirMultiplePlanner reservoirMultiplePlanner) {
//		this.reservoirMultiplePlanner = reservoirMultiplePlanner;
//		setName("Reservoir GUI");
////		Container container = getContentPane();
//		
//		this.setLayout(new GridLayout(0, 2));
//		
//		//construction of the graphPanels 
//		for(AbstractReservoir reservoir: this.reservoirMultiplePlanner.getReservoirList()) {
//			GraphPanel newPanel = new GraphPanel(reservoir);
//			this.panelList.add(newPanel);
//			this.add(newPanel);
//		}
//		
//		
//	}
	
	
	/**
	 * constructor for ReservoirMultiplePlanner
	 * draws GraphPanels for every reservoir and shows all in one frame
	 * @param reservoirDoublePlanner
	 */
	public ReservoirGuiTab(ReservoirMultiplePlanner reservoirDoublePlanner) {
		this.reservoirDoublePlanner = reservoirDoublePlanner;
		setName("Reservoir GUI");
		
		this.setLayout(new GridLayout(0, 2));


		//construction of the graphPanels 
		for(AbstractReservoir reservoir: this.reservoirDoublePlanner.getReservoirList()) {
			GraphPanel newPanel = new GraphPanel(reservoir);
			this.panelList.add(newPanel);
			this.add(newPanel);
		}
		
		setLocation(350, 10);
//		setSize(new Dimension(500, 700));
		setSize(new Dimension(700, 700));
	}
	
	/**
	 * if a new reservoir is added in the ReservoirMultiplePlanner,
	 * it is added to the Reservoir Gui
	 * but: we don't use the ability to add new reservoirs, so this method isn't tested often
	 * @param newReservoir
	 */
	public void refresh(AbstractReservoir newReservoir) {
		GraphPanel newPanel = new GraphPanel(newReservoir);
		this.panelList.add(newPanel);
		this.add(newPanel);
	}

	/**
	 * Graphs are repaint after the repaintInterval
	 */
	@Override
	public void onTick(LevelScene levelScene) {
		if (levelScene != null) {
			int elapsedTime = levelScene.startTime;
			if (elapsedTime % repaintInterval == 0) 
			{				
				for (GraphPanel panel : panelList) {
					panel.setElapsedTime(elapsedTime);
					//add a point to the ReservoirCoordinates list
					panel.addPoint(elapsedTime);
				}

				this.repaint();
			}
		}
		
	}
}
