/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.update;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IObjectUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.id.IOperationExecutionId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.operation.update.OperationExecutionUpdate")
public class OperationExecutionUpdate implements IUpdate, IObjectUpdate<IOperationExecutionId>
{

    private static final long serialVersionUID = 1L;

    @JsonProperty
    private IOperationExecutionId executionId;

    @JsonProperty
    private boolean deleteSummary;

    @JsonProperty
    private boolean deleteDetails;

    @Override
    @JsonIgnore
    public IOperationExecutionId getObjectId()
    {
        return getExecutionId();
    }

    @JsonIgnore
    public IOperationExecutionId getExecutionId()
    {
        return executionId;
    }

    @JsonIgnore
    public void setExecutionId(IOperationExecutionId executionId)
    {
        this.executionId = executionId;
    }

    public void deleteSummary()
    {
        this.deleteSummary = true;
    }

    public boolean isDeleteSummary()
    {
        return deleteSummary;
    }

    public void deleteDetails()
    {
        this.deleteDetails = true;
    }

    public boolean isDeleteDetails()
    {
        return deleteDetails;
    }

}