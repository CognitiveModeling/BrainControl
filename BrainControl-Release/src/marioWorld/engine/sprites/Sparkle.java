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
import marioWorld.engine.LevelScene;
import marioWorld.engine.SpriteRenderProperties;
import marioWorld.engine.SpriteRenderer;

public class Sparkle extends Sprite {
	public int life;
	public int xPicStart;

	public Sparkle(int x, int y, float xa, float ya) {
		this(x, y, xa, ya, (int) (Math.random() * 2), 0, 5);
	}

	public Sparkle(int x, int y, float xa, float ya, int xPic, int yPic, int timeSpan) {
		kind = KIND_SPARCLE;
				
		renderer = new SpriteRenderer(this, Art.particles, xPic, yPic);
		
		this.x = x;
		this.y = y;
		this.xa = xa;
		this.ya = ya;
		
		life = 10 + (int) (Math.random() * timeSpan);
		
		xPicStart = xPic;

//		sheet.xPicO = 4;
//		sheet.yPicO = 4;
//		wPic = 8;
//		hPic = 8;		
	}

	public void move() {
		if (life > 10)
			renderer.properties().selectedFrameX = 7;
		else
			renderer.properties().selectedFrameX = xPicStart + (10 - life) * 4 / 10;

		if (life-- < 0)
			Sprite.spriteContext.removeSprite(this);

		x += xa;
		y += ya;
	}

	@Override
	public Sparkle cloneWithNewLevelScene(LevelScene ls) {
		Sparkle ret = (Sparkle) super.clone();
		ret.life = life;
		ret.xPicStart = xPicStart;
		return ret;
	}
}
