/*
 * Copyright 2010 ETH Zuerich, CISD
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
public class IndistinguishableProtein extends ProteinReference
{
    private static final long serialVersionUID = 1L;

    @ResultColumn("amino_acid_sequence")
    private String sequence;

    @ResultColumn("coverage")
    private double coverage;
    
    public void setSequence(String sequence)
    {
        this.sequence = sequence;
    }

    public String getSequence()
    {
        return sequence;
    }

    public void setCoverage(double coverage)
    {
        this.coverage = coverage;
    }

    public double getCoverage()
    {
        return coverage;
    }
    
}
