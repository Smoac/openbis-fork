/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentTypePropertyType;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ExperimentTypeBuilder extends AbstractEntityTypeBuilder
{
    private ExperimentType experimentType = new ExperimentType();
    
    public ExperimentTypeBuilder()
    {
        experimentType.setExperimentTypePropertyTypes(new ArrayList<ExperimentTypePropertyType>());
    }
    
    public ExperimentTypeBuilder code(String code)
    {
        experimentType.setCode(code);
        return this;
    }
    
    public ExperimentType getExperimentType()
    {
        return experimentType;
    }
    
    public ExperimentTypeBuilder propertyType(String code, String label, DataTypeCode dataType)
    {
        addPropertyType(experimentType, new ExperimentTypePropertyType(), code, label, dataType);
        return this;
    }
    
    protected void addPropertyType(ExperimentType entityType, ExperimentTypePropertyType entityTypePropertyType, String code, String label,
            DataTypeCode type)
    {
        List<ExperimentTypePropertyType> types = entityType.getAssignedPropertyTypes();
        PropertyType propertyType = new PropertyType();
        propertyType.setCode(code);
        propertyType.setSimpleCode(code);
        propertyType.setLabel(label);
        propertyType.setDataType(new DataType(type));
        entityTypePropertyType.setPropertyType(propertyType);
        entityTypePropertyType.setEntityType(entityType);
        types.add(entityTypePropertyType);
    }
}
