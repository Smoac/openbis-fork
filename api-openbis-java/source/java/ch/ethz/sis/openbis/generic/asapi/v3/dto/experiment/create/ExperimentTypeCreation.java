/*
 * Copyright ETH 2017 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create;

import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.create.IEntityTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.IPluginId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyAssignmentCreation;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.experiment.create.ExperimentTypeCreation")
public class ExperimentTypeCreation implements IEntityTypeCreation
{

    private static final long serialVersionUID = 1L;

    private String code;

    private String description;

    private IPluginId validationPluginId;

    private List<PropertyAssignmentCreation> propertyAssignments;

    private Map<String, String> metaData;

    private boolean managedInternally;

    @Override
    public String getCode()
    {
        return code;
    }

    @Override
    public void setCode(String code)
    {
        this.code = code;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public void setDescription(String description)
    {
        this.description = description;
    }

    @Override
    public IPluginId getValidationPluginId()
    {
        return validationPluginId;
    }

    @Override
    public void setValidationPluginId(IPluginId validationPluginId)
    {
        this.validationPluginId = validationPluginId;
    }

    @Override
    public List<PropertyAssignmentCreation> getPropertyAssignments()
    {
        return propertyAssignments;
    }

    @Override
    public void setPropertyAssignments(List<PropertyAssignmentCreation> propertyAssignments)
    {
        this.propertyAssignments = propertyAssignments;
    }

    @Override
    public boolean isManagedInternally()
    {
        return managedInternally;
    }

    @Override
    public void setManagedInternally(boolean managedInternally)
    {
        this.managedInternally = managedInternally;
    }

    public Map<String, String> getMetaData()
    {
        return metaData;
    }

    public void setMetaData(Map<String, String> metaData)
    {
        this.metaData = metaData;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("code", code).toString();
    }

}
