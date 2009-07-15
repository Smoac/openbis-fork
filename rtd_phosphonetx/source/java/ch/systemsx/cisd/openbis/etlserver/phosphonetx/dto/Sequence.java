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

package ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto;

import net.lemnik.eodsql.ResultColumn;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class Sequence extends AbstractDTOWithID
{
    @ResultColumn("database_id")
    private long databaseID;
    
    @ResultColumn("protein_reference_id")
    private long proteinReferenceID;
    
    @ResultColumn("amino_acid_sequence")
    private String sequence;
    
    private String checksum;

    // Used by eodsql
    @SuppressWarnings("unused")
    private Sequence()
    {
    }

    public Sequence(String sequence)
    {
        setSequence(sequence);
        calculateChecksum();
    }
    
    public final long getDatabaseID()
    {
        return databaseID;
    }

    public final void setDatabaseID(long databaseID)
    {
        this.databaseID = databaseID;
    }

    public final long getProteinReferenceID()
    {
        return proteinReferenceID;
    }

    public final void setProteinReferenceID(long proteinDescriptionID)
    {
        this.proteinReferenceID = proteinDescriptionID;
    }

    public final String getSequence()
    {
        return sequence;
    }

    public final void setSequence(String sequence)
    {
        this.sequence = sequence;
    }
    
    public void calculateChecksum()
    {
        if (sequence != null)
        {
            setChecksum(Integer.toHexString(sequence.hashCode()));
        }
    }

    public final String getChecksum()
    {
        return checksum;
    }

    public final void setChecksum(String checksum)
    {
        this.checksum = checksum;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof Sequence == false)
        {
            return false;
        }
        return ((Sequence) obj).toString().equals(toString());
    }

    @Override
    public int hashCode()
    {
        return toString().hashCode();
    }

    @Override
    public String toString()
    {
        return getDatabaseID() + "-" + getProteinReferenceID() + ":" + sequence
                + "[" + checksum + "]";
    }
    
    
}
