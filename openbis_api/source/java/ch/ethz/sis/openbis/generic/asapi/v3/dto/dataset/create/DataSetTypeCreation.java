/*
 * Copyright 2017 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create;

import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.create.IEntityTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.IPluginId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyAssignmentCreation;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.dataset.create.DataSetTypeCreation")
public class DataSetTypeCreation implements IEntityTypeCreation
{

    private static final long serialVersionUID = 1L;

    private DataSetKind kind = DataSetKind.PHYSICAL;

    private String code;

    private String description;

    private String mainDataSetPattern;

    private String mainDataSetPath;

    private boolean disallowDeletion = false;

    private IPluginId validationPluginId;

    private List<PropertyAssignmentCreation> propertyAssignments;

    public DataSetKind getKind()
    {
        return kind;
    }

    public void setKind(DataSetKind kind)
    {
        this.kind = kind;
    }

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

    public String getMainDataSetPattern()
    {
        return mainDataSetPattern;
    }

    public void setMainDataSetPattern(String mainDataSetPattern)
    {
        this.mainDataSetPattern = mainDataSetPattern;
    }

    public String getMainDataSetPath()
    {
        return mainDataSetPath;
    }

    public void setMainDataSetPath(String mainDataSetPath)
    {
        this.mainDataSetPath = mainDataSetPath;
    }

    public boolean isDisallowDeletion()
    {
        return disallowDeletion;
    }

    public void setDisallowDeletion(boolean disallowDeletion)
    {
        this.disallowDeletion = disallowDeletion;
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

}
