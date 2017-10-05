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
package mario.main;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.JOptionPane;

import marioWorld.utils.ResourceStream;

public class StartJar {

	private static final String JARNAME = "brainControl.jar";

	public static enum ExitCode {
		DUMMY1, DUMMY2, QUIT, MENU, LEVEL_SOLVED, GAME_SOLVED
	}

	public static String path() {
		String path = null;
		String prefix = "";
		try {
			path = StartJar.class.getProtectionDomain().getCodeSource().getLocation().toURI().toString();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			System.exit(0);
		}
		if (!path.startsWith("rsrc:")) {
			prefix += "../output/";
		} 
		path = remove(path,"rsrc:");
		path = remove(path,"file:");
		path = remove(path, "/bin");
		path = remove(path,"/");
		path =  path + prefix + "/" + JARNAME;
		//System.out.println(path);
		//System.exit(0);
		return path;
	}

	public static String pathAlternative() {
		URL url = ResourceStream.class.getClassLoader().getResource(ResourceStream.DIR_RESOURCE+"/"+ResourceStream.RESOURCES_NEEDED_TO_COPY[0]);
		String path = url.toString();
		String prefix = "";
		if (!path.startsWith("jar:")) {
			prefix += "../../../output/";
		} 
		path = remove(path,"jar:");
		path = remove(path,"file:");
		if (path.startsWith("file:")) 
			path = path.substring(5);
		if (path.startsWith("file:")) 
			path = path.substring(5);
		if (path.startsWith("/")) 
			path = path.substring(1);
		path =  path + prefix + "/" + JARNAME;
		return path;
	}

	private static String remove(String string, String start) {
		return string.startsWith(start) ? string.substring(start.length()) : string;
	}

	public static int runJar(String commandline) {
		String command = "javaw -jar "+path() +" "+commandline;
		Process p;
		try {
			System.out.println("Starting command: "+command);
			p = Runtime.getRuntime().exec(command);
			pipe(p.getInputStream(),System.out);
			pipe(p.getErrorStream(),System.err);
			p.waitFor();
			return p.exitValue();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Could not find the jar file: "+path(),"Error occurred",JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		} 
		return -1;
	}

	private static void pipe(InputStream in, final PrintStream out) {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		new Thread() {
			@Override public void run() {
				String line;
				try {
					while ((line = reader.readLine()) != null) {
						out.println(line);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				out.flush();
				out.close();
			}
		}.start(); 
	}


	public static void main(String args[]) {
		if (args.length == 0) {
			// no command line args: start a loop until a quit exit code is received
			int returnCode = 0;
			do {
				// when previous return code = 2: start menu, = 3: start next active level 
				returnCode = runJar(returnCode == ExitCode.MENU.ordinal() ? "menu" : "-1");
				System.out.println("Obatined return code: "+returnCode);
				// return code not quit 
			} while((returnCode == ExitCode.LEVEL_SOLVED.ordinal()) || (returnCode == ExitCode.MENU.ordinal()));
			if (returnCode == ExitCode.GAME_SOLVED.ordinal()) {
				JOptionPane.showMessageDialog(null, "Congratulations you solved all 4 demo levels.\n If you have further questions or remarks write us a mail.\n Thank you for playing","Game solved!", JOptionPane.INFORMATION_MESSAGE);				
			}
			
		} else {
			// there is a command line, start PlayRelease
			PlayRelease.main(args);
		}
	}

}
