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

public class SpriteRenderProperties
{
	//which frame of the sprite sheet is currently selected (2D sprite sheet)
	public int selectedFrameX;
	public int selectedFrameY;
	
	//location relative to the sprite position at which rendering will start (top-left corner?)
	//should be constant
	public int renderFORx;
	public int renderFORy;
	
	//the height and width at which an image is currently drawn... normally, this equals the sprite size
	//but is may be changed by the respective sprite
	public int renderHeight;
	public int renderWidth;

	//invert the display of this sprite
	public boolean flipFrameX = false;
	public boolean flipFrameY = false;	
	
	//is this sprite currently visible?
	public boolean isVisible = true;
	
	@Override
	public SpriteRenderProperties clone()
	{
		SpriteRenderProperties ret = new SpriteRenderProperties(0,0);
		
		ret.selectedFrameX = selectedFrameX;
		ret.selectedFrameY = selectedFrameY;
		
		ret.renderFORx = renderFORx;
		ret.renderFORy = renderFORy;
		ret.renderHeight = renderHeight;
		ret.renderWidth = renderWidth;

		ret.flipFrameX = flipFrameX;
		ret.flipFrameY = flipFrameY;	
		ret.isVisible = isVisible;
		
		return ret;
	}
	
	public void update(int setXSize, int setYSize)
	{
		//sets the values that depend on the sprite size without changing other draw properties
		
		renderWidth=setXSize;
		renderHeight=setYSize;
		
		renderFORx=setXSize/2;
		renderFORy=setYSize-1;		
	}
	
	public SpriteRenderProperties(int setXSize, int setYSize)
	{
		update(setXSize, setYSize);
		
		selectedFrameX=0;
		selectedFrameY=0;
	}
}
