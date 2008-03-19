/*
 * Copyright 2007 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.common.db;

/**
 * Implementation of {@link ISequencerHandler} for Oracle.
 * 
 * @author Franz-Josef Elmer
 */
public class OracleSequencerHandler implements ISequencerHandler
{

    //
    // ISequencerProvider
    //

    public String getNextValueScript(String sequencer)
    {
        assert sequencer != null : "Given sequencer can not be null.";
        return "select " + sequencer + ".nextval from dual";
    }

}
