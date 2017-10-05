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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class Replayer {
	private ByteArrayInputStream bais;
	private DataInputStream dis;

	private byte tick = 0;
	private int tickCount = -99999999;

	public Replayer(byte[] bytes) {
		bais = new ByteArrayInputStream(bytes);
		dis = new DataInputStream(bais);
	}

	public long nextLong() {
		try {
			return dis.readLong();
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public byte nextTick() {
		if (tickCount == -99999999) {
			try {
				tickCount = dis.readInt();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (tickCount == 0) {
			try {
				tick = (byte) dis.read();
				tickCount = dis.readInt();
			} catch (IOException e) {
			}
		}

		if (tickCount > 0) {
			tickCount--;
		}

		return tick;
	}
}
