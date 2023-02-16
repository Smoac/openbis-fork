/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.update;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyAssignmentId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@JsonObject("as.dto.entitytype.update.PropertyAssignmentListUpdateValue")
public class PropertyAssignmentListUpdateValue 
        extends ListUpdateValue<PropertyAssignmentCreation, IPropertyAssignmentId, PropertyAssignmentCreation, Object>
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty
    private boolean forceRemovingAssignments;

    @JsonIgnore
    public boolean isForceRemovingAssignments()
    {
        return forceRemovingAssignments;
    }

    @JsonIgnore
    public void setForceRemovingAssignments(boolean forceRemovingAssignments)
    {
        this.forceRemovingAssignments = forceRemovingAssignments;
    }
    
    
}
