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
package marioWorld.engine.coordinates;

import marioWorld.mario.environments.Environment;

/**
 * This class contains coordinate relevant constants as well as a couple of tests to ensure their correct functionality.
 * 
 * @author Niels, Eduard
 * 
 */

public class CoordinatesUtil {

	/**
	 * The observation-relative tile x-coordinate of Mario.
	 */
	public static final int PLAYER_OBSERVATION_TILE_POS_X = Environment.HalfObsWidth;

	/**
	 * The observation-relative tile y-coordinate of Mario.
	 */
	public static final int PLAYER_OBSERVATION_TILE_POS_Y = Environment.HalfObsHeight;

	/**
	 * The world units per tile.
	 */
	public static final int UNITS_PER_TILE = 16;

	/**
	 * Resolution of the discrete coordinate system. 1: same as block size (16 pixel). 2: halb block size (8 pixel), ...
	 */
	private static final int GRAIN_FACTOR = 1; // use a power of 2 //default: 8

	public static int getGrainFactor() {
		return GRAIN_FACTOR;
	}

	/**
	 * Testing coordinate translation.
	 */

	public static void main(String[] arg) {
		GlobalContinuous player1 = new GlobalContinuous(1, 1);
		GlobalContinuous player2 = new GlobalContinuous(-1, -1);

		// THIS WORKS ONLY FOR GRAIN FACTOR = 2!
		System.out.println("Testing global coarse:");
		GlobalCoarse gc1 = new GlobalCoarse(2, 3);
		GlobalCoarse gc2 = new GlobalCoarse(-9, -7);

		/** global coarse to global continuous */
		System.out.println("Expected: [GlobalContinuous: 40.0,56.0]\n  Actual: " + gc1.toGlobalContinuous());
		System.out.println("Expected: [GlobalContinuous: -136.0,-104.0]\n  Actual: " + gc2.toGlobalContinuous());

		/** global coarse to global fine grain */
		System.out.println("Expected: [GlobalFineGrain: 5,7]\n  Actual: " + gc1.toGlobalFineGrain());
		System.out.println("Expected: [GlobalFineGrain: -17,-13]\n  Actual: " + gc2.toGlobalFineGrain());

		/** global coarse to observation relative coarse */
		System.out.println("Expected: [ObsRelCoarse: 13,14]\n  Actual: " + gc1.toObservationRelativeCoarse(player1));
		System.out.println("Expected: [ObsRelCoarse: 14,15]\n  Actual: " + gc1.toObservationRelativeCoarse(player2));
		System.out.println("Expected: [ObsRelCoarse: 2,4]\n  Actual: " + gc2.toObservationRelativeCoarse(player1));
		System.out.println("Expected: [ObsRelCoarse: 3,5]\n  Actual: " + gc2.toObservationRelativeCoarse(player2));

		/** global coarse to observation relative fine grain */
		System.out.println("Expected: [ObsRelFineGrain: 27,29]\n  Actual: " + gc1.toObservationRelativeFineGrain(player1));
		System.out.println("Expected: [ObsRelFineGrain: 29,31]\n  Actual: " + gc1.toObservationRelativeFineGrain(player2));
		System.out.println("Expected: [ObsRelFineGrain: 5,9]\n  Actual: " + gc2.toObservationRelativeFineGrain(player1));
		System.out.println("Expected: [ObsRelFineGrain: 7,11]\n  Actual: " + gc2.toObservationRelativeFineGrain(player2));

		// ------------------------------------------------------------
		System.out.println("\nTesting global continuous:");
		GlobalContinuous gn1 = new GlobalContinuous(14, 18);
		GlobalContinuous gn2 = new GlobalContinuous(-9, -7);

		/** global continuous to global coarse */
		System.out.println("Expected: [GlobalCoarse: 0,1]\n  Actual: " + gn1.toGlobalCoarse());
		System.out.println("Expected: [GlobalCoarse: -1,-1]\n  Actual: " + gn2.toGlobalCoarse());

		/** global continuous to global fine grain */
		System.out.println("Expected: [GlobalFineGrain: 1,2]\n  Actual: " + gn1.toGlobalFineGrain());
		System.out.println("Expected: [GlobalFineGrain: -2,-1]\n  Actual: " + gn2.toGlobalFineGrain());

		/** global continuous to observation relative coarse */
		System.out.println("Expected: [ObsRelCoarse: 11,12]\n  Actual: " + gn1.toObservationRelativeCoarse(player1));
		System.out.println("Expected: [ObsRelCoarse: 12,13]\n  Actual: " + gn1.toObservationRelativeCoarse(player2));
		System.out.println("Expected: [ObsRelCoarse: 10,10]\n  Actual: " + gn2.toObservationRelativeCoarse(player1));
		System.out.println("Expected: [ObsRelCoarse: 11,11]\n  Actual: " + gn2.toObservationRelativeCoarse(player2));

		/** global continuous to observation relative fine grain */
		System.out.println("Expected: [ObsRelFineGrain: 23,24]\n  Actual: " + gn1.toObservationRelativeFineGrain(player1));
		System.out.println("Expected: [ObsRelFineGrain: 25,26]\n  Actual: " + gn1.toObservationRelativeFineGrain(player2));
		System.out.println("Expected: [ObsRelFineGrain: 20,21]\n  Actual: " + gn2.toObservationRelativeFineGrain(player1));
		System.out.println("Expected: [ObsRelFineGrain: 22,23]\n  Actual: " + gn2.toObservationRelativeFineGrain(player2));

		// ------------------------------------------------------------
		System.out.println("\nTesting global fine grain:");
		GlobalFineGrain gf1 = new GlobalFineGrain(1, 2);
		GlobalFineGrain gf2 = new GlobalFineGrain(-1, -2);

		/** global fine grain to global coarse */
		System.out.println("Expected: [GlobalCoarse: 0,1]\n  Actual: " + gf1.toGlobalCoarse());
		System.out.println("Expected: [GlobalCoarse: -1,-1]\n  Actual: " + gf2.toGlobalCoarse());

		/** global fine grain to global continuous */
		System.out.println("Expected: [GlobalContinuous: 12.0,20.0]\n  Actual: " + gf1.toGlobalContinuous());
		System.out.println("Expected: [GlobalContinuous: -4.0,-12.0]\n  Actual: " + gf2.toGlobalContinuous());

		/** global fine grain to observation relative coarse */
		System.out.println("Expected: [ObsRelCoarse: 11,12]\n  Actual: " + gf1.toObservationRelativeCoarse(player1));
		System.out.println("Expected: [ObsRelCoarse: 12,13]\n  Actual: " + gf1.toObservationRelativeCoarse(player2));
		System.out.println("Expected: [ObsRelCoarse: 10,10]\n  Actual: " + gf2.toObservationRelativeCoarse(player1));
		System.out.println("Expected: [ObsRelCoarse: 11,11]\n  Actual: " + gf2.toObservationRelativeCoarse(player2));

		/** global fine grain to observation relative fine grain */
		System.out.println("Expected: [23,24]\n  Actual: " + gf1.toObservationRelativeFineGrain(player1));
		System.out.println("Expected: [25,26]\n  Actual: " + gf1.toObservationRelativeFineGrain(player2));
		System.out.println("Expected: [21,20]\n  Actual: " + gf2.toObservationRelativeFineGrain(player1));
		System.out.println("Expected: [23,22]\n  Actual: " + gf2.toObservationRelativeFineGrain(player2));

		// ------------------------------------------------------------
		System.out.println("\nTesting observation relative coarse:");
		ObservationRelativeCoarse orc1 = new ObservationRelativeCoarse(12, 10);
		ObservationRelativeCoarse orc2 = new ObservationRelativeCoarse(-1, -2);

		/** observation relative coarse to global coarse */
		System.out.println("Expected: [GlobalCoarse: 1,-1]\n  Actual: " + orc1.toGlobalCoarse(player1));
		System.out.println("Expected: [GlobalCoarse: 0,-2]\n  Actual: " + orc1.toGlobalCoarse(player2));
		System.out.println("Expected: [GlobalCoarse: -12,-13]\n  Actual: " + orc2.toGlobalCoarse(player1));
		System.out.println("Expected: [GlobalCoarse: -13,-14]\n  Actual: " + orc2.toGlobalCoarse(player2));

		/** observation relative coarse to global continuous */
		System.out.println("Expected: [GlobalContinuous: 24.0,-8.0]\n  Actual: " + orc1.toGlobalContinuous(player1));
		System.out.println("Expected: [GlobalContinuous: 8.0,-24.0]\n  Actual: " + orc1.toGlobalContinuous(player2));
		System.out.println("Expected: [GlobalContinuous: -184.0,-200.0]\n  Actual: " + orc2.toGlobalContinuous(player1));
		System.out.println("Expected: [GlobalContinuous: -200.0,-216.0]\n  Actual: " + orc2.toGlobalContinuous(player2));

		/** observation relative coarse to global fine grain */
		System.out.println("Expected: [GlobalFineGrain: 3,-1]\n  Actual: " + orc1.toGlobalFineGrain(player1));
		System.out.println("Expected: [GlobalFineGrain: 1,-3]\n  Actual: " + orc1.toGlobalFineGrain(player2));
		System.out.println("Expected: [GlobalFineGrain: -23,-25]\n  Actual: " + orc2.toGlobalFineGrain(player1));
		System.out.println("Expected: [GlobalFineGrain: -25,-27]\n  Actual: " + orc2.toGlobalFineGrain(player2));

		/** observation relative coarse to observation relative fine grain */
		System.out.println("Expected: [ObsRelFineGrain: 25,21]\n  Actual: " + orc1.toObservationRelativeFineGrain());
		System.out.println("Expected: [ObsRelFineGrain: -1,-3]\n  Actual: " + orc2.toObservationRelativeFineGrain());

		// ------------------------------------------------------------
		System.out.println("\nTesting observation relative fine grain:");
		ObservationRelativeFineGrain orfg1 = new ObservationRelativeFineGrain(24, 19);
		ObservationRelativeFineGrain orfg2 = new ObservationRelativeFineGrain(-1, -2);

		/** observation relative fine grain to global coarse */
		System.out.println("Expected: [GlobalCoarse: 1,-2]\n  Actual: " + orfg1.toGlobalCoarse(player1));
		System.out.println("Expected: [GlobalCoarse: 0,-3]\n  Actual: " + orfg1.toGlobalCoarse(player2));
		System.out.println("Expected: [GlobalCoarse: -12,-12]\n  Actual: " + orfg2.toGlobalCoarse(player1));
		System.out.println("Expected: [GlobalCoarse: -13,-13]\n  Actual: " + orfg2.toGlobalCoarse(player2));

		/** observation relative fine grain to global continuous */
		System.out.println("Expected: [GlobalContinuous: 20.0,-20.0]\n  Actual: " + orfg1.toGlobalContinuous(player1));
		System.out.println("Expected: [GlobalContinuous: 4.0,-36.0]\n  Actual: " + orfg1.toGlobalContinuous(player2));
		System.out.println("Expected: [GlobalContinuous: -180.0,-188.0]\n  Actual: " + orfg2.toGlobalContinuous(player1));
		System.out.println("Expected: [GlobalContinuous: -196.0,-204.0]\n  Actual: " + orfg2.toGlobalContinuous(player2));

		/** observation relative fine grain to global fine grain */
		System.out.println("Expected: [GlobalFineGrain: 2,-3]\n  Actual: " + orfg1.toGlobalFineGrain(player1));
		System.out.println("Expected: [GlobalFineGrain: 0,-5]\n  Actual: " + orfg1.toGlobalFineGrain(player2));
		System.out.println("Expected: [GlobalFineGrain: -23,-24]\n  Actual: " + orfg2.toGlobalFineGrain(player1));
		System.out.println("Expected: [GlobalFineGrain: -25,-26]\n  Actual: " + orfg2.toGlobalFineGrain(player2));

		/** observation relative fine grain to observation relative coarse */
		System.out.println("Expected: [ObsRelCoarse: 12,9]\n  Actual: " + orfg1.toObservationRelativeCoarse());
		System.out.println("Expected: [ObsRelCoarse: -1,-1]\n  Actual: " + orfg2.toObservationRelativeCoarse());
	}
}
