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

import java.awt.Image;
import java.awt.image.PixelGrabber;

public class SpriteSheet
{
	//the separate sprite images
	private Image[][] images;
	
	//the number of frames per animation may vary. thus, we define a frame mask for each line:
	private int[] frameMask;

	//the size of each image
	private int xSize;
	private int ySize;
	
	public int getXSize()
	{
		return xSize;
	}
	
	public int getYSize()
	{
		return ySize;
	}
			
	public int xImages()
	{
		return images.length-1;
	}

	public int yImages()
	{
		return images[0].length-1;
	}
	
	public Image getImage(int x, int y)
	{
		return images[x][y];
	}
	
	public int getNrOfFrames(int line)
	{
		//System.out.println("frameMask: " + frameMask.length + " und " + line);
		return frameMask[line];
	}
			
	public SpriteSheet(int setXSize, int setYSize, int setXImages, int setYImages, Image[][] setImages)
	{
		images = setImages;
		xSize=setXSize;
		ySize=setYSize;		
		
		//determine the frame mask:
		//TODO: this is pretty slow
		frameMask = new int[yImages()];
		for(int y=0;y<yImages();y++)
		{
			for(int x=0;x<xImages();x++)
			{				
				//Image smallimage = images[x][y].getScaledInstance(1, 1, Image.SCALE_FAST);
				
				int[] pixels = new int[setXSize*setYSize];
				PixelGrabber pg = new PixelGrabber(images[x][y], 0, 0, setXSize, setYSize, pixels, 0, setXSize);
				
				try
				{
					pg.grabPixels();
				} 
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				
				//check if the whole image is empty:
				int px;
				for(px=0;px<setXSize*setYSize;px++)
				{
					if(pixels[px]!=0)
					{
						break;
					}						
				}
				
				if(px==setXSize*setYSize || (px!=setXSize*setYSize && x==xImages()-1))
				{
					frameMask[y]=x;
					
					System.out.println("animation line "+y+": "+x+" frames");			
					
					break;
				}
			}
		}
	}
}
