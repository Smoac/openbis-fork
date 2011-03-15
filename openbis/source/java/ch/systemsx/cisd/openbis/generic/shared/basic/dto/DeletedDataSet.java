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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Describes a deleted data set.
 * 
 * @author Izabela Adamczyk
 */
public class DeletedDataSet implements Serializable
{

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private final String identifier;

    private final long eventId;

    private final String location; // the location where the data set existed before deletion

    public static List<String> extractDataSetCodes(List<DeletedDataSet> dataSets)
    {
        List<String> result = new ArrayList<String>();
        if (dataSets != null)
        {
            for (DeletedDataSet description : dataSets)
            {
                result.add(description.getIdentifier());
            }
        }
        return result;
    }

    public DeletedDataSet(String identifier, String location, long eventId)
    {
        this.eventId = eventId;
        this.identifier = identifier;
        this.location = location;
    }

    public String getLocation()
    {
        return location;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public long getEventId()
    {
        return eventId;
    }

    @Override
    public String toString()
    {
        return "DeletedDataSet [identifier=" + identifier + "]";
    }
}
