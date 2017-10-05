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

public class SpriteAnimation
{	
	//each animation refers to a single line in a single sprite sheet
	//it defines all the possible actions a sprite can do, however, not the health level! you have to load another sheet for each health level!
	//the enum (implicitly) defines the line of the current animation
	private SpriteRenderer renderer;
	private SpriteAnimationType line;
	
	private int iteration;				//the current iteration (starting from 0) of the current animation
	private int iterationsPerFrame;		//the number of iterations each frame is shown
	private boolean cyclic;				//is the animation cyclic (repeated), or is it stopped when the last frame is reached?
		
	public int getRow()
	{
		int step = (int)(iteration/iterationsPerFrame);
		
		if(cyclic)
			step = step % renderer.getSheet().getNrOfFrames(getLine());
		else 
		{
			step = Math.min(step, renderer.getSheet().getNrOfFrames(getLine()) - 1);
			//System.out.println("row " + step + ", line " + getLine() + ", nr of frames: " + renderer.getSheet().getNrOfFrames(getLine()) );
		}
		
		return step;
	}
	
	public int getLine()
	{
		return line.ordinal();
	}
	
	public void iterate()
	{
		iteration++;
	}
	
	public int[] getFrame()
	{
		//returns the X,Y coordinate in the spritesheet
		return new int[]{getRow(),getLine()};
	}
		
	@Override
	public boolean equals(Object other)	
	{
		if(other instanceof SpriteAnimation)
		{
			SpriteAnimation otherAnimation = (SpriteAnimation) other;			
			
			if(otherAnimation.getLine() == getLine() && otherAnimation.renderer.getSheet() == renderer.getSheet())
			{				
				//same type of animation and the same used sheet
				return true;
			}
		}
		
		return false;
	}
	
	public SpriteAnimation(SpriteRenderer setRenderer, SpriteAnimationType setType, boolean setCyclic, int setIterationsPerFrame)
	{
		renderer=setRenderer;
		line=setType;
		iteration=0;
		iterationsPerFrame=1;
		cyclic=setCyclic;
		iterationsPerFrame=setIterationsPerFrame;
	}
}
