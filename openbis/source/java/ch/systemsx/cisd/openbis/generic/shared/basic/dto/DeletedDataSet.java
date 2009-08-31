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
import java.util.Date;

/**
 * Describes a deleted data set.
 * 
 * @author Izabela Adamczyk
 */
public class DeletedDataSet implements Serializable
{
    private String identifier;

    private Date deletionDate;

    public DeletedDataSet(String identifier, Date deleted)
    {
        this.deletionDate = deleted;
        this.identifier = identifier;
    }

    public DeletedDataSet()
    {
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    public Date getDeletionDate()
    {
        return deletionDate;
    }

    public void setDeletionDate(Date deletionDate)
    {
        this.deletionDate = deletionDate;
    }

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

}
