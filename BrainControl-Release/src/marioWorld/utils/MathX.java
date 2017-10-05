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
package marioWorld.utils;

/**
 * Created by IntelliJ IDEA. User: Sergey Karakovskiy, firstname_at_idsia_dot_ch Date: Jul 25, 2009 Time: 3:26:43 PM Package: ch.idsia.utils
 */
public class MathX {
	public static int[] powsof2 = { 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768 };

	public static void show(char el) {
		System.out.print("block (" + Integer.valueOf(el) + ") :");
		for (int i = 0; i < 16; ++i)
			System.out.print((el & powsof2[i]) + " ");
		System.out.println("");
	}

}
