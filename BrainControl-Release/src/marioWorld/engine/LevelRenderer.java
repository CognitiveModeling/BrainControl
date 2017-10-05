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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Transparency;

import marioWorld.engine.level.Level;

public class LevelRenderer 
{
	private int xCam;
	private int yCam;
	
	private Image image;
	private Graphics2D g;
	private static final Color transparent = new Color(0, 0, 0, 0);
	private Level level;

	public boolean renderBehaviors = false;

	int width;
	int height;

	public LevelRenderer(Level level, GraphicsConfiguration graphicsConfiguration, int width, int height) {
		this.width = width;
		this.height = height;

		this.level = level;
		image = graphicsConfiguration.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
		
		g = (Graphics2D) image.getGraphics();
		g.setComposite(AlphaComposite.Src);
		
//		//ENABLE PARTIAL TRANSPARENCY FOR LEVEL (AND BACKGROUND?)
//		AlphaComposite ac = java.awt.AlphaComposite.getInstance(AlphaComposite.SRC_OVER);
//        g.setComposite(ac);		

		updateArea(0, 0, width, height);
	}

	public void setCam(int xCam, int yCam) 
	{
		//int xCamD = this.xCam - xCam;
		//int yCamD = this.yCam - yCam;
		this.xCam = xCam;
		this.yCam = yCam;
		
		//original, not working:
		/*
		g.setComposite(AlphaComposite.Src);
		g.copyArea(0, 0, width, height, xCamD, yCamD);
		
		if (xCamD < 0)
		{
			if (xCamD < -width)
				xCamD = -width;
			updateArea(width + xCamD, 0, -xCamD, height);
		} 
		else if (xCamD > 0) 
		{
			if (xCamD > width)
				xCamD = width;
			updateArea(0, 0, xCamD, height);
		}

		if (yCamD < 0)
		{
			if (yCamD < -width)
				yCamD = -width;
			updateArea(0, height + yCamD, width, -yCamD);
		} 
		else if (yCamD > 0) 
		{			
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

	private void updateArea(int x0, int y0, int w, int h) 
	{
		int blocksize = GlobalOptions.xScreen / 20;
		
		g.setBackground(transparent);
		g.clearRect(x0, y0, w, h);
		int xTileStart = (x0 + corXCam()) / blocksize;
		int yTileStart = (y0 + corYCam()) / blocksize;
		int xTileEnd = (x0 + corXCam() + w) / blocksize;
		int yTileEnd = (y0 + corYCam() + h) / blocksize;
		for (int x = xTileStart; x <= xTileEnd; x++) {
			for (int y = yTileStart; y <= yTileEnd; y++) {
				int b = level.getBlock(x, y) & 0xff;
				if (((Level.TILE_BEHAVIORS[b]) & Level.BIT_ANIMATED) == 0) 
				{
					g.drawImage(Art.level.getImage((b % 16),b / 16), (x * blocksize) - corXCam(), (y * blocksize) - corYCam(), null);
				}
			}
		}
	}
		
	public void render(Graphics2D g, int tick, float alpha) 
	{
		int blocksize = GlobalOptions.xScreen / 20;
		
		g.drawImage(image, 0, 0, null);

		for (int x = corXCam() / blocksize; x <= (corXCam() + width) / blocksize; x++)
			for (int y = corYCam() / blocksize; y <= (corYCam() + height) / blocksize; y++) {
				byte b = level.getBlock(x, y);

				if (((Level.TILE_BEHAVIORS[b & 0xff]) & Level.BIT_ANIMATED) > 0) {
					int animTime = (tick / 3) % 4;

					if (((b % 16) / 4 == 0 && b / 16 == 1) ||
							((b % 16) / 4 == 1 && b / 16 == 2)) {
						animTime = (tick / 2 + (x + y) / 8) % 20;
						if (animTime > 3)
							animTime = 0;
					}
					if ((b % 16) / 4 == 3 && b / 16 == 0) {
						animTime = 2;
					}
					int yo = 0;
					if (x >= 0 && y >= 0 && x < level.width && y < level.height)
						yo = level.data[x][y];
					if (yo > 0)
						yo = (int) (Math.sin((yo - alpha) / 4.0f * Math.PI) * 8);
					g.drawImage(Art.level.getImage((b % 16) / 4 * 4 + animTime,b / 16), (x * blocksize) - corXCam(), (y * blocksize) - corYCam() - yo, null);
				}
				/*
				 * else if (b == Level.TILE_BONUS) { int animTime = (tick / 3) % 4; int yo = 0; if (x >= 0 && y >= 0 && x < level.width && y < level.height) yo
				 * = level.data[x][y]; if (yo > 0) yo = (int) (Math.sin((yo - alpha) / 4.0f * Math.PI) * 8); g.drawImage(Art.mapSprites[(4 + animTime)][0], (x
				 * * blocksize) - corXCam(), (y * blocksize) - corYCam() - yo, null); }
				 */

				if (renderBehaviors) {
					if (((Level.TILE_BEHAVIORS[b & 0xff]) & Level.BIT_BLOCK_UPPER) > 0) {
						g.setColor(Color.RED);
						g.fillRect((x * blocksize) - corXCam(), (y * blocksize) - corYCam(), 16, 2);
					}
					if (((Level.TILE_BEHAVIORS[b & 0xff]) & Level.BIT_BLOCK_ALL) > 0) {
						g.setColor(Color.RED);
						g.fillRect((x * blocksize) - corXCam(), (y * blocksize) - corYCam(), 16, 2);
						g.fillRect((x * blocksize) - corXCam(), (y * blocksize) - corYCam() + 14, 16, 2);
						g.fillRect((x * blocksize) - corXCam(), (y * blocksize) - corYCam(), 2, 16);
						g.fillRect((x * blocksize) - corXCam() + 14, (y * blocksize) - corYCam(), 2, 16);
					}
					if (((Level.TILE_BEHAVIORS[b & 0xff]) & Level.BIT_BLOCK_LOWER) > 0) {
						g.setColor(Color.RED);
						g.fillRect((x * blocksize) - corXCam(), (y * blocksize) - corYCam() + 14, 16, 2);
					}
					if (((Level.TILE_BEHAVIORS[b & 0xff]) & Level.BIT_SPECIAL) > 0) {
						g.setColor(Color.PINK);
						g.fillRect((x * blocksize) - corXCam() + 2 + 4, (y * blocksize) - corYCam() + 2 + 4, 4, 4);
					}
					if (((Level.TILE_BEHAVIORS[b & 0xff]) & Level.BIT_BUMPABLE) > 0) {
						g.setColor(Color.BLUE);
						g.fillRect((x * blocksize) - corXCam() + 2, (y * blocksize) - corYCam() + 2, 4, 4);
					}
					if (((Level.TILE_BEHAVIORS[b & 0xff]) & Level.BIT_BREAKABLE) > 0) {
						g.setColor(Color.GREEN);
						g.fillRect((x * blocksize) - corXCam() + 2 + 4, (y * blocksize) - corYCam() + 2, 4, 4);
					}
					if (((Level.TILE_BEHAVIORS[b & 0xff]) & Level.BIT_PICKUPABLE) > 0) {
						g.setColor(Color.YELLOW);
						g.fillRect((x * blocksize) - corXCam() + 2, (y * blocksize) - corYCam() + 2 + 4, 4, 4);
					}
					if (((Level.TILE_BEHAVIORS[b & 0xff]) & Level.BIT_ANIMATED) > 0) {
					}
				}

			}
	}

	public void repaint(int x, int y, int w, int h) {
		updateArea(x * 16 - corXCam(), y * 16 - corYCam(), w * 16, h * 16);
	}

	public void setLevel(Level level) {
		this.level = level;
		updateArea(0, 0, width, height);
	}

	public void renderExit0(Graphics g, int tick, float alpha, boolean bar) 
	{
		int blocksize = GlobalOptions.xScreen / 20;
		
		for (int y = level.yExit - 8; y < level.yExit; y++) {
			g.drawImage(Art.level.getImage(12,y == level.yExit - 8 ? 4 : 5), (level.xExit * blocksize) - corXCam() - 16, (y * blocksize) - corYCam(), null);
		}
		int yh = level.yExit * 16 - (int) ((Math.sin((tick + alpha) / 20) * 0.5 + 0.5) * 7 * 16) - 8;
		if (bar) {
			g.drawImage(Art.level.getImage(12,3), (level.xExit * blocksize) - corXCam() - 16, yh - corYCam(), null);
			g.drawImage(Art.level.getImage(13,3), (level.xExit * blocksize) - corXCam(), yh - corYCam(), null);
		}
	}

	public void renderExit1(Graphics g, int tick, float alpha) 
	{
		int blocksize = GlobalOptions.xScreen / 20;
		
		for (int y = level.yExit - 8; y < level.yExit; y++) {
			g.drawImage(Art.level.getImage(13,y == level.yExit - 8 ? 4 : 5), (level.xExit * blocksize) - corXCam() + 16, (y * blocksize) - corYCam(), null);
		}
	}
}
