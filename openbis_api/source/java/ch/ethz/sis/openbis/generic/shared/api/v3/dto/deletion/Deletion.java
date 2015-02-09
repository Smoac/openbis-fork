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

package ch.ethz.sis.openbis.generic.shared.api.v3.dto.deletion;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.deletion.DeletionFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.deletion.IDeletionId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("dto.deletion.Deletion")
public class Deletion implements Serializable
{

    private static final long serialVersionUID = 1L;

    @JsonProperty
    private DeletionFetchOptions fetchOptions;

    @JsonProperty
    private IDeletionId id;

    @JsonProperty
    private String reason;

    @JsonProperty
    private List<DeletedObject> deletedObjects = new ArrayList<DeletedObject>();

    @JsonIgnore
    public DeletionFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    public void setFetchOptions(DeletionFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    @JsonIgnore
    public IDeletionId getId()
    {
        return id;
    }

    public void setId(IDeletionId id)
    {
        this.id = id;
    }

    @JsonIgnore
    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
    }

    @JsonIgnore
    public List<DeletedObject> getDeletedObjects()
    {
        if (getFetchOptions().hasDeletedObjects())
        {
            return deletedObjects;
        }
        else
        {
            throw new NotFetchedException("Deleted objects have not been fetched.");
        }
    }

    public void setDeletedObjects(List<DeletedObject> deletedObjects)
    {
        this.deletedObjects = deletedObjects;
    }

}
