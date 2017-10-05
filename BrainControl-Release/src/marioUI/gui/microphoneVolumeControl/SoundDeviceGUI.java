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
package marioUI.gui.microphoneVolumeControl;

import java.util.List;
import java.util.Random;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class SoundDeviceGUI {

	// SOUND DEVICE
	private static SoundInputDeviceControl sDeviceControl;
	// UI
	private static SoundDeviceSelectionUI UI;

	public SoundDeviceGUI() {
		
	}
	public static void start(){

		// Have java set the look and feel of the UI to be like the native
		// systems
		// so people dont get wierded out by the java UI scheeme.
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException ex) {
			System.out.println("bootstrap - Main - UnsupportedLookAndFeelException - " + ex);
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
		} catch (InstantiationException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}

		createSoundDeviceControl();
		sDeviceControl.listenForPresenceEvents(handleAudioInputLevelReading);
		List<String> soundDevices = sDeviceControl.ListAudioInputDevices();
//		int CurrentConfigFileIndex = sDeviceControl.getConfigFileDeviceIndex();

//		UI = new SoundDeviceSelectionUI(soundDevices, CurrentConfigFileIndex);
		UI = new SoundDeviceSelectionUI(soundDevices, sDeviceControl );
		UI.listenForPresenceEvents(handleCustomPresenceUpdate);
		UI.display();

	}

	// 10/2/2010
	// CDP
	//
	// For listening to events from the SoundDeviceSelection
	// The drop down box.
	// When the save button is pressed, this also is used to know we need to
	// save the config file.
	static CustomPresenceListener handleCustomPresenceUpdate = new CustomPresenceListener() {
		public void presenceEventOccurred(CustomPresenceEvent evt) {

			if (sDeviceControl != null) {
				if (evt.getNewPresence().equals("Save")) {
//					sDeviceControl.SaveConfigAndExit();
				} else {
					sDeviceControl.StartMonitoringLevelsOnMixer(evt.getFullJIDAndResource());
				}
			}
		}
	};

	// 10/3/2010
	// CDP
	//
	// For listening to events from the SoundInputDeviceControl
	// This changes the graphical indicator on the little red bar in the UI
	static CustomPresenceListener handleAudioInputLevelReading = new CustomPresenceListener() {
		public void presenceEventOccurred(CustomPresenceEvent evt) {

			if (UI != null ) {
				if(evt.getNewPresence() !="No AudioFormat"){
					UI.updateSoundLevel(Integer.parseInt(evt.getFullJIDAndResource()));
				} else {
					UI.displayNoAudioFormatMessage();
				}
				
			}
		}
	};

	private static int randomFromRange(int _lowest, int _highest) {
		int ReturnValue = 0;

		// Get a psudo random number gen ready
		Random randNumber = new Random();

		long range = (long) _highest - (long) _lowest + 1;
		long fraction = (long) (range * randNumber.nextDouble());

		ReturnValue = (int) (fraction + _lowest);

		randNumber = null;

		return ReturnValue;
	}

	private static void createSoundDeviceControl() {
		if (sDeviceControl == null) {
			sDeviceControl = new SoundInputDeviceControl();
		} else {
			destroySoundDeviceControl(true);
		}
	}

	private static void destroySoundDeviceControl(boolean _OptionalCallback) {
		if (sDeviceControl != null) {
			sDeviceControl = null;

			if (_OptionalCallback == true) {
				createSoundDeviceControl();
			}
		}
	}

}
