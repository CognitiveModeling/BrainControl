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
package marioWorld.mario.environments;

import marioWorld.engine.sprites.Player;

/**
 * Created by IntelliJ IDEA. User: Sergey Karakovskiy Date: Mar 28, 2009 Time: 8:51:57 PM Package: .Environments
 */

public interface Environment 
{
	public static final int numberOfButtons = 6;
	public static final int HalfObsWidth = 10;
	public static final int HalfObsHeight = 13;

	// always the same dimensionality: 22x22 .... CHECK: no!
	//public static final int numberOfObservationElements = 486 + 1;

	// KILLS

	// Chaning ZLevel during the game on-the-fly;
	// if your agent recieves too ambiguous observation, it might request for
	// more precise one for the next step

//	public byte[][] getCompleteObservation(Player player); // default: ZLevelScene = 1,

	// ZLevelEnemies = 0

//	public byte[][] getEnemiesObservation(Player player); // default: ZLevelEnemies = 0

//	public byte[][] getLevelSceneObservation(Player player); // default: ZLevelScene = 1

	public float[] getPlayerFloatPos(Player player);

	public int getPlayerMode(Player player);

	public float[] getEnemiesFloatPos();

	public boolean isPlayerOnGround(Player player);

	public boolean mayPlayerJump(Player player);

	public boolean isPlayerCarrying(Player player);

//	public byte[][] getMergedObservationZ(int ZLevelScene, int ZLevelEnemies, Player player);

//	public byte[][] getLevelSceneObservationZ(int ZLevelScene, Player player);

//	public byte[][] getEnemiesObservationZ(int ZLevelEnemies, Player player);

	public int getKillsTotal();

	public int getKillsByFire();

	public int getKillsByStomp();

	public int getKillsByGrumpy();

//	// For Server usage only, Java agents should use non-bitmap versions.
//	public String getBitmapEnemiesObservation(Player player);
//
//	public String getBitmapLevelObservation(Player player);
}
