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
package marioUI.voiceControl.input;

import java.util.LinkedList;
import java.util.List;

import edu.cmu.sphinx.frontend.BaseDataProcessor;
import edu.cmu.sphinx.frontend.Data;
import edu.cmu.sphinx.frontend.DataEndSignal;
import edu.cmu.sphinx.frontend.DataProcessingException;

/**
 * Interrupting Sphinx-4 in continuous mode
 * 
 * Solving problem adressed on:
 *  http://sourceforge.net/p/cmusphinx/discussion/sphinx4/thread/dc3470a0/
 *  http://sourceforge.net/p/cmusphinx/discussion/sphinx4/thread/3875fc39/
 *  
 * Source:
 *  http://www.timvasil.com/blog14/post/2011/05/14/Interrupting-the-Sphinx-4-speech-recognizers-blocking-call.aspx#comment
 * 
 * @author modified by smihael
 *
 */
public class MicrophoneBlocker extends BaseDataProcessor
{
    List<Data> insertionDatas = new LinkedList<Data>();

    @Override
    public Data getData() throws DataProcessingException
    {
        if (!insertionDatas.isEmpty())
        {
            insertionDatas.remove(0);
            throw new DataProcessingException("interupted");
                       
        }
        return getPredecessor().getData();
    }

    public void injectInterrupt()
    {
        insertionDatas.add(new DataEndSignal(0));  

     }
}
