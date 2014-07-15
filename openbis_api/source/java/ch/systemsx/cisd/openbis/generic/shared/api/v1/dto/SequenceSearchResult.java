/*
 * Copyright 2014 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import java.io.Serializable;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Result of a sequence search. Returns the code of the data set where the sequence has been found,
 * the path of the file inside the data set which has the found sequence, and the sequence identifier.
 *
 * @author Franz-Josef Elmer
 */
@JsonObject("SequenceSearchResult")
public class SequenceSearchResult implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private String sequenceDatabaseName;
    
    private String dataSetCode;
    
    private String pathInDataSet;
    
    private String sequenceIdentifier;
    
    private int positionInSequence;

    public String getSequenceDatabaseName()
    {
        return sequenceDatabaseName;
    }

    public void setSequenceDatabaseName(String sequenceDatabaseKey)
    {
        this.sequenceDatabaseName = sequenceDatabaseKey;
    }

    public String getDataSetCode()
    {
        return dataSetCode;
    }

    public void setDataSetCode(String dataSet)
    {
        this.dataSetCode = dataSet;
    }

    public String getPathInDataSet()
    {
        return pathInDataSet;
    }

    public void setPathInDataSet(String pathInDataSet)
    {
        this.pathInDataSet = pathInDataSet;
    }

    public String getSequenceIdentifier()
    {
        return sequenceIdentifier;
    }

    public void setSequenceIdentifier(String sequenceIdentifier)
    {
        this.sequenceIdentifier = sequenceIdentifier;
    }

    public int getPositionInSequence()
    {
        return positionInSequence;
    }

    public void setPositionInSequence(int positionInSequence)
    {
        this.positionInSequence = positionInSequence;
    }

    @Override
    public String toString()
    {
        return "Database: " + sequenceDatabaseName + ", Data set: " + dataSetCode + ", path: " + pathInDataSet 
                + ", identifier: [" + sequenceIdentifier + "], position: " + positionInSequence;
    }

}
