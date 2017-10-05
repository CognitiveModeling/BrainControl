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
import java.awt.Graphics;
import java.awt.Graphics2D;

import marioWorld.engine.sprites.Sprite;

public class SpriteRenderer
{
	protected Sprite sprite;
	
	protected SpriteSheet renderSheet = null;
	protected SpriteRenderProperties renderProperties = null;
	
	protected SpriteAnimation animation = null;
	
	//TODO: there should be NO ACCESS to these internal properties!
	//everything should work via SpriteAnimations!!!
	public SpriteRenderProperties properties()
	{
		return renderProperties;
	}
	
	//TODO: this should also not be necessary!
	//it is only needed when another spriteSheet is set during runtime
	//e.g. when mario changes his health status
	//also this should be done via some animation in the same sprite sheet!
	public void setSheet(SpriteSheet setSheet)
	{
		renderSheet=setSheet;
		
		//update properties:
		renderProperties.update(setSheet.getXSize(), setSheet.getYSize());
	}
	
	public SpriteSheet getSheet()
	{
		return renderSheet;
	}
	
	public void setAnimation(SpriteAnimation set)
	{
		if(animation == null || !animation.equals(set))
		{
			animation = set;			
		}		
	}

	public void render(Graphics2D og, float alpha)
	{
		if (!renderProperties.isVisible)
			return;
		
		//NEW APPROACH WITH ANIMATIONS:
		if(animation != null)
		{
			renderProperties.selectedFrameX=animation.getRow();
			renderProperties.selectedFrameY=animation.getLine();
			
			//System.out.println("frame "+renderProperties.selectedFrameX);
			
			animation.iterate();
		}

		//OLD APPROACH:
		int xPixel = (int) sprite.x * GlobalOptions.resolution_factor - renderProperties.renderFORx;
		int yPixel = (int) sprite.y * GlobalOptions.resolution_factor - renderProperties.renderFORy + 1;
		
		//WORKAROUND for buggy sprites
		renderProperties.selectedFrameX = Math.min(renderProperties.selectedFrameX,renderSheet.xImages());
		renderProperties.selectedFrameY = Math.min(renderProperties.selectedFrameY,renderSheet.yImages());
		
		og.drawImage
		(
			renderSheet.getImage(renderProperties.selectedFrameX,renderProperties.selectedFrameY), 
			xPixel + (renderProperties.flipFrameX ? renderProperties.renderWidth : 0), 
			yPixel + (renderProperties.flipFrameY ? renderProperties.renderHeight : 0), 
			renderProperties.flipFrameX ? -renderProperties.renderWidth : renderProperties.renderWidth, 
			renderProperties.flipFrameY ? -renderProperties.renderHeight : renderProperties.renderHeight, 
			null
		);
		
		if (GlobalOptions.Labels)
			og.drawString("" + xPixel + "," + yPixel, xPixel, yPixel);
	}
	
	private SpriteRenderer()
	{		
	}
	
	@Override
	public SpriteRenderer clone()
	{
		SpriteRenderer ret = new SpriteRenderer();
		ret.sprite=sprite;									//reference
		ret.renderSheet=renderSheet;						//reference
		ret.renderProperties=renderProperties.clone();		//properties may vary across instances
		
		return ret;
	}
	
	public SpriteRenderer(Sprite setSprite, SpriteSheet setSheet, int defaultXFrame, int defaultYFrame)
	{
		sprite=setSprite;
		renderSheet=setSheet;
		renderProperties = new SpriteRenderProperties(setSheet.getXSize(), setSheet.getYSize());
		renderProperties.selectedFrameX=defaultXFrame;
		renderProperties.selectedFrameY=defaultYFrame;
	}
}
