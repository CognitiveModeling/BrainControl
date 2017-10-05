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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import marioWorld.engine.sprites.Player;

public class HealthPanel extends JPanel {



	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		this.setPreferredSize(new Dimension(0, 15));
		// g.drawString("This is my custom Panel!",10,20);
		// g.setColor(Color.RED);
		// g.fillRect(squareX,squareY,squareW,squareH);
		// g.setColor(Color.BLACK);
		// g.drawRect(squareX,squareY,squareW,squareH);

	}

	public void draw(int healthLevel) {
		int squareW = this.getWidth();
		int squareH = this.getHeight()-2;
		int squareX = 0;
		int squareY = 0;
		int widthX = (int)((double)squareW / (double)(Player.healthLevelScaling*2));
		Graphics g = this.getGraphics();
		g.clearRect(0, 0, squareW, squareH);

		g.setColor(getColorForHealth(healthLevel));
		for(int i = 0; i<healthLevel;i++){
			
			g.fillRect(squareX+ i * widthX +2 , squareY, widthX, squareH);
			
		}
		g.setColor(Color.BLACK);
		for(int j =0; j <Player.healthLevelScaling*2;j++) {
			
			g.drawRect(squareX+ j * widthX, squareY, widthX-2, squareH);
		}


	}

	private Color getColorForHealth(int healthLevel) {
		if(healthLevel<=1) {
			return Color.RED;
		}
		if(healthLevel <=4) {
			return Color.YELLOW;
		}else {
			return Color.GREEN;
		}
		
	
	}
}
