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
package marioWorld.tools;

import java.awt.TextArea;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;

/**
 * Created by IntelliJ IDEA. User: Sergey Karakovskiy, firstname_at_idsia_dot_ch Date: May 7, 2009 Time: 8:59:20 PM Package: ch.idsia.tools
 */
// TODO: Warning message: bilk yourself is easier that the system.
public class LOGGER{
	private static int count = 0;

	public static void save(String fileName) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(fileName);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			bw.write(history);
			bw.close();
			System.out.println("\n\nlog file saved to " + fileName);
		} catch (FileNotFoundException e) {
			System.err.println("File not found: " + e.getMessage());
		} catch (IOException e) {
			System.err.println("I/O Error: " + e.getMessage());
		}
	}

	public enum VERBOSE_MODE {
		ALL, INFO, WARNING, ERROR, TOTAL_SILENCE
	}

	static TextArea textAreaConsole = null;
	private static VERBOSE_MODE verbose_mode = VERBOSE_MODE.TOTAL_SILENCE;
	//private static VERBOSE_MODE verbose_mode = VERBOSE_MODE.ALL;

	public static void setVerboseMode(VERBOSE_MODE verboseMode) {
		LOGGER.verbose_mode = verboseMode;
	}

	public static void setTextAreaConsole(TextArea tac) {
		textAreaConsole = tac;
	}

	private static String history = "console:\n";

	public static void println(String record, VERBOSE_MODE vm) {
		LOGGER.print(record + "\n", vm);
	}

	private static DecimalFormat df = new DecimalFormat("000");

	public static void print(String record, VERBOSE_MODE vm) {
		try {
			// upperbounded by maximum size of the string : 6826363
			addRecord(record, vm);
		} catch (OutOfMemoryError e) {
			System.err.println("OutOfMemory Exception while logging. Application data is not corrupted.");
			save(prepareDumpName());
			history = "console:\n";
		}
	}

	private static String prepareDumpName() {
		return "LOGGERDump" + df.format(count++) + ".txt";
	}

	private static void addRecord(String record, VERBOSE_MODE vm) {
		if (verbose_mode == VERBOSE_MODE.TOTAL_SILENCE)
			return; // Not recommended to use this mode. Nothing would be stored
		// in files as well!

		if (vm.compareTo(verbose_mode) >= 0) {
			if (vm.compareTo(VERBOSE_MODE.WARNING) >= 0)
				System.err.print(record);
			else
				System.out.print(record);
		}

		String r = "\n[:" + vm + ":] " + record;
		history += r;
		if (history.length() > 1048576) // 1024 * 1024, 1 MByte.
		{
			save(prepareDumpName());
			history = "console:\n";
		}
		if (textAreaConsole != null)
			textAreaConsole.setText(history);

	}

	public static String getHistory() {
		return history;
	}

	public void resetToDefaultValue() {
		// TODO Auto-generated method stub
		count = 0;
		textAreaConsole = null;
		verbose_mode = VERBOSE_MODE.TOTAL_SILENCE;
		history = "console:\n";
		df = new DecimalFormat("000");
	}
}
