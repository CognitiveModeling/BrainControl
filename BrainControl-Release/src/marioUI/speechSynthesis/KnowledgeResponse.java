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
package marioUI.speechSynthesis;

import java.util.TreeMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import marioAI.brain.Effect;
import marioAI.brain.Effect.ActionEffect;
import marioAI.brain.Generalizer;
import marioAI.collision.ConditionDirectionPair;
import marioAI.collision.ConditionDirectionPair.CollisionDirection;
import marioWorld.engine.Logging;
import marioWorld.engine.PlayerWorldObject;
import marioWorld.utils.ResetStaticInterface;

/**
 * Generate natural language responses for knowledge queries
 * 
 * @author smihael
 */

public class KnowledgeResponse implements ResetStaticInterface {

	public static TagsToSentences translator;
	private Effect effect;
	private ActionEffect consequence;
	private PlayerWorldObject effectTarget; // target of the effect
	private double probab;

	private PlayerWorldObject actor;
	private PlayerWorldObject target; // target of the interaction
	private CollisionDirection direction;

	private String responseTags;
	private String responseText;

	/**
	 * Constructor for Condition-Effect pair Parses data and extracts relevant
	 * details needed for constructing the tags
	 * 
	 * @param data
	 *            knowledge query result
	 * @param tagsmapping
	 *            tags to sentence mapping
	 */
	public KnowledgeResponse(Entry<ConditionDirectionPair, TreeMap<Effect, Double>> all_data,
			TagsToSentences tagsmapping) {
		classesWithStaticStuff.add(this.getClass());

		Entry<ConditionDirectionPair, Entry<Effect, Double>> data = new SimpleEntry<ConditionDirectionPair, Entry<Effect, Double>>(
				all_data.getKey(), all_data.getValue().firstEntry());

		if (data.getValue() == null || data.getValue().getValue() == null) {
			effect = new Effect(ActionEffect.UNDEFINED, PlayerWorldObject.UNDEFINED);
			probab = 1;
		} else {
			if (data.getValue() != null) {
				effect = data.getValue().getKey();
				probab = data.getValue().getValue();
			}
		}

		// actor = data.getKey().condition.getActor();
		// player(actor) talks about himself -> PLAYER (later "MYSELF")
		actor = PlayerWorldObject.PLAYER;
		target = Generalizer.generalizePlayerWorldObject(data.getKey().condition.getTarget());
		if (effect.getEffectTarget().isPlayer()) {
			effectTarget = PlayerWorldObject.PLAYER;
		} else effectTarget = effect.getEffectTarget();
		consequence = effect.getType();
		direction = data.getKey().collisionDirection;
		translator = tagsmapping;

		responseTags = buildTag(actor, direction, target, consequence, probab, effectTarget);
	}

	/**
	 * Constructor for direct assignment of the response tags
	 */
	/*
	 * public KnowledgeResponse(String actor, String direction, String target,
	 * String consequence, double prob, String effectTarget) { responseTags =
	 * actor+" "+direction+" "+target+" "+probabilityString(prob)+" "
	 * +consequence; }
	 */

	/**
	 * Direct for direct assignment of the response from tag
	 * 
	 * @param text
	 */
	public KnowledgeResponse(String tag) {

		classesWithStaticStuff.add(this.getClass());

		responseText = translator.getSentence(tag);
	}

	/*
	 * public KnowledgeResponse(PlayerWorldObject actor) { responseText =
	 * translator.getSentence(actor.toString()); }
	 */

	/**
	 * Builds key upon which it can be search in the tags to sentences mapping
	 * 
	 * @param actor
	 * @param direction
	 * @param target
	 * @param consequence
	 * @param prob
	 * @return key
	 */
	private String buildTag(PlayerWorldObject actor, CollisionDirection direction, PlayerWorldObject target,
			ActionEffect consequence, double prob, PlayerWorldObject effectTarget) {
		String effectTargetString = " " + effectTarget.toString();

		// an object can not destroy another object. that is why there is an "it
		// is destroyed" rule in the grammars
		// Remark: woah, this is non-generic coding.
		if (consequence.equals(ActionEffect.OBJECT_DESTRUCTION) || effectTarget.equals(PlayerWorldObject.NONE)) {
			effectTargetString = "";
		}

		// TODO: this only avoids buggy grammars ... they should use tags
		// anyway!!! (big TODO)
		if (effect.getType().equals(ActionEffect.NOTHING_HAPPENS)) {
			effectTargetString = "";
		}

		String tag = actor + " " + direction + " " + target + effectTargetString + " " + probabilityString(prob) + " "
				+ consequence;
		return tag;
	}

	private String probabilityString(double prob) {
		// Handle probabilities
		String probab;

		if (prob >= 0.75)
			probab = "CERTAINLY";
		else if (prob >= 0.5)
			probab = "PROBABLY";
		else
			probab = "MAYBE";
		return probab;
	}

	/**
	 * Returns sentence for tags that were determined from the data given in the
	 * constructor
	 */
	@Override
	public String toString() {
		Logging.logInfo("TagsToSentences", responseTags);

		if (responseText == null || responseText.isEmpty())
			return translator
					.getSentence(responseTags + " RESPONSETYPE(" + VoiceResponseType.KNOWLEDGE_RESPONSE.name() + ")");
		else
			return responseText;
	}

	public static void deleteStaticAttributes() {
		translator = null;
	}

	public static void resetStaticAttributes() {
		// TODO Auto-generated method stub

	}

}
