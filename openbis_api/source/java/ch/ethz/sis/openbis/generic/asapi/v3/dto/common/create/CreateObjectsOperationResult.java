/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create;

import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult;

/**
 * @author pkupczyk
 */
public abstract class CreateObjectsOperationResult<ID extends IObjectId> implements IOperationResult
{

    private static final long serialVersionUID = 1L;

    private List<ID> objectIds;

    protected CreateObjectsOperationResult()
    {
    }

    public CreateObjectsOperationResult(List<ID> objectIds)
    {
        this.objectIds = objectIds;
    }

    public List<ID> getObjectIds()
    {
        return objectIds;
    }

    @Override
    public String getMessage()
    {
        return toString();
    }

    @Override
    public String toString()
    {
        return this.getClass().getSimpleName() + (objectIds != null ? objectIds.toString() : "none");
    }

}
