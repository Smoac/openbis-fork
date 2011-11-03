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

package ch.systemsx.cisd.openbis.plugin.proteomics.shared.dto;

import java.io.Serializable;

import net.lemnik.eodsql.ResultColumn;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ProteinReference extends AbstractDTOWithID implements Serializable
{
    private static final long serialVersionUID = 1L;

    @ResultColumn("accession_number")
    private String accessionNumber;
    
    @ResultColumn("description")
    private String description;

    public final String getAccessionNumber()
    {
        return accessionNumber;
    }

    public final void setAccessionNumber(String accessionNumber)
    {
        this.accessionNumber = accessionNumber;
    }

    public final String getDescription()
    {
        return description;
    }

    public final void setDescription(String description)
    {
        this.description = description;
    }

}