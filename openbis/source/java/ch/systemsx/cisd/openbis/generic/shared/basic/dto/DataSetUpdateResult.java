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

import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;

/**
 * Stores result of data set update.
 * 
 * @author Piotr Buczek
 */
public class DataSetUpdateResult implements ISerializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    Date modificationDate;

    List<String> parentCodes;

    public DataSetUpdateResult()
    {
    }

    public Date getModificationDate()
    {
        return modificationDate;
    }

    public void setModificationDate(Date modificationDate)
    {
        this.modificationDate = modificationDate;
    }

    public List<String> getParentCodes()
    {
        return parentCodes;
    }

    public void setParentCodes(List<String> parentCodes)
    {
        this.parentCodes = parentCodes;
    }

}
