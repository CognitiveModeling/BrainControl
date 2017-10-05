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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Iterator;

import javax.swing.JPanel;

import marioAI.reservoirMotivation.AbstractReservoir;


/**
 * Draws the Graph for the ReservoirGUI 
 * @author Jannine 
 *
 */

public class GraphPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private AbstractReservoir reservoir;
	private ReservoirCoordinates coordinates;
	private int elapsedTime;
	public final int offset = 13; //space between the axis and the edge of the window

	
	public GraphPanel(AbstractReservoir reservoir){
		this.reservoir = reservoir;
		this.coordinates = new ReservoirCoordinates();
	}
	
	/**
	 * computes the position of the new coordinate in the coordinate system
	 * @param minTime
	 * @param graphWidth
	 * @param graphHeight
	 * @param coordinate
	 * @return GUIPoint 
	 */
	private Point getGuiPoint(int minTime, int graphWidth, int graphHeight, ReservoirGuiCoordinate coordinate) {
		double ratioX = (double)(coordinate.time - minTime)/coordinates.displayedTimeInterval;
		int x = (int)(ratioX*graphWidth) + offset;

		double ratioY = (double)(reservoir.maxValue - coordinate.value)/(reservoir.maxValue-reservoir.minValue);
		int y = (int)(ratioY*graphHeight) + offset;
		return new Point(x, y);
	}
	
	/**
	 * Painting of the Graph
	 */
	
	@Override
	public void paint(Graphics g) {

		
		//drawing the frame of the Graph
		Rectangle r = g.getClipBounds();
		g.setColor(Color.RED);
		g.drawRect(0, 0, (int)r.getWidth(), (int)r.getHeight());

		
		setBackground(Color.WHITE);
		
		g.setColor(Color.GRAY);
		
		
		final int minTime = Math.max(0, elapsedTime - coordinates.displayedTimeInterval);
//		final int maxTime = Math.max(elapsedTime, coordinates.displayedTimeInterval);
		final int graphWidth = (int)r.getWidth() - 2*offset;
		final int graphHeight = (int)r.getHeight() - 2*offset;
		
		//drawing the x axis
		g.drawLine(offset, (int)r.getHeight() - offset, (int)r.getWidth() - offset, (int)r.getHeight() - offset);
		//drawing the y axis
		g.drawLine(offset, offset, offset, (int)r.getHeight() - offset);
		
		g.setColor(Color.BLACK);
		
		//bold shows which reservoir is active and the name of it is to bold 
		if(reservoir.bold)
		{
			g.setFont(new Font("default", Font.BOLD, 14));
		}
		//draw name of reservoir
		g.drawString(reservoir.getName(), (int)(r.getWidth()/2-offset), offset);
		
		g.setFont(new Font("default", Font.PLAIN, 12));
		
		
		//String beginX = String.valueOf(minTime);
		//String endX = String.valueOf(coordinates.displayedTimeInterval + minTime);
		
		//label of the x axis
//		g.drawString(beginX, offset, (int)r.getHeight() - offset + 15);
//		g.drawString(endX, (int)r.getWidth() - offset,(int)r.getHeight() - offset + 15);
		
		//label of the y axis
		g.drawString("0", offset - 10, (int)r.getHeight() - offset);
		g.drawString("1", offset - 10, offset + 10);


		//drawing the graph: showing all the lines which connect the values that are saved in the list ReservoirCoordinate
		if(coordinates.size() > 1) {
			Iterator<ReservoirGuiCoordinate> it = coordinates.iterator();
			ReservoirGuiCoordinate lastCoordinate = it.next();
			Point lastGuiPoint = getGuiPoint(minTime, graphWidth, graphHeight, lastCoordinate);
			Point nextGuiPoint = null;
			while(it.hasNext()) {
				ReservoirGuiCoordinate nextCoordinate = it.next();
				nextGuiPoint = getGuiPoint(minTime, graphWidth, graphHeight, nextCoordinate);
				g.drawLine(lastGuiPoint.x, lastGuiPoint.y, nextGuiPoint.x, nextGuiPoint.y);
				lastGuiPoint = nextGuiPoint;
			}
		}
	
	}
	
	/**
	 * add a coordinate to the list
	 * @param elapsedTime
	 */
	public void addPoint(int elapsedTime) {
		coordinates.addLast(new ReservoirGuiCoordinate(elapsedTime, reservoir.getValue()));
	}
	
	public void setElapsedTime(int elapsedTime) {
		this.elapsedTime = elapsedTime;
	}
	
	
	

}
