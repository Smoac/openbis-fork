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

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleTypePropertyType;

/**
 * @author Franz-Josef Elmer
 */
public class SampleTypeBuilder extends AbstractEntityTypeBuilder<SampleType>
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
        SampleTypePropertyType entityTypePropertyType = new SampleTypePropertyType();
        List<SampleTypePropertyType> types = sampleType.getAssignedPropertyTypes();
        fillEntityTypePropertyType(sampleType, entityTypePropertyType, code, label, dataType);
        types.add(entityTypePropertyType);
        return this;
    }
}
