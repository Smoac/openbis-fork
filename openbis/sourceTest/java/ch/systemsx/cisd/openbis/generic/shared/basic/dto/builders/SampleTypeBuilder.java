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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleTypePropertyType;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class SampleTypeBuilder extends AbstractEntityTypeBuilder
{
    private SampleType sampleType = new SampleType();
    
    public SampleTypeBuilder()
    {
        sampleType.setSampleTypePropertyTypes(new ArrayList<SampleTypePropertyType>());
    }
    
    public SampleTypeBuilder id(long id)
    {
        sampleType.setId(id);
        return this;
    }
    
    public SampleTypeBuilder code(String code)
    {
        sampleType.setCode(code);
        return this;
    }
    
    public SampleType getSampleType()
    {
        return sampleType;
    }
    
    public SampleTypeBuilder propertyType(String code, String label, DataTypeCode dataType)
    {
        addPropertyType(sampleType, new SampleTypePropertyType(), code, label, dataType);
        return this;
    }
    
    protected void addPropertyType(SampleType entityType, SampleTypePropertyType entityTypePropertyType, String code, String label,
            DataTypeCode type)
    {
        List<SampleTypePropertyType> types = entityType.getAssignedPropertyTypes();
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
