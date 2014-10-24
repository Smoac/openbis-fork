/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.deletion;

import java.io.Serializable;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("DeletionFetchOptions")
public class DeletionFetchOptions implements Serializable
{
    private static final long serialVersionUID = 1L;

    private DeletedObjectFetchOptions allDeletedObjects;

    private DeletedObjectFetchOptions originallyDeletedObjects;

    public DeletedObjectFetchOptions fetchAllDeletedObjects()
    {
        if (allDeletedObjects == null)
        {
            allDeletedObjects = new DeletedObjectFetchOptions();
        }
        return allDeletedObjects;
    }

    public boolean hasAllDeletedObjects()
    {
        return allDeletedObjects != null;
    }

    public DeletedObjectFetchOptions fetchOriginallyDeletedObjects()
    {
        if (originallyDeletedObjects == null)
        {
            originallyDeletedObjects = new DeletedObjectFetchOptions();
        }
        return originallyDeletedObjects;
    }

    public boolean hasOriginallyDeletedObjects()
    {
        return originallyDeletedObjects != null;
    }

}