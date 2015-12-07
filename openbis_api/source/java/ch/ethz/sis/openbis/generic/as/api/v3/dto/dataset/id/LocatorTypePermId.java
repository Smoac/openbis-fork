/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.as.api.v3.dto.dataset.id;

import ch.ethz.sis.openbis.generic.as.api.v3.dto.common.id.ObjectPermId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Locator type perm id.
 * 
 * @author pkupczyk
 */
@JsonObject("dto.dataset.id.LocatorTypeTypePermId")
public class LocatorTypePermId extends ObjectPermId implements ILocatorTypeId
{

    private static final long serialVersionUID = 1L;

    /**
     * @param permId Locator type perm id, e.g. "RELATIVE_LOCATION".
     */
    public LocatorTypePermId(String permId)
    {
        super(permId);
    }

    //
    // JSON-RPC
    //

    @SuppressWarnings("unused")
    private LocatorTypePermId()
    {
        super();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (false == (obj instanceof LocatorTypePermId))
        {
            return false;
        }
        LocatorTypePermId other = (LocatorTypePermId) obj;
        if (getPermId() == null)
        {
            if (other.getPermId() != null)
            {
                return false;
            }
        } else if (!getPermId().equals(other.getPermId()))
        {
            return false;
        }
        return true;
    }

}
