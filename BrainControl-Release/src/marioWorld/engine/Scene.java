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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;

import marioWorld.engine.sprites.Sprite;

public abstract class Scene {
	public static boolean[] keys = new boolean[16];
	public static final String[] keysStr = { "LEFT  ", "RIGHT ", " DOWN ", " JUMP ", " SPEED" };

	// public void toggleKey(int key, boolean isPressed)
	// {
	// keys[key] = isPressed;
	// }

	public abstract void init(List<Sprite> initialSprites);

	public abstract void tick();

	public abstract void render(Graphics2D og, float alpha, int playerIndex);
}
