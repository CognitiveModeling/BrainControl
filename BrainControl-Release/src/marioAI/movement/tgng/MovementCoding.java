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

import java.util.ArrayList;

import marioAI.movement.tgng.Tgng.TgngType;

/**
 * used to adress the movement array in a human readable form
 * @author benjamin
 *
 */
public enum MovementCoding{
	RIGHT,
	LEFT,
	UP,
	RIGHTUP,
	LEFTUP,
	NOTMOVING,
	DUCK,
	FIRE_RIGHT,
	FIRE_LEFT,
	FIRE_UP,
	FIRE_RIGHTUP,
	FIRE_LEFTUP,
	FIRE_NOTMOVING,
	FIRE_DUCK;
	
	/**
	 * the originial movement codes
	 * need to be ordered in the same way as the enum
	 */
	private static boolean[][] movements = {
		{ false, true, false, false, false, false } , 	//right
		{ true, false, false, false, false, false } , 	//left
		{ false, false, false, true, false, false } ,	//jump
		{ false, true, false, true, false, false } ,	//jump right
		{ true, false, false, true, false, false } ,	//jump left
		{ false, false, false, false, false, false } , //stand still
		{ false, false, true, false, false, false },	//cower to floor
		{ false, true, false, false, true, false } , 	//right & fire
		{ true, false, false, false, true, false } , 	//left & fire
		{ false, false, false, true, true, false } ,	//jump & fire
		{ false, true, false, true, true, false } ,	//jump right & fire
		{ true, false, false, true, true, false } ,	//jump left & fire
		{ false, false, false, false, true, false } , //stand still & fire
		{ false, false, true, false, true, false }		//cower to floor & fire
		};

	private MovementCoding() {
	}
	
	/**
	 * returns the enum type of one movement
	 * @param objectID
	 * @return
	 */
	public static MovementCoding getMovementCoding(int objectID){
		switch (objectID){
		case 0: return RIGHT;
		case 1: return LEFT;
		case 2: return UP;
		case 3: return RIGHTUP;
		case 4: return LEFTUP;
		case 5: return NOTMOVING;
		case 6: return DUCK;
		case 7: return FIRE_RIGHT;
		case 8: return FIRE_LEFT;
		case 9: return FIRE_UP;
		case 10: return FIRE_RIGHTUP;
		case 11: return FIRE_LEFTUP;
		case 12: return FIRE_NOTMOVING;
		case 13: return FIRE_DUCK;
		default: return NOTMOVING;
		}
	}
	
	
	/**
	 * returns a random movement which is allowed in the currently used tgng type
	 * @param type
	 * @return
	 */
	public static MovementCoding getRandomMovementCoding(TgngType type){
		ArrayList<MovementCoding> notAllowedMovements = new ArrayList<MovementCoding>();
		
		// put not allowed movements in the list
		switch(type){
		case FIREBALL:
			break;
		case INTELLIGENT:
			// standing still or ducking can lead to loops
			notAllowedMovements.add(FIRE_DUCK);
			notAllowedMovements.add(FIRE_NOTMOVING);
			notAllowedMovements.add(DUCK);
			notAllowedMovements.add(NOTMOVING);
			break;
		case NON_INTELLIGENT:
			break;
		default:
			break;
		}
		MovementCoding randomMove;
		
		// pick random movements until the picked one is allowed
		do{
		randomMove = getMovementCoding((int)(Math.random() * MovementCoding.values().length));
		} while(notAllowedMovements.contains(randomMove));
		
		return randomMove;
	}

	/**
	 * converts the enum back to the original movement code
	 * @return
	 */
	public boolean[] getMovement() {
		return movements[this.ordinal()];	

	}
}

