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
package marioAI.movement.tgng;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import marioWorld.utils.ResourceStream;

/**
 * Class which can convert serialized Tgng-Objects into dot-Files to visualize
 * them
 * 
 * @author benjamin
 *
 */
public class TgngToDot {
	/**
	 * the path where the tgng save files are found
	 */
	private static final String PATH = (new File(ResourceStream.DIR_RESOURCE+"/"+ResourceStream.DIR_AI_RESOURCES+"/movement/tgng"))
			.getAbsolutePath();
	/**
	 * header for the dot file
	 */
	private static final String HEADER = createHeader();

	public static void main(String[] args) {
		ArrayList<File> saveFiles = getSaveFiles();
		createDotFiles(saveFiles);
	}

	/**
	 * returns all available save files
	 * 
	 * @return
	 */
	private static ArrayList<File> getSaveFiles() {
		File folder = new File(PATH);
		File[] files = folder.listFiles();
		ArrayList<File> saveFiles = new ArrayList<File>();
		for (int i = 0; i < files.length; i++) {
			if (files[i].getPath().endsWith("Tgng.ser")) {
				saveFiles.add(files[i]);
			}
		}
		return saveFiles;
	}

	/**
	 * creates a dot file for each TgngNetwork
	 * 
	 * @param saveFiles
	 */
	private static void createDotFiles(ArrayList<File> saveFiles) {
		File folder;
		Tgng tgng;
		for (int i = 0; i < saveFiles.size(); i++) {
			folder = createDir(saveFiles.get(i));
			tgng = new Tgng(folder.getName() /*, null*/);
			tgng.loadFromDisk(saveFiles.get(i).getAbsolutePath() /*, null*/);
			writeDotFiles(tgng, folder);
		}
	}

	/**
	 * creates a directory for the dot files
	 * 
	 * @param file
	 * @return
	 */
	private static File createDir(File file) {
		StringBuilder pathName;
		pathName = new StringBuilder(PATH);
		pathName.append("/");
		pathName.append((file.getName().split("T"))[0]);
		File directory = new File(pathName.toString());
		directory.mkdir();
		return directory;
	}

	/**
	 * writes each TgngNetwork into one dot file
	 * 
	 * @param tgng
	 * @param folder
	 */
	private static void writeDotFiles(Tgng tgng, File folder) {
//		StringBuilder path = new StringBuilder(folder.getAbsolutePath());
//		path.append("/");
//		path.append("FireballTgng.dot");
//		writeNetwork(tgng.getFireballTgng(), new File(path.toString()));
//
		StringBuilder path = new StringBuilder(folder.getAbsolutePath());
		path.append("/");
		path.append("NpcTgng.dot");
		writeNetwork(tgng.getNpcTgng(), new File(path.toString()));
//
//		path = new StringBuilder(folder.getAbsolutePath());
//		path.append("/");
//		path.append("PlayerTgng.dot");
//		writeNetwork(tgng.getPlayerTgng(), new File(path.toString()));
	}

	/**
	 * writes one TgngNetwork into the corresponding dot file
	 * 
	 * @param network
	 * @param path
	 */
	private static void writeNetwork(TgngNetwork network, File path) {
		ArrayList<TgngNode> nodes = network.getNodes();
		System.out.println(nodes.size());
		try {
			if (path.exists()) {
				path.delete();
			}
			FileWriter writer = new FileWriter(path);
			writer.write(HEADER);
			writer.write("{\n");
			for (int i = 0; i < nodes.size(); i++) {
				writeNode(nodes, i, writer);
				for (TgngEdge edge : nodes.get(i).getOutgoing()) {
					writeEdge(nodes, i, edge, writer);
				}
			}
			writer.write("}");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * writes on Node to the dot file
	 * 
	 * @param node
	 * @param nodeIndex
	 * @param writer
	 */
	private static void writeNode(ArrayList<TgngNode> node, int nodeIndex,
			FileWriter writer) {
		try {
			writer.write(String.valueOf(nodeIndex));
			writer.write(";\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * writes one edge to the dot file
	 * 
	 * @param nodes
	 * @param nodeIndex
	 * @param edge
	 * @param writer
	 */
	private static void writeEdge(ArrayList<TgngNode> nodes, int nodeIndex,
			TgngEdge edge, FileWriter writer) {
		try {
			writer.write(String.valueOf(nodeIndex));
			writer.write(" -> ");
			writer.write(String.valueOf(nodes.indexOf(edge.getSuccessor())));
			writer.write(";\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * method used to create the header, additional parameters for the dot
	 * compiler could be set here
	 * 
	 * @return
	 */
	private static String createHeader() {
		StringBuilder header = new StringBuilder("digraph");
		return header.toString();
	}

}
