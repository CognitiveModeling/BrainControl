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

public class Particle extends Sprite {
	public int life;

	public Particle(int x, int y, float xa, float ya) {
		this(x, y, xa, ya, (int) (Math.random() * 2), 0);
	}

	public Particle(int x, int y, float xa, float ya, int xPic, int yPic) 
	{
		kind = KIND_PARTICLE;
		
		renderer = new SpriteRenderer(this, Art.particles, xPic, yPic);
		
		this.x = x;
		this.y = y;
		this.xa = xa;
		this.ya = ya;
		life = 10;

//		sheet.xPicO = 4;
//		sheet.yPicO = 4;
//		wPic = 8;
//		hPic = 8;
	}

	@Override
	public void move() {
		if (life-- < 0)
			Sprite.spriteContext.removeSprite(this);
		x += xa;
		y += ya;
		ya *= 0.95f;
		ya += 3;
	}

	@Override
	public Particle cloneWithNewLevelScene(LevelScene ls) {
		Particle ret = (Particle) super.clone();
		ret.life = life;
		return ret;
	}
}
