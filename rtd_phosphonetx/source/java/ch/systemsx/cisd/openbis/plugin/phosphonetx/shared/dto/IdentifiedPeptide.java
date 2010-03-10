/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto;

import net.lemnik.eodsql.ResultColumn;


/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class IdentifiedPeptide extends AbstractDTOWithID
{
    private static final long serialVersionUID = 1L;

    @ResultColumn("sequence")
    private String sequence;
    
    @ResultColumn("charge")
    private int charge;

    public final String getSequence()
    {
        return sequence;
    }

    public final void setSequence(String sequence)
    {
        this.sequence = sequence;
    }

    public final int getCharge()
    {
        return charge;
    }

    public final void setCharge(int charge)
    {
        this.charge = charge;
    }
}
