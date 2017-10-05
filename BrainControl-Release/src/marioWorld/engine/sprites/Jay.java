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
import marioWorld.engine.SpriteRenderProperties;
import marioWorld.engine.SpriteRenderer;

public class Jay extends Player
{		
	protected void blink(boolean on)
	{
		this.large = on ? newLarge : lastLarge;
		this.bulb = on ? newBulb : lastBulb;
		setNonStaticHealth();

		if (large) 
		{
			if (bulb)
				renderer.setSheet(Art.bulbJay);
			else
				renderer.setSheet(Art.jay);
		} 
		else 
		{
			renderer.setSheet(Art.smallJay);
		}
		
		calcPic();
	}	
	
	public Jay(int playerIndex, int jayMode)
	{	
		super(playerIndex, jayMode);
		kind = KIND_JAY;
		height=24;
	}
}
