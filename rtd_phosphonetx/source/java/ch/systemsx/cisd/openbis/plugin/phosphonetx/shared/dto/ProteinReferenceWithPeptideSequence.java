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
public class ProteinReferenceWithPeptideSequence
{
    @ResultColumn("prre_id")
    private long id;
    
    @ResultColumn("sequ_id")
    private long sequenceID;
    
    @ResultColumn("dase_id")
    private long dataSetID;
    
    @ResultColumn("amino_acid_sequence")
    private String proteinSequence;

    @ResultColumn("sequence")
    private String peptideSequence;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public long getDataSetID()
    {
        return dataSetID;
    }
    
    public void setDataSetID(long dataSetID)
    {
        this.dataSetID = dataSetID;
    }
    
    public void setSequenceID(long sequenceID)
    {
        this.sequenceID = sequenceID;
    }
    
    public long getSequenceID()
    {
        return sequenceID;
    }
    
    public String getProteinSequence()
    {
        return proteinSequence;
    }

    public void setProteinSequence(String proteinSequence)
    {
        this.proteinSequence = proteinSequence;
    }

    public String getPeptideSequence()
    {
        return peptideSequence;
    }

    public void setPeptideSequence(String peptideSequence)
    {
        this.peptideSequence = peptideSequence;
    }

    
}
