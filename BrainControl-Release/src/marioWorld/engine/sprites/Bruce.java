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
package marioWorld.engine.sprites;

import marioWorld.engine.Art;
import marioWorld.engine.SpriteAnimation;
import marioWorld.engine.SpriteAnimationType;
import marioWorld.engine.SpriteRenderProperties;
import marioWorld.engine.SpriteRenderer;

public class Bruce  extends Player
{		
	protected void blink(boolean on)
	{
		this.large = on ? newLarge : lastLarge;
		this.bulb = on ? newBulb : lastBulb;
		setNonStaticHealth();

		if (large) 
		{
			if (bulb)
				renderer.setSheet(Art.bulbBruce);
			else
				renderer.setSheet(Art.bruce);
		} 
		else 
		{
			renderer.setSheet(Art.smallBruce);
		}
		
		calcPic();
	}
	
	@Override
	protected void calcPic() 
	{
		// code for new sprite sheets

		//things that should not be in this function:
		height = ducking ? 12 : 24;

		//create animations with respect to the player state:
		SpriteAnimation animation = null;

		if(isOnGround())
		{
			if(Math.abs(xa+ya)<1)
				animation = new SpriteAnimation(renderer, SpriteAnimationType.Stand, true, 2);
			else if(Math.abs(xa) < 8)
				animation = new SpriteAnimation(renderer, SpriteAnimationType.Walk, true, 2);
			else
				animation = new SpriteAnimation(renderer, SpriteAnimationType.Run, true, 4);
		}
		else
		{
			if(ya <= 0)
				animation = new SpriteAnimation(renderer, SpriteAnimationType.Jump, false, 2);
			else if(Math.abs(ya) < 1.5)
				animation = new SpriteAnimation(renderer, SpriteAnimationType.Fly, true, 1);
			else// if(ya > 1.5)
				animation = new SpriteAnimation(renderer, SpriteAnimationType.Fall, false, 2);
		}

		if(ducking)
			animation = new SpriteAnimation(renderer, SpriteAnimationType.Duck, true, 1);
		
		renderer.setAnimation(animation);

	}
		
	public Bruce(int playerIndex, int bruceMode)
	{	
		super(playerIndex, bruceMode);
		kind = KIND_BRUCE;		
		height=24;
		individualYJumpSpeedMultiplier = 2;
	}
}
