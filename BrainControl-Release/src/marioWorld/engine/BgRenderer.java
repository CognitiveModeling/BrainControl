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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Point;
import java.awt.Transparency;

import marioWorld.engine.level.Level;

public class BgRenderer 
{
	private int xCam;
	private int yCam;
	
	private Image image;
	private Graphics2D g;
	private static final Color transparent = new Color(0, 0, 0, 0);
	private Level level;

	public boolean renderBehaviors = false;

	private int width;
	private int height;
	private int distance;

	public BgRenderer(Level level, GraphicsConfiguration graphicsConfiguration, int width, int height, int distance) {
		this.distance = distance;
		this.width = width;
		this.height = height;
		
		this.level = level;
		image = graphicsConfiguration.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
		g = (Graphics2D) image.getGraphics();
		g.setComposite(AlphaComposite.Src);

		updateArea(0, 0, width, height);
	}

	public void setCam(int xCam, int yCam) 
	{
		xCam /= distance;
		yCam /= distance;
		//int xCamD = this.xCam - xCam;
		//int yCamD = this.yCam - yCam;
		this.xCam = xCam + width/2;	//shift background
		this.yCam = yCam;

		//original, not working:
		/*g.setComposite(AlphaComposite.Src);
		g.copyArea(0, 0, width, height, xCamD, yCamD);
		
		if (xCamD < 0) {
			if (xCamD < -width)
				xCamD = -width;
			updateArea(width + xCamD, 0, -xCamD, height);
		} else if (xCamD > 0) {
			if (xCamD > width)
				xCamD = width;
			updateArea(0, 0, xCamD, height);
		}

		if (yCamD < 0) {
			if (yCamD < -width)
				yCamD = -width;
			updateArea(0, height + yCamD, width, -yCamD);
		} else if (yCamD > 0) {
			if (yCamD > width)
				yCamD = width;
			updateArea(0, 0, width, yCamD);
		}*/
		
		//fallback, working:
		updateArea(0, 0, width, height);

	}

	private int corXCam()
	{
		return xCam*GlobalOptions.resolution_factor;
	}

	private int corYCam()
	{
		return yCam*GlobalOptions.resolution_factor;
	}	
	
	/**
	 * paint a tool tip which works different from default tooltips. This one is triggered by mouse pressed/released and positioned at a mouse location, not GUI
	 * Components.
	 */
	private Point toolTipLocation;
	private String toolTipText = null;

	public void setToolTip(Point location, String text) {
		this.toolTipLocation = location;
		this.toolTipText = text;
		updateArea(0, 0, width, height);
	}

	private void updateArea(int x0, int y0, int w, int h) 
	{
		int blocksize = 520 * GlobalOptions.resolution_factor;
		
		g.setBackground(transparent);
		g.clearRect(x0, y0, w, h);
		int xTileStart = (x0 + corXCam()) / blocksize;
		int xTileEnd = (x0 + corXCam() + w) / blocksize;
		for (int x = xTileStart; x <= xTileEnd; x++) {
			//only one iteration-loop, because there is no more random generation of BackGround
			int b = level.getBlock(x, 0) & 0xff;
			g.drawImage(Art.bg.getImage(b % 8, b / 8), (x * blocksize) - corXCam(), 0 - corYCam() - 16, null);
		}
		g.setColor(Color.BLACK);
		g.setFont(new Font("ARIAL", Font.BOLD, 12));
		if (toolTipText != null) {
			g.drawString(toolTipText, toolTipLocation.x, toolTipLocation.y + 20);
		}
	}

	public void render(Graphics g, int tick, float alpha) {
		g.drawImage(image, 0, 0, null);
	}

	public void setLevel(Level level) {
		this.level = level;
		updateArea(0, 0, width, height);
	}
}
