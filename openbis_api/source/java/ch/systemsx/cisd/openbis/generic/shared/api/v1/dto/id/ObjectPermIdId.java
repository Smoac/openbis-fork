/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * @author pkupczyk
 */
@JsonObject("ObjectPermIdId")
public abstract class ObjectPermIdId implements IObjectId
{

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String permId;

    public ObjectPermIdId(String permId)
    {
        setPermId(permId);
    }

    public String getPermId()
    {
        return permId;
    }

    //
    // JSON-RPC
    //

    protected ObjectPermIdId()
    {
    }

    private void setPermId(String permId)
    {
        if (permId == null)
        {
            throw new IllegalArgumentException("PermId cannot be null");
        }
        this.permId = permId;
    }

}
